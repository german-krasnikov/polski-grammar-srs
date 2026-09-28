// EnRuPackPlan.md §4/§6 EN-19: validates a pair's lifehacks.json (pair-scoped L1-transfer tips,
// outside core per ADR-15) against lifehacks-v1.schema.json, plus the cross-checks that schema
// alone cannot express: `pairId` matches the file's own directory, every `id` is globally unique
// and prefixed with that pairId, a non-null `skillId` resolves against the target language's own
// curriculum.json (so a lifehack can never point at a skill that doesn't exist), a null `skillId`
// carries a non-empty `topic` (the schema's own required-but-nullable pairing), and every
// `source.citation` is non-empty prose, not an obvious placeholder (ADR-15's "no invented
// citations" is a linguist-review call, not fully automatable — this only catches the
// mechanically detectable half: empty or placeholder-only text).
//
// Usage: node scripts/validate-lifehacks.mjs <pairId>
import { readFileSync } from 'node:fs';
import { pathToFileURL } from 'node:url';
import Ajv2020 from 'ajv/dist/2020.js';

const root = new URL('../', import.meta.url);
const readJson = (relativePath) => JSON.parse(readFileSync(new URL(relativePath, root), 'utf8'));
const schema = readJson('courses/schema/lifehacks-v1.schema.json');
const validateSchema = new Ajv2020({ allErrors: true, strict: true }).compile(schema);

const PLACEHOLDER = /^(tbd|todo|n\/a|unknown|placeholder|tba|\?+)$/i;

/**
 * `curriculumIds` is the target language's `lang/<code>/curriculum.json` ids (nullable — pass
 * null/undefined to skip the skillId cross-check, e.g. before that curriculum exists yet).
 */
export function validateLifehacks(pairId, lifehacks, curriculumIds) {
  if (!validateSchema(lifehacks)) {
    const error = validateSchema.errors[0];
    const suffix = error.keyword === 'required' ? `/${error.params.missingProperty}` : '';
    throw new Error(`lifehacks${error.instancePath}${suffix}: ${error.message}`);
  }
  if (lifehacks.pairId !== pairId) throw new Error(`lifehacks/pairId: expected "${pairId}", got "${lifehacks.pairId}"`);

  const knownSkillIds = curriculumIds ? new Set(curriculumIds) : null;
  const seenIds = new Set();
  lifehacks.lifehacks.forEach((lifehack, index) => {
    const path = `lifehacks/lifehacks/${index}`;
    if (!lifehack.id.startsWith(`${pairId}.`)) throw new Error(`${path}/id: "${lifehack.id}" must be prefixed with "${pairId}."`);
    if (seenIds.has(lifehack.id)) throw new Error(`${path}/id: duplicate id ${lifehack.id}`);
    seenIds.add(lifehack.id);

    if (lifehack.skillId === null) {
      if (!lifehack.topic?.trim()) throw new Error(`${path}/topic: cross-skill lifehack (skillId=null) needs a non-empty topic`);
    } else {
      if (lifehack.topic !== null) throw new Error(`${path}/topic: must be null when skillId is set`);
      if (knownSkillIds && !knownSkillIds.has(lifehack.skillId)) {
        throw new Error(`${path}/skillId: "${lifehack.skillId}" is not a known curriculum skill`);
      }
    }

    const citation = lifehack.source.citation.trim();
    if (!citation || PLACEHOLDER.test(citation)) throw new Error(`${path}/source/citation: empty or placeholder citation`);
  });
  return true;
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  const pairId = process.argv[2] ?? 'en-ru';
  try {
    const curriculum = readJson(`courses/lang/${pairId.split('-')[0]}/curriculum.json`);
    validateLifehacks(pairId, readJson(`courses/pairs/${pairId}/lifehacks.json`), curriculum.map((skill) => skill.id));
    process.stdout.write(`PASS lifehacks ${pairId}\n`);
  } catch (error) {
    process.stderr.write(`FAIL lifehacks ${pairId}: ${error.message}\n`);
    process.exitCode = 1;
  }
}
