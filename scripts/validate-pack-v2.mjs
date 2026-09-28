// UniversalCorePlan.md §3.1/§8.2/§12 UC-12: validates the schema-v2 layer files (core/*.json,
// every lang/<code>/lang.json + lexicon.json + prepositions.json it finds, pairs/pl-ru/pair.json
// and pairs/en-ru/pair.json) against their own schemas, then the cross-checks §8.2 names: a
// skill's `construction` exists in core/constructions.json, a feature value used by curriculum.json
// is declared both in core/features.json and in that language's lang.json `usesFeatures`, and
// curriculum.json's `lexicalFilter` references resolve against the actual lexicon. Those three
// cross-checks only run for a language that actually has a curriculum.json (EnRuPackPlan.md §6
// EN-12) — today that's pl and en both. It also proves pl-ru's lexicon.json + pair.json reconstruct
// a pack that is itself a valid v1 pack (reusing course-pack-v1.schema.json and validateCoursePack —
// see scripts/migrate-v1-to-v2.mjs's own `--check` for the exact-reconstruction proof; this script
// re-validates the *content*, not just the shape, of that reconstruction). That v1 bridge is
// pl-ru-specific by construction (course-pack-v1.schema.json's own case-label vocabulary is pl's 7
// cases, not a generic Case-role table — EnRuPackPlan.md §0 point 3) and is not generalized here.
// en-ru has no v1 course.json to reconstruct, so it instead gets EN-17's own acceptance directly:
// pairs/en-ru/pair.json validates against the loose pair-pack-v1.schema.json, its 16 skill ids
// match lang/en/curriculum.json's exactly, and every opaque copy/pattern key
// lang/en/exercise-recipes.json (EN-14) references resolves in the pair's own
// exerciseCopy/exercisePatterns (checkPairSkillIdsMatchCurriculum/checkRecipeCopyResolves below).
// Finally (EnRuPackPlan.md §6 EN-19/EN-20) it validates both pairs/en-ru/lifehacks.json and
// pairs/pl-ru/lifehacks.json against lifehacks-v1.schema.json, each resolving every skillId
// against its own language's curriculum.json.
//
// Usage: node scripts/validate-pack-v2.mjs
import { existsSync, readdirSync, readFileSync } from 'node:fs';
import { fileURLToPath, pathToFileURL } from 'node:url';
import Ajv2020 from 'ajv/dist/2020.js';
import { validateCoursePack } from './validate-course.mjs';
import { reconstructCoursePack } from './migrate-v1-to-v2.mjs';
import { validateLifehacks } from './validate-lifehacks.mjs';

const root = new URL('../', import.meta.url);
const readJson = (relativePath) => JSON.parse(readFileSync(new URL(relativePath, root), 'utf8'));
const exists = (relativePath) => existsSync(new URL(relativePath, root));
const ajv = new Ajv2020({ allErrors: true, strict: true, strictRequired: false });

function assertSchema(schema, data, prefix) {
  const validate = ajv.compile(schema);
  if (validate(data)) return;
  const first = validate.errors[0];
  const suffix = first.keyword === 'required' ? `/${first.params.missingProperty}` :
    first.keyword === 'additionalProperties' ? `/${first.params.additionalProperty}` : '';
  throw new Error(`${prefix}${first.instancePath}${suffix}: ${first.message}`);
}

/** §8.2: every curriculum skill's `construction` must be a registered core structure. */
function checkConstructionsResolve(langCode, curriculum, constructions) {
  const known = new Set(Object.keys(constructions.constructions));
  curriculum.forEach((skill, index) => {
    if (!known.has(skill.construction)) {
      throw new Error(`/lang/${langCode}/curriculum.json/${index}/construction: "${skill.construction}" is not in core/constructions.json`);
    }
  });
}

