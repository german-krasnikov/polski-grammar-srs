// Derive: the Kotlin progress-oracle test, printed by kotlin/shared/src/commonTest/kotlin/polski/progress/generate_parity.py --print from the pinned React fixture
// (tests/fixtures/kotlin-parity/progress.json). The Python script is the one source of the rule: run without --print it writes the same text to the checked-in Kotlin test, so this wrapper lets
// `biome generate --check` catch a drift between the fixture and that file instead of relying on someone to rerun the script by hand.
// It returns the text and writes nothing: `biome generate` writes OUT (generate) or compares it with the file (--check).
import { spawnSync } from 'node:child_process';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const dir = resolve(root, 'kotlin/shared/src/commonTest/kotlin/polski/progress');
export const OUT = resolve(dir, 'ProgressFixtureParityTest.kt');
const SCRIPT = resolve(dir, 'generate_parity.py');

/** The step `progress-oracle`: `generateArgs[0]` is tests/fixtures/kotlin-parity/progress.json, named in the step only so biome reruns it when that fixture changes; the script reads the fixture itself. */
export function progress() {
  const run = spawnSync('python3', [SCRIPT, '--print'], { cwd: root, encoding: 'utf8' });
  if (run.status !== 0) throw new Error(`derive-progress-oracle: generate_parity.py --print failed (${run.status ?? run.error?.message}): ${run.stderr?.trim()}`);
  return run.stdout;
}
