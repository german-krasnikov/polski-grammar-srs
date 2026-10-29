// Derive: the Kotlin text of the swipe Policy, printed by the existing `biome logic generate --target kt`.
// It returns the text and writes nothing: `biome generate` writes OUT (generate) or compares it with the file (--check).
// A derive that wrote OUT itself would compare the file with itself, and a stale consumer would read fresh (P16).
import { spawnSync } from 'node:child_process';
import { mkdtempSync, readFileSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..', '..', '..');

/** The runtime consumer: SwipeDecision calls Policy.rating from this file. */
export const OUT = join(root, 'kotlin/shared/src/commonMain/kotlin/polski/interaction/SwipePolicy.kt');

/**
 * `context` is what `biome generate` passes: the running CLI, node and env, so the toolchain that pins and compares is the one that prints.
 * The target prints into a temp directory that is removed before the text is returned; without --out no .mjs, .d.mts or inputs JSON exists.
 */
export async function generate({ cli, node = process.execPath, env = process.env } = {}) {
  if (!cli) throw new Error('derive-swipe-policy: no context.cli; run it with biome generate');
  const dir = mkdtempSync(join(tmpdir(), 'swipe-policy-'));
  try {
    const run = spawnSync(node, [cli, 'logic', 'generate',
      '--source', join(root, 'dsl/swipe/swipe.logic'),
      '--contract', join(root, 'dsl/swipe/swipe.contract'),
      '--target', 'kt SwipePolicy.kt AS polski.interaction',
    ], { cwd: dir, env, encoding: 'utf8', timeout: 120_000 });
    if (run.status !== 0) throw new Error(`biome logic generate failed (${run.status ?? run.error?.message}): ${run.stderr?.trim()}`);
    if (run.stderr) process.stderr.write(run.stderr);
    return readFileSync(join(dir, 'SwipePolicy.kt'), 'utf8');
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
}
