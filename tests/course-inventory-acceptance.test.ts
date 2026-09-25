import { createHash } from 'node:crypto';
import { readFileSync } from 'node:fs';
import { expect, test } from 'vitest';
import { buildCandidates, checkInventory } from '../scripts/check-course-inventory.mjs';

const sha256 = (value: string) => createHash('sha256').update(value).digest('hex');

function sample(source = 'const caption = "UI label";') {
  const path = 'src/ui/Synthetic.tsx';
  const inventory = {
    schemaVersion: 1,
    files: [{ path, sha256: sha256(source), literalLines: [{ line: 1, source: source.trim() }] }],
  };
  const sourceTextByPath = new Map([[path, source]]);
  const candidate = buildCandidates(inventory, sourceTextByPath)[0];
  const decision = {
    path: candidate.path, symbol: candidate.symbol,
    sourceFingerprint: candidate.sourceFingerprint, occurrence: candidate.occurrence,
    category: 'ui-localization-chrome', reason: 'Synthetic label for checker acceptance',
  };
  const course = { reference: { title: 'Pack-authored title' } };
  return { inventory, sourceTextByPath, decision, course };
}

test('coverage rejects unknown, duplicated, stale and drifted decisions', () => {
  const fixture = sample();
  const base = { inventory: fixture.inventory, course: fixture.course, sourceTextByPath: fixture.sourceTextByPath };
  expect(checkInventory({ ...base, decisions: { schemaVersion: 1, decisions: [fixture.decision] } }))
    .toMatchObject({ candidateCount: 1, tokenCount: 1 });
  expect(() => checkInventory({ ...base, decisions: { schemaVersion: 1, decisions: [] } }))
    .toThrow(/unclassified source candidate/);
  expect(() => checkInventory({ ...base, decisions: { schemaVersion: 1, decisions: [fixture.decision, fixture.decision] } }))
    .toThrow(/duplicate inventory decision/);
  const stale = { ...fixture.decision, sourceFingerprint: sha256('former text') };
  expect(() => checkInventory({ ...base, decisions: { schemaVersion: 1, decisions: [fixture.decision, stale] } }))
    .toThrow(/stale inventory decision/);
  expect(() => checkInventory({ ...base, decisions: { schemaVersion: 1, decisions: [stale] } }))
    .toThrow(/unclassified source candidate/);

  const changed = sample('const caption = "Changed label";');
  expect(() => checkInventory({ inventory: changed.inventory, course: changed.course,
    sourceTextByPath: changed.sourceTextByPath,
    decisions: { schemaVersion: 1, decisions: [fixture.decision] } }))
    .toThrow(/unclassified source candidate/);
});

test('pack-backed decisions require a resolvable pointer and a real reason', () => {
  const fixture = sample();
  const base = { inventory: fixture.inventory, course: fixture.course, sourceTextByPath: fixture.sourceTextByPath };
  const authored = { ...fixture.decision, category: 'pack-backed-authored' };
  expect(() => checkInventory({ ...base, decisions: { schemaVersion: 1, decisions: [authored] } }))
    .toThrow(/packPointer/);
  expect(() => checkInventory({ ...base, decisions: { schemaVersion: 1,
    decisions: [{ ...authored, packPointer: '/reference/missing' }] } }))
    .toThrow(/packPointer must resolve to nonblank authored string/);
  expect(() => checkInventory({ ...base, decisions: { schemaVersion: 1,
    decisions: [{ ...authored, packPointer: '/reference' }] } }))
    .toThrow(/packPointer must resolve to nonblank authored string/);
  expect(() => checkInventory({ ...base, decisions: { schemaVersion: 1,
    decisions: [{ ...authored, packPointer: '/toString' }] } }))
    .toThrow(/packPointer must resolve to nonblank authored string/);
  expect(() => checkInventory({ ...base, decisions: { schemaVersion: 1,
    decisions: [{ ...authored, packPointer: '/reference/title', reason: ' ' }] } }))
    .toThrow(/reason/);
  expect(() => checkInventory({ ...base, decisions: { schemaVersion: 1,
    decisions: [{ ...authored, category: 'anything', packPointer: '/reference/title' }] } }))
    .toThrow(/inventory decisions/);
});

test('each literal in one source line needs its own ordered classification', () => {
  const fixture = sample('const caption = getCourse("title") + "UI color cue";');
  const base = { inventory: fixture.inventory, course: fixture.course, sourceTextByPath: fixture.sourceTextByPath };
  expect(() => checkInventory({ ...base, decisions: { schemaVersion: 1, decisions: [fixture.decision] } }))
    .toThrow(/tokens require individual parts/);
  const { category: _category, reason: _reason, ...key } = fixture.decision;
  const incomplete = { ...key,
    parts: [{ ordinal: 0, category: 'pack-backed-authored', reason: 'Course claim', packPointer: '/reference/title' },
      { ordinal: 2, category: 'ui-localization-chrome', reason: 'Presentation cue' }] };
  expect(() => checkInventory({ ...base, decisions: { schemaVersion: 1, decisions: [incomplete] } }))
    .toThrow(/incomplete or unordered token parts/);
  const complete = { ...incomplete, parts: [incomplete.parts[0], { ...incomplete.parts[1], ordinal: 1 }] };
  expect(checkInventory({ ...base, decisions: { schemaVersion: 1, decisions: [complete] } }))
    .toMatchObject({ candidateCount: 1, tokenCount: 2 });
});

test('JSX prose is a candidate and token ordinals follow source order', () => {
  const prose = sample('const Example = () => <p>Новая грамматическая подсказка</p>;');
  expect(buildCandidates(prose.inventory, prose.sourceTextByPath)[0].tokens)
    .toEqual(['Новая грамматическая подсказка']);

  const mixed = sample('const Example = () => <p>Учебный текст<span title="UI">хром</span></p>;');
  expect(buildCandidates(mixed.inventory, mixed.sourceTextByPath)[0].tokens)
    .toEqual(['Учебный текст', '"UI"', 'хром']);
});

test('nested interpolation strings remain separately classifiable', () => {
  const sources = [
    ['src/ui/Synthetic.ts', 'const text = `outer ${format("authored")} tail`;', '"authored"'],
    ['kotlin/iosApp/Synthetic.swift', String.raw`let text = "outer \(format("authored")) tail"`, '"authored"'],
  ];
  for (const [path, source, nested] of sources) {
    const candidate = buildCandidates({ schemaVersion: 1, files: [{ path, sha256: sha256(source),
      literalLines: [{ line: 1, source }] }] }, new Map([[path, source]]))[0];
    expect(candidate.tokens).toContain(nested);
    expect(candidate.tokens.indexOf(nested)).toBeGreaterThan(0);
  }

  for (const [path, phrases] of [
    ['kotlin/composeApp/src/webMain/kotlin/polski/ui/MatrixWeb.kt', ['"ед. ч."', '"мн. ч."']],
    ['kotlin/composeApp/src/commonMain/kotlin/polski/ui/screens/VocabularyScreen.kt', ['"не введён"']],
  ] as const) {
    const source = readFileSync(path, 'utf8');
    const line = source.split('\n').findIndex(text => phrases.every(phrase => text.includes(phrase))) + 1;
    expect(line).toBeGreaterThan(0);
    const candidate = buildCandidates({ schemaVersion: 1, files: [{ path, sha256: sha256(source),
      literalLines: [{ line, source: source.split('\n')[line - 1].trim() }] }] }, new Map([[path, source]]))[0];
    for (const phrase of phrases) expect(candidate.tokens).toContain(phrase);
  }
});
