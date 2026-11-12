import { createHash, randomUUID } from 'node:crypto';
import { createReadStream, createWriteStream } from 'node:fs';
import { copyFile, mkdir, readFile, rename, rm, stat } from 'node:fs/promises';
import { Readable } from 'node:stream';
import { pipeline } from 'node:stream/promises';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { EXAM_AUDIO_ASSET_ROW_ROWS, type ExamAudioAssetRow } from '../dsl/exam-assets/generated/ExamAudioRows.ts';
import { EXAM_PUBLICATION_ROW_ROWS } from '../dsl/exam-published/generated/ExamPublicationRows.ts';

const resources = 'kotlin/composeApp/src/webMain/resources';

function validate(rows: readonly ExamAudioAssetRow[]): void {
  if (!rows.length) throw new Error('Exam audio manifest is empty');
  const ids = new Set<string>();
  const paths = new Set<string>();
  for (const row of rows) {
    if (!/^[a-f0-9]{64}$/.test(row.sha256)) throw new Error(`${row.examId}: invalid SHA-256`);
    if (!/^exam-audio\/[A-Za-z0-9-]+\.mp3$/.test(row.webPath)) throw new Error(`${row.examId}: invalid web path`);
    if (!/^ExamBin\/pl\/(B1|B2|C1)\/(?:2017|2019|2020|20\d{2}-\d{2})\/audio\.mp3$/.test(row.localSourcePath)) throw new Error(`${row.examId}: invalid local source path`);
    if ((row.webSha256 === undefined) !== (row.webLocalSourcePath === undefined)) {
      throw new Error(`${row.examId}: web derivative requires both webSha256 and webLocalSourcePath`);
    }
    if (row.webLocalSourcePath !== undefined) {
      if (!/^[a-f0-9]{64}$/.test(row.webSha256!)) throw new Error(`${row.examId}: invalid webSha256`);
      if (!/^ExamBin\/web\/(B1|B2|C1)\/(?:2017|2019|2020|20\d{2}-\d{2})\/audio\.mp3$/.test(row.webLocalSourcePath) ||
          row.webLocalSourcePath === row.localSourcePath) throw new Error(`${row.examId}: invalid webLocalSourcePath`);
    }
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

async function verifyFile(path: string, hash: string, examId: string): Promise<void> {
  try { await stat(path); }
  catch (error) {
    if ((error as NodeJS.ErrnoException).code === 'ENOENT') throw new Error(`${examId}: missing audio asset ${path}`);
    throw error;
  }
  const actual = await sha256(path);
  if (actual !== hash) throw new Error(`${examId}: SHA-256 mismatch for ${path}: ${actual}`);
}

const webHash = (row: ExamAudioAssetRow): string => row.webSha256 ?? row.sha256;
const webSource = (row: ExamAudioAssetRow): string => row.webLocalSourcePath ?? row.localSourcePath;

/** Every published exam has exactly one audio asset, with no inactive asset in web delivery. */
export function verifyPublishedAssetCoverage(rows: readonly ExamAudioAssetRow[], publishedIds: readonly string[]): void {
  validate(rows);
  const assets = new Set(rows.map(row => row.examId));
  const published = new Set(publishedIds);
  const missing = [...published].filter(id => !assets.has(id));
  const surplus = [...assets].filter(id => !published.has(id));
  if (published.size !== publishedIds.length || missing.length || surplus.length) {
    throw new Error(`Exam audio coverage: missing ${missing.join(', ') || '(none)'}; surplus ${surplus.join(', ') || '(none)'}`);
  }
}

async function stage(row: ExamAudioAssetRow, root: string, offline: boolean): Promise<{ target: string; temp: string }> {
  const target = join(root, resources, row.webPath);
  await mkdir(dirname(target), { recursive: true });
  const temp = `${target}.${randomUUID()}.tmp`;
  const source = join(root, webSource(row));
  try {
    try {
      await verifyFile(source, webHash(row), row.examId);
      await copyFile(source, temp);
    } catch (error) {
      if (!(error instanceof Error && error.message.includes('missing audio asset')) || offline || row.webLocalSourcePath) throw error;
      const response = await fetch(row.officialDownloadUrl, { signal: AbortSignal.timeout(60_000) });
      if (!response.ok || !response.body) throw new Error(`${row.examId}: download failed HTTP ${response.status}`);
      await pipeline(Readable.fromWeb(response.body as never), createWriteStream(temp, { flags: 'wx' }));
    }
    await verifyFile(temp, webHash(row), row.examId);
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
  for (const row of rows) await verifyFile(join(dist, row.webPath), webHash(row), row.examId);
}

/** Fetches the media paths as a browser would and checks every response body. */
export async function verifyServing(rows: readonly ExamAudioAssetRow[], baseUrl: string): Promise<void> {
  validate(rows);
  for (const row of rows) {
    const response = await fetch(new URL(row.webPath, baseUrl), { signal: AbortSignal.timeout(60_000) });
    if (!response.ok || !response.body) throw new Error(`${row.examId}: served audio HTTP ${response.status}`);
    const hash = createHash('sha256');
    for await (const chunk of Readable.fromWeb(response.body as never)) hash.update(chunk);
    if (hash.digest('hex') !== webHash(row)) throw new Error(`${row.examId}: served audio SHA-256 mismatch`);
  }
}

/** Rechecks authoritative original recordings independently from the web delivery payload. */
export async function verifyOriginalSources(rows: readonly ExamAudioAssetRow[], root: string): Promise<void> {
  validate(rows);
  for (const row of rows) await verifyFile(join(root, row.localSourcePath), row.sha256, row.examId);
}

/** Checks that every exam's DSL task path is its manifest path. */
export async function verifyTaskPaths(rows: readonly ExamAudioAssetRow[], root: string): Promise<void> {
  validate(rows);
  for (const row of rows) {
    const examDir = dirname(row.localSourcePath).replace(/^ExamBin\/pl\//, 'exams/pl/certyfikat-polski/');
    const source = await readFile(join(root, examDir, 'listening-tasks.core-data'), 'utf8');
    const urls = [...source.matchAll(/^\s*audioUrl:\s*"?([^"\s]+)"?\s*$/gm)].map(match => match[1]);
    if (urls.length !== 5 || urls.some(url => url !== row.webPath)) {
      throw new Error(`${row.examId}: expected five listening task audioUrl values matching ${row.webPath}`);
    }
  }
}

async function main(args: string[]): Promise<void> {
  const root = resolve(fileURLToPath(new URL('..', import.meta.url)));
  verifyPublishedAssetCoverage(EXAM_AUDIO_ASSET_ROW_ROWS, EXAM_PUBLICATION_ROW_ROWS.map(row => row.examRef));
  const [mode, value] = args;
  if (mode === 'prepare') {
    await verifyTaskPaths(EXAM_AUDIO_ASSET_ROW_ROWS, root);
    await prepareAudioAssets(EXAM_AUDIO_ASSET_ROW_ROWS, root, args.includes('--offline'));
  }
  else if (mode === 'check-dist' && value) await verifyDistribution(EXAM_AUDIO_ASSET_ROW_ROWS, resolve(value));
  else if (mode === 'check-url' && value) await verifyServing(EXAM_AUDIO_ASSET_ROW_ROWS, value);
  else if (mode === 'check-paths') await verifyTaskPaths(EXAM_AUDIO_ASSET_ROW_ROWS, root);
  else if (mode === 'check-source') await verifyOriginalSources(EXAM_AUDIO_ASSET_ROW_ROWS, root);
  else throw new Error('Usage: tsx scripts/prepare-exam-audio.ts prepare [--offline] | check-dist <directory> | check-url <base-url> | check-paths | check-source');
  console.log(`PASS exam audio ${mode}: ${EXAM_AUDIO_ASSET_ROW_ROWS.length} assets`);
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  main(process.argv.slice(2)).catch(error => { console.error(error); process.exitCode = 1; });
}
