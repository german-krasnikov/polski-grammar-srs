// Shared by the tests of the course data (R1.4+): the biome CLI, the contract, and which real file holds which record.
// The record-to-file pairs and their contracts are read from the data steps of dsl/biome.project, so no test keeps a second list of them.
import { spawnSync } from 'node:child_process';
import { existsSync, mkdtempSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { basename, dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { afterAll, expect } from 'vitest';

export const repo = resolve(dirname(fileURLToPath(import.meta.url)), '../..');
export const cli = process.env.BIOME_BIN ?? resolve(repo, '../../Python/biome-dsl/implementations/typescript/packages/biome/src/bin.mjs');
export const hasCli = existsSync(cli);
export const PACK = join(repo, 'courses-biome/pl-ru');
export const CONTRACT = join(repo, 'dsl/course.data-contract');

const project = readFileSync(join(repo, 'dsl/biome.project'), 'utf8');
const CONTRACTS = new Map([...project.matchAll(/^CONTRACT (\S+) FROM (\S+)/gm)].map(match => [match[1], join(repo, 'dsl', match[2])]));
/** For each record: its real file and the contract of the data step that binds it (`--rows <Record> <file>` and `--contract <name>`). */
const REAL = new Map<string, { file: string; contract: string }>();
for (const step of project.split('\n').filter(line => /^GENERATE \S+ data /.test(line))) {
  const contract = CONTRACTS.get(step.match(/--contract (\S+)/)![1])!;
  for (const match of step.matchAll(/--rows (\w+) \{dir\}\/\.\.\/(\S+)/g)) REAL.set(match[1], { file: join(repo, match[2]), contract });
}

export const biome = (...args: string[]) => spawnSync(process.execPath, [cli, ...args], { encoding: 'utf8' });

/** The real file of a record, as the data step of dsl/biome.project binds it. */
export const realFile = (record: string) => REAL.get(record)!.file;

/**
 * `biome data check` of rows the test writes for one record, with the real files of the records it needs bound as the step binds them
 * (a `ref` needs its target). The written file keeps the real file's name, so a problem reads `nouns.core-data:2:5: DATA_REQUIRED`;
 * a problem in a real file shows too, under its own name.
 */
export function rowsOf(record: string, ...needs: string[]) {
  const dir = mkdtempSync(join(tmpdir(), 'course-data-'));
  afterAll(() => rmSync(dir, { recursive: true, force: true }));
  /** A record no step binds to a file of its own (a part of a row, bound here only to be checked alone) is written as `<Record>.core-data` and checked against the course contract. */
  const { file: real, contract } = REAL.get(record) ?? { file: join(PACK, `${record}.core-data`), contract: CONTRACT }, file = join(dir, basename(real)), name = basename(real);
  const bound = [record, ...needs].flatMap(each => ['--rows', each, each === record ? file : REAL.get(each)!.file]);

  function check(text: string) {
    writeFileSync(file, text);
    const run = biome('data', 'check', '--contract', contract, ...bound);
    const problems = run.stderr.split('\n').flatMap(line => {
      const match = line.match(/^(\S+):(\d+):(\d+): (DATA_\w+)/);
      return match ? [`${basename(match[1])}:${match[2]}:${match[3]}: ${match[4]}`] : [];
    });
    return { status: run.status, problems };
  }
  /** One problem expected: `needle` is found by JS in row `row`, so the column is counted in UTF-16 units without asking the tool. */
  function problem(rows: string[], row: number, needle: string, code: string) {
    const column = rows[row].indexOf(needle) + 1;
    expect(column, `${needle} is in the row`).toBeGreaterThan(0);
    expect(check(rows.join('\n') + '\n')).toEqual({ status: 1, problems: [`${name}:${row + 1}:${column}: ${code}`] });
  }
  return { check, problem, file, dir, name };
}

/** `TAG {key: value ...}` from the fields of a valid row; `change` replaces a field, or drops it with `undefined`. */
export const row = (tag: string, fields: Record<string, string>, change: Record<string, string | undefined> = {}) =>
  `${tag} {${Object.entries({ ...fields, ...change }).filter(([, value]) => value !== undefined).map(([key, value]) => `${key}: ${value}`).join(' ')}}`;

const CASES = ['nom', 'gen', 'dat', 'acc', 'inst', 'loc', 'voc'];
const GENDERS = ['m_personal', 'm_animate', 'm_inanimate', 'f', 'n'];
/** The seven cases of one form record, each text tagged with where it sits; `drop` leaves one case out. */
export const cases = (tag: string, drop?: string) => `{${CASES.filter(c => c !== drop).map(c => `${c}: "${tag}-${c}"`).join(' ')}}`;
/** The five genders of a form record, each holding its seven cases; `drop` leaves one gender out. */
export const genders = (drop?: string) => `{${GENDERS.filter(g => g !== drop).map(g => `${g}: ${cases(g)}`).join(' ')}}`;
