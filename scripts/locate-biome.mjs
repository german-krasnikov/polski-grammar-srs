#!/usr/bin/env node
// The biome-dsl toolchain (its implementations/typescript), with no absolute default, in this order: $BIOME_TS | $BIOME_TOOLCHAIN | $BIOME_DSL (two set to
// different paths is an error), else the checkout of the script node runs (process.argv[1]: the biome CLI when a GENERATE derive module imports this), else the
// working directory, else a biome-dsl beside an ancestor of this file (<ancestor>/biome-dsl or <ancestor>/<dir>/biome-dsl). A GENERATE script step is a
// separate node process, so it gets this order too (BIOME_TS when started through `node locate-biome.mjs generate ...`).
//   node locate-biome.mjs                 prints the path (shell scripts: TS="${BIOME_TS:-$(node locate-biome.mjs)}"); exit 1 when none is found
//   node locate-biome.mjs <biome args>    runs `biome <args>` from that checkout, with BIOME_TS set so its script steps find the same one
import { spawnSync } from 'node:child_process';
import { existsSync, readdirSync, realpathSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const TS = 'implementations/typescript', BIN = 'packages/biome/src/bin.mjs', isToolchain = d => existsSync(join(d, BIN));
const ancestors = d => { const all = [resolve(d)]; while (dirname(all.at(-1)) !== all.at(-1)) all.push(dirname(all.at(-1))); return all; };
const subdirs = d => { try { return readdirSync(d, { withFileTypes: true }).filter(e => e.isDirectory()).map(e => join(d, e.name)); } catch { return []; } };
const real = p => (existsSync(p) ? realpathSync(p) : resolve(p));
const near = d => ancestors(d).flatMap(a => [a, join(a, TS), ...[a, ...subdirs(a)].map(s => join(s, 'biome-dsl', TS))]);

export function locateBiome() {
  const set = ['BIOME_TS', 'BIOME_TOOLCHAIN', 'BIOME_DSL'].filter(n => process.env[n]), env = set.length ? process.env[set[0]] : '';
  if (new Set(set.map(n => real(process.env[n]))).size > 1) throw new Error(`${set.map(n => `${n}=${process.env[n]}`).join(', ')} name different checkouts; set one`);
  if (env) { if (isToolchain(resolve(env))) return resolve(env); throw new Error(`no biome-dsl toolchain at ${env} (BIOME_TS / BIOME_TOOLCHAIN / BIOME_DSL)`); }
  const starts = [process.argv[1] && existsSync(process.argv[1]) && dirname(realpathSync(process.argv[1])), process.cwd(), dirname(fileURLToPath(import.meta.url))];
  for (const start of starts.filter(Boolean)) { const found = near(start).find(isToolchain); if (found) return found; }
  throw new Error('biome-dsl toolchain not found: set BIOME_TS=<biome-dsl>/implementations/typescript');
}

// node reports this module by its real path, so a symlinked invocation path is compared after realpath too.
if (process.argv[1] && existsSync(process.argv[1]) && realpathSync(process.argv[1]) === realpathSync(fileURLToPath(import.meta.url))) {
  try {
    const tc = locateBiome(), args = process.argv.slice(2);
    if (!args.length) console.log(tc);
    else process.exitCode = spawnSync(process.execPath, [join(tc, BIN), ...args], { stdio: 'inherit', env: { ...process.env, BIOME_TS: tc } }).status ?? 1;
  } catch (error) { console.error(error.message); process.exitCode = 1; }
}
