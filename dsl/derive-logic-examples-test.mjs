// Generic: convert a logic *-examples.json (facts + expected per decision) into a self-contained
// Kotlin commonTest that calls the generated Policy object and asserts each expected outcome.
// Expectations come from .examples (never from the generated policy) — P15.
// Add a new export pair (OUT_<NAME> + function) to enable another policy; only error-hunt is wired now.
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const sharedTestGenerated = resolve(root, 'kotlin/shared/src/commonTest/generated/kotlin');

/** Kotlin literal for a JSON fact value: boolean → true/false, number → Double literal, string → quoted. */
function lit(v) {
  if (typeof v === 'boolean') return String(v);
  if (typeof v === 'number') return `${v}.0`;
  return JSON.stringify(v);
}

/** "tapOutcome" → "TapOutcome" */
function pascal(name) { return name[0].toUpperCase() + name.slice(1); }

/**
 * Generate a complete Kotlin test file from a logic examples JSON.
 * @param {string} sourceName   e.g. "error-hunt"  (used in the banner comment)
 * @param {string} jsonPath     absolute path to the *-examples.json
 * @param {string} pkg          Kotlin package name (same as the generated Policy's package)
 * @param {string} className    Kotlin test class name
 */
function generateTest(sourceName, jsonPath, pkg, className) {
  const examples = JSON.parse(readFileSync(jsonPath, 'utf8'));
  const decisions = Object.entries(examples).filter(([k]) => !['generated', 'provenance'].includes(k));
  const totalCases = decisions.reduce((n, [, rows]) => n + rows.length, 0);

  const methods = decisions.map(([decision, rows]) => {
    const inputType = `Policy.${pascal(decision)}Input`;
    const assertions = rows.map(({ id, facts, expected }) => {
      const args = Object.entries(facts).map(([k, v]) => `${k} = ${lit(v)}`).join(', ');
      return `        assertEquals(${JSON.stringify(expected)}, Policy.${decision}(${inputType}(${args})), ${JSON.stringify(id)})`;
    }).join('\n');
    return `    @Test fun ${decision}() {\n${assertions}\n    }`;
  }).join('\n\n');

  const body = `// Generated from dsl/learning/${sourceName}.examples by biome generate. Do not edit.
package ${pkg}

import kotlin.test.Test
import kotlin.test.assertEquals

/** OK: ${totalCases} cases */
class ${className} {
${methods}
}
`;
  return body;
}

export const OUT_ERRORHUNT = resolve(sharedTestGenerated, 'polski/cloze/policy/ErrorHuntPolicyExamplesTest.kt');

export function errorHunt() {
  return generateTest(
    'error-hunt',
    resolve(import.meta.dirname, 'learning/generated/error-hunt-examples.json'),
    'polski.cloze.policy',
    'ErrorHuntPolicyExamplesTest',
  );
}
