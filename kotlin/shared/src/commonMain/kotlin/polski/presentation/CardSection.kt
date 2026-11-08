package polski.presentation

/**
 * One typed fragment of a training card, as [trainingCardModel] orders them in
 * [TrainingCardModel.front]/[TrainingCardModel.back]. Mirrors [Block]'s own file split — a plain
 * sealed interface, no HTML/Compose types, so every host renders each variant however fits its UI.
 */
sealed interface CardSection {
    /** A block [StyleComposer.compose] already produced for this phase — no per-[StyleId] branch
     *  belongs anywhere outside [StyleComposer]; a host reads [block] generically. */
    data class Styled(val block: Block) : CardSection

    /** The exercise's own source sentence, highlighted the "before" way ([ChangeSide.Before]). */
    data class SourceSentence(val parts: List<EndingPart>) : CardSection

    /** [prompt] is the exercise's own instruction; [lead] is the active method's lead-in line. */
    data class PromptBox(val prompt: String, val lead: String) : CardSection

    /** Shown only while [AppUiState.introPending] — the active method's own introduction text. */
    data class MethodIntro(val text: String) : CardSection

    /** The active method's retrieval prompt, shown while [CardPhase.Question]. */
    data class RetrieveHint(val text: String) : CardSection

    /** [mode] lets a host supply its own static oral-mode copy, or bind an editable [draft]. */
    data class AnswerInput(val mode: AnswerMode, val draft: String) : CardSection

    /** The one card-level button: "Перейти к заданию" ([AppAction.ContinueIntroduction]) or
     *  "Показать ответ"/"Проверить и показать ответ" ([AppAction.Reveal]), picked by [trainingCardModel]. */
    data class Action(val label: String, val action: AppAction) : CardSection

    /** The exercise's expected answer, highlighted the "after" way; [accepted] lists any other
     *  accepted phrasing. */
    data class Expected(val parts: List<EndingPart>, val accepted: List<String>) : CardSection

    /** Present only when [AnswerMode.Typed]; [matched] comes from `Evaluation.correct`.
     *  [answerText] is the raw frozen draft (possibly empty) — never a fallback phrase. */
    data class EvaluationFeedback(val matched: Boolean, val answerText: String) : CardSection

    /** Unconditional on back regardless of [StyleId]. */
    data class MethodFeedback(val feedback: String, val explanation: String) : CardSection

    /** Present only when the skill has at least one [Lifehack] — never an empty-list placeholder. */
    data class LifehackSection(val lifehacks: List<Lifehack>) : CardSection

    /** The active method's own "when to review" text. */
    data class ReviewHint(val text: String) : CardSection
}
