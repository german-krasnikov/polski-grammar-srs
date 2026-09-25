import { courseVocabulary } from '../data/course';
import frequency from '../../courses/pl-ru/frequency-top1000.json';

export type WordLevel = 'A1' | 'A2' | 'B1' | 'B2' | 'C1' | 'C2' | '—';
export interface VocabularyItem {
  id: string;
  lemma: string;
  translation: string;
  form: string;
  example: string;
  level: WordLevel;
  frequencyRank: number | null;
  custom?: boolean;
}

const shipped = courseVocabulary as VocabularyItem[];
if (new Set(shipped.map(item => item.id)).size !== shipped.length ||
    shipped.some(item => !item.lemma || !item.translation || !item.example || !item.form)) {
  throw new Error('Invalid Polish vocabulary pack');
}
if (frequency.items.length !== 1000 || frequency.items.some((item, index) => item.rank !== index + 1)) {
  throw new Error('Invalid Polish frequency slice');
}

export const vocabularyItems = shipped;
export const frequencyItems = frequency.items;
export const frequencyTop = (count: 100 | 500 | 1000) => frequencyItems.slice(0, count);
export const vocabularyById = (id: string, custom: VocabularyItem[] = []) =>
  [...shipped, ...custom].find(item => item.id === id);

export function validateVocabularyItem(value: VocabularyItem): VocabularyItem {
  const item = { ...value,
    lemma: value.lemma.trim(), translation: value.translation.trim(),
    form: value.form.trim(), example: value.example.trim(),
  };
  if (![item.lemma, item.translation, item.form, item.example].every(text => text.length > 0 && text.length <= 240)) {
    throw new Error('Заполни слово, перевод, форму и пример (до 240 символов).');
  }
  if (!['A1', 'A2', 'B1', 'B2', 'C1', 'C2', '—'].includes(item.level)) throw new Error('Неизвестный уровень');
  if (!/^user\.[a-f0-9-]{36}$/.test(item.id) || item.frequencyRank !== null) throw new Error('Недопустимый ID своего слова');
  return item;
}
