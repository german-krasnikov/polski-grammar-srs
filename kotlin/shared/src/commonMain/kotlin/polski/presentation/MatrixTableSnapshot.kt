package polski.presentation

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * UC-09 part 2/2: the generic wire shape every host reads for any [MatrixTableViewModel] — a
 * header row plus data rows, each cell either plain text or a "было → стало" pair shaped exactly
 * like every other contrast pair on the wire (`from`/`to`/`beforeParts`/`afterParts`, see
 * [contrastPairJson]), so a host's existing pair-rendering code reads a matrix cell unchanged.
 */
fun MatrixTableViewModel.toJson(): JsonObject = buildJsonObject {
    put("rowHeaderLabel", rowHeaderLabel)
    put("columnHeaders", JsonArray(columnHeaders.map(::JsonPrimitive)))
    put("rows", JsonArray(rows.map { row ->
        buildJsonObject {
            put("header", row.header)
            put("cells", JsonArray(row.cells.map { cell ->
                buildJsonObject {
                    put("value", cell.value)
                    put("contrast", cell.contrast?.let(::contrastPairJson) ?: JsonNull)
                }
            }))
        }
    }))
}

/** Shared shape for one [ContrastPair] on the wire — used by every snapshot section that sends a
 *  "было → стало" pair, not only matrix tables. */
fun contrastPairJson(pair: ContrastPair): JsonObject = buildJsonObject {
    put("from", pair.from)
    put("to", pair.to)
    put("beforeParts", JsonArray(pair.parts(ChangeSide.Before).map { part ->
        buildJsonObject { put("text", part.text); put("changed", part.isChanged) }
    }))
    put("afterParts", JsonArray(pair.parts(ChangeSide.After).map { part ->
        buildJsonObject { put("text", part.text); put("changed", part.isChanged) }
    }))
}
