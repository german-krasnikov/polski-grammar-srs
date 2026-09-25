import { describe, expect, test } from 'vitest';
import { validateVocabularyEditorial } from '../scripts/validate-vocabulary-editorial.mjs';
import course from '../courses/pl-ru/course.json';
import frequency from '../courses/pl-ru/frequency-top1000.json';
import editorial from '../courses/pl-ru/vocabulary-editorial.json';

const copy = () => [structuredClone(course), structuredClone(frequency), structuredClone(editorial)] as const;

describe('vocabulary editorial journal', () => {
  test('accepts the independently reviewed baseline at the release gate', () => {
    const [pack, ranks, journal] = copy();
    expect(() => validateVocabularyEditorial(pack, ranks, journal)).not.toThrow();
    expect(() => validateVocabularyEditorial(pack, ranks, journal, { strict: true })).not.toThrow();
  });

  test.each([
    ['missing card', (journal: typeof editorial) => { journal.cards.pop(); }, /missing editorial card/],
    ['stale translation', (journal: typeof editorial) => { journal.cards[0].translation = 'other'; }, /translation/],
    ['duplicate ID', (journal: typeof editorial) => { journal.cards[1].id = journal.cards[0].id; }, /duplicate.*id/],
    ['duplicate selectable lemma', (journal: typeof editorial) => { journal.cards[1].lemma = journal.cards[0].lemma; }, /duplicate selectable lemma/],
    ['rank drift', (journal: typeof editorial) => { journal.cards[0].rank = 1; }, /rank/],
    ['candidate reuses an approved rank', (journal: typeof editorial) => {
      const approved = journal.cards.find(card => card.rank !== null)!;
      journal.candidates[0].rank = approved.rank!;
      journal.candidates[0].lemma = approved.lemma;
    }, /duplicate or promoted rank/],
    ['unlicensed source', (journal: typeof editorial) => { journal.cards[0].provenance.example = { kind: 'external', url: 'https://example.org/example', revision: '1', license: '', attribution: '' } as never; }, /provenance\/example/],
    ['unknown candidate', (journal: typeof editorial) => { journal.candidates[0].lemma = 'unknown'; }, /candidate.*lemma/],
    ['source revision drift', (journal: typeof editorial) => { journal.frequencySource.revision = 'f'.repeat(40); }, /frequencySource\/revision/],
    ['missing review evidence', (journal: typeof editorial) => { journal.cards[0].reviewSources = []; }, /reviewSources/],
    ['unconfirmed level decision', (journal: typeof editorial) => { journal.cards[0].levelDecision = 'CEFR review pending'; }, /reviewSources/],
    ['unknown external license', (journal: typeof editorial) => { journal.cards[0].provenance.example = { kind: 'external', url: 'https://example.org/example', revision: '1', license: 'unknown', attribution: 'A' } as never; }, /license/],
  ])('rejects %s', (_name, mutate, message) => {
    const [pack, ranks, journal] = copy();
    mutate(journal);
    expect(() => validateVocabularyEditorial(pack, ranks, journal)).toThrow(message);
  });

  test('does not allow a newly authored runtime card to stay pending', () => {
    const [pack, ranks, journal] = copy();
    const newCard = { ...pack.vocabulary.items[0], id: 'vocab.000033', lemma: 'new-lemma', frequencyRank: null };
    pack.vocabulary.items.push(newCard);
    journal.cards.push({ ...journal.cards[0], id: newCard.id, lemma: newCard.lemma, rank: null,
      status: 'needs-review', reviewer: null as never, reviewedAt: null as never, reviewSources: [] });
    expect(() => validateVocabularyEditorial(pack, ranks, journal)).toThrow(/only legacy cards/);
  });

  test('strict gate rejects a pending runtime card', () => {
    const [pack, ranks, journal] = copy();
    journal.cards[0].status = 'needs-review';
    expect(() => validateVocabularyEditorial(pack, ranks, journal, { strict: true })).toThrow(/needs-review/);
  });
});
