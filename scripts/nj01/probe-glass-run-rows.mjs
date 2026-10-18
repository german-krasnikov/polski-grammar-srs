/**
 * Disposable NJ01 P8 probe. This is neither a selected .measure grammar nor a
 * production writer. It compares run-row work only, never publishes an artifact.
 */
import assert from 'node:assert/strict';
import { pathToFileURL } from 'node:url';
import { performance } from 'node:perf_hooks';

const [mode, countText, kitPath] = process.argv.slice(2);
if (!['native', 'kit'].includes(mode) || !['10', '100', '1000'].includes(countText) || (mode === 'kit' && !kitPath)) {
  throw new Error('Usage: node --expose-gc probe-glass-run-rows.mjs native|kit 10|100|1000 [absolute-kit-dist-path]');
}
const count = Number(countText);

function scalarText(value) {
  if (typeof value !== 'string') throw new Error('text required');
  let result = '';
  for (let index = 0; index < value.length;) {
    const code = value.codePointAt(index);
    if (code >= 0xd800 && code <= 0xdfff) throw new Error('unpaired surrogate');
    const character = String.fromCodePoint(code);
    result += code === 37 || code <= 0x1f || (code >= 0x7f && code <= 0x9f) || code === 0x2028 || code === 0x2029
      ? `%{${code.toString(16).toUpperCase().padStart(2, '0')}}`
      : character;
    index += character.length;
  }
  return result;
}

function readScalarText(value) {
  const decoded = value.replace(/%\{([0-9A-F]{2,6})\}/g, (_, digits) => String.fromCodePoint(Number.parseInt(digits, 16)));
  if (scalarText(decoded) !== value) throw new Error('noncanonical or unsafe text');
  return decoded;
}

function numberText(value) {
  if (typeof value !== 'number' || !Number.isFinite(value)) throw new Error('finite number required');
  return Object.is(value, -0) ? '-0' : String(value);
}

function readNumberText(value) {
  const number = Number(value);
  if (numberText(number) !== value) throw new Error('noncanonical number');
  return number;
}

function nullableText(value) {
  return value === null ? '~' : `s:${scalarText(value)}`;
}

function readNullableText(value) {
  if (value === '~') return null;
  if (!value.startsWith('s:')) throw new Error('missing nullable text tag');
  return readScalarText(value.slice(2));
}

function percentile(values, fraction) {
  const sorted = [...values].sort((left, right) => left - right);
  return sorted[Math.ceil(sorted.length * fraction) - 1];
}

function validateRun(row) {
  if (!Number.isSafeInteger(row.index) || row.index < 0 || !Number.isSafeInteger(row.round) || row.round < 1) throw new Error('bad index/round');
  if (!['baseline', 'candidate'].includes(row.variant)) throw new Error('bad variant');
  scalarText(row.engine);
  scalarText(row.browserVersion);
  nullableText(row.navFilter);
  nullableText(row.cardFilter);
  if (!Array.isArray(row.intervalsMs) || row.intervalsMs.length === 0 || row.intervalsMs.length > 1000) throw new Error('bad interval count');
  if (row.frames !== row.intervalsMs.length) throw new Error('frame count differs');
  for (const interval of row.intervalsMs) if (!(interval > 0) || !Number.isFinite(interval)) throw new Error('bad frame interval');
  if (![row.p50Ms, row.p95Ms, row.p99Ms].every(n => Number.isFinite(n) && n > 0)) throw new Error('bad percentile');
  if (!Number.isSafeInteger(row.over25Ms) || row.over25Ms < 0 || row.over25Ms > row.frames) throw new Error('bad over25');
  if (row.p50Ms !== percentile(row.intervalsMs, 0.5) || row.p95Ms !== percentile(row.intervalsMs, 0.95) ||
      row.p99Ms !== percentile(row.intervalsMs, 0.99) || row.over25Ms !== row.intervalsMs.filter(value => value > 25).length) {
    throw new Error('derived measurement differs from intervals');
  }
}

