#!/usr/bin/env node
// Syncs assets/rive/*.riv (the single source of truth) to all 4 host copies.
// Binary files, so this stays outside dsl/biome.project's text-only `derive` contract.
//
//   node scripts/sync-rive-assets.mjs           # copy source -> every host path
//   node scripts/sync-rive-assets.mjs --check   # SHA-256 compare; exit 1 if any host is stale/missing

import { createHash } from 'node:crypto';
import { copyFileSync, existsSync, readFileSync, mkdirSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const repoRoot = join(dirname(fileURLToPath(import.meta.url)), '..');
const sourceDir = join(repoRoot, 'assets', 'rive');

// Android's underscore filename (`chain_complete.riv`) is a valid-resource-name requirement;
// every other host uses the canonical hyphenated name.
const TARGETS = {
  'confetti.riv': [
    'kotlin/composeApp/src/androidMain/res/raw/confetti.riv',
    'kotlin/iosApp/PolskiGrammar/Rive/confetti.riv',
    'kotlin/macosApp/PolskiGrammarMac/Rive/confetti.riv',
    'kotlin/composeApp/src/webMain/resources/rive/confetti.riv',
  ],
  'again.riv': [
    'kotlin/composeApp/src/androidMain/res/raw/again.riv',
    'kotlin/iosApp/PolskiGrammar/Rive/again.riv',
    'kotlin/macosApp/PolskiGrammarMac/Rive/again.riv',
    'kotlin/composeApp/src/webMain/resources/rive/again.riv',
  ],
  'chain-complete.riv': [
    'kotlin/composeApp/src/androidMain/res/raw/chain_complete.riv',
    'kotlin/iosApp/PolskiGrammar/Rive/chain-complete.riv',
    'kotlin/macosApp/PolskiGrammarMac/Rive/chain-complete.riv',
    'kotlin/composeApp/src/webMain/resources/rive/chain-complete.riv',
  ],
};

function sha256(path) {
  return createHash('sha256').update(readFileSync(path)).digest('hex');
}

const check = process.argv.includes('--check');
let stale = [];

for (const [sourceName, relativeTargets] of Object.entries(TARGETS)) {
  const sourcePath = join(sourceDir, sourceName);
  if (!existsSync(sourcePath)) {
    throw new Error(`missing canonical source: assets/rive/${sourceName}`);
  }
  const sourceHash = check ? sha256(sourcePath) : null;

  for (const relativeTarget of relativeTargets) {
    const targetPath = join(repoRoot, relativeTarget);

    if (check) {
      if (!existsSync(targetPath)) {
        stale.push(`${relativeTarget} (missing)`);
        continue;
      }
      if (sha256(targetPath) !== sourceHash) {
        stale.push(`${relativeTarget} (stale)`);
      }
      continue;
    }

    mkdirSync(dirname(targetPath), { recursive: true });
    copyFileSync(sourcePath, targetPath);
  }
}

if (check) {
  if (stale.length > 0) {
    console.error('rive assets out of sync with assets/rive/:');
    for (const entry of stale) console.error(`  ${entry}`);
    process.exit(1);
  }
  console.log('rive assets in sync with assets/rive/.');
} else {
  console.log('rive assets synced from assets/rive/.');
}
