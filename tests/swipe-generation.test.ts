// R0 (Plans/Biome/ResumePlan.md): the runtime SwipePolicy.kt is written and compared by `biome generate`; the derive only returns its text.
// The behaviour tests run the real `biome generate` over an isolated copy of the swipe sources and never write to the working tree.
// They need a biome-dsl checkout: BIOME_BIN=<checkout>/implementations/typescript/packages/biome/src/bin.mjs, else the sibling ../../Python/biome-dsl.
// Without one they are skipped, which is NOT RUN, not a pass.
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { spawnSync } from 'node:child_process';
import { createHash } from 'node:crypto';
import { appendFileSync, copyFileSync, existsSync, mkdirSync, mkdtempSync, readdirSync, readFileSync, realpathSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join, relative, resolve } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

const repo = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const PROJECT = 'tests/fixtures/nj03-exercise-oracle';
const DERIVE = `${PROJECT}/derive-swipe-policy.mjs`;
const CONSUMER = 'kotlin/shared/src/commonMain/kotlin/polski/interaction/SwipePolicy.kt';
const DSL = ['dsl/swipe/swipe.logic', 'dsl/swipe/swipe.contract', 'dsl/swipe/swipe-native.js'];
const cli = process.env.BIOME_BIN ?? resolve(repo, '../../Python/biome-dsl/implementations/typescript/packages/biome/src/bin.mjs');
const SLOW = { timeout: 120_000 };

interface Derive { OUT: string; generate(context: { cli: string; node: string; env: NodeJS.ProcessEnv }): Promise<string> }
const load = async (file: string) => (await import(pathToFileURL(file).href)) as Derive;
const sha256 = (file: string) => createHash('sha256').update(readFileSync(file)).digest('hex');

