// Derive wrapper: generates SwipePolicy.kt via biome logic generate
import { execFileSync } from 'node:child_process';
import { join, resolve } from 'node:path';
import { readFileSync } from 'node:fs';

const root = resolve(import.meta.dirname, '..', '..', '..');
const biome = process.env.BIOME_BIN || join(root, '..', '..', 'Python', 'biome-dsl', 'implementations', 'typescript', 'packages', 'biome', 'src', 'bin.mjs');

export const OUT = join(root, 'dsl/swipe/out/SwipePolicy.kt');

export async function generate() {
  execFileSync('node', [biome, 'logic', 'generate',
    '--source', join(root, 'dsl/swipe/swipe.logic'),
    '--contract', join(root, 'dsl/swipe/swipe.contract'),
    '--out', join(root, 'dsl/swipe/out'),
    '--target', `kt ${OUT} AS polski.interaction`,
  ], { cwd: root, stdio: 'inherit' });
  return readFileSync(OUT, 'utf8');
}
