import { createHash, randomUUID } from 'node:crypto';
import { createReadStream, createWriteStream } from 'node:fs';
import { copyFile, mkdir, readFile, rename, rm, stat } from 'node:fs/promises';
import { Readable } from 'node:stream';
import { pipeline } from 'node:stream/promises';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { EXAM_AUDIO_ASSET_ROW_ROWS, type ExamAudioAssetRow } from '../dsl/exam-assets/generated/ExamAudioRows.ts';

const resources = 'kotlin/composeApp/src/webMain/resources';

function validate(rows: readonly ExamAudioAssetRow[]): void {
  if (!rows.length) throw new Error('Exam audio manifest is empty');
  const ids = new Set<string>();
  const paths = new Set<string>();
  for (const row of rows) {
    if (!/^[a-f0-9]{64}$/.test(row.sha256)) throw new Error(`${row.examId}: invalid SHA-256`);
    if (!/^exam-audio\/[A-Za-z0-9-]+\.mp3$/.test(row.webPath)) throw new Error(`${row.examId}: invalid web path`);
    if (!/^ExamBin\/pl\/(B1|B2)\/(2017|2019|2020)\/audio\.mp3$/.test(row.localSourcePath)) throw new Error(`${row.examId}: invalid local source path`);
    if (!row.officialDownloadUrl.startsWith('https://')) throw new Error(`${row.examId}: invalid official URL`);
    if (ids.has(row.examId) || paths.has(row.webPath)) throw new Error(`${row.examId}: duplicate asset identity or path`);
    ids.add(row.examId); paths.add(row.webPath);
  }
}

async function sha256(path: string): Promise<string> {
  const hash = createHash('sha256');
  for await (const chunk of createReadStream(path)) hash.update(chunk);
  return hash.digest('hex');
}

async function verifyFile(path: string, row: ExamAudioAssetRow): Promise<void> {
  try { await stat(path); }
  catch (error) {
    if ((error as NodeJS.ErrnoException).code === 'ENOENT') throw new Error(`${row.examId}: missing audio asset ${path}`);
    throw error;
  }
  const actual = await sha256(path);
  if (actual !== row.sha256) throw new Error(`${row.examId}: SHA-256 mismatch for ${path}: ${actual}`);
}

async function stage(row: ExamAudioAssetRow, root: string, offline: boolean): Promise<{ target: string; temp: string }> {
  const target = join(root, resources, row.webPath);
  await mkdir(dirname(target), { recursive: true });
  const temp = `${target}.${randomUUID()}.tmp`;
  const source = join(root, row.localSourcePath);
  try {
    try {
      await verifyFile(source, row);
      await copyFile(source, temp);
    } catch (error) {
      if (!(error instanceof Error && error.message.includes('missing audio asset')) || offline) throw error;
      const response = await fetch(row.officialDownloadUrl, { signal: AbortSignal.timeout(60_000) });
      if (!response.ok || !response.body) throw new Error(`${row.examId}: download failed HTTP ${response.status}`);
      await pipeline(Readable.fromWeb(response.body as never), createWriteStream(temp, { flags: 'wx' }));
    }
    await verifyFile(temp, row);
    return { target, temp };
  } catch (error) {
    await rm(temp, { force: true });
    throw error;
  }
}

/** Stages and validates every declared asset before atomically replacing resource files. */
export async function prepareAudioAssets(rows: readonly ExamAudioAssetRow[], root: string, offline = false): Promise<void> {
  validate(rows);
  const staged: { target: string; temp: string }[] = [];
  try {
    for (const row of rows) staged.push(await stage(row, root, offline));
    for (const { target, temp } of staged) await rename(temp, target);
  } finally {
    await Promise.all(staged.map(({ temp }) => rm(temp, { force: true })));
  }
}

/** Verifies the exact MP3 bytes in a production distribution, not source resources. */
export async function verifyDistribution(rows: readonly ExamAudioAssetRow[], dist: string): Promise<void> {
  validate(rows);
  for (const row of rows) await verifyFile(join(dist, row.webPath), row);
}

/** Fetches the media paths as a browser would and checks every response body. */
export async function verifyServing(rows: readonly ExamAudioAssetRow[], baseUrl: string): Promise<void> {
  validate(rows);
  for (const row of rows) {
    const response = await fetch(new URL(row.webPath, baseUrl), { signal: AbortSignal.timeout(60_000) });
    if (!response.ok || !response.body) throw new Error(`${row.examId}: served audio HTTP ${response.status}`);
    const hash = createHash('sha256');
    for await (const chunk of Readable.fromWeb(response.body as never)) hash.update(chunk);
    if (hash.digest('hex') !== row.sha256) throw new Error(`${row.examId}: served audio SHA-256 mismatch`);
  }
}

/** Checks that every exam's DSL task path is its manifest path. */
export async function verifyTaskPaths(rows: readonly ExamAudioAssetRow[], root: string): Promise<void> {
  validate(rows);
  for (const row of rows) {
    const examDir = dirname(row.localSourcePath).replace(/^ExamBin\/pl\//, 'exams/pl/certyfikat-polski/');
    const source = await readFile(join(root, examDir, 'listening-tasks.core-data'), 'utf8');
    const urls = [...source.matchAll(/^\s*audioUrl:\s*"?([^"\s]+)"?\s*$/gm)].map(match => match[1]);
    if (!urls.length || urls.some(url => url !== row.webPath)) throw new Error(`${row.examId}: listening task audioUrl differs from ${row.webPath}`);
  }
}

async function main(args: string[]): Promise<void> {
  const root = resolve(fileURLToPath(new URL('..', import.meta.url)));
  const [mode, value] = args;
  if (mode === 'prepare') {
    await verifyTaskPaths(EXAM_AUDIO_ASSET_ROW_ROWS, root);
    await prepareAudioAssets(EXAM_AUDIO_ASSET_ROW_ROWS, root, args.includes('--offline'));
  }
  else if (mode === 'check-dist' && value) await verifyDistribution(EXAM_AUDIO_ASSET_ROW_ROWS, resolve(value));
  else if (mode === 'check-url' && value) await verifyServing(EXAM_AUDIO_ASSET_ROW_ROWS, value);
  else if (mode === 'check-paths') await verifyTaskPaths(EXAM_AUDIO_ASSET_ROW_ROWS, root);
  else throw new Error('Usage: tsx scripts/prepare-exam-audio.ts prepare [--offline] | check-dist <directory> | check-url <base-url> | check-paths');
  console.log(`PASS exam audio ${mode}: ${EXAM_AUDIO_ASSET_ROW_ROWS.length} assets`);
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  main(process.argv.slice(2)).catch(error => { console.error(error); process.exitCode = 1; });
}
