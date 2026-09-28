import { describe, expect, test } from 'vitest';
import { validateVocabularyEditorial } from '../scripts/validate-vocabulary-editorial.mjs';
import frequency from '../courses/en-ru/frequency-top1000.json';
import editorial from '../courses/pairs/en-ru/vocabulary-editorial.json';

// Independent pin of the recorded source (courses/en-ru/ATTRIBUTION.md), not derived from
// the journal under test — catches accidental edits to frequencySource the same way pl-ru's
// default pin does, for the pack whose source is not pl-ru's (EN-15, MIT-licensed).
const enSource = {
  repository: 'https://github.com/IlyaSemenov/wikipedia-word-frequency',
  csvUrl: 'https://github.com/IlyaSemenov/wikipedia-word-frequency/blob/798ea9062d6e5aed1fa87deeeda1cd99d5b37903/results/enwiki-2023-04-13.txt',
  revision: '798ea9062d6e5aed1fa87deeeda1cd99d5b37903',
  license: 'MIT',
  csvSha256: 'e2071140a49a6c40c732bd68c36836ef13c0978233fb3d5e1b2a3c2d34faef59',
  jsonSha256: 'f64d9dcebdff358135aec3b6031763c553f3cbc8f2603e525ea40c82c5050253',
};

const copy = () => [structuredClone(frequency), structuredClone(editorial)] as const;

describe('en-ru vocabulary editorial journal (pre-EN-17: no pair.json wiring yet)', () => {
  test('validates schema, frequency-rank agreement and full top-1000 candidate coverage', () => {
    const [ranks, journal] = copy();
    expect(() => validateVocabularyEditorial(null, ranks, journal, { expectedFrequencySource: enSource })).not.toThrow();
  });

  test('every approved card has reviewer, reviewedAt and reviewSources', () => {
    const [, journal] = copy();
    const approved = journal.cards.filter(card => card.status === 'approved');
    expect(approved.length).toBe(journal.cards.length);
    for (const card of approved) {
      expect(card.reviewer?.trim()).toBeTruthy();
      expect(card.reviewedAt).toMatch(/^\d{4}-\d{2}-\d{2}$/);
      expect(card.reviewSources.length).toBeGreaterThan(0);
    }
  });

  test.each([
    ['duplicate ID', (journal: typeof editorial) => { journal.cards[1].id = journal.cards[0].id; }, /duplicate.*id/],
    ['duplicate selectable lemma', (journal: typeof editorial) => { journal.cards[1].lemma = journal.cards[0].lemma; }, /duplicate selectable lemma/],
    ['rank drift', (journal: typeof editorial) => { journal.cards[0].rank = 1; }, /rank/],
    ['candidate reuses an approved rank', (journal: typeof editorial) => {
      const approved = journal.cards.find(card => card.rank !== null)!;
      journal.candidates[0].rank = approved.rank!;
      journal.candidates[0].lemma = approved.lemma;
    }, /duplicate or promoted rank/],
    ['missing review evidence', (journal: typeof editorial) => { journal.cards[0].reviewSources = []; }, /reviewSources/],
    ['source revision drift', (journal: typeof editorial) => { journal.frequencySource.revision = 'f'.repeat(40); }, /frequencySource\/revision/],
  ])('rejects %s', (_name, mutate, message) => {
    const [ranks, journal] = copy();
    mutate(journal);
    expect(() => validateVocabularyEditorial(null, ranks, journal, { expectedFrequencySource: enSource })).toThrow(message);
  });
});
