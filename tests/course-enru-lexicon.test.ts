// R1.14a (Plans/Biome/R1Design.md): the en-ru lexicon rows against dsl/course.data-contract: what `biome data` rejects, with the exact position of each problem.
// The rows are read from the files the step course-enru of dsl/biome.project binds. They need a biome-dsl checkout (see course-data.test.ts); without one they are skipped, which is NOT RUN, not a pass.
import { describe, expect, it } from 'vitest';
import { readdirSync } from 'node:fs';
import { join } from 'node:path';
import { biome, hasCli, repo, row, rowsIn } from './support/course-data';

const EN = join(repo, 'courses-biome/en-ru');
/** Rows per file, from the sealed lexicon (lang/en/lexicon.json sha256 f96da7fc..., lang/en/prepositions.json sha256 140e900f...) read before the transcription. */
const ROWS: Record<string, number> = { adjectives: 7, nouns: 14, personalPronouns: 7, possessives: 7, prepositions: 1, verbs: 10 };

describe.skipIf(!hasCli)('courses-biome/en-ru lexicon is Core data', () => {
  it('holds exactly the six lexicon files so far', () => {
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

describe.skipIf(!hasCli)('en nouns and adjectives', () => {
  const nouns = rowsIn('course-enru', 'EnNounRow');
  const noun = (id: string, change: Record<string, string | undefined> = {}) =>
    row('NOUN', { id: `"${id}"`, lemma: `"${id}"`, meaning: '"жена"', forms: '{sg: "wife" pl: "wives"}' }, change);
  it('a second noun with the same id is DATA_DUPLICATE at its id', () => nouns.problem([noun('wife'), noun('wife')], 1, '"wife"', 'DATA_DUPLICATE'));
  it('a noun without its plural is DATA_REQUIRED at the { of its forms', () => nouns.problem([noun('wife', { forms: '{sg: "wife"}' })], 0, '{sg: "wife"}', 'DATA_REQUIRED'));
  it('a noun with a field the contract does not know is DATA_UNKNOWN_FIELD at its key', () => nouns.problem([noun('wife', { gender: '"f"' })], 0, 'gender', 'DATA_UNKNOWN_FIELD'));
  it('a lemma that is a number is DATA_TYPE at the value', () => nouns.problem([noun('wife', { lemma: '7' })], 0, '7', 'DATA_TYPE'));
  it('no noun at all is DATA_COUNT at 1:1', () => expect(nouns.check('').problems).toEqual(['nouns.core-data:1:1: DATA_COUNT']));
  it('Russian meanings and an irregular plural are accepted', () => expect(nouns.check([noun('child', { meaning: '"ребёнок"', forms: '{sg: "child" pl: "children"}' })].join('\n') + '\n')).toEqual({ status: 0, problems: [] }));

  const adjectives = rowsIn('course-enru', 'EnAdjectiveRow');
  const adjective = (change: Record<string, string | undefined> = {}) => row('ADJECTIVE', { id: '"new"', lemma: '"new"', meaning: '"новый"', forms: '{invariant: "new"}' }, change);
  it('an adjective without its invariable form is DATA_REQUIRED at the { of its forms', () => adjectives.problem([adjective({ forms: '{}' })], 0, '{}', 'DATA_REQUIRED'));
  it('an adjective with the form written is accepted', () => expect(adjectives.check(adjective() + '\n')).toEqual({ status: 0, problems: [] }));
});

describe.skipIf(!hasCli)('en verbs, pronouns and prepositions', () => {
  const verbs = rowsIn('course-enru', 'EnVerbRow');
  const verb = (id: string, change: Record<string, string | undefined> = {}) => row('VERB', { id: `"${id}"`, lemma: `"${id}"`, meaning: '"быть"', present3sg: '"is"' }, change);
  it('a regular verb, `be` with its extra forms and `will` with its single form are accepted', () => {
    const rows = [
      verb('walk', { past: '"walked"', pastParticiple: '"walked"', ing: '"walking"' }),
      verb('be', { presentSg1: '"am"', presentPl: '"are"', past: '"was"', pastPl: '"were"', pastParticiple: '"been"', ing: '"being"' }),
      verb('will', { present3sg: '"will"' }),
    ];
    expect(verbs.check(rows.join('\n') + '\n')).toEqual({ status: 0, problems: [] });
  });
  it('a verb without its third person is DATA_REQUIRED at the { of the row', () => verbs.problem([verb('walk', { present3sg: undefined })], 0, '{', 'DATA_REQUIRED'));
  it('a past that is a number is DATA_TYPE at the value', () => verbs.problem([verb('walk', { past: '777' })], 0, '777', 'DATA_TYPE'));
  it('a verb with a form the contract does not know is DATA_UNKNOWN_FIELD at its key', () => verbs.problem([verb('walk', { gerund: '"walking"' })], 0, 'gerund', 'DATA_UNKNOWN_FIELD'));
  it('a second verb with the same id is DATA_DUPLICATE at its id', () => verbs.problem([verb('walk'), verb('walk')], 1, '"walk"', 'DATA_DUPLICATE'));

  const pronouns = rowsIn('course-enru', 'EnPronounRow');
  const pronoun = (id: string, forms = '{Subj: "he" Obj: "him"}') => row('PRONOUN', { id: `"${id}"`, forms });
  it('a pronoun without its object form is DATA_REQUIRED at the { of its forms', () => pronouns.problem([pronoun('he', '{Subj: "he"}')], 0, '{Subj: "he"}', 'DATA_REQUIRED'));
  it('a second pronoun with the same id is DATA_DUPLICATE at its id', () => pronouns.problem([pronoun('he'), pronoun('he')], 1, '"he"', 'DATA_DUPLICATE'));
  it('both forms are accepted', () => expect(pronouns.check(pronoun('he') + '\n')).toEqual({ status: 0, problems: [] }));

  const prepositions = rowsIn('course-enru', 'EnPrepositionRow');
  const preposition = (forms: string) => row('PREPOSITION', { id: '"role"', forms });
  it('a role without its form is DATA_REQUIRED at the { of the pair', () => prepositions.problem([preposition('[{role: "In"}]')], 0, '{role: "In"}', 'DATA_REQUIRED'));
  it('the empty text of a role that needs no preposition is data and is accepted', () =>
    expect(prepositions.check(preposition('[{role: "Subj" form: ""} {role: "In" form: "in"}]') + '\n')).toEqual({ status: 0, problems: [] }));
});
