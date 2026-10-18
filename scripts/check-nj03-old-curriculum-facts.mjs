// Test-only static oracle check. JSON.parse reads only the two locked historical source files.
// It neither emits JSON nor executes a candidate Biome surface or product consumer.
import { strict as assert } from 'node:assert';
import { createHash } from 'node:crypto';
import { readFileSync } from 'node:fs';

const root = new URL('../', import.meta.url);
const defaultCases = new URL('../Plans/Biome/NJ03CurriculumIndependent.cases', import.meta.url);
const casesPath = process.argv.length === 2 ? defaultCases : process.argv.length === 4 && process.argv[2] === '--cases' ? process.argv[3] : null;
if (casesPath === null) throw new Error('usage: node scripts/check-nj03-old-curriculum-facts.mjs [--cases path]');

const topFields = ['id', 'construction', 'focus', 'fixed', 'lexicalFilter', 'level', 'prerequisites'];
const sources = new Map();
const rows = new Map([['en', []], ['pl', []]]);

function commaList(value, emptyMarker = '[]') {
  if (value === emptyMarker) return [];
  assert(value.length > 0 && !value.split(',').some((item) => item.length === 0), `invalid list: ${value}`);
  return value.split(',');
}

function focus(value) {
  if (value === 'NULL') return null;
  const match = /^([^:]+):([^>]+)>([^>]+)$/.exec(value);
  assert(match, `invalid focus: ${value}`);
  return { feature: match[1], from: match[2], to: match[3] };
}

function fixed(value) {
  if (value === '{}') return {};
  const result = {};
  for (const item of commaList(value)) {
    const match = /^([^=]+)=([^=]+)$/.exec(item);
    assert(match, `invalid fixed pair: ${item}`);
    assert(!(match[1] in result), `duplicate fixed key: ${match[1]}`);
    result[match[1]] = match[2];
  }
  return result;
}

function lexicalFilter(value) {
  if (value === 'NULL') return null;
  const match = /^([^/]+)\/([^=]+)=(.+)$/.exec(value);
  assert(match, `invalid lexical filter: ${value}`);
  return { slot: match[1], where: { [match[2]]: commaList(match[3]) } };
}

for (const [lineIndex, raw] of readFileSync(casesPath, 'utf8').split('\n').entries()) {
  const line = raw.trim();
  if (!line || line.startsWith('#')) continue;
  const cells = line.split('|').map((cell) => cell.trim());
  const label = `cases line ${lineIndex + 1}`;
  if (cells[0] === 'SOURCE') {
    assert.equal(cells.length, 4, `${label}: SOURCE arity`);
    const [, language, path, sha256] = cells;
    assert(rows.has(language) && !sources.has(language), `${label}: duplicate/unknown source ${language}`);
    assert.equal(path, `courses/lang/${language}/curriculum.json`, `${label}: exact source path`);
    assert(/^[a-f0-9]{64}$/.test(sha256), `${label}: invalid full-file SHA-256`);
    sources.set(language, { path, sha256 });
  } else if (cells[0] === 'ROW') {
    assert.equal(cells.length, 10, `${label}: ROW arity`);
    const [, language, position, id, construction, focusValue, fixedValue, filterValue, level, prereqValue] = cells;
    assert(rows.has(language), `${label}: unknown language ${language}`);
    assert.equal(Number(position), rows.get(language).length + 1, `${label}: nonconsecutive position`);
    assert(/^\d{2}$/.test(position), `${label}: position spelling`);
    rows.get(language).push({
      id,
      construction,
      focus: focus(focusValue),
      fixed: fixed(fixedValue),
      lexicalFilter: lexicalFilter(filterValue),
      level,
      prerequisites: commaList(prereqValue),
    });
  } else {
    assert.fail(`${label}: unknown directive ${cells[0]}`);
  }
}

assert.equal(sources.size, 2, 'both old source identities must be frozen');
for (const language of ['en', 'pl']) {
  const { path, sha256 } = sources.get(language);
  const bytes = readFileSync(new URL(path, root));
  assert.equal(createHash('sha256').update(bytes).digest('hex'), sha256, `${path}: old source bytes changed`);
  const actual = JSON.parse(bytes.toString('utf8'));
  const expected = rows.get(language);
  assert(Array.isArray(actual), `${path}: expected array`);
  assert.equal(expected.length, 16, `${language}: expected 16 frozen rows`);
  assert.equal(actual.length, expected.length, `${path}: row count`);
  for (const [index, skill] of actual.entries()) {
    const location = `${path} row ${index + 1} ${expected[index].id}`;
    assert.deepEqual(Object.keys(skill), topFields, `${location}: absent/extra/reordered top-level field`);
    assert.deepEqual(Object.keys(skill.focus ?? {}), skill.focus === null ? [] : ['feature', 'from', 'to'], `${location}: focus fields`);
    assert.deepEqual(Object.keys(skill.fixed), Object.keys(expected[index].fixed), `${location}: fixed key order`);
    assert.deepEqual(Object.keys(skill.lexicalFilter ?? {}), skill.lexicalFilter === null ? [] : ['slot', 'where'], `${location}: lexical fields`);
    if (skill.lexicalFilter !== null) {
      assert.deepEqual(Object.keys(skill.lexicalFilter.where), Object.keys(expected[index].lexicalFilter.where), `${location}: where key order`);
    }
    assert.deepEqual(skill, expected[index], `${location}: frozen semantic fact`);
  }
}

process.stdout.write('PASS NJ03 old-source fact consistency: 2 files, 32/32 rows; candidate executions 0, skipped candidate checks NOT RUN\n');
