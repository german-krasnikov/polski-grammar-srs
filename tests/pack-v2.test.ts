import { describe, expect, test } from 'vitest';
import { LEXICON_KEYS, splitCoursePack, reconstructCoursePack } from '../scripts/migrate-v1-to-v2.mjs';
import { validatePackV2 } from '../scripts/validate-pack-v2.mjs';
import course from '../courses/pl-ru/course.json';
import lexicon from '../courses/lang/pl/lexicon.json';
import pair from '../courses/pairs/pl-ru/pair.json';
import features from '../courses/core/features.json';
import constructions from '../courses/core/constructions.json';
import curriculum from '../courses/lang/pl/curriculum.json';
import lang from '../courses/lang/pl/lang.json';

describe('UC-12 schema v2 split', () => {
  test('checked-in lexicon.json/pair.json are exactly courses/pl-ru/course.json split by lexicon keys', () => {
    const { lexicon: expectedLexicon, pair: expectedPair } = splitCoursePack(structuredClone(course));
    expect(lexicon).toEqual(expectedLexicon);
    expect(pair).toEqual(expectedPair);
  });

  test('reconstructCoursePack(lexicon, pair) recovers course.json exactly (v2 pack load == v1)', () => {
    expect(reconstructCoursePack(structuredClone(lexicon), structuredClone(pair))).toEqual(course);
  });

  test('every lexicon key is language-scoped, never pair-scoped', () => {
    for (const key of LEXICON_KEYS) expect(lexicon).toHaveProperty(key);
    for (const key of LEXICON_KEYS) expect(pair).not.toHaveProperty(key);
  });

  test('validatePackV2 passes on the checked-in v2 layers', () => {
    expect(() => validatePackV2()).not.toThrow();
  });

  test('every curriculum construction is registered in core/constructions.json', () => {
    for (const skill of curriculum as unknown as Array<{ construction: string }>) {
      expect(Object.keys(constructions.constructions)).toContain(skill.construction);
    }
  });

  test('every curriculum focus/fixed feature is declared in core/features.json and lang.json usesFeatures', () => {
    const usedFeatures = new Set<string>();
    for (const skill of curriculum as unknown as Array<{ focus?: { feature: string }; fixed?: Record<string, string> }>) {
      if (skill.focus) usedFeatures.add(skill.focus.feature);
      for (const key of Object.keys(skill.fixed ?? {})) usedFeatures.add(key);
    }
    for (const key of usedFeatures) {
      expect(features.features).toHaveProperty(key);
      expect(lang.usesFeatures).toContain(key);
    }
  });

  test('EN-01: core/features.json additively carries en Case-roles + Aspect without touching pl values', () => {
    // Gap C (Plans/Kotlin/EnRuPackPlan.md §5): Case.values gains en's role values, a new
    // Aspect key appears, and none of the original pl values are removed or reordered away.
    const plCaseValues = ['Nom', 'Gen', 'Dat', 'Acc', 'Inst', 'Loc', 'Voc'];
    for (const value of plCaseValues) expect(features.features.Case.values).toContain(value);

    const enCaseRoles = ['Subj', 'Obj', 'In', 'With', 'To', 'About', 'Of'];
    for (const value of enCaseRoles) expect(features.features.Case.values).toContain(value);

    expect(features.features).toHaveProperty('Aspect');
    expect(features.features.Aspect.values).toEqual(['Simple', 'Continuous', 'Perfect']);
  });

  test('rejects a curriculum skill referencing an unregistered construction', async () => {
    const fs = await import('node:fs');
    const url = new URL('../courses/lang/pl/curriculum.json', import.meta.url);
    const original = fs.readFileSync(url, 'utf8');
    try {
      const broken = JSON.parse(original);
      broken[0].construction = 'core.does-not-exist';
      fs.writeFileSync(url, JSON.stringify(broken));
      // validatePackV2 re-reads every file from disk on each call, so no re-import is needed.
      expect(() => validatePackV2()).toThrow(/core\.does-not-exist/);
    } finally {
      fs.writeFileSync(url, original);
    }
  });
});
