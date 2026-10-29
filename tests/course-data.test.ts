// R1.2 (Plans/Biome/R1Design.md): the courses-biome files are Core `.core-data`, readable by `biome read`, not a private dialect.
// They need a biome-dsl checkout: BIOME_BIN=<checkout>/implementations/typescript/packages/biome/src/bin.mjs, else the sibling ../../Python/biome-dsl.
// Without one they are skipped, which is NOT RUN, not a pass.
import { describe, expect, it } from 'vitest';
import { spawnSync } from 'node:child_process';
import { existsSync, readdirSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const repo = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const cli = process.env.BIOME_BIN ?? resolve(repo, '../../Python/biome-dsl/implementations/typescript/packages/biome/src/bin.mjs');
const PACK = join(repo, 'courses-biome/pl-ru');

/** Rows per file, from the inventory of R1Design.md section 1 (nouns 14, adjectives 7, verbs 11, skills 16, possessives 7, seeds 12; the other sections are one row). */
const ROWS: Record<string, number> = {
  adjectives: 7, caseSentencePrefixes: 1, exerciseCopy: 1, exercisePatterns: 1, morphology: 1, nouns: 14, personalPronouns: 1, possessives: 7,
  reference: 1, sentenceSeeds: 12, skills: 16, stemAlternations: 1, training: 1, verbs: 11, vocabulary: 1,
};

describe.skipIf(!existsSync(cli))('courses-biome/pl-ru is Core data', () => {
  it('holds exactly the 15 known sections', () => {
    expect(readdirSync(PACK).filter(name => name.endsWith('.core-data')).map(name => name.replace('.core-data', '')).sort()).toEqual(Object.keys(ROWS).sort());
  });

  for (const [section, rows] of Object.entries(ROWS)) {
    it(`biome read ${section}.core-data reads ${rows} row(s)`, () => {
      const run = spawnSync(process.execPath, [cli, 'read', join(PACK, `${section}.core-data`), '--format', 'raw'], { encoding: 'utf8' });
      expect(run.stderr, 'biome read diagnostics').toBe('');
      expect(run.status).toBe(0);
      expect(run.stdout.trimEnd().split('\n')).toHaveLength(rows);
    });
  }
});
