// Derive wrapper: calls the Python exercise oracle generator.
// biome generate derive: exports OUT path and generate() → string for freshness tracking (P16).
import { execFileSync } from 'node:child_process';
import { join, resolve } from 'node:path';
import { readFileSync } from 'node:fs';

const here = import.meta.dirname;
const root = resolve(here, '..', '..', '..');

export const OUT = join(
  root,
  'kotlin/shared/src/commonTest/kotlin/polski/training/TrainingParityTest.kt',
);

export async function generate() {
  execFileSync(
    'python3',
    [join(root, 'scripts/nj03/generate-exercise-oracle-test.py')],
    { cwd: root, stdio: 'inherit' },
  );
  return readFileSync(OUT, 'utf8');
}
