import { describe, expect, test } from 'vitest';
import { validateLifehacks } from '../scripts/validate-lifehacks.mjs';
import lifehacks from '../courses/pairs/pl-ru/lifehacks.json';
import curriculum from '../courses/lang/pl/curriculum.json';

const curriculumIds = (curriculum as unknown as Array<{ id: string }>).map((skill) => skill.id);
const copy = () => structuredClone(lifehacks);

// EnRuPackPlan.md §4.4/§6 EN-20 authored a deliberately incomplete first set (5 records on the 5
// most typologically unexpected skills). This later task closes the rest of §7's open item ("full
// coverage of all 16 pl-ru skills") — every lang/pl/curriculum.json skill now has at least 2.
describe('pl-ru lifehacks.json (full 16-skill coverage, EnRuPackPlan.md §4.4/§7 follow-up)', () => {
  test('validates schema and resolves every skillId against lang/pl/curriculum.json', () => {
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
