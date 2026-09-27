package polski.progress

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant
import polski.srs.FsrsScheduler
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

    /** UC-04: golden pin — [ProgressCodec] never read a `"pl-ru"` literal, but this proves the
     *  v1 wire format (the only thing `activePackFilter`/[SkillQueue] must never disturb) is
     *  still exactly reproduced by decode -> encode. */
    @Test
    fun goldenDocumentRoundTripsByteIdentical() {
        // A single reviewed card only — an unreviewed one's stability/difficulty stay the exact
        // Double zero, which JS prints as "0" and JVM/Native as "0.0"; every FSRS-computed value
        // below is non-integral and prints identically on every target.
        val fsrs = FsrsScheduler()
        val fresh = ProgressCodec.fresh(listOf("case.acc.f"), at, "2026-03-29", fsrs)
        val reviewed = ReviewReducer(fsrs, setOf("case.acc.f"))
            .recordReview(ProgressDocument(fresh.progress), "case.acc.f", Rating.Good, null, at, "2026-03-29")
        val document = (reviewed as ReviewResult.Reviewed).document
        val golden = """{"version":1,"cards":[{"skillId":"case.acc.f","card":{"due":"2026-03-29T01:40:00.000Z",""" +
            """"stability":2.3065,"difficulty":2.11810397,"elapsed_days":0,"scheduled_days":0,"reps":1,"lapses":0,""" +
            """"learning_steps":1,"state":1,"last_review":"2026-03-29T01:30:00.000Z"}}],"stats":{"case.acc.f":""" +
            """{"reviews":1,"correct":1,"streak":1,"mistakes":0}},"reviewsToday":1,"lastDay":"2026-03-29","totalReviews":1}"""
        assertEquals(golden, ProgressCodec.encodeLegacyV1(document), "encodeLegacyV1 drifted from the pinned golden bytes")
        val decoded = assertIs<DecodeResult.Valid>(ProgressCodec.decode(golden)).document
        assertEquals(golden, ProgressCodec.encodeLegacyV1(decoded), "decode -> encode is not byte-identical")
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
