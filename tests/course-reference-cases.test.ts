import { expect, test } from 'vitest';
import course from '../courses/pl-ru/course.json';
import { courseCaseTeaching, courseComparisonNounIds } from '../src/data/course';

test('course pack owns the two existing case notes, reading hint and noun order', () => {
  const teaching = {
    caseNote: {
      react: 'Wołacz показан как форма обращения; с неодушевлёнными словами обычно используется только стилистически. «Zachwycam się…» = «Восхищаюсь…» (Narzędnik), «Przyglądam się…» — Celownik.',
      compact: 'Wołacz — форма обращения. «Zachwycam się…» требует Narzędnik; «Przyglądam się…» — Celownik.',
    },
    comparisonReadingHint: 'Читай по строке, чтобы сравнить типы. По столбцу — чтобы увидеть все формы одного слова.',
  };
  expect(course.reference.caseTeaching).toEqual(teaching);
  expect(courseCaseTeaching).toEqual(teaching);
  const ids = [
    'husband', 'friendM', 'dog', 'house', 'wife', 'book', 'child',
  ];
  expect(course.reference.comparisonNounIds).toEqual(ids);
  expect(courseComparisonNounIds).toEqual(ids);
});