describe('swipe Policy generation', () => {
  it('the swipe-policy step guards the runtime consumer, not the experimental copy', async () => {
    expect(relative(repo, (await load(join(repo, DERIVE))).OUT)).toBe(CONSUMER);
  });

  it('the derive takes its biome from the generate context and rejects when that biome fails', async () => {
    const { generate } = await load(join(repo, DERIVE));
    await expect(generate({} as never)).rejects.toThrow('no context.cli');
    await expect(generate({ cli: join(repo, 'no-such-biome.mjs'), node: process.execPath, env: process.env })).rejects.toThrow('biome logic generate failed');
  });

  describe.skipIf(!existsSync(cli))('with the biome CLI', () => {
    let dir: string;

    /** The variables that name another toolchain would make a child CLI refuse to run (TOOLCHAIN_CONFLICT); TMPDIR is private to see leftovers. */
    const env = (): NodeJS.ProcessEnv => ({
      ...Object.fromEntries(Object.entries(process.env).filter(([name]) => !['BIOME_CLI', 'BIOME_NODE', 'BIOME_TOOLCHAIN', 'BIOME_TS', 'BIOME_DSL'].includes(name))),
      TMPDIR: join(dir, 'tmp'),
    });

    /** `biome generate [--check]` over the isolated project, as a user runs it. */
    function generate(...flags: string[]) {
      const run = spawnSync(process.execPath, [cli, 'generate', ...flags, '--project', PROJECT], { cwd: dir, env: env(), encoding: 'utf8', timeout: 120_000 });
      return { status: run.status, stderr: run.stderr };
    }

    /** path -> sha256 of every file under the isolated tree, except the private TMPDIR. */
    function snapshot(): Record<string, string> {
      const files: Record<string, string> = {};
      const walk = (current: string) => {
        for (const entry of readdirSync(current, { withFileTypes: true })) {
          const path = join(current, entry.name);
          if (!entry.isDirectory()) files[relative(dir, path)] = sha256(path);
          else if (path !== join(dir, 'tmp')) walk(path);
        }
      };
      walk(dir);
      return files;
    }
    const added = (before: Record<string, string>) => Object.keys(snapshot()).filter(file => !(file in before));
    /** Temp directories the derive made in the private TMPDIR and did not remove. */
    const leftovers = () => readdirSync(join(dir, 'tmp')).filter(name => name.startsWith('swipe-policy-'));

    /** A fresh consumer written by `biome generate`: the precondition of every stale and rejected case. */
    function fresh(): string {
      expect(generate().status, 'biome generate').toBe(0);
      expect(existsSync(join(dir, CONSUMER)), 'biome generate writes the runtime consumer').toBe(true);
      return join(dir, CONSUMER);
    }

    const edit = (file: string, from: string, to: string) => {
      const path = join(dir, file);
      const text = readFileSync(path, 'utf8');
      expect(text, `${file} contains ${from}`).toContain(from);
      writeFileSync(path, text.replace(from, to));
    };

    /** An isolated tree: the swipe DSL, the real derive and the real `GENERATE swipe-policy` line. The consumer does not exist yet. */
    beforeEach(() => {
      dir = realpathSync(mkdtempSync(join(tmpdir(), 'swipe-generation-')));
      for (const file of [...DSL, DERIVE]) {
        mkdirSync(dirname(join(dir, file)), { recursive: true });
        copyFileSync(join(repo, file), join(dir, file));
      }
      const steps = readFileSync(join(repo, 'dsl/biome.project'), 'utf8').split('\n').filter(line => line.startsWith('GENERATE swipe-policy '));
      expect(steps, 'the swipe-policy step is registered once in dsl/biome.project').toHaveLength(1);
      writeFileSync(join(dir, PROJECT, 'biome.project'), `${steps[0]}\n`);
      mkdirSync(join(dir, 'tmp'));
    });
    afterEach(() => rmSync(dir, { recursive: true, force: true }));

    it('the derive returns the Kotlin text and writes nothing; biome generate writes it to the consumer only', SLOW, async () => {
      const before = snapshot();
      const text = await (await load(join(dir, DERIVE))).generate({ cli, node: process.execPath, env: env() });
      expect(snapshot(), 'the derive wrote files').toEqual(before);
      expect(text).toMatch(/^\/\/ Generated from swipe\.logic\n/);
      expect(text).toContain('package polski.interaction\n');
      expect(generate().status).toBe(0);
      expect(added(before)).toEqual([CONSUMER]);
      expect(readFileSync(join(dir, CONSUMER), 'utf8')).toBe(text);
      expect(generate('--check').status).toBe(0);
      expect(leftovers(), 'temp directories left behind').toEqual([]);
    });

    it('a hand-edited consumer fails --check and is not healed', SLOW, () => {
      const consumer = fresh();
      appendFileSync(consumer, '// edited by hand\n');
      const before = snapshot();
      const run = generate('--check');
      expect(run.status).toBe(1);
      expect(run.stderr).toContain(`stale generated file: ${consumer}`);
      expect(snapshot(), '--check changed the tree').toEqual(before);
    });

    it('a DSL change makes the consumer stale until biome generate rewrites it', SLOW, () => {
      const consumer = fresh();
      const old = readFileSync(consumer, 'utf8');
      edit('dsl/swipe/swipe.logic', 'swipeLeft AT 40', 'swipeLeft AT 25');
      const before = snapshot();
      const run = generate('--check');
      expect(run.status).toBe(1);
      expect(run.stderr).toContain(`stale generated file: ${consumer}`);
      expect(snapshot(), '--check changed the tree').toEqual(before);
      expect(generate().status).toBe(0);
      expect(readFileSync(consumer, 'utf8')).not.toBe(old);
      expect(generate('--check').status).toBe(0);
    });

    for (const [code, from, to] of [
      ['CONTRACT_SYNTAX', 'FACT swipe.yDominates bool', 'FACT swipe.yDominates frobnicate'],
      ['LOGIC_UNKNOWN_INPUT_FACT', 'FACT swipe.yDominates bool\n', ''],
    ]) {
      it(`an invalid contract (${code}) is rejected and the consumer is left as it was`, SLOW, () => {
        fresh();
        edit('dsl/swipe/swipe.contract', from, to);
        const before = snapshot();
        for (const flags of [['--check'], []]) {
          const run = generate(...flags);
          expect(run.status, `biome generate ${flags.join(' ')}`).toBe(1);
          expect(run.stderr).toContain(code);
          expect(snapshot(), `biome generate ${flags.join(' ')} changed the tree`).toEqual(before);
        }
        expect(leftovers(), 'temp directories left behind').toEqual([]);
      });
    }
  });
});
