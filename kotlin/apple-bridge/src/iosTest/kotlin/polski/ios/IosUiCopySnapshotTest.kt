package polski.ios

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.random.Random
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import polski.data.selectActiveCoursePack
import polski.presentation.AppUiState
import platform.Foundation.NSUserDefaults

class IosUiCopySnapshotTest {
    @Test
    fun styleRecipesSupplyAllCurrentLabelsAndDescriptionsWithoutSwiftFallback() {
        val suite = "polski-ios-style-copy-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            for (pack in listOf("pl-ru", "en-ru")) {
                selectActiveCoursePack(pack)
                val styles = Json.parseToJsonElement(IosPreferencesSession(defaults).currentSnapshot())
                    .jsonObject.getValue("styles").jsonArray
                assertEquals(4, styles.size, pack)
                assertTrue(styles.all { row ->
                    row.jsonObject.getValue("label").jsonPrimitive.content.isNotBlank() &&
                        row.jsonObject.getValue("description").jsonPrimitive.content.isNotBlank()
                }, pack)
            }
        } finally {
            selectActiveCoursePack("pl-ru")
            defaults.removePersistentDomainForName(suite)
        }
    }

    @Test
    fun snapshotRebuildsPackCopyWhilePreservingLegacyFields() {
        try {
            selectActiveCoursePack("pl-ru")
            val pl = Json.parseToJsonElement(snapshot(AppUiState())).jsonObject
            val legacy = mapOf(
                "uiResetProgress" to "Сбросить прогресс",
                "uiProgressTab" to "Прогресс",
                "uiSettingsTab" to "Настройки",
                "uiLifehacks" to "Лайфхаки",
                "uiAgain" to "Повторить",
                "uiGood" to "Вспомнил",
                "uiShowAnswer" to "Показать ответ",
            )
            for ((key, value) in legacy) assertEquals(value, pl.getValue(key).jsonPrimitive.content)
            assertTrue(pl.getValue("uiCopy").jsonObject.values.all { it.jsonPrimitive.isString })
            assertEquals("Тренировка", pl.getValue("uiCopy").jsonObject.getValue("trainingTab").jsonPrimitive.content)
            assertEquals("Прогресс экспортирован", pl.getValue("uiCopy").jsonObject.getValue("progressExported").jsonPrimitive.content)
            assertEquals("Совпадает с правильным вариантом", pl.getValue("uiCopy").jsonObject.getValue("matches").jsonPrimitive.content)

            selectActiveCoursePack("en-ru")
            val en = Json.parseToJsonElement(snapshot(AppUiState())).jsonObject
            for ((key, value) in legacy) assertEquals(value, en.getValue(key).jsonPrimitive.content)
            assertEquals("Training", en.getValue("uiCopy").jsonObject.getValue("trainingTab").jsonPrimitive.content)
            assertEquals("Next: {date}", en.getValue("uiCopy").jsonObject.getValue("nextDueTemplate").jsonPrimitive.content)
            assertEquals("Progress exported", en.getValue("uiCopy").jsonObject.getValue("progressExported").jsonPrimitive.content)
            assertEquals("Vocabulary import failed", en.getValue("uiCopy").jsonObject.getValue("vocabularyImportFailed").jsonPrimitive.content)
            assertEquals("Matches", en.getValue("uiCopy").jsonObject.getValue("matches").jsonPrimitive.content)
            assertEquals("Differs", en.getValue("uiCopy").jsonObject.getValue("differs").jsonPrimitive.content)

            selectActiveCoursePack("pl-ru")
            val restored = Json.parseToJsonElement(snapshot(AppUiState())).jsonObject
            assertEquals("Тренировка", restored.getValue("uiCopy").jsonObject.getValue("trainingTab").jsonPrimitive.content)
        } finally {
            selectActiveCoursePack("pl-ru")
        }
    }
}
