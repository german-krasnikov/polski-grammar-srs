package polski.core.model

/**
 * UniversalCorePlan.md-style: a sentence-builder exercise — [segments] in surface order, some
 * [TileSegment.Locked] (shown as plain text) and some [TileSegment.Choice] (the learner picks a
 * form). [scaffolding] is the progress-dependent level `TileGenerator` was called with when it
 * built this instance (`core-model` itself holds no progress knowledge).
 */
data class TileExercise(
    val segments: List<TileSegment>,
    val scaffolding: ScaffoldingLevel,
)

sealed class TileSegment {
    abstract val position: Int

    data class Locked(override val position: Int, val text: String) : TileSegment()

    /** [lexeme] is the slot's lexeme id (CoreExercise.slots[name]) — reused by the UI to
     *  re-derive the Morphology key together with [TileAnswer]'s own evaluator; the model
     *  stores no Morphology-internal key format. */
    data class Choice(
        override val position: Int,
        val correctForm: String,
        val lexeme: String,
        val targetFeature: FeatureKey,
        val alternatives: List<String>,
    ) : TileSegment()
}

enum class ScaffoldingLevel { Full, Partial, None }

data class TileSelection(val position: Int, val selectedForm: String)

data class TileError(
    val position: Int,
    val selected: String,
    val expected: String,
    val feature: FeatureKey,
    /** Always null out of core-engine's TileAnswerEvaluator (SB-3) and never read by
     *  `StyleComposer.blocksForError` (SB-7), which derives its own reason text from
     *  `Exercise.changes`/skill metadata instead of this field. Kept for a future evaluator that
     *  computes a reason closer to the evaluation itself. */
    val reason: String? = null,
)

data class TileAnswer(val selections: List<TileSelection>, val correct: Boolean, val errors: List<TileError>)