function nativeRun(row) {
  validateRun(row);
  return [row.index, row.round, row.variant, scalarText(row.engine), scalarText(row.browserVersion), row.frames,
    numberText(row.p50Ms), numberText(row.p95Ms), numberText(row.p99Ms), row.over25Ms,
    nullableText(row.navFilter), nullableText(row.cardFilter), row.intervalsMs.map(numberText).join(',')].join('\t');
}

function readNativeRun(line) {
  const fields = line.split('\t');
  if (fields.length !== 13) throw new Error('wrong column count');
  const row = {
    index: readNumberText(fields[0]), round: readNumberText(fields[1]), variant: fields[2],
    engine: readScalarText(fields[3]), browserVersion: readScalarText(fields[4]), frames: readNumberText(fields[5]),
    p50Ms: readNumberText(fields[6]), p95Ms: readNumberText(fields[7]), p99Ms: readNumberText(fields[8]),
    over25Ms: readNumberText(fields[9]), navFilter: readNullableText(fields[10]), cardFilter: readNullableText(fields[11]),
    intervalsMs: fields[12].split(',').map(readNumberText),
  };
  validateRun(row);
  return row;
}

function checkSequence(rows) {
  if (!rows.length) throw new Error('no runs');
  for (let index = 0; index < rows.length; index += 1) {
    const row = rows[index];
    validateRun(row);
    if (row.index !== index || row.round !== Math.floor(index / 2) + 1 ||
        row.variant !== (index % 4 === 0 || index % 4 === 3 ? 'baseline' : 'candidate')) {
      throw new Error('duplicate, gap, round, or order');
    }
  }
}

function writeDocument(rows, writeRow) {
  checkSequence(rows);
  return `PROBE\t${rows.length}\n${rows.map(writeRow).join('\n')}\nEND\n`;
}

function readDocument(document, readRow) {
  const lines = document.split('\n');
  if (!/^PROBE\t[1-9][0-9]*$/.test(lines[0]) || lines.at(-2) !== 'END' || lines.at(-1) !== '') throw new Error('header/trailer');
  const count = Number(lines[0].split('\t')[1]);
  if (lines.length !== count + 3) throw new Error('truncated or excess runs');
  const rows = lines.slice(1, -2).map(readRow);
  checkSequence(rows);
  return rows;
}

const rows = Array.from({ length: count }, (_, index) => ({
  index,
  round: Math.floor(index / 2) + 1,
  variant: index % 4 === 0 || index % 4 === 3 ? 'baseline' : 'candidate',
  engine: 'chromium',
  browserVersion: '155.0.0-żółć-😀\u0085',
  frames: 30,
  p50Ms: 16,
  p95Ms: 16,
  p99Ms: 16,
  over25Ms: 0,
  navFilter: index === 0 ? 'line1\nline2\tΩ' : '',
  cardFilter: null,
  intervalsMs: Array(30).fill(16),
}));

let writeRow = nativeRun;
let readRow = readNativeRun;
if (mode === 'kit') {
  const kit = await import(pathToFileURL(kitPath).href);
  writeRow = row => kit.formatDataValue(row);
  readRow = line => {
    const parsed = kit.readData(line);
    if (!('value' in parsed)) throw new Error(parsed.message);
    return parsed.value;
  };
}

