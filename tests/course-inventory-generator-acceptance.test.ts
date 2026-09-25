import { expect, test } from 'vitest';
import { literalLinesForSource } from '../scripts/inventory-course.mjs';

test.each([
  ['TypeScript template', 'src/ui/Example.ts', 'const copy = `first\ncourse meaning\nlast`;', 2],
  ['Kotlin raw string', 'kotlin/shared/src/commonMain/kotlin/Example.kt', 'val copy = """first\ncourse meaning\nlast"""', 2],
  ['Swift multiline string', 'kotlin/iosApp/Example.swift', 'let copy = """\nfirst\ncourse meaning\nlast\n"""', 3],
  ['JSX text', 'src/ui/Example.tsx', 'const Example = () => <p>first\ncourse meaning</p>;', 2],
])('inventory includes unquoted interior line in %s', (_name, path, source, interiorLine) => {
  expect(literalLinesForSource(path, source).some((row: { line: number; source: string }) =>
    row.line === interiorLine && row.source.includes('course meaning'))).toBe(true);
});

test('inventory excludes an unrelated line between separate literals', () => {
  const source = 'const first = "a";\nconst answer = 42;\nconst second = "b";';
  expect(literalLinesForSource('src/ui/Example.ts', source).map((row: { line: number }) => row.line)).toEqual([1, 3]);
});
