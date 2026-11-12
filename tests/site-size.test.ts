import { mkdtemp, mkdir, rm, symlink, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { afterEach, expect, it } from 'vitest';
import { assertSiteBudget, measureSiteBytes, siteTargetBytes } from '../scripts/check-site-size.ts';

const roots: string[] = [];
afterEach(async () => { await Promise.all(roots.splice(0).map(path => rm(path, { recursive: true, force: true }))); });

it('counts nested and hidden files in the assembled site', async () => {
  const root = await mkdtemp(join(tmpdir(), 'polski-site-'));
  roots.push(root);
  await mkdir(join(root, 'exam-audio'));
  await writeFile(join(root, '.nojekyll'), 'x');
  await writeFile(join(root, 'exam-audio', 'file.mp3'), '1234');
  expect(await measureSiteBytes(root)).toBe(5);
  expect(() => assertSiteBudget(siteTargetBytes)).not.toThrow();
  expect(() => assertSiteBudget(siteTargetBytes + 1)).toThrow(/target/);
});

it('rejects symlinks instead of counting their target outside the site', async () => {
  const root = await mkdtemp(join(tmpdir(), 'polski-site-'));
  roots.push(root);
  await symlink(tmpdir(), join(root, 'outside'));
  await expect(measureSiteBytes(root)).rejects.toThrow(/symlink/);
});
