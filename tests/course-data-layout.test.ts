// R1.19 (Plans/Biome/R1Design.md): the course data is in the one canonical layout of `biome fmt`, and the project says so for every file: each tracked courses-biome/**/*.core-data is named by an `fmt` step of
// dsl/biome.project, which `biome generate --check` runs (no file is written; the fix is `biome fmt <file>`). The first test needs no biome checkout; the second needs one (see course-data.test.ts) and is NOT RUN without it.
import { describe, expect, it } from 'vitest';
import { execFileSync } from 'node:child_process';
import { readFileSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { biome, hasCli, repo, rowsOf } from './support/course-data';

const tracked = () => execFileSync('git', ['ls-files', 'courses-biome'], { cwd: repo, encoding: 'utf8' }).split('\n').filter(Boolean).sort();
/** Course files named by `fmt` steps; other steps also format independent DSL oracles. */
const formatted = () => readFileSync(join(repo, 'dsl/biome.project'), 'utf8').split('\n').filter(line => /^GENERATE \S+ fmt /.test(line))
  .flatMap(line => line.split(/\s+/).slice(3)).map(file => file.replace('{dir}/../', ''))
  .filter(file => file.startsWith('courses-biome/')).sort();

describe('the fmt steps of dsl/biome.project', () => {
  it('name every tracked file of courses-biome once, and nothing else', () => {
    expect(tracked().length).toBeGreaterThan(40);
    expect(formatted()).toEqual(tracked());
  });
});

describe.skipIf(!hasCli)('the canonical layout', () => {
  const { file, name } = rowsOf('FrequencyRow');
  it('refuses a row that is not in it, and says which file to format', () => {
    writeFileSync(file, 'FREQ {rank: 1   lemma: "w" count: 1}\n');
    const run = biome('fmt', '--check', file);
    expect(run.status).toBe(1);
    expect(run.stderr).toContain(`${name}: not canonically formatted; run biome fmt`);
  });
});
