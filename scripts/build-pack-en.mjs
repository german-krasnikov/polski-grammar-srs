// Plans/Kotlin/EnRuPackPlan.md §1.3/§5 (gap B) / EN-24 / EnRuAcceptance-2026-09-28.md §7 item 1
// (ADR-37 blocker 1): materializes EVERY en form `:core-engine`'s `ConstructionRealizer` can
// query — noun/adjective/possessive/pronoun/preposition/negation/verb/auxiliary — from
// `courses/lang/en/lexicon.json` + `courses/lang/en/prepositions.json` into
// `courses/lang/en/forms.generated.json`, the same build-time table-lookup artifact
// `scripts/build-pack.mjs` produces for pl-ru (from a different source shape), consumed at
// runtime by `:core-engine`'s `TableMorphology`.
//
// The FeatureBundle shapes below are not guessed: they are exactly the (category, requiredFeatures)
// pairs `courses/lang/en/realization.json`'s construction templates declare for each slot category
// (`noun`: Number; `adjective`/`possessive`/`pronoun`/`neg`: none, i.e. one invariant form; `prep`:
// Case; `verb`/`aux`: Tense+Person+Number, collapsing to Tense alone at Tense=Past/Fut and to
// Aspect alone at Aspect=Continuous, per `core.verb.tense`/`core.sentence.polarity`'s own
// `requiredFeaturesWhen`) — verified against `EnExerciseGeneratorTest`'s hand-authored fixture
// morphology table, which already proves this exact shape realizes all 16 `lang/en` skills.
// English needs no declension rules: `lexicon.json`'s flat verb fields (`present3sg`/`presentSg1`/
// `presentPl`/`past`/`pastPl`/`ing`) already ARE the whole paradigm (English distinguishes
// person/number only for "be" and 3rd-singular present).
//
// "will" (lexicon.verbs, no `past` field) has no past/person paradigm of its own — its only use is
// the invariant future-tense `aux:will` marker, one form repeated across every Person/Number at
// Tense=Fut (`core.verb.tense`'s `aux` slot requires the full bundle there, no override). "not"
// (`neg:not`) is a closed-class function word with no lexicon entry of its own (mirroring how
// `scripts/build-pack.mjs` hand-writes pl's own closed-class functors) — its one invariant form is
// the literal English word already named by `courses/lang/en/exercise-recipes.json`'s own
// `constantSlots` entry (`{"slot":"neg","lexeme":"not",...}`), not an invented fact.
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

// The full present×person×number paradigm one verb-shaped lexeme (a real verb, or "do"/"be" also
// read as `aux:*`) contributes — the entries `core.verb.tense`/`core.sentence.polarity`'s `verb`
// and `aux` slots both query at their un-collapsed default bundle.
function presentEntries(verb) {
  const entries = [];
  for (const number of NUMBERS) for (const person of PERSONS) {
    entries.push({ bundle: { Tense: 'Pres', Person: String(person), Number: number }, form: presentForm(verb, person, number) });
  }
  return entries;
}

// A verb slot's collapsed forms: `Tense=Past`→just `{Tense}` (one form, English regular/irregular
// past does not vary by person except "be", already folded into `pastForm` above), `Tense=Fut`→
// just `{Tense}` (the bare lemma; "will" itself carries the future), `Aspect=Continuous`→just
// `{Aspect}` (the -ing form), and the empty bundle (`Polarity=Neg`'s do-support collapse — the bare
// lemma again, since "do"/"does"/"did" alone carries tense/person there).
function collapsedVerbEntries(verb) {
  return [
    { bundle: { Tense: 'Past' }, form: pastForm(verb, 3, 'sg') },
    { bundle: { Tense: 'Fut' }, form: verb.lemma },
    { bundle: { Aspect: 'Continuous' }, form: verb.ing },
    { bundle: {}, form: verb.lemma },
  ];
}

