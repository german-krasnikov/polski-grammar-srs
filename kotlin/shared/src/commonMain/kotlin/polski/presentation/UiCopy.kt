package polski.presentation

import polski.data.packRegistry

/**
 * Pack-aware UI string catalog. Each property reads the active pack's `uiCopy` map,
 * so switching packs changes all labels. Fallback = key name (visibly broken if a pack omits it).
 */
object UiCopy {
    /** Complete read-only label snapshot for the currently selected course pack. */
    fun all(): Map<String, String> = packRegistry.active.uiCopy.toMap()

    val Again: String get() = text("again")
    val Good: String get() = text("good")
    val ProgressTab: String get() = text("progressTab")
    val SettingsTab: String get() = text("settingsTab")
    val Lifehacks: String get() = text("lifehacks")
    val ShowAnswer: String get() = text("showAnswer")
    val CheckAndShowAnswer: String get() = text("checkAndShowAnswer")
    val ExportJson: String get() = text("exportJson")
    val ResetProgress: String get() = text("resetProgress")
    val HideTable: String get() = text("hideTable")
    val TableAtHand: String get() = text("tableAtHand")
    val AllTablesAndSchema: String get() = text("allTablesAndSchema")
    val WhenToReview: String get() = text("whenToReview")
    val NoDueTitle: String get() = text("noDueTitle")
    val CompareAnswer: String get() = text("compareAnswer")
    val BackSideReference: String get() = text("backSideReference")
    val NoAnswerEntered: String get() = text("noAnswerEntered")
    val Matches: String get() = text("matches")
    val Differs: String get() = text("differs")
    val SelectExam: String get() = text("selectExam")
    val Start: String get() = text("start")
    val Next: String get() = text("next")
    val Finish: String get() = text("finish")
    val TryAgain: String get() = text("tryAgain")
    val Task: String get() = text("task")
    val Grammar: String get() = text("grammar")
    val Listening: String get() = text("listening")
    val Reading: String get() = text("reading")
    val Writing: String get() = text("writing")
    val WritingInstructionsAndCriteria: String get() = text("writingInstructionsAndCriteria")
    val SelectWritingSet: String get() = text("selectWritingSet")
    val ChangeWritingSet: String get() = text("changeWritingSet")
    val WritingSet: String get() = text("writingSet")
    val Results: String get() = text("results")
    val ExamConfirmedPoints: String get() = text("examConfirmedPoints")
    val ExamPendingPoints: String get() = text("examPendingPoints")
    val ExamManualReview: String get() = text("examManualReview")
    val ExamImmediateFeedbackSetting: String get() = text("examImmediateFeedbackSetting")
    val ExamCheck: String get() = text("check")
    val ExamYourAnswer: String get() = text("examYourAnswer")
    val ExamAnswerLocked: String get() = text("examAnswerLocked")
    val ExamFeedbackCorrect: String get() = text("examFeedbackCorrect")
    val ExamFeedbackPartial: String get() = text("examFeedbackPartial")
    val ExamFeedbackWrong: String get() = text("examFeedbackWrong")
    val ExamOfficialExamples: String get() = text("examOfficialExamples")
    val ExamTimedStart: String get() = text("examTimedStart")
    val ExamTimedPart: String get() = text("examTimedPart")
    val ExamTimedComplete: String get() = text("examTimedComplete")
    val ExamBackToModules: String get() = text("examBackToModules")
    val ExamPrevious: String get() = text("examPrevious")
    val ExamAnswerPrompt: String get() = text("examAnswerPrompt")
    val ExamNativeUnavailable: String get() = text("examNativeUnavailable")
    val ExamChooseModule: String get() = text("examChooseModule")
    val ComingSoon: String get() = text("comingSoon")
    val Exam: String get() = text("exam")
    val CardsTab: String get() = text("cardsTab")
    val VocabularyTab: String get() = text("vocabularyTab")
    val MatrixTab: String get() = text("matrixTab")

    fun languageName(code: String): String = text("lang.$code")
    fun languageAdverb(code: String): String = text("adverb.$code")
    fun tenseLabel(tense: String): String = text("tense.$tense")
}

private fun text(key: String): String = packRegistry.active.uiCopy[key] ?: key
