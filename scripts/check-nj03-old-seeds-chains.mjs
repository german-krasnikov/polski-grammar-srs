// Development-only read of locked old course inputs and their v2 projections.
// This is a static source/fixture consistency check, not candidate or host execution.
import { strict as assert } from 'node:assert';
import { createHash } from 'node:crypto';
import { readFileSync } from 'node:fs';

const root = new URL('../', import.meta.url);
const defaultCases = new URL('../Plans/Biome/NJ03SeedsNounsChains.cases', import.meta.url);
const casesPath = process.argv.length === 2 ? defaultCases : process.argv.length === 4 && process.argv[2] === '--cases' ? process.argv[3] : null;
if (!casesPath) throw new Error('usage: node scripts/check-nj03-old-seeds-chains.mjs [--cases path]');

const sources = new Map();
const rows = { NOUN: [], SEED: [], FIRST: [], STEP: [], PREFIX: [], PATTERN: [], CHAIN: [] };
const allowedSources = new Map([
  ['courses/pl-ru/course.json', 'AUTHORED'],
  ['courses/lang/pl/exercise-recipes.json', 'AUTHORED'],
  ['courses/lang/pl/lexicon.json', 'DERIVED'],
  ['courses/pairs/pl-ru/pair.json', 'DERIVED'],
]);
for (const [index, raw] of readFileSync(casesPath, 'utf8').split('\n').entries()) {
  const line = raw.trim();
  if (!line || line.startsWith('#')) continue;
  const parts = line.split(' | ');
  const label = `case line ${index + 1}`;
  if (parts[0] === 'SOURCE') {
    assert.equal(parts.length, 4, `${label}: source arity`);
    const [, role, path, sha256] = parts;
    assert.equal(role, allowedSources.get(path), `${label}: exact path/role`);
    assert(!sources.has(path), `${label}: duplicate source`);
    assert(/^[a-f0-9]{64}$/.test(sha256), `${label}: source SHA-256`);
    sources.set(path, sha256);
  } else {
    assert(Object.hasOwn(rows, parts[0]), `${label}: unknown row kind`);
    rows[parts[0]].push(parts);
  }
}
assert.equal(sources.size, allowedSources.size, 'all four old-source/projection identities required');

function readLocked(path) {
  const bytes = readFileSync(new URL(path, root));
  assert.equal(createHash('sha256').update(bytes).digest('hex'), sources.get(path), `${path}: complete old bytes changed`);
  return JSON.parse(bytes.toString('utf8'));
}

const course = readLocked('courses/pl-ru/course.json');
const recipe = readLocked('courses/lang/pl/exercise-recipes.json');
const lexicon = readLocked('courses/lang/pl/lexicon.json');
const pair = readLocked('courses/pairs/pl-ru/pair.json');
const formCases = ['nom', 'gen', 'dat', 'acc', 'inst', 'loc', 'voc'];

// The v2 files are projections of the old v1 authority, never independent expectations.
for (const [name, original, projection] of [
  ['nouns', course.nouns, lexicon.nouns],
  ['sentenceSeeds', course.sentenceSeeds, pair.sentenceSeeds],
  ['exerciseCopy', course.exerciseCopy, pair.exerciseCopy],
  ['exercisePatterns', course.exercisePatterns, pair.exercisePatterns],
  ['chainPresentation', course.training.chainPresentation, pair.training.chainPresentation],
  ['reference.chainRows', course.reference.chainRows, pair.reference.chainRows],
]) assert.deepEqual(projection, original, `v2 ${name} must match old v1 authority`);

assert.equal(rows.NOUN.length, 14, '14 frozen noun rows');
assert.equal(course.nouns.length, rows.NOUN.length, '14 old nouns');
for (const [index, noun] of course.nouns.entries()) {
  const expected = rows.NOUN[index];
  assert.equal(expected.length, 8, `noun row ${index + 1}: arity`);
  assert.equal(expected[1], String(index + 1).padStart(2, '0'), `noun row ${index + 1}: position`);
  assert.deepEqual(Object.keys(noun), ['id', 'lemma', 'meaning', 'gender', 'forms'], `noun row ${index + 1}: fields`);
  assert.deepEqual(Object.keys(noun.forms), ['sg', 'pl'], `noun row ${index + 1}: number order`);
  for (const number of ['sg', 'pl']) assert.deepEqual(Object.keys(noun.forms[number]), formCases, `noun row ${index + 1}: ${number} case order`);
  assert.deepEqual([noun.id, noun.lemma, noun.meaning, noun.gender], expected.slice(2, 6), `noun row ${index + 1}: identity/Unicode`);
  assert.deepEqual(formCases.map((name) => noun.forms.sg[name]), expected[6].split(','), `noun row ${index + 1}: singular forms`);
  assert.deepEqual(formCases.map((name) => noun.forms.pl[name]), expected[7].split(','), `noun row ${index + 1}: plural forms`);
}

assert.equal(rows.SEED.length, 12, '12 frozen seed rows');
assert.equal(course.sentenceSeeds.length, rows.SEED.length, '12 old seeds');
for (const [index, seed] of course.sentenceSeeds.entries()) {
  const expected = rows.SEED[index];
  assert.equal(expected.length, 4, `seed ${index + 1}: arity`);
  assert.equal(expected[1], String(index + 1).padStart(2, '0'), `seed ${index + 1}: position`);
  assert.deepEqual(Object.keys(seed), ['nounId', 'adjectiveId'], `seed ${index + 1}: fields`);
  assert.deepEqual([seed.nounId, seed.adjectiveId], expected.slice(2), `seed ${index + 1}: IDs`);
  assert(course.nouns.some((noun) => noun.id === seed.nounId), `seed ${index + 1}: noun reference`);
  assert(course.adjectives.some((adjective) => adjective.id === seed.adjectiveId), `seed ${index + 1}: adjective reference`);
}

