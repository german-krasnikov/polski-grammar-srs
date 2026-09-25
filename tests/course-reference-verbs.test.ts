import { expect, test } from 'vitest';
import course from '../courses/pl-ru/course.json';

test('course pack owns ordered verb subjects and host-specific teaching copy', () => {
  const teaching = (course.reference as unknown as Record<string, unknown>).verbTeaching as {
    subjects: Array<{ id: string; person: number; number: string; genderMode: string; fixedGender?: string; label: { full: string; compact: string } }>;
    genderControlLabel: { full: string; compact: string };
    genderOptions: Array<{ id: string; label: { full: string; compact: string } }>;
    tenseLabels: Record<string, { full: string; compact: string }>;
    futureExplanation: { react: string; compact: string };
  };
  expect(teaching.subjects.map(row => [row.id, row.person, row.number, row.genderMode, row.fixedGender ?? null])).toEqual([
    ['ja', 1, 'sg', 'selected', null], ['ty', 2, 'sg', 'selected', null],
    ['on', 3, 'sg', 'fixed', 'm-personal'], ['ona', 3, 'sg', 'fixed', 'f'], ['ono', 3, 'sg', 'fixed', 'n'],
    ['my', 1, 'pl', 'selected', null], ['wy', 2, 'pl', 'selected', null],
    ['oni', 3, 'pl', 'fixed', 'm-personal'], ['one', 3, 'pl', 'fixed', 'f'],
  ]);
  expect(teaching.subjects.map(row => row.label.full)).toEqual([
    'ja — я', 'ty — ты', 'on — он', 'ona — она', 'ono — оно', 'my — мы', 'wy — вы',
    'oni — мужская личная группа', 'one — остальные',
  ]);
  expect(teaching.subjects.map(row => row.label.compact)).toEqual([
    'ja — я', 'ty — ты', 'on — он', 'ona — она', 'ono — оно', 'my — мы', 'wy — вы',
    'oni — мужская группа', 'one — остальные',
  ]);
  expect(teaching.genderControlLabel).toEqual({ full: 'Род для ja / ty / my / wy', compact: 'Род' });
  expect(teaching.genderOptions).toEqual([
    { id: 'm', label: { full: 'Мужской / мужская личная группа', compact: 'Мужской' } },
    { id: 'f', label: { full: 'Женский / женская группа', compact: 'Женский' } },
  ]);
  expect(teaching.tenseLabels).toEqual({
    present: { full: 'Teraz · сейчас', compact: 'Teraz' },
    past: { full: 'Przeszłość · прошлое', compact: 'Przeszłość' },
    future: { full: 'Przyszłość · будущее', compact: 'Przyszłość' },
  });
  expect(teaching.futureExplanation).toEqual({
    react: 'Составное будущее: będę + инфинитив (będę robić) или форма на -ł с родом и числом (będę robił / robiła). У być: będę, без второго глагола.',
    compact: 'Составное будущее: będę + инфинитив или форма на -ł. У być: będę, без второго глагола.',
  });
});
