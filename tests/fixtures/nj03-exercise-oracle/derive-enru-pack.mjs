// Derive wrapper: generates GeneratedEnRuPack.kt
import { execFileSync } from 'node:child_process';
import { join, resolve } from 'node:path';
import { readFileSync } from 'node:fs';

const root = resolve(import.meta.dirname, '..', '..', '..');

export const OUT = join(
  root,
  'kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedEnRuPack.kt',
);

export async function generate() {
  execFileSync('python3', [join(root, 'scripts/nj03/generate-enru-pack.py')],
    { cwd: root, stdio: 'inherit' });
  return readFileSync(OUT, 'utf8');
}
