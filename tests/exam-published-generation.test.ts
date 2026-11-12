import { afterEach, describe, expect, it } from 'vitest';
import { cpSync, mkdtempSync, readFileSync, renameSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join } from 'node:path';
import { pathToFileURL } from 'node:url';
import { cli, hasCli, repo } from './support/course-data';
import { RECORD_TAGS, aggregate, discoverSources, validateSources } from '../dsl/exam-published/pipeline.mjs';

const temporary: string[] = [];
const paper = 'exams/pl/certyfikat-polski/B1/2017';
function fixture() {
  const root = mkdtempSync(join(tmpdir(), 'exam-published-'));
  temporary.push(root);
  const directory = join(root, paper);
  cpSync(join(repo, paper), directory, { recursive: true });
  const publication = join(root, 'exam-published.core-data');
  writeFileSync(publication, `DSL 1\nEXAM_PUBLICATION {\n  routeId: b1-2017\n  examRef: certyfikat-polski-B1-2017\n  displayTitle: "B1 Certyfikat 2017"\n  sourceDir: ${paper}\n}\n`);
  return { root, directory, publication };
}
afterEach(() => {
  for (const path of temporary.splice(0)) rmSync(path, { recursive: true, force: true });
});

describe.skipIf(!hasCli)('published exam source gate', () => {
  it('accepts either module filename and rejects both as duplicate source records', async () => {
    const { root, directory, publication } = fixture();
    renameSync(join(directory, 'modules.core-data'), join(directory, 'exam-modules.core-data'));
    expect(discoverSources(cli, directory).filter((source) => source.record === 'ExamModuleRow')).toHaveLength(1);
    cpSync(join(directory, 'exam-modules.core-data'), join(directory, 'modules.core-data'));
    await expect(validateSources(cli, { root, publication })).rejects.toThrow(/modules\.core-data:\d+:\d+: DATA_DUPLICATE/);
  }, 20_000);

  it('rejects missing source files and retains original diagnostics for invalid refs', async () => {
    const { root, directory, publication } = fixture();
    rmSync(join(directory, 'reading-questions.core-data'));
    await expect(validateSources(cli, { root, publication })).rejects.toThrow(/missing ReadingQuestionRow/);
    cpSync(join(repo, paper, 'reading-questions.core-data'), join(directory, 'reading-questions.core-data'));
    const file = join(directory, 'modules.core-data');
    writeFileSync(file, readFileSync(file, 'utf8').replace('examRef: certyfikat-polski-B1-2017', 'examRef: absent-paper'));
    await expect(validateSources(cli, { root, publication })).rejects.toThrow(/modules\.core-data:\d+:\d+: DATA_REF/);
    cpSync(join(repo, paper, 'modules.core-data'), file);
    const listening = join(directory, 'listening-tasks.core-data');
    writeFileSync(listening, readFileSync(listening, 'utf8').replace('exam-audio/B1-2017.mp3', 'exam-audio/undeclared.mp3'));
    await expect(validateSources(cli, { root, publication })).rejects.toThrow(/undeclared audioUrl/);
  }, 20_000);

  it('maps aggregate lines back to authored source lines deterministically', async () => {
    const { root, publication } = fixture();
    const papers = await validateSources(cli, { root, publication });
    const first = await aggregate(cli, papers, { outputDir: join(root, 'generated') });
    const second = await aggregate(cli, papers, { outputDir: join(root, 'generated') });
    expect(second).toEqual(first);
    const source = first.find(([path]) => path.endsWith('ExamModuleRow.provenance.json'))?.[1];
    expect(JSON.parse(source!)[0]).toMatchObject({ aggregateLine: 2, sourceLine: 2 });
  }, 30_000);

  it('rejects invalid cover order, duration and maximum at the original field line', async () => {
    const cases = [
      { from: 'order: 2', to: 'order: 1', field: 'order', message: 'duplicate module order' },
      { from: 'order: 2', to: 'order: 5', field: 'order', message: 'invalid module order' },
      { from: 'durationSeconds: 1500', to: 'durationSeconds: 0', field: 'durationSeconds', message: 'positive durationSeconds' },
      { from: 'maximumQuarter: 120', to: 'maximumQuarter: 0', field: 'maximumQuarter', message: 'positive maximumQuarter' },
    ];
    for (const fault of cases) {
      const { root, directory, publication } = fixture();
      const file = join(directory, 'modules.core-data');
      const source = readFileSync(file, 'utf8');
      const line = source.slice(0, source.indexOf(fault.from)).split('\n').length;
      writeFileSync(file, source.replace(fault.from, fault.to));
      await expect(validateSources(cli, { root, publication })).rejects.toThrow(
        new RegExp(`modules\\.core-data:${line}:\\d+: .*${fault.message}`),
      );
    }
  }, 60_000);

  it('preserves every source value and the six stable publication IDs and titles', async () => {
    const papers = await validateSources(cli);
    const reader = pathToFileURL(join(dirname(cli), 'support/data-reader.mjs')).href;
    const { readDataText } = await import(reader);
    expect(papers.map(({ publication }) => [publication.routeId, publication.displayTitle])).toEqual([
      ['b1-2017', 'B1 Certyfikat 2017'], ['b1-2019', 'B1 Certyfikat 2019'],
      ['b1-2020', 'B1 Certyfikat 2020'], ['b2-2017', 'B2 Certyfikat 2017'],
      ['b2-2019', 'B2 Certyfikat 2019'], ['b2-2020', 'B2 Certyfikat 2020'],
    ]);
    for (const record of RECORD_TAGS.keys()) {
      if (record === 'ExamPublicationRow') continue;
      const expected = papers.flatMap(({ sources }) => sources.filter((source) => source.record === record)
        .flatMap((source) => source.rows));
      const file = join(repo, 'dsl/exam-published/generated', `${record}.core-data`);
      const actual = readDataText(readFileSync(file, 'utf8'), file);
      expect(actual.diagnostics, record).toEqual([]);
      expect(actual.entries.map((entry: { tag: string; value: unknown }) => ({ tag: entry.tag, value: entry.value })), record)
        .toEqual(expected);
    }
  }, 45_000);
});
