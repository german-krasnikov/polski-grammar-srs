import { beforeAll, describe, expect, it } from 'vitest';
import { join } from 'node:path';
import { cli, hasCli, repo } from './support/course-data';
import { discoverSources, loadExamDataReader } from '../dsl/exam-published/pipeline.mjs';

// Independently transcribed from the 16 official arkusz writing-criteria pages.
const expected = [
  ['B1/2017', 'bogactwo językowe', 10, 30],
  ['B1/2019', 'środki językowe', 10, 30],
  ['B1/2020', 'środki językowe', 10, 30],
  ['B2/2017', 'bogactwo językowe', 16, 40],
  ['B2/2019', 'środki językowe', 16, 40],
  ['B2/2020', 'środki językowe', 16, 40],
  ['B2/2022-02', 'środki językowe', 16, 40],
  ['B2/2023-02', 'środki językowe', 16, 40],
  ['B2/2023-06', 'środki językowe', 16, 40],
  ['B2/2024-02', 'środki językowe', 16, 40],
  ['B2/2024-06', 'środki językowe', 16, 40],
  ['C1/2022-03', 'środki językowe', 18, 40],
  ['C1/2022-11', 'środki językowe', 18, 40],
  ['C1/2023-04', 'środki językowe', 18, 40],
  ['C1/2023-11', 'środki językowe', 18, 40],
  ['C1/2024-04', 'środki językowe', 18, 40],
] as const;

describe.skipIf(!hasCli)('official writing guidance source', () => {
  let reader: Awaited<ReturnType<typeof loadExamDataReader>>;
  beforeAll(async () => { reader = await loadExamDataReader(cli); });

  it('publishes one paper-specific rubric on each writing cover, without inline duplicates', () => {
    let taskCount = 0;
    for (const [paper, languageCategory, languageMaximum, total] of expected) {
      const sources = discoverSources(cli, join(repo, 'exams/pl/certyfikat-polski', paper), reader);
      const covers = sources.find((source) => source.record === 'ExamModuleRow')!.rows.map((row) => row.value);
      const writing = covers.find((cover) => cover.module === 'writing')!;
      expect(Number(writing.maximumQuarter), paper).toBe(paper.startsWith('B1/') ? 120 : 160);
      const guidance = String(writing.writingGuidance ?? '');
      expect(guidance.trim(), paper).not.toBe('');
      expect(guidance.toLocaleLowerCase('pl-PL'), paper).toContain(languageCategory);
      expect(guidance, paper).toMatch(/wykonanie zadania[^\n]*0\s*[–-]\s*10\s*p\./i);
      expect(guidance.toLocaleLowerCase('pl-PL'), paper)
        .toMatch(new RegExp(`${languageCategory}[^\\n]*0\\s*[–-]\\s*${languageMaximum}\\s*p\\.`));
      expect(guidance, paper).toMatch(new RegExp(`poprawność językowa[^\\n]*0\\s*[–-]\\s*${total - 10 - languageMaximum}\\s*p\\.`, 'i'));
      expect(guidance, paper).toMatch(new RegExp(`RAZEM[^\\n]*${total}\\s*p\\.`, 'i'));
      expect(covers.filter((cover) => cover.writingGuidance !== undefined), paper).toHaveLength(1);
      const tasks = sources.find((source) => source.record === 'WritingTaskRow')!.rows.map((row) => row.value);
      taskCount += tasks.length;
      for (const task of tasks) {
        expect(task.instruction, String(task.taskId)).not.toMatch(/KRYTERIA OCENY|ZASADY OCENIANIA ZADAŃ/);
      }
    }
    expect(taskCount).toBe(91);
  });

  it('keeps C1 task instructions free of duplicated cover criteria', () => {
    for (const paper of ['C1/2023-04', 'C1/2023-11']) {
      const sources = discoverSources(cli, join(repo, 'exams/pl/certyfikat-polski', paper), reader);
      const tasks = sources.find((source) => source.record === 'WritingTaskRow')!.rows.map((row) => row.value);
      expect(tasks, paper).toHaveLength(5);
      for (const task of tasks) expect(task.instruction, String(task.taskId)).not.toContain('KRYTERIA OCENY');
    }
  });
});
