// R1.8a (Plans/Biome/R1Design.md): the frequency rows against dsl/frequency.data-contract: what `biome data` rejects, with the exact position of each problem.
import { beforeAll, describe, expect, it } from 'vitest';
import { execFileSync } from 'node:child_process';
import { createHash } from 'node:crypto';
import { join } from 'node:path';
import { biome, hasCli, repo, row, rowsOf } from './support/course-data';

const freq = (rank: number, lemma: string, change: Record<string, string | undefined> = {}) =>
  row('FREQ', { rank: `${rank}`, lemma: `"${lemma}"`, count: '10' }, change);

describe.skipIf(!hasCli)('frequency rows', () => {
  const { check, problem } = rowsOf('FrequencyRow');
  it('a rank that is a text is DATA_TYPE at the value', () => problem([freq(1, 'w', { rank: '"1"' })], 0, '"1"', 'DATA_TYPE'));
  it('a count with a fraction is DATA_TYPE at the value', () => problem([freq(1, 'w', { count: '1.5' })], 0, '1.5', 'DATA_TYPE'));
  it('a lemma left out is DATA_REQUIRED at the { of the row', () => problem([freq(1, 'w', { lemma: undefined })], 0, '{', 'DATA_REQUIRED'));
  it('an empty file is DATA_COUNT at 1:1', () => expect(check('').problems).toEqual(['frequency.core-data:1:1: DATA_COUNT']));
  it('a lemma may repeat, as in the corpus the list was cut from, and non-ASCII is data', () =>
    expect(check([freq(1, 'być'), freq(2, 'być')].join('\n') + '\n')).toEqual({ status: 0, problems: [] }));
});

// R1.19: the en-ru list is a file of its own, checked and not connected to anything (a step without --target). Its JSON left the tree, so the comparison reads it from the commit that still has it.
describe.skipIf(!hasCli)('the en-ru frequency rows against the sealed frequency-top1000.json', () => {
  let raw: Buffer;
  let run: ReturnType<typeof biome>;
  let rows: { rank: number; lemma: string; count: number }[];
  let items: any[];
  beforeAll(() => {
    raw = execFileSync('git', ['show', '7358aeb72ab79dce80fca1faadba6be64892fa18:courses/en-ru/frequency-top1000.json'], { cwd: repo });
    run = biome('read', join(repo, 'courses-biome/en-ru/frequency.core-data'), '--format', 'json');
    rows = (JSON.parse(run.stdout) as { value: { rank: number; lemma: string; count: number } }[]).map(r => r.value);
    items = JSON.parse(raw.toString('utf8')).items;
  });

  it('is the file that the pin in R1Receipt.md and ATTRIBUTION.md names (sha256 f64d9dce...)', () =>
    expect(createHash('sha256').update(raw).digest('hex')).toBe('f64d9dcebdff358135aec3b6031763c553f3cbc8f2603e525ea40c82c5050253'));
  it('has the 1000 items of the file in rank order, each with its lemma and count', () => {
    expect(run.status, run.stderr).toBe(0);
    expect(rows).toHaveLength(1000);
    expect(rows.map(r => r.rank)).toEqual(Array.from({ length: 1000 }, (_, i) => i + 1));
    expect(rows).toEqual(items);
    expect([rows[0], rows[999]]).toEqual([{ rank: 1, lemma: 'the', count: 186631452 }, { rank: 1000, lemma: 'administrative', count: 268268 }]);
  });
});
