import { spawnSync } from 'node:child_process';
import { existsSync, readFileSync, readdirSync } from 'node:fs';
import { dirname, isAbsolute, join, relative, resolve, sep } from 'node:path';
import { pathToFileURL } from 'node:url';

const projectRoot = resolve(import.meta.dirname, '../..');
const publicationFile = join(projectRoot, 'dsl/exam-published.core-data');
const audioFile = join(projectRoot, 'dsl/exam-assets/exam-audio.core-data');
const contractFile = join(projectRoot, 'dsl/exam.data-contract');
const imageRoot = join(projectRoot, 'kotlin/composeApp/src/webMain/resources');

// The only structural table is Core's record-to-tag relationship, never a paper registry.
export const RECORD_TAGS = new Map(Object.entries({
  ExamPublicationRow: 'EXAM_PUBLICATION', ExamRow: 'EXAM', ExamModuleRow: 'EXAM_MODULE',
  ListeningTaskRow: 'LISTENING_TASK', ListeningQuestionRow: 'LISTENING_QUESTION',
  ListeningAnswerVariantRow: 'LISTENING_ANSWER_VARIANT', ReadingPassageRow: 'READING_PASSAGE',
  ListeningAcceptedFactRow: 'LISTENING_ACCEPTED_FACT', ListeningAcceptedFactAliasRow: 'LISTENING_ACCEPTED_FACT_ALIAS',
  ReadingQuestionRow: 'READING_QUESTION', ReadingAnswerVariantRow: 'READING_ANSWER_VARIANT',
  ReadingAcceptedFactRow: 'READING_ACCEPTED_FACT', ReadingAcceptedFactAliasRow: 'READING_ACCEPTED_FACT_ALIAS',
  GrammarTestRow: 'GRAMMAR_TEST', GrammarBankTokenRow: 'GRAMMAR_BANK_TOKEN',
  GrammarItemRow: 'GRAMMAR_ITEM', GrammarAnswerVariantRow: 'GRAMMAR_ANSWER_VARIANT',
  GrammarRubricComponentRow: 'GRAMMAR_RUBRIC_COMPONENT', GrammarAnswerPairRow: 'GRAMMAR_ANSWER_PAIR',
  WritingTaskRow: 'WRITING_TASK',
}));
const RECORD_BY_TAG = new Map([...RECORD_TAGS].map(([record, tag]) => [tag, record]));
const mandatory = new Set(['ExamRow', 'ExamModuleRow', 'ListeningTaskRow', 'ListeningQuestionRow',
  'ReadingPassageRow', 'ReadingQuestionRow', 'GrammarTestRow', 'GrammarItemRow', 'WritingTaskRow']);

function command(cli, args) {
  const run = spawnSync(process.execPath, [cli, ...args], {
    cwd: projectRoot, encoding: 'utf8', timeout: 120_000,
  });
  if (run.status !== 0) throw new Error(`biome ${args.slice(0, 2).join(' ')} failed: ${run.stderr || run.error?.message || run.status}`);
  return run.stdout;
}

function read(cli, file) {
  return JSON.parse(command(cli, ['read', file]));
}

function inside(root, path) {
  const rel = relative(root, path);
  return rel !== '' && rel !== '..' && !rel.startsWith(`..${sep}`) && !isAbsolute(rel);
}

async function locatedReader(cli) {
  const reader = pathToFileURL(join(dirname(cli), 'support/data-reader.mjs')).href;
  return (await import(reader)).readDataText; // Documented Biome JS extension seam.
}

function sourceFieldError(file, entry, field, message) {
  const span = entry.root.entries[field]?.value.at;
  const line = entry.origin.line + (span?.line ?? 1) - 1;
  const column = (span?.line ?? 1) === 1
    ? entry.origin.column + (span?.column ?? 1) - 1
    : span.column;
  throw new Error(`${file}:${line}:${column}: ${message}`);
}

