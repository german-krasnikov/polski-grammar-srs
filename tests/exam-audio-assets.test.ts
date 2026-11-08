import { createHash } from 'node:crypto';
import { mkdtemp, mkdir, readFile, rm, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { createServer } from 'node:http';
import { afterEach, expect, it } from 'vitest';
import type { ExamAudioAssetRow } from '../dsl/exam-assets/generated/ExamAudioRows.ts';
import { prepareAudioAssets, verifyDistribution, verifyServing } from '../scripts/prepare-exam-audio.ts';

const tempRoots: string[] = [];
afterEach(async () => { await Promise.all(tempRoots.splice(0).map(path => rm(path, { recursive: true, force: true }))); });
async function fixture() {
  const root = await mkdtemp(join(tmpdir(), 'polski-audio-'));
  tempRoots.push(root);
  const data = Buffer.from('small IO sentinel; no playable media');
  const row: ExamAudioAssetRow = {
    examId: 'test-B1-2020', officialDownloadUrl: 'https://example.invalid/audio.mp3',
    sha256: createHash('sha256').update(data).digest('hex'),
    localSourcePath: 'ExamBin/pl/B1/2020/audio.mp3', webPath: 'exam-audio/B1-2020.mp3',
  };
  const source = join(root, row.localSourcePath);
  const target = join(root, 'kotlin/composeApp/src/webMain/resources', row.webPath);
  await mkdir(join(root, 'ExamBin/pl/B1/2020'), { recursive: true });
  return { root, row, data, source, target };
}

it('fails closed when source is absent and leaves no output', async () => {
  const f = await fixture();
  await expect(prepareAudioAssets([f.row], f.root, true)).rejects.toThrow(/missing/i);
  await expect(readFile(f.target)).rejects.toMatchObject({ code: 'ENOENT' });
});

it('rejects wrong source bytes without replacing an existing verified asset', async () => {
  const f = await fixture();
  await writeFile(f.source, f.data);
  await prepareAudioAssets([f.row], f.root, true);
  await writeFile(f.source, 'corrupt');
  await expect(prepareAudioAssets([f.row], f.root, true)).rejects.toThrow(/sha-256/i);
  expect(await readFile(f.target)).toEqual(f.data);
});

it('copies verified bytes and detects missing or corrupt distribution media', async () => {
  const f = await fixture();
  await writeFile(f.source, f.data);
  await prepareAudioAssets([f.row], f.root, true);
  expect(await readFile(f.target)).toEqual(f.data);
  const dist = join(f.root, 'dist');
  await expect(verifyDistribution([f.row], dist)).rejects.toThrow(/missing/i);
  await mkdir(join(dist, 'exam-audio'), { recursive: true });
  await writeFile(join(dist, f.row.webPath), 'corrupt');
  await expect(verifyDistribution([f.row], dist)).rejects.toThrow(/sha-256/i);
  await writeFile(join(dist, f.row.webPath), f.data);
  await expect(verifyDistribution([f.row], dist)).resolves.toBeUndefined();
});

it('does not publish an earlier row when a later row is missing', async () => {
  const f = await fixture();
  await writeFile(f.source, f.data);
  const missing: ExamAudioAssetRow = {
    ...f.row,
    examId: 'test-B2-2017',
    localSourcePath: 'ExamBin/pl/B2/2017/audio.mp3',
    webPath: 'exam-audio/B2-2017.mp3',
  };
  await expect(prepareAudioAssets([f.row, missing], f.root, true)).rejects.toThrow(/missing/i);
  await expect(readFile(f.target)).rejects.toMatchObject({ code: 'ENOENT' });
});


it('checks served media bytes, not only a successful HTTP status', async () => {
  const f = await fixture();
  let body: Buffer = Buffer.from('corrupt');
  const server = createServer((_request, response) => {
    response.writeHead(200, { 'content-type': 'audio/mpeg' });
    response.end(body);
  });
  await new Promise<void>(resolve => server.listen(0, '127.0.0.1', resolve));
  try {
    const address = server.address();
    if (!address || typeof address === 'string') throw new Error('Expected TCP address');
    const base = `http://127.0.0.1:${address.port}/`;
    await expect(verifyServing([f.row], base)).rejects.toThrow(/sha-256/i);
    body = f.data;
    await expect(verifyServing([f.row], base)).resolves.toBeUndefined();
  } finally {
    await new Promise<void>((resolve, reject) => server.close(error => error ? reject(error) : resolve()));
  }
});
