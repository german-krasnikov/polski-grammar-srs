package polski.macos

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import platform.CoreFoundation.CFRunLoopRunInMode
import platform.CoreFoundation.kCFRunLoopDefaultMode
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID

/**
 * D3 (`Plans/Kotlin/FlipCardRivePlan.md` §12-§18): the macOS vocabulary card's decorative Rive
 * rating overlay listens to [MacVocabularySession.onEffect], mirroring [MacSession.onEffect] for
 * the training card — it must fire exactly once per accepted rating with the domain's
 * `cardEffectFor` mapping, and stay silent for a rate the domain itself rejects (no revealed
 * current card yet).
 */
@OptIn(ExperimentalForeignApi::class)
class MacVocabularySessionTest {
    private fun withSession(block: (MacVocabularySession) -> Unit) {
        val directory = NSTemporaryDirectory() + "polski-mac-vocab-session-${NSUUID().UUIDString}"
        val session = MacVocabularySession(directory)
        try {
            awaitReady(session)
            block(session)
        } finally {
            session.close()
            NSFileManager.defaultManager.removeItemAtPath(directory, null)
        }
    }

    private fun awaitReady(session: MacVocabularySession) {
        repeat(200) {
            if (state(session).getValue("loadStatus").jsonPrimitive.content == "Ready") return
            CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.02, true)
        }
        error("MacVocabularySession did not reach Ready in time")
    }

    // "good"/"again" commit through a launched coroutine that hops onto a background dispatcher
    // for the actual file write (`MacVocabularyRepository.saveRaw`) and back — unlike the
    // synchronous "reveal" — so the Main run loop must be pumped, polling for the effect to
    // actually land rather than a fixed pump count that could race the write.
    private fun awaitEffect(effects: List<String>, expectedSize: Int) {
        repeat(200) {
            if (effects.size >= expectedSize) return
            CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.02, true)
        }
    }

    private fun pumpBriefly() = repeat(10) { CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.02, true) }

    private fun state(session: MacVocabularySession) = Json.parseToJsonElement(session.currentSnapshot()).jsonObject

    // A fresh document has no selected words, so `currentId` starts out null — `select` (also
    // async, like "good"/"again") must land before `reveal` has anything to reveal.
    private fun selectWord(session: MacVocabularySession) {
        session.dispatch("select", "noun.wife")
        repeat(200) {
            if (state(session).getValue("currentId").jsonPrimitive.content == "noun.wife") return
            CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.02, true)
        }
        error("select never landed a due currentId")
    }

    @Test
    fun ratingGoodFiresTheRememberedEffectOnlyAfterReveal() = withSession { session ->
        selectWord(session)
        val effects = mutableListOf<String>()
        session.onEffect = { effects.add(it) }

        // Not yet revealed: VocabularySession.rate's own `allowed` guard must reject this, so no
        // effect should fire for a rate the domain silently drops.
        session.dispatch("good")
        pumpBriefly()
        assertTrue(effects.isEmpty(), "a rate before reveal must not fire an effect")

        session.dispatch("reveal")
        assertEquals("true", state(session).getValue("revealed").jsonPrimitive.content, "reveal() must have succeeded")
        session.dispatch("good")
        awaitEffect(effects, 1)

        assertEquals(listOf("Remembered"), effects)
    }

    @Test
    fun ratingAgainFiresTheAgainEffect() = withSession { session ->
        selectWord(session)
        val effects = mutableListOf<String>()
        session.onEffect = { effects.add(it) }

        session.dispatch("reveal")
        session.dispatch("again")
        awaitEffect(effects, 1)

        assertEquals(listOf("Again"), effects)
    }

    /** EnRuAcceptance-2026-09-28.md §7 item 4: the snapshot must expose the active pack's own 2
     *  study directions (never a hardcoded pl-ru list) plus a ready-made prompt caption and a
     *  `recallTarget` flag — a Swift card needs exactly this, no language names of its own. */
    @Test
    fun snapshotExposesTheActivePacksOwnDirectionOptionsAndPromptCaption() = withSession { session ->
        val initial = state(session)
        val options = initial.getValue("directionOptions").jsonArray.map {
            it.jsonObject.getValue("wire").jsonPrimitive.content to it.jsonObject.getValue("label").jsonPrimitive.content
        }
        assertEquals(listOf("ru-pl" to "Русский → польский", "pl-ru" to "Польский → русский"), options)
        assertEquals("ru-pl", initial.getValue("direction").jsonPrimitive.content)
        assertEquals("Вспомни по-польски", initial.getValue("promptCaption").jsonPrimitive.content)
        assertEquals("true", initial.getValue("recallTarget").jsonPrimitive.content)

        session.dispatch("direction", "pl-ru")
        val flipped = state(session)
        assertEquals("pl-ru", flipped.getValue("direction").jsonPrimitive.content)
        assertEquals("Вспомни по-русски", flipped.getValue("promptCaption").jsonPrimitive.content)
        assertEquals("false", flipped.getValue("recallTarget").jsonPrimitive.content)

        // Only the active pack's own 2 wires are ever accepted — an unknown wire is a silent no-op,
        // the same contract `builtInStudyDirections`'s own lookup already had.
        session.dispatch("direction", "en-ru")
        assertEquals("pl-ru", state(session).getValue("direction").jsonPrimitive.content)
    }
}