/** Find source records by the tag returned by the supported Biome reader. */
export function discoverSources(cli, directory) {
  if (!existsSync(directory)) throw new Error(`${directory}: missing source directory`);
  const paths = readdirSync(directory).filter((name) => name.endsWith('.core-data')).sort();
  if (!paths.length) throw new Error(`${directory}: no .core-data files`);
  const sources = [];
  for (const name of paths) {
    const file = join(directory, name);
    const rows = read(cli, file);
    if (!rows.length) throw new Error(`${file}: empty source file`);
    const records = new Set(rows.map(({ tag }) => RECORD_BY_TAG.get(tag)));
    if (records.has(undefined) || records.size !== 1) throw new Error(`${file}: unknown or mixed exam record tags`);
    sources.push({ file, record: [...records][0], rows });
  }
  for (const record of mandatory) {
    if (!sources.some((source) => source.record === record)) throw new Error(`${directory}: missing ${record}`);
  }
  return sources;
}

const answerKey = (value) => value.normalize('NFC').toLocaleLowerCase('pl-PL').trim()
  .replace(/[.!?]+$/, '').replace(/\s+/gu, ' ');

/** Arithmetic and cross-row checks that Core's record grammar cannot express. */
export function validateForwardSourceRecords(sources) {
  const sourceOf = (record) => sources.find((source) => source.record === record);
  const rowsOf = (record) => sourceOf(record)?.rows.map(({ value }) => value) ?? [];
  const fail = (record, key, id, message) => {
    const file = sourceOf(record).file;
    const line = readFileSync(file, 'utf8').split('\n').findIndex((text) => text.trim() === `${key}: ${id}`) + 1;
    throw new Error(`${file}:${line || 1}:1: ${message}`);
  };
  const items = new Map(rowsOf('GrammarItemRow').map((item) => [item.itemId, item]));
  const questions = new Map(rowsOf('ListeningQuestionRow').map((question) => [question.questionId, question]));
  const partner = new Map(), tuples = new Set(), firstItems = new Set(), secondItems = new Set();
  for (const pair of rowsOf('GrammarAnswerPairRow')) {
    const first = items.get(pair.firstItemRef), second = items.get(pair.secondItemRef);
    if (!first || !second || first.itemId === second.itemId || first.testRef !== second.testRef) {
      fail('GrammarAnswerPairRow', 'pairId', pair.pairId, `invalid pair items in ${pair.pairId}`);
    }
    if ((partner.has(first.itemId) && partner.get(first.itemId) !== second.itemId) ||
        (partner.has(second.itemId) && partner.get(second.itemId) !== first.itemId) ||
        secondItems.has(first.itemId) || firstItems.has(second.itemId)) {
      fail('GrammarAnswerPairRow', 'pairId', pair.pairId, `inconsistent pair partner in ${pair.pairId}`);
    }
    firstItems.add(first.itemId); secondItems.add(second.itemId);
    partner.set(first.itemId, second.itemId); partner.set(second.itemId, first.itemId);
    const firstAnswer = answerKey(pair.firstAnswer), secondAnswer = answerKey(pair.secondAnswer);
    const tuple = `${first.itemId}\u0000${second.itemId}\u0000${firstAnswer}\u0000${secondAnswer}`;
    if (!firstAnswer || !secondAnswer || tuples.has(tuple) ||
        pair.firstEarnedQuarter < 1 || pair.firstEarnedQuarter > (first.pointsQuarter ?? first.points * 4) ||
        pair.secondEarnedQuarter < 1 || pair.secondEarnedQuarter > (second.pointsQuarter ?? second.points * 4)) {
      fail('GrammarAnswerPairRow', 'pairId', pair.pairId, `invalid pair answer or credit in ${pair.pairId}`);
    }
    tuples.add(tuple);
  }
  const rubricByItem = new Map();
  for (const component of rowsOf('GrammarRubricComponentRow')) {
    const group = rubricByItem.get(component.itemRef) ?? [];
    group.push(component);
    rubricByItem.set(component.itemRef, group);
  }
  for (const [itemId, components] of rubricByItem) {
    const item = items.get(itemId);
    if (!item || components.some((component) => component.order < 1 || component.creditQuarter < 1 || !component.criterionText.trim()) ||
        new Set(components.map((component) => component.order)).size !== components.length ||
        components.reduce((sum, component) => sum + component.creditQuarter, 0) !== (item.pointsQuarter ?? item.points * 4)) {
      fail('GrammarRubricComponentRow', 'itemRef', itemId, `invalid rubric credit or order for ${itemId}`);
    }
  }
  const factById = new Map(), acceptedByQuestion = new Map();
  for (const fact of rowsOf('ListeningAcceptedFactRow')) {
    const question = questions.get(fact.questionRef);
    const maximum = question?.pointsQuarter ?? (question?.points ?? 0) * 4;
    if (!question || question.answerMode !== 'unordered_set' || question.requiredSelections == null ||
        fact.creditQuarter !== question.creditPerCorrectQuarter || fact.creditQuarter < 1 ||
        question.requiredSelections * fact.creditQuarter !== maximum || !answerKey(fact.displayText)) {
      fail('ListeningAcceptedFactRow', 'factId', fact.factId, `invalid accepted fact ${fact.factId}`);
    }
    factById.set(fact.factId, fact);
    const accepted = acceptedByQuestion.get(fact.questionRef) ?? new Map();
    const key = answerKey(fact.displayText);
    if (accepted.has(key)) fail('ListeningAcceptedFactRow', 'factId', fact.factId, `duplicate fact wording ${key}`);
    accepted.set(key, fact.factId); acceptedByQuestion.set(fact.questionRef, accepted);
  }
  for (const question of questions.values()) {
    if (question.answerMode === 'unordered_set' && question.gradingMode === 'open_manual' &&
        question.requiredSelections != null && !acceptedByQuestion.has(question.questionId)) {
      fail('ListeningQuestionRow', 'questionId', question.questionId, `source set ${question.questionId} needs accepted facts`);
    }
    const accepted = acceptedByQuestion.get(question.questionId);
    if (accepted && new Set(accepted.values()).size < question.requiredSelections) {
      fail('ListeningQuestionRow', 'questionId', question.questionId, `insufficient accepted facts for ${question.questionId}`);
    }
  }
  for (const alias of rowsOf('ListeningAcceptedFactAliasRow')) {
    const fact = factById.get(alias.factRef);
    const accepted = fact && acceptedByQuestion.get(fact.questionRef);
    const key = answerKey(alias.answerText);
    if (!accepted || !key || accepted.has(key)) {
      fail('ListeningAcceptedFactAliasRow', 'aliasId', alias.aliasId, `duplicate or invalid fact alias ${alias.aliasId}`);
    }
    accepted.set(key, fact.factId);
  }
}

