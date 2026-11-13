package polski.core.engine

import polski.core.model.TileAnswer
import polski.core.model.TileError
import polski.core.model.TileExercise
import polski.core.model.TileSegment
import polski.core.model.TileSelection

/**
 * SB-3 blueprint §4: exact-match grading for a [TileExercise] (never Levenshtein —
 * [TileSegment.Choice] already enumerates every legal form, so "close but wrong" is never
 * ambiguous the way free-typed answers are). A top-level function, not a class — no state/
 * dependencies, matching `:shared/training/AnswerEvaluator.kt`'s own shape but placed here
 * because [TileExercise]/[TileSelection] are `core-model` types and this logic needs zero
 * pl-specific knowledge; imports nothing from `:shared`, keeping the dependency direction
 * `:shared -> :core-engine`, never the reverse.
 */
fun evaluateTileAnswer(exercise: TileExercise, selections: List<TileSelection>): TileAnswer {
    val byPosition = selections.associateBy { it.position }
    val choices = exercise.segments.filterIsInstance<TileSegment.Choice>()
    val errors = choices.mapNotNull { choice ->
        val selected = byPosition[choice.position]?.selectedForm
        if (selected == null || selected == choice.correctForm) null
        else TileError(choice.position, selected, choice.correctForm, choice.targetFeature)
    }
    val allFilled = choices.all { it.position in byPosition }
    return TileAnswer(selections, correct = allFilled && errors.isEmpty(), errors = errors)
}
