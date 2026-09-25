import { describe, expect, test } from 'vitest';
import course from '../courses/pl-ru/course.json';

type ChainPack = { training?: { chainPresentation?: {
  steps: Array<{ id: string; label: string }>;
  completion: { title: string; reactEyebrow: string; reactBody: string; webBody: string };
} } };

const training = (course as ChainPack).training;

describe('authored training chain presentation', () => {
  test('keeps five ordered training labels separate from reference examples', () => {
    expect(training?.chainPresentation?.steps).toEqual([
      { id: 'acc', label: 'Вижу' },
      { id: 'past', label: 'Прошлое' },
      { id: 'neg', label: 'Отрицание' },
      { id: 'owner', label: 'Владелец' },
      { id: 'loc', label: 'Говорю о' },
    ]);
    expect(training?.chainPresentation?.steps.map(step => step.label))
      .not.toEqual(course.reference.chainRows.map(row => row.label));
  });

  test('preserves each host completion variant', () => {
    expect(training?.chainPresentation?.completion).toEqual({
      title: 'Цепочка завершена',
      reactEyebrow: '5 преобразований',
      reactBody: 'Ты изменил время, отрицание, владельца и падеж, сохранив одну мысль. Оценки сохранены в расписании повторений.',
      webBody: 'Пять преобразований завершены. Оценки сохранены в расписании повторений.',
    });
  });
});

import { validateCoursePack } from '../scripts/validate-course.mjs';
import frequency from '../courses/pl-ru/frequency-top1000.json';

const edited = () => structuredClone(course);

test('rejects missing, duplicate, reordered and blank chain presentation', () => {
  const missing = edited();
  delete (missing as Partial<typeof missing>).training;
  expect(() => validateCoursePack(missing, frequency)).toThrow(/training/);

  const duplicate = edited();
  duplicate.training.chainPresentation.steps[1].id = duplicate.training.chainPresentation.steps[0].id;
  expect(() => validateCoursePack(duplicate, frequency)).toThrow(/training\/chainPresentation\/steps\/1\/id/);

  const reordered = edited();
  [reordered.training.chainPresentation.steps[0], reordered.training.chainPresentation.steps[1]] =
    [reordered.training.chainPresentation.steps[1], reordered.training.chainPresentation.steps[0]];
  expect(() => validateCoursePack(reordered, frequency)).toThrow(/training\/chainPresentation\/steps\/0\/id/);

  const blank = edited();
  blank.training.chainPresentation.completion.webBody = '  ';
  expect(() => validateCoursePack(blank, frequency)).toThrow(/training\/chainPresentation\/completion\/webBody/);

  const shortReference = edited();
  shortReference.reference.chainRows.pop();
  expect(() => validateCoursePack(shortReference, frequency)).toThrow(/reference\/chainRows/);
});
