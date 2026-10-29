// R1.8a (Plans/Biome/R1Design.md): the frequency rows against dsl/frequency.data-contract: what `biome data` rejects, with the exact position of each problem.
import { describe, expect, it } from 'vitest';
import { hasCli, row, rowsOf } from './support/course-data';

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