assert.deepEqual(rows.FIRST, [
  ['FIRST', 'f', 'case.acc.f'], ['FIRST', 'n', 'case.acc.n'], ['FIRST', 'default', 'case.acc.m'],
], 'frozen first-skill cases');
assert.deepEqual(Object.entries(recipe.firstSkillByGender), rows.FIRST.map(([, gender, skill]) => [gender, skill]), 'authored first-skill map');
assert.equal(rows.STEP.length, 5, 'five frozen chain steps');
assert.equal(recipe.chain.length, rows.STEP.length, 'five authored recipe steps');
assert.equal(course.training.chainPresentation.steps.length, rows.STEP.length, 'five authored presentation steps');
for (const [index, step] of recipe.chain.entries()) {
  const expected = rows.STEP[index];
  const presentation = course.training.chainPresentation.steps[index];
  const sourceKey = step.source.type === 'prefixed' ? 'nomSg' : step.source.key;
  assert.deepEqual(expected, ['STEP', String(index + 1).padStart(2, '0'), presentation.id, presentation.label,
    step.skillId ?? 'gender', sourceKey, step.expected.key, step.tags.join(','), step.ownerOut,
    step.promptKey, step.explanationKey], `step ${index + 1}: authored order, keys, tags and owner`);
  assert(Object.hasOwn(course.exerciseCopy, step.promptKey), `step ${index + 1}: prompt reference`);
  assert(Object.hasOwn(course.exerciseCopy, step.explanationKey), `step ${index + 1}: explanation reference`);
  if (index === 0) {
    assert.equal(step.source.type, 'prefixed', 'first source is nominative sentence');
    assert.equal(step.source.prefixCase, 'nom', 'first source uses nominative prefix');
  } else {
    assert.equal(step.source.key, recipe.chain[index - 1].expected.key, `step ${index + 1}: authored source link`);
  }
}

assert.deepEqual(rows.PREFIX, [['PREFIX', 'nomSg', course.caseSentencePrefixes.nomSg]], 'nominative sentence prefix');
const patternKeys = ['seenAcc', 'chainPast', 'chainNeg', 'chainOwner', 'chainLoc'];
assert.deepEqual(rows.PATTERN, patternKeys.map((key) => ['PATTERN', key, course.exercisePatterns[key]]), 'five authored chain patterns');

const nounById = new Map(course.nouns.map((noun) => [noun.id, noun]));
const adjectiveById = new Map(course.adjectives.map((adjective) => [adjective.id, adjective]));
const my = course.possessives.find((owner) => owner.id === 'my');
const their = course.possessives.find((owner) => owner.id === 'their');
assert.equal(my.forms.kind, 'declined', 'authored my paradigm');
assert.equal(their.forms.kind, 'invariant', 'authored invariant their paradigm');
assert.equal(their.forms.value, 'ich', 'authored invariant ich value');

function phrase(seed, gramCase, owner) {
  const noun = nounById.get(seed.nounId);
  const adjective = adjectiveById.get(seed.adjectiveId);
  const ownerForm = owner === 'their' ? their.forms.value : my.forms.sg[noun.gender][gramCase];
  return [ownerForm, adjective.forms.sg[noun.gender][gramCase], noun.forms.sg[gramCase]].join(' ');
}

function pattern(key, slot, value) {
  const source = course.exercisePatterns[key];
  const marker = `{${slot}}`;
  assert(source.includes(marker) && source.indexOf(marker) === source.lastIndexOf(marker), `${key}: exactly one expected slot`);
  return source.replace(marker, value);
}

assert.equal(rows.CHAIN.length, 60, '12 × 5 frozen source/expected rows');
for (const [seedIndex, seed] of course.sentenceSeeds.entries()) {
  const noun = nounById.get(seed.nounId);
  const slots = {
    acc: phrase(seed, 'acc', 'my'), gen: phrase(seed, 'gen', 'my'),
    theirGen: phrase(seed, 'gen', 'their'), theirLoc: phrase(seed, 'loc', 'their'),
  };
  const expectedSentences = [
    pattern('seenAcc', 'acc', slots.acc), pattern('chainPast', 'acc', slots.acc),
    pattern('chainNeg', 'gen', slots.gen), pattern('chainOwner', 'theirGen', slots.theirGen),
    pattern('chainLoc', 'theirLoc', slots.theirLoc),
  ];
  const initialSource = `${course.caseSentencePrefixes.nomSg} ${phrase(seed, 'nom', 'my')}.`;
  for (let stepIndex = 0; stepIndex < 5; stepIndex += 1) {
    const source = stepIndex === 0 ? initialSource : expectedSentences[stepIndex - 1];
    const skill = stepIndex === 0
      ? recipe.firstSkillByGender[noun.gender] ?? recipe.firstSkillByGender.default
      : recipe.chain[stepIndex].skillId;
    assert.deepEqual(rows.CHAIN[seedIndex * 5 + stepIndex], [
      'CHAIN', String(seedIndex + 1).padStart(2, '0'), String(stepIndex + 1).padStart(2, '0'),
      skill, source, expectedSentences[stepIndex],
    ], `seed ${seedIndex + 1} step ${stepIndex + 1}: old-authored source projection`);
  }
}
assert.deepEqual(course.reference.chainRows.map(({ from, to }) => [from, to]),
  rows.CHAIN.slice(0, 5).map(([, , , , source, expected]) => [source, expected]),
  'independently authored wife reference chain');

process.stdout.write('PASS NJ03 old source/fixture consistency: 4 locked files, 14/14 nouns, 12/12 seeds, 5/5 steps, 60/60 source/expected rows; candidate executions 0, hosts NOT RUN\n');
