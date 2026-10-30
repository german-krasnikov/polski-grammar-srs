// R1.16 (Plans/Biome/R1Design.md): the form table of en-ru is derived from the English lexicon rows by dsl/derive-forms.mjs. The rules on hand-built rows (expectations written out) and the real table against
// the table that scripts/build-pack-en.mjs built from the JSON lexicon; the file left the tree in R1.17, so it is read from the commit that still has it (git show <rev>:path, sha256 pinned).
// The data cases need a biome-dsl checkout (see course-data.test.ts); without one they are skipped, which is NOT RUN, not a pass.
import { describe, expect, it } from 'vitest';
import { execFileSync } from 'node:child_process';
import { createHash } from 'node:crypto';
import { join } from 'node:path';
import { enruForms, formRows, type FormEntry } from '../dsl/derive-forms.mjs';
import { biome, hasCli, repo } from './support/course-data';

const SEALED = { rev: '7358aeb72ab79dce80fca1faadba6be64892fa18', path: 'courses/lang/en/forms.generated.json', sha256: 'a0796ddfbb8eddebc7860ec0dd84cffee757223aa755b747c98289ef6b9edd3d' };
const will = { id: 'will', lemma: 'will', present3sg: 'will' };
const walk = { id: 'walk', lemma: 'walk', present3sg: 'walks', past: 'walked', ing: 'walking' };
const be = { id: 'be', lemma: 'be', present3sg: 'is', presentSg1: 'am', presentPl: 'are', past: 'was', pastPl: 'were', ing: 'being' };
const dos = { id: 'do', lemma: 'do', present3sg: 'does', past: 'did', ing: 'doing' };
const lexicon = (change: Record<string, unknown> = {}) => ({ nouns: [], adjectives: [], verbs: [will], possessives: [], pronouns: [], prepositions: [], ...change });
/** The form under a bundle, or undefined: the bundle is compared as the engine does, as a set of features. */
const formOf = (entries: FormEntry[], bundle: Record<string, string>) => entries.find(e => JSON.stringify(Object.entries(e.bundle).sort()) === JSON.stringify(Object.entries(bundle).sort()))?.form;
/** The forms of a tense in the order 1sg, 1pl, 2sg, 2pl, 3sg, 3pl. */
const cellsOf = (entries: FormEntry[], tense: string) => ['1', '2', '3'].flatMap(person => ['sg', 'pl'].map(number => formOf(entries, { Tense: tense, Person: person, Number: number })));

