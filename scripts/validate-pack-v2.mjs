// UniversalCorePlan.md §3.1/§8.2/§12 UC-12: validates the schema-v2 layer files (core/*.json,
// every lang/<code>/lang.json + lexicon.json + prepositions.json it finds, pairs/pl-ru/pair.json)
// against their own schemas, then the cross-checks §8.2 names: a skill's `construction` exists in
// core/constructions.json, a feature value used by curriculum.json is declared both in
// core/features.json and in that language's lang.json `usesFeatures`, and curriculum.json's
// `lexicalFilter` references resolve against the actual lexicon. Those three cross-checks only run
// for a language that actually has a curriculum.json yet (EnRuPackPlan.md §6 EN-12) — today that's
// pl only; lang/en (EN-11) gets the lang.json/lexicon.json/prepositions.json schema+cross-checks
// below but not these, honestly, rather than a no-op pretending to check something that doesn't
// exist yet. It also proves pl-ru's lexicon.json + pair.json reconstruct a pack that is itself a
// valid v1 pack (reusing course-pack-v1.schema.json and validateCoursePack — see
// scripts/migrate-v1-to-v2.mjs's own `--check` for the exact-reconstruction proof; this script
// re-validates the *content*, not just the shape, of that reconstruction). That v1 bridge is
// pl-ru-specific by construction (course-pack-v1.schema.json's own case-label vocabulary is pl's 7
// cases, not a generic Case-role table — EnRuPackPlan.md §0 point 3) and is not generalized here;
// a second pair goes through the v2 layers directly once its own loader exists (EN-04/EN-05).
//
// Usage: node scripts/validate-pack-v2.mjs
import { existsSync, readdirSync, readFileSync } from 'node:fs';
import { fileURLToPath, pathToFileURL } from 'node:url';
import Ajv2020 from 'ajv/dist/2020.js';
import { validateCoursePack } from './validate-course.mjs';
import { reconstructCoursePack } from './migrate-v1-to-v2.mjs';

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
