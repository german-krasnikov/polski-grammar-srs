// Derive wrapper: calls the Python exercise oracle generator (en characterization, 92 cases (R1.1)).
// biome generate derive: exports OUT path and generate() → string for freshness tracking (P16).
import { execFileSync } from 'node:child_process';
import { join, resolve } from 'node:path';
import { readFileSync } from 'node:fs';

const root = resolve(import.meta.dirname, '..', '..', '..');
const oracle = 'tests/fixtures/nj03-exercise-oracle/requirements-en.core-data';
const target = 'kotlin/shared/src/commonTest/kotlin/polski/training/TrainingParityEnTest.kt';

export const OUT = join(root, target);

export async function generate() {
  execFileSync('python3', [join(root, 'scripts/nj03/generate-exercise-oracle-test.py'), oracle, target, '92'],
    { cwd: root, stdio: 'inherit' });
  return readFileSync(OUT, 'utf8');
}
