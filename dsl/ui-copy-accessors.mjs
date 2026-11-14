// Derive: UiCopy.kt (polski.presentation), the typed accessors over a pack's `uiCopy` string map.
// The accessors are declared once in dsl/ui-copy.declare (Name -> key, or FN name template param for a parameterized
// fun); this script checks every non-PARTIAL key against both packs' uiCopy.core-data (PARTIAL: at least one) and
// prints the object. A key missing from a pack it must be in fails `biome generate` before a stale accessor ships.
// It returns the text and writes nothing: `biome generate` writes OUT (generate) or compares it with the file (--check).
import { spawnSync } from 'node:child_process';
import { readFileSync } from 'node:fs';
import { join, resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
export const OUT = join(root, 'kotlin/shared/src/commonMain/generated/kotlin/polski/presentation/UiCopy.kt');
const DECLARE = join(root, 'dsl/ui-copy.declare');
const PACKS = { 'pl-ru': join(root, 'courses-biome/pl-ru/uiCopy.core-data'), 'en-ru': join(root, 'courses-biome/en-ru/uiCopy.core-data') };

function biome(node, cli, env, args) {
  const run = spawnSync(node, [cli, ...args], { cwd: root, env, encoding: 'utf8', timeout: 120_000 });
  if (run.status !== 0) throw new Error(`biome ${args.slice(0, 2).join(' ')} failed (${run.status ?? run.error?.message}): ${run.stderr?.trim()}`);
  return JSON.parse(run.stdout);
}

/** Entries of dsl/ui-copy.declare, in file order: `{kind:'val', name, key, partial, doc}` or `{kind:'fn', name, template, param, doc}`. A `#` line is a comment, attached as
 *  KDoc to the very next entry unless a blank line comes between (which also ends a group banner such as the file's own header). */
function parseDeclare(text) {
  const entries = [];
  let doc = [];
  for (const line of text.split('\n')) {
    const trimmed = line.trim();
    if (!trimmed) { doc = []; continue; }
    if (trimmed.startsWith('#')) { doc.push(trimmed.replace(/^#\s?/, '')); continue; }
    const fn = trimmed.match(/^FN\s+(\w+)\s+(\S+)\s+(\w+)$/);
    if (fn) { entries.push({ kind: 'fn', name: fn[1], template: fn[2], param: fn[3], doc }); doc = []; continue; }
    const val = trimmed.match(/^(\w+)\s+(\S+)(\s+PARTIAL)?$/);
    if (!val) throw new Error(`dsl/ui-copy.declare: unreadable line "${line}"`);
    entries.push({ kind: 'val', name: val[1], key: val[2], partial: Boolean(val[3]), doc });
    doc = [];
  }
  return entries;
}

/** Every non-PARTIAL entry's key must exist in both packs; a PARTIAL one in at least one. Throws on the first gap. */
function checkCoverage(entries, keysByPack) {
  for (const entry of entries) {
    if (entry.kind !== 'val') continue;
    const packs = Object.entries(keysByPack).filter(([, keys]) => !keys.has(entry.key)).map(([pair]) => pair);
    if (entry.partial ? packs.length === 2 : packs.length > 0) {
      throw new Error(`dsl/ui-copy.declare: "${entry.key}" (${entry.name}) is missing from ${packs.join(', ')}`);
    }
  }
}

const doc = (lines, indent = '    ') => lines.length ? `${indent}/** ${lines.join(' ')} */\n` : '';

function printEntry(entry) {
  if (entry.kind === 'fn') return `${doc(entry.doc)}    fun ${entry.name}(${entry.param}: String): String = text("${entry.template}")`;
  return `${doc(entry.doc)}    val ${entry.name}: String get() = text("${entry.key}")`;
}

export async function generate(context = {}) {
  const { cli, node = process.execPath, env = process.env } = context;
  if (!cli) throw new Error('ui-copy-accessors: no context.cli; run it with biome generate');
  const keysByPack = Object.fromEntries(Object.entries(PACKS).map(([pair, file]) =>
    [pair, new Set(biome(node, cli, env, ['read', file, '--format', 'json']).map((row) => row.value.key))]));
  const entries = parseDeclare(readFileSync(DECLARE, 'utf8'));
  checkCoverage(entries, keysByPack);
  const vals = entries.filter((e) => e.kind === 'val').map(printEntry).join('\n\n');
  const fns = entries.filter((e) => e.kind === 'fn').map(printEntry).join('\n\n');
  return `// Generated from dsl/ui-copy.declare, checked against courses-biome/pl-ru/uiCopy.core-data and courses-biome/en-ru/uiCopy.core-data, by dsl/ui-copy-accessors.mjs.
// Edit dsl/ui-copy.declare, then run biome generate.
package polski.presentation

import polski.data.packRegistry

/**
 * Pack-aware UI string catalog. Each property reads the active pack's \`uiCopy\` map,
 * so switching packs changes all labels. Fallback = key name (visibly broken if a pack omits it).
 */
object UiCopy {
    /** Complete read-only label snapshot for the currently selected course pack. */
    fun all(): Map<String, String> = packRegistry.active.uiCopy.toMap()

${vals}

${fns}
}

private fun text(key: String): String = packRegistry.active.uiCopy[key] ?: key
`;
}
