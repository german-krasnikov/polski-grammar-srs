import { expect, test } from 'vitest';
import course from '../courses/pl-ru/course.json';
import { validateCoursePack } from '../scripts/validate-course.mjs';
import frequency from '../courses/pl-ru/frequency-top1000.json';
import { coursePronounTeaching, coursePronounContextValue } from '../src/data/course';
import { nounPhrase } from '../src/grammar/engine';
import { possessives } from '../src/data/pronouns';

test('course pack owns the ordered pronoun teaching copy and demo', () => {
  const teaching = (course.reference as unknown as { pronounTeaching?: unknown }).pronounTeaching;
  expect(teaching).toMatchObject({
    personal: {
      title: 'Личные местоимения в конструкциях',
      pronounIds: ['ja', 'ty', 'on', 'ona', 'ono', 'my', 'wy', 'oni', 'one'],
    },
    contexts: [
      { id: 'gen', caseId: 'gen' }, { id: 'dat', caseId: 'dat' },
      { id: 'acc', caseId: 'acc' }, { id: 'inst', caseId: 'inst' }, { id: 'loc', caseId: 'loc' },
    ],
    possessive: { demo: { nounId: 'wife', adjectiveId: 'beautiful', number: 'sg' } },
  });
});

test('all nine personal rows and seven owner demos retain the exact Polish forms', () => {
  expect(coursePronounTeaching.personal.intro).toEqual({
    react: 'Вместо отдельной формы запоминай её место в предложении. После предлогов у местоимений третьего лица появляется n-.',
    compact: 'После предлогов у местоимений третьего лица появляется n-.',
  });
  expect(coursePronounTeaching.personal.footer).toEqual({
    react: 'Здесь обычные безударные формы. Для ударения или противопоставления: mnie, tobie, jego, jemu. После предлога: do niego, do niej, do nich, dla mnie, dla ciebie.',
    web: 'Здесь обычные безударные формы. После предлога: do niego, do niej, do nich, dla mnie, dla ciebie.',
    native: 'После предлога: do niego, do niej, do nich, dla mnie, dla ciebie.',
  });
  expect(coursePronounTeaching.contexts.map(row => [row.cue.full, row.cue.ios, row.caseName])).toEqual([
    ['Nie widzę…', 'Nie widzę', 'Dopełniacz'], ['Daję prezent…', 'Daję prezent', 'Celownik'],
    ['Widzę…', 'Widzę', 'Biernik'], ['Idę z…', 'Idę', 'Narzędnik'], ['Mówię o…', 'Mówię o', 'Miejscownik'],
  ]);
  expect(coursePronounTeaching.possessive.demo.cases.map(row => [row.caption.web, row.caption.desktop, row.caption.ios])).toEqual([
    ['Mianownik', 'Mianownik', 'Mianownik'],
    ['Widzę… · Biernik', 'Biernik', 'Biernik'],
    ['Nie widzę… · Dopełniacz', 'Dopełniacz', 'Dopełniacz'],
  ]);
  const expected = [
    ['mnie', 'mi', 'mnie', 'ze mną', 'o mnie'],
    ['ciebie', 'ci', 'ciebie', 'z tobą', 'o tobie'],
    ['go', 'mu', 'go', 'z nim', 'o nim'],
    ['jej', 'jej', 'ją', 'z nią', 'o niej'],
    ['go', 'mu', 'je', 'z nim', 'o nim'],
    ['nas', 'nam', 'nas', 'z nami', 'o nas'],
    ['was', 'wam', 'was', 'z wami', 'o was'],
    ['ich', 'im', 'ich', 'z nimi', 'o nich'],
    ['ich', 'im', 'je', 'z nimi', 'o nich'],
  ];
  expect(coursePronounTeaching.personal.pronounIds.map(id =>
    coursePronounTeaching.contexts.map(context => coursePronounContextValue(id, context)))).toEqual(expected);
  const demo = coursePronounTeaching.possessive.demo;
  expect(possessives.map(owner => demo.cases.map(row => nounPhrase(demo.nounId, row.id as 'nom'|'acc'|'gen',
    { adjectiveId: demo.adjectiveId, number: 'sg', possessive: owner.id })))).toEqual([
    ['moja piękna żona', 'moją piękną żonę', 'mojej pięknej żony'],
    ['twoja piękna żona', 'twoją piękną żonę', 'twojej pięknej żony'],
    ['jego piękna żona', 'jego piękną żonę', 'jego pięknej żony'],
    ['jej piękna żona', 'jej piękną żonę', 'jej pięknej żony'],
    ['nasza piękna żona', 'naszą piękną żonę', 'naszej pięknej żony'],
    ['wasza piękna żona', 'waszą piękną żonę', 'waszej pięknej żony'],
    ['ich piękna żona', 'ich piękną żonę', 'ich pięknej żony'],
  ]);
});

test.each([
  ['reordered pronoun', (sample: typeof course) => { sample.reference.pronounTeaching.personal.pronounIds.reverse(); }, /pronounIds\/0/],
  ['unknown pronoun', (sample: typeof course) => { sample.reference.pronounTeaching.personal.pronounIds[0] = 'unknown'; }, /pronounIds\/0/],
  ['wrong context', (sample: typeof course) => { sample.reference.pronounTeaching.contexts[0].caseId = 'acc'; }, /contexts\/0\/caseId/],
  ['reordered context', (sample: typeof course) => { sample.reference.pronounTeaching.contexts.reverse(); }, /contexts\/0\/id/],
  ['missing iOS cue', (sample: typeof course) => { delete (sample.reference.pronounTeaching.contexts[0].cue as { ios?: string }).ios; }, /contexts\/0\/cue\/ios/],
  ['wrong locative prefix', (sample: typeof course) => { sample.reference.pronounTeaching.contexts[4].valuePrefix = 'z '; }, /contexts\/4\/valuePrefix/],
  ['missing instrumental override', (sample: typeof course) => { delete (sample.reference.pronounTeaching.contexts[3] as { specialValues?: unknown }).specialValues; }, /contexts\/3\/specialValues/],
  ['blank text', (sample: typeof course) => { sample.reference.pronounTeaching.personal.intro.react = ' '; }, /personal\/intro\/react/],
  ['HTML text', (sample: typeof course) => { sample.reference.pronounTeaching.personal.footer.web = '<b>bad</b>'; }, /personal\/footer\/web/],
  ['missing noun', (sample: typeof course) => { sample.reference.pronounTeaching.possessive.demo.nounId = 'missing'; }, /demo\/nounId/],
  ['missing adjective', (sample: typeof course) => { sample.reference.pronounTeaching.possessive.demo.adjectiveId = 'missing'; }, /demo\/adjectiveId/],
  ['missing demo case', (sample: typeof course) => { sample.reference.pronounTeaching.possessive.demo.cases.pop(); }, /demo\/cases/],
  ['duplicate owner', (sample: typeof course) => { sample.reference.pronounTeaching.possessive.demo.invariableOwnerIds[1] = 'his'; }, /invariableOwnerIds\/1/],
] as const)('rejects %s in pronoun teaching', (_name, mutate, path) => {
  const sample = structuredClone(course);
  mutate(sample);
  expect(() => validateCoursePack(sample, frequency)).toThrow(path);
});

test('course validator rejects missing pronoun teaching', () => {
  const sample = structuredClone(course);
  delete (sample.reference as { pronounTeaching?: unknown }).pronounTeaching;
  expect(() => validateCoursePack(sample, frequency)).toThrow(/reference\/pronounTeaching/);
});
