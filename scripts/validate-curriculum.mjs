import { readFileSync } from 'node:fs';
import { fileURLToPath, pathToFileURL } from 'node:url';
import Ajv2020 from 'ajv/dist/2020.js';

const root = new URL('../', import.meta.url);
const readJson = (relativePath) => JSON.parse(readFileSync(new URL(relativePath, root), 'utf8'));
const ajv = new Ajv2020({ allErrors: true, strict: true });
const curriculumSchema = ajv.compile(readJson('courses/schema/curriculum-v1.schema.json'));

function assertSchema(validate, data, prefix) {
  if (validate(data)) return;
  const first = validate.errors[0];
  const suffix = first.keyword === 'required' ? `/${first.params.missingProperty}` :
    first.keyword === 'additionalProperties' ? `/${first.params.additionalProperty}` : '';
  throw new Error(`${prefix}${first.instancePath}${suffix}: ${first.message}`);
}

/** Same cycle-detection shape as `validate-course.mjs`'s `validatePrerequisites`, kept local
 *  since curriculum.json and course.json are validated independently (UC-06 stays scoped to the
 *  new lang/pl/curriculum.json layer, not a shared-code refactor of the v1 validator). */
function validatePrerequisiteDag(skills) {
  const byId = new Map(skills.map((skill, index) => [skill.id, { skill, index }]));
  const visiting = new Set();
  const visited = new Set();
  function visit(id) {
    if (visiting.has(id)) throw new Error(`/${byId.get(id).index}/prerequisites: prerequisite cycle at ${id}`);
    if (visited.has(id)) return;
    visiting.add(id);
    const { skill, index } = byId.get(id);
    skill.prerequisites.forEach((prerequisite, position) => {
      if (!byId.has(prerequisite)) {
        throw new Error(`/${index}/prerequisites/${position}: unknown skill ${prerequisite}`);
      }
      visit(prerequisite);
    });
    visiting.delete(id);
    visited.add(id);
  }
  skills.forEach((skill) => visit(skill.id));
}

/** Rejects malformed curriculum data: schema, duplicate ids, dangling/cyclic prerequisites, and
 *  a `focus.from`/`focus.to` that collapse to the same value (a no-op flip, UC-06). */
export function validateCurriculum(skills, prefix = 'curriculum') {
  assertSchema(curriculumSchema, skills, prefix);
  const seen = new Set();
  skills.forEach((skill, index) => {
    if (seen.has(skill.id)) throw new Error(`/${prefix}/${index}/id: duplicate ${skill.id}`);
    seen.add(skill.id);
    if (skill.focus && skill.focus.from === skill.focus.to) {
      throw new Error(`/${prefix}/${index}/focus: from and to must differ`);
    }
  });
  validatePrerequisiteDag(skills);
  return true;
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  try {
    const path = process.argv[2] ?? fileURLToPath(new URL('../courses/lang/pl/curriculum.json', import.meta.url));
    validateCurriculum(JSON.parse(readFileSync(path, 'utf8')));
    process.stdout.write(`PASS curriculum: ${path}\n`);
  } catch (error) {
    process.stderr.write(`FAIL curriculum: ${error.message}\n`);
    process.exitCode = 1;
  }
}
