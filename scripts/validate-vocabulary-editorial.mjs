import { createHash } from 'node:crypto';
import { readFileSync } from 'node:fs';
import { pathToFileURL } from 'node:url';
import Ajv2020 from 'ajv/dist/2020.js';

const root = new URL('../', import.meta.url);
const readJson = path => JSON.parse(readFileSync(new URL(path, root), 'utf8'));
const schema = readJson('courses/schema/vocabulary-editorial-v1.schema.json');
const baseline = readJson('tests/fixtures/vocabulary-baseline-32.json');
const validateSchema = new Ajv2020({ allErrors: true, strict: true }).compile(schema);
// pl-ru's pinned frequencySource values (default expectation, kept for backward compatibility).
const plSource = {
  repository: 'https://github.com/KubaCiolo/leksjo-dane',
  csvUrl: 'https://github.com/KubaCiolo/leksjo-dane/blob/01782aa92cc842d0d3199079eba47ecbf05879e1/dane/nkjp-frekwencja.csv',
  revision: '01782aa92cc842d0d3199079eba47ecbf05879e1',
  csvSha256: '4cd43eb6acb22fe19c83cdcdd408dc8519f0b583627ee3f00f31079871a208ca',
  jsonSha256: '4549d27b90ec4bb3e1ca9db55fcdd2256b91e28a25f0474cbd55f6a94e2a9a70',
};
const baselineById = new Map(baseline.cards.map(card => [card.id, card]));

/**
 * Checks authoring provenance and pack agreement; strict mode is the V2 release gate.
 * `course` is nullable: pass null/undefined to validate an editorial journal on its own,
 * before its pack's pair.json exists yet or wires any card into `vocabulary.items` (the
 * authoring-ahead-of-wiring stage every pack's editorial journal goes through first).
 * `expectedFrequencySource` pins known-good frequencySource values for one pack; it
 * defaults to pl-ru's so existing callers are unaffected — a second pack passes its own.
 */
export function validateVocabularyEditorial(course, frequency, journal, { strict = false, frequencyBytes, expectedFrequencySource = plSource } = {}) {
  if (!validateSchema(journal)) {
    const error = validateSchema.errors[0];
    const suffix = error.keyword === 'required' ? `/${error.params.missingProperty}` : '';
    throw new Error(`editorial${error.instancePath}${suffix}: ${error.message}`);
  }
  for (const [key, value] of Object.entries(expectedFrequencySource)) {
    if (journal.frequencySource[key] !== value) throw new Error(`frequencySource/${key}: source revision or hash drift`);
  }
  if (frequencyBytes) {
    const digest = createHash('sha256').update(frequencyBytes).digest('hex');
    if (digest !== expectedFrequencySource.jsonSha256) throw new Error('frequencySource/jsonSha256: local index drift');
  }
  const byId = new Map();
  const byLemma = new Set();
  const occupiedRanks = new Set();
  journal.cards.forEach((card, index) => {
    const path = `cards/${index}`;
    if (byId.has(card.id)) throw new Error(`${path}/id: duplicate id ${card.id}`);
    if (byLemma.has(card.lemma)) throw new Error(`${path}/lemma: duplicate selectable lemma ${card.lemma}`);
    byId.set(card.id, card);
    byLemma.add(card.lemma);
    if (card.rank !== null) {
      if (frequency.items[card.rank - 1]?.lemma !== card.lemma) throw new Error(`${path}/rank: lemma mismatch`);
      if (occupiedRanks.has(card.rank)) throw new Error(`${path}/rank: duplicate rank`);
      occupiedRanks.add(card.rank);
    }
    for (const [field, provenance] of Object.entries(card.provenance)) {
      if (provenance.kind === 'external' && /^(unknown|unlicensed|tbd|none)$/i.test(provenance.license.trim())) {
        throw new Error(`${path}/provenance/${field}/license: unresolved external license`);
      }
    }
    if (card.status === 'approved') {
      if (!card.reviewer?.trim() || !card.reviewedAt || Number.isNaN(Date.parse(card.reviewedAt))) {
        throw new Error(`${path}/reviewer: approved card needs reviewer and review date`);
      }
      if (!card.reviewSources.length || /pending/i.test(card.levelDecision)) {
        throw new Error(`${path}/reviewSources: approved card needs lexical review evidence and a level decision`);
      }
    } else if (strict) {
      throw new Error(`${path}/status: needs-review card is in runtime pack`);
    } else if (!baselineById.has(card.id)) {
      throw new Error(`${path}/status: only legacy cards may await independent review`);
    }
  });
  if (course) {
    const runtimeIds = new Set();
    course.vocabulary.items.forEach((item, index) => {
      if (runtimeIds.has(item.id)) throw new Error(`vocabulary/items/${index}/id: duplicate id`);
      runtimeIds.add(item.id);
      const card = byId.get(item.id);
      if (!card) throw new Error(`vocabulary/items/${index}/id: missing editorial card`);
      for (const [field, editorialField] of [['lemma', 'lemma'], ['frequencyRank', 'rank'],
        ['translation', 'translation'], ['form', 'form'], ['example', 'example'], ['level', 'level']]) {
        if (item[field] !== card[editorialField]) throw new Error(`cards/${journal.cards.indexOf(card)}/${editorialField}: runtime mismatch`);
      }
    });
    for (const card of journal.cards) {
      if (!runtimeIds.has(card.id)) throw new Error(`cards/${journal.cards.indexOf(card)}/id: stale editorial card`);
    }
  }
  const candidateRanks = new Set();
  journal.candidates.forEach((candidate, index) => {
    if (frequency.items[candidate.rank - 1]?.lemma !== candidate.lemma) {
      throw new Error(`candidates/${index}/lemma: frequency mismatch`);
    }
    if (occupiedRanks.has(candidate.rank) || candidateRanks.has(candidate.rank)) {
      throw new Error(`candidates/${index}/rank: duplicate or promoted rank`);
    }
    candidateRanks.add(candidate.rank);
  });
  if (occupiedRanks.size + candidateRanks.size !== frequency.items.length) {
    throw new Error('candidates: every unpromoted frequency rank needs an editorial queue record');
  }
  return true;
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  try {
    const frequencyBytes = readFileSync(new URL('courses/pl-ru/frequency-top1000.json', root));
    validateVocabularyEditorial(
      readJson('courses/pl-ru/course.json'), JSON.parse(frequencyBytes),
      readJson('courses/pl-ru/vocabulary-editorial.json'),
      { strict: process.argv.includes('--strict'), frequencyBytes },
    );
    process.stdout.write('PASS vocabulary editorial journal\n');
  } catch (error) {
    process.stderr.write(`FAIL vocabulary editorial journal: ${error.message}\n`);
    process.exitCode = 1;
  }
}
