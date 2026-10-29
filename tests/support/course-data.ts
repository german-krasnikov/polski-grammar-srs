// Shared by the tests of the course data (R1.4+): the biome CLI, the contract, and which real file holds which record.
// The record-to-file pairs are read from the course-plru step of dsl/biome.project, so no test keeps a second list of them.
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

const step = readFileSync(join(repo, 'dsl/biome.project'), 'utf8').split('\n').find(line => line.startsWith('GENERATE course-plru '))!;
/** `--rows <Record> <file>` of the step: the real file of each record. */
const REAL = new Map([...step.matchAll(/--rows (\w+) \{dir\}\/\.\.\/(\S+)/g)].map(match => [match[1], join(repo, match[2])]));

export const biome = (...args: string[]) => spawnSync(process.execPath, [cli, ...args], { encoding: 'utf8' });

/** The real file of a record, as the step of dsl/biome.project binds it. */
export const realFile = (record: string) => REAL.get(record)!;

/**
 * `biome data check` of rows the test writes for one record, with the real files of the records it needs bound as the step binds them
 * (a `ref` needs its target). The written file keeps the real file's name, so a problem reads `nouns.core-data:2:5: DATA_REQUIRED`;
 * a problem in a real file shows too, under its own name.
 */
export function rowsOf(record: string, ...needs: string[]) {
  const dir = mkdtempSync(join(tmpdir(), 'course-data-'));
  afterAll(() => rmSync(dir, { recursive: true, force: true }));
  const real = REAL.get(record)!, file = join(dir, basename(real)), name = basename(real);
  const bound = [record, ...needs].flatMap(each => ['--rows', each, each === record ? file : REAL.get(each)!]);

  function check(text: string) {
    writeFileSync(file, text);
    const run = biome('data', 'check', '--contract', CONTRACT, ...bound);
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
