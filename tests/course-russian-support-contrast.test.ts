import { expect, test } from 'vitest';
import course from '../courses/pl-ru/course.json';
import frequency from '../courses/pl-ru/frequency-top1000.json';
import { validateCoursePack } from '../scripts/validate-course.mjs';

type Segment = { text: string; isChanged: boolean; isEnding: boolean };
type AuthoredPair = { from: string; to: string; beforeParts: Segment[]; afterParts: Segment[] };
type SupportRow = { id: string; comparisons?: AuthoredPair[] };

const rows = course.reference.russianSupport.rows as SupportRow[];

test('each Russian-support row has an exact, independent Polish comparison', () => {
  expect(rows.map(row => row.id)).toEqual(['accusative', 'instrumental', 'locative', 'possessive']);
  for (const row of rows) {
    expect(row.comparisons, row.id).toBeDefined();
    expect(row.comparisons?.length, row.id).toBeGreaterThan(0);
    for (const pair of row.comparisons ?? []) {
      expect(pair.beforeParts.map(part => part.text).join('')).toBe(pair.from);
      expect(pair.afterParts.map(part => part.text).join('')).toBe(pair.to);
      expect(pair.from).not.toMatch(/\s\/\s/);
      expect(pair.to).not.toMatch(/\s\/\s/);
    }
  }
  expect(rows[0].comparisons?.map(pair => [pair.from, pair.to])).toContainEqual(['moja żona', 'moją żonę']);
  expect(rows[1].comparisons?.map(pair => [pair.from, pair.to])).toContainEqual(['moja żona', 'moją żoną']);
  expect(rows[2].comparisons?.map(pair => [pair.from, pair.to])).toContainEqual(['żona', 'żonie']);
  expect(rows[3].comparisons?.map(pair => [pair.from, pair.to])).toEqual([
    ['moją żonę', 'jego żonę'],
    ['moją żonę', 'ich żonę'],
  ]);
});

test('pack validation rejects a missing support comparison at its field path', () => {
  const changed = structuredClone(course) as unknown as { reference: { russianSupport: { rows: SupportRow[] } } };
  delete changed.reference.russianSupport.rows[0].comparisons;
  expect(() => validateCoursePack(changed, frequency)).toThrow(/reference\/russianSupport\/rows\/0\/comparisons/);
});

test('pack validation rejects a segment that changes the Polish form', () => {
  const changed = structuredClone(course);
  changed.reference.russianSupport.rows[0].comparisons[0].afterParts[1].text = 'a';
  expect(() => validateCoursePack(changed, frequency)).toThrow(/reference\/russianSupport\/rows\/0\/comparisons\/0\/afterParts/);
});

test('pack validation rejects a whole-word replacement mislabelled as an ending', () => {
  const changed = structuredClone(course);
  changed.reference.russianSupport.rows[3].comparisons[0].afterParts[0].isEnding = true;
  expect(() => validateCoursePack(changed, frequency)).toThrow(/reference\/russianSupport\/rows\/3\/comparisons\/0\/afterParts\/0\/isEnding/);
});

test('pack validation rejects an empty segment', () => {
  const changed = structuredClone(course);
  changed.reference.russianSupport.rows[0].comparisons[0].beforeParts[0].text = '';
  expect(() => validateCoursePack(changed, frequency)).toThrow(/reference\/russianSupport\/rows\/0\/comparisons\/0\/beforeParts\/0\/text/);
});

test('pack validation rejects a form change marked as unchanged', () => {
  const changed = structuredClone(course);
  changed.reference.russianSupport.rows[0].comparisons[0].afterParts[1].isChanged = false;
  expect(() => validateCoursePack(changed, frequency)).toThrow(/reference\/russianSupport\/rows\/0\/comparisons\/0/);
});
