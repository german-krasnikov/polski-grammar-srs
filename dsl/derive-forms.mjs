// Derive: the form table of pl-ru as typed Kotlin rows (R1.12). The table is the forms of the lexicon (courses-biome/pl-ru/{nouns,adjectives,verbs,possessives,morphology}.core-data)
// laid out under the feature bundle that asks for each, so a fact is authored once: a noun's seven cases are in nouns.core-data and nowhere else. The rules are those of
// scripts/build-pack.mjs, which read a course.json that no longer exists (the past tense: stem and ending by person, number and gender; the future: the auxiliary and the lemma).
// It returns the text and writes nothing: `biome generate` writes OUT (generate) or compares it with the file (--check). The rows are printed by `biome data generate`, not by a printer of this file.
import { spawnSync } from 'node:child_process';
import { mkdtempSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, relative, resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
export const OUT_PLRU = join(root, 'kotlin/shared/src/commonMain/generated/kotlin/polski/data/rows/plru/forms/FormRows.kt');

const CASES = ['nom', 'gen', 'dat', 'acc', 'inst', 'loc', 'voc'];
const NUMBERS = ['sg', 'pl'];
const GENDERS = ['m-personal', 'm-animate', 'm-inanimate', 'f', 'n']; // the ids of the model; the keys of the rows are m_personal.. (an identifier): key()
const PERSONS = [1, 2, 3];                                            // p1..p3 in the rows
const PAST_GENDERS = ['m-personal', 'f', 'n', 'm-animate'];           // the genders a verb is asked for in the past: m-inanimate never is
const key = (gender) => gender.replace(/-/g, '_');

const nounEntries = (noun) => NUMBERS.flatMap((number) => CASES.map((gramCase) => ({ bundle: { Number: number, Case: gramCase }, form: noun.forms[number][gramCase] })));
const adjectiveEntries = (adjective) => NUMBERS.flatMap((number) => GENDERS.flatMap((gender) => CASES.map((gramCase) => ({
  bundle: { Number: number, Gender: gender, Case: gramCase }, form: adjective.forms[number][key(gender)][gramCase] }))));
/** An invariable possessive is one text everywhere; a declined one has the singular by gender and, in the plural, the masculine personal group and all the rest. */
const possessiveEntries = ({ forms }) => NUMBERS.flatMap((number) => GENDERS.flatMap((gender) => CASES.map((gramCase) => ({
  bundle: { Number: number, Gender: gender, Case: gramCase },
  form: forms.kind === 'invariant' ? forms.value : number === 'sg' ? forms.sg[key(gender)][gramCase] : forms.pl[gender === 'm-personal' ? 'm_personal' : 'other'][gramCase] }))));

/** Stem by number, then gender; ending by person and number, short for the feminine and the neuter. */
function pastForm(verb, person, number, gender) {
  const short = gender === 'f' || gender === 'n';
  const ending = number === 'sg'
    ? person === 1 ? (short ? 'm' : 'em') : person === 2 ? (short ? 'ś' : 'eś') : ''
    : person === 1 ? 'śmy' : person === 2 ? 'ście' : '';
  const stem = number === 'pl' ? (gender === 'm-personal' ? verb.pastStem.mp : verb.pastStem.np)
    : gender === 'f' ? verb.pastStem.f : gender === 'n' ? verb.pastStem.n ?? verb.pastStem.m : verb.pastStem.m;
  return stem + ending;
}

/** Only an imperfective verb has a present. The future is the present for a verb of that type, the auxiliary alone for the auxiliary itself, else the auxiliary and the lemma. */
function verbEntries(verb, auxiliary) {
  const present = (number, person) => verb.present[number][`p${person}`];
  const aspectual = verb.aspect === 'imperfective'
    ? NUMBERS.flatMap((number) => PERSONS.map((person) => ({ bundle: { Tense: 'present', Person: String(person), Number: number }, form: present(number, person) }))) : [];
  const past = NUMBERS.flatMap((number) => PERSONS.flatMap((person) => PAST_GENDERS.map((gender) => ({
    bundle: { Tense: 'past', Person: String(person), Number: number, Gender: gender }, form: pastForm(verb, person, number, gender) }))));
  const future = NUMBERS.flatMap((number) => PERSONS.map((person) => {
    const aux = auxiliary.forms[number][`p${person}`];
    const form = verb.futureType === 'present' ? present(number, person) : verb.id === auxiliary.verbId ? aux : `${aux} ${verb.lemma}`;
    return { bundle: { Tense: 'future', Person: String(person), Number: number }, form };
  }));
  return [...aspectual, ...past, ...future];
}

/** The rows of the five files (arrays of the records of the contract, `morphology` the one row of morphology.core-data) as `{ 'noun:wife': [{ bundle, form }] }`. */
export function plruForms({ nouns, adjectives, verbs, possessives, morphology }) {
  const forms = {};
  for (const noun of nouns) forms[`noun:${noun.id}`] = nounEntries(noun);
  for (const adjective of adjectives) forms[`adjective:${adjective.id}`] = adjectiveEntries(adjective);
  for (const possessive of possessives) forms[`possessive:${possessive.id}`] = possessiveEntries(possessive);
  for (const verb of verbs) forms[`verb:${verb.id}`] = verbEntries(verb, morphology.futureAuxiliary);
  return forms;
}

/** The text of a value in the notation of Kit: a text in double quotes, a record `{k: v}`, a list `[a b]`. */
const text = (v) => typeof v === 'string' ? JSON.stringify(v) : Array.isArray(v) ? `[${v.map(text).join(' ')}]` : `{${Object.entries(v).map(([k, x]) => `${k}: ${text(x)}`).join(' ')}}`;

/** One row FORMS for each lexeme: `plruForms` as the rows of forms.data-contract. */
export const formRows = (forms) => Object.entries(forms).map(([lexeme, entries]) =>
  `FORMS ${text({ lexeme, forms: entries.map(({ bundle, form }) => ({ bundle: Object.entries(bundle).map(([feature, value]) => ({ feature, value })), form })) })}\n`).join('');

function biome(node, cli, env, args, cwd) {
  const run = spawnSync(node, [cli, ...args], { cwd, env, encoding: 'utf8', timeout: 120_000 });
  if (run.status !== 0) throw new Error(`biome ${args.slice(0, 2).join(' ')} failed (${run.status ?? run.error?.message}): ${run.stderr?.trim()}`);
  return run.stdout;
}

/** `context` is what `biome generate` passes: the running CLI, node and env, and the five files named after OUT in the step, in the order nouns, adjectives, verbs, possessives, morphology. */
export async function plru({ cli, node = process.execPath, env = process.env, generateArgs = [] } = {}) {
  if (!cli) throw new Error('derive-forms: no context.cli; run it with biome generate');
  if (generateArgs.length !== 5) throw new Error('derive-forms plru: the files nouns, adjectives, verbs, possessives and morphology, in that order');
  const files = generateArgs.map((file) => JSON.parse(biome(node, cli, env, ['read', file, '--format', 'json'], root)).map((row) => row.value));
  const [nouns, adjectives, verbs, possessives, [morphology]] = files; // morphology.core-data is one row
  const dir = mkdtempSync(join(tmpdir(), 'forms-plru-'));
  try {
    writeFileSync(join(dir, 'forms.core-data'), formRows(plruForms({ nouns, adjectives, verbs, possessives, morphology })));
    biome(node, cli, env, ['data', 'generate', '--project', join(import.meta.dirname, 'biome.project'), '--contract', 'forms', '--rows', 'FormLexemeRow', join(dir, 'forms.core-data'),
      '--target', 'kt', '--kt-package', 'polski.data.rows.plru.forms', '--out', join(dir, 'FormRows.kt')], root);
    const [, ...rest] = readFileSync(join(dir, 'FormRows.kt'), 'utf8').split('\n');
    const sources = generateArgs.map((file) => relative(root, resolve(file))).join(', ');
    return [`// Generated from dsl/forms.data-contract, derived by dsl/derive-forms.mjs from ${sources}`, ...rest].join('\n');
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
}
