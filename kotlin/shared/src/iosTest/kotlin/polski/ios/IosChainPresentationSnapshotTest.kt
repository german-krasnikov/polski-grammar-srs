package polski.ios

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import polski.presentation.AppUiState
import polski.presentation.CardPhase
import polski.training.PlExerciseEngine
import polski.core.engine.ExerciseIdFactory
import polski.core.engine.RandomSource
import polski.training.sentenceSeeds

class IosChainPresentationSnapshotTest {
    @Test fun completionSnapshotShowsAllFiveRatedSteps() {
        val chain = PlExerciseEngine(RandomSource { 0.1 }, ExerciseIdFactory { "ios-display" })
            .generateChain(sentenceSeeds.first())
        val state = AppUiState(chain = chain, chainIndex = 4, phase = CardPhase.ChainComplete)
        val snapshot = Json.parseToJsonElement(snapshot(state)).jsonObject
        assertEquals(4, snapshot.getValue("chainIndex").jsonPrimitive.content.toInt())
        assertEquals(5, snapshot.getValue("chainDisplayCount").jsonPrimitive.content.toInt())
    }

    @Test fun trainingSnapshotCarriesOrderedLabelsSummaryAndNativeTitle() {
        val state = Json.parseToJsonElement(snapshot(AppUiState())).jsonObject
        assertEquals(
            listOf("Вижу", "Прошлое", "Отрицание", "Владелец", "Говорю о"),
            state.getValue("chainStepLabels").jsonArray.map { it.jsonPrimitive.content },
        )
        assertEquals(
            "Вижу → Прошлое → Отрицание → Владелец → Говорю о",
            state.getValue("chainStepSummary").jsonPrimitive.content,
        )
        assertEquals("Цепочка завершена", state.getValue("chainCompletionTitle").jsonPrimitive.content)
    }
}
