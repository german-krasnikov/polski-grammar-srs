// R1.9 (Plans/Biome/R1Design.md): the reference row against dsl/course.data-contract: what `biome data` rejects, with the exact position of each problem.
// The row is 16 KB of texts, so each case changes the real row by one text instead of building a second copy of it.
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

describe.skipIf(!hasCli)('reference row', () => {
  const { check, problem } = rowsOf('ReferenceRow', 'SkillRow', 'NounRow');
  it('the real row passes', () => expect(check(real.join('\n') + '\n')).toEqual({ status: 0, problems: [] }));
  it('a case row that names a skill that does not exist is DATA_REF at the value', () =>
    problem(changed('skill: "case.gen.neg"', 'skill: "case.gen.ng"'), ROW, '"case.gen.ng"', 'DATA_REF'));
  it('a comparison noun that is no noun is DATA_REF at the value', () =>
    problem(changed('comparisonNounIds: ["husband"', 'comparisonNounIds: ["husbnd"'), ROW, '"husbnd"', 'DATA_REF'));
  it('an aspect row with its present left out is DATA_REQUIRED at the { of the row (null is written)', () =>
    problem(changed(' present: "robię"', ''), ROW, '{label: "robić · процесс"', 'DATA_REQUIRED'));
  it('the dashed gender key of the old data is no key any more', () =>
    problem(changed('genderNames: {m_personal:', 'genderNames: {m-personal:'), ROW, '{m-personal:', 'DATA_REQUIRED'));
  it('a field the contract does not know is DATA_UNKNOWN_FIELD at its key', () =>
    problem(changed('{id: "nom" pl:', '{extra: 1 id: "nom" pl:'), ROW, 'extra:', 'DATA_UNKNOWN_FIELD'));
  it('a text that is a number is DATA_TYPE at the value', () => problem(changed('matrixIntroduction: "', 'matrixIntroduction: 5 xx: "'), ROW, '5', 'DATA_TYPE'));
});
