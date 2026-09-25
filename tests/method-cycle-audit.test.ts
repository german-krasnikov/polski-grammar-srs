import { expect, test } from 'vitest';
import course from '../courses/pl-ru/course.json';
import { generateChain, generateForSkill, sentenceSeeds } from '../src/training/generator';

test('intro cues do not contain answers across focused seeds, owner draws, and chains', () => {
  const originalRandom = Math.random;
  try {
    for (const skill of course.skills) for (const seed of sentenceSeeds) for (const draw of [0, 1 / 6, 2 / 6, 3 / 6, 4 / 6, 5 / 6]) {
      Math.random = () => draw;
      const exercise = generateForSkill(skill.id, seed);
      expect(exercise.source).toBeTruthy();
      expect(exercise.prompt).toBeTruthy();
      for (const method of ['logic', 'situations'] as const) {
        expect(skill.methods[method].introduce).not.toContain(exercise.expected);
      }
    }
    for (const seed of sentenceSeeds) for (const exercise of generateChain(seed)) {
      const skill = course.skills.find(item => item.id === exercise.primarySkill)!;
      for (const method of ['logic', 'situations'] as const) {
        expect(skill.methods[method].introduce).not.toContain(exercise.expected);
      }
    }
  } finally {
    Math.random = originalRandom;
  }
});

test('known variant-specific scenes stay neutral across owner, subject, and tense changes', () => {
  const byId = Object.fromEntries(course.skills.map(skill => [skill.id, skill.methods.situations.introduce]));
  expect(byId['agreement.my']).not.toContain('книге друга');
  expect(byId['sentence.question']).not.toMatch(/предмет/iu);
  expect(byId['verb.future']).not.toMatch(/będę/iu);
  expect(byId['verb.past']).not.toMatch(/вчера/iu);
  expect(byId.mixed).not.toMatch(/друг|вчера/iu);
  expect(byId['case.gen.neg']).not.toMatch(/увидел|видел/iu);
});
