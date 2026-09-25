import { readFileSync, writeFileSync } from 'node:fs';

const fixturePath = new URL('../tests/fixtures/kotlin-parity/grammar.json', import.meta.url);
const coursePath = new URL('../courses/pl-ru/course.json', import.meta.url);
const fixture = JSON.parse(readFileSync(fixturePath, 'utf8'));
const course = JSON.parse(readFileSync(coursePath, 'utf8'));
const owners = ['my', 'your', 'his', 'her', 'our', 'yourPlural', 'their'];
const genders = ['m-personal', 'm-animate', 'm-inanimate', 'f', 'n'];
const cases = ['nom', 'gen', 'dat', 'acc', 'inst', 'loc', 'voc'];
const declined = new Set(['my', 'your', 'our', 'yourPlural']);
const output = new Map(owners.map(id => [id, declined.has(id) ? {kind: 'declined', sg: {}, pl: {'m-personal': {}, other: {}}} : {kind: 'invariant'}]));
const seen = new Set();
for (const item of fixture.cases.filter(row => row.id.startsWith('G-POSS-'))) {
  const {owner, number, gender, gramCase} = item.input;
  if (!owners.includes(owner) || !['sg', 'pl'].includes(number) || !genders.includes(gender) || !cases.includes(gramCase)) throw new Error(item.id);
  const key = `${owner}/${number}/${gender}/${gramCase}`;
  if (seen.has(key)) throw new Error(`duplicate ${key}`);
  seen.add(key);
  const forms = output.get(owner);
  const value = item.expected.form;
  if (forms.kind === 'invariant') {
    if (forms.value && forms.value !== value) throw new Error(`inconsistent ${key}`);
    forms.value = value;
  } else {
    const bucket = number === 'sg' ? (forms.sg[gender] ??= {}) : forms.pl[gender === 'm-personal' ? 'm-personal' : 'other'];
    if (bucket[gramCase] && bucket[gramCase] !== value) throw new Error(`inconsistent ${key}`);
    bucket[gramCase] = value;
  }
}
if (seen.size !== 490) throw new Error(`expected 490 possessive cases, got ${seen.size}`);
for (const id of declined) {
  const forms = output.get(id);
  for (const gender of genders) for (const gramCase of cases) if (!forms.sg[gender]?.[gramCase]) throw new Error(`missing ${id}/sg/${gender}/${gramCase}`);
  for (const group of ['m-personal', 'other']) for (const gramCase of cases) if (!forms.pl[group][gramCase]) throw new Error(`missing ${id}/pl/${group}/${gramCase}`);
}
const futureAuxiliary = {verbId: 'be', forms: {sg: {}, pl: {}}};
for (const number of ['sg', 'pl']) for (const person of [1, 2, 3]) {
  const id = `G-VERB-be-future-${person}-${number}-m-personal`;
  const item = fixture.cases.find(row => row.id === id);
  if (!item) throw new Error(`missing ${id}`);
  futureAuxiliary.forms[number][person] = item.expected.form;
}
const extracted = {sourceRevision: fixture.sourceRevision, possessives: Object.fromEntries(output), morphology: {futureAuxiliary}};
const mode = process.argv[2];
if (mode === '--write') {
  for (const row of course.possessives) row.forms = output.get(row.id);
  course.morphology = {futureAuxiliary};
  writeFileSync(coursePath, `${JSON.stringify(course, null, 2)}\n`);
} else if (mode === '--check') {
  for (const row of course.possessives) if (JSON.stringify(row.forms) !== JSON.stringify(output.get(row.id))) throw new Error(`/${row.id}: differs from ${fixture.sourceRevision}`);
  if (JSON.stringify(course.morphology) !== JSON.stringify(extracted.morphology)) throw new Error(`/morphology: differs from ${fixture.sourceRevision}`);
} else if (!mode) {
  process.stdout.write(`${JSON.stringify(extracted, null, 2)}\n`);
} else throw new Error('usage: node scripts/extract-course-morphology.mjs [--write|--check]');
