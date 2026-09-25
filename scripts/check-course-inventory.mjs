import { createHash } from 'node:crypto';
import { execFileSync } from 'node:child_process';
import { readFileSync, writeFileSync } from 'node:fs';
import { fileURLToPath, pathToFileURL } from 'node:url';
import Ajv2020 from 'ajv/dist/2020.js';
import ts from 'typescript';

const root = new URL('../', import.meta.url);
const readJson = (path) => JSON.parse(readFileSync(new URL(path, root), 'utf8'));
const decisionSchema = readJson('courses/schema/inventory-decisions-v1.schema.json');
const validateDecisions = new Ajv2020({ allErrors: true, strict: true, strictRequired: false }).compile(decisionSchema);
const categories = new Set(decisionSchema.$defs.category.enum);
const inactivePrototypePaths = new Set([
  'kotlin/composeApp/src/commonMain/kotlin/polski/ui/SpikeScreen.kt',
  'kotlin/composeApp/src/webMain/kotlin/polski/ui/SpikeHtmlControls.kt',
  'kotlin/composeApp/src/androidMain/kotlin/polski/ui/SpikeControlsAndroid.kt',
  'kotlin/composeApp/src/desktopMain/kotlin/polski/ui/SpikeControlsDesktop.kt',
  'kotlin/shared/src/commonMain/kotlin/polski/spike/SpikeSession.kt',
]);

const sha256 = (value) => createHash('sha256').update(value).digest('hex');
const keyOf = ({ path, symbol, sourceFingerprint, occurrence }) =>
  JSON.stringify([path, symbol, sourceFingerprint, occurrence]);

function namedTsNode(node) {
  if (ts.isFunctionDeclaration(node) || ts.isClassDeclaration(node) || ts.isInterfaceDeclaration(node) ||
    ts.isEnumDeclaration(node) || ts.isTypeAliasDeclaration(node) || ts.isMethodDeclaration(node) ||
    ts.isVariableDeclaration(node) || ts.isPropertyDeclaration(node) || ts.isGetAccessorDeclaration(node)) {
    return node.name?.getText() ?? null;
  }
  return null;
}

function tsSymbols(path, source) {
  const file = ts.createSourceFile(path, source, ts.ScriptTarget.Latest, true,
    path.endsWith('.tsx') ? ts.ScriptKind.TSX : ts.ScriptKind.TS);
  return (line) => {
    const offset = file.getPositionOfLineAndCharacter(line - 1, 0);
    const names = [];
    function visit(node) {
      if (node.pos > offset || node.end <= offset) return;
      const name = namedTsNode(node);
      if (name) names.push(name);
      ts.forEachChild(node, visit);
    }
    visit(file);
    return names.join('.') || 'file-scope';
  };
}

function tsLiteralTokens(path, source) {
  const tokens = new Map();
  const file = ts.createSourceFile(path, source, ts.ScriptTarget.Latest, true,
    path.endsWith('.tsx') ? ts.ScriptKind.TSX : ts.ScriptKind.TS);
  function visit(node) {
    if (ts.isStringLiteral(node) || ts.isNoSubstitutionTemplateLiteral(node) ||
      node.kind === ts.SyntaxKind.TemplateHead || node.kind === ts.SyntaxKind.TemplateMiddle ||
      node.kind === ts.SyntaxKind.TemplateTail || ts.isJsxText(node)) {
      const position = file.getLineAndCharacterOfPosition(node.getStart(file));
      const start = position.line;
      node.getText(file).split('\n').forEach((text, offset) => {
        const value = text.trim();
        if (!value) return;
        const line = start + offset + 1;
        const column = (offset ? 0 : position.character) + text.indexOf(value);
        tokens.set(line, [...(tokens.get(line) ?? []), { start: column, text: value }]);
      });
    }
    ts.forEachChild(node, visit);
  }
  visit(file);
  return tokens;
}

