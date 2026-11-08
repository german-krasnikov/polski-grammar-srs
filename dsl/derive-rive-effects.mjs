// Derive: the Rive effect catalog printed for every host from the one source
// dsl/effects/card-effects.core-data (Plans/Architecture/DSL-MIGRATION-PLAN.md Block A). Four hosts
// (Android, web, iOS, macOS) used to each carry their own copy of the same effect -> Rive trigger
// table; this file reads the one RiveCatalogRow (checked against dsl/effects.data-contract by the
// `effects-validate` step, the same `biome read` trick derive-forms.mjs uses to parse a lexicon file)
// and prints it three ways: a Kotlin object, a Swift struct (one file, copied for iOS and macOS) and
// a JS module the web bridge reads instead of its own inline table.
// It returns the text and writes nothing: `biome generate` writes OUT (generate) or compares it with
// the file (--check).
import { spawnSync } from 'node:child_process';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const BANNER = 'Generated from dsl/effects/card-effects.core-data by derive-rive-effects.mjs — do not edit.';

export const OUT_KOTLIN = resolve(root, 'kotlin/shared/src/commonMain/generated/kotlin/polski/data/rows/effects/CardEffectSpecs.kt');
export const OUT_IOS = resolve(root, 'kotlin/iosApp/PolskiGrammar/Generated/RiveEffects.swift');
export const OUT_MACOS = resolve(root, 'kotlin/macosApp/PolskiGrammarMac/Generated/RiveEffects.swift');
export const OUT_JS = resolve(root, 'kotlin/composeApp/src/webMain/resources/rive/rive-effects.generated.js');

function biome(node, cli, env, args) {
  const run = spawnSync(node, [cli, ...args], { cwd: root, env, encoding: 'utf8', timeout: 120_000 });
  if (run.status !== 0) throw new Error(`biome ${args.slice(0, 2).join(' ')} failed (${run.status ?? run.error?.message}): ${run.stderr?.trim()}`);
  return run.stdout;
}

/** The one RiveCatalogRow of the step's file, read with `biome read` (the same sub-process trick derive-forms.mjs uses to parse a lexicon file). */
function readCatalog({ cli, node = process.execPath, env = process.env, generateArgs = [] } = {}) {
  if (!cli) throw new Error('derive-rive-effects: no context.cli; run it with biome generate');
  const [file] = generateArgs;
  if (!file) throw new Error('derive-rive-effects: the card-effects.core-data file');
  const [row] = JSON.parse(biome(node, cli, env, ['read', resolve(file), '--format', 'json'])).map((entry) => entry.value);
  return row;
}

const playsFor = (catalog, effect) => catalog.effects.find((row) => row.effect === effect)?.plays ?? [];

