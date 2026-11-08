#!/usr/bin/env node
// One-off converter: Certyfikat Polski PDF → .core-data rows.
// Usage: node dsl/exam-import/import-certyfikat.mjs --pdf <path> --level B1 --year 2023 --out exams/pl/certyfikat-polski/B1/2023/
// Requires: npm i -D pdf-parse
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { join, resolve } from 'node:path';

const args = process.argv.slice(2);
const flag = (name) => { const i = args.indexOf(`--${name}`); return i >= 0 ? args[i + 1] : undefined; };
const pdf = flag('pdf');
const level = flag('level') || 'B1';
const year = flag('year') || '2023';
const outDir = flag('out') || `exams/pl/certyfikat-polski/${level}/${year}/`;

if (!pdf) {
  console.error('Usage: node import-certyfikat.mjs --pdf <file.pdf> --level B1 --year 2023 --out <dir>');
  process.exit(1);
}

async function run() {
  const pdfParse = (await import('pdf-parse')).default;
  const buffer = readFileSync(resolve(pdf));
  const data = await pdfParse(buffer);
  const text = data.text;

  mkdirSync(outDir, { recursive: true });
  const examId = `certyfikat-polski-${level}-${year}`;

  // Exam metadata
  const examRows = [
    `DSL 1`,
    `EXAM {`,
    `  examId: "${examId}"`,
    `  provider: "certyfikat-polski"`,
    `  targetLanguage: "pl"`,
    `  level: "${level}"`,
    `  year: ${year}`,
    `  sourceUrl: "https://certyfikatpolski.pl/o-egzaminie/przykladowe-testy-zbiory-zadan/"`,
    `  license: "Państwowa Komisja ds. Poświadczania Znajomości Języka Polskiego jako Obcego"`,
    `}`,
  ];
  writeFileSync(join(outDir, 'exam.core-data'), examRows.join('\n') + '\n');
  console.log(`Wrote ${join(outDir, 'exam.core-data')}`);

  // Extract sections by markers
  const sections = {
    listening: extractBetween(text, 'ROZUMIENIE ZE SŁUCHU', 'CZYTANIE ZE ZROZUMIENIEM'),
    reading: extractBetween(text, 'CZYTANIE ZE ZROZUMIENIEM', 'UŻYCIE JĘZYKA'),
    grammar: extractBetween(text, 'UŻYCIE JĘZYKA', 'PISANIE'),
    writing: extractBetween(text, 'TEST 4. PISANIE', 'KLUCZ'),
  };

  // Parse grammar section (most structured)
  if (sections.grammar) {
    const grammarData = parseGrammar(sections.grammar, examId);
    writeFileSync(join(outDir, 'grammar.core-data'), grammarData);
    console.log(`Wrote ${join(outDir, 'grammar.core-data')} (grammar)`);
  }

  // Listening stub (needs manual audio timestamps)
  if (sections.listening) {
    const listeningData = parseListeningStub(sections.listening, examId);
    writeFileSync(join(outDir, 'listening.core-data'), listeningData);
    console.log(`Wrote ${join(outDir, 'listening.core-data')} (listening stub — add audio timestamps manually)`);
  }

  // Reading
  if (sections.reading) {
    const readingData = parseReadingStub(sections.reading, examId);
    writeFileSync(join(outDir, 'reading.core-data'), readingData);
    console.log(`Wrote ${join(outDir, 'reading.core-data')} (reading)`);
  }

  // Writing
  if (sections.writing) {
    const writingData = parseWritingStub(sections.writing, examId);
    writeFileSync(join(outDir, 'writing.core-data'), writingData);
    console.log(`Wrote ${join(outDir, 'writing.core-data')} (writing)`);
  }

  console.log('\nDone. Review each file, then: biome fmt <file> && biome generate --check');
}

function extractBetween(text, start, end) {
  const si = text.indexOf(start);
  const ei = end ? text.indexOf(end, si + 1) : text.length;
  return si >= 0 ? text.slice(si, ei > si ? ei : undefined) : '';
}

function parseGrammar(text, examId) {
  const lines = ['DSL 1'];
  // Find zadanie blocks (3.1, 3.2)
  const tasks = text.split(/Zadanie|3\.\d/).filter(Boolean);
  let testNum = 0;
  const taskMatches = [...text.matchAll(/3\.(\d)/g)];
  for (const m of taskMatches) {
    testNum++;
    const testId = `${examId}-grammar-${testNum}`;
    lines.push(`GRAMMAR_TEST {`);
    lines.push(`  testId: "${testId}"`);
    lines.push(`  examRef: "${examId}"`);
    lines.push(`  taskNumber: ${testNum}`);
    lines.push(`  instruction: "Uzupełnij luki"`);
    lines.push(`}`);
  }
  lines.push('# Items extracted from PDF — review and correct manually');
  return lines.join('\n') + '\n';
}

function parseListeningStub(text, examId) {
  const lines = ['DSL 1', '# Listening tasks — add segmentStartMs/segmentEndMs after checking audio'];
  for (let i = 1; i <= 2; i++) {
    const taskId = `${examId}-listening-${i}`;
    lines.push(`LISTENING_TASK {`);
    lines.push(`  taskId: "${taskId}"`);
    lines.push(`  examRef: "${examId}"`);
    lines.push(`  taskNumber: ${i}`);
    lines.push(`  audioUrl: "https://certyfikatpolski.pl/o-egzaminie/przykladowe-testy-zbiory-zadan/#audio-b1"`);
    lines.push(`  segmentStartMs: 0`);
    lines.push(`  segmentEndMs: 0`);
    lines.push(`  instruction: "Proszę uważnie słuchać nagrania"`);
    lines.push(`}`);
  }
  return lines.join('\n') + '\n';
}

function parseReadingStub(text, examId) {
  const lines = ['DSL 1', '# Reading passages — review extracted text'];
  for (let i = 1; i <= 3; i++) {
    lines.push(`READING_PASSAGE {`);
    lines.push(`  passageId: "${examId}-reading-${i}"`);
    lines.push(`  examRef: "${examId}"`);
    lines.push(`  taskNumber: ${i}`);
    lines.push(`  instruction: "Przeczytaj tekst i odpowiedz na pytania"`);
    lines.push(`  text: "TODO: extract from PDF"`);
    lines.push(`}`);
  }
  return lines.join('\n') + '\n';
}

function parseWritingStub(text, examId) {
  const lines = ['DSL 1'];
  const prompts = text.split(/4\.\d/).filter(s => s.trim().length > 20);
  let i = 0;
  for (const p of prompts.slice(0, 2)) {
    i++;
    lines.push(`WRITING_TASK {`);
    lines.push(`  taskId: "${examId}-writing-${i}"`);
    lines.push(`  examRef: "${examId}"`);
    lines.push(`  taskNumber: ${i}`);
    lines.push(`  instruction: "${p.trim().slice(0, 200).replace(/"/g, '\\"')}"`);
    lines.push(`  promptText: "${p.trim().slice(0, 200).replace(/"/g, '\\"')}"`);
    lines.push(`  wordLimit: 80`);
    lines.push(`  genre: letter`);
    lines.push(`}`);
  }
  return lines.join('\n') + '\n';
}

run().catch(e => { console.error(e); process.exit(1); });
