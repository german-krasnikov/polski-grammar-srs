package polski.ios

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.JsonNull
import platform.CoreFoundation.CFRunLoopRunInMode
import platform.CoreFoundation.kCFRunLoopDefaultMode
import platform.Foundation.NSUserDefaults
import polski.data.selectActiveCoursePack
import polski.cloze.ClozeSessionState
import polski.presentation.AppUiState

/**
 * M10: a store built for a fresh scene must start from the answerMode (and explanationMethod)
 * already persisted in defaults, the same way both preferences are re-applied after import.
 */
class IosSessionTest {
    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    @Test
    fun tileBuilderCheckExposesWrongChoiceAndErrorExplanation() {
        val suite = "polski-ios-tile-feedback-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        val session = IosSession(defaults)
        try {
            fun state() = Json.parseToJsonElement(session.currentSnapshot()).jsonObject
            repeat(200) {
                if (state().getValue("loadStatus").jsonPrimitive.content == "Ready") return@repeat
                CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.02, true)
            }
            assertEquals("Ready", state().getValue("loadStatus").jsonPrimitive.content)
            session.dispatch("continueIntroduction")
            session.dispatch("answerMode", "TileBuilder")
            assertEquals("TileBuilder", state().getValue("answerMode").jsonPrimitive.content)
            val choices = state().getValue("tileBuilder").jsonObject.getValue("segments").jsonArray
                .map { it.jsonObject }.filter { it.getValue("type").jsonPrimitive.content == "choice" }
            assertTrue(choices.isNotEmpty())
            for ((index, choice) in choices.withIndex()) {
                val form = if (index == 0) choice.getValue("alternatives").jsonArray.first().jsonPrimitive.content
                    else choice.getValue("correctForm").jsonPrimitive.content
                session.dispatch("tilePlace", "${choice.getValue("position").jsonPrimitive.content}:$form")
            }
            assertEquals("true", state().getValue("tileBuilder").jsonObject.getValue("canCheck").jsonPrimitive.content)
            session.dispatch("tileCheck")
            val revealed = state()
            assertEquals("Revealed", revealed.getValue("phase").jsonPrimitive.content)
            assertEquals("TileBuilder", revealed.getValue("answerMode").jsonPrimitive.content)
            assertTrue(revealed.getValue("tileBuilder").jsonObject.getValue("result") != JsonNull,
                "Tile result absent after Check: ${revealed.getValue("tileBuilder")}")
            val result = revealed.getValue("tileBuilder").jsonObject.getValue("result").jsonObject
            assertEquals("false", result.getValue("correct").jsonPrimitive.content)
            val errors = result.getValue("errors").jsonArray
            assertTrue(errors.isNotEmpty())
            session.dispatch("tileTapError", errors.first().jsonObject.getValue("position").jsonPrimitive.content)
            assertTrue(revealed.getValue("tileBuilder").jsonObject.getValue("errorExplanation") == JsonNull)
            assertTrue(state().getValue("tileBuilder").jsonObject.getValue("errorExplanation").jsonArray.isNotEmpty())

            val beforeRating = state()
            val beforeReviews = beforeRating.getValue("totalReviews").jsonPrimitive.content.toInt()
            val beforeRevision = beforeRating.getValue("revision").jsonPrimitive.content.toInt()
            session.dispatch("rate", "Good")
            val afterRating = state()
            assertEquals(beforeReviews + 1, afterRating.getValue("totalReviews").jsonPrimitive.content.toInt())
            assertEquals(beforeRevision + 1, afterRating.getValue("revision").jsonPrimitive.content.toInt())
            session.dispatch("rate", "Good")
            val afterDuplicate = state()
            assertEquals(afterRating.getValue("totalReviews"), afterDuplicate.getValue("totalReviews"))
            assertEquals(afterRating.getValue("revision"), afterDuplicate.getValue("revision"))
        } finally {
            session.close()
            defaults.removePersistentDomainForName(suite)
        }
    }

    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    @Test
    fun clozeBridgeUsesCurrentExerciseAndLeavesProgressUntouched() {
        val suite = "polski-ios-cloze-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        val session = IosSession(defaults)
        try {
            fun state() = Json.parseToJsonElement(session.currentSnapshot()).jsonObject
            session.dispatch("clozeNext")
            assertTrue(state()["cloze"] == JsonNull || state()["cloze"] == null)
            repeat(200) {
                if (state().getValue("loadStatus").jsonPrimitive.content == "Ready") return@repeat
                CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.02, true)
            }
            assertEquals("Ready", state().getValue("loadStatus").jsonPrimitive.content)
            val beforeRevision = state().getValue("revision")
            val beforeSavedRevision = state().getValue("savedRevision")
            session.dispatch("tab", "Cloze")
            val first = state().getValue("cloze").jsonObject
            val current = first.getValue("current").jsonObject
            val id = current.getValue("id").jsonPrimitive.content
            val options = current.getValue("options").jsonArray.map { it.jsonPrimitive.content }
            assertEquals(3, options.size)
            assertTrue("correctForm" !in current)
            assertEquals(0, first.getValue("currentIndex").jsonPrimitive.content.toInt())
            session.dispatch("tab", "Training")
            session.dispatch("tab", "Cloze")
            assertEquals(id, state().getValue("cloze").jsonObject.getValue("current").jsonObject.getValue("id").jsonPrimitive.content)
            session.chooseCloze("stale", options[0])
            session.chooseCloze(id, "not an option")
            assertEquals(JsonNull, state().getValue("cloze").jsonObject.getValue("current").jsonObject.getValue("selectedForm"))
            session.chooseCloze(id, options[0])
            val answered = state().getValue("cloze").jsonObject.getValue("current").jsonObject
            assertEquals(options[0], answered.getValue("selectedForm").jsonPrimitive.content)
            assertTrue(answered.getValue("correctForm").jsonPrimitive.content in options)
            session.chooseCloze(id, options[1])
            assertEquals(options[0], state().getValue("cloze").jsonObject.getValue("current").jsonObject.getValue("selectedForm").jsonPrimitive.content)
            session.dispatch("clozePrevious")
            assertEquals("0", state().getValue("cloze").jsonObject.getValue("currentIndex").jsonPrimitive.content)
            session.dispatch("clozeNext")
            assertEquals("1", state().getValue("cloze").jsonObject.getValue("currentIndex").jsonPrimitive.content)
            session.chooseCloze(id, options[1])
            assertEquals(JsonNull, state().getValue("cloze").jsonObject.getValue("current").jsonObject.getValue("selectedForm"))
            session.dispatch("clozeRestart")
            val restarted = state().getValue("cloze").jsonObject
            assertEquals("0", restarted.getValue("currentIndex").jsonPrimitive.content)
            assertEquals(JsonNull, restarted.getValue("current").jsonObject.getValue("selectedForm"))
            repeat(restarted.getValue("total").jsonPrimitive.content.toInt() + 2) { session.dispatch("clozeNext") }
            val last = state().getValue("cloze").jsonObject
            assertEquals(last.getValue("total").jsonPrimitive.content.toInt() - 1,
                last.getValue("currentIndex").jsonPrimitive.content.toInt())
            assertEquals(beforeRevision, state().getValue("revision"))
            assertEquals(beforeSavedRevision, state().getValue("savedRevision"))
        } finally {
            session.close()
            defaults.removePersistentDomainForName(suite)
        }
    }

    @Test
    fun emptyClozeSessionHasNullCurrent() {
        val cloze = Json.parseToJsonElement(snapshot(AppUiState(cloze = ClozeSessionState(emptyList()))))
            .jsonObject.getValue("cloze").jsonObject
        assertEquals("0", cloze.getValue("total").jsonPrimitive.content)
        assertEquals(JsonNull, cloze.getValue("current"))
    }
    @Test
    fun lifehackSnapshotKeepsIosStatusLabel() {
        val suite = "polski-ios-lifehack-wire-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        val session = IosSession(defaults)
        try {
            val groups = Json.parseToJsonElement(session.currentSnapshot()).jsonObject
                .getValue("lifehackGroups").jsonArray
            val hack = groups.flatMap { it.jsonObject.getValue("lifehacks").jsonArray }.first().jsonObject
            assertTrue("statusLabel" in hack)
            assertTrue("status" !in hack)
        } finally {
            session.close()
            defaults.removePersistentDomainForName(suite)
        }
    }

    @Test
    fun tabCommandPreservesInvalidInputsAndResetsMatrixOnlyOnReentry() {
        val suite = "polski-ios-tab-wire-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        val session = IosSession(defaults)
        try {
            fun tab() = Json.parseToJsonElement(session.currentSnapshot()).jsonObject
                .getValue("tab").jsonPrimitive.content
            fun section() = Json.parseToJsonElement(session.currentSnapshot()).jsonObject
                .getValue("matrix").jsonObject.getValue("section").jsonPrimitive.content

            assertEquals("Training", tab())
            for (value in listOf("Vocabulary", "Progress", "Training", "Matrix")) {
                session.dispatch("tab", value)
                assertEquals(value, tab())
            }
            session.dispatch("matrixSection", "Cases")
            assertEquals("Cases", section())
            session.dispatch("tab", "Matrix")
            assertEquals("Cases", section())
            for ((command, value) in listOf("unknown" to "Training", "tab" to "", "tab" to "Unknown", "tab" to "matrix")) {
                session.dispatch(command, value)
                assertEquals("Matrix", tab())
                assertEquals("Cases", section())
            }
            session.dispatch("tab", "Vocabulary")
            session.dispatch("tab", "Matrix")
            assertEquals("Map", section())
        } finally {
            session.close()
            defaults.removePersistentDomainForName(suite)
        }
    }

    @Test
    fun newSessionRestoresPersistedAnswerModeAndExplanationMethod() {
        val suite = "polski-ios-session-test-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            defaults.setObject("Typed", forKey = "answerMode")
            defaults.setObject("Situations", forKey = "explanationMethod")

            val session = IosSession(defaults)
            val snapshot = Json.parseToJsonElement(session.currentSnapshot()).jsonObject
            assertEquals("Typed", snapshot.getValue("answerMode").jsonPrimitive.content)
            assertEquals("Situations", snapshot.getValue("explanationMethod").jsonPrimitive.content)
            session.close()
        } finally {
            defaults.removePersistentDomainForName(suite)
        }
    }

    @Test
    fun newSessionRestoresTileBuilderAnswerMode() {
        val suite = "polski-ios-tile-mode-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            defaults.setObject("TileBuilder", forKey = "answerMode")
            val session = IosSession(defaults)
            assertEquals("TileBuilder", Json.parseToJsonElement(session.currentSnapshot())
                .jsonObject.getValue("answerMode").jsonPrimitive.content)
            session.close()
        } finally {
            defaults.removePersistentDomainForName(suite)
        }
    }

    /**
     * EN-22 diagnostic: reproduces the exact live crash found on iPhone 17 Pro Simulator — a
     * [session]'s `currentSnapshot()` called *after* `packRegistry` is switched to en-ru (as
     * `AppModel.init()` now does, `reapplySavedCoursePack()` before `session.currentSnapshot()`)
     * must not throw, even though the first call already happened with pl-ru active.
     *
     * EnRuAcceptance-2026-09-28.md §7 item 2: en-ru now passes `usableCourseSelections` (item 1),
     * so [selectActiveCoursePack] genuinely switches the active pack here, not the no-op ADR-35
     * described when this test was written — this is now the real regression this task fixed
     * ([IosSession.rebuildIfCourseSwitched]'s own KDoc has the full root cause and fallback).
     */
    @Test
    fun currentSnapshotAfterSwitchingActivePackToEnRuDoesNotThrow() {
        val suite = "polski-ios-session-enru-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            val session = IosSession(defaults)
            session.currentSnapshot() // mirrors the first, pl-active call `onState`'s setter makes
            selectActiveCoursePack("en-ru") // the host path — a real switch now (item 1)
            session.currentSnapshot() // mirrors AppModel.init()'s explicit call after reapply
            session.close()
        } finally {
            selectActiveCoursePack("pl-ru")
            defaults.removePersistentDomainForName(suite)
        }
    }

    @Test
    fun newSessionDefaultsToOralAndLogicWithoutPersistedPreferences() {
        val suite = "polski-ios-session-test-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            val session = IosSession(defaults)
            val snapshot = Json.parseToJsonElement(session.currentSnapshot()).jsonObject
            assertEquals("Oral", snapshot.getValue("answerMode").jsonPrimitive.content)
            assertEquals("Logic", snapshot.getValue("explanationMethod").jsonPrimitive.content)
            session.close()
        } finally {
            defaults.removePersistentDomainForName(suite)
        }
    }

    /**
     * FlipCardRivePlan.md FC-01/FC-17/FC-20: the iOS host's decorative Rive overlay listens to
     * [IosSession.onEffect], which must fire exactly once per accepted rating — with the effect
     * `cardEffectFor` maps that [polski.srs.Rating] to — and never for a rate the domain itself
     * drops (here: a second "rate" landing on the already-advanced, still-unrevealed next card).
     */
    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    @Test
    fun rateFiresOnEffectOnceWithTheDomainsMappedEffectAndSkipsADroppedSecondRate() {
        val suite = "polski-ios-session-test-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            val session = IosSession(defaults)
            val effects = mutableListOf<String>()
            session.onEffect = { effects.add(it) }
            var loaded = false
            repeat(200) {
                val snapshot = Json.parseToJsonElement(session.currentSnapshot()).jsonObject
                if (snapshot.getValue("loadStatus").jsonPrimitive.content == "Ready") loaded = true
                if (!loaded) CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.02, true)
            }
            assertTrue(loaded, "iOS session did not finish loading")

            session.dispatch("continueIntroduction")
            session.dispatch("reveal")
            val ratedId = Json.parseToJsonElement(session.currentSnapshot()).jsonObject
                .getValue("exercise").jsonObject.getValue("id").jsonPrimitive.content

            session.dispatch("rate", "Good")
            assertEquals(listOf("Remembered"), effects)
            val nextId = Json.parseToJsonElement(session.currentSnapshot()).jsonObject
                .getValue("exercise").jsonObject.getValue("id").jsonPrimitive.content
            assertTrue(ratedId != nextId, "a real rate must advance past the rated card")

            // Same command dispatched again now targets the new, still-Question card: TrainingStore.rate's
            // own phase guard drops it, and onEffect must stay silent — no spurious second burst.
            session.dispatch("rate", "Good")
            assertEquals(listOf("Remembered"), effects)

            session.close()
        } finally {
            defaults.removePersistentDomainForName(suite)
        }
    }

    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    @Test
    fun rateAgainFiresTheAgainEffect() {
        val suite = "polski-ios-session-test-${Random.nextLong()}"
        val defaults = assertNotNull(NSUserDefaults(suiteName = suite))
        defaults.removePersistentDomainForName(suite)
        try {
            val session = IosSession(defaults)
            val effects = mutableListOf<String>()
            session.onEffect = { effects.add(it) }
            var loaded = false
            repeat(200) {
                val snapshot = Json.parseToJsonElement(session.currentSnapshot()).jsonObject
                if (snapshot.getValue("loadStatus").jsonPrimitive.content == "Ready") loaded = true
                if (!loaded) CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.02, true)
            }
            assertTrue(loaded, "iOS session did not finish loading")

            session.dispatch("continueIntroduction")
            session.dispatch("reveal")
            session.dispatch("rate", "Again")
            assertEquals(listOf("Again"), effects)

            session.close()
        } finally {
            defaults.removePersistentDomainForName(suite)
        }
    }
}
