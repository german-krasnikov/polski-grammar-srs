// R1.10b (Plans/Biome/R1Design.md): the pronoun and possessive teaching and the Russian support against dsl/course.data-contract: what `biome data` rejects, with the exact position of each problem.
// The reference row is 16 KB of texts, so each case changes the real row by one text instead of building a second copy of it.
import { readFileSync } from 'node:fs';
import { describe, expect, it } from 'vitest';
import { hasCli, realFile, rowsOf } from './support/course-data';

const real = readFileSync(realFile('ReferenceRow'), 'utf8').split('\n').filter(Boolean);
const ROW = real.length - 1;
/** The file's lines with one text of the row replaced; the text has to be in the real row. */
const changed = (from: string, to: string) => {
  expect(real[ROW], `${from} is in the real row`).toContain(from);
  return [...real.slice(0, ROW), real[ROW].replace(from, to)];
};

describe.skipIf(!hasCli)('pronoun teaching and Russian support in the reference row', () => {
  const { problem, check } = rowsOf('ReferenceRow', 'SkillRow', 'NounRow', 'AdjectiveRow', 'PronounRow', 'PossessiveRow');
  it('a pronoun id of the table that is no pronoun is DATA_REF at the value', () => problem(changed('pronounIds: ["ja"', 'pronounIds: ["jaa"'), ROW, '"jaa"', 'DATA_REF'));
  it('an invariable owner that is no possessive is DATA_REF at the value', () => problem(changed('invariableOwnerIds: ["his"', 'invariableOwnerIds: ["hiss"'), ROW, '"hiss"', 'DATA_REF'));
  it('a demo noun that is no noun is DATA_REF at the value', () => problem(changed('nounId: "wife" adjectiveId', 'nounId: "wif" adjectiveId'), ROW, '"wif"', 'DATA_REF'));
  it('a special value for a pronoun that does not exist is DATA_REF at the value', () =>
    problem(changed('specialValues: [{pronounId: "ja"', 'specialValues: [{pronounId: "jaa"'), ROW, '"jaa" value', 'DATA_REF'));
  it('a context without its cue is DATA_REQUIRED at the { of the context', () =>
    problem(changed('cue: {full: "Idę z…" compact: "Idę z…" ios: "Idę"} ', ''), ROW, '{id: "inst"', 'DATA_REQUIRED'));
  it('a support row without its mobile line is DATA_REQUIRED at the { of the row', () =>
    problem(changed(' mobileLine: "вижу кого? что? → Widzę moją żonę. Окончания меняются во всей группе."', ''), ROW, '{id: "accusative"', 'DATA_REQUIRED'));
  it('an ending part whose changed flag is a text is DATA_TYPE at the value', () => problem(changed('isChanged: false', 'isChanged: "yes"'), ROW, '"yes"', 'DATA_TYPE'));
  it('a context may leave out its prefix and its special values (the real row does)', () => expect(check(real.join('\n') + '\n')).toEqual({ status: 0, problems: [] }));
});

describe.skipIf(!hasCli)('a pronoun the teaching names', () => {
  const withReference = rowsOf('PronounRow', 'ReferenceRow', 'SkillRow', 'NounRow', 'AdjectiveRow', 'PossessiveRow');
  it('cannot be removed: every place that names it is DATA_REF in reference.core-data', () => {
    const without = readFileSync(realFile('PronounRow'), 'utf8').split('\n').filter(line => !line.startsWith('PRONOUN {id: "ja"')).join('\n');
    const { status, problems } = withReference.check(without);
    expect(status).toBe(1);
    expect(problems.length).toBeGreaterThan(0);
    expect(problems.every(p => p.startsWith('reference.core-data:') && p.endsWith('DATA_REF'))).toBe(true);
  });
});
