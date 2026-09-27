package polski.presentation

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Flat, `kind`-tagged JSON for [Block]s, one object per block — the same shape iOS/macOS already
 * parse for [AppUiState]'s other snapshot sections (`matrix`, `supportRows`, …): no new native
 * Kotlin type crosses the Obj-C boundary, Swift just reads `kind` and picks a view. `kind` is
 * always the fixed lowercase [BlockKind] wire name.
 */
fun blocksToJson(blocks: List<Block>): JsonArray = JsonArray(blocks.map(::blockToJson))

private fun endingPartsJson(parts: List<EndingPart>): JsonArray = JsonArray(parts.map { part ->
    buildJsonObject { put("text", part.text); put("changed", part.isChanged) }
})

private fun blockToJson(block: Block): JsonObject = when (block) {
    is Block.Formula -> buildJsonObject { put("kind", "formula"); put("text", block.text); put("parts", endingPartsJson(block.parts)) }
    is Block.Rule -> buildJsonObject {
        put("kind", "rule"); put("text", block.text); put("detail", block.detail); put("parts", endingPartsJson(block.parts))
    }
    is Block.Table -> buildJsonObject {
        put("kind", "table")
        put("caption", block.caption)
        put("rows", JsonArray(block.rows.map { row ->
            buildJsonObject { put("label", row.label); put("before", endingPartsJson(row.before)); put("after", endingPartsJson(row.after)) }
        }))
    }
    is Block.Scene -> buildJsonObject { put("kind", "scene"); put("text", block.text); put("parts", endingPartsJson(block.parts)) }
    is Block.NativeParallel -> buildJsonObject {
        put("kind", "nativeParallel")
        put("pairs", JsonArray(block.pairs.map { pair ->
            buildJsonObject {
                put("native", pair.native); put("target", pair.target); put("note", pair.note); put("matches", pair.matches)
                put("targetParts", endingPartsJson(pair.targetParts))
            }
        }))
    }
    is Block.Examples -> buildJsonObject {
        put("kind", "examples")
        put("items", JsonArray(block.items.map(::JsonPrimitive)))
        put("itemParts", JsonArray(block.itemParts.map(::endingPartsJson)))
    }
    is Block.WhyOnDemand -> buildJsonObject {
        put("kind", "whyOnDemand"); put("text", block.text); put("collapsedLabel", block.collapsedLabel); put("parts", endingPartsJson(block.parts))
    }
    is Block.Changes -> buildJsonObject {
        put("kind", "changes")
        put("items", JsonArray(block.items.map { item ->
            buildJsonObject { put("before", endingPartsJson(item.before)); put("after", endingPartsJson(item.after)); put("reason", item.reason) }
        }))
    }
    is Block.Contrast -> buildJsonObject { put("kind", "contrast"); put("before", endingPartsJson(block.before)); put("after", endingPartsJson(block.after)) }
}
