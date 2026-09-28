// UniversalCorePlan.md §3.1/§8.2/§12 UC-12: validates the schema-v2 layer files (core/*.json,
// lang/pl/lang.json, pairs/pl-ru/pair.json) against their own schemas, then the cross-checks §8.2
// names: a skill's `construction` exists in core/constructions.json, a feature value used by
// curriculum.json is declared both in core/features.json and in lang.json's `usesFeatures`, and
// curriculum.json's `lexicalFilter` references resolve against the actual lexicon. It also proves
// lexicon.json + pair.json reconstruct a pack that is itself a valid v1 pack (reusing
// course-pack-v1.schema.json and validateCoursePack — see scripts/migrate-v1-to-v2.mjs's own
// `--check` for the exact-reconstruction proof; this script re-validates the *content*, not just
// the shape, of that reconstruction).
//
// Usage: node scripts/validate-pack-v2.mjs
import { readFileSync } from 'node:fs';
import { fileURLToPath, pathToFileURL } from 'node:url';
import Ajv2020 from 'ajv/dist/2020.js';
import { validateCoursePack } from './validate-course.mjs';
import { reconstructCoursePack } from './migrate-v1-to-v2.mjs';

const root = new URL('../', import.meta.url);
const readJson = (relativePath) => JSON.parse(readFileSync(new URL(relativePath, root), 'utf8'));
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
function checkConstructionsResolve(curriculum, constructions) {
  const known = new Set(Object.keys(constructions.constructions));
  curriculum.forEach((skill, index) => {
    if (!known.has(skill.construction)) {
      throw new Error(`/lang/pl/curriculum.json/${index}/construction: "${skill.construction}" is not in core/constructions.json`);
    }
  });
}

/** §8.2: every focus/fixed feature key+value curriculum uses must be declared by core/features.json and by lang.json's usesFeatures. */
function checkFeaturesResolve(curriculum, features, lang) {
  const uses = new Set(lang.usesFeatures);
  const checkKeyValue = (path, key, value) => {
    const feature = features.features[key];
    if (!feature) throw new Error(`${path}: feature "${key}" is not in core/features.json`);
    if (!uses.has(key)) throw new Error(`${path}: feature "${key}" is not in lang/pl/lang.json's usesFeatures`);
    if (!feature.values.includes(value)) throw new Error(`${path}: value "${value}" is not a declared value of feature "${key}"`);
  };
  curriculum.forEach((skill, index) => {
    const path = `/lang/pl/curriculum.json/${index}`;
    if (skill.focus) {
      checkKeyValue(`${path}/focus`, skill.focus.feature, skill.focus.from);
      checkKeyValue(`${path}/focus`, skill.focus.feature, skill.focus.to);
    }
    for (const [key, value] of Object.entries(skill.fixed ?? {})) checkKeyValue(`${path}/fixed/${key}`, key, value);
  });
}

/** §8.2 "lexicalFilter резолвятся": each `where` clause's values must be real lexicon facts. */
function checkLexicalFiltersResolve(curriculum, lexicon) {
  const nounIds = new Set(lexicon.nouns.map((noun) => noun.id));
  const genderIds = new Set(lexicon.nouns.map((noun) => noun.gender));
  curriculum.forEach((skill, index) => {
    const where = skill.lexicalFilter?.where;
    if (!where) return;
    const path = `/lang/pl/curriculum.json/${index}/lexicalFilter/where`;
    for (const [key, values] of Object.entries(where)) {
      if (!Array.isArray(values)) throw new Error(`${path}/${key}: expected an array of allowed values`);
      if (key === 'gender') values.forEach((value) => { if (!genderIds.has(value)) throw new Error(`${path}/gender: unknown gender "${value}"`); });
      else if (key === 'nounId') values.forEach((value) => { if (!nounIds.has(value)) throw new Error(`${path}/nounId: unknown noun "${value}"`); });
      else throw new Error(`${path}/${key}: unrecognized lexicalFilter key (only "gender"/"nounId" resolve today)`);
    }
  });
}

export function validatePackV2() {
  const features = readJson('courses/core/features.json');
  const constructions = readJson('courses/core/constructions.json');
  const templateOps = readJson('courses/core/template-ops.json');
  const exerciseKinds = readJson('courses/core/exercise-kinds.json');
  const lang = readJson('courses/lang/pl/lang.json');
  const lexicon = readJson('courses/lang/pl/lexicon.json');
  const pair = readJson('courses/pairs/pl-ru/pair.json');
  const curriculum = readJson('courses/lang/pl/curriculum.json');
  const frequency = readJson('courses/pl-ru/frequency-top1000.json');

  assertSchema(readJson('courses/schema/core-features-v1.schema.json'), features, '/core/features.json');
  assertSchema(readJson('courses/schema/core-constructions-v1.schema.json'), constructions, '/core/constructions.json');
  assertSchema(readJson('courses/schema/core-template-ops-v1.schema.json'), templateOps, '/core/template-ops.json');
  assertSchema(readJson('courses/schema/core-exercise-kinds-v1.schema.json'), exerciseKinds, '/core/exercise-kinds.json');
  assertSchema(readJson('courses/schema/lang-pack-v2.schema.json'), lang, '/lang/pl/lang.json');
  assertSchema(readJson('courses/schema/pair-pack-v1.schema.json'), pair, '/pairs/pl-ru/pair.json');

  checkConstructionsResolve(curriculum, constructions);
  checkFeaturesResolve(curriculum, features, lang);
  checkLexicalFiltersResolve(curriculum, lexicon);

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
