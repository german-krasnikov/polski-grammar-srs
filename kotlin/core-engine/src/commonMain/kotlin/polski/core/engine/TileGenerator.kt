package polski.core.engine

import polski.core.model.CoreExercise
import polski.core.model.FeatureBundle
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.core.model.ScaffoldingLevel
import polski.core.model.SkillSpec
import polski.core.model.TileExercise
import polski.core.model.TilePhrase
import polski.core.model.TileSegment

/**
 * SB-2 blueprint §3c/§1: decomposes one [CoreExercise]'s answer phrase ([CoreExercise.tilePhrase])
 * into sentence-builder tiles, on demand — `ScaffoldingLevel` is a progress-dependent fact
 * `ExerciseGenerator` doesn't have, so tiles are never stored on `CoreExercise` itself (see
 * blueprint §1). Fail-soft throughout: every precondition miss returns `null`, never throws —
 * Sentence Builder is additive UX and must never break the existing Typed/Oral path for the same
 * exercise.
 */
class TileGenerator(
    private val realizer: ConstructionRealizer,
    private val morphology: MorphologyParadigm,
    private val skills: Map<String, SkillSpec>,
    private val random: RandomSource,
) {
    fun fromExercise(exercise: CoreExercise, scaffolding: ScaffoldingLevel, distractorCount: Int = 3): TileExercise? =
        fromAnswerPhrase(
            exercise.primarySkill, exercise.expected, exercise.slots, exercise.tileTargetSlots, exercise.tilePhrase,
            scaffolding, distractorCount,
        )

    /** Pure-data overload — callable from `:shared` on `Exercise` once it carries the same fields
     *  (SB-4), without coupling [TileGenerator] to [CoreExercise]'s type. */
    fun fromAnswerPhrase(
        primarySkill: String,
        expected: String,
        slots: Map<String, String>,
        tileTargetSlots: List<String>,
        tilePhrase: TilePhrase?,
        scaffolding: ScaffoldingLevel,
        distractorCount: Int = 3,
    ): TileExercise? {
        if (tileTargetSlots.isEmpty() || tilePhrase == null) return null
        val offset = expected.indexOf(tilePhrase.text)
        if (offset < 0) return null // fail-soft: see TilePhrase KDoc

        val bundle: FeatureBundle = tilePhrase.bundle.entries.associate { (k, v) -> FeatureKey(k) to FeatureValue(v) }
        val facts = realizer.slotFacts(tilePhrase.construction, bundle, slots)
        val focusFeature = skills[primarySkill]?.focus?.feature

        // Which of the DSL-eligible slots actually become Choice THIS instance (scaffolding):
        val activeChoices = when (scaffolding) {
            ScaffoldingLevel.Full -> tileTargetSlots.take((tileTargetSlots.size + 1) / 3) // ~30% Choice, rest Locked
            ScaffoldingLevel.Partial, ScaffoldingLevel.None -> tileTargetSlots
        }.toSet()

        val segments = buildSegments(expected, offset, tilePhrase, activeChoices, facts, focusFeature, distractorCount)
        return TileExercise(segments, scaffolding)
    }

    private fun buildSegments(
        expected: String,
        offset: Int,
        phrase: TilePhrase,
        activeChoices: Set<String>,
        facts: Map<String, SlotFact>,
        focusFeature: FeatureKey?,
        distractorCount: Int,
    ): List<TileSegment> {
        val ordered = phrase.slotSpans.entries.sortedBy { it.value.first }
        val builders = mutableListOf<(Int) -> TileSegment>()
        if (offset > 0) {
            val prefix = expected.substring(0, offset)
            builders += { position -> TileSegment.Locked(position, prefix) }
        }
        var cursor = offset
        for ((name, span) in ordered) {
            val absStart = span.first + offset
            val absEndExclusive = span.last + 1 + offset
            if (absStart > cursor) {
                val gap = expected.substring(cursor, absStart)
                builders += { position -> TileSegment.Locked(position, gap) }
            }
            val correctForm = expected.substring(absStart, absEndExclusive)
            val fact = facts[name]
            if (name in activeChoices && fact != null) {
                val targetFeature = focusFeature ?: fact.requiredBundle.keys.firstOrNull() ?: FeatureKey(name)
                val alternatives = distractorsFor(fact, correctForm, distractorCount)
                builders += { position -> TileSegment.Choice(position, correctForm, fact.lexeme, targetFeature, alternatives) }
            } else {
                builders += { position -> TileSegment.Locked(position, correctForm) }
            }
            cursor = absEndExclusive
        }
        if (cursor < expected.length) {
            val suffix = expected.substring(cursor)
            builders += { position -> TileSegment.Locked(position, suffix) }
        }
        return builders.mapIndexed { index, build -> build(index) }
    }

    /** Real forms only — [MorphologyParadigm.formsOf] of the exact lexeme, never an invented string. */
    private fun distractorsFor(fact: SlotFact, correctForm: String, count: Int): List<String> {
        val candidates = morphology.formsOf("${fact.category}:${fact.lexeme}")
            .filterKeys { it != fact.requiredBundle }
            .values
            .distinct()
            .filter { it != correctForm } // syncretism: a form text-equal to the correct one is never a distractor
        if (candidates.size <= count) return candidates
        val pool = candidates.toMutableList()
        return List(count) {
            val index = (random.nextDouble() * pool.size).toInt().coerceIn(0, pool.size - 1)
            pool.removeAt(index)
        }
    }
}
