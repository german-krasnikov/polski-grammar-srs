import { expect, test } from 'vitest';
import course from '../courses/pl-ru/course.json';

test('course pack owns the four ordered system map cards', () => {
  expect(course.reference.systemCards).toEqual([
    { id: 'noun', title: 'Существительное', explanation: 'Род × число × падеж', example: 'żona → żonę → żony',
      steps: ['żona', 'żonę', 'żony'] },
    { id: 'agreement', title: 'Прилагательное и владелец', explanation: 'Копируют род, число и падеж', example: 'moja piękna → moją piękną',
      steps: ['moja piękna', 'moją piękną'] },
    { id: 'verb', title: 'Глагол', explanation: 'Лицо × число × время × вид', example: 'widzę → widziałem → będę widzieć',
      steps: ['widzę', 'widziałem', 'będę widzieć'] },
    { id: 'modifiers', title: 'Модификаторы', explanation: 'Отрицание · вопрос · владелец', example: 'Widzę… → Nie widzę… → Czy widzę…?',
      steps: ['Widzę…', 'Nie widzę…', 'Czy widzę…?'] },
  ]);
  course.reference.systemCards.forEach((card) => {
    expect(card.steps.join(' → ')).toBe(card.example);
  });
});
