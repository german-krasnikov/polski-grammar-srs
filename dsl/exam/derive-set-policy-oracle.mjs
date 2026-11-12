// Print typed commonTest cases from authored Logic examples; do not author Kotlin expectations twice.
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

export const OUT = resolve(import.meta.dirname, '../../kotlin/shared/src/commonTest/generated/kotlin/polski/exam/setpolicy/SetPolicyCases.kt');

const fields = [
  ['selectionCount', 'Int'], ['requiredSelections', 'Int'], ['distinctKnownFactCount', 'Int'],
  ['knownCreditQuarter', 'Int'], ['maximumQuarter', 'Int'], ['unknownNonblankCount', 'Int'],
  ['hasMalformedSelection', 'Boolean'], ['answerNonblank', 'Boolean'], ['openManual', 'Boolean'],
  ['usesSourceConservativeOverselection', 'Boolean'], ['hasPerFactCredit', 'Boolean'],
];

export function kotlin() {
  const rows = JSON.parse(readFileSync(resolve(import.meta.dirname, 'generated/set-policy-examples.json'), 'utf8')).disposition;
  const declarations = fields.map(([name, type]) => `    val ${name}: ${type},`).join('\n');
  const cases = rows.map(row => `    SetPolicyCase(${JSON.stringify(row.id)}, ${fields.map(([name]) => row.facts[name]).join(', ')}, ${JSON.stringify(row.expected)}),`).join('\n');
  return `// Generated from dsl/exam/set-policy.examples by biome generate. Do not edit.\npackage polski.exam.setpolicy\n\ndata class SetPolicyCase(\n    val id: String,\n${declarations}\n    val expectedDisposition: String,\n)\n\nval SET_POLICY_CASES = listOf(\n${cases}\n)\n`;
}
