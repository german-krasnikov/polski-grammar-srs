// R1.4a (Plans/Biome/R1Design.md): every generated Kotlin file is the output of exactly one GENERATE step, so `biome generate --check` guards it, and git marks it generated.
// A generated file is a tracked .kt whose first line is the generator banner `// Generated from`. Hybrids (a hand-written shell around a GENERATED region) are outputs but have no banner.
import { describe, expect, it } from 'vitest';
import { execFileSync } from 'node:child_process';
import { readFileSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

const repo = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const git = (...args: string[]) => execFileSync('git', args, { cwd: repo, encoding: 'utf8' }).split('\n').filter(Boolean);

/** Files that are not the output of exactly one step: `missing` none, `repeated` more than one. */
export function registration(generated: string[], outputs: string[]) {
  return {
    missing: generated.filter(file => !outputs.includes(file)),
    repeated: [...new Set(outputs.filter((file, i) => outputs.indexOf(file) !== i))],
  };
}

/** The output file of every GENERATE step of every tracked biome.project: a derive step names it in its OUT export, a data step in --out, a logic generator in its quoted --target, a render step in its trailing path, a feature --flat step in its .biome-files manifest. */
async function stepOutputs(): Promise<string[]> {
  const outputs: string[] = [];
  for (const manifest of git('ls-files', '*biome.project')) {
    const dir = dirname(join(repo, manifest));
    for (const line of readFileSync(join(repo, manifest), 'utf8').split('\n').filter(l => l.startsWith('GENERATE '))) {
      const tokens = line.trim().split(/\s+/).map(token => token.replace('{dir}', dir));
      const out = tokens.indexOf('--out');
      const logicTarget = tokens[2] === 'logic' && tokens[3] === 'generate'
        ? line.match(/--target\s+"kt\s+([^"]+?)\s+AS\s+[^"]+"/)?.[1].replaceAll('{dir}', dir)
        : undefined;
      // render steps: GENERATE name render --surface surfaceName source lang outputFile
      const renderOutput = tokens[2] === 'render' && tokens.length >= 7 && tokens[tokens.length - 1].endsWith('.kt')
        ? tokens[tokens.length - 1] : undefined;
      // feature --flat steps: output directory contains a .biome-files manifest listing each managed file
      if (tokens[2] === 'feature' && tokens.includes('--flat') && out >= 0) {
        const outDir = resolve(dir, tokens[out + 1]);
        const testsOut = tokens.indexOf('--tests-out');
        for (const d of [outDir, ...(testsOut >= 0 ? [resolve(dir, tokens[testsOut + 1])] : [])]) {
          try {
            const biomeFiles = readFileSync(join(d, '.biome-files'), 'utf8');
            for (const entry of biomeFiles.split('\n').filter(Boolean)) {
              const path = entry.split(/\s+/)[1];
              if (path?.endsWith('.kt')) outputs.push(resolve(d, path).slice(repo.length + 1));
            }
          } catch { /* no .biome-files yet */ }
        }
        continue;
      }
      const file = tokens[2] === 'derive' ? (await import(pathToFileURL(resolve(tokens[3])).href))[tokens[5]] : renderOutput ?? (out >= 0 ? tokens[out + 1] : logicTarget);
      if (file) outputs.push(resolve(dir, file).slice(repo.length + 1));
    }
  }
  return outputs;
}

const generatedFiles = () => git('ls-files', '*.kt').filter(file => readFileSync(join(repo, file), 'utf8').startsWith('// Generated from'));

/** Generated files that no step registers yet, with the slice that owns them. The list only shrinks: an entry that gets a step, or goes away, fails the test below. */
const UNREGISTERED: Record<string, string> = {
  'tests/fixtures/nj03-textwire/generated/kt/PolskiTextCodecExamples.kt': 'R4 (textwire): written once by `biome test generate`, no step, nothing compiles it',
};
const registrable = () => generatedFiles().filter(file => !(file in UNREGISTERED));

describe('generated files and GENERATE steps', () => {
  it('registration finds a stray file and a doubly registered one', () => {
    expect(registration(['a.kt', 'stray.kt'], ['a.kt', 'b.kt'])).toEqual({ missing: ['stray.kt'], repeated: [] });
    expect(registration(['a.kt'], ['a.kt', 'a.kt'])).toEqual({ missing: [], repeated: ['a.kt'] });
  });

  it('every generated Kotlin file is the output of exactly one step', async () => {
    expect(registrable().length).toBeGreaterThan(0);
    expect(registration(registrable(), await stepOutputs())).toEqual({ missing: [], repeated: [] });
  });

  it('every known unregistered file is still generated and still unregistered', async () => {
    const outputs = await stepOutputs();
    for (const file of Object.keys(UNREGISTERED)) {
      expect(generatedFiles(), `${file} is no longer a tracked generated file: drop it from UNREGISTERED`).toContain(file);
      expect(outputs, `${file} has a step now: drop it from UNREGISTERED`).not.toContain(file);
    }
  });

  it('git marks every registered generated Kotlin file as generated', () => {
    const marks = git('check-attr', 'linguist-generated', '--', ...registrable());
    expect(marks.filter(mark => !/: linguist-generated: (set|true)$/.test(mark))).toEqual([]);
  });
});
