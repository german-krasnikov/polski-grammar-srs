import { describe, expect, it } from 'vitest';
import { frequencyTop, vocabularyItems } from '../src/vocabulary/catalog';
import { dueWordIds, exportVocabulary, freshVocabulary, mergeVocabulary, reviewWord, setWordSelected, validateVocabulary, wordCardKey } from '../src/vocabulary/storage';
import baseline from './fixtures/vocabulary-baseline-32.json';
import legacy from './fixtures/react-vocabulary-sample.json';
import fourCards from './fixtures/vocabulary-four-cards.json';

describe('Polish vocabulary course', () => {
  it('keeps all legacy cards and both scheduled directions addressable', () => {
    const corrected = structuredClone(baseline.cards);
    const edits: Record<string, Record<string, string>> = {
      'noun.book': { form: 'książka · вин. książkę · род. książki' },
      'noun.friendM': { example: 'Mój kolega z pracy ma psa.' },
      'adjective.beautiful': { translation: 'прекрасный, очень красивый' },
      'adjective.new': { form: 'nowy · ж. nowa · ж. вин. nową' },
      'adjective.nice': { translation: 'симпатичный, приятный на вид' },
      'verb.like': { translation: 'нравиться (о занятии); любить делать что-либо' },
      'verb.buyDone': { form: 'kupić · буд. я kupię · прош. я (м.) kupiłem' },
      'verb.doDone': { form: 'zrobić · буд. я zrobię · прош. я (м.) zrobiłem' },
    };
    corrected.forEach(card => Object.assign(card, edits[card.id] ?? {}));
    expect(vocabularyItems).toEqual(corrected);
    expect(vocabularyItems.flatMap(item => [wordCardKey(item.id, 'ru-pl'), wordCardKey(item.id, 'pl-ru')]))
      .toEqual(baseline.cardKeys);
    const loaded = validateVocabulary(structuredClone(legacy));
    expect(loaded.selectedIds).toEqual(legacy.selectedIds);
    expect(loaded.cards[wordCardKey('noun.wife', 'ru-pl')]).toEqual(legacy.cards['pl-ru:vocabulary:ru-pl:noun.wife']);
    expect(exportVocabulary(loaded)).toBe(JSON.stringify(legacy, null, 2));
  });
  it('keeps top-100, 500 and 1000 cumulative and separates rank from level', () => {
    expect(frequencyTop(100)).toEqual(frequencyTop(500).slice(0, 100));
    expect(frequencyTop(500)).toEqual(frequencyTop(1000).slice(0, 500));
    expect(vocabularyItems).toHaveLength(32);
    expect(vocabularyItems.every(item => item.translation && item.form && item.example)).toBe(true);
  });

  it('keeps independent recall schedules and history after deselection', () => {
    const selected = setWordSelected(freshVocabulary(), 'noun.wife', true);
    expect(dueWordIds(selected, 'ru-pl')).toEqual(['noun.wife']);
    expect(dueWordIds(selected, 'pl-ru')).toEqual(['noun.wife']);
    const reviewed = reviewWord(selected, 'noun.wife', 'ru-pl', 'good');
    expect(reviewed.cards[wordCardKey('noun.wife', 'ru-pl')].reps).toBe(1);
    expect(reviewed.cards[wordCardKey('noun.wife', 'pl-ru')]).toBeUndefined();
    const removed = setWordSelected(reviewed, 'noun.wife', false);
    expect(dueWordIds(removed, 'ru-pl')).toEqual([]);
    expect(removed.cards[wordCardKey('noun.wife', 'ru-pl')]).toEqual(reviewed.cards[wordCardKey('noun.wife', 'ru-pl')]);
  });

  it('round-trips one shipped and one custom ID with all four direction keys', () => {
    const decoded = validateVocabulary(structuredClone(fourCards));
    expect(decoded.selectedIds).toEqual(fourCards.selectedIds);
    expect(decoded.custom).toEqual(fourCards.custom);
    expect(Object.keys(decoded.cards)).toEqual(Object.keys(fourCards.cards));
    expect(JSON.parse(exportVocabulary(decoded))).toEqual(fourCards);
  });

  it('rejects a new imported custom lemma collision before changing local cards', () => {
    const current = validateVocabulary(structuredClone(fourCards));
    const before = structuredClone(current);
    const anotherId = 'user.00000000-0000-4000-8000-000000000002';
    const imported = { ...freshVocabulary(), custom: [{ ...current.custom[0], id: anotherId, lemma: ' SZKOŁA ' }] };
    expect(() => mergeVocabulary(current, imported)).toThrow(/польское слово уже есть/i);
    expect(current).toEqual(before);
    expect(() => mergeVocabulary(current, { ...freshVocabulary(), custom: [
      { ...current.custom[0], id: anotherId, lemma: 'ŻONA' },
    ] })).toThrow(/польское слово уже есть/i);
  });

  it('keeps a previously saved custom homonym of a shipped card readable', () => {
    const saved = validateVocabulary({ ...freshVocabulary(), custom: [
      { ...fourCards.custom[0], lemma: 'żona' },
    ], selectedIds: [fourCards.custom[0].id] });
    expect(mergeVocabulary(saved, freshVocabulary()).custom).toEqual(saved.custom);
    const another = { ...fourCards.custom[0], id: 'user.00000000-0000-4000-8000-000000000002', lemma: 'w' };
    expect(mergeVocabulary(saved, { ...freshVocabulary(), custom: [another] }).custom)
      .toEqual([...saved.custom, another]);
  });

  it('restores a legacy shipped homonym and all four histories into an empty profile', () => {
    const backup = structuredClone(fourCards);
    backup.custom[0].lemma = 'żona';
    const restored = mergeVocabulary(freshVocabulary(), backup);
    expect(restored).toEqual(backup);
    expect(restored.cards).toEqual(fourCards.cards);
  });

  it('rejects duplicate custom lemmas inside an imported backup', () => {
    const backup = structuredClone(fourCards);
    backup.custom.push({ ...backup.custom[0], id: 'user.00000000-0000-4000-8000-000000000002', lemma: ' SZKOŁA ' });
    expect(() => mergeVocabulary(freshVocabulary(), backup)).toThrow(/польское слово уже есть/i);
  });

  it('merges new IDs and archived cards while local reviews win matching keys', () => {
    const current = validateVocabulary(structuredClone(fourCards));
    const added = { ...fourCards.custom[0], id: 'user.00000000-0000-4000-8000-000000000002', lemma: 'w' };
    const reviewKey = wordCardKey('noun.wife', 'ru-pl');
    const archivedKey = 'pl-ru:vocabulary:pl-ru:user.archived';
    const imported = { ...freshVocabulary(), selectedIds: [added.id], custom: [added],
      cards: { [reviewKey]: { ...current.cards[reviewKey], reps: 9 },
        [archivedKey]: current.cards[reviewKey] } };
    const merged = mergeVocabulary(current, imported);
    expect(merged.selectedIds).toEqual([...current.selectedIds, added.id]);
    expect(merged.cards[reviewKey]).toEqual(current.cards[reviewKey]);
    expect(merged.cards[archivedKey]).toEqual(current.cards[reviewKey]);
    expect(Object.keys(merged.cards)).toHaveLength(5);
  });

  it('rejects malformed imports and merges without overwriting local review history', () => {
    const local = reviewWord(setWordSelected(freshVocabulary(), 'noun.wife', true), 'noun.wife', 'ru-pl', 'again');
    expect(() => validateVocabulary({ version: 1, pair: 'pl-ru', selectedIds: ['unknown'], custom: [], cards: {} })).toThrow();
    const imported = setWordSelected(freshVocabulary(), 'noun.book', true);
    const merged = mergeVocabulary(local, imported);
    expect(merged.selectedIds).toEqual(['noun.wife', 'noun.book']);
    expect(merged.cards[wordCardKey('noun.wife', 'ru-pl')]).toEqual(local.cards[wordCardKey('noun.wife', 'ru-pl')]);
  });
});
