import { createHash } from 'node:crypto';
import { readFileSync, readdirSync, writeFileSync } from 'node:fs';
import { fileURLToPath, pathToFileURL } from 'node:url';
import ts from 'typescript';
import { scanLines } from './check-course-inventory.mjs';

const root = new URL('../', import.meta.url);
const target = new URL('../courses/pl-ru/source-inventory.json', import.meta.url);
const explicitSources = [
  ['src/training/generator.ts', 'exercise factory'],
  ['src/grammar/engine.ts', 'Polish morphology and sentence templates'],
  ['src/ui/GrammarTables.tsx', 'React grammar matrix'],
  ['src/ui/App.tsx', 'React rule and training UI'],
  ['src/ui/VocabularyView.tsx', 'React vocabulary UI'],
  ['kotlin/shared/src/commonMain/kotlin/polski/training/ExerciseFactory.kt', 'Kotlin exercise factory'],
  ['kotlin/shared/src/commonMain/kotlin/polski/grammar/GrammarEngine.kt', 'Kotlin Polish morphology and sentence templates'],
  ['kotlin/shared/src/iosMain/kotlin/polski/ios/IosSnapshot.kt', 'iOS presentation adapter'],
  ['kotlin/composeApp/src/webMain/kotlin/polski/ui/MatrixWeb.kt', 'Kotlin browser grammar matrix'],
  ['kotlin/composeApp/src/webMain/kotlin/polski/ui/TrainingWebApp.kt', 'Kotlin browser rule and training UI'],
  ['kotlin/composeApp/src/webMain/kotlin/polski/ui/VocabularyWeb.kt', 'Kotlin browser vocabulary UI'],
  ['kotlin/composeApp/src/desktopMain/kotlin/polski/ui/screens/MatrixScreen.kt', 'macOS grammar matrix'],
  ['kotlin/composeApp/src/desktopMain/kotlin/polski/ui/screens/TrainingScreen.kt', 'macOS rule and training UI'],
  ['kotlin/composeApp/src/desktopMain/kotlin/polski/ui/screens/ProgressScreen.kt', 'macOS progress UI'],
  ['kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidMatrixScreen.kt', 'Android grammar matrix'],
  ['kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidTrainingScreen.kt', 'Android rule and training UI'],
  ['kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidProgressScreen.kt', 'Android progress UI'],
  ['kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidContent.kt', 'Android recovery UI'],
  ['kotlin/composeApp/src/androidMain/kotlin/polski/ui/screens/AndroidWidgets.kt', 'Android UI controls'],
  ['kotlin/composeApp/src/commonMain/kotlin/polski/ui/screens/VocabularyScreen.kt', 'shared native vocabulary UI'],
  ['kotlin/iosApp/PolskiGrammar/PolskiGrammarApp.swift', 'SwiftUI training and matrix UI'],
];
const sourceDirectories = [
  ['src/data', 'React course data adapter'],
  ['src/grammar', 'React Polish grammar'],
  ['kotlin/shared/src/commonMain/kotlin/polski/data', 'Kotlin course data adapter'],
  ['kotlin/shared/src/commonMain/kotlin/polski/grammar', 'Kotlin Polish grammar'],
  ['kotlin/composeApp/src/commonMain/kotlin/polski/ui', 'shared Compose course presentation'],
];
const productionRoots = [
  ['src', /\.(ts|tsx)$/],
  ['kotlin/shared/src', /\.kt$/],
  ['kotlin/composeApp/src', /\.kt$/],
  ['kotlin/androidApp/src/main', /\.kt$/],
  ['kotlin/iosApp/PolskiGrammar', /\.swift$/],
];
function walk(directory) {
  return readdirSync(new URL(`${directory}/`, root), { withFileTypes: true })
    .sort((left, right) => left.name.localeCompare(right.name, 'en')).flatMap(entry => {
    const path = `${directory}/${entry.name}`;
    return entry.isDirectory() ? walk(path) : [path];
  });
}
const discovered = productionRoots.flatMap(([directory, extension]) =>
  walk(directory).filter(path => extension.test(path) &&
    !/(?:\/|^)[^/]*Test(?:s)?(?:\/|\.)/.test(path) &&
    (!path.startsWith('kotlin/shared/src/') && !path.startsWith('kotlin/composeApp/src/') ||
      /\/[^/]+Main\//.test(path))).map(path => [path, 'production source audit']));
const sources = [...new Map([
  ...explicitSources,
  ...sourceDirectories.flatMap(([directory, kind]) =>
    readdirSync(new URL(directory, root)).sort().filter(name => /\.(ts|kt)$/.test(name))
      .map(name => [`${directory}/${name}`, kind])),
  ...discovered,
]).entries()];

function jsxTextLines(path, content) {
  if (!path.endsWith('.tsx')) return new Set();
  const file = ts.createSourceFile(path, content, ts.ScriptTarget.Latest, true, ts.ScriptKind.TSX);
  const lines = new Set();
  function visit(node) {
    if (ts.isJsxText(node)) {
      const start = file.getLineAndCharacterOfPosition(node.getStart(file)).line;
      node.getText(file).split('\n').forEach((text, offset) => {
        if (text.trim()) lines.add(start + offset + 1);
      });
    }
    ts.forEachChild(node, visit);
  }
  visit(file);
  return lines;
}

export function literalLinesForSource(path, content) {
  const jsxLines = jsxTextLines(path, content);
  const scanned = scanLines(content, path);
  return content.split('\n').flatMap((text, index) =>
    /["'`]/.test(text) || jsxLines.has(index + 1) || scanned[index].tokens.length ?
      [{ line: index + 1, source: text.trim() }] : []);
}

function main() {
  const inventory = {
    schemaVersion: 1,
    description: 'Exact source lines containing string, template or JSX text syntax in production code. Includes program/UI literals; each candidate requires editorial classification.',
    files: sources.map(([path, kind]) => {
      const content = readFileSync(new URL(path, root), 'utf8');
      return {
        path,
        owner: kind,
        migrationStatus: 'classified in inventory-decisions.json; review Evidence.md for acceptance',
        sha256: createHash('sha256').update(content).digest('hex'),
        literalLines: literalLinesForSource(path, content),
      };
    }),
  };
  const serialized = `${JSON.stringify(inventory, null, 2)}\n`;
  if (process.argv.includes('--check')) {
    if (readFileSync(target, 'utf8') !== serialized) {
      process.stderr.write('FAIL course inventory: regenerate with node scripts/inventory-course.mjs\n');
      process.exitCode = 1;
    } else process.stdout.write(`PASS course inventory: ${inventory.files.length} source files\n`);
  } else {
    writeFileSync(target, serialized);
    process.stdout.write(`Updated ${fileURLToPath(target)} (${inventory.files.length} source files)\n`);
  }
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) main();
