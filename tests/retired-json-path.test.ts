// R1.3 (Plans/Biome/R1Design.md): the JSON pack path is retired, and this keeps it retired: the CoursePack JSON reader, the :pack-format module
// and the JSON that the Gradle task used to embed for them (frequency, lexicon, pair, lifehacks, the manifest). R1.19 widens it to every courses/**.json reader.
import { describe, expect, it } from 'vitest';
import { existsSync, readdirSync, readFileSync, statSync } from 'node:fs';
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
