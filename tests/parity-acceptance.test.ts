import { afterEach, describe, expect, it, vi } from 'vitest';
import grammar from './fixtures/kotlin-parity/grammar.json';
import exercises from './fixtures/kotlin-parity/exercises.json';
import evaluation from './fixtures/kotlin-parity/evaluation.json';
import scheduler from './fixtures/kotlin-parity/scheduler.json';
import progress from './fixtures/kotlin-parity/progress.json';
import { verbForm } from '../src/grammar/engine';
import { recordReview } from '../src/progress/review';
import type { Progress } from '../src/types';

const originalZone = process.env.TZ;

afterEach(() => {
  vi.useRealTimers();
  if (originalZone === undefined) delete process.env.TZ;
  else process.env.TZ = originalZone;
});

describe('independent baseline acceptance', () => {
  it('keeps every portable case identifiable before replay or exercise ID normalization', () => {
    for (const [name, file] of Object.entries({ grammar, exercises, evaluation, scheduler, progress })) {
      const ids = file.cases.map(item => item.id);
      expect(file.schemaVersion, name).toBe(1);
      expect(ids.every(Boolean), name).toBe(true);
      expect(new Set(ids).size, name).toBe(ids.length);
      expect(file.cases.every(item => Object.hasOwn(item, 'input') && Object.hasOwn(item, 'expected')), name).toBe(true);
    }
  });

  it.each(['buyDone', 'doDone'])('rejects present tense for perfective %s', verbId => {
    expect(() => verbForm(verbId, 'present', 1, 'sg')).toThrow('Perfective verbs have no present tense');
  });

  it.each([
    ['winter midnight', '2026-02-03T22:59:59.000Z', '2026-02-03T23:00:01.000Z', '2026-02-03', '2026-02-04'],
    ['DST day midnight', '2026-03-29T21:59:59.000Z', '2026-03-29T22:00:01.000Z', '2026-03-29', '2026-03-30'],
  ])('resets actual review counters at Warsaw %s', (_label, before, after, firstDay, secondDay) => {
    process.env.TZ = 'Europe/Warsaw';
    vi.useFakeTimers();
    const baseline = structuredClone(progress.cases.find(item => item.id === 'P-fresh')!.expected) as Progress;
    baseline.lastDay = firstDay;
    baseline.reviewsToday = 4;
    baseline.totalReviews = 9;
    vi.setSystemTime(new Date(before));
    const first = recordReview(baseline, 'case.acc.f', 'good');
    expect(first).toMatchObject({ lastDay: firstDay, reviewsToday: 5, totalReviews: 10 });
    vi.setSystemTime(new Date(after));
    const second = recordReview(first, 'case.acc.f', 'good');
    expect(second).toMatchObject({ lastDay: secondDay, reviewsToday: 1, totalReviews: 11 });
    expect(second.stats['case.acc.f'].reviews).toBe(baseline.stats['case.acc.f'].reviews + 2);
    expect(baseline).toMatchObject({ lastDay: firstDay, reviewsToday: 4, totalReviews: 9 });
  });
});
