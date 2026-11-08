// Derive: the Kotlin scheduler-oracle test, printed by kotlin/shared/src/commonTest/kotlin/polski/srs/generate_parity.py --print from the pinned ts-fsrs 5.4.2 fixture
// (tests/fixtures/kotlin-parity/scheduler.json). The Python script is the one source of the rule: run without --print it writes the same text to the checked-in Kotlin test, so this wrapper lets
// `biome generate --check` catch a drift between the fixture and that file instead of relying on someone to rerun the script by hand.
// It returns the text and writes nothing: `biome generate` writes OUT (generate) or compares it with the file (--check).
import { spawnSync } from 'node:child_process';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const dir = resolve(root, 'kotlin/shared/src/commonTest/kotlin/polski/srs');
export const OUT = resolve(dir, 'SchedulerOracleTest.kt');
const SCRIPT = resolve(dir, 'generate_parity.py');

/** The step `scheduler-oracle`: `generateArgs[0]` is tests/fixtures/kotlin-parity/scheduler.json, named in the step only so biome reruns it when that fixture changes; the script reads the fixture itself. */
export function srs() {
  const run = spawnSync('python3', [SCRIPT, '--print'], { cwd: root, encoding: 'utf8' });
  if (run.status !== 0) throw new Error(`derive-scheduler-oracle: generate_parity.py --print failed (${run.status ?? run.error?.message}): ${run.stderr?.trim()}`);
  return run.stdout;
}