/** Quote-aware native scanner, including nested literals inside Kotlin/Swift interpolation. */
export function scanLines(source, path) {
  const swift = path.endsWith('.swift');
  const lines = source.split('\n');
  const result = [];
  let blockComment = false;
  const frames = [{ kind: 'code', interpolation: false, close: null, depth: 0 }];
  for (const line of lines) {
    const tokens = [];
    const tokenStarts = [];
    const braces = [];
    if (frames.at(-1).kind === 'string') frames.at(-1).start = 0;
    const emit = (start, end) => {
      if (end > start) { tokens.push(line.slice(start, end)); tokenStarts.push(start); }
    };
    for (let index = 0; index < line.length; index += 1) {
      const char = line[index];
      const next = line[index + 1];
      const frame = frames.at(-1);
      if (frame.kind === 'string') {
        if (char === '\\' && (!frame.triple || swift)) {
          if (swift && next === '(') {
            emit(frame.start, index + 2);
            frames.push({ kind: 'code', interpolation: true, close: ')', depth: 1 });
            index += 1;
          } else index += 1;
          continue;
        }
        if (!swift && char === '$' && next === '{' && frame.quote !== '\'') {
          emit(frame.start, index + 2);
          frames.push({ kind: 'code', interpolation: true, close: '}', depth: 1 });
          index += 1;
          continue;
        }
        const closing = frame.triple ? '"""' : frame.quote;
        if (line.slice(index, index + closing.length) === closing) {
          emit(frame.start, index + closing.length);
          frames.pop();
          index += closing.length - 1;
        }
        continue;
      }
      if (blockComment) {
        if (char === '*' && next === '/') { blockComment = false; index += 1; }
        continue;
      }
      if (char === '/' && next === '/') break;
      if (char === '/' && next === '*') { blockComment = true; index += 1; continue; }
      if (char === '"' && line.slice(index, index + 3) === '"""') {
        frames.push({ kind: 'string', quote: '"', triple: true, start: index }); index += 2; continue;
      }
      if (char === '"' || char === '\'' || (!path.endsWith('.kt') && char === '`')) {
        frames.push({ kind: 'string', quote: char, triple: false, start: index }); continue;
      }
      if (frame.interpolation) {
        const opener = frame.close === '}' ? '{' : '(';
        if (char === opener) frame.depth += 1;
        if (char === frame.close) {
          frame.depth -= 1;
          if (frame.depth === 0) {
            frames.pop();
            frames.at(-1).start = index;
          }
        }
      } else if (char === '{' || char === '}') braces.push(char);
    }
    if (frames.at(-1).kind === 'string') emit(frames.at(-1).start, line.length);
    result.push({ tokens, tokenStarts, braces });
  }
  return result;
}

function declarationOnLine(line, path) {
  if (path.endsWith('.swift')) {
    return line.match(/\b(?:func|struct|class|enum|protocol|extension|var|let)\s+([A-Za-z_][A-Za-z0-9_]*)/)?.[1] ?? null;
  }
  return line.match(/\b(?:fun|class|interface|object|val|var)\s+([A-Za-z_][A-Za-z0-9_]*)/)?.[1] ?? null;
}

function nativeSymbols(path, source, scans) {
  const lines = source.split('\n');
  const symbols = [];
  const stack = [];
  let depth = 0;
  let pending = null;
  for (let index = 0; index < lines.length; index += 1) {
    const line = lines[index];
    const declared = declarationOnLine(line, path);
    const parent = stack.map(entry => entry.name).join('.');
    symbols.push(declared ? [parent, declared].filter(Boolean).join('.') : (parent || 'file-scope'));
    if (declared && /\b(?:fun|func|class|struct|object|interface|enum|protocol|extension|var)\b/.test(line)) {
      pending = declared;
    }
    for (const brace of scans[index].braces) {
      if (brace === '{') {
        depth += 1;
        if (pending) { stack.push({ name: pending, depth }); pending = null; }
      } else {
        depth -= 1;
        while (stack.length && stack.at(-1).depth > depth) stack.pop();
      }
    }
    if (pending && /[;=]/.test(line) && !line.includes('{')) pending = null;
  }
  return (line) => symbols[line - 1] ?? 'file-scope';
}

