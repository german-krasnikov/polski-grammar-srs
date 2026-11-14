// Checks every non-PARTIAL KEY of dsl/ui-copy.declare (CATALOG UiCopy, dsl/constructs/ui-copy.construct) against
// both packs' uiCopy.core-data: a non-PARTIAL key must exist in every pack, a PARTIAL key in at least one. Throws
// with the offending key and pack(s) on the first gap, same failure shape as the retired ui-copy-accessors.mjs:
// `biome generate` fails before a stale accessor ships. A GENERATE `script` step (biome.project), run after the
// `ui-copy` render step so a broken declare file is reported by `render` first.
import { spawnSync } from 'node:child_process';
import { pathToFileURL } from 'node:url';
import { readFileSync } from 'node:fs';
import { join, resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const DECLARE = join(root, 'dsl/ui-copy.declare');
const PACKS = { 'pl-ru': join(root, 'courses-biome/pl-ru/uiCopy.core-data'), 'en-ru': join(root, 'courses-biome/en-ru/uiCopy.core-data') };

function biomeRead(cli, file) {
  const run = spawnSync(process.execPath, [cli, 'read', file, '--format', 'json'], { cwd: root, encoding: 'utf8', timeout: 120_000 });
  if (run.status !== 0) throw new Error(`biome read ${file} failed (${run.status ?? run.error?.message}): ${run.stderr?.trim()}`);
  return JSON.parse(run.stdout);
}

async function main() {
  const cli = process.env.BIOME_CLI;
  if (!cli) throw new Error('ui-copy-coverage: BIOME_CLI is required; run through biome generate');
  const { readSurface } = await import(`${pathToFileURL(cli)}?lib`);

  const text = readFileSync(DECLARE, 'utf8');
  const { uses, diagnostics } = readSurface('ui-copy', text, DECLARE, { dirs: [join(root, 'dsl/constructs')] });
  if (diagnostics.length) throw new Error(diagnostics.map((d) => `${d.file}:${d.line}:${d.col}: ${d.code}: ${d.message}`).join('\n'));

  const keysByPack = Object.fromEntries(Object.entries(PACKS).map(([pair, file]) =>
    [pair, new Set(biomeRead(cli, file).map((row) => row.value.key))]));

  for (const use of uses) {
    for (const item of use.items) {
      if (!item.has('key')) continue; // an FN item: nothing of a pack to check
      const key = item.get('key').raw, partial = item.has('partial');
      const missing = Object.entries(keysByPack).filter(([, keys]) => !keys.has(key)).map(([pair]) => pair);
      if (partial ? missing.length === 2 : missing.length > 0) {
        throw new Error(`${DECLARE}: "${key}" is missing from ${missing.join(', ')}`);
      }
    }
  }
}

await main();
