import { expect, test } from 'vitest';
import course from '../courses/pl-ru/course.json';
import frequency from '../courses/pl-ru/frequency-top1000.json';
import { validateCoursePack } from '../scripts/validate-course.mjs';

const stages = ['introduce', 'retrieve', 'feedback', 'review'] as const;

test('all sixteen skills author each stage for both methods', () => {
  expect(course.skills).toHaveLength(16);
  for (const skill of course.skills) for (const method of ['logic', 'situations'] as const) {
    const content = skill.methods[method] as Record<string, string>;
    for (const stage of stages) expect(content[stage]?.trim(), `${skill.id}/${method}/${stage}`).toBeTruthy();
  }
});

test('pack validation rejects a missing or blank method stage with its path', () => {
  const missing = structuredClone(course) as unknown as {
    skills: Array<{ methods: { logic: Record<string, string> } }>;
  };
  delete missing.skills[0].methods.logic.introduce;
  expect(() => validateCoursePack(missing, frequency)).toThrow(/skills\/0\/methods\/logic\/introduce/);
  const blank = structuredClone(course) as unknown as {
    skills: Array<{ methods: { situations: Record<string, string> } }>;
  };
  blank.skills[1].methods.situations.review = '   ';
  expect(() => validateCoursePack(blank, frequency)).toThrow(/skills\/1\/methods\/situations\/review/);
});
