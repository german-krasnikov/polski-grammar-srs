import { describe, expect, test } from 'vitest';
import { validateLifehacks } from '../scripts/validate-lifehacks.mjs';
import lifehacks from '../courses/pairs/en-ru/lifehacks.json';
import curriculum from '../courses/lang/en/curriculum.json';

const curriculumIds = (curriculum as unknown as Array<{ id: string }>).map((skill) => skill.id);
const copy = () => structuredClone(lifehacks);

describe('en-ru lifehacks.json (EnRuPackPlan.md §4/§6 EN-19)', () => {
  test('validates schema and resolves every skillId against lang/en/curriculum.json', () => {
    expect(() => validateLifehacks('en-ru', copy(), curriculumIds)).not.toThrow();
  });

  test('covers every one of the 16 en curriculum skills at least once', () => {
    const covered = new Set(lifehacks.lifehacks.map((lifehack) => lifehack.skillId));
    for (const id of curriculumIds) expect(covered.has(id)).toBe(true);
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
    ['unknown skillId', (data: typeof lifehacks) => { data.lifehacks[0].skillId = 'en:does-not-exist'; }, /not a known curriculum skill/],
    ['null skillId without topic', (data: typeof lifehacks) => { (data.lifehacks[0] as { skillId: string | null }).skillId = null; }, /needs a non-empty topic/],
    ['empty citation', (data: typeof lifehacks) => { data.lifehacks[0].source.citation = '   '; }, /source\/citation/],
    ['placeholder citation', (data: typeof lifehacks) => { data.lifehacks[0].source.citation = 'TBD'; }, /empty or placeholder citation/],
    ['pairId mismatch', (data: typeof lifehacks) => { data.pairId = 'pl-ru'; }, /expected "en-ru"/],
    ['fabricated vote count', (data: typeof lifehacks) => { (data.lifehacks[0].votes as { helpful: number }).helpful = 7; }, /votes/],
  ])('rejects %s', (_name, mutate, message) => {
    const data = copy();
    mutate(data);
    expect(() => validateLifehacks('en-ru', data, curriculumIds)).toThrow(message);
  });
});
