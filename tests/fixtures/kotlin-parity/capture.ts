import type { Gender, GramCase, NumberGram, Person, PossessiveId, RatingName, Tense } from '../../../src/types';
import { nouns } from '../../../src/data/nouns';
import { adjectives } from '../../../src/data/adjectives';
import { verbs } from '../../../src/data/verbs';
import { personalPronouns, possessives, possessiveForm } from '../../../src/data/pronouns';
import { adjectiveForm, normalize, nounForm, nounPhrase, verbForm } from '../../../src/grammar/engine';
import { caseSentence, generateChain, generateForSkill, sentenceSeeds } from '../../../src/training/generator';
import { skills } from '../../../src/training/skills';
import { evaluate } from '../../../src/training/evaluator';
import { nextSkillId } from '../../../src/training/queue';
import { deserialize, isDue, newSkillCard, preview, review, serialize } from '../../../src/srs/scheduler';
import { exportProgress, freshProgress, importProgress, loadProgress, localDay, saveProgress } from '../../../src/progress/storage';
import { recordReview } from '../../../src/progress/review';

export const sourceRevision = 'df59774e153a5bdf590fe27bd8780c7bd5457a26';
export const nowIso = '2026-02-03T12:00:00.000Z';
export const timeZone = 'Europe/Warsaw';
const cases: GramCase[] = ['nom', 'gen', 'dat', 'acc', 'inst', 'loc', 'voc'];
const numbers: NumberGram[] = ['sg', 'pl'];
const ratings: RatingName[] = ['again', 'hard', 'good', 'easy'];

export interface FixtureCase {
  id: string;
  input: Record<string, unknown>;
  expected: unknown;
  context?: Record<string, unknown>;
}
export interface FixtureFile { schemaVersion: 1; sourceRevision: string; cases: FixtureCase[] }
const fixture = (items: FixtureCase[]): FixtureFile => ({ schemaVersion: 1, sourceRevision, cases: items });
const add = (items: FixtureCase[], id: string, input: FixtureCase['input'], expected: unknown, context?: FixtureCase['context']) =>
  items.push({ id, input, expected, ...(context ? { context } : {}) });

/** Restores both globals even when a source call throws. The draw count is part of the fixture contract. */
function controlled<T>(fn: () => T, instant = nowIso, randomDraws: number[] = []): { value: T; drawsUsed: number } {
  const RealDate = Date;
  const random = Math.random;
  let drawsUsed = 0;
  class FixedDate extends RealDate {
    constructor(...args: [string | number | Date] | []) {
      if (args.length === 0) super(instant);
      else super(...args);
    }
    static now() { return new RealDate(instant).getTime(); }
  }
  try {
    globalThis.Date = FixedDate as DateConstructor;
    Math.random = () => {
      if (drawsUsed >= randomDraws.length) throw new Error('Unexpected Math.random draw');
      return randomDraws[drawsUsed++];
    };
    return { value: fn(), drawsUsed };
  } finally {
    globalThis.Date = RealDate;
    Math.random = random;
  }
}

function exercise(value: ReturnType<typeof generateForSkill>) {
  if (!value.id) throw new Error('Empty generated exercise ID');
  return { ...value, id: '<generated-id>' };
}

