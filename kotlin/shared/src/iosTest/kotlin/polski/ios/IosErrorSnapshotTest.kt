package polski.ios

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import polski.presentation.AppUiState
import polski.presentation.LoadStatus

/**
 * Locks the contract the Swift host relies on for C1: a save failure sets [AppUiState.error]
 * while [AppUiState.loadStatus] stays [LoadStatus.Ready] (the user keeps training), and the
 * snapshot must still expose that error so the host can show it regardless of load status.
 */
class IosErrorSnapshotTest {
    @Test
    fun snapshotExposesErrorEvenWhileLoadStatusIsReady() {
        val message = "Не удалось сохранить прогресс. Экспортируй JSON перед закрытием."
        val state = AppUiState(loadStatus = LoadStatus.Ready, error = message)

        val json = Json.parseToJsonElement(snapshot(state)).jsonObject

        assertEquals("Ready", json.getValue("loadStatus").jsonPrimitive.content)
        assertEquals(message, json.getValue("error").jsonPrimitive.content)
    }
}
