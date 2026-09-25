import { execFileSync } from 'node:child_process';
import { mkdirSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';

const adb = process.env.GLASS_ADB ?? '/Users/german/Library/Android/sdk/platform-tools/adb';
const serial = process.env.GLASS_ANDROID_SERIAL;
const outputDirectory = process.env.GLASS_ANDROID_OUT ?? 'Plans/Kotlin/artifacts/glass-android/auto-scroll';
const testPairs = Number(process.env.GLASS_ANDROID_PAIRS ?? 8);
const controlPairs = Number(process.env.GLASS_ANDROID_CONTROLS ?? 4);
const measureMs = Number(process.env.GLASS_ANDROID_MEASURE_MS ?? 5000);
const warmupMs = Number(process.env.GLASS_ANDROID_WARMUP_MS ?? 1800);
const minFrames = Number(process.env.GLASS_ANDROID_MIN_FRAMES ?? 100);
const packageName = process.env.GLASS_ANDROID_PACKAGE ?? 'dev.polski.grammarmatrix.benchmark';
const activityClass = 'dev.polski.grammarmatrix.GlassBenchmarkActivity';
const activity = `${packageName}/${activityClass}`;
const baseVariant = process.env.GLASS_ANDROID_BASE ?? 'opaque';
const candidateVariant = process.env.GLASS_ANDROID_CANDIDATE ?? 'tint';
if (![testPairs, controlPairs, measureMs, warmupMs].every(Number.isFinite) ||
    !Number.isInteger(testPairs) || !Number.isInteger(controlPairs) ||
    testPairs < 1 || controlPairs < 0 || measureMs < 1000 || warmupMs < 0 ||
    !Number.isInteger(minFrames) || minFrames < 1 || baseVariant === candidateVariant) {
  throw new Error('Invalid benchmark settings');
}
mkdirSync(outputDirectory, { recursive: true });

function call(...args) {
  return execFileSync(adb, serial ? ['-s', serial, ...args] : args, {
    encoding: 'utf8', maxBuffer: 8 * 1024 * 1024,
  }).trim();
}
function pause(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}
function percentile(values, fraction) {
  const sorted = [...values].sort((a, b) => a - b);
  return sorted[Math.min(sorted.length - 1, Math.ceil(sorted.length * fraction) - 1)];
}
function parseFrames(dump) {
  const windowStart = dump.indexOf(`Window: ${packageName}/${activityClass}`);
  if (windowStart < 0) throw new Error('Benchmark Activity missing from gfxinfo');
  const markerStart = dump.indexOf('---PROFILEDATA---', windowStart);
  const markerEnd = dump.indexOf('---PROFILEDATA---', markerStart + 1);
  if (markerStart < 0 || markerEnd < 0) throw new Error('Frame profile block missing');
  const lines = dump.slice(markerStart, markerEnd).trim().split(/\r?\n/);
  const header = lines[1].split(',');
  const flags = header.indexOf('Flags');
  const intended = header.indexOf('IntendedVsync');
  const completed = header.indexOf('FrameCompleted');
  if ([flags, intended, completed].some(index => index < 0)) throw new Error('Unknown frame columns');
  const frameMs = lines.slice(2).map(line => line.split(',')).filter(columns =>
    columns.length >= header.length - 1 && columns[flags] === '0',
  ).map(columns => (Number(columns[completed]) - Number(columns[intended])) / 1e6)
    .filter(value => Number.isFinite(value) && value > 0 && value < 1000);
  if (frameMs.length < minFrames) throw new Error(`Too few frames: ${frameMs.length}`);
  return frameMs;
}

const surfaceFlinger = call('shell', 'dumpsys', 'SurfaceFlinger');
const gles = surfaceFlinger.split('\n').find(line => line.includes('GLES:'))?.trim() ?? 'unknown';
const result = {
  date: new Date().toISOString(),
  device: {
    serial: serial ?? 'default',
    model: call('shell', 'getprop', 'ro.product.model'),
    api: call('shell', 'getprop', 'ro.build.version.sdk'),
    size: call('shell', 'wm', 'size'),
    density: call('shell', 'wm', 'density'),
    gles,
  },
  setup: { testPairs, controlPairs, measureMs, warmupMs, minFrames, automaticScrollPxPerSec: 1400, baseVariant, candidateVariant },
  runs: [],
};

call('shell', 'am', 'force-stop', packageName);
let pid;
async function run(variant, pair, arm, kind, record = true) {
  call('shell', 'am', 'start', '-n', activity, '--es', 'variant', variant, '--ez', 'automatic', 'true');
  await pause(warmupMs);
  const currentPid = call('shell', 'pidof', packageName);
  if (!currentPid || (pid && currentPid !== pid)) throw new Error(`Process changed: ${pid} -> ${currentPid}`);
  pid = currentPid;
  call('shell', 'dumpsys', 'gfxinfo', packageName, 'reset');
  await pause(measureMs);
  const dump = call('shell', 'dumpsys', 'gfxinfo', packageName, 'framestats');
  if (!record) return;
  writeFileSync(join(outputDirectory, `pair-${pair}-${arm}-${variant}-gfxinfo.txt`), dump);
  const frameMs = parseFrames(dump);
  const entry = {
    pair, arm, kind, variant, pid, frameCount: frameMs.length,
    p50Ms: percentile(frameMs, 0.5), p95Ms: percentile(frameMs, 0.95),
    p99Ms: percentile(frameMs, 0.99),
    over11Ms: frameMs.filter(value => value > 11.1).length,
    over22Ms: frameMs.filter(value => value > 22.2).length,
    over33Ms: frameMs.filter(value => value > 33.3).length,
    frameMs,
  };
  result.runs.push(entry);
  writeFileSync(join(outputDirectory, 'raw-results.json'), JSON.stringify(result, null, 2));
  process.stdout.write(`${kind} ${pair} ${arm} ${variant}: ${entry.frameCount} frames, p95 ${entry.p95Ms.toFixed(2)}ms, >33ms ${entry.over33Ms}\n`);
}

await run(baseVariant, 0, 'warmup-base', 'warmup', false);
await run(candidateVariant, 0, 'warmup-candidate', 'warmup', false);

let testCount = 0;
let controlCount = 0;
const pairCount = testPairs + controlPairs;
for (let pair = 1; pair <= pairCount; pair += 1) {
  const nextControl = Math.floor(pair * controlPairs / pairCount) > controlCount;
  const kind = nextControl ? 'control' : 'test';
  if (nextControl) controlCount += 1;
  else testCount += 1;
  const variants = nextControl ? [baseVariant, baseVariant] :
    testCount % 2 === 1 ? [baseVariant, candidateVariant] : [candidateVariant, baseVariant];
  await run(variants[0], pair, 'first', kind);
  await run(variants[1], pair, 'second', kind);
}

const pairs = Array.from({ length: pairCount }, (_, index) => {
  const matching = result.runs.filter(run => run.pair === index + 1);
  const first = matching.find(run => run.arm === 'first');
  const second = matching.find(run => run.arm === 'second');
  return {
    pair: index + 1, kind: first.kind,
    deltaP95Ms: first.kind === 'test'
      ? matching.find(run => run.variant === candidateVariant).p95Ms - matching.find(run => run.variant === baseVariant).p95Ms
      : second.p95Ms - first.p95Ms,
    deltaOver33Ms: first.kind === 'test'
      ? matching.find(run => run.variant === candidateVariant).over33Ms - matching.find(run => run.variant === baseVariant).over33Ms
      : second.over33Ms - first.over33Ms,
  };
});
const test = pairs.filter(pair => pair.kind === 'test').map(pair => pair.deltaP95Ms);
const control = pairs.filter(pair => pair.kind === 'control').map(pair => pair.deltaP95Ms);
result.summary = {
  testMedianDeltaP95Ms: percentile(test, 0.5),
  testRangeDeltaP95Ms: [Math.min(...test), Math.max(...test)],
  controlMedianDeltaP95Ms: control.length ? percentile(control, 0.5) : null,
  controlRangeDeltaP95Ms: control.length ? [Math.min(...control), Math.max(...control)] : null,
  pairs,
};
writeFileSync(join(outputDirectory, 'raw-results.json'), JSON.stringify(result, null, 2));
