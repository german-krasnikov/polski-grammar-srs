// Convert authored Logic examples into typed commonTest rows. Expected values stay in .examples.
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const directory = import.meta.dirname;
const testRoot = resolve(directory, '../../kotlin/shared/src/commonTest/generated/kotlin/polski/exam');

function cases(name) {
  return JSON.parse(readFileSync(resolve(directory, `generated/${name}-examples.json`), 'utf8')).disposition;
}

function render(name, packageName, typeName, fields, mapRow) {
  const rows = cases(name).map(row => `    ${typeName}(${JSON.stringify(row.id)}, ${mapRow(row.facts)}, ${JSON.stringify(row.expected)}),`);
  return `// Generated from dsl/exam/${name}.examples by biome generate. Do not edit.\npackage ${packageName}\n\ndata class ${typeName}(\n    val id: String,\n${fields.map(([field, type]) => `    val ${field}: ${type},`).join('\n')}\n    val expectedDisposition: String,\n)\n\nval ${name.replaceAll('-', '_').toUpperCase()}_CASES = listOf(\n${rows.join('\n')}\n)\n`;
}

export const OUT_RESULT = resolve(testRoot, 'resultpolicy/ResultPolicyCases.kt');
export const OUT_PART = resolve(testRoot, 'partactionpolicy/PartActionPolicyCases.kt');
export const OUT_SESSION = resolve(testRoot, 'sessionpolicy/SessionPolicyCases.kt');

export function result() {
  return render('result-policy', 'polski.exam.resultpolicy', 'ResultPolicyCase', [
    ['resultReady', 'Boolean'], ['scope', 'String'], ['sittingCompleted', 'Boolean'],
    ['manualPendingQuarter', 'Int'], ['possibleQuarter', 'Int'],
  ], facts => [facts.resultReady, JSON.stringify(facts.scope), facts.sittingCompleted, facts.manualPendingQuarter, facts.possibleQuarter].join(', '));
}

export function part() {
  return render('part-action-policy', 'polski.exam.partactionpolicy', 'PartActionPolicyCase', [
    ['kind', 'String'], ['action', 'String'], ['submitted', 'Boolean'], ['validTarget', 'Boolean'],
    ['validSelectedSet', 'Boolean'], ['targetInSelectedSet', 'Boolean'], ['atFirst', 'Boolean'], ['atLast', 'Boolean'],
  ], facts => [JSON.stringify(facts.kind), JSON.stringify(facts.action), facts.submitted, facts.validTarget, facts.validSelectedSet, facts.targetInSelectedSet, facts.atFirst, facts.atLast].join(', '));
}

export function session() {
  return render('session-policy', 'polski.exam.sessionpolicy', 'SessionPolicyCase', [
    ['event', 'String'], ['completed', 'Boolean'], ['started', 'Boolean'],
    ['moduleMatchesCurrent', 'Boolean'], ['deadlineReached', 'Boolean'],
  ], facts => [JSON.stringify(facts.event), facts.completed, facts.started, facts.moduleMatchesCurrent, facts.deadlineReached].join(', '));
}