function grammar(): FixtureFile {
  const items: FixtureCase[] = [];
  for (const noun of nouns) for (const number of numbers) for (const gramCase of cases)
    add(items, `G-NOUN-${noun.id}-${number}-${gramCase}`, { nounId: noun.id, number, gramCase },
      { lemma: noun.lemma, meaning: noun.meaning, gender: noun.gender, form: nounForm(noun.id, gramCase, number) });
  for (const adjective of adjectives) for (const number of numbers) for (const gender of Object.keys(adjective.forms[number]) as Gender[]) for (const gramCase of cases)
    add(items, `G-ADJ-${adjective.id}-${number}-${gender}-${gramCase}`, { adjectiveId: adjective.id, number, gender, gramCase },
      { lemma: adjective.lemma, meaning: adjective.meaning, form: adjectiveForm(adjective.id, gender, gramCase, number) });
  for (const verb of verbs) for (const tense of ['present', 'past', 'future'] as Tense[]) {
    if (tense === 'present' && verb.aspect === 'perfective') continue;
    for (const number of numbers) for (const person of [1, 2, 3] as Person[]) for (const gender of (tense === 'past' ? ['m-personal', 'f', 'n', 'm-animate'] : ['m-personal']) as Gender[]) {
      add(items, `G-VERB-${verb.id}-${tense}-${person}-${number}-${gender}`, { verbId: verb.id, tense, person, number, gender },
        { lemma: verb.lemma, meaning: verb.meaning, aspect: verb.aspect, futureType: verb.futureType, perfectivePair: verb.perfectivePair ?? null, form: verbForm(verb.id, tense, person, number, gender) });
    }
  }
  for (const verbId of ['buyDone', 'doDone']) {
    let error: { kind: string; message: string } | undefined;
    try { verbForm(verbId, 'present', 1, 'sg'); }
    catch (cause) {
      if (cause instanceof Error) error = { kind: cause.name, message: cause.message };
      else throw cause;
    }
    if (!error) throw new Error(`Expected perfective present rejection for ${verbId}`);
    add(items, `G-VERB-${verbId}-present-rejected`,
      { verbId, tense: 'present', person: 1, number: 'sg', gender: 'm-personal' },
      { error }, { sourceSymbol: 'verbForm' });
  }
  for (const [id, forms] of Object.entries(personalPronouns)) add(items, `G-PRON-${id}`, { pronounId: id }, forms);
  for (const owner of possessives) for (const number of numbers) for (const gender of ['m-personal', 'm-animate', 'm-inanimate', 'f', 'n'] as Gender[]) for (const gramCase of cases)
    add(items, `G-POSS-${owner.id}-${number}-${gender}-${gramCase}`, { owner: owner.id, number, gender, gramCase },
      { label: owner.label, form: possessiveForm(owner.id, gender, number, gramCase) });
  for (const noun of nouns) for (const number of numbers) for (const gramCase of cases) {
    const adjective = adjectives[(nouns.indexOf(noun)) % adjectives.length];
    const owner: PossessiveId = noun.gender === 'm-personal' ? 'their' : 'my';
    const opts = { adjectiveId: adjective.id, possessive: owner, number };
    add(items, `G-PHRASE-${noun.id}-${number}-${gramCase}`, { nounId: noun.id, ...opts, gramCase }, nounPhrase(noun.id, gramCase, opts));
    add(items, `G-SENTENCE-${noun.id}-${number}-${gramCase}`, { seed: { nounId: noun.id, adjectiveId: adjective.id }, owner, number, gramCase },
      caseSentence({ nounId: noun.id, adjectiveId: adjective.id }, gramCase, owner, number));
  }
  for (const skill of skills) add(items, `S-${skill.id}`, { skillId: skill.id }, skill);
  return fixture(items);
}

function exercises(): FixtureFile {
  const items: FixtureCase[] = [];
  sentenceSeeds.forEach((seed, index) => {
    const draws = [0.11, 0.22, 0.33, 0.44, 0.55];
    const generated = controlled(() => generateChain(seed), nowIso, draws);
    if (generated.drawsUsed !== 5) throw new Error('Chain draw count changed');
    if (new Set(generated.value.map(item => item.id)).size !== 5) throw new Error('Duplicate generated exercise ID');
    generated.value.forEach((item, step) => {
      if (step > 0 && generated.value[step - 1].expected !== item.source) throw new Error('Broken chain link');
      add(items, `C-${String(index + 1).padStart(2, '0')}-${String(step + 1).padStart(2, '0')}`,
        { seed, step: step + 1 }, exercise(item), { randomDraws: [draws[step]], drawsUsed: 1 });
    });
  });
  for (const skill of skills) {
    const preferred = sentenceSeeds.find(seed => seed.nounId === 'wife')!;
    for (const variant of ['preferred', 'fallback', 'random'] as const) {
      const seed = variant === 'preferred' ? preferred : variant === 'fallback' ? { nounId: 'window', adjectiveId: 'new' } : undefined;
      const draws = [0.34, 0.72, 0.18];
      const result = controlled(() => generateForSkill(skill.id, seed), nowIso, draws);
      add(items, `E-${skill.id}-${variant}`, { skillId: skill.id, preferred: seed ?? null },
        { exercise: exercise(result.value), drawsUsed: result.drawsUsed }, { randomDraws: draws, nowIso });
    }
  }
  for (const [skillId, seed] of [['agreement.my', preferredSeed('wife')], ['verb.future', preferredSeed('husband')], ['aspect', preferredSeed('book')], ['mixed', preferredSeed('wife')]] as const) {
    const result = controlled(() => generateForSkill(skillId, seed), nowIso, [0.2, 0.4, 0.6]);
    add(items, `E-${skillId}-accepted`, { skillId, preferred: seed }, { exercise: exercise(result.value), drawsUsed: result.drawsUsed }, { nowIso, randomDraws: [0.2, 0.4, 0.6] });
  }
  return fixture(items);
}
function preferredSeed(nounId: string) { return sentenceSeeds.find(seed => seed.nounId === nounId)!; }

