// R1.13 (Plans/Biome/R1Design.md section 5.3): the SkillSpec fields of the pl-ru skill rows against the frozen, independent cases of Plans/Biome/NJ03CurriculumIndependent.cases.
// The chain of evidence: the cases are equal to the old lang/pl/curriculum.json (scripts/check-nj03-old-curriculum-facts.mjs reads it from git at a pinned revision and checks
// its sha256), and the rows are equal to the cases (this test); the engine reads the rows, and CurriculumReaderTest checks what it reads against its own expectations.
// Needs a biome-dsl checkout (see course-data.test.ts); without one it is skipped, which is NOT RUN, not a pass.
import { describe, expect, it } from 'vitest';
import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import { PACK, biome, hasCli, repo } from './support/course-data';

/** The notation of the cases file: `Case:Nom>Acc`, `Tense=Pres,Person=1`, `noun/gender=n`, NULL, `{}` and `[]` for nothing. */
const list = (items: string[]) => (items.length ? items.join(',') : '[]');
const focus = (f: { feature: string; from: string; to: string } | null) => (f === null ? 'NULL' : `${f.feature}:${f.from}>${f.to}`);
const fixed = (pairs: { feature: string; value: string }[]) => (pairs.length ? pairs.map(p => `${p.feature}=${p.value}`).join(',') : '{}');
const filter = (f: { slot: string; where: Record<string, string[]> } | null) => {
  if (f === null) return 'NULL';
  const [property, ...more] = Object.keys(f.where);
  expect(more, 'the cases notation has one property per filter').toEqual([]);
  return `${f.slot}/${property}=${f.where[property].join(',')}`;
};

describe.skipIf(!hasCli)('the skill rows of pl-ru against NJ03CurriculumIndependent.cases', () => {
  const cases = readFileSync(join(repo, 'Plans/Biome/NJ03CurriculumIndependent.cases'), 'utf8').split('\n').filter(line => line.startsWith('ROW | pl | '))
    .map(line => line.split('|').map(cell => cell.trim()).slice(2));
  const run = biome('read', join(PACK, 'skills.core-data'), '--format', 'json');
  const rows = (JSON.parse(run.stdout) as { value: any }[]).map(r => r.value);

  it('has the sixteen rows of the cases, in the same order', () => {
    expect(run.status, run.stderr).toBe(0);
    expect(cases).toHaveLength(16);
    expect(rows.map(r => r.id)).toEqual(cases.map(c => c[1]));
  });

  it('carries the construction, focus, fixed features, filter, level and prerequisites of every case', () => {
    expect(rows.map(r => [r.construction, focus(r.featureFocus), fixed(r.fixed), filter(r.lexicalFilter), r.level, list(r.prerequisites)]))
      .toEqual(cases.map(c => c.slice(2)));
  });
});
