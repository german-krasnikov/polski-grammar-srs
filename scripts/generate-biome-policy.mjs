import { spawnSync } from 'node:child_process';
import { readFileSync, realpathSync } from 'node:fs';
import { join, resolve } from 'node:path';
import { locateBiome } from './locate-biome.mjs';

const root = resolve(import.meta.dirname, '..');
const manifest = readFileSync(join(root, 'biome.project'), 'utf8');
const pinned = /^TOOLCHAIN ([0-9a-f]{40})$/m.exec(manifest)?.[1];
if (!pinned) throw new Error('biome.project must pin one Biome compiler commit');

const toolchain = locateBiome();
const repository = resolve(toolchain, '../..');
const git = args => {
  const result = spawnSync('git', ['-C', repository, ...args], { encoding: 'utf8' });
  if (result.status !== 0) throw new Error(`Cannot verify Biome checkout: ${result.stderr.trim()}`);
  return result.stdout.trim();
};
if (realpathSync(git(['rev-parse', '--show-toplevel'])) !== realpathSync(repository)) {
  throw new Error(`Biome CLI is not a standalone checkout: ${repository}`);
}
if (git(['rev-parse', 'HEAD']) !== pinned) throw new Error(`Biome compiler must be at ${pinned}`);
if (git(['status', '--porcelain'])) throw new Error('Biome compiler checkout has uncommitted changes');
if (process.argv.includes('--verify-only')) process.exit(0);

const result = spawnSync(process.execPath, [join(toolchain, 'packages/biome/src/bin.mjs'),
  'generate', '--project', join(root, 'biome.project'), 'session-policy'],
{ cwd: root, stdio: 'inherit', env: { ...process.env, BIOME_TS: toolchain } });
if (result.error) throw result.error;
process.exitCode = result.status ?? 1;
