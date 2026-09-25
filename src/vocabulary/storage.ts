import type { SerializedCard, StoredCard, RatingName } from '../types';
import { isDue, newSkillCard, review } from '../srs/scheduler';
import { validateVocabularyItem, vocabularyById, vocabularyItems, type VocabularyItem } from './catalog';

const KEY = 'polski-vocabulary-pl-ru-v1';
export type StudyDirection = 'ru-pl' | 'pl-ru';
export interface VocabularyDocument {
  version: 1;
  pair: 'pl-ru';
  selectedIds: string[];
  custom: VocabularyItem[];
  cards: Record<string, SerializedCard>;
}

export const freshVocabulary = (): VocabularyDocument => ({ version: 1, pair: 'pl-ru', selectedIds: [], custom: [], cards: {} });
export const wordCardKey = (id: string, direction: StudyDirection) => `pl-ru:vocabulary:${direction}:${id}`;

export function validateVocabulary(value: unknown): VocabularyDocument {
  if (!value || typeof value !== 'object') throw new Error('Словарь повреждён');
  const doc = value as VocabularyDocument;
  if (doc.version !== 1 || doc.pair !== 'pl-ru' || !Array.isArray(doc.selectedIds) ||
      !Array.isArray(doc.custom) || !doc.cards || typeof doc.cards !== 'object' || Array.isArray(doc.cards)) {
    throw new Error('Неподдерживаемый формат словаря');
  }
  const custom = doc.custom.map(validateVocabularyItem);
  if (new Set(custom.map(item => item.id)).size !== custom.length ||
      new Set(doc.selectedIds).size !== doc.selectedIds.length ||
      !doc.selectedIds.every(id => typeof id === 'string' && vocabularyById(id, custom))) {
    throw new Error('Словарь содержит неизвестные или повторяющиеся ID');
  }
  for (const [key, card] of Object.entries(doc.cards)) {
    if (!/^pl-ru:vocabulary:(ru-pl|pl-ru):/.test(key) || !card || typeof card !== 'object' ||
        typeof card.due !== 'string' || Number.isNaN(new Date(card.due).getTime()) ||
        !['stability', 'difficulty', 'elapsed_days', 'scheduled_days', 'reps', 'lapses', 'learning_steps', 'state']
          .every(field => Number.isFinite((card as unknown as Record<string, number>)[field]))) {
      throw new Error('Повреждена карточка интервального повторения');
    }
  }
  return { ...doc, custom };
}

export function loadVocabulary(): { document: VocabularyDocument; recoveryRaw?: string; error?: string } {
  let raw: string | null = null;
  try {
    raw = localStorage.getItem(KEY);
    return { document: raw ? validateVocabulary(JSON.parse(raw)) : freshVocabulary() };
  } catch (error) {
    return { document: freshVocabulary(), recoveryRaw: raw ?? undefined, error: error instanceof Error ? error.message : 'Не удалось открыть словарь' };
  }
}
export const saveVocabulary = (document: VocabularyDocument) => localStorage.setItem(KEY, JSON.stringify(validateVocabulary(document)));
export const exportVocabulary = (document: VocabularyDocument) => JSON.stringify(validateVocabulary(document), null, 2);

export function setWordSelected(document: VocabularyDocument, id: string, selected: boolean): VocabularyDocument {
  if (!vocabularyById(id, document.custom)) throw new Error('Слово не найдено');
  const ids = new Set(document.selectedIds);
  if (selected) ids.add(id); else ids.delete(id);
  return { ...document, selectedIds: [...ids] };
}

export function dueWordIds(document: VocabularyDocument, direction: StudyDirection): string[] {
  return document.selectedIds.filter(id => {
    const key = wordCardKey(id, direction);
    return isDue({ skillId: key, card: document.cards[key] ?? newSkillCard(key).card });
  });
}

export function reviewWord(document: VocabularyDocument, id: string, direction: StudyDirection, rating: Extract<RatingName, 'again' | 'good'>): VocabularyDocument {
  if (!document.selectedIds.includes(id)) throw new Error('Слово не выбрано');
  const key = wordCardKey(id, direction);
  const stored: StoredCard = { skillId: key, card: document.cards[key] ?? newSkillCard(key).card };
  return { ...document, cards: { ...document.cards, [key]: review(stored, rating).card } };
}

/** An empty local document restores v1 backups; populated documents merge with local IDs and reviews winning. */
export function mergeVocabulary(current: VocabularyDocument, imported: unknown): VocabularyDocument {
  const other = validateVocabulary(imported);
  const incoming = other.custom.filter(item => !current.custom.some(local => local.id === item.id));
  const lemmaKey = (lemma: string) => lemma.trim().toLocaleLowerCase('pl');
  const restoring = !current.selectedIds.length && !current.custom.length && !Object.keys(current.cards).length;
  const existingLemmas = new Set((restoring ? [] : [...vocabularyItems, ...current.custom])
    .map(item => lemmaKey(item.lemma)));
  for (const item of incoming) {
    const key = lemmaKey(item.lemma);
    if (existingLemmas.has(key)) throw new Error('Это польское слово уже есть в словаре.');
    existingLemmas.add(key);
  }
  const custom = [...current.custom, ...incoming];
  const selectedIds = [...new Set([...current.selectedIds, ...other.selectedIds])];
  return validateVocabulary({ ...current, custom, selectedIds, cards: { ...other.cards, ...current.cards } });
}
