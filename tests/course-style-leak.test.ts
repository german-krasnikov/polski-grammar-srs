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

/** Every string a Front block can show, for every style, derived exactly the way
 *  StyleComposer.block (Kotlin) resolves each BlockKind: authored styleContent wins,
 *  derived skill.formula/focus/methods.situations.introduce otherwise. Only Front blocks
 *  are listed (StylesBlueprint.md §1 table: Formula/Table for rule-first, Scene for
 *  situation-first, NativeParallel for native-contrast, Examples for minimal-theory) —
 *  Rule/WhyOnDemand/Changes/Contrast are Back-only and legitimately show the answer after
 *  reveal, so they are intentionally excluded here. */
function frontVisibleStrings(skill: (typeof course.skills)[number]): { field: string; value: string }[] {
  const content = (skill.styleContent ?? {}) as SkillStyleContent;
  const tableRows = content.table?.rows ?? (skill.focus ? [{ label: '', before: skill.focus.before, after: skill.focus.after }] : []);
  return [
    ...(skill.formula ? [{ field: 'formula', value: skill.formula }] : []),
    ...(skill.focus ? [
      { field: 'focus.before', value: skill.focus.before },
      { field: 'focus.after', value: skill.focus.after },
    ] : []),
    ...tableRows.flatMap((row, i) => [
      { field: `table.rows[${i}].before`, value: row.before },
      { field: `table.rows[${i}].after`, value: row.after },
    ]),
    { field: 'scene', value: content.scene ?? skill.methods.situations.introduce },
    ...(content.nativeParallel ?? []).flatMap(pair => [
      { field: 'nativeParallel.native', value: pair.native },
      { field: 'nativeParallel.target', value: pair.target },
    ]),
    ...(content.examples ?? []).map(example => ({ field: 'examples', value: example })),
  ];
}

test('no Front-visible string (formula/table/focus/scene/nativeParallel/examples) equals or embeds a producible answer', () => {
  for (const skill of course.skills) {
    const answers = producibleAnswers(skill.id);
    if (answers.size === 0) continue;
    for (const { field, value } of frontVisibleStrings(skill)) {
      const normalized = normalize(value);
      expect(answers.has(normalized), `${skill.id}.${field} "${value}" equals a producible answer`).toBe(false);
      for (const answer of answers) {
        if (!answer) continue;
        // Either direction: a short field (focus/table) can be a substring of a full-sentence
        // answer, and a full-sentence field (nativeParallel.target/examples) can embed a short
        // answer fragment. A single-token side (no space) is exempt from the *substring* check —
        // one bare grammatical word/ending (e.g. "-ą", or a closed-class pronoun form) is exactly
        // what formula/scene are allowed to show; only a multi-word PHRASE match hands over the
        // graded transformation itself. Exact equality (above) still catches a single-token field
        // that alone equals the whole answer.
        const multiWordEmbed = (answer.includes(' ') && normalized.includes(answer)) ||
          (normalized.includes(' ') && answer.includes(normalized));
        expect(multiWordEmbed, `${skill.id}.${field} "${value}" embeds producible answer "${answer}"`).toBe(false);
      }
    }
  }
});
