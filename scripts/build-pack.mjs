// UniversalCorePlan.md §5.1/§5.3/§8 UC-05 (partial): materializes noun/adjective/verb/possessive
// forms from `courses/pl-ru/course.json` into `forms.generated.json` — a flat, build-time table
// consumed at runtime by :core-engine's `TableMorphology` (`fun form(lexeme, bundle): String`,
// a lookup, never a rule). GrammarEngine.kt stays the live path in this task; this script's job is
// only to prove the same forms can be produced as data. Every rule below (past-tense stem/suffix
// selection, future auxiliary/periphrasis, possessive invariant-vs-declined, plural m-personal vs.
// "other") is a straight port of `kotlin/shared/src/commonMain/kotlin/polski/grammar/GrammarEngine.kt`
// — the source of truth for what a "correct" form is until UC-07/08 replace it.
//
// Usage: node scripts/build-pack.mjs           (writes courses/pl-ru/forms.generated.json)
//        node scripts/build-pack.mjs --check   (fails if the checked-in file is stale, or if any
//                                                entry disagrees with tests/fixtures/core-golden/
//                                                grammar.json — the pinned parity gate, §6/§12).
import { readFileSync, writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';

const root = new URL('../', import.meta.url);
const readJson = (relativePath) => JSON.parse(readFileSync(new URL(relativePath, root), 'utf8'));
const CHECK = process.argv.includes('--check');

const CASES = ['nom', 'gen', 'dat', 'acc', 'inst', 'loc', 'voc'];
const NUMBERS = ['sg', 'pl'];
const GENDERS = ['m-personal', 'm-animate', 'm-inanimate', 'f', 'n'];
const PERSONS = [1, 2, 3];
// GrammarEngine.verbForm only ever distinguishes these 4 past-tense genders (m-animate/m-personal
// share the singular stem, m-inanimate is never drilled for verbs) — mirrors the golden fixture.
const PAST_GENDERS = ['m-personal', 'f', 'n', 'm-animate'];

function nounEntries(noun) {
  const entries = [];
  for (const number of NUMBERS) {
    for (const gramCase of CASES) {
      entries.push({ bundle: { Number: number, Case: gramCase }, form: noun.forms[number][gramCase] });
    }
  }
  return entries;
}

function adjectiveEntries(adjective) {
  const entries = [];
  for (const number of NUMBERS) {
    for (const gender of GENDERS) {
      for (const gramCase of CASES) {
        entries.push({
          bundle: { Number: number, Gender: gender, Case: gramCase },
          form: adjective.forms[number][gender][gramCase],
        });
      }
    }
  }
  return entries;
}

function possessiveEntries(possessive) {
  const entries = [];
  const forms = possessive.forms;
  for (const number of NUMBERS) {
    for (const gender of GENDERS) {
      for (const gramCase of CASES) {
        const form =
          forms.kind === 'invariant'
            ? forms.value
            : number === 'sg'
              ? forms.sg[gender][gramCase]
              : forms.pl[gender === 'm-personal' ? 'm-personal' : 'other'][gramCase];
        entries.push({ bundle: { Number: number, Gender: gender, Case: gramCase }, form });
      }
    }
  }
  return entries;
}

// Port of GrammarEngine.verbForm's past-tense branch: suffix by person/number/gender-shortness,
// stem selected by number (mp/np for plural) then gender (f/n/else) for singular.
function pastForm(verb, person, number, gender) {
  const shortSuffix = gender === 'f' || gender === 'n';
  const suffix =
    number === 'sg'
      ? person === 1
        ? shortSuffix ? 'm' : 'em'
        : person === 2
          ? shortSuffix ? 'ś' : 'eś'
          : ''
      : person === 1
        ? 'śmy'
        : person === 2
          ? 'ście'
          : '';
  const stem =
    number === 'pl'
      ? gender === 'm-personal' ? verb.pastStem.mp : verb.pastStem.np
      : gender === 'f'
        ? verb.pastStem.f
        : gender === 'n'
          ? verb.pastStem.n ?? verb.pastStem.m
          : verb.pastStem.m;
  return stem + suffix;
}

function verbEntries(verb, auxiliary) {
  const entries = [];
  if (verb.aspect === 'imperfective') {
    for (const number of NUMBERS) {
      for (const person of PERSONS) {
        entries.push({
          bundle: { Tense: 'present', Person: String(person), Number: number },
          form: verb.present[number][String(person)],
        });
      }
    }
  }
  for (const number of NUMBERS) {
    for (const person of PERSONS) {
      for (const gender of PAST_GENDERS) {
        entries.push({
          bundle: { Tense: 'past', Person: String(person), Number: number, Gender: gender },
          form: pastForm(verb, person, number, gender),
        });
      }
    }
  }
  for (const number of NUMBERS) {
    for (const person of PERSONS) {
      const form =
        verb.futureType === 'present'
          ? verb.present[number][String(person)]
          : verb.id === auxiliary.verbId
            ? auxiliary.forms[number][String(person)]
            : `${auxiliary.forms[number][String(person)]} ${verb.lemma}`;
      entries.push({ bundle: { Tense: 'future', Person: String(person), Number: number }, form });
    }
  }
  return entries;
}

function buildForms(course) {
  const forms = {};
  for (const noun of course.nouns) forms[`noun:${noun.id}`] = nounEntries(noun);
  for (const adjective of course.adjectives) forms[`adjective:${adjective.id}`] = adjectiveEntries(adjective);
  for (const possessive of course.possessives) forms[`possessive:${possessive.id}`] = possessiveEntries(possessive);
  const auxiliary = course.morphology.futureAuxiliary;
  for (const verb of course.verbs) forms[`verb:${verb.id}`] = verbEntries(verb, auxiliary);
  return forms;
}

function bundleKey(bundle) {
  return Object.keys(bundle).sort().map((k) => `${k}=${bundle[k]}`).join(',');
}

// Cross-checks every generated entry against the pinned golden fixture (UniversalCorePlan.md §6
// risk / §12 UC-05 acceptance: "match tests/fixtures/core-golden"). Golden `input` field names
// differ per category (nounId/adjectiveId/verbId/owner, gramCase/number/gender/person/tense) — this
// maps each generated (lexeme, bundle) to the golden case it corresponds to and diffs `form`.
function checkAgainstGolden(forms) {
  const golden = readJson('tests/fixtures/core-golden/grammar.json').cases;
  const byKey = new Map();
  for (const c of golden) {
    const i = c.input;
    let category, id, bundle;
    if (c.id.startsWith('G-NOUN-')) { category = 'noun'; id = i.nounId; bundle = { Number: i.number, Case: i.gramCase }; }
    else if (c.id.startsWith('G-ADJ-')) { category = 'adjective'; id = i.adjectiveId; bundle = { Number: i.number, Gender: i.gender, Case: i.gramCase }; }
    else if (c.id.startsWith('G-POSS-')) { category = 'possessive'; id = i.owner; bundle = { Number: i.number, Gender: i.gender, Case: i.gramCase }; }
    else if (c.id.startsWith('G-VERB-') && !c.id.endsWith('-rejected')) {
      category = 'verb'; id = i.verbId;
      bundle = i.tense === 'past'
        ? { Tense: i.tense, Person: String(i.person), Number: i.number, Gender: i.gender }
        : { Tense: i.tense, Person: String(i.person), Number: i.number };
    } else continue;
    byKey.set(`${category}:${id}\u0000${bundleKey(bundle)}`, c.expected.form);
  }
  let checked = 0;
  const mismatches = [];
  for (const [lexeme, entries] of Object.entries(forms)) {
    for (const entry of entries) {
      const key = `${lexeme}\u0000${bundleKey(entry.bundle)}`;
      if (!byKey.has(key)) continue; // golden doesn't cover every combo (e.g. m-inanimate verbs) — skip, don't invent
      checked += 1;
      if (byKey.get(key) !== entry.form) mismatches.push(`${key}: generated=${entry.form} golden=${byKey.get(key)}`);
    }
  }
  if (mismatches.length > 0) {
    throw new Error(`forms.generated.json disagrees with core-golden fixtures:\n${mismatches.join('\n')}`);
  }
  if (checked === 0) throw new Error('forms.generated.json cross-check matched zero golden cases — key format drifted');
  return checked;
}

function main() {
  const course = readJson('courses/pl-ru/course.json');
  const forms = buildForms(course);
  const checkedAgainstGolden = checkAgainstGolden(forms);
  const output = {
    schemaVersion: 1,
    generatedFrom: 'courses/pl-ru/course.json',
    courseContentVersion: course.contentVersion,
    forms,
  };
  const content = JSON.stringify(output, null, 2) + '\n';
  const target = new URL('../courses/pl-ru/forms.generated.json', import.meta.url);
  if (CHECK) {
    let existing;
    try {
      existing = readFileSync(target, 'utf8');
    } catch {
      throw new Error(`forms.generated.json missing — run: node scripts/build-pack.mjs`);
    }
    if (existing !== content) throw new Error('forms.generated.json is stale — run: node scripts/build-pack.mjs');
    console.log(`forms.generated.json is up to date; ${checkedAgainstGolden} entries verified against core-golden.`);
  } else {
    writeFileSync(target, content, 'utf8');
    console.log(`Wrote ${fileURLToPath(target)} (${checkedAgainstGolden} entries verified against core-golden).`);
  }
}

main();
