import { expect, test } from 'vitest';
import course from '../courses/pl-ru/course.json';
import type { SkillStyleContent } from '../src/data/course';
import { generateForSkill, sentenceSeeds } from '../src/training/generator';

/** Every producible expected/accepted answer for one skill, across all seeds and RNG draws
 *  (owner picks, seed picks: see method-cycle-audit.test.ts for the same 6-way draw pattern). */
function producibleAnswers(skillId: string): Set<string> {
  const answers = new Set<string>();
  const originalRandom = Math.random;
  try {
    for (const seed of sentenceSeeds) for (const draw of [0, 1 / 6, 2 / 6, 3 / 6, 4 / 6, 5 / 6]) {
      Math.random = () => draw;
      let exercise;
      try { exercise = generateForSkill(skillId, seed); } catch { continue; }
      answers.add(normalize(exercise.expected));
      for (const accepted of exercise.accepted ?? []) answers.add(normalize(accepted));
    }
  } finally {
    Math.random = originalRandom;
  }
  return answers;
}

const normalize = (value: string) => value.trim().toLowerCase().replace(/[.!?]+$/, '');

test('no Front-visible authored style string equals or embeds a producible answer', () => {
  for (const skill of course.skills) {
    const content = skill.styleContent as SkillStyleContent | undefined;
    if (!content) continue;
    const answers = producibleAnswers(skill.id);
    const strings: { field: string; value: string }[] = [
      ...(content.nativeParallel ?? []).flatMap(pair => [
        { field: 'nativeParallel.native', value: pair.native },
        { field: 'nativeParallel.target', value: pair.target },
      ]),
      ...(content.examples ?? []).map(example => ({ field: 'examples', value: example })),
      ...(content.scene ? [{ field: 'scene', value: content.scene }] : []),
    ];
    for (const { field, value } of strings) {
      const normalized = normalize(value);
      expect(answers.has(normalized), `${skill.id}.${field} "${value}" equals a producible answer`).toBe(false);
      if (field === 'nativeParallel.target') {
        for (const answer of answers) {
          if (!answer) continue;
          expect(normalized.includes(answer), `${skill.id}.${field} "${value}" contains producible answer "${answer}"`).toBe(false);
        }
      }
    }
  }
});