/** Source check precedes aggregation and keeps Core diagnostics at the author's file/line. */
export async function validateSources(cli, { root = projectRoot, publication = publicationFile } = {}) {
  const readDataText = await locatedReader(cli);
  const published = read(cli, publication);
  if (!published.length || published.some(({ tag }) => tag !== RECORD_TAGS.get('ExamPublicationRow'))) {
    throw new Error(`${publication}: expected EXAM_PUBLICATION rows`);
  }
  const audio = read(cli, audioFile).map(({ value }) => value);
  const audioByExam = new Map(audio.map((asset) => [asset.examId, asset.webPath]));
  const seenRoute = new Set(), seenExam = new Set(), seenDir = new Set();
  const papers = [];
  for (const { value: publicationRow } of published) {
    const { routeId, examRef, sourceDir, displayTitle } = publicationRow;
    if (!routeId || !examRef || !sourceDir || !displayTitle) throw new Error(`${publication}: incomplete publication`);
    if (seenRoute.has(routeId) || seenExam.has(examRef) || seenDir.has(sourceDir)) {
      throw new Error(`${publication}: duplicate routeId, examRef or sourceDir for ${routeId}`);
    }
    seenRoute.add(routeId); seenExam.add(examRef); seenDir.add(sourceDir);
    const directory = resolve(root, sourceDir);
    if (!inside(join(root, 'exams'), directory)) throw new Error(`${publication}: sourceDir escapes exams: ${sourceDir}`);
    const sources = discoverSources(cli, directory);
    validateForwardSourceRecords(sources);
    command(cli, ['data', 'check', '--contract', contractFile,
      ...sources.flatMap(({ record, file }) => ['--rows', record, file])]);
    const byRecord = new Map();
    for (const source of sources) {
      if (byRecord.has(source.record)) {
        throw new Error(`${source.file}: duplicate ${source.record} source record`);
      }
      byRecord.set(source.record, source.file);
    }
    const exams = sources.flatMap(({ record, rows }) => record === 'ExamRow' ? rows : []);
    if (exams.length !== 1 || exams[0].value.examId !== examRef) {
      throw new Error(`${directory}: publication examRef ${examRef} does not match its one ExamRow`);
    }
    const moduleSource = sources.find(({ record }) => record === 'ExamModuleRow');
    const covers = moduleSource
      ? readDataText(readFileSync(moduleSource.file, 'utf8'), moduleSource.file, { tag: 'EXAM_MODULE' }).entries
      : [];
    const modules = covers.map(({ value }) => value.module);
    if (covers.length !== 4 || new Set(modules).size !== 4 ||
        !['listening', 'reading', 'grammar', 'writing'].every((module) => modules.includes(module)) ||
        covers.some(({ value }) => value.examRef !== examRef)) {
      throw new Error(`${directory}: incomplete or mismatched four-module cover`);
    }
    const orders = new Set();
    for (const cover of covers) {
      const { order, durationSeconds, maximumQuarter } = cover.value;
      if (!Number.isInteger(order) || order < 1 || order > 4) {
        sourceFieldError(moduleSource.file, cover, 'order', 'invalid module order; expected 1..4');
      }
      if (orders.has(order)) sourceFieldError(moduleSource.file, cover, 'order', 'duplicate module order');
      orders.add(order);
      if (durationSeconds <= 0) sourceFieldError(moduleSource.file, cover, 'durationSeconds', 'expected positive durationSeconds');
      if (maximumQuarter <= 0) sourceFieldError(moduleSource.file, cover, 'maximumQuarter', 'expected positive maximumQuarter');
    }
    for (const { record, rows, file } of sources) {
      if (record === 'ListeningTaskRow') for (const row of rows) {
        if (row.value.examRef !== examRef || audioByExam.get(examRef) !== row.value.audioUrl) {
          throw new Error(`${file}: undeclared audioUrl for ${examRef}: ${row.value.audioUrl}`);
        }
      }
      if (record === 'ListeningTaskRow' || record === 'WritingTaskRow') for (const row of rows) {
        const image = row.value.promptImageUrl;
        if (image && (!inside(imageRoot, resolve(imageRoot, image)) || !existsSync(resolve(imageRoot, image)))) {
          throw new Error(`${file}: missing prompt image ${image}`);
        }
      }
    }
    command(cli, ['fmt', '--check', ...sources.map(({ file }) => file)]);
    papers.push({ publication: publicationRow, sources });
  }
  // The publication ref is validated against all original ExamRow sources together.
  command(cli, ['data', 'check', '--contract', contractFile, '--rows', 'ExamPublicationRow', publication,
    ...papers.flatMap(({ sources }) => sources.filter(({ record }) => record === 'ExamRow')
      .flatMap(({ record, file }) => ['--rows', record, file]))]);
  command(cli, ['fmt', '--check', publication]);
  return papers;
}

