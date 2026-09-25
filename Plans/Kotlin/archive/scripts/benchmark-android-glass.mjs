import { execFileSync } from 'node:child_process';
import { mkdirSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';

const adb = process.env.GLASS_ADB ?? '/Users/german/Library/Android/sdk/platform-tools/adb';
const outputDirectory = process.env.GLASS_ANDROID_OUT ?? 'Plans/Kotlin/artifacts/glass-android';
const rounds = Number(process.env.GLASS_ANDROID_ROUNDS ?? 5);
const packageName = 'dev.polski.grammarmatrix';
const activity = `${packageName}/.GlassBenchmarkActivity`;
if (!Number.isInteger(rounds) || rounds < 1) throw new Error('Invalid rounds');
mkdirSync(outputDirectory, { recursive: true });

function call(...args) {
  return execFileSync(adb, args, { encoding: 'utf8', maxBuffer: 8 * 1024 * 1024 });
}
function pause(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}
function percentile(values, fraction) {
  const sorted = [...values].sort((a, b) => a - b);
  return sorted[Math.min(sorted.length - 1, Math.ceil(sorted.length * fraction) - 1)];
}
function parseFrames(dump) {
  const windowStart = dump.indexOf(`Window: ${packageName}/${packageName}.GlassBenchmarkActivity`);
  if (windowStart < 0) throw new Error('Benchmark activity window missing from gfxinfo');
  const markerStart = dump.indexOf('---PROFILEDATA---', windowStart);
  const markerEnd = dump.indexOf('---PROFILEDATA---', markerStart + 1);
  if (markerStart < 0 || markerEnd < 0) throw new Error('Frame profile block missing');
  const lines = dump.slice(markerStart, markerEnd).trim().split(/\r?\n/);
  const header = lines[1].split(',');
  const flags = header.indexOf('Flags');
  const intended = header.indexOf('IntendedVsync');
  const completed = header.indexOf('FrameCompleted');
  if ([flags, intended, completed].some(index => index < 0)) throw new Error('Unknown gfxinfo frame header');
  const frameMs = lines.slice(2).map(line => line.split(',')).filter(columns =>
    columns.length >= header.length - 1 && columns[flags] === '0',
  ).map(columns => (Number(columns[completed]) - Number(columns[intended])) / 1e6)
    .filter(value => Number.isFinite(value) && value > 0 && value < 1000);
  if (frameMs.length < 40) throw new Error(`Too few frames: ${frameMs.length}`);
  return frameMs;
}

const device = {
  model: call('shell', 'getprop', 'ro.product.model').trim(),
  api: call('shell', 'getprop', 'ro.build.version.sdk').trim(),
  size: call('shell', 'wm', 'size').trim(),
  density: call('shell', 'wm', 'density').trim(),
};
const results = { date: new Date().toISOString(), device, rounds, runs: [] };
call('shell', 'am', 'force-stop', packageName);
for (let round = 0; round < rounds; round += 1) {
  const order = round % 2 === 0 ? ['opaque', 'tint'] : ['tint', 'opaque'];
  for (const variant of order) {
    call('shell', 'am', 'start', '-n', activity, '--es', 'variant', variant);
    await pause(round === 0 && variant === order[0] ? 1400 : 350);
    for (let i = 0; i < 2; i += 1) {
      call('shell', 'input', 'swipe', '540', '1850', '540', '500', '300');
      call('shell', 'input', 'swipe', '540', '500', '540', '1850', '300');
    }
    call('shell', 'dumpsys', 'gfxinfo', packageName, 'reset');
    for (let i = 0; i < 12; i += 1) {
      const fromY = i % 2 === 0 ? '1850' : '500';
      const toY = i % 2 === 0 ? '500' : '1850';
      call('shell', 'input', 'swipe', '540', fromY, '540', toY, '350');
    }
    await pause(300);
    const dump = call('shell', 'dumpsys', 'gfxinfo', packageName, 'framestats');
    writeFileSync(join(outputDirectory, `round-${round + 1}-${variant}-gfxinfo.txt`), dump);
    const frameMs = parseFrames(dump);
    const result = {
      round: round + 1, variant, frameCount: frameMs.length,
      p50Ms: percentile(frameMs, 0.5), p95Ms: percentile(frameMs, 0.95),
      p99Ms: percentile(frameMs, 0.99),
      over33Ms: frameMs.filter(value => value > 33.3).length,
      frameMs,
    };
    results.runs.push(result);
    writeFileSync(join(outputDirectory, 'raw-results.json'), JSON.stringify(results, null, 2));
    process.stdout.write(`round ${round + 1}/${rounds} ${variant}: ${frameMs.length} frames, p95 ${result.p95Ms.toFixed(2)}ms, >33ms ${result.over33Ms}\n`);
  }
}
const pairedDifferences = [...Array(rounds).keys()].map(index => {
  const matching = results.runs.filter(run => run.round === index + 1);
  return matching.find(run => run.variant === 'tint').p95Ms - matching.find(run => run.variant === 'opaque').p95Ms;
});
results.summary = {
  opaqueMedianP95Ms: percentile(results.runs.filter(run => run.variant === 'opaque').map(run => run.p95Ms), 0.5),
  tintMedianP95Ms: percentile(results.runs.filter(run => run.variant === 'tint').map(run => run.p95Ms), 0.5),
  pairedMedianDeltaP95Ms: percentile(pairedDifferences, 0.5),
  pairedRangeDeltaP95Ms: [Math.min(...pairedDifferences), Math.max(...pairedDifferences)],
};
writeFileSync(join(outputDirectory, 'raw-results.json'), JSON.stringify(results, null, 2));
