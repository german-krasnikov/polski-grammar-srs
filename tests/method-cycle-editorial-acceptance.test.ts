import { expect, test } from 'vitest';
import course from '../courses/pl-ru/course.json';
import { generateChain, generateForSkill, sentenceSeeds } from '../src/training/generator';

test('negation introduction remains true for focused present and chain past', () => {
  const introduction = course.skills.find(skill => skill.id === 'case.gen.neg')!.methods.situations.introduce;
  for (const seed of sentenceSeeds) {
    const focused = generateForSkill('case.gen.neg', seed);
    const chain = generateChain(seed).find(exercise => exercise.primarySkill === 'case.gen.neg')!;
    expect(focused.expected).toMatch(/^Nie widzę /);
    expect(chain.expected).toMatch(/^Nie widziałem /);
  }
  expect(introduction).not.toMatch(/не увидел(?:а)?(?:\s|[?.!,]|$)/iu);
});
