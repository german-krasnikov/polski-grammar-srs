package polski.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * S4 (EmphasisUXAudit E7): [blocksToJson] must carry the same explicit-pair highlight parts iOS/
 * macOS need to render "before"/"after" roles in a style block's own prose — the flat, `kind`-
 * tagged shape every host bridge already reads (see [blocksToJson]'s own doc).
 */
class StyleSnapshotTest {
    private val parts = listOf(EndingPart("mojej", false, true), EndingPart(" and ", false), EndingPart("ich", false, true))

    @Test fun formulaRuleSceneWhyOnDemandExportTheirOwnParts() {
        val json = blocksToJson(listOf(
            Block.Formula("mojej and ich", parts),
            Block.Rule("mojej and ich", "detail", parts),
            Block.Scene("mojej and ich", parts),
            Block.WhyOnDemand("mojej and ich", parts, "collapsed"),
        )).map { it.jsonObject }
        json.forEach { obj ->
            val exported = obj["parts"]!!.jsonArray.map { it.jsonObject["text"]!!.jsonPrimitive.content }
            assertEquals(listOf("mojej", " and ", "ich"), exported)
        }
    }

    @Test fun examplesExportItemsAndParallelItemParts() {
        val json = blocksToJson(listOf(Block.Examples(listOf("mojej and ich"), listOf(parts))))
            .single().jsonObject
        assertEquals("mojej and ich", json["items"]!!.jsonArray.single().jsonPrimitive.content)
        val exportedParts = json["itemParts"]!!.jsonArray.single().jsonArray.map { it.jsonObject["text"]!!.jsonPrimitive.content }
        assertEquals(listOf("mojej", " and ", "ich"), exportedParts)
    }

    @Test fun nativeParallelExportsTargetPartsButNeverColoursNative() {
        val pair = NativeParallelPair("native prose stays plain", "mojej and ich", "note", true, parts)
        val json = blocksToJson(listOf(Block.NativeParallel(listOf(pair)))).single().jsonObject
        val exportedPair = json["pairs"]!!.jsonArray.single().jsonObject
        assertEquals("native prose stays plain", exportedPair["native"]!!.jsonPrimitive.content)
        assertEquals(listOf("mojej", " and ", "ich"), exportedPair["targetParts"]!!.jsonArray.map { it.jsonObject["text"]!!.jsonPrimitive.content })
        // The native side carries no parallel "nativeParts" key — the model gives L1 prose no parts field at all.
        assertEquals(false, exportedPair.containsKey("nativeParts"))
    }
}
