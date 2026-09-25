import { createHash } from 'node:crypto';
import { readFileSync } from 'node:fs';
import { expect, test } from 'vitest';
import { verbForm } from '../src/grammar/engine';
import grammar from './fixtures/kotlin-parity/grammar.json';
import course from '../courses/pl-ru/course.json';
import frequency from '../courses/pl-ru/frequency-top1000.json';
import { validateCoursePack } from '../scripts/validate-course.mjs';

test('the pre-migration grammar oracle remains pinned and covers every owner slot', () => {
  const sha256 = createHash('sha256')
    .update(readFileSync('tests/fixtures/kotlin-parity/grammar.json'))
    .digest('hex');
  expect(grammar.sourceRevision).toBe('df59774e153a5bdf590fe27bd8780c7bd5457a26');
  expect(sha256).toBe('f17084de1f877860701b43614668104c4f5817923465b2d4d70fec44bdacbfb9');

  const possessives = grammar.cases.filter(item => item.id.startsWith('G-POSS-'));
  expect(possessives).toHaveLength(490);
  expect(new Set(possessives.map(item => item.id)).size).toBe(490);
  for (const owner of ['my', 'your', 'our', 'yourPlural', 'his', 'her', 'their']) {
    expect(possessives.filter(item => item.input.owner === owner), owner).toHaveLength(70);
  }
  expect(grammar.cases.filter(item => item.id.startsWith('G-VERB-be-future-')).map(item => item.expected.form))
    .toEqual(['będę', 'będziesz', 'będzie', 'będziemy', 'będziecie', 'będą']);
});

test('future-form data migration preserves unknown and perfective failures', () => {
  expect(() => verbForm('missing-verb', 'future', 1, 'sg')).toThrow('Unknown verb missing-verb');
  expect(() => verbForm('doDone', 'present', 1, 'sg')).toThrow('Perfective verbs have no present tense');
  expect(verbForm('doDone', 'future', 1, 'sg')).toBe('zrobię');
});

test('course validation locates an extra declined case', () => {
  const extraCase = structuredClone(course);
  const feminine = extraCase.possessives[0].forms.sg!.f as Record<string, string>;
  feminine.extra = 'nieznana';
  expect(() => validateCoursePack(extraCase, frequency)).toThrow(/possessives\/0\/forms\/sg\/f\/extra/);
});

test('course validation locates a missing declined case and gender', () => {
  const missingCase = structuredClone(course);
  delete (missingCase.possessives[0].forms.sg!.f as Record<string, string>).gen;
  expect(() => validateCoursePack(missingCase, frequency)).toThrow(/possessives\/0\/forms\/sg\/f\/gen/);

  const missingGender = structuredClone(course);
  delete (missingGender.possessives[0].forms.sg as Record<string, unknown>)['m-animate'];
  expect(() => validateCoursePack(missingGender, frequency)).toThrow(/possessives\/0\/forms\/sg\/m-animate/);
});

test('course validation locates an extra auxiliary person', () => {
  const extraPerson = structuredClone(course);
  const singular = extraPerson.morphology.futureAuxiliary.forms.sg as Record<string, string>;
  singular['4'] = 'będę';
  expect(() => validateCoursePack(extraPerson, frequency)).toThrow(/morphology\/futureAuxiliary\/forms\/sg\/4/);
});

test('course validation locates a missing auxiliary person', () => {
  const missingPerson = structuredClone(course);
  delete (missingPerson.morphology.futureAuxiliary.forms.pl as Record<string, string>)['3'];
  expect(() => validateCoursePack(missingPerson, frequency)).toThrow(/morphology\/futureAuxiliary\/forms\/pl\/3/);
});

test('course validation rejects an unknown auxiliary identity', () => {
  const unknownAuxiliary = structuredClone(course);
  unknownAuxiliary.morphology.futureAuxiliary.verbId = 'missing-verb';
  expect(() => validateCoursePack(unknownAuxiliary, frequency)).toThrow(/morphology\/futureAuxiliary\/verbId/);
});

test('course validation locates a blank declined form', () => {
  const blank = structuredClone(course);
  blank.possessives[0].forms.sg!.f.voc = ' \t ';
  expect(() => validateCoursePack(blank, frequency)).toThrow(/possessives\/0\/forms\/sg\/f\/voc/);
});

test('course validation rejects a changed invariant kind', () => {
  const changedKind = structuredClone(course);
  changedKind.possessives[2].forms.kind = 'declined';
  expect(() => validateCoursePack(changedKind, frequency)).toThrow(/possessives\/2\/forms/);
});
