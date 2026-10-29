// R1.6 (Plans/Biome/R1Design.md): the pronoun and possessive rows against dsl/course.data-contract: what `biome data` rejects, with the exact position of each problem.
import { describe, expect, it } from 'vitest';
import { cases, genders, hasCli, row, rowsOf } from './support/course-data';

const pronoun = (id: string, change: Record<string, string | undefined> = {}) => row('PRONOUN', { id: `"${id}"`, forms: cases(id) }, change);
const plural = `{m_personal: ${cases('pm')} other: ${cases('po')}}`;
const possessive = (id: string, change: Record<string, string | undefined> = {}) =>
  row('POSSESSIVE', { id: `"${id}"`, label: '"l"', forms: `{kind: declined sg: ${genders()} pl: ${plural}}` }, change);

describe.skipIf(!hasCli)('pronoun rows', () => {
  const { check, problem } = rowsOf('PronounRow');
  it('a case left out is DATA_REQUIRED at the { of the forms', () => problem([pronoun('a', { forms: cases('a', 'voc') })], 0, '{nom: "a-nom"', 'DATA_REQUIRED'));
  it('a second pronoun with the same id is DATA_DUPLICATE at its id', () => problem([pronoun('a'), pronoun('a')], 1, '"a"', 'DATA_DUPLICATE'));
  it('pronouns with all seven cases pass', () => expect(check([pronoun('a'), pronoun('b')].join('\n') + '\n')).toEqual({ status: 0, problems: [] }));
});

describe.skipIf(!hasCli)('possessive rows', () => {
  const { check, problem } = rowsOf('PossessiveRow');
  it('a kind outside the enum is DATA_ENUM at the value', () => problem([possessive('a', { forms: `{kind: soon value: "x"}` })], 0, 'soon', 'DATA_ENUM'));
  it('a plural group left out is DATA_REQUIRED at the { of the plural', () =>
    problem([possessive('a', { forms: `{kind: declined sg: ${genders()} pl: {m_personal: ${cases('pm')}}}` })], 0, '{m_personal: {nom: "pm-nom"', 'DATA_REQUIRED'));
  it('the dashed plural key of the old data is no key any more', () =>
    problem([possessive('a', { forms: `{kind: declined sg: ${genders()} pl: {m-personal: ${cases('pm')} other: ${cases('po')}}}` })], 0, '{m-personal:', 'DATA_REQUIRED'));
  it('a second possessive with the same id is DATA_DUPLICATE at its id', () => problem([possessive('a'), possessive('a')], 1, '"a"', 'DATA_DUPLICATE'));
  it('a declined and an invariable possessive pass', () =>
    expect(check([possessive('a'), possessive('b', { forms: '{kind: invariant value: "x"}' })].join('\n') + '\n')).toEqual({ status: 0, problems: [] }));
  it('a possessive that is both passes the contract: the mapper refuses it (PlRuPronounRowsTest)', () =>
    expect(check(possessive('a', { forms: `{kind: invariant value: "x" sg: ${genders()} pl: ${plural}}` }) + '\n')).toEqual({ status: 0, problems: [] }));
});