/** §8.2: every focus/fixed feature key+value curriculum uses must be declared by core/features.json and by lang.json's usesFeatures. */
function checkFeaturesResolve(langCode, curriculum, features, lang) {
  const uses = new Set(lang.usesFeatures);
  const checkKeyValue = (path, key, value) => {
    const feature = features.features[key];
    if (!feature) throw new Error(`${path}: feature "${key}" is not in core/features.json`);
    if (!uses.has(key)) throw new Error(`${path}: feature "${key}" is not in lang/${langCode}/lang.json's usesFeatures`);
    if (!feature.values.includes(value)) throw new Error(`${path}: value "${value}" is not a declared value of feature "${key}"`);
  };
  curriculum.forEach((skill, index) => {
    const path = `/lang/${langCode}/curriculum.json/${index}`;
    if (skill.focus) {
      checkKeyValue(`${path}/focus`, skill.focus.feature, skill.focus.from);
      checkKeyValue(`${path}/focus`, skill.focus.feature, skill.focus.to);
    }
    for (const [key, value] of Object.entries(skill.fixed ?? {})) checkKeyValue(`${path}/fixed/${key}`, key, value);
  });
}

/** §8.2 "lexicalFilter резолвятся": each `where` clause's values must be real lexicon facts. */
function checkLexicalFiltersResolve(langCode, curriculum, lexicon) {
  const nounIds = new Set(lexicon.nouns.map((noun) => noun.id));
  const genderIds = new Set(lexicon.nouns.map((noun) => noun.gender));
  curriculum.forEach((skill, index) => {
    const where = skill.lexicalFilter?.where;
    if (!where) return;
    const path = `/lang/${langCode}/curriculum.json/${index}/lexicalFilter/where`;
    for (const [key, values] of Object.entries(where)) {
      if (!Array.isArray(values)) throw new Error(`${path}/${key}: expected an array of allowed values`);
      if (key === 'gender') values.forEach((value) => { if (!genderIds.has(value)) throw new Error(`${path}/gender: unknown gender "${value}"`); });
      else if (key === 'nounId') values.forEach((value) => { if (!nounIds.has(value)) throw new Error(`${path}/nounId: unknown noun "${value}"`); });
      else throw new Error(`${path}/${key}: unrecognized lexicalFilter key (only "gender"/"nounId" resolve today)`);
    }
  });
}

/** EnRuPackPlan.md §5 gap A: the `role` prepositions entry must cover exactly lang.json's own `case` (role) values. */
function checkPrepositionsCoverRoles(langCode, lang, prepositions) {
  const role = prepositions.prepositions.find((entry) => entry.id === 'role');
  if (!role) return;
  const declared = new Set(lang.case ?? []);
  const covered = new Set(Object.keys(role.forms));
  for (const value of declared) if (!covered.has(value)) throw new Error(`/lang/${langCode}/prepositions.json/role/forms/${value}: missing — declared in lang.json's "case" but not covered`);
  for (const value of covered) if (!declared.has(value)) throw new Error(`/lang/${langCode}/prepositions.json/role/forms/${value}: not a case value lang/${langCode}/lang.json declares`);
}

/** EnRuPackPlan.md §6 EN-17: `pairs/<id>/pair.json`'s own 16 skill ids must be exactly
 *  `lang/<targetCode>/curriculum.json`'s ids (same set, same order) — the acceptance this task names. */
function checkPairSkillIdsMatchCurriculum(pairId, pair, curriculum) {
  const pairIds = pair.skills.map((skill) => skill.id);
  const curriculumIds = curriculum.map((skill) => skill.id);
  if (JSON.stringify(pairIds) !== JSON.stringify(curriculumIds)) {
    throw new Error(`/pairs/${pairId}/pair.json/skills: ids [${pairIds}] must match lang/curriculum.json's ids [${curriculumIds}] exactly (same set, same order)`);
  }
}

