import { describe, expect, test } from 'vitest';
import { validateLifehacks } from '../scripts/validate-lifehacks.mjs';
import lifehacks from '../courses/pairs/pl-ru/lifehacks.json';
import curriculum from '../courses/lang/pl/curriculum.json';

const curriculumIds = (curriculum as unknown as Array<{ id: string }>).map((skill) => skill.id);
const copy = () => structuredClone(lifehacks);

// EnRuPackPlan.md §4.4/§6 EN-20: pl-ru's first, deliberately incomplete lifehacks set — 5 records
// on the 5 skills the plan names (most typologically unexpected transfer points for a ru-speaker
// learning pl), not full 16-skill coverage (that is an explicit future task, see plan §7).
const EXPECTED_SKILLS = ['case.gen.neg', 'case.inst', 'agreement.my', 'aspect', 'mixed'];

describe('pl-ru lifehacks.json (EnRuPackPlan.md §4.4/§6 EN-20)', () => {
  test('validates schema and resolves every skillId against lang/pl/curriculum.json', () => {
    expect(() => validateLifehacks('pl-ru', copy(), curriculumIds)).not.toThrow();
  });

  test('has exactly 5 records, one per the plan-named skill, no more', () => {
    expect(lifehacks.lifehacks).toHaveLength(5);
    const covered = lifehacks.lifehacks.map((lifehack) => lifehack.skillId);
    expect(new Set(covered)).toEqual(new Set(EXPECTED_SKILLS));
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
