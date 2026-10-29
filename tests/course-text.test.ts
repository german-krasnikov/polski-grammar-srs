// R1.7 (Plans/Biome/R1Design.md): the seeds and the text dictionaries against dsl/course.data-contract: what `biome data` rejects, with the exact position of each problem.
import { describe, expect, it } from 'vitest';
import { hasCli, row, rowsOf } from './support/course-data';

const seed = (nounId: string, adjectiveId: string, change: Record<string, string | undefined> = {}) =>
  row('SEED', { nounId: `"${nounId}"`, adjectiveId: `"${adjectiveId}"` }, change);
const entry = (tag: string, key: string, value = '"v"') => row(tag, { key: `"${key}"`, value });

describe.skipIf(!hasCli)('sentence seed rows', () => {
  const { check, problem } = rowsOf('SeedRow', 'NounRow', 'AdjectiveRow');
  it('a noun that does not exist is DATA_REF at the value', () => problem([seed('nobody', 'good')], 0, '"nobody"', 'DATA_REF'));
  it('an adjective that does not exist is DATA_REF at the value', () => problem([seed('wife', 'nothing')], 0, '"nothing"', 'DATA_REF'));
  it('a seed without its adjective is DATA_REQUIRED at the { of the row', () => problem([seed('wife', 'good', { adjectiveId: undefined })], 0, '{', 'DATA_REQUIRED'));
  it('a seed of a real noun and a real adjective passes', () => expect(check(seed('wife', 'good') + '\n')).toEqual({ status: 0, problems: [] }));
});

describe.skipIf(!hasCli)('dictionary rows: copy, patterns, prefixes', () => {
  for (const [record, tag] of [['CopyEntryRow', 'COPY'], ['PatternEntryRow', 'PATTERN'], ['PrefixEntryRow', 'PREFIX']]) {
    const { check, problem } = rowsOf(record);
    it(`${tag}: a second row with the same key is DATA_DUPLICATE at its key`, () => problem([entry(tag, 'a'), entry(tag, 'a')], 1, '"a"', 'DATA_DUPLICATE'));
    it(`${tag}: a number for the text is DATA_TYPE at the value`, () => problem([entry(tag, 'a', '5')], 0, '5', 'DATA_TYPE'));
    it(`${tag}: an empty file is DATA_COUNT at 1:1`, () => expect(check('').problems).toEqual([expect.stringMatching(/:1:1: DATA_COUNT$/)]));
    it(`${tag}: a text with braces, quotes and non-ASCII passes`, () => expect(check(entry(tag, 'a', '"Widzę {acc}. «tak» \\"x\\""') + '\n')).toEqual({ status: 0, problems: [] }));
  }
});
