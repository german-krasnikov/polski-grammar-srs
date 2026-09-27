import { describe, expect, test } from 'vitest';
import { assertNoDuplicateBlockKinds } from '../scripts/validate-course.mjs';

/**
 * C3 (EmphasisUXAudit-2026-09-27.md E10): a block kind repeated within one phase, or shared
 * between front and back, renders twice on the revealed card — front blocks stay visible after
 * reveal (TrainingWebApp.kt/AndroidTrainingScreen.kt append both). The validator must reject both
 * shapes so a recipe author can't reintroduce rule-first's/native-contrast's pre-fix duplicate.
 */
describe('assertNoDuplicateBlockKinds', () => {
  const recipe = (front: string[], back: string[]) => ({ id: 'test-style', blocks: { front, back } });

  test('rejects a kind repeated within front', () => {
    expect(() => assertNoDuplicateBlockKinds(recipe(['formula', 'formula'], ['rule']))).toThrow(/duplicate block kind formula/);
  });

  test('rejects a kind repeated within back', () => {
    expect(() => assertNoDuplicateBlockKinds(recipe(['table'], ['rule', 'contrast', 'rule']))).toThrow(/duplicate block kind rule/);
  });

  test('rejects a kind shared between front and back', () => {
    expect(() => assertNoDuplicateBlockKinds(recipe(['formula'], ['formula', 'rule']))).toThrow(/formula.*both front and back/);
  });

  test('accepts disjoint, duplicate-free front/back', () => {
    expect(() => assertNoDuplicateBlockKinds(recipe(['table', 'formula'], ['rule', 'changes', 'contrast']))).not.toThrow();
  });

  test('accepts the fixed real rule-first and native-contrast recipes', async () => {
    const ruleFirst = (await import('../courses/styles/rule-first.json')).default;
    const nativeContrast = (await import('../courses/styles/native-contrast.json')).default;
    expect(() => assertNoDuplicateBlockKinds(ruleFirst)).not.toThrow();
    expect(() => assertNoDuplicateBlockKinds(nativeContrast)).not.toThrow();
  });
});
