package polski.core.engine

import polski.core.engine.policy.Policy
import polski.core.engine.policy.data.TILE_REVEAL_OUTCOME_ROW_ROWS
import polski.core.model.FeatureBundle
import polski.core.model.FeatureKey
import polski.core.model.ScaffoldingLevel
import polski.core.model.TileExercise
import polski.core.model.TileSegment

/**
 * Gap B Phase 1 (tile-coverage plan Fix 3): builds a [TileExercise] for a pre-authored expected
 * string using whitespace-token reverse morphology lookup.  Every whitespace-separated token that
 * (after stripping trailing punctuation) matches a known form of [nounId] or [adjectiveId] becomes
 * a [TileSegment.Choice]; everything else is [TileSegment.Locked].
 *
 * **Limitations**
 * - Ambiguous surface forms (a word that is a valid form of both the noun and the adjective) are
 *   matched as noun-first.
 * - Reverse lookup covers only the forms the [morphology] table holds — a form absent from the
 *   table is treated as Locked (fail-soft, no crash).
 * - Distractors are drawn only from the matched lexeme's own paradigm (no cross-lexeme noise).
 *
 * Returns `null` when fewer than 2 whitespace tokens are found (nothing to pick from).
 */
object StaticTileBuilder {

    private val DISTRACTOR_COUNT = 3
    private val TRAILING_PUNCT = setOf('.', '?', '!', ',', ';', ':', '…', '»', '"')

    private data class FormEntry(val lexeme: String, val category: String, val bundle: FeatureBundle)

    private sealed interface TokenKind {
        data class Known(val correctForm: String, val entry: FormEntry, val suffix: String) : TokenKind
        data class Unknown(val text: String) : TokenKind
    }

    private sealed interface BuildSeg {
        data class Locked(val text: String) : BuildSeg
        data class Choice(val correctForm: String, val entry: FormEntry) : BuildSeg
    }

    private fun MutableList<BuildSeg>.appendLocked(text: String) {
        val last = lastOrNull() as? BuildSeg.Locked
        if (last != null) set(lastIndex, last.copy(text = last.text + text)) else add(BuildSeg.Locked(text))
    }

    fun fromStaticExercise(
        expected: String,
        nounId: String,
        adjectiveId: String,
        morphology: MorphologyParadigm,
        scaffolding: ScaffoldingLevel,
        random: RandomSource,
    ): TileExercise? {
        val tokens = expected.split(" ").filter { it.isNotEmpty() }
        if (tokens.size < 2) return null

        val nounForms = morphology.formsOf("noun:$nounId")
        val adjForms = morphology.formsOf("adjective:$adjectiveId")

        // Build a reverse map: surface form → (lexeme, category, bundle)
        val reverseMap = buildMap<String, FormEntry> {
            adjForms.forEach { (bundle, form) -> put(form, FormEntry(adjectiveId, "adjective", bundle)) }
            // noun takes precedence over adjective for the same form text
            nounForms.forEach { (bundle, form) -> put(form, FormEntry(nounId, "noun", bundle)) }
        }

        // Classify each token
        val classified = tokens.map { token ->
            val stripped = token.trimEnd(*TRAILING_PUNCT.toCharArray())
            val suffix = token.removePrefix(stripped)
            val entry = reverseMap[stripped]
            if (entry != null) TokenKind.Known(stripped, entry, suffix)
            else TokenKind.Unknown(token)
        }

        // Build the unscaffolded segment list (mutable text groups, positions assigned at end).
        // Locked text is accumulated; Choice interrupts accumulation.
        val built = mutableListOf<BuildSeg>()
        for ((index, kind) in classified.withIndex()) {
            val spaceBefore = if (index > 0) " " else ""
            when (kind) {
                is TokenKind.Known -> {
                    if (spaceBefore.isNotEmpty()) built.appendLocked(spaceBefore)
                    built.add(BuildSeg.Choice(kind.correctForm, kind.entry))
                    if (kind.suffix.isNotEmpty()) built.appendLocked(kind.suffix)
                }
                is TokenKind.Unknown -> built.appendLocked(spaceBefore + kind.text)
            }
        }

        // Apply scaffolding: take only the first `activeCount` Choice segments.
        val choiceIndices = built.indices.filter { built[it] is BuildSeg.Choice }
        val disposition = Policy.disposition(Policy.DispositionInput(scaffolding = scaffolding.name.lowercase()))
        val ratio = TILE_REVEAL_OUTCOME_ROW_ROWS.single { it.disposition == disposition }
        val activeCount = (choiceIndices.size * ratio.numerator + ratio.addend) / ratio.divisor
        val activeChoiceIndices = choiceIndices.take(activeCount).toSet()

        // Convert to TileSegment and assign positions.
        var position = 0
        val segments = mutableListOf<TileSegment>()
        for ((idx, seg) in built.withIndex()) {
            when (seg) {
                is BuildSeg.Locked -> segments += TileSegment.Locked(position++, seg.text)
                is BuildSeg.Choice -> {
                    if (idx in activeChoiceIndices) {
                        val alts = distractorsFor(seg.entry.lexeme, seg.entry.category, seg.correctForm, morphology, random)
                        val targetFeature = seg.entry.bundle.keys.firstOrNull() ?: FeatureKey(seg.entry.category)
                        segments += TileSegment.Choice(position++, seg.correctForm, seg.entry.lexeme, targetFeature, alts)
                    } else {
                        // Scaffolding demoted this Choice to Locked — merge with adjacent if possible.
                        val prev = segments.lastOrNull() as? TileSegment.Locked
                        if (prev != null) {
                            segments[segments.lastIndex] = prev.copy(text = prev.text + seg.correctForm)
                        } else {
                            segments += TileSegment.Locked(position++, seg.correctForm)
                        }
                    }
                }
            }
        }

        return TileExercise(segments, scaffolding)
    }

    private fun distractorsFor(
        lexeme: String,
        category: String,
        correctForm: String,
        morphology: MorphologyParadigm,
        random: RandomSource,
    ): List<String> {
        val candidates = morphology.formsOf("$category:$lexeme")
            .values
            .distinct()
            .filter { it != correctForm }
        if (candidates.size <= DISTRACTOR_COUNT) return candidates
        val pool = candidates.toMutableList()
        return List(DISTRACTOR_COUNT) {
            val index = (random.nextDouble() * pool.size).toInt().coerceIn(0, pool.size - 1)
            pool.removeAt(index)
        }
    }
}
