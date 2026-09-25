package polski.progress

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant
import polski.srs.Rating
import polski.srs.SchedulePreview
import polski.srs.Scheduler
import polski.srs.StoredCard
import polski.srs.SrsCard

class ProgressCodecTest {
    private val at = Instant.parse("2026-03-29T01:30:00Z")
    private val scheduler = object : Scheduler {
        override fun newCard(skillId: String, at: Instant) = StoredCard(skillId, SrsCard(at))
        override fun preview(card: StoredCard, at: Instant) = SchedulePreview(at, at, at, at)
        override fun review(card: StoredCard, rating: Rating, at: Instant) = card
        override fun isDue(card: StoredCard, at: Instant) = false
    }

    @Test
    fun unknownPropertiesAndUnknownSkillsSurviveCompletionAndExport() {
        val raw = """{"version":1,"cards":[{"skillId":"future","card":{"due":"2026-03-29T01:30:00.000Z","stability":1,"difficulty":2,"elapsed_days":0,"scheduled_days":0,"reps":0,"lapses":0,"learning_steps":0,"state":2,"futureCard":"keep"},"wrapperExtra":true}],"stats":{"future":{"reviews":0,"correct":0,"streak":0,"mistakes":0,"futureStats":7}},"reviewsToday":0,"lastDay":"2026-03-29","totalReviews":0,"topExtra":{"nested":true}}"""
        val decoded = assertIs<DecodeResult.Valid>(ProgressCodec.decode(raw)).document
        val completed = ProgressCodec.completeKnownSkills(decoded, listOf("known"), at, scheduler)
        val export = ProgressCodec.encodeLegacyV1(completed)
        val tree = Json.parseToJsonElement(export).jsonObject

        assertEquals(2, completed.progress.cards.size)
        assertEquals("future", completed.progress.cards.first().skillId)
        assertEquals(Json.parseToJsonElement("""{"nested":true}"""), tree["topExtra"])
        assertTrue(export.contains("futureCard"))
        assertTrue(export.contains("wrapperExtra"))
        assertTrue(export.contains("futureStats"))
        assertIs<DecodeResult.Valid>(ProgressCodec.decode(export))
    }

    @Test
    fun duplicateUnsupportedAndInvalidKnownFieldsAreRejected() {
        val raw = ProgressCodec.encodeLegacyV1(ProgressCodec.fresh(listOf("known"), at, "2026-03-29", scheduler))
        assertIs<DecodeResult.Unsupported>(ProgressCodec.decode(raw.replace("\"version\":1", "\"version\":99")))
        val duplicate = raw.replace("\"cards\":[", "\"cards\":[${Json.parseToJsonElement(raw).jsonObject["cards"].toString().drop(1).dropLast(1)},")
        assertIs<DecodeResult.Invalid>(ProgressCodec.decode(duplicate))
        assertIs<DecodeResult.Invalid>(ProgressCodec.decode(raw.replace("\"state\":0", "\"state\":4")))
        assertIs<DecodeResult.Invalid>(ProgressCodec.decode(raw.replace("\"lastDay\":\"2026-03-29\"", "\"lastDay\":\"2026-02-30\"")))
        assertIs<DecodeResult.Invalid>(ProgressCodec.decode("{"))
    }

    @Test
    fun absentLastReviewRemainsAbsentAndDayNormalizationPreservesHistory() {
        val fresh = ProgressCodec.fresh(listOf("known"), at, "2026-03-28", scheduler)
        val normalized = ProgressCodec.normalizeDay(fresh.copy(progress = fresh.progress.copy(reviewsToday = 3, totalReviews = 5)), "2026-03-29")
        val export = ProgressCodec.encodeLegacyV1(normalized)
        assertEquals(0, normalized.progress.reviewsToday)
        assertEquals(5, normalized.progress.totalReviews)
        assertTrue(!export.contains("last_review"))
        assertIs<DecodeResult.Valid>(ProgressCodec.decode(export))
    }
}
