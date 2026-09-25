import { createHash } from 'node:crypto';
import { describe, expect, it } from 'vitest';
import { buildCandidates, checkInventory, type InventoryDecision } from '../scripts/check-course-inventory.mjs';

const digest = (text: string) => createHash('sha256').update(text).digest('hex');
const path = 'src/ui/Example.tsx';
const source = 'export function Example() { return <p title="teaching">Учебная подсказка</p>; }\n';
const inventory = {
  files: [{ path, sha256: digest(source), literalLines: [{ line: 1, source: source.trim() }] }],
};
const sourceTextByPath = new Map([[path, source]]);
const candidate = buildCandidates(inventory, sourceTextByPath)[0];
const parts: InventoryDecision['parts'] = [
  { ordinal: 0, category: 'pack-backed-authored', reason: 'Course-specific teaching text from the pack', packPointer: '/teaching' },
  { ordinal: 1, category: 'ui-localization-chrome', reason: 'Static visible label in the host UI' },
];
const decision = { path, symbol: candidate.symbol, sourceFingerprint: candidate.sourceFingerprint,
  occurrence: candidate.occurrence, parts };
const input = (decisions: InventoryDecision[]) => ({ inventory, decisions: { schemaVersion: 1 as const, decisions },
  course: { teaching: 'Different text now owned by pack' }, sourceTextByPath });

describe('course inventory decision checker', () => {
  it('sees both JSX attributes and visible text as separate tokens', () => {
    expect(candidate.tokens).toEqual(['"teaching"', 'Учебная подсказка']);
    expect(checkInventory(input([decision])).tokenCount).toBe(2);
  });

  it('rejects unclassified, stale and duplicate decisions', () => {
    expect(() => checkInventory(input([]))).toThrow('unclassified source candidate');
    expect(() => checkInventory(input([{ ...decision, sourceFingerprint: digest('changed') }]))).toThrow('unclassified source candidate');
    expect(() => checkInventory(input([decision, decision]))).toThrow('duplicate inventory decision');
  });

  it('requires every token and a real pack pointer without a second hardcoded copy', () => {
    expect(() => checkInventory(input([{ ...decision, parts: parts?.slice(0, 1) }]))).toThrow('inventory decisions');
    expect(() => checkInventory(input([{ ...decision, parts: [{ ...parts![0], packPointer: '/missing' }, parts![1]] }]))).toThrow('packPointer must resolve');
    expect(() => checkInventory(input([{ ...decision, parts: [{ ...parts![0], packPointer: '/__proto__' }, parts![1]] }]))).toThrow('packPointer must resolve');
    expect(() => checkInventory({ ...input([{ ...decision, parts: [{ ...parts![0], packPointer: '/reference' }, parts![1]] }]),
      course: { reference: { title: 'A real authored leaf' } } })).toThrow('packPointer must resolve');
    expect(() => checkInventory({ ...input([{ ...decision, parts: [{ ...parts![0], packPointer: '/other' }, parts![1]] }]),
      course: { other: 'Valid but unrelated text' } })).toThrow('pack-backed token must name pointer leaf');
    expect(() => checkInventory({ ...input([decision]), course: { teaching: 'Учебная подсказка' } })).toThrow('second hardcoded copy');
  });

  it('keeps the legacy Spike fixtures outside the mounted application', () => {
    expect(() => checkInventory(input([{ ...decision, parts: [
      { ordinal: 0, category: 'inactive-prototype', reason: 'Synthetic bypass' }, parts![1],
    ] }]))).toThrow('inactive-prototype is restricted');
    const mounted = new Map(sourceTextByPath);
    mounted.set('src/ui/Mount.tsx', 'export const mount = SpikeScreen;');
    expect(() => checkInventory({ ...input([decision]), sourceTextByPath: mounted }))
      .toThrow('inactive SpikeScreen prototype is referenced');
  });
});