function evaluation(): FixtureFile {
  const items: FixtureCase[] = [];
  const ex = controlled(() => generateForSkill('mixed', preferredSeed('wife')), nowIso, [0.3]).value;
  const examples = [
    ['exact', ex.expected], ['accepted', ex.accepted![0]], ['case', ex.expected.toLocaleUpperCase('pl-PL')],
    ['whitespace', `  ${ex.expected.replaceAll(' ', '   ')}  `], ['punctuation', ex.expected.replace('.', '?!')],
    ['decomposed', ex.expected.normalize('NFD')], ['no-diacritics', 'Nie widziales mojej pieknej zony.'],
    ['empty', ''], ['wrong', 'Widzę mojego dobrego kolegę.'],
  ] as const;
  for (const [variant, answer] of examples) add(items, `A-${variant}`, { answer, exercise: exercise(ex) },
    { normalized: normalize(answer), result: evaluate(answer, ex) }, { locale: 'pl-PL' });
  return fixture(items);
}

function scheduler(): FixtureFile {
  const items: FixtureCase[] = [];
  const initial = controlled(() => newSkillCard('case.acc.f')).value;
  let learning = controlled(() => review(initial, 'again')).value;
  let reviewState = controlled(() => review(initial, 'easy')).value;
  let relearning = controlled(() => review(reviewState, 'again'), '2026-02-10T12:00:00.000Z').value;
  const states = { New: initial, Learning: learning, Review: reviewState, Relearning: relearning };
  for (const [state, card] of Object.entries(states)) for (const rating of ratings) {
    const instant = state === 'Relearning' ? '2026-02-10T12:00:00.000Z' : nowIso;
    const context = { nowIso: instant, timeZone, fsrsParameters: { request_retention: 0.9, maximum_interval: 3650, enable_fuzz: true, enable_short_term: true, learning_steps: ['1m', '10m'], relearning_steps: ['10m'] } };
    const result = controlled(() => ({ preview: dates(preview(card)), review: review(card, rating), due: isDue(card), roundTrip: serialize(deserialize(card.card)) }), instant).value;
    add(items, `F-${state}-${rating}`, { card, rating }, result, context);
  }
  for (const [variant, card, instant] of [
    ['same-day', learning, '2026-02-03T12:01:00.000Z'],
    ['overdue', reviewState, '2026-04-03T12:00:00.000Z'],
    ['short-step', relearning, '2026-02-10T12:01:00.000Z'],
    ['fuzz', reviewState, '2026-02-10T12:00:00.000Z'],
    ['cap', { ...reviewState, card: { ...reviewState.card, stability: 999999 } }, '2026-03-03T12:00:00.000Z'],
  ] as const) {
    const result = controlled(() => ({ preview: dates(preview(card)), review: review(card, 'good'), due: isDue(card) }), instant).value;
    add(items, `F-${variant}`, { card, rating: 'good' }, result, { nowIso: instant, timeZone });
  }
  return fixture(items);
}
function dates(value: ReturnType<typeof preview>) { return Object.fromEntries(Object.entries(value).map(([rating, date]) => [rating, date.toISOString()])); }

