// @vitest-environment jsdom
import { afterEach, beforeEach, expect, test } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from '../src/ui/App';

beforeEach(() => localStorage.clear());
afterEach(cleanup);

test('first meeting introduces the skill without an answer or review', async () => {
  const user = userEvent.setup();
  render(<App />);
  expect(screen.getByRole('button', { name: 'Перейти к заданию' })).toBeTruthy();
  expect(screen.queryByText('Widzę moją piękną żonę.')).toBeNull();
  expect(localStorage.getItem('polski-grammar-srs-v1')).toBeNull();
  await user.selectOptions(screen.getByRole('combobox', { name: 'Подача объяснений' }), 'situations');
  expect(document.body.textContent).not.toContain('Widzę moją piękną żonę.');
  await user.click(screen.getByRole('button', { name: 'Перейти к заданию' }));
  expect(screen.getByRole('button', { name: /Показать ответ/ })).toBeTruthy();
  expect(document.body.textContent).not.toContain('Widzę moją piękną żonę.');
  expect(localStorage.getItem('polski-grammar-srs-v1')).toBeNull();
});

test('switching methods preserves the current draft, reveal, and single review', async () => {
  const user = userEvent.setup();
  const app = render(<App />);
  const method = screen.getByRole('combobox', { name: 'Подача объяснений' });
  await user.selectOptions(method, 'situations');
  expect(document.querySelector('.method-introduce')?.textContent).toContain('кого или что видишь');
  await user.click(screen.getByRole('button', { name: 'Перейти к заданию' }));
  const source = document.querySelector('.source-sentence')?.textContent;
  await user.click(screen.getByRole('button', { name: 'Напечатать ответ' }));
  const input = screen.getByRole('textbox', { name: 'Ответ по-польски' });
  await user.type(input, 'Moja próba');
  await user.selectOptions(method, 'logic');
  expect(document.querySelector('.source-sentence')?.textContent).toBe(source);
  expect(input).toHaveProperty('value', 'Moja próba');
  expect(localStorage.getItem('polski-grammar-srs-v1')).toBeNull();
  await user.click(screen.getByRole('button', { name: /Проверить и показать ответ/ }));
  expect(document.querySelector('.typed-result p')?.textContent).toBe('Moja próba');
  expect(document.querySelector('.method-feedback h3')?.textContent).toBe('Разбор изменений');
  expect(document.querySelectorAll('.method-feedback p')).toHaveLength(1);
  await user.selectOptions(method, 'situations');
  expect(document.querySelector('.typed-result p')?.textContent).toBe('Moja próba');
  expect(document.querySelector('.method-feedback')?.textContent).toContain('эталоном');
  expect(document.querySelector('.method-feedback h3')?.textContent).toBe('Сравни смысл и форму');
  expect(document.querySelectorAll('.method-feedback p')).toHaveLength(2);
  expect(localStorage.getItem('polski-grammar-srs-v1')).toBeNull();
  await user.click(screen.getByRole('button', { name: /2 Вспомнил/ }));
  const saved = JSON.parse(localStorage.getItem('polski-grammar-srs-v1')!);
  expect(saved.totalReviews).toBe(1);
  expect(saved.stats['case.acc.f'].reviews).toBe(1);
  expect(localStorage.getItem('polski-explanation-method-v1')).toBe('situations');
  app.unmount();
  render(<App />);
  expect(screen.getByRole('combobox', { name: 'Подача объяснений' })).toHaveProperty('value', 'situations');
});

test('continuing a first encounter focuses the newly rendered answer action', async () => {
  const user = userEvent.setup();
  render(<App />);
  await user.click(screen.getByRole('button', { name: 'Перейти к заданию' }));
  expect(document.activeElement).toBe(screen.getByRole('button', { name: /Показать ответ/ }));
  await user.click(screen.getByRole('button', { name: /Показать ответ/ }));
  await user.click(screen.getByRole('button', { name: /Вспомнил/ }));
  expect(screen.getByRole('button', { name: 'Перейти к заданию' })).toBeTruthy();
  await user.click(screen.getByRole('button', { name: 'Перейти к заданию' }));
  expect(document.activeElement).toBe(screen.getByRole('button', { name: /Показать ответ/ }));
});

test('reference toggle waits for retrieval before showing the current card forms', async () => {
  const user = userEvent.setup();
  render(<App />);
  expect(screen.getByRole('button', { name: 'Таблица под рукой' }).hasAttribute('disabled')).toBe(true);
  expect(document.querySelector('.reference-panel')).toBeNull();
  await user.selectOptions(screen.getByRole('combobox', { name: 'Подача объяснений' }), 'situations');
  expect(document.querySelector('.reference-panel')).toBeNull();
  await user.click(screen.getByRole('button', { name: 'Перейти к заданию' }));
  await user.click(screen.getByRole('button', { name: 'Таблица под рукой' }));
  expect(document.querySelector('.reference-panel .compact-table')).toBeTruthy();
  expect(document.querySelector('.reference-panel')?.textContent).toContain('moją piękną żonę');
  await user.click(screen.getByRole('button', { name: 'Напечатать ответ' }));
  await user.type(screen.getByRole('textbox', { name: 'Ответ по-польски' }), 'Moja próba');
  await user.click(screen.getByRole('button', { name: 'Скрыть таблицу' }));
  await user.click(screen.getByRole('button', { name: 'Таблица под рукой' }));
  expect(screen.getByRole('textbox', { name: 'Ответ по-польски' })).toHaveProperty('value', 'Moja próba');
  await user.click(screen.getByRole('button', { name: /Проверить и показать ответ/ }));
  await user.click(screen.getByRole('button', { name: /Вспомнил/ }));
  expect(document.querySelector('.reference-panel')).toBeNull();
  expect(screen.getByRole('button', { name: 'Таблица под рукой' }).hasAttribute('disabled')).toBe(true);
  await user.click(screen.getByRole('button', { name: 'Перейти к заданию' }));
  expect(document.querySelector('.reference-panel .compact-table')).toBeTruthy();
});
