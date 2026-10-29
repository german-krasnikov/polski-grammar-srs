import { describe, expect, test } from 'vitest';
import { validateLifehacks } from '../scripts/validate-lifehacks.mjs';
import lifehacks from '../courses/pairs/pl-ru/lifehacks.json';

// The 16 skill ids of the pl curriculum, in the order of the file lang/pl/curriculum.json (sha256 4f98cb4e...) that left the tree in R1.13. The same fact is
// courses-biome/pl-ru/skills.core-data, which the Kotlin tests and `biome data check` read; this old TS test keeps its own copy until R1.18 maps it to a Kotlin test.
const curriculumIds = [
  'case.acc.n', 'case.acc.f', 'case.acc.m', 'case.gen.neg', 'case.inst', 'case.loc', 'case.dat', 'agreement.my',
  'verb.present', 'verb.past', 'verb.future', 'aspect', 'pronouns', 'mixed', 'sentence.question', 'sentence.plural',
];
const copy = () => structuredClone(lifehacks);

// EnRuPackPlan.md §4.4/§6 EN-20 authored a deliberately incomplete first set (5 records on the 5
// most typologically unexpected skills). This later task closes the rest of §7's open item ("full
// coverage of all 16 pl-ru skills") — every one of the 16 pl skills now has at least 2.
describe('pl-ru lifehacks.json (full 16-skill coverage, EnRuPackPlan.md §4.4/§7 follow-up)', () => {
  test('validates schema and resolves every skillId against the 16 pl skill ids', () => {
    expect(() => validateLifehacks('pl-ru', copy(), curriculumIds)).not.toThrow();
  });

  test('covers every one of the 16 pl curriculum skills at least twice', () => {
    const counts = new Map<string, number>();
    for (const lifehack of lifehacks.lifehacks) {
      if (lifehack.skillId === null) continue;
      counts.set(lifehack.skillId, (counts.get(lifehack.skillId) ?? 0) + 1);
    }
    for (const id of curriculumIds) expect(counts.get(id) ?? 0).toBeGreaterThanOrEqual(2);
  });

  test('every lifehack has a real, non-placeholder source.citation', () => {
    for (const lifehack of lifehacks.lifehacks) {
      expect(lifehack.source.citation.trim().length).toBeGreaterThan(10);
      expect(lifehack.source.citation).not.toMatch(/^(tbd|todo|n\/a|unknown|placeholder)$/i);
    }
  });

  test('votes stay an unused placeholder (no invented vote counts, ADR-15)', () => {
    for (const lifehack of lifehacks.lifehacks) {
      expect(lifehack.votes).toEqual({ helpful: 0, notHelpful: 0 });
    }
  });

  test.each([
    ['unprefixed id', (data: typeof lifehacks) => { data.lifehacks[0].id = 'not-prefixed'; }, /must be prefixed/],
    ['duplicate id', (data: typeof lifehacks) => { data.lifehacks[1].id = data.lifehacks[0].id; }, /duplicate id/],
    ['unknown skillId', (data: typeof lifehacks) => { data.lifehacks[0].skillId = 'does-not-exist'; }, /not a known curriculum skill/],
    ['null skillId without topic', (data: typeof lifehacks) => { (data.lifehacks[0] as { skillId: string | null }).skillId = null; }, /needs a non-empty topic/],
    ['empty citation', (data: typeof lifehacks) => { data.lifehacks[0].source.citation = '   '; }, /source\/citation/],
    ['placeholder citation', (data: typeof lifehacks) => { data.lifehacks[0].source.citation = 'TBD'; }, /empty or placeholder citation/],
    ['pairId mismatch', (data: typeof lifehacks) => { data.pairId = 'en-ru'; }, /expected "pl-ru"/],
    ['fabricated vote count', (data: typeof lifehacks) => { (data.lifehacks[0].votes as { helpful: number }).helpful = 7; }, /votes/],
  ])('rejects %s', (_name, mutate, message) => {
    const data = copy();
    mutate(data);
    expect(() => validateLifehacks('pl-ru', data, curriculumIds)).toThrow(message);
  });
});
