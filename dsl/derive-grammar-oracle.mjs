// One-off converter (run once, by hand; NOT wired into dsl/biome.project — same status as the
// retired scripts/build-pack.mjs, see derive-forms.mjs's header comment): reads
// tests/fixtures/kotlin-parity/grammar.json and prints its 1979 pinned cases as GRAMMAR_CASE rows
// of dsl/grammar-oracle.data-contract's GrammarCaseRow, one row per case, grouped by the id prefix
// that names its kind (Plans/Architecture/DSL-MIGRATION-PLAN.md block F). The fixture's `expected`
// is either a plain string (phrase/sentence cases) or an object whose fields this script joins with
// "|" in the same order the hand-written GrammarParityTest.kt assertions always joined them — or,
// for the two present-tense-of-a-perfective-verb cases, an `{ error: { message } }` that becomes
// `expectError: true` plus the thrown message as `expected`.
//
// Usage: node dsl/derive-grammar-oracle.mjs > dsl/grammar-oracle/grammar.core-data
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const fixturePath = resolve(root, 'tests/fixtures/kotlin-parity/grammar.json');
const fixture = JSON.parse(readFileSync(fixturePath, 'utf8'));

// The order personalPronouns/possessiveForm cases join cases in, and the order GramCase.entries
// iterates in (kotlin/shared/src/commonMain/kotlin/polski/model/Grammar.kt) — the fixture's
// G-PRON- `expected` object happens to use the same key order, but this script does not rely on
// object key order.
const CASE_ORDER = ['nom', 'gen', 'dat', 'acc', 'inst', 'loc', 'voc'];

const FIELD_ORDER = [
  'id', 'kind', 'nounId', 'adjectiveId', 'verbId', 'pronounId', 'skillId', 'owner',
  'number', 'gramCase', 'gender', 'tense', 'person', 'expectError', 'expected',
];

/** Bareword for a kind/bool/int literal (same convention `gender: f`/`includeNounGenderTag: true`/
 *  `frequencyRank: 506` use elsewhere in courses-biome/*.core-data); a quoted, escaped string for
 *  everything else — simplest-and-correct over "bareword when it would also be legal". */
function quoted(value) {
  return `"${String(value).replace(/\\/g, '\\\\').replace(/"/g, '\\"')}"`;
}

function printField(key, value) {
  if (key === 'kind' || key === 'expectError' || key === 'person') return `${key}: ${value}`;
  return `${key}: ${quoted(value)}`;
}

function printRow(row) {
  const body = FIELD_ORDER.filter((key) => row[key] !== undefined).map((key) => printField(key, row[key])).join(' ');
  return `GRAMMAR_CASE {${body}}`;
}

function rowFor({ id, input, expected }) {
  if (id.startsWith('G-NOUN-')) {
    return {
      id, kind: 'noun', nounId: input.nounId, number: input.number, gramCase: input.gramCase,
      expected: [expected.lemma, expected.meaning, expected.gender, expected.form].join('|'),
    };
  }
  if (id.startsWith('G-ADJ-')) {
    return {
      id, kind: 'adjective', adjectiveId: input.adjectiveId, number: input.number, gender: input.gender, gramCase: input.gramCase,
      expected: [expected.lemma, expected.meaning, expected.form].join('|'),
    };
  }
  if (id.startsWith('G-VERB-')) {
    const base = { id, kind: 'verb', verbId: input.verbId, tense: input.tense, person: input.person, number: input.number, gender: input.gender };
    if (expected.error) return { ...base, expectError: true, expected: expected.error.message };
    return { ...base, expected: [expected.lemma, expected.meaning, expected.aspect, expected.futureType, expected.perfectivePair ?? '', expected.form].join('|') };
  }
  if (id.startsWith('G-PRON-')) {
    return { id, kind: 'pronoun', pronounId: input.pronounId, expected: CASE_ORDER.map((gramCase) => expected[gramCase]).join('|') };
  }
  if (id.startsWith('G-POSS-')) {
    return {
      id, kind: 'possessive', owner: input.owner, number: input.number, gender: input.gender, gramCase: input.gramCase,
      expected: [expected.label, expected.form].join('|'),
    };
  }
  if (id.startsWith('G-PHRASE-')) {
    return {
      id, kind: 'phrase', nounId: input.nounId, adjectiveId: input.adjectiveId, owner: input.possessive,
      number: input.number, gramCase: input.gramCase, expected,
    };
  }
  if (id.startsWith('G-SENTENCE-')) {
    return {
      id, kind: 'sentence', nounId: input.seed.nounId, adjectiveId: input.seed.adjectiveId, owner: input.owner,
      number: input.number, gramCase: input.gramCase, expected,
    };
  }
  if (id.startsWith('S-')) {
    return {
      id, kind: 'skill', skillId: input.skillId,
      expected: [expected.id, expected.title, expected.group, expected.level, expected.formula, expected.theory, expected.hint, expected.prerequisites.join(',')].join('|'),
    };
  }
  throw new Error(`Unknown grammar-oracle case id prefix: ${id}`);
}

const header = [
  'DSL 1',
  `# Converted once from tests/fixtures/kotlin-parity/grammar.json (schemaVersion ${fixture.schemaVersion},`,
  `# sourceRevision ${fixture.sourceRevision}) by dsl/derive-grammar-oracle.mjs — a one-off migration`,
  '# script, not a steady-state GENERATE derive step. See dsl/grammar-oracle.data-contract and',
  '# Plans/Architecture/DSL-MIGRATION-PLAN.md block F.',
];
const lines = [...header, ...fixture.cases.map(rowFor).map(printRow)];
process.stdout.write(lines.join('\n') + '\n');
