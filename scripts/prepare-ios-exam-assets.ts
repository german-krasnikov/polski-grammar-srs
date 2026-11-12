import { createHash } from 'node:crypto';
import { spawnSync } from 'node:child_process';
import { copyFileSync, existsSync, mkdirSync, readFileSync, readdirSync, rmSync } from 'node:fs';
import { basename, dirname, join, resolve } from 'node:path';
import { EXAM_AUDIO_ASSET_ROW_ROWS } from '../dsl/exam-assets/generated/ExamAudioRows';

const root = resolve(import.meta.dirname, '..');
const destination = join(root, 'kotlin/iosApp/PolskiGrammar/ExamAssets');
const mode = process.argv[2];
if (mode !== 'prepare' && mode !== 'check' && mode !== 'check-app') throw new Error('Use prepare, check, or check-app <app-directory>');
const source = (path: string) => join(root, path);

/** Use the same public `biome read` command as the published-row pipeline. */
function biomeCli(): string {
  if (process.env.BIOME_BIN) {
    const cli = resolve(process.env.BIOME_BIN);
    if (!existsSync(cli)) throw new Error(`BIOME_BIN does not exist: ${cli}`);
    return cli;
  }
  const relativeCli = 'Python/biome-dsl/implementations/typescript/packages/biome/src/bin.mjs';
  for (let directory = root; ; directory = dirname(directory)) {
    const candidate = join(directory, relativeCli);
    if (existsSync(candidate)) return candidate;
    if (directory === dirname(directory)) break;
  }
  throw new Error('Set BIOME_BIN to the neighboring Biome checkout packages/biome/src/bin.mjs');
}

function rows<T>(cli: string, file: string, tag: string): T[] {
  const run = spawnSync(process.execPath, [cli, 'read', source(file)], {
    cwd: root, encoding: 'utf8', timeout: 120_000,
  });
  if (run.status !== 0) throw new Error(`biome read ${file} failed: ${run.stderr || run.error?.message || run.status}`);
  const parsed: { tag: string; value: T }[] = JSON.parse(run.stdout);
  if (!Array.isArray(parsed) || parsed.some((entry) => entry.tag !== tag)) {
    throw new Error(`Unexpected ${tag} rows in ${file}`);
  }
  return parsed.map((entry) => entry.value);
}

const cli = biomeCli();
const publications = rows<{ examRef: string }>(cli, 'dsl/exam-published/generated/ExamPublicationRow.core-data', 'EXAM_PUBLICATION');
if (!publications.length) throw new Error('Published catalog is empty');
const publishedIds = new Set(publications.map((row) => row.examRef));
if (publishedIds.size !== publications.length) throw new Error('Duplicate published exam reference');
const audio = EXAM_AUDIO_ASSET_ROW_ROWS.filter((row) => publishedIds.has(row.examId));
if (audio.length !== publishedIds.size || new Set(audio.map((row) => row.examId)).size !== audio.length) {
  throw new Error('Published audio manifest is incomplete or duplicated');
}
const images = new Set<string>();
for (const [file, tag] of [
  ['ListeningTaskRow.core-data', 'LISTENING_TASK'],
  ['WritingTaskRow.core-data', 'WRITING_TASK'],
] as const) {
  for (const row of rows<{ examRef: string; promptImageUrl?: string }>(cli, `dsl/exam-published/generated/${file}`, tag)) {
    if (publishedIds.has(row.examRef) && row.promptImageUrl) images.add(row.promptImageUrl);
  }
}
const expected = new Set<string>();
if (mode === 'prepare') { rmSync(destination, { recursive: true, force: true }); mkdirSync(destination, { recursive: true }); }
for (const row of audio) {
  if (!/^exam-audio\/[A-Za-z0-9-]+\.mp3$/.test(row.webPath) ||
      !/^ExamBin\/pl\/[A-Za-z0-9-]+\/[A-Za-z0-9-]+\/audio\.mp3$/.test(row.localSourcePath)) {
    throw new Error(`Invalid audio path: ${row.examId}`);
  }
  const from = source(row.localSourcePath);
  const name = basename(row.webPath);
  const to = join(destination, name);
  const hash = createHash('sha256').update(readFileSync(from)).digest('hex');
  if (hash !== row.sha256) throw new Error(`Audio hash mismatch: ${row.examId}`);
  if (expected.has(name)) throw new Error(`Duplicate staged asset name: ${name}`);
  expected.add(name);
  if (mode === 'prepare') copyFileSync(from, to);
  if (!existsSync(to) || createHash('sha256').update(readFileSync(to)).digest('hex') !== hash) throw new Error(`Staged audio mismatch: ${name}`);
}
for (const image of images) {
  if (!/^exam-images\/[A-Za-z0-9-]+\.(?:png|jpeg|jpg)$/.test(image)) throw new Error(`Invalid image path: ${image}`);
  const from = source(`kotlin/composeApp/src/webMain/resources/${image}`);
  const name = basename(image);
  const to = join(destination, name);
  if (expected.has(name)) throw new Error(`Duplicate staged asset name: ${name}`);
  expected.add(name);
  if (mode === 'prepare') copyFileSync(from, to);
  if (!existsSync(to) || !readFileSync(to).equals(readFileSync(from))) throw new Error(`Staged image mismatch: ${name}`);
}
const staged = new Set(readdirSync(destination));
if (staged.size !== expected.size || [...staged].some((name) => !expected.has(name))) throw new Error('Staged exam asset set differs from published rows');
if (mode === 'check-app') {
  const app = process.argv[3] && resolve(process.argv[3]);
  if (!app || !existsSync(app)) throw new Error('check-app requires a built .app directory');
  for (const name of expected) {
    if (!existsSync(join(app, name)) || !readFileSync(join(app, name)).equals(readFileSync(join(destination, name)))) {
      throw new Error(`Bundled asset differs from verified staged source: ${name}`);
    }
  }
}
console.log(`${mode === 'prepare' ? 'Prepared' : 'Checked'} ${audio.length} pinned audio and ${images.size} published images${mode === 'check-app' ? ' in app bundle' : ''}`);