// EN-24's `MatrixTables.kt#enVerbForm` reads the SAME `verb:<id>` key through a different,
// older bundle shape — `polski.model.Tense`'s lowercase ids (`present`/`past`/`future`), one
// person/number-varying entry per cell, future's own form already carrying "will " (that table
// has no separate aux column). Kept verbatim (not migrated to the `ConstructionRealizer` shape
// above) so `MatrixTablesTest`'s pinned expectations — a working, shipped, tested feature —
// stay byte-identical; `TableMorphology` is a flat (lexeme, bundle) map, so both shapes coexist
// under the same `verb:<id>` key without conflict (different Tense value casing = different keys).
function legacyMatrixEntries(verb) {
  const entries = [];
  for (const number of NUMBERS) for (const person of PERSONS) {
    entries.push({ bundle: { Tense: 'present', Person: String(person), Number: number }, form: presentForm(verb, person, number) });
    entries.push({ bundle: { Tense: 'past', Person: String(person), Number: number }, form: pastForm(verb, person, number) });
    entries.push({ bundle: { Tense: 'future', Person: String(person), Number: number }, form: `will ${verb.lemma}` });
  }
  return entries;
}

function verbEntries(verb) {
  return [...presentEntries(verb), ...collapsedVerbEntries(verb), ...legacyMatrixEntries(verb)];
}

// `aux:do`/`aux:be` need the same present paradigm plus the past collapse (`core.sentence.polarity`/
// `core.sentence.mood`'s `aux` slot also collapses to just `{Tense}` at Tense=Past) — no future/
// continuous/empty-bundle entry, since those bundle shapes are never requested for `aux`.
function auxEntries(verb) {
  return [...presentEntries(verb), { bundle: { Tense: 'Past' }, form: pastForm(verb, 3, 'sg') }];
}

function buildForms(lexicon, prepositions) {
  const forms = {};
  for (const noun of lexicon.nouns) {
    forms[`noun:${noun.id}`] = [
      { bundle: { Number: 'sg' }, form: noun.forms.sg },
      { bundle: { Number: 'pl' }, form: noun.forms.pl },
    ];
  }
  for (const adjective of lexicon.adjectives) forms[`adjective:${adjective.id}`] = [{ bundle: {}, form: adjective.forms.invariant }];
  for (const possessive of lexicon.possessives) forms[`possessive:${possessive.id}`] = [{ bundle: {}, form: possessive.forms.value }];
  for (const [id, forms_] of Object.entries(lexicon.personalPronouns)) forms[`pronoun:${id}`] = [{ bundle: {}, form: forms_.Subj }];
  forms['neg:not'] = [{ bundle: {}, form: 'not' }];
  for (const prep of prepositions.prepositions) {
    forms[`prep:${prep.id}`] = Object.entries(prep.forms).map(([caseId, form]) => ({ bundle: { Case: caseId }, form }));
  }
  for (const verb of lexicon.verbs) {
    if (verb.past === undefined) continue; // "will": no paradigm of its own, see module comment.
    forms[`verb:${verb.id}`] = verbEntries(verb);
    if (verb.id === 'do' || verb.id === 'be') forms[`aux:${verb.id}`] = auxEntries(verb);
  }
  const will = lexicon.verbs.find((verb) => verb.id === 'will');
  forms['aux:will'] = NUMBERS.flatMap((number) => PERSONS.map((person) => ({ bundle: { Tense: 'Fut', Person: String(person), Number: number }, form: will.lemma })));
  return forms;
}

function main() {
  const lexicon = readJson('courses/lang/en/lexicon.json');
  const prepositions = readJson('courses/lang/en/prepositions.json');
  const forms = buildForms(lexicon, prepositions);
  const output = { schemaVersion: 1, generatedFrom: 'courses/lang/en/lexicon.json + courses/lang/en/prepositions.json', forms };
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
    console.log(`Wrote ${fileURLToPath(target)} (${Object.keys(forms).length} lexemes).`);
  }
}

main();
