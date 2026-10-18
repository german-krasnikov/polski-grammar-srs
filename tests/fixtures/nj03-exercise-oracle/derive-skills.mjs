// Derive wrapper for B4: generates GeneratedCourseValueSource.kt from skills.core-data.
import { execFileSync } from 'node:child_process';
import { join, resolve } from 'node:path';
import { readFileSync } from 'node:fs';

const here = import.meta.dirname;
const root = resolve(here, '..', '..', '..');

export const OUT = join(
  root,
  'kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedCourseValueSource.kt',
);

export async function generate() {
  execFileSync(
    'python3',
    [join(root, 'scripts/nj03/generate-course-value-source.py')],
    { cwd: root, stdio: 'inherit' },
  );
  return readFileSync(OUT, 'utf8');
}
