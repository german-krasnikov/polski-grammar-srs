// Plans/Kotlin/EnRuPackPlan.md §1.3/§5 (gap B, "not a gap") / EN-24: materializes en verb forms
// from `courses/lang/en/lexicon.json` into `courses/lang/en/forms.generated.json` — the same
// build-time table-lookup artifact `scripts/build-pack.mjs` produces for pl-ru (from a different
// source shape), consumed at runtime by :core-engine's `TableMorphology`. English needs no
// declension rules: `lexicon.json`'s flat verb fields (`present3sg`/`presentSg1`/`presentPl`/
// `past`/`pastPl`) already ARE the whole paradigm (English distinguishes person/number only for
// "be" and 3rd-singular present) — this script only expands that flat data into the
// Tense×Person×Number `FeatureBundle` shape `TableMorphology`/`ConstructionRealizer` expect.
//
// The modal auxiliary "will" is excluded: it has no past/person paradigm of its own (its only use
// is as the invariant future-tense marker already inlined by `futureForm` below), so a
// `verb:will` table entry would have no real forms to hold.
//
// Usage: node scripts/build-pack-en.mjs           (writes courses/lang/en/forms.generated.json)
//        node scripts/build-pack-en.mjs --check   (fails if the checked-in file is stale)
import { readFileSync, writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';

const root = new URL('../', import.meta.url);
const readJson = (relativePath) => JSON.parse(readFileSync(new URL(relativePath, root), 'utf8'));
const CHECK = process.argv.includes('--check');

const PERSONS = [1, 2, 3];
const NUMBERS = ['sg', 'pl'];

// "you" collapses to the plural form (real English fact: "you" historically takes the same verb
// form as "we/they", both present and past — "you are/were", never "you am/was"), matching how
// `presentPl`/`pastPl` already fold m-personal vs "other" plural for pl's own possessives.
function presentForm(verb, person, number) {
  if (person === 3 && number === 'sg') return verb.present3sg;
  if (person === 1 && number === 'sg') return verb.presentSg1 ?? verb.lemma;
  return verb.presentPl ?? verb.lemma;
}

function pastForm(verb, person, number) {
  if (number === 'pl' || person === 2) return verb.pastPl ?? verb.past;
  return verb.past;
}

// English never inflects "will" itself by person/number ("I/you/he will walk") — the future is
// always "will" + the bare lemma, one invariant form per verb.
function futureForm(verb) {
  return `will ${verb.lemma}`;
}

function verbEntries(verb) {
  const entries = [];
  for (const number of NUMBERS) {
    for (const person of PERSONS) {
      entries.push({ bundle: { Tense: 'present', Person: String(person), Number: number }, form: presentForm(verb, person, number) });
      entries.push({ bundle: { Tense: 'past', Person: String(person), Number: number }, form: pastForm(verb, person, number) });
      entries.push({ bundle: { Tense: 'future', Person: String(person), Number: number }, form: futureForm(verb) });
    }
  }
  return entries;
}

function buildForms(lexicon) {
  const forms = {};
  for (const verb of lexicon.verbs) {
    if (verb.past === undefined) continue; // "will": no paradigm of its own, see module comment.
    forms[`verb:${verb.id}`] = verbEntries(verb);
  }
  return forms;
}

function main() {
  const lexicon = readJson('courses/lang/en/lexicon.json');
  const forms = buildForms(lexicon);
  const output = { schemaVersion: 1, generatedFrom: 'courses/lang/en/lexicon.json', forms };
  const content = JSON.stringify(output, null, 2) + '\n';
  const target = new URL('../courses/lang/en/forms.generated.json', import.meta.url);
  if (CHECK) {
    let existing;
    try {
      existing = readFileSync(target, 'utf8');
    } catch {
      throw new Error('courses/lang/en/forms.generated.json missing — run: node scripts/build-pack-en.mjs');
    }
    if (existing !== content) throw new Error('courses/lang/en/forms.generated.json is stale — run: node scripts/build-pack-en.mjs');
    console.log('courses/lang/en/forms.generated.json is up to date.');
  } else {
    writeFileSync(target, content, 'utf8');
    console.log(`Wrote ${fileURLToPath(target)} (${Object.keys(forms).length} verbs).`);
  }
}

main();
