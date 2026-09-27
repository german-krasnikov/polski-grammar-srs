package polski.presentation

import polski.srs.Rating

/** Semantic UI actions; browser keyboard guards are applied by the host before dispatch. */
sealed interface AppAction {
    data class SelectTab(val tab: AppTab) : AppAction
    data class StartChain(val seedIndex: Int? = null) : AppAction
    data class SelectChainSeed(val index: Int) : AppAction
    data object StartSchedule : AppAction
    data object OpenSkillPicker : AppAction
    data class ChooseSkill(val skillId: String, val preferredSeed: polski.model.SentenceSeed? = null) : AppAction
    data class SetAnswerMode(val mode: AnswerMode) : AppAction
    data class SetStyle(val styleId: StyleId) : AppAction
    data object ContinueIntroduction : AppAction
    data class EditAnswer(val text: String) : AppAction
    data class Reveal(val exerciseId: String) : AppAction
    data class Rate(val exerciseId: String, val rating: Rating) : AppAction
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
