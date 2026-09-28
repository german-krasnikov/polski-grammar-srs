package polski.data

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * EN-04 (Plans/Kotlin/EnRuPackPlan.md §6): a pack reconstructed from its v2 layer files
 * (`lang/pl/lexicon.json` + `pairs/pl-ru/pair.json`) must be the exact same [CoursePack] contract
 * as the same pack read from the v1 `course.json` it was split from (ADR-20's
 * `scripts/migrate-v1-to-v2.mjs` already proves the JSON-level round trip Node-side; this is the
 * Kotlin-side fixture comparison the ADR names as the follow-up task).
 */
class CoursePackLoaderTest {
    private val v1Source = embeddedCoursePackSources.first { it.id == "pl-ru" }
    private val v2Source = CoursePackLoader.fromV2Layers(
        pairId = "pl-ru",
        lexiconJson = generatedLexiconJsonByLang.getValue("pl"),
        pairJson = generatedPairJsonByPairId.getValue("pl-ru"),
    )

    @Test fun v2LayersReconstructTheExactV1JsonDocument() {
        val v1Json = Json.parseToJsonElement(v1Source.load())
        val v2Json = Json.parseToJsonElement(v2Source.load())
        assertEquals(v1Json, v2Json)
    }

    @Test fun v2ReconstructedPackParsesToTheSameCoursePackContentAsV1() {
        val v1 = CoursePack(v1Source)
        val v2 = CoursePack(v2Source)
        assertEquals(v1.id, v2.id)
        assertEquals(v1.pairId, v2.pairId)
        assertEquals(v1.nouns, v2.nouns)
        assertEquals(v1.adjectives, v2.adjectives)
        assertEquals(v1.verbs, v2.verbs)
        assertEquals(v1.skills, v2.skills)
        assertEquals(v1.sentenceSeeds, v2.sentenceSeeds)
        assertEquals(v1.vocabulary, v2.vocabulary)
        assertEquals(v1.personalPronouns, v2.personalPronouns)
        assertEquals(v1.possessives, v2.possessives)
    }
}
