import { createHash } from 'node:crypto';
import { expect, test } from '@playwright/test';
import { installProgressReader } from './kotlin-storage';

test('progress reader returns flat root fields from POLSKI-TEXT framing', async ({ page }) => {
  await installProgressReader(page);
  await page.goto('/');

  const body = [
    'POLSKI-TEXT\t1\tprogress\t2',
    'M\t-\t3',
    'I\tK:reviewsToday\t2',
    'S\tK:lastDay\t2026-11-08',
    'I\tK:totalReviews\t7',
    '',
  ].join('\n');
  const digest = createHash('sha256').update(body).digest('hex');
  const wire = `${body}H\tsha256\t${digest}\n`;
  const key = 'test-only-progress-reader';
  const actual = await page.evaluate(({ key, wire }) => {
    localStorage.setItem(key, wire);
    return {
      full: (window as any).__readProgress(key),
      count: (window as any).__readProgress(key, 'totalReviews'),
    };
  }, { key, wire });

  expect(actual).toEqual({
    full: { reviewsToday: 2, lastDay: '2026-11-08', totalReviews: 7 },
    count: 7,
  });
});
