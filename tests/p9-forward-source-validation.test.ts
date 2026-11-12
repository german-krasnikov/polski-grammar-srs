import { describe, expect, it } from 'vitest';
import { join } from 'node:path';
import { cli, hasCli, repo } from './support/course-data';
import { discoverSources, validateForwardSourceRecords } from '../dsl/exam-published/pipeline.mjs';

describe.skipIf(!hasCli)('P9 forward source completeness', () => {
  it('rejects fewer distinct facts than the printed answer slots at the source question', () => {
    const sources = discoverSources(cli, join(repo, 'exams/pl/certyfikat-polski/C1/2023-04'));
    const facts = sources.find((source) => source.record === 'ListeningAcceptedFactRow')!;
    facts.rows = facts.rows.filter((row) => !String(row.value.factId).endsWith('l5-q2-fact-2'));

    expect(() => validateForwardSourceRecords(sources)).toThrow(
      /listening-questions\.core-data:\d+:1: .*insufficient accepted facts.*l5-q2/,
    );
  }, 20_000);

  it('rejects an alias that collides with a canonical fact at the alias source line', () => {
    const sources = discoverSources(cli, join(repo, 'exams/pl/certyfikat-polski/C1/2022-11'));
    const aliases = sources.find((source) => source.record === 'ListeningAcceptedFactAliasRow')!;
    aliases.rows[0].value.answerText = 'skarbu';

    expect(() => validateForwardSourceRecords(sources)).toThrow(
      /listening-accepted-fact-aliases\.core-data:\d+:1: duplicate or invalid fact alias/,
    );
  }, 20_000);

  it('rejects a fact credit that would exceed the printed maximum at the fact source line', () => {
    const sources = discoverSources(cli, join(repo, 'exams/pl/certyfikat-polski/C1/2023-04'));
    const facts = sources.find((source) => source.record === 'ListeningAcceptedFactRow')!;
    const fact = facts.rows.find((row) => String(row.value.factId).endsWith('l5-q2-fact-1'))!;
    fact.value.creditQuarter = 4;

    expect(() => validateForwardSourceRecords(sources)).toThrow(
      /listening-accepted-facts\.core-data:\d+:1: invalid accepted fact/,
    );
  }, 20_000);
});
