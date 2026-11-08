package polski.ios

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import polski.data.referencePronounTeaching
import polski.data.personalPronouns
import polski.data.possessives
import polski.model.GramCase
import polski.presentation.AppUiState

class IosPronounTeachingSnapshotTest {
    @Test fun orderedCompactLabelsAndComputedPhrasesReachSwiftUi() {
        val teaching = referencePronounTeaching
        val matrix = Json.parseToJsonElement(snapshot(AppUiState())).jsonObject.getValue("matrix").jsonObject
        assertEquals(teaching.compactIntro, matrix.getValue("pronounIntro").jsonPrimitive.content)
        assertEquals(teaching.nativeFooter, matrix.getValue("pronounFooter").jsonPrimitive.content)
        val contexts = matrix.getValue("pronounContexts").jsonArray.map { it.jsonObject }
        assertEquals(teaching.contexts.map { it.cue.ios }, contexts.map { it.getValue("iosCue").jsonPrimitive.content })
        val rows = matrix.getValue("pronouns").jsonArray.map { it.jsonObject }
        assertEquals(teaching.pronounIds, rows.map { it.getValue("title").jsonPrimitive.content })
        rows.forEachIndexed { index, row ->
            val id = teaching.pronounIds[index]
            teaching.contexts.forEach { context ->
                val expected = if (context.id == GramCase.LOC) personalPronouns.getValue(id).getValue(GramCase.LOC)
                    else context.value(id, personalPronouns.getValue(id))
                assertEquals(expected, row.getValue(context.id.id).jsonPrimitive.content)
            }
        }
        assertEquals(teaching.possessiveTitle, matrix.getValue("possessiveTitle").jsonPrimitive.content)
        assertEquals(teaching.demo.cases.map { it.ios }, matrix.getValue("possessiveCases").jsonArray.map {
            it.jsonObject.getValue("iosCaption").jsonPrimitive.content
        })
        val owners = matrix.getValue("possessives").jsonArray.map { it.jsonObject }
        assertEquals(7, owners.size)
        owners.forEachIndexed { index, owner ->
            teaching.demo.cases.forEach { demoCase ->
                assertEquals(teaching.demo.phrase(possessives[index].id, demoCase.id),
                    owner.getValue(demoCase.id.id).jsonPrimitive.content)
            }
        }
    }
}
