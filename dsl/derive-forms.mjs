// Derive: the form tables of pl-ru and en-ru as typed Kotlin rows (R1.12, R1.16). A table is the forms of the lexicon (courses-biome/<pair>/*.core-data) laid out under the feature
// bundle that asks for each, so a fact is authored once: a noun's seven cases are in nouns.core-data and nowhere else. The rules of pl-ru are those of scripts/build-pack.mjs, which read
// a course.json that no longer exists (the past tense: stem and ending by person, number and gender; the future: the auxiliary and the lemma); those of en-ru are scripts/build-pack-en.mjs's, which read lang/en/lexicon.json (the scripts left the tree in R1.13 and R1.17; `git show 7358aeb72ab79dce80fca1faadba6be64892fa18:scripts/build-pack-en.mjs`).
// It returns the text and writes nothing: `biome generate` writes OUT (generate) or compares it with the file (--check). The rows are printed by `biome data generate`, not by a printer of this file.
import { spawnSync } from 'node:child_process';
import { mkdtempSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, relative, resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const rowsDir = (pair) => join(root, `kotlin/shared/src/commonMain/generated/kotlin/polski/data/rows/${pair}/forms/FormRows.kt`);
export const OUT_PLRU = rowsDir('plru');
export const OUT_ENRU = rowsDir('enru');

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

/**
 * English has no declension, so the forms are the fields of the rows. "you" takes the plural of the verb ("you are", "you were"): the first person singular reads `presentSg1` and the third
 * `present3sg`, and everything else `presentPl`; the past is `past` for the first and third persons singular and `pastPl` for the rest. Only "be" has the fields; the others fall back to the lemma and `past`.
 */
const enPresent = (verb, person, number) => person === 3 && number === 'sg' ? verb.present3sg : person === 1 && number === 'sg' ? verb.presentSg1 ?? verb.lemma : verb.presentPl ?? verb.lemma;
const enPast = (verb, person, number) => number === 'pl' || person === 2 ? verb.pastPl ?? verb.past : verb.past;
/** One entry for each number and person, in that order, under the tense `tense`. */
const cells = (verb, tense, form) => NUMBERS.flatMap((number) => PERSONS.map((person) => ({ bundle: { Tense: tense, Person: String(person), Number: number }, form: form(verb, person, number) })));
const presentCells = (verb) => cells(verb, 'Pres', enPresent);

/**
 * What the construction slots ask a verb for: the present by person and number (`Tense=Pres`), then the bundles that collapse: `{Tense: Past}` (the past by person is folded into `enPast`),
 * `{Tense: Fut}` (the bare lemma: "will" carries the future), `{Aspect: Continuous}` (the -ing form) and the empty bundle (the do-support of a negation: the bare lemma again). The matrix of the app
 * (MatrixTables.kt) reads the same `verb:<id>` through the lowercase ids of the model (`present`, `past`, `future`: "will" and the lemma), one entry for each cell; the two shapes do not clash,
 * since a bundle with another value is another key.
 */
const verbEnglish = (verb) => [
  ...presentCells(verb),
  { bundle: { Tense: 'Past' }, form: enPast(verb, 3, 'sg') }, { bundle: { Tense: 'Fut' }, form: verb.lemma }, { bundle: { Aspect: 'Continuous' }, form: verb.ing }, { bundle: {}, form: verb.lemma },
  ...NUMBERS.flatMap((number) => PERSONS.flatMap((person) => ['present', 'past', 'future'].map((tense) => ({
    bundle: { Tense: tense, Person: String(person), Number: number }, form: tense === 'present' ? enPresent(verb, person, number) : tense === 'past' ? enPast(verb, person, number) : `will ${verb.lemma}` })))),
];
/** "do" and "be" are also auxiliaries: their present, and the past that the polarity and the question collapse to. */
const auxiliaryEnglish = (verb) => [...presentCells(verb), { bundle: { Tense: 'Past' }, form: enPast(verb, 3, 'sg') }];

/**
 * The rows of the six files (arrays of the records of the contract) as `{ 'noun:wife': [{ bundle, form }] }`: nouns by number; adjectives, possessives, pronouns (the subject form) and the negation "not" in the
 * empty bundle; prepositions by role (`Case`); verbs and auxiliaries as above. "will" has no past, hence no paradigm of its own: it is the invariable `aux:will` of the future, one entry for each person and number.
 * "not" has no row (a function word, like the closed class of pl-ru): its form is the lexeme `neg:not` that the constant slots of the recipes name.
 */
export function enruForms({ nouns, adjectives, verbs, possessives, pronouns, prepositions }) {
  const forms = {};
  for (const noun of nouns) forms[`noun:${noun.id}`] = NUMBERS.map((number) => ({ bundle: { Number: number }, form: noun.forms[number] }));
  for (const adjective of adjectives) forms[`adjective:${adjective.id}`] = [{ bundle: {}, form: adjective.forms.invariant }];
  for (const possessive of possessives) {
    if (possessive.forms.kind !== 'invariant') throw new Error(`derive-forms enru: the possessive "${possessive.id}" is declined, and English possessives are not`);
    forms[`possessive:${possessive.id}`] = [{ bundle: {}, form: possessive.forms.value }];
  }
  for (const pronoun of pronouns) forms[`pronoun:${pronoun.id}`] = [{ bundle: {}, form: pronoun.forms.Subj }];
  forms['neg:not'] = [{ bundle: {}, form: 'not' }];
  for (const preposition of prepositions) forms[`prep:${preposition.id}`] = preposition.forms.map(({ role, form }) => ({ bundle: { Case: role }, form }));
  for (const verb of verbs.filter((v) => v.past !== undefined)) {
    if (verb.ing === undefined) throw new Error(`derive-forms enru: the verb "${verb.id}" has a past and no ing form`);
    forms[`verb:${verb.id}`] = verbEnglish(verb);
    if (verb.id === 'do' || verb.id === 'be') forms[`aux:${verb.id}`] = auxiliaryEnglish(verb);
  }
  const will = verbs.find((verb) => verb.id === 'will');
  if (!will) throw new Error('derive-forms enru: no verb "will" among the verbs');
  forms['aux:will'] = cells(will, 'Fut', (v) => v.lemma);
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

/**
 * `context` is what `biome generate` passes: the running CLI, node and env, and the files named after OUT in the step. They are read with `biome read` as arrays of rows (`names` says which, in order), laid out by `build`
 * and printed as the rows of dsl/forms.data-contract in the package `pkg` by a nested `biome data generate`.
 */
async function derive({ cli, node = process.execPath, env = process.env, generateArgs = [] }, name, names, build, pkg) {
  if (!cli) throw new Error('derive-forms: no context.cli; run it with biome generate');
  if (generateArgs.length !== names.length) throw new Error(`derive-forms ${name}: the files ${names.join(', ')}, in that order`);
  const files = generateArgs.map((file) => JSON.parse(biome(node, cli, env, ['read', resolve(file), '--format', 'json'], root)).map((row) => row.value));
  const dir = mkdtempSync(join(tmpdir(), `forms-${name}-`));
  try {
    writeFileSync(join(dir, 'forms.core-data'), formRows(build(files)));
    biome(node, cli, env, ['data', 'generate', '--project', join(import.meta.dirname, 'biome.project'), '--contract', 'forms', '--rows', 'FormLexemeRow', join(dir, 'forms.core-data'),
      '--target', 'kt', '--kt-package', pkg, '--out', join(dir, 'FormRows.kt')], root);
    const [, ...rest] = readFileSync(join(dir, 'FormRows.kt'), 'utf8').split('\n');
    const sources = generateArgs.map((file) => relative(root, resolve(file))).join(', ');
    return [`// Generated from dsl/forms.data-contract, derived by dsl/derive-forms.mjs from ${sources}`, ...rest].join('\n');
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
}

/** The step `forms-plru`: the files nouns, adjectives, verbs, possessives and morphology (the one row of morphology.core-data). */
export const plru = (context = {}) => derive(context, 'plru', ['nouns', 'adjectives', 'verbs', 'possessives', 'morphology'],
  ([nouns, adjectives, verbs, possessives, [morphology]]) => plruForms({ nouns, adjectives, verbs, possessives, morphology }), 'polski.data.rows.plru.forms');
/** The step `forms-enru`: the files nouns, adjectives, verbs, possessives, personalPronouns and prepositions. */
export const enru = (context = {}) => derive(context, 'enru', ['nouns', 'adjectives', 'verbs', 'possessives', 'pronouns', 'prepositions'],
  ([nouns, adjectives, verbs, possessives, pronouns, prepositions]) => enruForms({ nouns, adjectives, verbs, possessives, pronouns, prepositions }), 'polski.data.rows.enru.forms');
