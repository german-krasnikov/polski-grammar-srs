// R1.12 (Plans/Biome/R1Design.md): the form table of pl-ru is derived from the lexicon rows by dsl/derive-forms.mjs. The rules on hand-built rows (expectations written out), the real table against
// the pinned golden forms (tests/fixtures/core-golden/grammar.json, which scripts/build-pack.mjs checked before it lost its source), and the rows against dsl/forms.data-contract.
// The data cases need a biome-dsl checkout (see course-data.test.ts); without one they are skipped, which is NOT RUN, not a pass.
import { beforeAll, describe, expect, it } from 'vitest';
import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import { formRows, plruForms, type FormEntry } from '../dsl/derive-forms.mjs';
import { PACK, biome, hasCli, repo, row, rowsOf } from './support/course-data';

const CASES = ['nom', 'gen', 'dat', 'acc', 'inst', 'loc', 'voc'];
/** The seven cases of a form record, each text tagged with `tag` and the case. */
const cases = (tag: string) => Object.fromEntries(CASES.map(c => [c, `${tag}-${c}`]));
const genders = (tag: string) => Object.fromEntries(['m_personal', 'm_animate', 'm_inanimate', 'f', 'n'].map(g => [g, cases(`${tag}-${g}`)]));
const persons = (tag: string) => ({ p1: `${tag}1`, p2: `${tag}2`, p3: `${tag}3` });
const noun = { id: 'n', forms: { sg: cases('n-sg'), pl: cases('n-pl') } };
const adjective = { id: 'a', forms: { sg: genders('a-sg'), pl: genders('a-pl') } };
const verb = (id: string, change: Record<string, unknown> = {}) => ({
  id, lemma: `lemma-${id}`, aspect: 'imperfective', futureType: 'compound', present: { sg: persons(`${id}-sg`), pl: persons(`${id}-pl`) },
  pastStem: { m: 'M', f: 'F', n: 'N', mp: 'MP', np: 'NP' }, ...change,
});
const morphology = { futureAuxiliary: { verbId: 'be', forms: { sg: persons('be-sg'), pl: persons('be-pl') } } };
const lexicon = (change: Record<string, unknown> = {}) => ({ nouns: [], adjectives: [], possessives: [], verbs: [], morphology, ...change });
/** The form under a bundle, or undefined: the bundle is compared as the engine does, as a set of features. */
const formOf = (entries: FormEntry[], bundle: Record<string, string>) => entries.find(e => JSON.stringify(Object.entries(e.bundle).sort()) === JSON.stringify(Object.entries(bundle).sort()))?.form;

