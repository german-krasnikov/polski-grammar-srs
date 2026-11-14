package polski.presentation

import polski.srs.Rating

/** Semantic UI actions; browser keyboard guards are applied by the host before dispatch. */
sealed interface AppAction {
    data class SelectTab(val tab: AppTab) : AppAction
    data class StartChain(val seedIndex: Int? = null) : AppAction
    data class SelectChainSeed(val index: Int) : AppAction
    data object StartSchedule : AppAction
    data object StartDaily : AppAction
    data object ContinueDaily : AppAction
    data object OpenSkillPicker : AppAction
    data class ChooseSkill(val skillId: String, val preferredSeed: polski.model.SentenceSeed? = null) : AppAction
    /** M10 blueprint: "Потренировать навык" after an exam mistake — [verificationItemId] is
     *  whatever the exam host resolved via `polski.exam.grammar.verificationItemFor` at the
     *  moment this was dispatched (`null` when no second linked item exists yet for [skillId]);
     *  `TrainingStore` only carries it forward on [AppUiState.examRetryOrigin], it never computes
     *  it itself (`TrainingStore` must not depend on `polski.exam`). */
    data class TrainExamMistake(val examRouteId: String, val itemId: String, val skillId: String, val verificationItemId: String?) : AppAction
    data class SetAnswerMode(val mode: AnswerMode) : AppAction
    data class SetStyle(val styleId: StyleId) : AppAction
    data object ContinueIntroduction : AppAction
    /** M7 blueprint: dismisses [AppUiState.predictionPending]; the existing [ContinueIntroduction]
     *  flow runs next, unchanged. */
    data object ContinuePrediction : AppAction
    data class EditAnswer(val text: String) : AppAction
    data class Reveal(val exerciseId: String) : AppAction
    data class Rate(val exerciseId: String, val rating: Rating) : AppAction
    /** SB-6 blueprint §1.2: carries one [TileAction] for the sentence-builder card. */
    data class Tile(val exerciseId: String, val action: TileAction) : AppAction
    data object ToggleReference : AppAction
    data class SelectMatrixSection(val section: MatrixSection) : AppAction
    data class SetMatrixSelection(val selection: MatrixSelection) : AppAction
    data object RequestExport : AppAction
    data object RequestMigration : AppAction
    data object RequestReset : AppAction
    data class ResetDecision(val effectId: Long, val confirmed: Boolean) : AppAction
    data object RefreshTime : AppAction
    data class EffectAcknowledged(val effectId: Long, val outcome: EffectOutcome) : AppAction
}
