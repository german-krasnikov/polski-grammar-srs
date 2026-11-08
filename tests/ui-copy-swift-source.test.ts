import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import { describe, expect, it } from 'vitest';

const root = join(import.meta.dirname, '..');
const rowKeys = (pack: string) => new Set(
  [...readFileSync(join(root, `courses-biome/${pack}/uiCopy.core-data`), 'utf8')
    .matchAll(/UI_COPY\s*\{\s*key:\s*(?:"([^"]+)"|([\w.]+))/g)]
    .map(([, quoted, bare]) => quoted ?? bare),
);
const swiftFiles = ['PolskiGrammarApp.swift', 'FlashCardView.swift', 'VocabularyCardView.swift'];

// Structural consumer check. Literal expected copy lives in the DSL oracle; this checks that
// Swift never silently shows its old-framework fallback for a missing authored semantic key.
describe('Swift UI copy bindings', () => {
  it('has a row in both packs for every statically named Swift key', () => {
    const used = new Set(swiftFiles.flatMap(file => {
      const source = readFileSync(join(root, 'kotlin/iosApp/PolskiGrammar', file), 'utf8');
      return [...source.matchAll(/ui(?:Text|Format)\(\s*[^,]+,\s*"([^"]+)"/g)]
        .map(([, key]) => key.startsWith('ui')
          ? key[2].toLowerCase() + key.slice(3) : key)
        .filter(key => !key.includes('\\('));
    }));
    for (const pack of ['pl-ru', 'en-ru']) {
      const keys = rowKeys(pack);
      expect([...used].filter(key => !keys.has(key)), `${pack} missing Swift copy`).toEqual([]);
    }
  });
});