const first = writeDocument(rows, writeRow);
assert.deepStrictEqual(readDocument(first, readRow), rows);
const p01Intervals = [[16, 17, 20, 26], [17, 18, 21, 27], [19, 20, 23, 28], [15, 16, 19, 25]];
const p01 = p01Intervals.map((intervalsMs, index) => ({
  ...rows[index], index, round: Math.floor(index / 2) + 1,
  frames: 4, intervalsMs,
  p50Ms: [17, 18, 20, 16][index], p95Ms: [26, 27, 28, 25][index], p99Ms: [26, 27, 28, 25][index],
  over25Ms: [1, 1, 1, 0][index], navFilter: [null, 'blur(8px)', 'none', 'none'][index],
  cardFilter: ['', 'none', null, 'none'][index],
}));
assert.deepStrictEqual(readDocument(writeDocument(p01, writeRow), readRow), p01);
assert.throws(() => readDocument(first.replace('END\n', ''), readRow));
assert.throws(() => readDocument(first.replace('PROBE\t', 'PROBE\t0'), readRow));
const duplicate = [...rows];
duplicate[1] = rows[0];
assert.throws(() => writeDocument(duplicate, writeRow));
const wrongOrder = [...p01];
[wrongOrder[1], wrongOrder[2]] = [wrongOrder[2], wrongOrder[1]];
assert.throws(() => writeDocument(wrongOrder, writeRow));
assert.throws(() => writeDocument([{ ...p01[0], index: 1 }, ...p01.slice(1)], writeRow));
assert.throws(() => writeDocument([{ ...p01[0], navFilter: undefined }, ...p01.slice(1)], writeRow));
assert.throws(() => writeDocument([{ ...p01[0], over25Ms: 0 }, ...p01.slice(1)], writeRow));
assert.throws(() => writeDocument([{ ...p01[0], intervalsMs: [0, 17, 20, 26] }, ...p01.slice(1)], writeRow));
assert.throws(() => writeDocument([{ ...p01[0], intervalsMs: [Number.NaN, 17, 20, 26] }, ...p01.slice(1)], writeRow));
assert.throws(() => writeDocument([{ ...p01[0], browserVersion: '\ud800' }, ...p01.slice(1)], writeRow));
if (mode === 'native') {
  const probes = [+0, -0, 0.30000000000000004, 16.666666666666668, 1.0000000000000002, 5e-324, 1.7976931348623157e308];
  for (const number of probes) assert.ok(Object.is(readNumberText(numberText(number)), number));
  assert.equal(readScalarText(scalarText('żółć 😀\u0085\u2028\u2029\n\tΩ')), 'żółć 😀\u0085\u2028\u2029\n\tΩ');
  assert.throws(() => scalarText('\ud800'));
  assert.throws(() => readScalarText('%{000A}'));
  assert.ok(!first.includes('\u0085'));
} else {
  const kit = await import(pathToFileURL(kitPath).href);
  const probes = [+0, -0, 0.30000000000000004, 16.666666666666668, 1.0000000000000002, 5e-324, 1.7976931348623157e308];
  for (const number of probes) {
    assert.equal(Object.is(kit.readData(kit.formatDataValue(number)).value, number), !Object.is(number, -0));
  }
  assert.equal(kit.formatDataValue('żółć 😀\u0085').includes('\u0085'), true);
}

for (let warm = 0; warm < 3; warm += 1) readDocument(writeDocument(rows, writeRow), readRow);
if (global.gc) global.gc();
const heapBefore = process.memoryUsage().heapUsed;
const runs = 20;
let encodeNs = 0n;
let decodeNs = 0n;
let maxHeap = heapBefore;
for (let repetition = 0; repetition < runs; repetition += 1) {
  const startWrite = process.hrtime.bigint();
  const document = writeDocument(rows, writeRow);
  const endWrite = process.hrtime.bigint();
  readDocument(document, readRow);
  const endRead = process.hrtime.bigint();
  encodeNs += endWrite - startWrite;
  decodeNs += endRead - endWrite;
  maxHeap = Math.max(maxHeap, process.memoryUsage().heapUsed);
}
process.stdout.write(`mode=${mode} node=${process.version} rows=${count} bytes=${Buffer.byteLength(first)} tests=PASS encodeMeanMs=${(Number(encodeNs) / runs / 1e6).toFixed(3)} decodeMeanMs=${(Number(decodeNs) / runs / 1e6).toFixed(3)} heapBefore=${heapBefore} heapPeak=${maxHeap} rss=${process.memoryUsage().rss}\n`);
