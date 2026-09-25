import { expect, test } from 'vitest';
import course from '../courses/pl-ru/course.json';
import frequency from '../courses/pl-ru/frequency-top1000.json';
import { validateCoursePack } from '../scripts/validate-course.mjs';

test('course validator rejects an omitted middle training step', () => {
  const broken = structuredClone(course);
  broken.training.chainPresentation.steps.splice(2, 1);
  expect(() => validateCoursePack(broken, frequency)).toThrow(/training\/chainPresentation\/steps/);
});

test('course validator rejects a blank visible step label', () => {
  const broken = structuredClone(course);
  broken.training.chainPresentation.steps[3].label = '   ';
  expect(() => validateCoursePack(broken, frequency)).toThrow(/training\/chainPresentation\/steps\/3\/label/);
});
