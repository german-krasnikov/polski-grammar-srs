import { createHash } from 'node:crypto';
import { spawnSync } from 'node:child_process';
import { mkdtemp, mkdir, rm, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { dirname, join } from 'node:path';
import { afterEach, expect, it } from 'vitest';
import type { ExamAudioAssetRow } from '../dsl/exam-assets/generated/ExamAudioRows.ts';
import { verifyWebAudioArchive } from '../scripts/verify-exam-web-audio-archive.ts';

const roots: string[] = [];
afterEach(async () => { await Promise.all(roots.splice(0).map(path => rm(path, { recursive: true, force: true }))); });

it('accepts exactly the pinned effective web member without needing the original', async () => {
  const root = await mkdtemp(join(tmpdir(), 'polski-archive-'));
  roots.push(root);
  const webLocalSourcePath = 'ExamBin/web/B2/2023-02/audio.mp3';
  const bytes = Buffer.from('web derivative bytes');
  const row: ExamAudioAssetRow = {
    examId: 'certyfikat-polski-B2-2023-02', officialDownloadUrl: 'https://example.invalid/source.mp3',
    sha256: createHash('sha256').update('original').digest('hex'),
    localSourcePath: 'ExamBin/pl/B2/2023-02/audio.mp3',
    webLocalSourcePath, webSha256: createHash('sha256').update(bytes).digest('hex'),
    webPath: 'exam-audio/B2-2023-02.mp3',
  };
  const archive = join(root, 'audio.tar');
  await mkdir(dirname(join(root, webLocalSourcePath)), { recursive: true });
  await writeFile(join(root, webLocalSourcePath), bytes);
  const pack = (...members: string[]) => {
    const result = spawnSync('tar', ['-cf', archive, '-C', root, ...members], { encoding: 'utf8' });
    expect(result.status, result.stderr).toBe(0);
  };
  pack(webLocalSourcePath);
  await expect(verifyWebAudioArchive(archive, [row])).resolves.toBeUndefined();
  await writeFile(join(root, webLocalSourcePath), 'corrupt');
  pack(webLocalSourcePath);
  await expect(verifyWebAudioArchive(archive, [row])).rejects.toThrow(/SHA-256/);
  await writeFile(join(root, 'extra.txt'), 'extra');
  pack(webLocalSourcePath, 'extra.txt');
  await expect(verifyWebAudioArchive(archive, [row])).rejects.toThrow(/member list/);
});