/**
 * Walks a `lang/<code>/exercise-recipes.json` wiring and collects every opaque string it names:
 * a `{"type":"pattern","key":...}` node names an `exercisePatterns` key; a `{"type":"copy"|"fixed","key":...}`
 * node, any `...Key`/`...Keys` field and `ownerDraw.labelKeys`' values each name an `exerciseCopy` key.
 * Generic over the recipe JSON shape (no skill/construction names hardcoded) so it applies to any lang.
 */
function collectRecipeKeys(recipes) {
  const copyKeys = new Set();
  const patternKeys = new Set();
  const walk = (node) => {
    if (Array.isArray(node)) { node.forEach(walk); return; }
    if (!node || typeof node !== 'object') return;
    if (node.type === 'pattern' && typeof node.key === 'string') patternKeys.add(node.key);
    if ((node.type === 'copy' || node.type === 'fixed') && typeof node.key === 'string') copyKeys.add(node.key);
    for (const [key, value] of Object.entries(node)) {
      if (key.endsWith('Key') && typeof value === 'string') copyKeys.add(value);
      else if (key.endsWith('Keys') && Array.isArray(value)) value.forEach((entry) => copyKeys.add(entry));
      else if (key === 'labelKeys' && value && typeof value === 'object') Object.values(value).forEach((entry) => copyKeys.add(entry));
      walk(value);
    }
  };
  walk(recipes);
  return { copyKeys, patternKeys };
}

/** EnRuPackPlan.md §6 EN-17: every opaque copy/pattern key `lang/<code>/exercise-recipes.json` (EN-14)
 *  references must resolve in the pair's own `exerciseCopy`/`exercisePatterns` — the last-mile check that
 *  EN-17's content actually satisfies EN-14's wiring, not just the loose pair-pack-v1 shape. */
function checkRecipeCopyResolves(langCode, pairId, recipes, pair) {
  const { copyKeys, patternKeys } = collectRecipeKeys(recipes);
  for (const key of copyKeys) if (!(key in pair.exerciseCopy)) throw new Error(`/pairs/${pairId}/pair.json/exerciseCopy/${key}: missing — referenced by lang/${langCode}/exercise-recipes.json`);
  for (const key of patternKeys) if (!(key in pair.exercisePatterns)) throw new Error(`/pairs/${pairId}/pair.json/exercisePatterns/${key}: missing — referenced by lang/${langCode}/exercise-recipes.json`);
}

/**
 * Every `courses/lang/<code>/` directory with a lang.json: schema-validate lang.json (always),
 * lexicon.json and prepositions.json (whichever exist), and — only once a curriculum.json exists
 * for that language (EN-12+) — the §8.2 cross-checks above. Sorted for deterministic error order.
 */
function validateLanguages(schemas, features, constructions) {
  const codes = readdirSync(new URL('courses/lang/', root)).filter((code) => exists(`courses/lang/${code}/lang.json`)).sort();
  for (const code of codes) {
    const lang = readJson(`courses/lang/${code}/lang.json`);
    assertSchema(schemas.langPack, lang, `/lang/${code}/lang.json`);

    if (exists(`courses/lang/${code}/lexicon.json`)) {
      const lexicon = readJson(`courses/lang/${code}/lexicon.json`);
      assertSchema(schemas.lexicon, lexicon, `/lang/${code}/lexicon.json`);

      if (exists(`courses/lang/${code}/curriculum.json`)) {
        const curriculum = readJson(`courses/lang/${code}/curriculum.json`);
        checkConstructionsResolve(code, curriculum, constructions);
        checkFeaturesResolve(code, curriculum, features, lang);
        checkLexicalFiltersResolve(code, curriculum, lexicon);
      }
    }

    if (exists(`courses/lang/${code}/prepositions.json`)) {
      const prepositions = readJson(`courses/lang/${code}/prepositions.json`);
      assertSchema(schemas.prepositions, prepositions, `/lang/${code}/prepositions.json`);
      checkPrepositionsCoverRoles(code, lang, prepositions);
    }
  }
}