describe('the rules of the form table on hand-built rows', () => {
  it('a noun has its seven cases in both numbers, in the order of the row', () => {
    const entries = plruForms(lexicon({ nouns: [noun] }))['noun:n'];
    expect(entries).toHaveLength(14);
    expect(entries[0]).toEqual({ bundle: { Number: 'sg', Case: 'nom' }, form: 'n-sg-nom' });
    expect(entries[13]).toEqual({ bundle: { Number: 'pl', Case: 'voc' }, form: 'n-pl-voc' });
    expect(entries.map(e => e.form)).toEqual(['sg', 'pl'].flatMap(n => CASES.map(c => `n-${n}-${c}`)));
  });

  it('an adjective is asked for by the id of the model (m-personal) and read from the key of the row (m_personal)', () => {
    const entries = plruForms(lexicon({ adjectives: [adjective] }))['adjective:a'];
    expect(entries).toHaveLength(70);
    expect(formOf(entries, { Number: 'sg', Gender: 'm-personal', Case: 'gen' })).toBe('a-sg-m_personal-gen');
    expect(formOf(entries, { Number: 'pl', Gender: 'm-inanimate', Case: 'voc' })).toBe('a-pl-m_inanimate-voc');
  });

  it('a declined possessive has the singular by gender and, in the plural, the masculine personal group and all the rest', () => {
    const possessive = { id: 'p', forms: { kind: 'declined', sg: genders('p-sg'), pl: { m_personal: cases('p-pl-mp'), other: cases('p-pl-other') } } };
    const entries = plruForms(lexicon({ possessives: [possessive] }))['possessive:p'];
    expect(entries).toHaveLength(70);
    expect(formOf(entries, { Number: 'sg', Gender: 'm-animate', Case: 'acc' })).toBe('p-sg-m_animate-acc');
    expect(formOf(entries, { Number: 'pl', Gender: 'm-personal', Case: 'acc' })).toBe('p-pl-mp-acc');
    for (const gender of ['m-animate', 'm-inanimate', 'f', 'n']) expect(formOf(entries, { Number: 'pl', Gender: gender, Case: 'acc' })).toBe('p-pl-other-acc');
  });

  it('an invariable possessive is one text everywhere', () => {
    const entries = plruForms(lexicon({ possessives: [{ id: 'q', forms: { kind: 'invariant', value: 'ich' } }] }))['possessive:q'];
    expect(entries).toHaveLength(70);
    expect(new Set(entries.map(e => e.form))).toEqual(new Set(['ich']));
  });

  it('the present of an imperfective verb is its row, a perfective verb has none', () => {
    const forms = plruForms(lexicon({ verbs: [verb('v'), verb('w', { aspect: 'perfective' })] }));
    expect(formOf(forms['verb:v'], { Tense: 'present', Person: '2', Number: 'pl' })).toBe('v-pl2');
    expect(forms['verb:v']).toHaveLength(36);
    expect(forms['verb:w']).toHaveLength(30);
    expect(formOf(forms['verb:w'], { Tense: 'present', Person: '1', Number: 'sg' })).toBeUndefined();
  });

  it('the past takes the stem by number and gender and the ending by person, number and gender', () => {
    const entries = plruForms(lexicon({ verbs: [verb('v')] }))['verb:v'];
    const past = (person: string, number: string, gender: string) => formOf(entries, { Tense: 'past', Person: person, Number: number, Gender: gender });
    expect([past('1', 'sg', 'm-personal'), past('1', 'sg', 'm-animate'), past('1', 'sg', 'f'), past('1', 'sg', 'n')]).toEqual(['Mem', 'Mem', 'Fm', 'Nm']);
    expect([past('2', 'sg', 'm-personal'), past('2', 'sg', 'f'), past('2', 'sg', 'n')]).toEqual(['Meś', 'Fś', 'Nś']);
    expect([past('3', 'sg', 'm-personal'), past('3', 'sg', 'f'), past('3', 'sg', 'n')]).toEqual(['M', 'F', 'N']);
    expect([past('1', 'pl', 'm-personal'), past('1', 'pl', 'f'), past('1', 'pl', 'n'), past('1', 'pl', 'm-animate')]).toEqual(['MPśmy', 'NPśmy', 'NPśmy', 'NPśmy']);
    expect([past('2', 'pl', 'm-personal'), past('2', 'pl', 'f')]).toEqual(['MPście', 'NPście']);
    expect([past('3', 'pl', 'm-personal'), past('3', 'pl', 'n')]).toEqual(['MP', 'NP']);
    expect(past('1', 'sg', 'm-inanimate')).toBeUndefined();
  });

  it('a neuter past without its own stem takes the masculine one', () => {
    const entries = plruForms(lexicon({ verbs: [verb('v', { pastStem: { m: 'M', f: 'F', mp: 'MP', np: 'NP' } })] }))['verb:v'];
    expect(formOf(entries, { Tense: 'past', Person: '1', Number: 'sg', Gender: 'n' })).toBe('Mm');
    expect(formOf(entries, { Tense: 'past', Person: '3', Number: 'sg', Gender: 'n' })).toBe('M');
  });

  it('the future is the auxiliary and the lemma, the present for a verb of that type, and the auxiliary alone for the auxiliary itself', () => {
    const forms = plruForms(lexicon({ verbs: [verb('v'), verb('w', { futureType: 'present' }), verb('be')] }));
    const future = (id: string, person: string, number: string) => formOf(forms[`verb:${id}`], { Tense: 'future', Person: person, Number: number });
    expect([future('v', '1', 'sg'), future('v', '3', 'pl')]).toEqual(['be-sg1 lemma-v', 'be-pl3 lemma-v']);
    expect([future('w', '1', 'sg'), future('w', '3', 'pl')]).toEqual(['w-sg1', 'w-pl3']);
    expect([future('be', '1', 'sg'), future('be', '3', 'pl')]).toEqual(['be-sg1', 'be-pl3']);
  });

  it('the lexemes come in the order nouns, adjectives, possessives, verbs and the rows keep it', () => {
    const forms = plruForms(lexicon({ nouns: [noun], adjectives: [adjective], possessives: [{ id: 'q', forms: { kind: 'invariant', value: 'ich' } }], verbs: [verb('v')] }));
    expect(Object.keys(forms)).toEqual(['noun:n', 'adjective:a', 'possessive:q', 'verb:v']);
    expect(formRows(forms).split('\n').filter(Boolean).map(line => line.match(/lexeme: "([^"]+)"/)![1])).toEqual(Object.keys(forms));
  });
});

describe.skipIf(!hasCli)('the real table', () => {
  let forms: Record<string, any[]>;
  let entriesOf: (prefix: string) => number[];
  beforeAll(() => {
    const rows = (file: string) => {
      const run = biome('read', join(PACK, `${file}.core-data`), '--format', 'json');
      expect(run.status, run.stderr).toBe(0);
      return (JSON.parse(run.stdout) as { value: any }[]).map(r => r.value);
    };
    forms = plruForms({ nouns: rows('nouns'), adjectives: rows('adjectives'), verbs: rows('verbs'), possessives: rows('possessives'), morphology: rows('morphology')[0] });
    entriesOf = (prefix: string) => Object.entries(forms).filter(([lexeme]) => lexeme.startsWith(`${prefix}:`)).map(([, entries]) => entries.length);
  });

  it('has 14 nouns, 7 adjectives, 7 possessives and 11 verbs with 196, 490, 490 and 384 forms', () => {
    expect([entriesOf('noun').length, entriesOf('adjective').length, entriesOf('possessive').length, entriesOf('verb').length]).toEqual([14, 7, 7, 11]);
    expect([entriesOf('noun'), entriesOf('adjective'), entriesOf('possessive'), entriesOf('verb')].map(sizes => sizes.reduce((a, b) => a + b, 0))).toEqual([196, 490, 490, 384]);
  });

  it('agrees with every golden form: 1560 forms, each with a golden case, none different', () => {
    const golden = JSON.parse(readFileSync(join(repo, 'tests/fixtures/core-golden/grammar.json'), 'utf8')).cases as { id: string; input: Record<string, any>; expected: { form: string } }[];
    const key = (lexeme: string, bundle: Record<string, string>) => `${lexeme}\u0000${Object.entries(bundle).sort().map(([k, v]) => `${k}=${v}`).join(',')}`;
    const expected = new Map<string, string>();
    for (const { id, input: i, expected: e } of golden) {
      if (id.startsWith('G-NOUN-')) expected.set(key(`noun:${i.nounId}`, { Number: i.number, Case: i.gramCase }), e.form);
      else if (id.startsWith('G-ADJ-')) expected.set(key(`adjective:${i.adjectiveId}`, { Number: i.number, Gender: i.gender, Case: i.gramCase }), e.form);
      else if (id.startsWith('G-POSS-')) expected.set(key(`possessive:${i.owner}`, { Number: i.number, Gender: i.gender, Case: i.gramCase }), e.form);
      else if (id.startsWith('G-VERB-') && !id.endsWith('-rejected')) {
        const bundle: Record<string, string> = { Tense: i.tense, Person: String(i.person), Number: i.number };
        expected.set(key(`verb:${i.verbId}`, i.tense === 'past' ? { ...bundle, Gender: i.gender } : bundle), e.form);
      }
    }
    const different: string[] = [];
    let covered = 0;
    for (const [lexeme, entries] of Object.entries(forms)) {
      for (const { bundle, form } of entries) {
        const want = expected.get(key(lexeme, bundle));
        if (want === undefined) continue;
        covered += 1;
        if (want !== form) different.push(`${lexeme} ${JSON.stringify(bundle)}: ${form} != ${want}`);
      }
    }
    expect(different).toEqual([]);
    expect(covered).toBe(1560);
  });
});

describe.skipIf(!hasCli)('form rows against dsl/forms.data-contract', () => {
  const { check, problem } = rowsOf('FormLexemeRow');
  const pair = (feature: string, value: string) => `{feature: "${feature}" value: "${value}"}`;
  const lexeme = (name: string, change: Record<string, string | undefined> = {}) =>
    row('FORMS', { lexeme: `"${name}"`, forms: `[{bundle: [${pair('Number', 'sg')} ${pair('Case', 'nom')}] form: "żona"} {bundle: [] form: "x"}]` }, change);

  it('lexemes with bundles of different features, an empty bundle and non-ASCII forms are accepted', () =>
    expect(check([lexeme('noun:wife'), lexeme('verb:be')].join('\n') + '\n')).toEqual({ status: 0, problems: [] }));
  it('a second row for one lexeme is DATA_DUPLICATE at its name', () => problem([lexeme('noun:wife'), lexeme('noun:wife')], 1, '"noun:wife"', 'DATA_DUPLICATE'));
  it('a feature of a bundle without its value is DATA_REQUIRED at the { of the pair', () =>
    problem([lexeme('noun:wife', { forms: '[{bundle: [{feature: "Number"}] form: "x"}]' })], 0, '{feature: "Number"}', 'DATA_REQUIRED'));
  it('a form left out is DATA_REQUIRED at the { of the entry', () =>
    problem([lexeme('noun:wife', { forms: `[{bundle: [${pair('Number', 'sg')}]}]` })], 0, '{bundle:', 'DATA_REQUIRED'));
  it('a form that is a number is DATA_TYPE at the value', () => problem([lexeme('noun:wife', { forms: `[{bundle: [] form: 7}]` })], 0, '7', 'DATA_TYPE'));
  it('no row at all is DATA_COUNT at 1:1', () => expect(check('').problems).toEqual(['FormLexemeRow.core-data:1:1: DATA_COUNT']));
  it('a bundle written twice for one lexeme is beyond the contract: it passes here and the mapper refuses it', () =>
    expect(check(lexeme('noun:wife', { forms: `[{bundle: [] form: "a"} {bundle: [] form: "b"}]` }) + '\n')).toEqual({ status: 0, problems: [] }));
});
