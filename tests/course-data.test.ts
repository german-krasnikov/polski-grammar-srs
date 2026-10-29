// R1.2 (Plans/Biome/R1Design.md): the courses-biome files are Core `.core-data`, readable by `biome read`, not a private dialect.
// R1.4a: the skill rows against dsl/course.data-contract: what `biome data` accepts and rejects, with the exact position of each problem.
// They need a biome-dsl checkout: BIOME_BIN=<checkout>/implementations/typescript/packages/biome/src/bin.mjs, else the sibling ../../Python/biome-dsl.
// Without one they are skipped, which is NOT RUN, not a pass.
import { afterAll, describe, expect, it } from 'vitest';
import { spawnSync } from 'node:child_process';
import { existsSync, mkdtempSync, readdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const repo = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const cli = process.env.BIOME_BIN ?? resolve(repo, '../../Python/biome-dsl/implementations/typescript/packages/biome/src/bin.mjs');
const PACK = join(repo, 'courses-biome/pl-ru');

/** Rows per file, from the inventory of R1Design.md section 1 (nouns 14, adjectives 7, verbs 11, skills 16, possessives 7, seeds 12; the other sections are one row). */
const ROWS: Record<string, number> = {
  adjectives: 7, caseSentencePrefixes: 1, exerciseCopy: 1, exercisePatterns: 1, morphology: 1, nouns: 14, personalPronouns: 1, possessives: 7,
  reference: 1, sentenceSeeds: 12, skills: 16, stemAlternations: 1, training: 1, verbs: 11, vocabulary: 1,
};

describe.skipIf(!existsSync(cli))('courses-biome/pl-ru is Core data', () => {
  it('holds exactly the 15 known sections', () => {
    expect(readdirSync(PACK).filter(name => name.endsWith('.core-data')).map(name => name.replace('.core-data', '')).sort()).toEqual(Object.keys(ROWS).sort());
  });

  for (const [section, rows] of Object.entries(ROWS)) {
    it(`biome read ${section}.core-data reads ${rows} row(s)`, () => {
      const run = spawnSync(process.execPath, [cli, 'read', join(PACK, `${section}.core-data`), '--format', 'raw'], { encoding: 'utf8' });
      expect(run.stderr, 'biome read diagnostics').toBe('');
      expect(run.status).toBe(0);
      expect(run.stdout.trimEnd().split('\n')).toHaveLength(rows);
    });
  }
});

const CONTRACT = join(repo, 'dsl/course.data-contract');
const method = (text: string) => `{introduction: "${text}" promptLead: "${text}" introduce: "${text}" retrieve: "${text}" feedback: "${text}" review: "${text}"}`;
/** The fields of one valid SKILL row, as .core-data text; a test replaces one, or drops it with `undefined`. */
const fields = (id: string): Record<string, string> => ({
  id: `"${id}"`, title: '"T"', group: '"G"', level: 'A1', formula: '"f"', theory: '"t"', hint: '"h"', prerequisites: '[]',
  focus: '{before: "b" after: "a"}', methods: `{logic: ${method('l')} situations: ${method('s')}}`, styleContent: '{why: "w"}',
});
const skill = (id: string, change: Record<string, string | undefined> = {}) =>
  `SKILL {${Object.entries({ ...fields(id), ...change }).filter(([, value]) => value !== undefined).map(([key, value]) => `${key}: ${value}`).join(' ')}}`;

describe.skipIf(!existsSync(cli))('skill rows against dsl/course.data-contract', () => {
  const dir = mkdtempSync(join(tmpdir(), 'course-data-'));
  afterAll(() => rmSync(dir, { recursive: true, force: true }));
  const FILE = join(dir, 'skills.core-data');
  const biome = (...args: string[]) => spawnSync(process.execPath, [cli, 'data', ...args], { encoding: 'utf8' });
  /** `biome data check` over the rows: its exit status and each problem as `file:line:col: CODE`. */
  function check(text: string) {
    writeFileSync(FILE, text);
    const run = biome('check', '--contract', CONTRACT, '--rows', 'SkillRow', FILE);
    return { status: run.status, problems: run.stderr.split('\n').flatMap(line => line.replace(FILE, 'skills.core-data').match(/^skills\.core-data:\d+:\d+: DATA_\w+/) ?? []) };
  }
  /** One problem expected: `needle` is found by JS in row `row`, so the column is counted in UTF-16 units without asking the tool. */
  function problem(rows: string[], row: number, needle: string, code: string) {
    const column = rows[row].indexOf(needle) + 1;
    expect(column, `${needle} is in the row`).toBeGreaterThan(0);
    expect(check(rows.join('\n') + '\n')).toEqual({ status: 1, problems: [`skills.core-data:${row + 1}:${column}: ${code}`] });
  }

  it('a missing field is DATA_REQUIRED at the { of the row', () => problem([skill('a', { formula: undefined })], 0, '{', 'DATA_REQUIRED'));
  it('an unknown field is DATA_UNKNOWN_FIELD at its key', () => problem([skill('a', { extra: '1' })], 0, 'extra', 'DATA_UNKNOWN_FIELD'));
  it('a level outside the enum is DATA_ENUM at the value', () => problem([skill('a', { level: 'C9' })], 0, 'C9', 'DATA_ENUM'));
  it('null for a text is DATA_TYPE at the value', () => problem([skill('a', { title: 'null' })], 0, 'null', 'DATA_TYPE'));
  it('text for a bool is DATA_TYPE at the value', () =>
    problem([skill('a', { styleContent: '{nativeParallel: [{native: "n" target: "t" note: "x" matches: "yes"}]}' })], 0, '"yes"', 'DATA_TYPE'));
  it('a second row with the same id is DATA_DUPLICATE at its id', () => problem([skill('a'), skill('a')], 1, '"a"', 'DATA_DUPLICATE'));
  it('a prerequisite that is no skill is DATA_REF at the value', () => problem([skill('a', { prerequisites: '[nope]' })], 0, 'nope', 'DATA_REF'));
  it('an unterminated record is DATA_SYNTAX at its {', () => problem([skill('a').slice(0, -1)], 0, '{', 'DATA_SYNTAX'));
  it('no row at all is DATA_COUNT at 1:1', () => expect(check('')).toEqual({ status: 1, problems: ['skills.core-data:1:1: DATA_COUNT'] }));
  it('columns count UTF-16 units: each emoji before the fault is two', () => problem([skill('a', { title: '"😀😀"', level: 'C9' })], 0, 'C9', 'DATA_ENUM'));

  it('the optional style fields may be left out and an empty text is data', () =>
    expect(check([skill('a', { styleContent: undefined }), skill('b', { styleContent: '{why: ""}' })].join('\n') + '\n')).toEqual({ status: 0, problems: [] }));
  it('a row may be written over several lines, as `biome fmt` lays it out', () => {
    const lines = Object.entries(fields('a')).map(([key, value]) => `  ${key}: ${value}`);
    expect(check(`SKILL {\n${lines.join('\n')}\n}\n`)).toEqual({ status: 0, problems: [] });
  });
  it('the real pl-ru skills pass: 16 rows, no problem', () => {
    const run = biome('check', '--contract', CONTRACT, '--rows', 'SkillRow', join(PACK, 'skills.core-data'));
    expect(run.status).toBe(0);
    expect(run.stdout).toContain('16 row(s) of 1 record(s), 0 problem(s)');
  });

  /** The text of a Kotlin string literal body (\t \b \n \r \' \" \\ \$ \uXXXX); an unescaped `$name` or `${` would be a template, so it throws. */
  function decodeKotlin(body: string): string {
    let text = '';
    for (let i = 0; i < body.length; i++) {
      if (body[i] === '\\') {
        const escape = body[++i];
        text += escape === 'u' ? String.fromCharCode(parseInt(body.slice(i + 1, i + 5), 16)) : ({ t: '\t', b: '\b', n: '\n', r: '\r' } as Record<string, string>)[escape] ?? escape;
        if (escape === 'u') i += 4;
      } else if (body[i] === '$' && /[A-Za-z_{`]/.test(body[i + 1] ?? '')) throw new Error(`unescaped $ in ${body}`);
      else text += body[i];
    }
    return text;
  }

  it('the Kotlin literal decoder reads escapes and refuses a template', () => {
    expect(decodeKotlin('a\\tb\\n\\"q\\" \\\\ \\$x \\u00e9 $ ')).toBe('a\tb\n"q" \\ $x \u00e9 $ ');
    expect(() => decodeKotlin('cost $x')).toThrow('unescaped $');
    expect(() => decodeKotlin('cost ${x}')).toThrow('unescaped $');
  });

  it('the printed Kotlin carries a multi-line text with quotes, backslash, $ and astral characters exactly', () => {
    const title = 'Zażółć\ngęślą 😀 "q" \\ $x ${y}\te\u0301';
    const quoted = '"' + title.replace(/\\/g, '\\\\').replace(/"/g, '\\"').replace(/\n/g, '\\n').replace(/\t/g, '\\t') + '"';
    writeFileSync(FILE, skill('a', { title: quoted }) + '\n');
    const out = join(dir, 'CourseRows.kt');
    const run = biome('generate', '--contract', CONTRACT, '--rows', 'SkillRow', FILE, '--target', 'kt', '--kt-package', 'edge.rows', '--out', out);
    expect(run.status, run.stderr).toBe(0);
    const printed = readFileSync(out, 'utf8').match(/^\s+title = "(.*)",$/m);
    expect(printed, 'a title line').not.toBeNull();
    expect(decodeKotlin(printed![1])).toBe(title);
  });
});