describe('the rules of the English form table on hand-built rows', () => {
  it('a noun has its singular and its plural; an adjective, a possessive and a pronoun one form in the empty bundle, the pronoun its subject form; "not" is always there', () => {
    const forms = enruForms(lexicon({
      nouns: [{ id: 'n', forms: { sg: 'child', pl: 'children' } }], adjectives: [{ id: 'a', forms: { invariant: 'new' } }],
      possessives: [{ id: 'p', forms: { kind: 'invariant', value: 'its' } }], pronouns: [{ id: 'I', forms: { Subj: 'I', Obj: 'me' } }],
    }));
    expect(forms['noun:n']).toEqual([{ bundle: { Number: 'sg' }, form: 'child' }, { bundle: { Number: 'pl' }, form: 'children' }]);
    expect(forms['adjective:a']).toEqual([{ bundle: {}, form: 'new' }]);
    expect(forms['possessive:p']).toEqual([{ bundle: {}, form: 'its' }]);
    expect(forms['pronoun:I']).toEqual([{ bundle: {}, form: 'I' }]);
    expect(forms['neg:not']).toEqual([{ bundle: {}, form: 'not' }]);
  });

  it('a preposition has a form for each role, in the order of the row, empty for the roles that word order carries', () => {
    const forms = enruForms(lexicon({ prepositions: [{ id: 'role', forms: [{ role: 'Subj', form: '' }, { role: 'In', form: 'in' }, { role: 'Of', form: 'of' }] }] }));
    expect(forms['prep:role']).toEqual([{ bundle: { Case: 'Subj' }, form: '' }, { bundle: { Case: 'In' }, form: 'in' }, { bundle: { Case: 'Of' }, form: 'of' }]);
  });

  it('the present of a verb is its third person for "he", `presentSg1` for "I" and `presentPl` for everyone else, each falling back to the lemma, in the order of the numbers and persons', () => {
    const forms = enruForms(lexicon({ verbs: [walk, be, will] }));
    expect(forms['verb:walk'].slice(0, 6).map(e => e.bundle)).toEqual(['sg', 'pl'].flatMap(number => ['1', '2', '3'].map(person => ({ Tense: 'Pres', Person: person, Number: number }))));
    expect(forms['verb:walk'].slice(0, 6).map(e => e.form)).toEqual(['walk', 'walk', 'walks', 'walk', 'walk', 'walk']);
    expect(forms['verb:be'].slice(0, 6).map(e => e.form)).toEqual(['am', 'are', 'is', 'are', 'are', 'are']);
  });

  it('the past is `past` for "I" and "he" and `pastPl` for "you" and the plural, falling back to `past`; the future is "will" and the lemma, both under the lowercase ids of the matrix', () => {
    const forms = enruForms(lexicon({ verbs: [walk, be, will] }));
    expect(cellsOf(forms['verb:walk'], 'past')).toEqual(Array(6).fill('walked'));
    expect(cellsOf(forms['verb:be'], 'past')).toEqual(['was', 'were', 'were', 'were', 'was', 'were']);
    expect(cellsOf(forms['verb:be'], 'present')).toEqual(['am', 'are', 'are', 'are', 'is', 'are']);
    expect(cellsOf(forms['verb:walk'], 'future')).toEqual(Array(6).fill('will walk'));
  });

  it('the bundles that the slots collapse to hold the past, the bare lemma for the future, the -ing form and the bare lemma again for the empty bundle', () => {
    const forms = enruForms(lexicon({ verbs: [walk, be, will] }));
    for (const [id, past, lemma, ing] of [['walk', 'walked', 'walk', 'walking'], ['be', 'was', 'be', 'being']]) {
      expect([formOf(forms[`verb:${id}`], { Tense: 'Past' }), formOf(forms[`verb:${id}`], { Tense: 'Fut' }), formOf(forms[`verb:${id}`], { Aspect: 'Continuous' }), formOf(forms[`verb:${id}`], {})]).toEqual([past, lemma, ing, lemma]);
    }
    expect(forms['verb:walk']).toHaveLength(28);
  });

  it('"do" and "be" are also auxiliaries with their present and their past, a verb without a past is no verb, and "will" is the one form of the future for every person', () => {
    const forms = enruForms(lexicon({ verbs: [walk, dos, be, will] }));
    expect(Object.keys(forms).filter(lexeme => /^(verb|aux):/.test(lexeme))).toEqual(['verb:walk', 'verb:do', 'aux:do', 'verb:be', 'aux:be', 'aux:will']);
    expect(forms['aux:do']).toHaveLength(7);
    expect([formOf(forms['aux:do'], { Tense: 'Past' }), formOf(forms['aux:do'], { Tense: 'Pres', Person: '3', Number: 'sg' }), formOf(forms['aux:be'], { Tense: 'Pres', Person: '1', Number: 'sg' })]).toEqual(['did', 'does', 'am']);
    expect(formOf(forms['aux:do'], { Tense: 'Fut' })).toBeUndefined();
    expect(forms['aux:will']).toEqual(['sg', 'pl'].flatMap(number => ['1', '2', '3'].map(person => ({ bundle: { Tense: 'Fut', Person: person, Number: number }, form: 'will' }))));
  });

  it('the lexemes come in the order nouns, adjectives, possessives, pronouns, "not", prepositions, verbs and the rows keep it', () => {
    const forms = enruForms(lexicon({
      nouns: [{ id: 'n', forms: { sg: 'a', pl: 'b' } }], adjectives: [{ id: 'a', forms: { invariant: 'c' } }], possessives: [{ id: 'p', forms: { kind: 'invariant', value: 'd' } }],
      pronouns: [{ id: 'I', forms: { Subj: 'I', Obj: 'me' } }], prepositions: [{ id: 'role', forms: [] }], verbs: [walk, will],
    }));
    expect(Object.keys(forms)).toEqual(['noun:n', 'adjective:a', 'possessive:p', 'pronoun:I', 'neg:not', 'prep:role', 'verb:walk', 'aux:will']);
    expect(formRows(forms).split('\n').filter(Boolean).map(line => line.match(/lexeme: "([^"]+)"/)![1])).toEqual(Object.keys(forms));
  });

  it('a declined possessive, a verb with a past and no -ing form and a lexicon without "will" are refused, not printed', () => {
    expect(() => enruForms(lexicon({ possessives: [{ id: 'p', forms: { kind: 'declined' } }] }))).toThrow('the possessive "p" is declined');
    expect(() => enruForms(lexicon({ verbs: [{ ...walk, ing: undefined }, will] }))).toThrow('the verb "walk" has a past and no ing form');
    expect(() => enruForms(lexicon({ verbs: [walk] }))).toThrow('no verb "will"');
  });
});

describe.skipIf(!hasCli)('the real table', () => {
  const rows = (file: string) => {
    const run = biome('read', join(repo, 'courses-biome/en-ru', `${file}.core-data`), '--format', 'json');
    expect(run.status, run.stderr).toBe(0);
    return (JSON.parse(run.stdout) as { value: any }[]).map(r => r.value);
  };
  const forms = enruForms({ nouns: rows('nouns'), adjectives: rows('adjectives'), verbs: rows('verbs'), possessives: rows('possessives'), pronouns: rows('personalPronouns'), prepositions: rows('prepositions') });
  const sealed = () => {
    const raw = execFileSync('git', ['show', `${SEALED.rev}:${SEALED.path}`], { cwd: repo });
    expect(createHash('sha256').update(raw).digest('hex'), `${SEALED.path} at ${SEALED.rev}`).toBe(SEALED.sha256);
    return JSON.parse(raw.toString('utf8')).forms as Record<string, FormEntry[]>;
  };
  const sizes = (prefix: string) => Object.entries(forms).filter(([lexeme]) => lexeme.startsWith(`${prefix}:`)).map(([, entries]) => entries.length);

  it('has 49 lexemes with 329 forms: 14 nouns, 7 adjectives, 7 possessives, 7 pronouns, "not", the prepositions, 9 verbs and 3 auxiliaries', () => {
    expect(['noun', 'adjective', 'possessive', 'pronoun', 'neg', 'prep', 'verb', 'aux'].map(prefix => [sizes(prefix).length, sizes(prefix).reduce((a, b) => a + b, 0)]))
      .toEqual([[14, 28], [7, 7], [7, 7], [7, 7], [1, 1], [1, 7], [9, 252], [3, 20]]);
  });

  it('keeps the facts of the lexicon that the engine asks for: "am", "went", "children", "has", the empty subject preposition and "will be"', () => {
    expect([formOf(forms['verb:be'], { Tense: 'present', Person: '1', Number: 'sg' }), formOf(forms['verb:go'], { Tense: 'Past' }), formOf(forms['noun:child'], { Number: 'pl' }), formOf(forms['verb:have'], { Tense: 'Pres', Person: '3', Number: 'sg' }),
      formOf(forms['prep:role'], { Case: 'Subj' }), formOf(forms['verb:be'], { Tense: 'future', Person: '3', Number: 'pl' }), formOf(forms['pronoun:she'], {})]).toEqual(['am', 'went', 'children', 'has', '', 'will be', 'she']);
  });

  it('is the table that scripts/build-pack-en.mjs built: the same lexemes in the same order, each with the same entries, bundles and forms in the same order', () => {
    const want = sealed();
    expect(Object.keys(forms)).toEqual(Object.keys(want));
    expect(Object.keys(want).filter(lexeme => JSON.stringify(forms[lexeme]) !== JSON.stringify(want[lexeme]))).toEqual([]);
  });
});
