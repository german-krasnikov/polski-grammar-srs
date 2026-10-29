// R1.2 (Plans/Biome/R1Design.md): the courses-biome files are Core `.core-data`, readable by `biome read`, not a private dialect.
// R1.4a: the skill rows against dsl/course.data-contract: what `biome data` accepts and rejects, with the exact position of each problem.
// They need a biome-dsl checkout: BIOME_BIN=<checkout>/implementations/typescript/packages/biome/src/bin.mjs, else the sibling ../../Python/biome-dsl.
// Without one they are skipped, which is NOT RUN, not a pass.
import { describe, expect, it } from 'vitest';
import { readFileSync, readdirSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { CONTRACT, PACK, biome, hasCli, realFile, row, rowsOf } from './support/course-data';

/** Rows per file, from the inventory of R1Design.md section 1 (nouns 14, adjectives 7, verbs 11, skills 16, possessives 7, personalPronouns 9, seeds 12, exerciseCopy 73, exercisePatterns 13, caseSentencePrefixes 7, frequency 1000; the other sections are one row). */
const ROWS: Record<string, number> = {
  adjectives: 7, caseSentencePrefixes: 7, exerciseCopy: 73, exercisePatterns: 13, frequency: 1000, morphology: 1, nouns: 14, personalPronouns: 9, possessives: 7,
  reference: 1, sentenceSeeds: 12, skills: 16, stemAlternations: 1, training: 1, verbs: 11, vocabulary: 1,
};

describe.skipIf(!hasCli)('courses-biome/pl-ru is Core data', () => {
  it('holds exactly the 16 known sections', () => {
    expect(readdirSync(PACK).filter(name => name.endsWith('.core-data')).map(name => name.replace('.core-data', '')).sort()).toEqual(Object.keys(ROWS).sort());
  });

  for (const [section, rows] of Object.entries(ROWS)) {
    it(`biome read ${section}.core-data reads ${rows} row(s)`, () => {
      const run = biome('read', join(PACK, `${section}.core-data`), '--format', 'raw');
      expect(run.stderr, 'biome read diagnostics').toBe('');
      expect(run.status).toBe(0);
      expect(run.stdout.trimEnd().split('\n')).toHaveLength(rows);
    });
  }

  it('the course-plru step of dsl/biome.project passes: every row against the contract, and the printed Kotlin is current', () => {
    const run = biome('generate', '--check', '--project', join(PACK, '../../dsl/biome.project'));
    expect(run.stderr.replace(/^warning TOOLCHAIN_PIN_STALE.*\n/m, '')).toBe('');
    expect(run.status).toBe(0);
  });
});

const method = (text: string) => `{introduction: "${text}" promptLead: "${text}" introduce: "${text}" retrieve: "${text}" feedback: "${text}" review: "${text}"}`;
/** The fields of one valid SKILL row, as .core-data text; a test replaces one, or drops it with `undefined`. */
const fields = (id: string): Record<string, string> => ({
  id: `"${id}"`, title: '"T"', group: '"G"', level: 'A1', formula: '"f"', theory: '"t"', hint: '"h"', prerequisites: '[]',
  focus: '{before: "b" after: "a"}', methods: `{logic: ${method('l')} situations: ${method('s')}}`,
  construction: '"core.c"', featureFocus: 'null', fixed: '[]', lexicalFilter: 'null', styleContent: '{why: "w"}',
});
const skill = (id: string, change: Record<string, string | undefined> = {}) => row('SKILL', fields(id), change);

describe.skipIf(!hasCli)('skill rows against dsl/course.data-contract', () => {
  const { check, problem, file, name } = rowsOf('SkillRow', 'NounRow');

  it('a missing field is DATA_REQUIRED at the { of the row', () => problem([skill('a', { formula: undefined })], 0, '{', 'DATA_REQUIRED'));
  it('an unknown field is DATA_UNKNOWN_FIELD at its key', () => problem([skill('a', { extra: '1' })], 0, 'extra', 'DATA_UNKNOWN_FIELD'));
  it('a level outside the enum is DATA_ENUM at the value', () => problem([skill('a', { level: 'C9' })], 0, 'C9', 'DATA_ENUM'));
  it('null for a text is DATA_TYPE at the value', () => problem([skill('a', { title: 'null' })], 0, 'null', 'DATA_TYPE'));
  it('text for a bool is DATA_TYPE at the value', () =>
    problem([skill('a', { styleContent: '{nativeParallel: [{native: "n" target: "t" note: "x" matches: "yes"}]}' })], 0, '"yes"', 'DATA_TYPE'));
  it('a second row with the same id is DATA_DUPLICATE at its id', () => problem([skill('a'), skill('a')], 1, '"a"', 'DATA_DUPLICATE'));
  it('a prerequisite that is no skill is DATA_REF at the value', () => problem([skill('a', { prerequisites: '[nope]' })], 0, 'nope', 'DATA_REF'));
  it('an unterminated record is DATA_SYNTAX at its {', () => problem([skill('a').slice(0, -1)], 0, '{', 'DATA_SYNTAX'));
  it('no row at all is DATA_COUNT at 1:1', () => expect(check('')).toEqual({ status: 1, problems: [`${name}:1:1: DATA_COUNT`] }));
  it('columns count UTF-16 units: each emoji before the fault is two', () => problem([skill('a', { title: '"😀😀"', level: 'C9' })], 0, 'C9', 'DATA_ENUM'));

  it('a NULLABLE field that was not written is DATA_REQUIRED at the { of the row', () =>
    problem([skill('a', { featureFocus: undefined })], 0, '{', 'DATA_REQUIRED'));
  it('a fixed feature without its value is DATA_REQUIRED at the { of the pair', () =>
    problem([skill('a', { fixed: '[{feature: "Tense"}]' })], 0, '{feature: "Tense"}', 'DATA_REQUIRED'));
  it('a filter property the contract does not know is DATA_UNKNOWN_FIELD at its key', () =>
    problem([skill('a', { lexicalFilter: '{slot: "noun" where: {genre: ["f"]}}' })], 0, 'genre', 'DATA_UNKNOWN_FIELD'));
  it('a noun id that is no noun is DATA_REF at the value', () =>
    problem([skill('a', { lexicalFilter: '{slot: "noun" where: {nounId: ["nobody"]}}' })], 0, '"nobody"', 'DATA_REF'));
  it('a written focus, fixed pairs and a filter with either or both properties are accepted', () => {
    const spec = { featureFocus: '{feature: "Case" from: "Nom" to: "Acc"}', fixed: '[{feature: "Tense" value: "Pres"} {feature: "Person" value: "1"}]' };
    const rows = ['{nounId: ["wife"]}', '{gender: ["n"]}', '{nounId: ["wife"] gender: ["f"]}'].map((where, i) =>
      skill(`s${i}`, { ...spec, lexicalFilter: `{slot: "noun" where: ${where}}` }));
    expect(check(rows.join('\n') + '\n')).toEqual({ status: 0, problems: [] });
  });

  it('the optional style fields may be left out and an empty text is data', () =>
    expect(check([skill('a', { styleContent: undefined }), skill('b', { styleContent: '{why: ""}' })].join('\n') + '\n')).toEqual({ status: 0, problems: [] }));
  it('a row may be written over several lines, as `biome fmt` lays it out', () => {
    const lines = Object.entries(fields('a')).map(([key, value]) => `  ${key}: ${value}`);
    expect(check(`SKILL {\n${lines.join('\n')}\n}\n`)).toEqual({ status: 0, problems: [] });
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
    expect(decodeKotlin('a\\tb\\n\\"q\\" \\\\ \\$x \\u00e9 $ ')).toBe('a\tb\n"q" \\ $x é $ ');
    expect(() => decodeKotlin('cost $x')).toThrow('unescaped $');
    expect(() => decodeKotlin('cost ${x}')).toThrow('unescaped $');
  });

  it('the printed Kotlin carries a multi-line text with quotes, backslash, $ and astral characters exactly', () => {
    const title = 'Zażółć\ngęślą 😀 "q" \\ $x ${y}\té';
    const quoted = '"' + title.replace(/\\/g, '\\\\').replace(/"/g, '\\"').replace(/\n/g, '\\n').replace(/\t/g, '\\t') + '"';
    writeFileSync(file, skill('a', { title: quoted }) + '\n');
    const out = join(file, '../CourseRows.kt');
    const run = biome('data', 'generate', '--contract', CONTRACT, '--rows', 'SkillRow', file, '--rows', 'NounRow', join(PACK, 'nouns.core-data'), '--target', 'kt', '--kt-package', 'edge.rows', '--out', out);
    expect(run.status, run.stderr).toBe(0);
    const printed = readFileSync(out, 'utf8').match(/^\s+title = "(.*)",$/m);
    expect(printed, 'a title line').not.toBeNull();
    expect(decodeKotlin(printed![1])).toBe(title);
  });
});
