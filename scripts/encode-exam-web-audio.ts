import { createHash, randomUUID } from 'node:crypto';
import { spawn, spawnSync } from 'node:child_process';
import { createReadStream } from 'node:fs';
import { mkdir, rename, rm, stat } from 'node:fs/promises';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { EXAM_AUDIO_ASSET_ROW_ROWS, type ExamAudioAssetRow } from '../dsl/exam-assets/generated/ExamAudioRows.ts';

const root = resolve(fileURLToPath(new URL('..', import.meta.url)));
const encoderVersion = 'ffmpeg version 8.0.1';

function run(program: string, args: string[]): string {
  const result = spawnSync(program, args, { cwd: root, encoding: 'utf8', timeout: 600_000 });
  if (result.status !== 0) throw new Error(`${program} ${args.join(' ')}: ${result.stderr || result.error?.message || result.status}`);
  return result.stdout;
}

async function hashFile(path: string): Promise<string> {
  const hash = createHash('sha256');
  for await (const chunk of createReadStream(path)) hash.update(chunk);
  return hash.digest('hex');
}

async function verify(path: string, expected: string): Promise<number> {
  const size = (await stat(path)).size;
  const actual = await hashFile(path);
  if (actual !== expected) throw new Error(`${path}: SHA-256 mismatch: ${actual}`);
  return size;
}

function metadata(path: string): { sample_rate: string; channels: number; bit_rate: string } {
  const result = JSON.parse(run('ffprobe', ['-v', 'error', '-show_entries', 'stream=sample_rate,channels,bit_rate',
    '-of', 'json', path])) as { streams: Array<{ sample_rate: string; channels: number; bit_rate: string }> };
  if (result.streams.length !== 1) throw new Error(`${path}: expected one audio stream`);
  return result.streams[0];
}

async function decodedSamplesPerChannel(path: string): Promise<number> {
  const child = spawn('ffmpeg', ['-nostdin', '-hide_banner', '-v', 'error', '-threads', '1', '-err_detect', 'explode',
    '-i', path, '-map', '0:a:0', '-xerror', '-ac', '2', '-f', 's16le', 'pipe:1'],
  { stdio: ['ignore', 'pipe', 'pipe'] });
  const closed = new Promise<{ code: number | null; error?: Error }>(resolveExit => {
    child.once('error', error => resolveExit({ code: null, error }));
    child.once('close', code => resolveExit({ code }));
  });
  let bytes = 0;
  let error = '';
  child.stderr.setEncoding('utf8');
  child.stderr.on('data', chunk => { error += chunk; });
  for await (const chunk of child.stdout) bytes += chunk.length;
  const exit = await closed;
  if (exit.code !== 0 || bytes % 4 !== 0) throw new Error(`${path}: full stereo decode failed: ${error || exit.error?.message || exit.code}`);
  return bytes / 4;
}

function derivatives(): ExamAudioAssetRow[] {
  const rows = EXAM_AUDIO_ASSET_ROW_ROWS.filter(row => row.webLocalSourcePath !== undefined);
  if (rows.some(row => !row.webSha256 || !row.webLocalSourcePath)) throw new Error('Incomplete web derivative declaration');
  return rows;
}

async function processRow(row: ExamAudioAssetRow, mode: string): Promise<void> {
  const source = join(root, row.localSourcePath);
  const target = join(root, row.webLocalSourcePath!);
  const originalBytes = await verify(source, row.sha256);
  const original = metadata(source);
  if (original.channels !== 2 || original.sample_rate !== '44100') throw new Error(`${source}: expected 44.1 kHz stereo source`);
  if (mode === 'encode') {
    await mkdir(dirname(target), { recursive: true });
    const temp = `${target}.${randomUUID()}.mp3`;
    try {
      run('ffmpeg', ['-nostdin', '-hide_banner', '-v', 'error', '-threads', '1', '-i', source,
        '-map', '0:a:0', '-vn', '-c:a', 'libmp3lame', '-b:a', '160k', '-ac', '2',
        '-id3v2_version', '0', '-y', temp]);
      await verify(temp, row.webSha256!);
      await rename(temp, target);
    } finally {
      await rm(temp, { force: true });
    }
  }
  const webBytes = await verify(target, row.webSha256!);
  const web = metadata(target);
  if (web.channels !== original.channels || web.sample_rate !== original.sample_rate || web.bit_rate !== '160000') {
    throw new Error(`${target}: derivative must be 160 kb/s at the original sample rate and stereo channel count`);
  }
  const sourceSamples = await decodedSamplesPerChannel(source);
  const webSamples = await decodedSamplesPerChannel(target);
  if (sourceSamples !== webSamples) throw new Error(`${row.examId}: decoded sample count differs: ${sourceSamples} vs ${webSamples}`);
  console.log(`${row.examId}: source ${originalBytes} ${row.sha256}; web ${webBytes} ${row.webSha256}; samples/channel ${sourceSamples}`);
}

async function main(mode: string): Promise<void> {
  if (mode !== 'encode' && mode !== 'verify-derivatives') {
    throw new Error('Usage: tsx scripts/encode-exam-web-audio.ts encode | verify-derivatives');
  }
  const version = run('ffmpeg', ['-version']).split('\n')[0];
  if (!version.startsWith(encoderVersion)) throw new Error(`Expected ${encoderVersion}; found ${version}`);
  const rows = derivatives();
  if (!rows.length) throw new Error('No declared web derivatives');
  for (const row of rows) await processRow(row, mode);
  console.log(`PASS ${mode}: ${rows.length} verified derivatives with ${encoderVersion}`);
}

main(process.argv[2]).catch(error => { console.error(error); process.exitCode = 1; });
