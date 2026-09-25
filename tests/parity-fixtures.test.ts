import { describe, expect, it } from 'vitest';
import { createHash } from 'node:crypto';
import { execFileSync } from 'node:child_process';
import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import { capture } from './fixtures/kotlin-parity/capture';
import grammar from './fixtures/kotlin-parity/grammar.json';
import exercises from './fixtures/kotlin-parity/exercises.json';
import evaluation from './fixtures/kotlin-parity/evaluation.json';
import scheduler from './fixtures/kotlin-parity/scheduler.json';
import progress from './fixtures/kotlin-parity/progress.json';
import manifest from './fixtures/kotlin-parity/manifest.json';

describe('portable React baseline fixtures', () => {
  const checkedIn = { grammar, exercises, evaluation, scheduler, progress };
  const current = capture();
  for (const name of Object.keys(checkedIn) as (keyof typeof checkedIn)[]) {
    it(`${name} replays against current exports with unique semantic IDs`, () => {
      const actual = current[name];
      const ids = actual.cases.map(item => item.id);
      expect(new Set(ids).size).toBe(ids.length);
      expect(actual).toEqual(checkedIn[name]);
    });
  }
  it('covers the required inventory and linked chains', () => {
    expect(grammar.cases.filter(item => item.id.startsWith('G-NOUN-'))).toHaveLength(14 * 2 * 7);
    expect(grammar.cases.filter(item => item.id.startsWith('S-'))).toHaveLength(16);
    const chain = exercises.cases.filter(item => item.id.startsWith('C-'));
    expect(chain).toHaveLength(12 * 5);
    for (let i = 1; i < chain.length; i++) if (i % 5 !== 0)
      expect((chain[i - 1].expected as { expected: string }).expected).toBe((chain[i].expected as { source: string }).source);
    expect(scheduler.cases.filter(item => /^F-(New|Learning|Review|Relearning)-(again|hard|good|easy)$/.test(item.id))).toHaveLength(16);
  });
  it('records exact captured source and fixture hashes while allowing unrelated tooling changes', () => {
    const hash = (path: string) => createHash('sha256').update(readFileSync(path)).digest('hex');
    const pinned = (path: string) => createHash('sha256').update(execFileSync('git', ['show', `${manifest.sourceRevision}:${path}`])).digest('hex');
    for (const [path, digest] of Object.entries(manifest.sourceSha256))
      expect(pinned(path), path).toBe(digest);
    const installed = JSON.parse(readFileSync(join(process.cwd(), 'package-lock.json'), 'utf8'));
    expect(installed.packages['node_modules/ts-fsrs'].version).toBe(manifest.tsFsrs);
    expect(installed.packages['node_modules/tsx'].version).toBe(manifest.tsx);
    expect(manifest.lockSha256).toMatch(/^[a-f0-9]{64}$/);
    for (const [name, file] of Object.entries(manifest.files)) {
      expect(hash(join(process.cwd(), `tests/fixtures/kotlin-parity/${name}.json`))).toBe(file.sha256);
      expect((checkedIn[name as keyof typeof checkedIn]).cases.map(item => item.id)).toEqual(file.caseIds);
    }
  });
});