function progress(): FixtureFile {
  const items: FixtureCase[] = [];
  const base = controlled(freshProgress).value;
  add(items, 'P-fresh', {}, base, { nowIso, timeZone });
  const oral = controlled(() => recordReview(base, 'case.acc.f', 'good')).value;
  add(items, 'P-oral-good', { progress: base, skillId: 'case.acc.f', rating: 'good' }, oral, { nowIso, timeZone });
  const typed = controlled(() => recordReview(base, 'case.acc.f', 'good', false)).value;
  add(items, 'P-typed-wrong', { progress: base, skillId: 'case.acc.f', rating: 'good', typedCorrect: false }, typed, { nowIso, timeZone });
  const queue = structuredClone(base);
  queue.cards[0].card.due = '2099-01-01T00:00:00.000Z';
  queue.cards[1].card.due = '2000-01-01T00:00:00.000Z';
  add(items, 'P-queue', { progress: queue }, { automatic: nextSkillId(queue), focused: nextSkillId(queue, queue.cards[0].skillId) });
  const legacy = structuredClone(base);
  legacy.cards = legacy.cards.slice(0, 1);
  delete legacy.stats[skills[1].id];
  const storage = makeStorage();
  storage.setItem('polski-grammar-srs-v1', JSON.stringify(legacy));
  add(items, 'P-missing-skills', { stored: legacy }, withStorage(storage, () => controlled(loadProgress).value), { nowIso, timeZone });
  for (const [id, raw] of [['malformed', '{'], ['unsupported-version', '{"version":99,"cards":[],"stats":{},"lastDay":"2026-02-03","reviewsToday":0,"totalReviews":0}']] as const) {
    const store = makeStorage();
    store.setItem('polski-grammar-srs-v1', raw);
    add(items, `P-load-${id}`, { storedRaw: raw }, withStorage(store, () => controlled(loadProgress).value), { nowIso, timeZone });
  }
  const roundTripStorage = makeStorage();
  const roundTrip = withStorage(roundTripStorage, () => controlled(() => { saveProgress(oral); return loadProgress(); }).value);
  add(items, 'P-save-load', { progress: oral }, roundTrip, { nowIso, timeZone });
  const exported = exportProgress(oral);
  add(items, 'P-export-import', { progress: oral, exported }, importProgress(exported));
  for (const [id, raw] of [['invalid-json', '{'], ['missing-fields', '{"version":1}'], ['unsupported-version', '{"version":99,"cards":[],"stats":{}}']] as const) {
    let expected: unknown;
    try { expected = importProgress(raw); } catch (error) { expected = { error: { kind: (error as Error).name, message: (error as Error).message } }; }
    add(items, `P-import-${id}`, { raw }, expected);
  }
  const badStorage = { getItem: () => { throw new Error('storage unavailable'); }, setItem: () => { throw new Error('storage unavailable'); }, removeItem: () => {} };
  add(items, 'P-storage-read-error', {}, withStorage(badStorage, () => controlled(loadProgress).value), { nowIso, timeZone });
  for (const [id, instant, zone] of [
    ['before-midnight-warsaw', '2026-02-03T22:59:59.000Z', 'Europe/Warsaw'],
    ['after-midnight-warsaw', '2026-02-03T23:00:01.000Z', 'Europe/Warsaw'],
    ['dst-before', '2026-03-28T22:59:59.000Z', 'Europe/Warsaw'],
    ['dst-after', '2026-03-29T22:00:01.000Z', 'Europe/Warsaw'],
    ['same-instant-utc', '2026-02-03T23:00:01.000Z', 'UTC'],
  ] as const) {
    const old = process.env.TZ;
    try { process.env.TZ = zone; add(items, `P-day-${id}`, { instant, zone }, controlled(() => localDay(), instant).value, { nowIso: instant, timeZone: zone }); }
    finally { if (old === undefined) delete process.env.TZ; else process.env.TZ = old; }
  }
  for (const [id, firstNowIso, secondNowIso, firstDay] of [
    ['winter', '2026-02-03T22:59:59.000Z', '2026-02-03T23:00:01.000Z', '2026-02-03'],
    ['dst', '2026-03-29T21:59:59.000Z', '2026-03-29T22:00:01.000Z', '2026-03-29'],
  ] as const) {
    const initial = structuredClone(base);
    initial.lastDay = firstDay;
    initial.reviewsToday = 4;
    initial.totalReviews = 9;
    initial.stats['case.acc.f'] = { reviews: 3, correct: 2, mistakes: 1, streak: 2 };
    const snapshot = structuredClone(initial);
    const first = controlled(() => recordReview(initial, 'case.acc.f', 'good'), firstNowIso).value;
    const second = controlled(() => recordReview(first, 'case.acc.f', 'good'), secondNowIso).value;
    if (JSON.stringify(initial) !== JSON.stringify(snapshot)) throw new Error('recordReview mutated its input');
    add(items, `P-review-midnight-${id}`,
      { progress: snapshot, skillId: 'case.acc.f', rating: 'good', firstNowIso, secondNowIso },
      { first, second, initialAfter: initial },
      { timeZone, sourceSymbol: 'recordReview' });
  }
  return fixture(items);
}
function makeStorage() {
  const values = new Map<string, string>();
  return { getItem: (key: string) => values.get(key) ?? null, setItem: (key: string, value: string) => { values.set(key, value); }, removeItem: (key: string) => { values.delete(key); } };
}
function withStorage<T>(storage: Pick<Storage, 'getItem' | 'setItem' | 'removeItem'>, fn: () => T): T {
  const prior = globalThis.localStorage;
  try { Object.defineProperty(globalThis, 'localStorage', { configurable: true, value: storage }); return fn(); }
  finally { Object.defineProperty(globalThis, 'localStorage', { configurable: true, value: prior }); }
}

export const capture = () => {
  const old = process.env.TZ;
  try {
    process.env.TZ = timeZone;
    return { grammar: grammar(), exercises: exercises(), evaluation: evaluation(), scheduler: scheduler(), progress: progress() };
  } finally {
    if (old === undefined) delete process.env.TZ;
    else process.env.TZ = old;
  }
};
