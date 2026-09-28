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

describe('EN-11: lang/en lang.json + lexicon.json + prepositions.json', () => {
  test('lang/en/lang.json declares en Case-roles + Aspect (uses EN-01)', async () => {
    const enLang = await import('../courses/lang/en/lang.json');
    expect(enLang.default.code).toBe('en');
    expect(enLang.default.usesFeatures).toEqual(
      expect.arrayContaining(['Case', 'Number', 'Person', 'Tense', 'Polarity', 'Mood', 'Aspect']),
    );
    expect(enLang.default.case).toEqual(expect.arrayContaining(['Subj', 'Obj', 'In', 'With', 'To', 'About', 'Of']));
  });

  test('validatePackV2 also validates the checked-in lang/en layers (generalized, not pl-only)', () => {
    expect(() => validatePackV2()).not.toThrow();
  });

  test('lang/en/lexicon.json has the 5 required lexical categories, each internally consistent', async () => {
    const enLexicon = await import('../courses/lang/en/lexicon.json');
    const lex = enLexicon.default as {
      nouns: Array<{ id: string; forms: { sg: string; pl: string } }>;
      adjectives: Array<{ id: string; forms: { invariant: string } }>;
      verbs: Array<{ id: string; lemma: string; present3sg?: string }>;
      personalPronouns: Record<string, { subject: string; object: string }>;
      possessives: Array<{ id: string; forms: { kind: string } }>;
    };
    expect(lex.nouns.length).toBeGreaterThan(0);
    for (const noun of lex.nouns) {
      expect(noun.forms.sg.length).toBeGreaterThan(0);
      expect(noun.forms.pl.length).toBeGreaterThan(0);
    }
    for (const adjective of lex.adjectives) expect(adjective.forms.invariant.length).toBeGreaterThan(0);
    for (const possessive of lex.possessives) expect(possessive.forms.kind).toBe('invariant');
    // do/be/have/will are auxiliaries as plain verbs[] rows (EnRuPackPlan.md §1.3), not a separate file.
    for (const auxId of ['do', 'be', 'have', 'will']) expect(lex.verbs.map((v) => v.id)).toContain(auxId);
    // English personal pronouns collapse pl's 9-way ja/ty/on/ona/ono/my/wy/oni/one split to 7 (no gender split on "they").
    expect(Object.keys(lex.personalPronouns).sort()).toEqual(['I', 'he', 'it', 'she', 'they', 'we', 'you'].sort());
  });

  test('lang/en/prepositions.json role table covers exactly lang.json case values, empty string for Subj/Obj', async () => {
    const enLang = await import('../courses/lang/en/lang.json');
    const enPrepositions = await import('../courses/lang/en/prepositions.json');
    const role = enPrepositions.default.prepositions.find((p: { id: string }) => p.id === 'role');
    if (!role) throw new Error('lang/en/prepositions.json has no "role" entry');
    expect(Object.keys(role.forms).sort()).toEqual([...enLang.default.case].sort());
    expect(role.forms.Subj).toBe('');
    expect(role.forms.Obj).toBe('');
    expect(role.forms.With).toBe('with');
  });

  test('rejects a malformed lang/en/lexicon.json (missing required category)', async () => {
    const fs = await import('node:fs');
    const url = new URL('../courses/lang/en/lexicon.json', import.meta.url);
    const original = fs.readFileSync(url, 'utf8');
    try {
      const broken = JSON.parse(original);
      delete broken.possessives;
      fs.writeFileSync(url, JSON.stringify(broken));
      expect(() => validatePackV2()).toThrow(/lang\/en\/lexicon\.json/);
    } finally {
      fs.writeFileSync(url, original);
    }
  });
});

