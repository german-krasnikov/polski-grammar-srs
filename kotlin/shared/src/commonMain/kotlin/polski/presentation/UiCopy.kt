package polski.presentation

import polski.data.packRegistry

/**
 * Pack-aware UI string catalog. Each property reads the active pack's `uiCopy` map,
 * so switching packs changes all labels. Fallback = key name (visibly broken if a pack omits it).
 */
object UiCopy {
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
}

private fun text(key: String): String = packRegistry.active.uiCopy[key] ?: key
