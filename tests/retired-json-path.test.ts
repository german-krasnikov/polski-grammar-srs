// R1.3 and R1.19 (Plans/Biome/R1Design.md): the JSON path of the course data is retired, and this keeps it retired. R1.3: the CoursePack JSON reader, the :pack-format module and the JSON that the Gradle task used
// to embed for them. R1.19: every courses/**.json reader: no Gradle task, Kotlin constant or Python script takes the course data from a JSON file, and the files themselves are gone. The course data is
// courses-biome/<pair>/*.core-data, printed by `biome generate --project dsl/biome.project`.
import { describe, expect, it } from 'vitest';
import { existsSync, readdirSync, readFileSync, statSync } from 'node:fs';
import { execFileSync } from 'node:child_process';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const repo = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const read = (path: string) => readFileSync(join(repo, path), 'utf8');

function kotlinSources(dir: string): string[] {
  if (dir.endsWith('/build')) return [];
  return readdirSync(dir).flatMap(name => {
    const path = join(dir, name);
    return statSync(path).isDirectory() ? kotlinSources(path) : path.endsWith('.kt') ? [path] : [];
  });
}

describe('the JSON pack path stays retired', () => {
  it('the :pack-format module is not part of the build', () => {
    expect(existsSync(join(repo, 'kotlin/pack-format/build.gradle.kts'))).toBe(false);
    expect(existsSync(join(repo, 'kotlin/pack-format/src'))).toBe(false);
    expect(read('kotlin/settings.gradle.kts')).not.toContain('pack-format');
    expect(read('kotlin/shared/build.gradle.kts')).not.toContain('pack-format');
  });

  it('the generated course source embeds only what a live reader needs', () => {
    const gradle = read('kotlin/shared/build.gradle.kts');
    for (const dead of ['embeddedCoursePackSources', 'coursePackManifest', 'generatedFrequencyJson', 'generatedLexiconJsonByLang', 'generatedPairJsonByPairId', 'generatedLifehacksJsonByPairId']) {
      expect(gradle, dead).not.toContain(dead);
    }
  });

  it('no Kotlin source declares or uses the JSON CoursePack reader', () => {
    expect(existsSync(join(repo, 'kotlin/shared/src/commonMain/kotlin/polski/data/CoursePackJson.kt'))).toBe(false);
    const users = kotlinSources(join(repo, 'kotlin')).filter(file => /\b(CoursePackSource|EmbeddedCoursePackSource|PackManifest)\b|\bCoursePack\(/.test(readFileSync(file, 'utf8')));
    expect(users.map(file => file.slice(repo.length + 1))).toEqual([]);
  });
});

// R1.19: the rest of the readers.
const tracked = (...patterns: string[]) => execFileSync('git', ['ls-files', ...patterns], { cwd: repo, encoding: 'utf8' }).split('\n').filter(Boolean);
/** Every course JSON file that R1 deleted since the base of R1.0 (9d867a6), and the three that left the tree before it. The checkers read them from history, not from the tree. */
const RETIRED = [
  'courses/pl-ru/course.json', 'courses/pairs/pl-ru/pair.json', 'courses/lang/pl/lexicon.json',
  'courses/lang/pl/curriculum.json', 'courses/lang/pl/exercise-recipes.json', 'courses/lang/pl/realization.json', 'courses/pl-ru/forms.generated.json', 'courses/pl-ru/frequency-top1000.json',
  'courses/lang/en/curriculum.json', 'courses/lang/en/exercise-recipes.json', 'courses/lang/en/realization.json', 'courses/lang/en/forms.generated.json', 'courses/lang/en/lexicon.json',
  'courses/lang/en/prepositions.json', 'courses/pairs/en-ru/pair.json', 'courses/pairs/en-ru/lifehacks.json', 'courses/pairs/pl-ru/lifehacks.json', 'courses/en-ru/frequency-top1000.json',
  'courses/styles/minimal-theory.json', 'courses/styles/native-contrast.json', 'courses/styles/rule-first.json', 'courses/styles/situation-first.json',
];

describe('no reader of the course JSON is left (R1.19)', () => {
  it('none of the course JSON files that R1 retired is in the tree, and none is tracked again', () => {
    for (const path of RETIRED) expect(existsSync(join(repo, path)), path).toBe(false);
    expect(tracked('courses').filter(path => RETIRED.includes(path))).toEqual([]);
  });

  it('no Gradle file or version catalog names the courses directory', () => {
    const files = tracked('*.gradle.kts', '*.gradle', '*.properties', '*.toml');
    expect(files.length).toBeGreaterThan(5);
    expect(files.filter(file => /\bcourses\//.test(read(file)))).toEqual([]);
  });

  it('no Kotlin source declares a constant that embeds JSON text (val generated...Json...)', () => {
    const files = tracked('*.kt');
    expect(files.length).toBeGreaterThan(100);
    expect(files.filter(file => /\bval\s+generated\w*Json\w*\b/.test(read(file)))).toEqual([]);
  });

  it('no source of polski.data, polski.core or polski.training uses kotlinx.serialization', () => {
    const files = tracked('*.kt').filter(file => /\/polski\/(data|core|training)\//.test(file));
    expect(files.length).toBeGreaterThan(50);
    expect(files.filter(file => read(file).includes('kotlinx.serialization'))).toEqual([]);
  });

  it('a Python script or NJ03 checker that names a courses JSON file reads it from git history, at a pinned revision', () => {
    const named = [...new Set([...tracked('*.py'), ...tracked('scripts/check-nj03-*', 'scripts/nj03/*')])].filter(file => /courses\/[\w./-]*\.json/.test(read(file)));
    expect(named.length).toBeGreaterThan(2);
    for (const file of named) expect(read(file), file).toMatch(/\bgit\b[^\n]*\bshow\b/);
    for (const file of named) expect(read(file), file).toMatch(/[0-9a-f]{40}/);
  });
});