/** Preserve authored row slices and a map from every aggregate line to its original file/line. */
export async function aggregate(cli, papers, { outputDir = join(projectRoot, 'dsl/exam-published/generated') } = {}) {
  const readDataText = await locatedReader(cli);
  const result = [];
  for (const [record, tag] of RECORD_TAGS) {
    const files = record === 'ExamPublicationRow' ? [publicationFile] : papers.flatMap(({ sources }) =>
      sources.filter((source) => source.record === record).map((source) => source.file));
    const output = join(outputDir, `${record}.core-data`);
    const lines = ['DSL 1'];
    const provenance = [];
    for (const file of files) {
      const { entries, diagnostics } = readDataText(readFileSync(file, 'utf8'), file, { tag });
      if (diagnostics.length) throw new Error(`${file}: ${diagnostics.map((d) => d.message).join('; ')}`);
      for (const entry of entries) {
        const fragment = `${entry.tag} ${entry.text}`.split('\n');
        for (let i = 0; i < fragment.length; i++) {
          lines.push(fragment[i]);
          provenance.push({ aggregateLine: lines.length, source: relative(projectRoot, file), sourceLine: entry.origin.line + i });
        }
      }
    }
    result.push([output, `${lines.join('\n')}\n`]);
    result.push([join(outputDir, `${record}.provenance.json`), `${JSON.stringify(provenance, null, 2)}\n`]);
  }
  return result;
}
