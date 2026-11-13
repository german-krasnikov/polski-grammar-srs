package polski.presentation

import polski.core.engine.RandomSource
import polski.core.model.ScaffoldingLevel
import polski.core.model.TileAnswer
import polski.core.model.TileExercise
import polski.core.model.TileSelection
import polski.core.plTileGenerator
import polski.model.Exercise

/**
 * SB-4 blueprint §5 (grounds `SENTENCE-BUILDER-DESIGN.md` §8): the sentence-builder's own card
 * state, built from one [Exercise] whenever `AppUiState.answerMode == AnswerMode.TileBuilder`.
 * [placed] accumulates [TileAction.Place]/[TileAction.Remove]; [result] is set once
 * [TileAction.Check] runs `polski.core.engine.evaluateTileAnswer`; [errorExplanation] is filled by
 * `StyleComposer.blocksForError` (SB-7) from a [TileAction.TapError].
 */
data class TileBuilderState(
    val exercise: TileExercise,
    val placed: List<TileSelection> = emptyList(),
    val result: TileAnswer? = null,
    val errorExplanation: List<Block>? = null,
)

sealed class TileAction {
    data class Place(val position: Int, val form: String) : TileAction()
    data class Remove(val position: Int) : TileAction()
    object Check : TileAction()
    data class TapError(val position: Int) : TileAction()
}

/**
 * SB-4 blueprint §5: builds [TileBuilderState] for [exercise] via `TileGenerator.fromAnswerPhrase`
 * (`polski.core.plTileGenerator`) — the pure-data overload, so this call needs no `CoreExercise`.
 * `null` (fail-soft, AC4.3) whenever [exercise] opts out ([Exercise.tileTargetSlots] empty or
 * [Exercise.tilePhrase] absent — every skill today) or the phrase can't be located in
 * [Exercise.expected]; callers fall back to Typed/Oral the same way the rest of
 * `AnswerMode.TileBuilder` already does (see [AnswerMode]'s own KDoc) until SB-6+ wires a host UI.
 * [scaffolding] defaults to [ScaffoldingLevel.Partial] for direct/test callers; `TrainingStore`
 * (SB-8) resolves the real value from `SkillStats.tileScaffoldingRung` instead of relying on this
 * default.
 */
fun tileBuilderStateFor(exercise: Exercise, random: RandomSource, scaffolding: ScaffoldingLevel = ScaffoldingLevel.Partial): TileBuilderState? {
    // The same slots shape CoreExercise.slots carries (PlExerciseEngine.toExercise's own mapping, reversed) — ConstructionRealizer.slotFacts needs every slot the construction names, not just noun/adjective.
    val slots = mapOf("noun" to exercise.nounId, "adjective" to exercise.adjectiveId, "owner" to exercise.possessive.id, "number" to exercise.number.id)
    return plTileGenerator(random)
        .fromAnswerPhrase(exercise.primarySkill, exercise.expected, slots, exercise.tileTargetSlots, exercise.tilePhrase, scaffolding)
        ?.let { TileBuilderState(exercise = it) }
}
