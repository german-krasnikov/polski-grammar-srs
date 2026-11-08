package polski.macos

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
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
        assertTrue("rule" in ruleFirstBack && "contrast" in ruleFirstBack)
        // C3 (EmphasisUXAudit E10): formula already shows on front, which stays visible after
        // reveal — back must not repeat it, or it would render twice on the revealed card.
        assertTrue("formula" !in ruleFirstBack)
        assertEquals(listOf("changes", "rule"), situationFirstBack, "situation-first.json's back is [changes, rule] — Rule (from skill.theory) always follows Changes")
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

        assertEquals(listOf("table", "formula"), ruleFirstFront, "rule-first.json's front order is [table, formula]")
        assertEquals(listOf("scene"), situationFirstFront)
    }

    // CONTENT has since authored `nativeParallel` for every real skill (UC-10 §3/ST-11), so the
    // session's current skill never lacks it any more and NativeContrast no longer falls back to
    // RuleFirst on the real course — StyleComposerTest.nativeContrastFallsBackWithoutContentAndComposesWithIt
    // still covers the fallback itself with a literal SkillStyleContent() fixture. This now checks
    // the other half: with real authored content, NativeContrast shows its own blocks, not RuleFirst's.
    @Test
    fun nativeContrastWithRealSkillContentDoesNotFallBackToRuleFirst() = withSession { session ->
        session.dispatch("styleId", "RuleFirst")
        val ruleFirstFront = blockKinds(session, "frontBlocks")

        session.dispatch("styleId", "NativeContrast")

        assertEquals("NativeContrast", state(session).getValue("styleBlocks").jsonObject.getValue("effectiveStyleId").jsonPrimitive.content)
        assertEquals(listOf("nativeParallel"), blockKinds(session, "frontBlocks"))
        assertNotEquals(ruleFirstFront, blockKinds(session, "frontBlocks"))
    }

    // EN-21 (`Plans/Kotlin/EnRuPackPlan.md` §4.2/§4.3/§6): `styleBlocks.lifehacks` is a separate
    // field from `frontBlocks`/`backBlocks` — a lifehack is not a `BlockKind`, it shows the same
    // way for every style (`MacFlashCardView`'s `backFace` renders it once, outside `MacStyleBlockView`'s
    // switch). `case.inst`'s original EN-20 record (still first — the §4.4/§7 follow-up only ever
    // appends) has no `source.url`, so this also locks the optional-url shape end to end.
    @Test
    fun backBlocksLifehacksMatchTheActiveSkillsAuthoredPackLifehack() = withSession { session ->
        session.dispatch("skill", "case.inst")

        val lifehacks = state(session).getValue("styleBlocks").jsonObject.getValue("lifehacks").jsonArray
        assertEquals(2, lifehacks.size)
        val hack = lifehacks[0].jsonObject
        assertTrue(hack.getValue("text").jsonPrimitive.content.contains("być"))
        assertEquals("editorial", hack.getValue("status").jsonPrimitive.content)
        assertTrue(hack.getValue("citation").jsonPrimitive.content.startsWith("Bielec, D. (1998)"))
        assertEquals(JsonNull, hack.getValue("url"))
    }

    // Full 16-skill pl-ru coverage (EnRuPackPlan.md §4.4/§7 follow-up) means no real skill is empty
    // any more — `case.acc.n` now has 2 authored records instead. Genuinely empty-list snapshot
    // behavior (the `?: emptyList()` branch) stays covered by
    // `LifehackTest.staticProviderReturnsEmptyForASkillWithNoAuthoredLifehack`, a fixture-id test.
    @Test
    fun backBlocksLifehacksListEveryAuthoredRecordForASkillWithMoreThanOne() = withSession { session ->
        session.dispatch("skill", "case.acc.n")

        val lifehacks = state(session).getValue("styleBlocks").jsonObject.getValue("lifehacks").jsonArray
        assertEquals(2, lifehacks.size)
        assertTrue(lifehacks.all { it.jsonObject.getValue("status").jsonPrimitive.content == "editorial" })
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
