// Development-only baseline check of the old React generator against independent text cases.
// The candidate DSL is neither imported nor used to calculate expectations.
import { strict as assert } from 'node:assert';
import { readFileSync } from 'node:fs';
import { generateChain, sentenceSeeds } from '../src/training/generator';

const caseRows = readFileSync(new URL('../Plans/Biome/NJ03SeedsNounsChains.cases', import.meta.url), 'utf8')
  .split('\n').filter((line) => line.startsWith('CHAIN | ')).map((line) => line.split(' | '));
const seedRows = readFileSync(new URL('../Plans/Biome/NJ03SeedsNounsChains.cases', import.meta.url), 'utf8')
  .split('\n').filter((line) => line.startsWith('SEED | ')).map((line) => line.split(' | '));
const stepRows = readFileSync(new URL('../Plans/Biome/NJ03SeedsNounsChains.cases', import.meta.url), 'utf8')
  .split('\n').filter((line) => line.startsWith('STEP | ')).map((line) => line.split(' | '));
assert.equal(sentenceSeeds.length, 12);
assert.equal(seedRows.length, 12);
assert.equal(stepRows.length, 5);
assert.equal(caseRows.length, 60);

for (const [seedIndex, seed] of sentenceSeeds.entries()) {
  assert.deepEqual([seed.nounId, seed.adjectiveId], seedRows[seedIndex].slice(2), `seed ${seedIndex + 1}`);
  const chain = generateChain(seed);
  assert.equal(chain.length, 5, `seed ${seedIndex + 1}: five steps`);
  for (const [stepIndex, exercise] of chain.entries()) {
    const [, expectedSeed, expectedStep, skill, source, expected] = caseRows[seedIndex * 5 + stepIndex];
    assert.equal(expectedSeed, String(seedIndex + 1).padStart(2, '0'));
    assert.equal(expectedStep, String(stepIndex + 1).padStart(2, '0'));
    assert.equal(exercise.primarySkill, skill, `seed ${seedIndex + 1} step ${stepIndex + 1}: skill`);
    assert.equal(exercise.source, source, `seed ${seedIndex + 1} step ${stepIndex + 1}: source`);
    assert.equal(exercise.expected, expected, `seed ${seedIndex + 1} step ${stepIndex + 1}: expected`);
    assert.deepEqual(exercise.tags, stepRows[stepIndex][7].split(','), `seed ${seedIndex + 1} step ${stepIndex + 1}: tags`);
    assert.equal(exercise.possessive, stepRows[stepIndex][8], `seed ${seedIndex + 1} step ${stepIndex + 1}: owner`);
    if (stepIndex > 0) assert.equal(exercise.source, chain[stepIndex - 1].expected, `seed ${seedIndex + 1} step ${stepIndex + 1}: linked`);
  }
}

process.stdout.write('PASS old React chain baseline: 12/12 seeds, 60/60 source/expected/skill/tag/owner cases, 48/48 links; candidate and hosts NOT RUN\n');
