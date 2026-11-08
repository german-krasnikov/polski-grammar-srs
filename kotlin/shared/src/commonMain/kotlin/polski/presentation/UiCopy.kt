package polski.presentation

/**
 * Catalog of UI strings duplicated across 4-5 Kotlin hosts today (DSL-MIGRATION-PLAN.md block D).
 * Those hosts (web/desktop/Android) reference these constants instead of their own literal, so
 * changing a label is a one-place edit for them.
 *
 * iOS/macOS are out of scope here: their chrome labels (e.g. "Повторить"/"Вспомнил"/"Прогресс")
 * are plain Swift literals today, not values read from the `IosSnapshot`/`MacSnapshot`
 * `put(...)` bridge (that bridge only carries pack-authored *content* such as
 * `chainCompletionTitle`, never UI chrome). Routing these constants to Swift would need a new
 * bridge entry plus a Swift-side read for each one — a follow-up for the Swift developer, not a
 * drop-in addition to an existing `put(...)` call.
 *
 * Wording here is copied verbatim from each string's current majority host; this catalog does
 * not reconcile the cross-host inconsistencies the scan found elsewhere.
 */
object UiCopy {
    const val Again = "Повторить"
    const val Good = "Вспомнил"
    const val ProgressTab = "Прогресс"
    const val SettingsTab = "Настройки"
    const val Lifehacks = "Лайфхаки"
    const val ShowAnswer = "Показать ответ"
    const val ExportJson = "Экспорт JSON"
    const val ResetProgress = "Сбросить прогресс"
    const val HideTable = "Скрыть таблицу"
    const val TableAtHand = "Таблица под рукой"
    const val AllTablesAndSchema = "Все таблицы и схема"
    const val WhenToReview = "Когда повторить?"
    const val NoDueTitle = "Новых повторений пока нет."
    const val CompareAnswer = "Сравни свой ответ с эталоном"
}
