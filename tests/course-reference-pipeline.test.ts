import { expect, test } from 'vitest';
import course from '../courses/pl-ru/course.json';
import { courseReferencePipeline, courseReferencePipelineSummary } from '../src/data/course';

const expected = {
  title: 'Сначала конструкция, затем формы',
  steps: [
    { id: 'intent', label: '01 · Смысл', question: 'Что хочу сказать?', example: 'Вижу / не вижу / говорю о…' },
    { id: 'case', label: '02 · Операция', question: 'Какой падеж нужен?', example: 'widzę → Biernik' },
    { id: 'agreement', label: '03 · Согласование', question: 'Меняю всю группу', example: 'moją + piękną + żonę' },
  ],
  compactExample: 'widzę → Biernik → moją + piękną + żonę',
};

test('course pack owns the exact ordered pipeline used by the map', () => {
  expect(course.reference.pipeline).toEqual(expected);
  expect(courseReferencePipeline).toEqual(expected);
  expect(courseReferencePipelineSummary()).toBe('Что хочу сказать? → Какой падеж нужен? → Меняю всю группу');
  expect(course.reference.pipeline).toEqual(expected);
});
