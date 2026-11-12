import { afterEach, beforeAll, describe, expect, it } from 'vitest';
import { cpSync, mkdtempSync, readFileSync, renameSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join } from 'node:path';
import { pathToFileURL } from 'node:url';
import { cli, hasCli, repo } from './support/course-data';
import { RECORD_TAGS, aggregate, discoverSources, loadExamDataReader, validateForwardSourceRecords, validateSources } from '../dsl/exam-published/pipeline.mjs';

const temporary: string[] = [];
const paper = 'exams/pl/certyfikat-polski/B1/2017';
function fixture() {
  const root = mkdtempSync(join(tmpdir(), 'exam-published-'));
  temporary.push(root);
  const directory = join(root, paper);
  cpSync(join(repo, paper), directory, { recursive: true });
  const publication = join(root, 'exam-published.core-data');
  writeFileSync(publication, `DSL 1\nEXAM_PUBLICATION {\n  routeId: b1-2017\n  examRef: certyfikat-polski-B1-2017\n  displayTitle: "B1 Certyfikat 2017"\n  sourceDir: ${paper}\n}\n`);
  const audioManifest = join(root, 'exam-audio.core-data');
  writeFileSync(audioManifest, 'DSL 1\nEXAM_AUDIO_ASSET { examId: certyfikat-polski-B1-2017 webPath: exam-audio/B1-2017.mp3 }\n');
  return { root, directory, publication, audioManifest };
}
afterEach(() => {
  for (const path of temporary.splice(0)) rmSync(path, { recursive: true, force: true });
});

