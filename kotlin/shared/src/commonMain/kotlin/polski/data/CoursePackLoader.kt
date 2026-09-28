package polski.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import polski.pack.CoursePackSource

/**
 * EN-04/ADR-20 (UC-12): the Kotlin-side counterpart of `scripts/migrate-v1-to-v2.mjs`'s
 * `reconstructCoursePack` — merges the two v2 layer files (`lang/<code>/lexicon.json` +
 * `pairs/<pairId>/pair.json`) back into the same v1-shaped JSON [CoursePack] already parses, so a
 * pack can be loaded from either layout without [CoursePack] itself changing. The merge is
 * mechanical, not a reinterpretation: every lexicon field except `schemaVersion` (the pair's own
 * `schemaVersion` wins, matching the Node script) overlays the pair's fields, none renamed or
 * reshaped. ADR-20 named this loader "not built yet" — this is it.
 */
internal object CoursePackLoader {
    /** [pairId] becomes the returned source's `id` (and so [CoursePack.id]/[CoursePack.pairId]). */
    fun fromV2Layers(pairId: String, lexiconJson: String, pairJson: String): CoursePackSource {
        val lexicon = Json.parseToJsonElement(lexiconJson).jsonObject
        val pair = Json.parseToJsonElement(pairJson).jsonObject
        val json = JsonObject(pair + lexicon.filterKeys { it != "schemaVersion" }).toString()
        return object : CoursePackSource {
            override val id: String = pairId
            override fun load(): String = json
        }
    }
}
