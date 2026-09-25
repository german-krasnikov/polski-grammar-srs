import { expect, test } from 'vitest';
import course from '../courses/pl-ru/course.json';
import { courseRussianSupport } from '../src/data/course';

const expected = {
  title: { full: 'Опора на русский: что переносится, а что проверить', compact: 'Опора на русский' },
  columns: ['Русская опора', 'Польская конструкция', 'Проверка'],
  rows: [
    {
      id: 'accusative', cue: 'вижу кого? что?',
      react: { construction: 'Widzę moją żonę.', check: 'Логика винительного знакома; польские окончания нужно менять во всей группе.' },
      web: { construction: 'Widzę moją żonę.', check: 'Польские окончания меняются во всей группе.' },
      desktop: { construction: 'Widzę moją żonę.', check: 'Окончания меняются во всей группе' },
      mobileLine: 'вижу кого? что? → Widzę moją żonę. Окончания меняются во всей группе.',
    },
    {
      id: 'instrumental', cue: 'с моей женой',
      react: { construction: 'z moją żoną', check: 'Польское женское -ą соответствует здесь творительному; это же окончание есть у прилагательного в Bierniku.' },
      web: { construction: 'z moją żoną', check: 'Женское -ą здесь соответствует творительному.' },
      desktop: { construction: 'z moją żoną', check: 'Женское -ą — творительный' },
      mobileLine: 'с моей женой → z moją żoną. Женское -ą — творительный.',
    },
    {
      id: 'locative', cue: 'говорю о жене',
      react: { construction: 'mówię o żonie', check: 'Местный падеж требует предлога; żona → żonie.' },
      web: { construction: 'mówię o żonie', check: 'Местный падеж требует предлога; żona → żonie.' },
      desktop: { construction: 'mówię o żonie', check: 'Местный требует предлога' },
      mobileLine: 'говорю о жене → mówię o żonie. Местный требует предлога.',
    },
    {
      id: 'possessive', cue: 'мой / его / их',
      react: { construction: 'moją żonę / jego żonę / ich żonę', check: 'jego, jej, ich не склоняются. Формы mojego и mojej зависят от предмета обладания.' },
      web: { construction: 'moją żonę / jego żonę / ich żonę', check: 'jego, jej, ich не склоняются.' },
      desktop: { construction: 'moją / jego / ich żonę', check: 'jego, jej, ich не склоняются' },
      mobileLine: 'мой / его / их → moją / jego / ich żonę. jego, jej, ich не склоняются.',
    },
  ],
};

test('course pack owns every existing Russian-support wording and order', () => {
  const withoutComparisons = (support: typeof course.reference.russianSupport) => ({
    ...support,
    rows: support.rows.map(({ comparisons: _comparisons, ...row }) => row),
  });
  expect(withoutComparisons(course.reference.russianSupport)).toEqual(expected);
  expect(withoutComparisons(courseRussianSupport)).toEqual(expected);
});
