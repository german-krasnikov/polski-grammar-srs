// @vitest-environment jsdom
import { afterEach, beforeEach, expect, it, vi } from 'vitest';
import { cleanup, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import VocabularyView from '../src/ui/VocabularyView';
import fourCards from './fixtures/vocabulary-four-cards.json';

beforeEach(() => localStorage.clear());
afterEach(() => { cleanup(); vi.restoreAllMocks(); });

it('keeps an unsaved custom word in the editor when storage rejects the write', async () => {
  const user = userEvent.setup();
  render(<VocabularyView />);
  await user.type(screen.getByLabelText('Польское слово'), 'szkoła');
  await user.type(screen.getByLabelText('Перевод', { exact: true }), 'школа');
  await user.type(screen.getByLabelText('Форма'), 'szkoły');
  await user.type(screen.getByLabelText('Пример в предложении'), 'To jest szkoła.');
  vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => { throw new Error('Нет места'); });
  await user.click(screen.getByRole('button', { name: 'Добавить слово' }));
  expect(screen.getByRole('alert').textContent).toContain('Нет места');
  expect((screen.getByLabelText('Польское слово') as HTMLInputElement).value).toBe('szkoła');
  expect(localStorage.getItem('polski-vocabulary-pl-ru-v1')).toBeNull();
});

it('shows exact frequency coverage and keeps unavailable ranks unselectable', async () => {
  const user = userEvent.setup();
  render(<VocabularyView />);
  await user.selectOptions(screen.getByLabelText('Подборка слов'), '100');
  expect(screen.getByText('Готово 7/100 · недоступно 93')).toBeTruthy();
  expect(screen.getAllByRole('checkbox')).toHaveLength(100);
  expect(screen.getAllByRole('checkbox').filter(input => (input as HTMLInputElement).disabled)).toHaveLength(93);
  await user.selectOptions(screen.getByLabelText('Подборка слов'), '500');
  expect(screen.getByText('Готово 23/500 · недоступно 477')).toBeTruthy();
  await user.selectOptions(screen.getByLabelText('Подборка слов'), '1000');
  expect(screen.getByText('Готово 30/1000 · недоступно 970')).toBeTruthy();
});

it('does not promote an imported custom homonym into a frequency card', async () => {
  const custom = { id: 'user.00000000-0000-4000-8000-000000000001', lemma: 'w', translation: 'в',
    form: 'w', example: 'Jestem w domu.', level: '—', frequencyRank: null, custom: true };
  localStorage.setItem('polski-vocabulary-pl-ru-v1', JSON.stringify({
    version: 1, pair: 'pl-ru', selectedIds: [custom.id], custom: [custom], cards: {},
  }));
  const user = userEvent.setup();
  render(<VocabularyView />);
  await user.selectOptions(screen.getByLabelText('Подборка слов'), '100');
  const frequencyRow = screen.getByText('w').closest('label')!;
  const frequencyCheckbox = frequencyRow.querySelector('input[type="checkbox"]') as HTMLInputElement;
  expect(frequencyCheckbox.disabled).toBe(true);
  expect(frequencyCheckbox.checked).toBe(false);
  expect(screen.getByText('Готово 7/100 · недоступно 93')).toBeTruthy();
  await user.selectOptions(screen.getByLabelText('Подборка слов'), 'mine');
  const ownRow = screen.getByText('w').closest('label')!;
  const ownCheckbox = ownRow.querySelector('input[type="checkbox"]') as HTMLInputElement;
  expect(ownCheckbox.disabled).toBe(false);
  expect(ownCheckbox.checked).toBe(true);
});

