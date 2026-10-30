// R1.14b (Plans/Biome/R1Design.md): the files of courses-biome/en-ru and the pair rows of English against dsl/course.data-contract: what `biome data` rejects, with the exact position of each problem.
// The lexicon cases are course-enru-lexicon.test.ts. They need a biome-dsl checkout (see course-data.test.ts); without one they are skipped, which is NOT RUN, not a pass.
import { describe, expect, it } from 'vitest';
import { readdirSync } from 'node:fs';
import { join } from 'node:path';
import { biome, hasCli, repo, row, rowsIn, skill } from './support/course-data';

const EN = join(repo, 'courses-biome/en-ru');
/** Rows per file, from the sealed sources (lang/en/lexicon.json, lang/en/prepositions.json, pairs/en-ru/pair.json: the 16 skills, 12 seeds, 73 copy entries, 6 patterns, 1 prefix, 1 vocabulary of 30 cards, 1 chain). */
const ROWS: Record<string, number> = {
  adjectives: 7, caseSentencePrefixes: 1, exerciseCopy: 73, exercisePatterns: 6, nouns: 14, personalPronouns: 7, possessives: 7, prepositions: 1, sentenceSeeds: 12, skills: 16, training: 1, verbs: 10, vocabulary: 1,
};

describe.skipIf(!hasCli)('courses-biome/en-ru is Core data', () => {
  it('holds exactly the 13 known files', () => {
    expect(readdirSync(EN).filter(name => name.endsWith('.core-data')).map(name => name.replace('.core-data', '')).sort()).toEqual(Object.keys(ROWS).sort());
  });
  for (const [file, rows] of Object.entries(ROWS)) {
    it(`biome read ${file}.core-data reads ${rows} row(s)`, () => {
      const run = biome('read', join(EN, `${file}.core-data`), '--format', 'raw');
      expect(run.stderr, 'biome read diagnostics').toBe('');
      expect(run.status).toBe(0);
      expect(run.stdout.trimEnd().split('\n')).toHaveLength(rows);
    });
  }
});

describe.skipIf(!hasCli)('en seeds against the contract', () => {
  const seeds = rowsIn('course-enru', 'EnSeedRow', 'EnNounRow', 'EnAdjectiveRow');
  const seed = (change: Record<string, string | undefined> = {}) => row('SEED', { nounId: '"wife"', adjectiveId: '"beautiful"' }, change);
  it('a seed of a noun and an adjective of the lexicon is accepted', () => expect(seeds.check(seed() + '\n')).toEqual({ status: 0, problems: [] }));
  it('a noun that is no noun of the lexicon is DATA_REF at the value', () => seeds.problem([seed({ nounId: '"nobody"' })], 0, '"nobody"', 'DATA_REF'));
  it('an adjective that is no adjective of the lexicon is DATA_REF at the value', () => seeds.problem([seed({ adjectiveId: '"nothing"' })], 0, '"nothing"', 'DATA_REF'));
  it('a seed without its adjective is DATA_REQUIRED at the { of the row', () => seeds.problem([seed({ adjectiveId: undefined })], 0, '{', 'DATA_REQUIRED'));
});

describe.skipIf(!hasCli)('en skill rows against the contract', () => {
  const skills = rowsIn('course-enru', 'SkillRow', 'EnNounRow');
  const filtered = (where: string) => skill('en:a', { lexicalFilter: `{slot: "noun" where: ${where}}` });
  it('a filter that names its nouns by enNounId is accepted, also with a gender', () => {
    expect(skills.check(filtered('{enNounId: ["wife" "dog"]}') + '\n')).toEqual({ status: 0, problems: [] });
    expect(skills.check(filtered('{enNounId: ["wife"] gender: ["f"]}') + '\n')).toEqual({ status: 0, problems: [] });
  });
  it('an enNounId that is no noun of the lexicon is DATA_REF at the value', () => skills.problem([filtered('{enNounId: ["nobody"]}')], 0, '"nobody"', 'DATA_REF'));
  it('a nounId, the reference of pl-ru, has no noun to refer to in the English step: DATA_REF', () => skills.problem([filtered('{nounId: ["wife"]}')], 0, '"wife"', 'DATA_REF'));
  it('a prerequisite that is no skill of the pack is DATA_REF at the value', () => skills.problem([skill('en:a', { prerequisites: '["en:nope"]' })], 0, '"en:nope"', 'DATA_REF'));
});
