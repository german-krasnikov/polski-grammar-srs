// R1.18 (Plans/Biome/R1Design.md): the four style rows of courses-biome/styles/styles.core-data against the sealed courses/styles/*.json, which left the tree in this slice, so they are read from the commit that
// still has them (git show <rev>:path, sha256 pinned), and against dsl/style.data-contract: what `biome data check` rejects, with the exact position of each problem.
// The data cases need a biome-dsl checkout (see course-data.test.ts); without one they are skipped, which is NOT RUN, not a pass.
import { describe, expect, it } from 'vitest';
import { execFileSync } from 'node:child_process';
import { createHash } from 'node:crypto';
import { join } from 'node:path';
import { biome, hasCli, repo, row, rowsIn } from './support/course-data';

const REV = '7358aeb72ab79dce80fca1faadba6be64892fa18';
/** The file names in the order the Gradle task embedded them, with the sha256 of each. */
const SEALED: [string, string][] = [
  ['minimal-theory', '3dc89ab8a1549bd3ffa44de0b6e49c93eb0d371291221733e14171e800190375'],
  ['native-contrast', 'e62dd012c32f470b818a154cbac09dfe91f89d1ec5428955a9b52418e0869e99'],
  ['rule-first', '2b34a01937f8e80f77fa40d47bb99e5416179aba47c2e234a2d53663beefd305'],
  ['situation-first', 'f29d62285b12357685970490fa2f80b4f3a4e39814cd0145ec6610a6423dba2f'],
];

describe.skipIf(!hasCli)('the style rows against the sealed courses/styles/*.json', () => {
  const run = biome('read', join(repo, 'courses-biome/styles/styles.core-data'), '--format', 'json');
  const rows = (JSON.parse(run.stdout) as { value: any }[]).map(r => r.value);
  const sealed = SEALED.map(([name, sha256]) => {
    const raw = execFileSync('git', ['show', `${REV}:courses/styles/${name}.json`], { cwd: repo });
    expect(createHash('sha256').update(raw).digest('hex'), `courses/styles/${name}.json at ${REV}`).toBe(sha256);
    return JSON.parse(raw.toString('utf8'));
  });

  it('has the four rows, in the order of the file names', () => {
    expect(run.status, run.stderr).toBe(0);
    expect(rows.map(r => r.id)).toEqual(SEALED.map(([name]) => name));
  });

  it('carries the id, label, description, blocks, requires and fallback of every file, and nothing else', () => {
    const regrouped = rows.map(r => ({
      id: r.id, label: r.label, description: r.description, blocks: { front: r.front, back: r.back },
      ...(r.requires === undefined ? {} : { requires: r.requires }), ...(r.fallback === undefined ? {} : { fallback: r.fallback }),
    }));
    expect(regrouped).toEqual(sealed);
  });
});

describe.skipIf(!hasCli)('style rows against dsl/style.data-contract', () => {
  const { check, problem } = rowsIn('styles', 'StyleRecipeRow');
  const fields = (id: string): Record<string, string> => ({
    id: `"${id}"`, label: '{ru: "r" en: "e"}', description: '{ru: "r" en: "e"}', front: '[table formula]', back: '[rule changes]',
  });
  const style = (id: string, change: Record<string, string | undefined> = {}) => row('STYLE', fields(id), change);

  it('a style with a fallback to a later style, optional requires and every kind of block is accepted', () =>
    expect(check([style('a', { requires: '[nativeParallel]', fallback: '"b"', front: '[formula table rule scene]', back: '[nativeParallel examples whyOnDemand changes contrast]' }), style('b')].join('\n') + '\n'))
      .toEqual({ status: 0, problems: [] }));
  it('a kind of block that is none is DATA_ENUM at the value', () => problem([style('a', { front: '[table formla]' })], 0, 'formla', 'DATA_ENUM'));
  it('a kind in requires that is none is DATA_ENUM at the value', () => problem([style('a', { requires: '[scene sceen]' })], 0, 'sceen', 'DATA_ENUM'));
  it('a label without its English text is DATA_REQUIRED at the { of the label', () => problem([style('a', { label: '{ru: "r"}' })], 0, '{ru: "r"}', 'DATA_REQUIRED'));
  it('a description of the wrong kind is DATA_TYPE at the value', () => problem([style('a', { description: '"just text"' })], 0, '"just text"', 'DATA_TYPE'));
  it('a fallback to a style that is none is DATA_REF at the value', () => problem([style('a', { fallback: '"nowhere"' })], 0, '"nowhere"', 'DATA_REF'));
  it('a style written twice is DATA_DUPLICATE at its id', () => problem([style('a'), style('a')], 1, '"a"', 'DATA_DUPLICATE'));
  it('a style without its back blocks is DATA_REQUIRED at the { of the row', () => problem([style('a', { back: undefined })], 0, '{', 'DATA_REQUIRED'));
  it('no row at all is DATA_COUNT at 1:1', () => expect(check('').problems).toEqual(['styles.core-data:1:1: DATA_COUNT']));
});