export function buildCandidates(inventory, sourceTextByPath) {
  const candidates = [];
  for (const file of inventory.files) {
    const source = sourceTextByPath.get(file.path);
    if (typeof source !== 'string') throw new Error(`${file.path}: source file missing`);
    if (sha256(source) !== file.sha256) throw new Error(`${file.path}: source hash differs from inventory`);
    const lines = source.split('\n');
    const scans = scanLines(source, file.path);
    const tsTokens = file.path.endsWith('.ts') || file.path.endsWith('.tsx') ? tsLiteralTokens(file.path, source) : null;
    const symbolAt = file.path.endsWith('.ts') || file.path.endsWith('.tsx') ?
      tsSymbols(file.path, source) : nativeSymbols(file.path, source, scans);
    const occurrences = new Map();
    for (const row of file.literalLines) {
      if (lines[row.line - 1]?.trim() !== row.source) {
        throw new Error(`${file.path}:${row.line}: generated source line differs from file`);
      }
      const symbol = symbolAt(row.line);
      const sourceFingerprint = sha256(row.source);
      const repeated = JSON.stringify([symbol, sourceFingerprint]);
      const occurrence = (occurrences.get(repeated) ?? 0) + 1;
      occurrences.set(repeated, occurrence);
      const scan = scans[row.line - 1];
      const tokens = (tsTokens ? (tsTokens.get(row.line) ?? []) :
        scan.tokens.map((text, index) => ({ text, start: scan.tokenStarts[index] })))
        .sort((left, right) => left.start - right.start).map(item => item.text);
      candidates.push({ path: file.path, line: row.line, source: row.source, symbol,
        sourceFingerprint, occurrence, tokens });
    }
  }
  return candidates;
}

function valueAtPointer(document, pointer) {
  const keys = pointer.slice(1).split('/').map(key => key.replaceAll('~1', '/').replaceAll('~0', '~'));
  let value = document;
  for (const key of keys) {
    if (value === null || typeof value !== 'object' || !Object.hasOwn(value, key)) return undefined;
    value = value[key];
  }
  return value;
}

function validateClassification(classification, course, candidate, ordinal, sourceTextByPath) {
  if (!categories.has(classification.category) || !classification.reason?.trim()) {
    throw new Error(`${candidate.path}:${candidate.line} part ${ordinal}: invalid category/reason`);
  }
  if (classification.category === 'pack-backed-authored') {
    const authored = valueAtPointer(course, classification.packPointer);
    if (typeof authored !== 'string' || !authored.trim()) {
      throw new Error(`${candidate.path}:${candidate.line} part ${ordinal}: packPointer must resolve to nonblank authored string ${classification.packPointer}`);
    }
    const token = candidate.tokens[ordinal] ?? '';
    const leaf = classification.packPointer.split('/').at(-1)
      .replaceAll('~1', '/').replaceAll('~0', '~');
    if (token !== JSON.stringify(leaf) && token !== `'${leaf}'`) {
      throw new Error(`${candidate.path}:${candidate.line} part ${ordinal}: pack-backed token must name pointer leaf ${leaf}`);
    }
    if (authored.length > 3) {
      for (const [path, source] of sourceTextByPath) {
        if (source.includes(authored)) {
          throw new Error(`${path}: second hardcoded copy of ${classification.packPointer}`);
        }
      }
    }
  } else if (classification.packPointer !== undefined) {
    throw new Error(`${candidate.path}:${candidate.line} part ${ordinal}: packPointer requires pack-backed-authored`);
  }
  if (classification.category === 'inactive-prototype' && !inactivePrototypePaths.has(candidate.path)) {
    throw new Error(`${candidate.path}:${candidate.line} part ${ordinal}: inactive-prototype is restricted to the unmounted Spike files`);
  }
}

