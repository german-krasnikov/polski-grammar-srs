// R1.5 (Plans/Biome/R1Design.md): the lexicon rows against dsl/course.data-contract: what `biome data` rejects, with the exact position of each problem.
// The keys of the data that are no identifiers are written m_personal.. and p1..p3 (owner decision 2026-10-29); the ids as values stay verbatim.
import { describe, expect, it } from 'vitest';
import { readFileSync } from 'node:fs';
import { hasCli, realFile, row, rowsOf } from './support/course-data';

const CASES = ['nom', 'gen', 'dat', 'acc', 'inst', 'loc', 'voc'];
const GENDERS = ['m_personal', 'm_animate', 'm_inanimate', 'f', 'n'];
/** The seven cases of one form record, each text tagged with where it sits; `drop` leaves one case out. */
const cases = (tag: string, drop?: string) => `{${CASES.filter(c => c !== drop).map(c => `${c}: "${tag}-${c}"`).join(' ')}}`;
const genders = (drop?: string) => `{${GENDERS.filter(g => g !== drop).map(g => `${g}: ${cases(g)}`).join(' ')}}`;
const persons = (tag: string) => `{p1: "${tag}1" p2: "${tag}2" p3: "${tag}3"}`;

const noun = (id: string, change: Record<string, string | undefined> = {}) =>
  row('NOUN', { id: `"${id}"`, lemma: '"l"', meaning: '"m"', gender: '"f"', forms: `{sg: ${cases('sg')} pl: ${cases('pl')}}` }, change);
const adjective = (id: string, change: Record<string, string | undefined> = {}) =>
  row('ADJ', { id: `"${id}"`, lemma: '"l"', meaning: '"m"', forms: `{sg: ${genders()} pl: ${genders()}}` }, change);
const verb = (id: string, change: Record<string, string | undefined> = {}) => row('VERB', {
  id: `"${id}"`, lemma: '"l"', meaning: '"m"', aspect: 'imperfective', futureType: 'compound',
  present: `{sg: ${persons('s')} pl: ${persons('p')}}`, pastStem: '{m: "m" f: "f" n: "n" mp: "mp" np: "np"}',
}, change);

describe.skipIf(!hasCli)('noun rows', () => {
  const { check, problem } = rowsOf('NounRow');
  const withSkills = rowsOf('NounRow', 'SkillRow');
  it('a case left out is DATA_REQUIRED at the { of its number', () =>
    problem([noun('a', { forms: `{sg: ${cases('sg')} pl: ${cases('pl', 'voc')}}` })], 0, '{nom: "pl-nom"', 'DATA_REQUIRED'));
  it('a case the contract does not know is DATA_UNKNOWN_FIELD at its key', () =>
    problem([noun('a', { forms: `{sg: {${cases('sg').slice(1, -1)} ins: "x"} pl: ${cases('pl')}}` })], 0, 'ins:', 'DATA_UNKNOWN_FIELD'));
  it('a second noun with the same id is DATA_DUPLICATE at its id', () => problem([noun('a'), noun('a')], 1, '"a"', 'DATA_DUPLICATE'));
  it('a number for a text is DATA_TYPE at the value', () => problem([noun('a', { lemma: '5' })], 0, '5', 'DATA_TYPE'));
  it('a noun that a skill draws from cannot be removed: every skill that names it is DATA_REF', () => {
    const without = readFileSync(realFile('NounRow'), 'utf8').split('\n').filter(line => !line.startsWith('NOUN {id: "wife"')).join('\n');
    const { status, problems } = withSkills.check(without);
    expect(status).toBe(1);
    expect(problems.length).toBeGreaterThan(0);
    expect(problems.every(p => p.startsWith('skills.core-data:') && p.endsWith('DATA_REF'))).toBe(true);
  });
  it('a gender text is the model\'s to read: the contract takes any, Gender.fromId refuses it (PlRuLexiconRowsTest)', () =>
    expect(check(noun('a', { gender: '"m-persnal"' }) + '\n')).toEqual({ status: 0, problems: [] }));
});

describe.skipIf(!hasCli)('adjective rows', () => {
  const { check, problem } = rowsOf('AdjectiveRow');
  it('a gender left out is DATA_REQUIRED at the { of its number', () =>
    problem([adjective('a', { forms: `{sg: ${genders('n')} pl: ${genders()}}` })], 0, '{m_personal:', 'DATA_REQUIRED'));
  it('the dashed key of the old data is no key any more: the alias is the one spelling', () =>
    problem([adjective('a', { forms: `{sg: {m-personal: ${cases('x')}} pl: ${genders()}}` })], 0, '{m-personal:', 'DATA_REQUIRED'));
  it('a row with every gender in both numbers passes', () => expect(check(adjective('a') + '\n')).toEqual({ status: 0, problems: [] }));
});

describe.skipIf(!hasCli)('verb rows', () => {
  const { check, problem } = rowsOf('VerbRow');
  it('an aspect outside the enum is DATA_ENUM at the value', () => problem([verb('a', { aspect: 'soon' })], 0, 'soon', 'DATA_ENUM'));
  it('a perfective pair that is no verb is DATA_REF at the value', () => problem([verb('a', { perfectivePair: '"nope"' })], 0, '"nope"', 'DATA_REF'));
  it('a past stem without its neuter is DATA_REQUIRED at the { of the stem', () =>
    problem([verb('a', { pastStem: '{m: "m" f: "f" mp: "mp" np: "np"}' })], 0, '{m: "m"', 'DATA_REQUIRED'));
  it('the dashless person keys are the only ones: "1" is no key', () =>
    problem([verb('a', { present: `{sg: {"1": "x" p2: "x" p3: "x"} pl: ${persons('p')}}` })], 0, '{"1"', 'DATA_REQUIRED'));
  it('the perfective pair may be left out, and may name another verb of the file', () =>
    expect(check([verb('a', { perfectivePair: '"b"' }), verb('b')].join('\n') + '\n')).toEqual({ status: 0, problems: [] }));
});
