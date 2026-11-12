// Print commonTest fixtures from the single authored Logic examples; biome generate owns the output.
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

export const OUT = resolve(import.meta.dirname, '../../kotlin/shared/src/commonTest/generated/kotlin/polski/exam/policy/AnswerPolicyCases.kt');

export function kotlin() {
  const examples = JSON.parse(readFileSync(resolve(import.meta.dirname, 'generated/answer-policy-examples.json'), 'utf8'));
  const rows = examples.disposition.map(row => {
    const facts = row.facts;
    return `    AnswerPolicyCase(${JSON.stringify(row.id)}, ${facts.matchesCanonical}, ${facts.variantQuarter}, ${facts.maximumQuarter}, ${facts.answerNonblank}, ${facts.openManual}, ${JSON.stringify(row.expected)}),`;
  });
  return `// Generated from dsl/exam/answer-policy.examples by biome generate. Do not edit.\npackage polski.exam.policy\n\ndata class AnswerPolicyCase(\n    val id: String,\n    val matchesCanonical: Boolean,\n    val variantQuarter: Int,\n    val maximumQuarter: Int,\n    val answerNonblank: Boolean,\n    val openManual: Boolean,\n    val expectedDisposition: String,\n)\n\nval ANSWER_POLICY_CASES = listOf(\n${rows.join('\n')}\n)\n`;
}
