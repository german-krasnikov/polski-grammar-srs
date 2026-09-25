// @vitest-environment jsdom
import { expect, test } from 'vitest';
import { renderToStaticMarkup } from 'react-dom/server';
import { FormContrast } from '../src/ui/FormContrast';

test('a form comparison shows full visible Polish forms with ordered text labels', () => {
  const html = renderToStaticMarkup(<FormContrast from="moja żona" to="moją żonę" />);
  const root = document.createElement('div');
  root.innerHTML = html;
  const pair = root.querySelector('.form-contrast')!;
  const semantic = root.querySelector('.contrast-sr-only')!;
  expect(pair.getAttribute('aria-label')).toBe('Было: moja żona. Стало: moją żonę');
  expect(pair.getAttribute('aria-labelledby')).toBe(semantic.id);
  expect([...semantic.querySelectorAll('[lang="pl"]')].map(node => node.textContent)).toEqual(['moja żona', 'moją żonę']);
  expect([...pair.children].filter(node => !node.classList.contains('contrast-sr-only')).map(node => node.textContent).join(''))
    .toMatch(/Было:.*moja żona.*→.*Стало:.*moją żonę/);
});

test('an authored possessive replacement marks the whole owner word', () => {
  const html = renderToStaticMarkup(<FormContrast
    from="moją żonę" to="jego żonę"
    beforeParts={[{ text: 'moją', isChanged: true, isEnding: false }, { text: ' żonę', isChanged: false, isEnding: false }]}
    afterParts={[{ text: 'jego', isChanged: true, isEnding: false }, { text: ' żonę', isChanged: false, isEnding: false }]}
  />);
  const root = document.createElement('div');
  root.innerHTML = html;
  expect(root.querySelector('.change-before')?.textContent).toBe('moją');
  expect(root.querySelector('.change-after')?.textContent).toBe('jego');
  expect([...root.querySelector('.form-contrast')!.children].filter(node => !node.classList.contains('contrast-sr-only'))
    .map(node => node.textContent).join('')).toBe('Было:moją żonę→Стало:jego żonę');
});
