import { expect, it } from 'vitest';
import examples from '../biome/generated/session.json';
import { countsCorrect, policy } from '../src/generated/biome/session.mjs';
import { recordReview } from '../src/progress/review';
import type { Progress } from '../src/types';
import progress from './fixtures/kotlin-parity/progress.json';

it('matches the independent review-credit examples', () => {
  const acceptedRatings = ['again', 'hard', 'good', 'easy'] as const;
  for (const example of examples.countsCorrect) {
    // JSON widens enum literals to string. Assert the accepted domain before narrowing it.
    expect(acceptedRatings, example.id).toContain(example.facts.reviewRating);
    const facts = example.facts as Parameters<typeof policy.countsCorrect>[0];
    const nativeAnswer = (facts.reviewHasTypedResult ? facts.reviewTypedCorrect : undefined)
      ?? facts.reviewRating !== 'again';
    expect(nativeAnswer, `${example.id}: native helper`).toBe(example.expected);
    expect(policy.countsCorrect(facts), example.id).toBe(example.expected);
  }
});

it('applies every authored example through the React review consumer once', () => {
  const original = progress.cases.find(item => item.id === 'P-fresh')!.expected as Progress;
  const skillId = 'case.acc.f';
  for (const example of examples.countsCorrect) {
    const baseline = structuredClone(original);
    const facts = example.facts as Parameters<typeof policy.countsCorrect>[0];
    const reviewed = recordReview(
      baseline,
      skillId,
      facts.reviewRating,
      facts.reviewHasTypedResult ? facts.reviewTypedCorrect : undefined,
    );
    expect(reviewed.stats[skillId].reviews, example.id).toBe(baseline.stats[skillId].reviews + 1);
    expect(reviewed.totalReviews, example.id).toBe(baseline.totalReviews + 1);
    expect(reviewed.stats[skillId].correct, example.id)
      .toBe(baseline.stats[skillId].correct + Number(example.expected));
    expect(reviewed.stats[skillId].mistakes, example.id)
      .toBe(baseline.stats[skillId].mistakes + Number(!example.expected));
  }
});

it('rejects a stale fact binding before making a verdict', () => {
  const stale = {
    reviewHasTypedResult: () => false,
    reviewRatingStale: () => 'good',
    reviewTypedCorrect: () => false,
  };
  expect(() => countsCorrect(stale as unknown as Parameters<typeof countsCorrect>[0]))
    .toThrow('Missing getter: review.rating');
});

it('treats runtime null like an absent typed result at the React review boundary', () => {
  const baseline = structuredClone(progress.cases.find(item => item.id === 'P-fresh')!.expected) as Progress;
  const skillId = 'case.acc.f';
  const absent = null as unknown as boolean;
  const good = recordReview(baseline, skillId, 'good', absent);
  const again = recordReview(baseline, skillId, 'again', absent);
  expect(good.stats[skillId].correct).toBe(baseline.stats[skillId].correct + 1);
  expect(again.stats[skillId].mistakes).toBe(baseline.stats[skillId].mistakes + 1);
});