export function validatePackV2() {
  const features = readJson('courses/core/features.json');
  const constructions = readJson('courses/core/constructions.json');
  const templateOps = readJson('courses/core/template-ops.json');
  const exerciseKinds = readJson('courses/core/exercise-kinds.json');

  assertSchema(readJson('courses/schema/core-features-v1.schema.json'), features, '/core/features.json');
  assertSchema(readJson('courses/schema/core-constructions-v1.schema.json'), constructions, '/core/constructions.json');
  assertSchema(readJson('courses/schema/core-template-ops-v1.schema.json'), templateOps, '/core/template-ops.json');
  assertSchema(readJson('courses/schema/core-exercise-kinds-v1.schema.json'), exerciseKinds, '/core/exercise-kinds.json');

  validateLanguages(
    {
      langPack: readJson('courses/schema/lang-pack-v2.schema.json'),
      lexicon: readJson('courses/schema/lexicon-v1.schema.json'),
      prepositions: readJson('courses/schema/prepositions-v1.schema.json'),
    },
    features,
    constructions,
  );

  // pl-ru's v1 legacy bridge (see this file's header comment for why it stays pl-ru-specific).
  const lexicon = readJson('courses/lang/pl/lexicon.json');
  const pair = readJson('courses/pairs/pl-ru/pair.json');
  const frequency = readJson('courses/pl-ru/frequency-top1000.json');
  assertSchema(readJson('courses/schema/pair-pack-v1.schema.json'), pair, '/pairs/pl-ru/pair.json');
  const reconstructed = reconstructCoursePack(lexicon, pair);
  assertSchema(readJson('courses/schema/course-pack-v1.schema.json'), reconstructed, '/reconstructed-v2-pack');
  validateCoursePack(reconstructed, frequency);

  // pl-ru (EnRuPackPlan.md §6 EN-20): pairs/pl-ru/lifehacks.json against lifehacks-v1.schema.json,
  // with every skillId resolved against lang/pl/curriculum.json's own ids — same schema/checks as
  // en-ru's EN-19 lifehacks below, just a smaller, deliberately incomplete first set (5 of 15 pl
  // skills, see plan §4.4/§7).
  const plCurriculum = readJson('courses/lang/pl/curriculum.json');
  const plRuLifehacks = readJson('courses/pairs/pl-ru/lifehacks.json');
  validateLifehacks('pl-ru', plRuLifehacks, plCurriculum.map((skill) => skill.id));

  // en-ru (EnRuPackPlan.md §6 EN-17): the pair-pack-v1 layer only — en-ru has no v1 course.json to
  // reconstruct against, so this stays the loose pair-pack-v1.schema.json shape check plus the two
  // cross-checks EN-17's own acceptance names, not pl-ru's full v1-bridge reconstruction above.
  const pairSchema = readJson('courses/schema/pair-pack-v1.schema.json');
  const enRuPair = readJson('courses/pairs/en-ru/pair.json');
  const enCurriculum = readJson('courses/lang/en/curriculum.json');
  const enRecipes = readJson('courses/lang/en/exercise-recipes.json');
  assertSchema(pairSchema, enRuPair, '/pairs/en-ru/pair.json');
  checkPairSkillIdsMatchCurriculum('en-ru', enRuPair, enCurriculum);
  checkRecipeCopyResolves('en', 'en-ru', enRecipes, enRuPair);

  // en-ru (EnRuPackPlan.md §6 EN-19): pairs/en-ru/lifehacks.json against lifehacks-v1.schema.json,
  // with every skillId resolved against lang/en/curriculum.json's own ids.
  const enRuLifehacks = readJson('courses/pairs/en-ru/lifehacks.json');
  validateLifehacks('en-ru', enRuLifehacks, enCurriculum.map((skill) => skill.id));

  return true;
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  try {
    validatePackV2();
    process.stdout.write('PASS pack v2\n');
  } catch (error) {
    process.stderr.write(`FAIL pack v2: ${error.message}\n`);
    process.exitCode = 1;
  }
}
