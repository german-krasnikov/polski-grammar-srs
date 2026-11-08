import { createHash } from 'node:crypto';
import { expect, test } from '@playwright/test';
import { progressAsLegacyShape, readPolskiText } from './polski-text';

const body = [
  'POLSKI-TEXT\t1\tprogress\t2',
  'M\t-\t4',
  'I\tK:totalReviews\t1',
  'L\tK:cards\t1',
  'M\t-\t3',
  'S\tK:skillId\tcase.acc.f',
  'S\tK:due\t2026-02-03T12:10:00.000Z',
  'D\tK:stability\t400274e000000000',
  'L\tK:stats\t1',
  'M\t-\t2',
  'S\tK:skillId\tcase.acc.f',
  'I\tK:reviews\t1',
  'S\tK:lastDay\t2026-02-03',
].join('\n') + '\n';
const raw = `${body}H\tsha256\t${createHash('sha256').update(body).digest('hex')}\n`;

test('test-side text-wire reader preserves nested cards, doubles and root values', () => {
  expect(readPolskiText(raw, 'progress')).toEqual({
    totalReviews: 1,
    cards: [{ skillId: 'case.acc.f', due: '2026-02-03T12:10:00.000Z', stability: 2.30706787109375 }],
    stats: [{ skillId: 'case.acc.f', reviews: 1 }],
    lastDay: '2026-02-03',
  });
  expect(progressAsLegacyShape(raw).cards).toEqual([
    { skillId: 'case.acc.f', card: { due: '2026-02-03T12:10:00.000Z', stability: 2.30706787109375 } },
  ]);
  expect(progressAsLegacyShape(raw).stats).toEqual({ 'case.acc.f': { reviews: 1 } });
});

test('test-side text-wire reader rejects changed bytes before asserting behavior', () => {
  expect(() => readPolskiText(raw.replace('case.acc.f', 'case.acc.m'), 'progress')).toThrow('digest mismatch');
  expect(() => readPolskiText(raw, 'preferences')).toThrow('header');
});