/** The Kotlin side: `CardEffectSpecs`, keyed by the shared [polski.presentation.CardEffect] (Android's existing index-based view pool reads `plays(effect)` in order). */
export function kotlin(context = {}) {
  const catalog = readCatalog(context);
  const remembered = playsFor(catalog, 'remembered');
  const again = playsFor(catalog, 'again');
  const cc = catalog.chainComplete;
  const play = (p) => `EffectPlay(asset = "${p.asset}", stateMachine = "${p.stateMachine}", trigger = "${p.trigger}")`;
  const list = (plays) => `listOf(\n${plays.map((p) => `        ${play(p)},`).join('\n')}\n    )`;
  return `${[
    `// ${BANNER}`,
    'package polski.data.rows.effects',
    '',
    'import polski.presentation.CardEffect',
    '',
    '/** One Rive trigger to fire on a given state-machine asset. */',
    'data class EffectPlay(val asset: String, val stateMachine: String, val trigger: String)',
    '',
    '/** The chain-complete celebration clip: an artboard and a plain animation name, not a state-machine trigger. */',
    'data class ChainCompleteSpec(val asset: String, val artboard: String, val animation: String)',
    '',
    '/** The effect -> Rive trigger table, the one source for every host (DSL-MIGRATION-PLAN.md Block A). */',
    'object CardEffectSpecs {',
    `    val remembered: List<EffectPlay> = ${list(remembered)}`,
    `    val again: List<EffectPlay> = ${list(again)}`,
    `    val chainComplete: ChainCompleteSpec = ChainCompleteSpec(asset = "${cc.asset}", artboard = "${cc.artboard}", animation = "${cc.animation}")`,
    '',
    '    /** The plays for [effect], in order; [CardEffect.None] has none. */',
    '    fun plays(effect: CardEffect): List<EffectPlay> = when (effect) {',
    '        CardEffect.Remembered -> remembered',
    '        CardEffect.Again -> again',
    '        CardEffect.None -> emptyList()',
    '    }',
    '}',
  ].join('\n')}\n`;
}

/** The Swift side: `RiveEffects`, one file copied for both iOS and macOS, keyed by the Kotlin `CardEffect` wire name ("Remembered"/"Again") the two hosts already read off `CardEffectEvent.name`. */
export function swift(context = {}) {
  const catalog = readCatalog(context);
  const remembered = playsFor(catalog, 'remembered');
  const again = playsFor(catalog, 'again');
  const cc = catalog.chainComplete;
  const play = (p) => `RiveEffectPlay(asset: "${p.asset}", stateMachine: "${p.stateMachine}", trigger: "${p.trigger}")`;
  const list = (plays) => `[\n${plays.map((p) => `        ${play(p)},`).join('\n')}\n    ]`;
  return `${[
    `// ${BANNER}`,
    '',
    '/// One Rive trigger to fire on a given state-machine asset.',
    'struct RiveEffectPlay { let asset: String; let stateMachine: String; let trigger: String }',
    '',
    '/// The chain-complete celebration clip: an artboard and a plain animation name, not a state-machine trigger.',
    'struct RiveChainComplete { let asset: String; let artboard: String; let animation: String }',
    '',
    '/// The effect -> Rive trigger table, the one source for every host (DSL-MIGRATION-PLAN.md Block A).',
    'enum RiveEffects {',
    `    static let remembered: [RiveEffectPlay] = ${list(remembered)}`,
    `    static let again: [RiveEffectPlay] = ${list(again)}`,
    `    static let chainComplete = RiveChainComplete(asset: "${cc.asset}", artboard: "${cc.artboard}", animation: "${cc.animation}")`,
    '',
    '    /// The plays for the Kotlin `CardEffect` wire name ("Remembered"/"Again"); anything else (including "None") has none.',
    '    static func plays(for effectName: String) -> [RiveEffectPlay] {',
    '        switch effectName {',
    '        case "Remembered": return remembered',
    '        case "Again": return again',
    '        default: return []',
    '        }',
    '    }',
    '}',
  ].join('\n')}\n`;
}

/** The web side: plain globals (no module system — `rive-bridge.js` loads this the same way it already lazily loads rive.js) matching the shape `rive-bridge.js`'s own Rive calls expect. */
export function js(context = {}) {
  const catalog = readCatalog(context);
  const remembered = playsFor(catalog, 'remembered');
  const again = playsFor(catalog, 'again');
  const cc = catalog.chainComplete;
  const play = (p) => `{ src: 'rive/${p.asset}.riv', stateMachine: '${p.stateMachine}', input: '${p.trigger}' }`;
  const list = (plays) => `[\n${plays.map((p) => `    ${play(p)},`).join('\n')}\n  ]`;
  return `${[
    `// ${BANNER}`,
    '// A plain global script, no IIFE and no module system: rive-bridge.js loads this with the same',
    '// dynamic <script> trick it already uses to lazy-load rive.js, then reads these two globals.',
    `var RATING_EFFECTS = {`,
    `  remembered: ${list(remembered)},`,
    `  again: ${list(again)},`,
    `};`,
    `var CHAIN_COMPLETE = { src: 'rive/${cc.asset}.riv', artboard: '${cc.artboard}', animation: '${cc.animation}' };`,
  ].join('\n')}\n`;
}
