// R1.14b and R1.15 (Plans/Biome/R1Design.md): the files of courses-biome/en-ru, the pair rows and the recipes of English against dsl/course.data-contract: what `biome data` rejects, with the exact position of each problem.
// The lexicon cases are course-enru-lexicon.test.ts. They need a biome-dsl checkout (see course-data.test.ts); without one they are skipped, which is NOT RUN, not a pass.
import { describe, expect, it } from 'vitest';
import { readdirSync } from 'node:fs';
import { join } from 'node:path';
import { biome, hasCli, repo, row, rowsIn, skill } from './support/course-data';

const EN = join(repo, 'courses-biome/en-ru');
/** Rows per file, from the sealed sources (lang/en/lexicon.json, lang/en/prepositions.json, pairs/en-ru/pair.json: the 16 skills, 12 seeds, 73 copy entries, 6 patterns, 1 prefix, 1 vocabulary of 30 cards, 1 chain; R1.15, lang/en/realization.json and exercise-recipes.json: 6 constructions, 16 recipes, 5 chain steps, 1 defaults row; R1.18, pairs/en-ru/lifehacks.json: 32 lifehacks; R1.19, en-ru/frequency-top1000.json: 1000 items). */
const ROWS: Record<string, number> = {
  adjectives: 7, caseSentencePrefixes: 1, chainSteps: 5, constructions: 6, exerciseCopy: 73, exercisePatterns: 6, frequency: 1000, lifehacks: 32, nouns: 14, personalPronouns: 7, possessives: 7, prepositions: 1,
  recipeDefaults: 1, recipes: 16, sentenceSeeds: 12, skills: 16, training: 1, verbs: 10, vocabulary: 1,
};

describe.skipIf(!hasCli)('courses-biome/en-ru is Core data', () => {
  it('holds the sealed course files and the separate DSL UI copy catalog', () => {
    // P12 adds UI copy; the sealed language-source row counts above stay unchanged.
    expect(readdirSync(EN).filter(name => name.endsWith('.core-data')).map(name => name.replace('.core-data', '')).sort()).toEqual([...Object.keys(ROWS), 'uiCopy'].sort());
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

describe.skipIf(!hasCli)('en recipes and constructions against the contract', () => {
  const needs = ['SkillRow', 'EnNounRow', 'CopyEntryRow', 'PatternEntryRow', 'ConstructionRow', 'PossessiveRow'];
  const recipes = rowsIn('course-enru', 'SkillRecipeRow', ...needs);
  const chain = rowsIn('course-enru', 'ChainStepRecipeRow', ...needs);
  const defaults = rowsIn('course-enru', 'RecipeDefaultsRow', ...needs);
  const direct = '{kind: direct value: {kind: copy key: "roleObjPrompt"}}';
  const recipe = (skill: string, change: Record<string, string | undefined> = {}) => row('RECIPE', {
    skill: `"${skill}"`, source: direct, expected: direct, promptKey: '"roleObjPrompt"', explanationKey: '"roleObjExplanation"', changes: '[]', tags: '["obj"]',
  }, change);
  it('a recipe for an English skill, with an English copy key, is accepted', () => expect(recipes.check(recipe('en:role.object') + '\n')).toEqual({ status: 0, problems: [] }));
  it('a recipe for a skill of pl-ru is DATA_REF at the value: the step binds the skills of English', () => recipes.problem([recipe('case.acc.n')], 0, '"case.acc.n"', 'DATA_REF'));
  it('a copy key of pl-ru is DATA_REF at the value', () => recipes.problem([recipe('en:role.object', { promptKey: '"accPrompt"' })], 0, '"accPrompt"', 'DATA_REF'));
  it('a phrase of a construction that English has no template for is DATA_REF at the value', () =>
    recipes.problem([recipe('en:role.object', { source: '{kind: prefixed spec: {construction: "core.composite" bundle: []} prefixCase: "Subj" punct: "."}' })], 0, '"core.composite"', 'DATA_REF'));
  it('an owner of pl-ru that English has not (yourPlural) is DATA_REF at the value', () =>
    chain.problem([row('CHAIN', { source: direct, expected: direct, promptKey: '"roleObjPrompt"', explanationKey: '"roleObjExplanation"', changes: '[]', tags: '[]', ownerOut: '"yourPlural"' })], 0, '"yourPlural"', 'DATA_REF'));
  it('constant slots that are always there, and ones that wait for a feature of the phrase, are accepted', () =>
    expect(defaults.check(row('DEFAULTS', {
      defaultOwnerLexeme: '"my"', verbLexeme: '"walk"', firstSkillByGender: '[{gender: "default" skill: "en:role.object"}]',
      constantSlots: '[{slot: "prep" lexeme: "role" whenFeature: "Case" whenValues: ["In" "With"]} {slot: "subject" lexeme: "you"}]',
    }) + '\n')).toEqual({ status: 0, problems: [] }));
});
