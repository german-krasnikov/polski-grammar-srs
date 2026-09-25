// @vitest-environment jsdom
import { afterEach, expect, test } from 'vitest';
import { createElement } from 'react';
import { cleanup, render } from '@testing-library/react';
import GrammarTables from '../src/ui/GrammarTables';
import { courseReferenceSystemCards } from '../src/data/course';

afterEach(cleanup);

test('system map renders every authored card in pack order', () => {
  const { container } = render(createElement(GrammarTables, { onTrain: () => {} }));
  const rendered = Array.from(container.querySelectorAll('.system-grid article')).map(article => ({
    title: article.querySelector('h4')?.textContent,
    explanation: article.querySelector('p')?.textContent,
    example: article.querySelector('code')?.textContent,
  }));

  expect(rendered).toEqual(courseReferenceSystemCards.map(({ title, explanation, example }) => ({
    title, explanation, example,
  })));
});
