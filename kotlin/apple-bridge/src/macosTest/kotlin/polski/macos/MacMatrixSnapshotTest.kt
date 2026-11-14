package polski.macos

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import polski.data.maleAccRows
import polski.data.referenceVerbTeaching
import polski.grammar.caseRows
import polski.model.Tense
import polski.presentation.AppUiState

/**
 * `Plans/DSL-DEDUP-MIGRATION.md` §3 AC1 flagged gap: before this extraction no test asserted
 * `MacMatrixSnapshot.kt`'s own copy of the fields that now live once in
 * `polski.apple.putCommonMatrixFields` (`apple-bridge/src/appleMain`) — so sharing that block with
 * iOS (already covered by `IosMatrixSnapshotTest`) would have been an unverified assumption on the
 * macOS side. This proves the shared block actually renders through `MacMatrixSnapshot.kt`'s own
 * `matrixSnapshot(state)`.
 */
class MacMatrixSnapshotTest {
    @Test
    fun sharedMatrixFieldsRenderThroughTheMacHost() {
        val matrix = Json.parseToJsonElement(snapshot(AppUiState())).jsonObject.getValue("matrix").jsonObject

        assertEquals(AppUiState().matrixSelection.section.name, matrix.getValue("section").jsonPrimitive.content)
        assertTrue(matrix.getValue("matrixIntroduction").jsonPrimitive.content.isNotBlank())
        assertTrue(matrix.getValue("nouns").jsonArray.isNotEmpty())
        assertTrue(matrix.getValue("adjectives").jsonArray.isNotEmpty())
        assertTrue(matrix.getValue("owners").jsonArray.isNotEmpty())
        assertEquals(2, matrix.getValue("numbers").jsonArray.size)
        assertTrue(matrix.getValue("verbs").jsonArray.isNotEmpty())
        assertTrue(matrix.getValue("pipelineTitle").jsonPrimitive.content.isNotBlank())
        assertTrue(matrix.getValue("supportRows").jsonArray.isNotEmpty())
        assertTrue(matrix.getValue("caseNote").jsonPrimitive.content.isNotBlank())
        assertTrue(matrix.getValue("systemCards").jsonArray.isNotEmpty())

        assertEquals(maleAccRows.size, matrix.getValue("maleAccRows").jsonArray.size)
        assertEquals(caseRows.map { it.id.name }, matrix.getValue("comparisonCases").jsonArray.map { it.jsonObject.getValue("id").jsonPrimitive.content })

        val teaching = referenceVerbTeaching
        assertEquals(teaching.genderControlLabel.compact, matrix.getValue("verbGenderControlLabel").jsonPrimitive.content)
        assertEquals(teaching.compactFutureExplanation, matrix.getValue("verbFutureExplanation").jsonPrimitive.content)
        Tense.entries.forEach { tense ->
            assertEquals(teaching.tenseLabels.getValue(tense).compact,
                matrix.getValue("verbTenseLabels").jsonObject.getValue(tense.id).jsonPrimitive.content)
        }

        assertTrue(matrix.getValue("pronounIntro").jsonPrimitive.content.isNotBlank())
        assertTrue(matrix.getValue("pronounContexts").jsonArray.isNotEmpty())
        assertTrue(matrix.getValue("possessiveTitle").jsonPrimitive.content.isNotBlank())
        assertTrue(matrix.getValue("possessiveCases").jsonArray.isNotEmpty())
    }
}
