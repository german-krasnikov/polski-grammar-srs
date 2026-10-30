// R1.13, R1.17 (Plans/Biome/R1Design.md section 5.3): the SkillSpec fields of the pl-ru and en-ru skill rows against the frozen, independent cases of Plans/Biome/NJ03CurriculumIndependent.cases.
// The chain of evidence: the cases are equal to the old lang/<code>/curriculum.json (scripts/check-nj03-old-curriculum-facts.mjs reads them from git at a pinned revision and checks
// their sha256), and the rows are equal to the cases (this test); the engine reads the rows, and CurriculumReaderTest and EnRuEngineInputsTest check what it reads against their own expectations.
// Needs a biome-dsl checkout (see course-data.test.ts); without one it is skipped, which is NOT RUN, not a pass.
import { describe, expect, it } from 'vitest';
import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import { biome, hasCli, repo } from './support/course-data';

/** The notation of the cases file: `Case:Nom>Acc`, `Tense=Pres,Person=1`, `noun/gender=n`, NULL, `{}` and `[]` for nothing. */
const list = (items: string[]) => (items.length ? items.join(',') : '[]');
const focus = (f: { feature: string; from: string; to: string } | null) => (f === null ? 'NULL' : `${f.feature}:${f.from}>${f.to}`);
const fixed = (pairs: { feature: string; value: string }[]) => (pairs.length ? pairs.map(p => `${p.feature}=${p.value}`).join(',') : '{}');
/** The cases name the nouns of a filter `nounId`; the filter of an English skill has them in `enNounId` (a reference to the English nouns). */
const filter = (f: { slot: string; where: Record<string, string[]> } | null) => {
  if (f === null) return 'NULL';
  const [property, ...more] = Object.keys(f.where);
  expect(more, 'the cases notation has one property per filter').toEqual([]);
  return `${f.slot}/${property === 'enNounId' ? 'nounId' : property}=${f.where[property].join(',')}`;
};

for (const { pack, lang } of [{ pack: 'pl-ru', lang: 'pl' }, { pack: 'en-ru', lang: 'en' }]) {
  describe.skipIf(!hasCli)(`the skill rows of ${pack} against NJ03CurriculumIndependent.cases`, () => {
    const cases = readFileSync(join(repo, 'Plans/Biome/NJ03CurriculumIndependent.cases'), 'utf8').split('\n').filter(line => line.startsWith(`ROW | ${lang} | `))
      .map(line => line.split('|').map(cell => cell.trim()).slice(2));
    const run = biome('read', join(repo, 'courses-biome', pack, 'skills.core-data'), '--format', 'json');
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
}
