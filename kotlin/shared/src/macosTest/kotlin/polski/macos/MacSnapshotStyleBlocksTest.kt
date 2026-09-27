package polski.macos

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import platform.CoreFoundation.CFRunLoopRunInMode
import platform.CoreFoundation.kCFRunLoopDefaultMode
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID

/**
 * macOS S2: the training card (`MacFlashCardView.swift`) renders `styleBlocks.frontBlocks`/
 * `backBlocks` generically per `BlockKind` instead of the old hardcoded Formula/Changes-only
 * layout. This locks the exact seam Swift reads: switching `styleId` on the same exercise must
 * change which block kinds `MacSnapshot.kt`'s `styleBlocksSnapshot` returns, and switching alone
 * (no reveal, no rate) must never create a review or move the exercise.
 */
@OptIn(ExperimentalForeignApi::class)
class MacSnapshotStyleBlocksTest {
    private fun withSession(block: (MacSession) -> Unit) {
        val directory = NSTemporaryDirectory() + "polski-mac-style-blocks-${NSUUID().UUIDString}"
        val session = MacSession(directory)
        try {
            awaitReady(session)
            session.dispatch("continueIntroduction")
            block(session)
        } finally {
            session.close()
            NSFileManager.defaultManager.removeItemAtPath(directory, null)
        }
    }

    private fun awaitReady(session: MacSession) {
        repeat(200) {
            if (state(session).getValue("loadStatus").jsonPrimitive.content == "Ready") return
            CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.02, true)
        }
        error("MacSession did not reach Ready in time")
    }

    private fun state(session: MacSession) = Json.parseToJsonElement(session.currentSnapshot()).jsonObject
    private fun exerciseId(session: MacSession) = state(session).getValue("exercise").jsonObject.getValue("id").jsonPrimitive.content
    private fun phase(session: MacSession) = state(session).getValue("phase").jsonPrimitive.content

    private fun blockKinds(session: MacSession, field: String): List<String> =
        state(session).getValue("styleBlocks").jsonObject.getValue(field).jsonArray
            .map { it.jsonObject.getValue("kind").jsonPrimitive.content }

    @Test
    fun switchingStyleChangesWhichBackBlockKindsAreShown() = withSession { session ->
        session.dispatch("styleId", "RuleFirst")
        val ruleFirstBack = blockKinds(session, "backBlocks")

        session.dispatch("styleId", "SituationFirst")
        val situationFirstBack = blockKinds(session, "backBlocks")

        session.dispatch("styleId", "MinimalTheory")
        val minimalTheoryBack = blockKinds(session, "backBlocks")

        assertNotEquals(ruleFirstBack, situationFirstBack, "rule-first and situation-first must show different back blocks")
        assertNotEquals(ruleFirstBack, minimalTheoryBack, "rule-first and minimal-theory must show different back blocks")
        assertTrue("formula" in ruleFirstBack && "rule" in ruleFirstBack && "contrast" in ruleFirstBack)
        assertEquals(listOf("changes"), situationFirstBack, "situation-first has no styleContent yet, so only the exercise-data Changes block shows")
        assertTrue("whyOnDemand" in minimalTheoryBack)
        // Changes is exercise data, not style data (ST-05): every style still carries it.
        assertTrue("changes" in ruleFirstBack && "changes" in minimalTheoryBack)
    }

    @Test
    fun switchingStyleChangesWhichFrontBlockKindsAreShown() = withSession { session ->
        session.dispatch("styleId", "RuleFirst")
        val ruleFirstFront = blockKinds(session, "frontBlocks")

        session.dispatch("styleId", "SituationFirst")
        val situationFirstFront = blockKinds(session, "frontBlocks")

        assertEquals(listOf("formula", "table"), ruleFirstFront)
        assertEquals(listOf("scene"), situationFirstFront)
    }

    @Test
    fun nativeContrastWithoutSkillContentFallsBackToRuleFirstsBlocks() = withSession { session ->
        session.dispatch("styleId", "RuleFirst")
        val ruleFirstFront = blockKinds(session, "frontBlocks")
        val ruleFirstBack = blockKinds(session, "backBlocks")

        session.dispatch("styleId", "NativeContrast")

        assertEquals("RuleFirst", state(session).getValue("styleBlocks").jsonObject.getValue("effectiveStyleId").jsonPrimitive.content)
        assertEquals(ruleFirstFront, blockKinds(session, "frontBlocks"))
        assertEquals(ruleFirstBack, blockKinds(session, "backBlocks"))
    }

    @Test
    fun switchingStyleNeverCreatesAReviewOrMovesTheExercise() = withSession { session ->
        val exerciseId = exerciseId(session)
        val phaseBefore = phase(session)

        session.dispatch("styleId", "SituationFirst")
        session.dispatch("styleId", "NativeContrast")
        session.dispatch("styleId", "MinimalTheory")

        assertEquals(exerciseId, exerciseId(session))
        assertEquals(phaseBefore, phase(session))
        assertEquals(0, state(session).getValue("totalReviews").jsonPrimitive.content.toInt())
    }
}
