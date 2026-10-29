// R1.10a (Plans/Biome/R1Design.md): the case and verb teaching and the future auxiliary against dsl/course.data-contract: what `biome data` rejects, with the exact position of each problem.
// The reference row is 16 KB of texts, so each case changes the real row by one text instead of building a second copy of it.
import { readFileSync } from 'node:fs';
import { describe, expect, it } from 'vitest';
import { hasCli, realFile, row, rowsOf } from './support/course-data';

const real = readFileSync(realFile('ReferenceRow'), 'utf8').split('\n').filter(Boolean);
const ROW = real.length - 1;
/** The file's lines with one text of the row replaced; the text has to be in the real row. */
const changed = (from: string, to: string) => {
  expect(real[ROW], `${from} is in the real row`).toContain(from);
  return [...real.slice(0, ROW), real[ROW].replace(from, to)];
};

describe.skipIf(!hasCli)('verb and case teaching in the reference row', () => {
  const { problem, check } = rowsOf('ReferenceRow', 'SkillRow', 'NounRow', 'AdjectiveRow', 'PronounRow', 'PossessiveRow');
  it('a gender mode outside the enum is DATA_ENUM at the value', () => problem(changed('genderMode: "selected"', 'genderMode: "chosen"'), ROW, '"chosen"', 'DATA_ENUM'));
  it('a subject without its label is DATA_REQUIRED at the { of the subject', () =>
    problem(changed(' label: {full: "ja — я" compact: "ja — я"}', ''), ROW, '{id: "ja" person: 1', 'DATA_REQUIRED'));
  it('a subject selected with a fixed gender passes the contract: the mapper refuses it (PlRuTeachingRowsTest)', () =>
    expect(check(changed('genderMode: "selected" label: {full: "ja', 'genderMode: "selected" fixedGender: "f" label: {full: "ja').join('\n') + '\n')).toEqual({ status: 0, problems: [] }));
  it('a tense label left out is DATA_REQUIRED at the { of the labels', () =>
    problem(changed(' future: {full: "Przyszłość · будущее" compact: "Przyszłość"}', ''), ROW, '{present: {full: "Teraz', 'DATA_REQUIRED'));
});

describe.skipIf(!hasCli)('morphology row', () => {
  const { check, problem } = rowsOf('MorphologyRow', 'VerbRow');
  const forms = (persons: string) => `{sg: ${persons} pl: ${persons}}`;
  const persons = '{p1: "a" p2: "b" p3: "c"}';
  const morphology = (change: Record<string, string | undefined> = {}) => row('MORPHOLOGY', { futureAuxiliary: `{verbId: "be" forms: ${forms(persons)}}` }, change);
  it('an auxiliary that is no verb is DATA_REF at the value', () => problem([morphology({ futureAuxiliary: `{verbId: "bee" forms: ${forms(persons)}}` })], 0, '"bee"', 'DATA_REF'));
  it('the quoted person key of the old data is no key any more', () =>
    problem([morphology({ futureAuxiliary: `{verbId: "be" forms: ${forms('{"1": "a" "2": "b" "3": "c"}')}}` })], 0, '{"1"', 'DATA_REQUIRED'));
  it('an auxiliary of a verb of the pack with all six forms passes', () => expect(check(morphology() + '\n')).toEqual({ status: 0, problems: [] }));
});
