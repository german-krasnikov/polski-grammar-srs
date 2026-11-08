// Derive: shared Kotlin constants → Swift for iOS and macOS hosts.
// Reads AXIS_RATIO from SwipeDecision.kt, HALF_MS/FULL_MS from CardFlipTiming.kt,
// and prints a Swift enum with those values so Swift hosts reference the same
// numbers without hardcoding them (6 copies of 1.25 and 3 copies of 250/500
// found by `biome patterns` — see Plans/Architecture/PHASE2-PLAN.md).
// It returns the text and writes nothing: `biome generate` writes OUT (generate)
// or compares it with the file (--check).
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const BANNER = 'Generated from Kotlin shared constants by derive-swift-constants.mjs — do not edit.';

export const OUT_IOS = resolve(root, 'kotlin/iosApp/PolskiGrammar/Generated/SharedConstants.swift');
export const OUT_MACOS = resolve(root, 'kotlin/macosApp/PolskiGrammarMac/Generated/SharedConstants.swift');

function extractConst(file, name) {
  const src = readFileSync(resolve(root, file), 'utf8');
  const match = src.match(new RegExp(`const\\s+val\\s+${name}\\s*=\\s*([\\d.]+)`));
  if (!match) throw new Error(`derive-swift-constants: ${name} not found in ${file}`);
  return match[1];
}

function generate() {
  const axisRatio = extractConst('kotlin/shared/src/commonMain/kotlin/polski/interaction/SwipeDecision.kt', 'AXIS_RATIO');
  const halfMs = extractConst('kotlin/shared/src/commonMain/kotlin/polski/presentation/CardFlipTiming.kt', 'HALF_MS');
  // FULL_MS = HALF_MS * 2 — compute rather than regex a computed expression
  const fullMs = parseInt(halfMs, 10) * 2;

  return `// ${BANNER}
enum SharedConstants {
    static let axisRatio: Double = ${axisRatio}
    static let flipHalfMs: Int = ${halfMs}
    static let flipFullMs: Int = ${fullMs}
}
`;
}

export function swift() { return generate(); }