it('leaves saved vocabulary untouched when file import conflicts on a custom lemma', async () => {
  const local = { id: 'user.00000000-0000-4000-8000-000000000001', lemma: 'szkoła', translation: 'школа',
    form: 'szkoła', example: 'To jest szkoła.', level: 'A1', frequencyRank: null, custom: true };
  const saved = JSON.stringify({ version: 1, pair: 'pl-ru', selectedIds: [local.id], custom: [local], cards: {} });
  localStorage.setItem('polski-vocabulary-pl-ru-v1', saved);
  const writes = vi.spyOn(Storage.prototype, 'setItem');
  render(<VocabularyView />);
  const imported = JSON.stringify({ version: 1, pair: 'pl-ru', selectedIds: [], custom: [
    { ...local, id: 'user.00000000-0000-4000-8000-000000000002', lemma: ' SZKOŁA ' },
  ], cards: {} });
  const file = new File([imported], 'conflict.json', { type: 'application/json' });
  Object.defineProperty(file, 'text', { value: async () => imported });
  await userEvent.setup().upload(document.querySelector('input[type="file"]') as HTMLInputElement, file);
  await waitFor(() => expect(screen.getByRole('alert').textContent).toContain('уже есть'));
  expect(writes).not.toHaveBeenCalled();
  expect(localStorage.getItem('polski-vocabulary-pl-ru-v1')).toBe(saved);
});

it('restores a repaired legacy custom homonym from recovery without losing its ID', async () => {
  localStorage.setItem('polski-vocabulary-pl-ru-v1', '{broken');
  const repairedDocument = structuredClone(fourCards);
  repairedDocument.custom[0].lemma = 'żona';
  const repaired = JSON.stringify(repairedDocument);
  render(<VocabularyView />);
  const input = document.querySelector('input[type="file"]') as HTMLInputElement;
  const user = userEvent.setup();
  const wrongPair = repaired.replace('"pair":"pl-ru"', '"pair":"de-ru"');
  const invalid = new File([wrongPair], 'wrong-pair.json', { type: 'application/json' });
  Object.defineProperty(invalid, 'text', { value: async () => wrongPair });
  await user.upload(input, invalid);
  await waitFor(() => expect(screen.getByRole('alert').textContent).toContain('Неподдерживаемый формат'));
  expect(localStorage.getItem('polski-vocabulary-pl-ru-v1')).toBe('{broken');
  const file = new File([repaired], 'repaired.json', { type: 'application/json' });
  Object.defineProperty(file, 'text', { value: async () => repaired });
  await user.upload(input, file);
  await waitFor(() => expect(localStorage.getItem('polski-vocabulary-pl-ru-v1')).not.toBe('{broken'));
  expect(JSON.parse(localStorage.getItem('polski-vocabulary-pl-ru-v1')!)).toEqual(repairedDocument);
});

it('imports a legacy shipped homonym with all four histories into a fresh profile', async () => {
  const backup = structuredClone(fourCards);
  backup.custom[0].lemma = 'żona';
  render(<VocabularyView />);
  const raw = JSON.stringify(backup);
  const file = new File([raw], 'old-vocabulary.json', { type: 'application/json' });
  Object.defineProperty(file, 'text', { value: async () => raw });
  await userEvent.setup().upload(document.querySelector('input[type="file"]') as HTMLInputElement, file);
  await waitFor(() => expect(localStorage.getItem('polski-vocabulary-pl-ru-v1')).not.toBeNull());
  expect(JSON.parse(localStorage.getItem('polski-vocabulary-pl-ru-v1')!)).toEqual(backup);
});

it('keeps the answer visible when a review cannot be saved', async () => {
  const user = userEvent.setup();
  render(<VocabularyView />);
  await user.click(screen.getAllByRole('checkbox')[0]);
  await user.click(screen.getByRole('button', { name: 'Показать ответ' }));
  vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => { throw new Error('Нет места'); });
  await user.click(screen.getByRole('button', { name: /Вспомнил/ }));
  expect(screen.getByRole('alert').textContent).toContain('Нет места');
  expect(screen.getByText('Эталон · польский')).toBeTruthy();
  expect(screen.getByRole('button', { name: /Вспомнил/ })).toBeTruthy();
});
