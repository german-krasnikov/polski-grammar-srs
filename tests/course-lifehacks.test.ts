// R1.18 (Plans/Biome/R1Design.md): the lifehack rows of courses-biome/{pl-ru,en-ru}/lifehacks.core-data against the sealed courses/pairs/<pair>/lifehacks.json, which left the tree in this slice, so they are
// read from the commit that still has them (git show <rev>:path, sha256 pinned), and against the lifehack records of dsl/course.data-contract: what `biome data check` rejects, with the exact position of
// each problem. What the contract cannot state (an id prefix, a topic for a tip with no skill, a placeholder citation) is LifehackRowsTest. Replaces tests/lifehacks-pl-ru.test.ts and lifehacks-en-ru.test.ts.
// The data cases need a biome-dsl checkout (see course-data.test.ts); without one they are skipped, which is NOT RUN, not a pass.
import { beforeAll, describe, expect, it } from 'vitest';
import { execFileSync } from 'node:child_process';
import { createHash } from 'node:crypto';
import { join } from 'node:path';
import { biome, hasCli, repo, row, rowsIn } from './support/course-data';

const REV = '7358aeb72ab79dce80fca1faadba6be64892fa18';
const PAIRS = [
  { pair: 'pl-ru', step: 'course-plru', nouns: 'NounRow', sha256: '6b851b198f19e2a594175050439b0ee4392bc670f8ed0207fe00949b295cbc61', skill: 'case.gen.neg', other: 'agreement.my' },
  { pair: 'en-ru', step: 'course-enru', nouns: 'EnNounRow', sha256: '4cc8eb175cef2189b30493308e03d6f75cbbc6f19b2e899309394143bf6369c0', skill: 'en:role.object', other: 'en:verb.presentSimple' },
];
/** The kind as the old file spelled it, for the kind as the rows spell it (an enum tag is an identifier). */
const OLD_KIND: Record<string, string> = { research: 'research', teaching_practice: 'teaching-practice', project_authored: 'project-authored' };

for (const { pair, step, nouns, sha256, skill, other } of PAIRS) {
  describe.skipIf(!hasCli)(`the lifehack rows of ${pair} against the sealed lifehacks.json`, () => {
    let run: ReturnType<typeof biome>;
    let rows: any[];
    let sealed: any;
    beforeAll(() => {
      run = biome('read', join(repo, `courses-biome/${pair}/lifehacks.core-data`), '--format', 'json');
      rows = (JSON.parse(run.stdout) as { value: any }[]).map(r => r.value);
      const raw = execFileSync('git', ['show', `${REV}:courses/pairs/${pair}/lifehacks.json`], { cwd: repo });
      expect(createHash('sha256').update(raw).digest('hex'), `courses/pairs/${pair}/lifehacks.json at ${REV}`).toBe(sha256);
      sealed = JSON.parse(raw.toString('utf8'));
    });

    it('has the 32 rows of the file, in the same order', () => {
      expect(run.status, run.stderr).toBe(0);
      expect(sealed.pairId).toBe(pair);
      expect(rows.slice(0, 32).map(r => r.id)).toEqual(sealed.lifehacks.map((l: any) => l.id));
      expect(rows).toHaveLength(pair === 'pl-ru' ? 42 : 32);
    });

    it('carries the id, skill, topic, text, source and status of every tip, and only the vote counts of 0 are left out', () => {
      const regrouped = rows.map(r => ({
        id: r.id, skillId: r.skillId ?? null, topic: r.topic ?? null, text: r.text,
        source: { kind: OLD_KIND[r.source.kind], citation: r.source.citation, ...(r.source.url === undefined ? {} : { url: r.source.url }) },
        status: r.status, votes: { helpful: 0, notHelpful: 0 },
      }));
      expect(regrouped.slice(0, 32)).toEqual(sealed.lifehacks);
      if (pair === 'pl-ru') expect(regrouped[32].id).toBe('pl-ru.degree.comparison.od-genitive');
    });

    describe('against the contract', () => {
      const { check, problem } = rowsIn(step, 'LifehackRow', 'SkillRow', nouns);
      const fields = (id: string): Record<string, string> => ({
        id: `"${pair}.${id}"`, skillId: `"${skill}"`, text: '"text"', source: '{kind: research citation: "a citation"}', status: 'editorial',
      });
      const tip = (id: string, change: Record<string, string | undefined> = {}) => row('LIFEHACK', fields(id), change);

      it('a tip for a skill with an address, a tip for no skill with a topic and every kind and status are accepted', () =>
        expect(check([
          tip('a', { source: '{kind: teaching_practice citation: "c" url: "https://doi.org/x"}' }), tip('b', { skillId: undefined, topic: '"word order"', source: '{kind: project_authored citation: "c"}', status: 'community' }),
        ].join('\n') + '\n')).toEqual({ status: 0, problems: [] }));
      it('a skill that is none is DATA_REF at the value', () => problem([tip('a', { skillId: '"no.such.skill"' })], 0, '"no.such.skill"', 'DATA_REF'));
      it('an id used twice is DATA_DUPLICATE at the second', () => problem([tip('a'), tip('a', { skillId: `"${other}"` })], 1, `"${pair}.a"`, 'DATA_DUPLICATE'));
      it('the old spelling of a kind (teaching-practice) is DATA_ENUM at the value', () => problem([tip('a', { source: '{kind: teaching-practice citation: "c"}' })], 0, 'teaching-practice', 'DATA_ENUM'));
      it('a status that is none (approved) is DATA_ENUM at the value', () => problem([tip('a', { status: 'approved' })], 0, 'approved', 'DATA_ENUM'));
      it('a source without its citation is DATA_REQUIRED at the { of the source', () => problem([tip('a', { source: '{kind: research}' })], 0, '{kind: research}', 'DATA_REQUIRED'));
      it('a vote count, which the old file carried as 0 and nothing read, is DATA_UNKNOWN_FIELD at its key', () => problem([tip('a', { votes: '{helpful: 7 notHelpful: 0}' })], 0, 'votes', 'DATA_UNKNOWN_FIELD'));
      it('a text that is a number is DATA_TYPE at the value', () => problem([tip('a', { text: '7' })], 0, '7', 'DATA_TYPE'));
      it('no row at all is DATA_COUNT at 1:1', () => expect(check('').problems).toEqual(['lifehacks.core-data:1:1: DATA_COUNT']));
    });
  });
}