/** Pure coverage check used by the CLI and independent adversarial tests. */
export function checkInventory({ inventory, decisions, course, sourceTextByPath }) {
  if (!validateDecisions(decisions)) {
    const first = validateDecisions.errors[0];
    throw new Error(`inventory decisions${first.instancePath}: ${first.message}`);
  }
  const candidates = buildCandidates(inventory, sourceTextByPath);
  for (const [path, source] of sourceTextByPath) {
    if (!inactivePrototypePaths.has(path) && /\bSpikeScreen\b/.test(source)) {
      throw new Error(`${path}: inactive SpikeScreen prototype is referenced by production source`);
    }
  }
  const byKey = new Map();
  for (const decision of decisions.decisions) {
    const key = keyOf(decision);
    if (byKey.has(key)) throw new Error(`${decision.path} ${decision.symbol}: duplicate inventory decision`);
    byKey.set(key, decision);
  }
  const rows = [];
  for (const candidate of candidates) {
    const key = keyOf(candidate);
    const decision = byKey.get(key);
    if (!decision) throw new Error(`${candidate.path}:${candidate.line} ${candidate.symbol}: unclassified source candidate`);
    byKey.delete(key);
    const count = Math.max(1, candidate.tokens.length);
    if (count > 1 && !decision.parts) {
      throw new Error(`${candidate.path}:${candidate.line}: ${count} tokens require individual parts`);
    }
    if (count === 1 && decision.parts) {
      throw new Error(`${candidate.path}:${candidate.line}: single token must use direct classification`);
    }
    const parts = decision.parts ?? [{ ordinal: 0, category: decision.category,
      reason: decision.reason, packPointer: decision.packPointer }];
    if (parts.length !== count || parts.some((part, ordinal) => part.ordinal !== ordinal)) {
      throw new Error(`${candidate.path}:${candidate.line}: incomplete or unordered token parts`);
    }
    parts.forEach((part, ordinal) => validateClassification(part, course, candidate, ordinal, sourceTextByPath));
    rows.push({ path: candidate.path, line: candidate.line, symbol: candidate.symbol,
      sourceFingerprint: candidate.sourceFingerprint, occurrence: candidate.occurrence,
      source: candidate.source, tokens: candidate.tokens, parts });
  }
  if (byKey.size) {
    const stale = byKey.values().next().value;
    throw new Error(`${stale.path} ${stale.symbol}: stale inventory decision`);
  }
  return { schemaVersion: 1, candidateCount: rows.length,
    tokenCount: rows.reduce((sum, row) => sum + row.parts.length, 0), rows };
}

function option(name, fallback) {
  const index = process.argv.indexOf(name);
  return index < 0 ? fallback : process.argv[index + 1];
}

function main() {
  execFileSync(process.execPath, ['scripts/inventory-course.mjs', '--check'], { cwd: fileURLToPath(root), stdio: 'pipe' });
  const inventoryPath = option('--inventory', 'courses/pl-ru/source-inventory.json');
  const decisionsPath = option('--decisions', 'courses/pl-ru/inventory-decisions.json');
  const reportPath = option('--report', 'courses/pl-ru/inventory-coverage.json');
  const inventory = readJson(inventoryPath);
  const decisions = readJson(decisionsPath);
  const course = readJson('courses/pl-ru/course.json');
  const sourceTextByPath = new Map(inventory.files.map(file =>
    [file.path, readFileSync(new URL(file.path, root), 'utf8')]));
  const report = checkInventory({ inventory, decisions, course, sourceTextByPath });
  const serialized = `${JSON.stringify(report, null, 2)}\n`;
  if (process.argv.includes('--check')) {
    if (readFileSync(new URL(reportPath, root), 'utf8') !== serialized) {
      throw new Error(`stale coverage report ${reportPath}; regenerate with node scripts/check-course-inventory.mjs`);
    }
  } else writeFileSync(new URL(reportPath, root), serialized);
  process.stdout.write(`PASS course inventory decisions: ${report.candidateCount} candidates, ${report.tokenCount} tokens\n`);
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  try { main(); }
  catch (error) { process.stderr.write(`FAIL course inventory decisions: ${error.message}\n`); process.exitCode = 1; }
}