describe('EN-12: lang/en/curriculum.json — 16 SkillSpec (EnRuPackPlan.md §1.2)', () => {
  type EnSkill = {
    id: string;
    construction: string;
    focus: { feature: string; from: string; to: string } | null;
    fixed: Record<string, string>;
    lexicalFilter: unknown;
    level: string;
    prerequisites: string[];
  };

  const EXPECTED_IDS = [
    'en:role.object',
    'en:verb.presentSimple',
    'en:possessive.my',
    'en:mood.question',
    'en:role.location',
    'en:role.instrument',
    'en:polarity.present',
    'en:verb.pastSimple',
    'en:role.recipient',
    'en:number.plural',
    'en:polarity.past',
    'en:verb.futureSimple',
    'en:pronouns',
    'en:verb.presentContinuous',
    'en:tense.contrast',
    'en:mixed',
  ];

  test('has exactly the 16 skill ids from §1.2, in order', async () => {
    const enCurriculum = (await import('../courses/lang/en/curriculum.json')).default as unknown as EnSkill[];
    expect(enCurriculum.map((skill) => skill.id)).toEqual(EXPECTED_IDS);
  });

  test('validatePackV2 also runs the §8.2 cross-checks against lang/en/curriculum.json (uses EN-01, EN-11)', () => {
    expect(() => validatePackV2()).not.toThrow();
  });

  test('every prerequisite references an id actually declared in this same file', async () => {
    const enCurriculum = (await import('../courses/lang/en/curriculum.json')).default as unknown as EnSkill[];
    const ids = new Set(enCurriculum.map((skill) => skill.id));
    for (const skill of enCurriculum) {
      for (const prerequisite of skill.prerequisites) expect(ids.has(prerequisite)).toBe(true);
    }
  });

  test('every skill uses only en-declared Case-role/Aspect values, never a pl case', async () => {
    const enCurriculum = (await import('../courses/lang/en/curriculum.json')).default as unknown as EnSkill[];
    const plOnlyCases = ['Nom', 'Gen', 'Dat', 'Acc', 'Inst', 'Loc', 'Voc'];
    for (const skill of enCurriculum) {
      const values = [skill.focus?.from, skill.focus?.to, ...Object.values(skill.fixed)].filter(Boolean) as string[];
      for (const value of values) expect(plOnlyCases).not.toContain(value);
    }
  });

  test('mood.question is the only skill whose construction is core.sentence.mood (needs orderWhen, gap D)', async () => {
    const enCurriculum = (await import('../courses/lang/en/curriculum.json')).default as unknown as EnSkill[];
    const moodSkills = enCurriculum.filter((skill) => skill.construction === 'core.sentence.mood');
    expect(moodSkills.map((skill) => skill.id)).toEqual(['en:mood.question']);
  });

  test('rejects a curriculum skill using a feature value lang/en/lang.json does not declare in usesFeatures', async () => {
    const fs = await import('node:fs');
    const url = new URL('../courses/lang/en/curriculum.json', import.meta.url);
    const original = fs.readFileSync(url, 'utf8');
    try {
      const broken = JSON.parse(original);
      broken[0].fixed.Bogus = 'Nope';
      fs.writeFileSync(url, JSON.stringify(broken));
      expect(() => validatePackV2()).toThrow(/lang\/en\/curriculum\.json/);
    } finally {
      fs.writeFileSync(url, original);
    }
  });
});

describe('EN-18: nativeParallel coverage for all 16 en-ru skills (EnRuPackPlan.md §2.2/§6)', () => {
  type NativeParallelPair = { native: string; target: string; note: string; matches: boolean };
  type EnRuSkill = { id: string; styleContent?: { nativeParallel?: NativeParallelPair[] } };

  test('every one of the 16 skills has a non-empty, well-formed nativeParallel (native-contrast renders its own content, not the rule-first fallback)', async () => {
    const enRuPair = (await import('../courses/pairs/en-ru/pair.json')).default as unknown as { skills: EnRuSkill[] };
    expect(enRuPair.skills.length).toBe(16);
    for (const skill of enRuPair.skills) {
      const pairs = skill.styleContent?.nativeParallel;
      expect(pairs, skill.id).toBeDefined();
      expect(pairs!.length, skill.id).toBeGreaterThan(0);
      for (const pair of pairs!) {
        expect(pair.native.trim().length, `${skill.id}.native`).toBeGreaterThan(0);
        expect(pair.target.trim().length, `${skill.id}.target`).toBeGreaterThan(0);
        expect(pair.note.trim().length, `${skill.id}.note`).toBeGreaterThan(0);
        expect(typeof pair.matches, `${skill.id}.matches`).toBe('boolean');
      }
    }
  });

  test('at least one skill authentically shows a match (typological contrast is not always "everything differs")', async () => {
    const enRuPair = (await import('../courses/pairs/en-ru/pair.json')).default as unknown as { skills: EnRuSkill[] };
    const anyMatch = enRuPair.skills.some((skill) => (skill.styleContent?.nativeParallel ?? []).some((pair) => pair.matches));
    expect(anyMatch).toBe(true);
  });

  test('no nativeParallel.target sentence is copy-pasted verbatim across two different skills (each is authored for its own contrast)', async () => {
    const enRuPair = (await import('../courses/pairs/en-ru/pair.json')).default as unknown as { skills: EnRuSkill[] };
    const seen = new Map<string, string>();
    for (const skill of enRuPair.skills) {
      for (const pair of skill.styleContent?.nativeParallel ?? []) {
        const key = pair.target.trim().toLowerCase();
        const owner = seen.get(key);
        expect(owner, `"${pair.target}" reused verbatim in ${skill.id} (first seen in ${owner})`).toBeUndefined();
        seen.set(key, skill.id);
      }
    }
  });
});