describe.skipIf(!hasCli)('published exam source gate', () => {
  let sourceReader: Awaited<ReturnType<typeof loadExamDataReader>>;
  beforeAll(async () => { sourceReader = await loadExamDataReader(cli); });

  it('discovers forward grammar pair and rubric records in authored source', () => {
    const directory = join(repo, 'exams/pl/certyfikat-polski/C1/2024-04');
    expect(discoverSources(cli, directory, sourceReader).find((source) => source.record === 'GrammarAnswerPairRow')?.rows).toHaveLength(3);
    const november = join(repo, 'exams/pl/certyfikat-polski/C1/2022-11');
    expect(discoverSources(cli, november, sourceReader).find((source) => source.record === 'GrammarRubricComponentRow')?.rows).toHaveLength(20);
  }, 20_000);

  it('rejects a mismatched pair partner and incomplete component credits', () => {
    const april = discoverSources(cli, join(repo, 'exams/pl/certyfikat-polski/C1/2024-04'), sourceReader);
    validateForwardSourceRecords(april);
    const pairs = april.find((source) => source.record === 'GrammarAnswerPairRow')!;
    pairs.rows[1].value.secondItemRef = 'certyfikat-polski-C1-2024-04-g1-q7';
    expect(() => validateForwardSourceRecords(april)).toThrow(/grammar-answer-pairs\.core-data:\d+:1: inconsistent pair partner/);
    const november = discoverSources(cli, join(repo, 'exams/pl/certyfikat-polski/C1/2022-11'), sourceReader);
    validateForwardSourceRecords(november);
    const rubrics = november.find((source) => source.record === 'GrammarRubricComponentRow')!;
    rubrics.rows[0].value.creditQuarter = 2;
    expect(() => validateForwardSourceRecords(november)).toThrow(/grammar-rubric-components\.core-data:\d+:1: invalid rubric credit/);
  }, 20_000);

  it('retains explicit identities for every P9 open listening set', () => {
    const papers = ['B2/2022-02', 'B2/2022-06', 'B2/2023-02', 'B2/2023-06',
      'B2/2024-06', 'C1/2022-11', 'C1/2023-04', 'C1/2023-11'];
    let count = 0;
    for (const paper of papers) {
      const sources = discoverSources(cli, join(repo, 'exams/pl/certyfikat-polski', paper), sourceReader);
      validateForwardSourceRecords(sources);
      const factIds = new Set(sources.filter((source) => source.record === 'ListeningAcceptedFactRow')
        .flatMap((source) => source.rows.map((row) => row.value.questionRef)));
      const questions = sources.filter((source) => source.record === 'ListeningQuestionRow')
        .flatMap((source) => source.rows.map((row) => row.value));
      for (const question of questions) {
        if (question.answerMode !== 'unordered_set' || question.gradingMode !== 'open_manual') continue;
        expect(factIds.has(question.questionId), String(question.questionId)).toBe(true);
        count++;
      }
    }
    expect(count).toBe(19);
  }, 60_000);

  it('accepts either module filename and rejects both as duplicate source records', async () => {
    const { root, directory, publication, audioManifest } = fixture();
    renameSync(join(directory, 'modules.core-data'), join(directory, 'exam-modules.core-data'));
    expect(discoverSources(cli, directory, sourceReader).filter((source) => source.record === 'ExamModuleRow')).toHaveLength(1);
    cpSync(join(directory, 'exam-modules.core-data'), join(directory, 'modules.core-data'));
    await expect(validateSources(cli, { root, publication, audioManifest })).rejects.toThrow(/modules\.core-data:\d+:\d+: DATA_DUPLICATE/);
  }, 20_000);

  it('rejects missing source files and retains original diagnostics for invalid refs', async () => {
    const { root, directory, publication, audioManifest } = fixture();
    rmSync(join(directory, 'reading-questions.core-data'));
    await expect(validateSources(cli, { root, publication, audioManifest })).rejects.toThrow(/missing ReadingQuestionRow/);
    cpSync(join(repo, paper, 'reading-questions.core-data'), join(directory, 'reading-questions.core-data'));
    const file = join(directory, 'modules.core-data');
    writeFileSync(file, readFileSync(file, 'utf8').replace('examRef: certyfikat-polski-B1-2017', 'examRef: absent-paper'));
    await expect(validateSources(cli, { root, publication, audioManifest })).rejects.toThrow(/modules\.core-data:\d+:\d+: DATA_REF/);
    cpSync(join(repo, paper, 'modules.core-data'), file);
    const listening = join(directory, 'listening-tasks.core-data');
    writeFileSync(listening, readFileSync(listening, 'utf8').replace('exam-audio/B1-2017.mp3', 'exam-audio/undeclared.mp3'));
    await expect(validateSources(cli, { root, publication, audioManifest })).rejects.toThrow(/undeclared audioUrl/);
  }, 20_000);

  it('requires exact one-to-one coverage between published papers and audio assets', async () => {
    const { root, publication, audioManifest } = fixture();
    writeFileSync(audioManifest, 'DSL 1\nEXAM_AUDIO_ASSET { examId: extra-paper webPath: exam-audio/extra.mp3 }\n');
    await expect(validateSources(cli, { root, publication, audioManifest })).rejects.toThrow(/audio.*coverage|missing audio/i);
    writeFileSync(audioManifest, 'DSL 1\nEXAM_AUDIO_ASSET { examId: certyfikat-polski-B1-2017 webPath: exam-audio/B1-2017.mp3 }\nEXAM_AUDIO_ASSET { examId: extra-paper webPath: exam-audio/extra.mp3 }\n');
    await expect(validateSources(cli, { root, publication, audioManifest })).rejects.toThrow(/audio.*coverage|surplus audio/i);
  }, 20_000);

  it('keeps Core reference validation scoped to each paper', async () => {
    const { root, directory, publication, audioManifest } = fixture();
    const second = 'exams/pl/certyfikat-polski/B1/2019';
    cpSync(join(repo, second), join(root, second), { recursive: true });
    writeFileSync(publication, readFileSync(publication, 'utf8') +
      `EXAM_PUBLICATION { routeId: b1-2019 examRef: certyfikat-polski-B1-2019 displayTitle: "B1 Certyfikat 2019" sourceDir: ${second} }\n`);
    writeFileSync(audioManifest, readFileSync(audioManifest, 'utf8') +
      'EXAM_AUDIO_ASSET { examId: certyfikat-polski-B1-2019 webPath: exam-audio/B1-2019.mp3 }\n');
    const questions = join(directory, 'reading-questions.core-data');
    writeFileSync(questions, readFileSync(questions, 'utf8').replace(
      'passageRef: certyfikat-polski-B1-2017-read-1', 'passageRef: B1-2019-read-1'));
    await expect(validateSources(cli, { root, publication, audioManifest }))
      .rejects.toThrow(/reading-questions\.core-data:\d+:\d+: DATA_REF/);
  }, 30_000);

  it('maps aggregate lines back to authored source lines deterministically', async () => {
    const { root, publication, audioManifest } = fixture();
    const papers = await validateSources(cli, { root, publication, audioManifest });
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
      const { root, directory, publication, audioManifest } = fixture();
      const file = join(directory, 'modules.core-data');
      const source = readFileSync(file, 'utf8');
      const line = source.slice(0, source.indexOf(fault.from)).split('\n').length;
      writeFileSync(file, source.replace(fault.from, fault.to));
      await expect(validateSources(cli, { root, publication, audioManifest })).rejects.toThrow(
        new RegExp(`modules\\.core-data:${line}:\\d+: .*${fault.message}`),
      );
    }
  }, 60_000);

  it('preserves every source value, six stable routes and ten dated P9 routes', async () => {
    const papers = await validateSources(cli);
    const reader = pathToFileURL(join(dirname(cli), 'support/data-reader.mjs')).href;
    const { readDataText } = await import(reader);
    expect(papers.map(({ publication }) => [publication.routeId, publication.displayTitle])).toEqual([
      ['b1-2017', 'B1 Certyfikat 2017'], ['b1-2019', 'B1 Certyfikat 2019'],
      ['b1-2020', 'B1 Certyfikat 2020'], ['b2-2017', 'B2 Certyfikat 2017'],
      ['b2-2019', 'B2 Certyfikat 2019'], ['b2-2020', 'B2 Certyfikat 2020'],
      ['b2-2022-02', 'B2 Certyfikat 2022-02'], ['b2-2023-02', 'B2 Certyfikat 2023-02'],
      ['b2-2023-06', 'B2 Certyfikat 2023-06'], ['b2-2024-02', 'B2 Certyfikat 2024-02'],
      ['b2-2024-06', 'B2 Certyfikat 2024-06'], ['c1-2022-03', 'C1 Certyfikat 2022-03'],
      ['c1-2022-11', 'C1 Certyfikat 2022-11'], ['c1-2023-04', 'C1 Certyfikat 2023-04'],
      ['c1-2023-11', 'C1 Certyfikat 2023-11'], ['c1-2024-04', 'C1 Certyfikat 2024-04'],
    ]);
    expect(papers.map(({ publication }) => publication.routeId)).not.toContain('b2-2022-06');
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
  }, 180_000);
});
