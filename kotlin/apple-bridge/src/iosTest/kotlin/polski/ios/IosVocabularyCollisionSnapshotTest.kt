package polski.ios

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import platform.CoreFoundation.CFRunLoopRunInMode
import platform.CoreFoundation.kCFRunLoopDefaultMode
import platform.Foundation.NSUserDefaults
import polski.data.VocabularyItem
import polski.srs.FsrsScheduler
import polski.vocabulary.StudyDirection
import polski.vocabulary.VocabularyCodec
import polski.vocabulary.VocabularyDocument

class IosVocabularyCollisionSnapshotTest {
    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    @Test
    fun importedCustomLemmaDoesNotPromoteFrequencyRowOrLoseHistory() {
        val defaults = NSUserDefaults.standardUserDefaults
        val key = VocabularyCodec.key
        val prior = defaults.stringForKey(key)
        val id = "user.00000000-0000-4000-8000-000000000091"
        val cardKey = VocabularyCodec.cardKey(id, StudyDirection.RussianToPolish)
        val reviewCard = FsrsScheduler().newCard(cardKey, Instant.parse("2099-01-01T00:00:00Z")).card
        val imported = VocabularyDocument(
            selectedIds = listOf(id),
            custom = listOf(VocabularyItem(id, "w", "в (собственное)", "w", "Jestem w domu.", "—", null, true)),
            cards = mapOf(cardKey to reviewCard),
        )
        defaults.setObject(VocabularyCodec.encode(imported), forKey = key)
        assertTrue(defaults.synchronize())
        val bridge = IosVocabularySession()
        try {
            var loaded = false
            repeat(100) {
                val snapshot = Json.parseToJsonElement(bridge.currentSnapshot()).jsonObject
                if (snapshot.getValue("loadStatus").jsonPrimitive.content == "Ready" &&
                    snapshot.getValue("selectedCount").jsonPrimitive.content == "1") loaded = true
                if (!loaded) CFRunLoopRunInMode(kCFRunLoopDefaultMode, 0.02, true)
            }
            assertTrue(loaded, "iOS vocabulary bridge did not load the imported document")

            bridge.dispatch("filter", "100")
            val top = Json.parseToJsonElement(bridge.currentSnapshot()).jsonObject
            assertEquals("Готово 7/100 · недоступно 93", top.getValue("coverage").jsonPrimitive.content)
            val rankOne = top.getValue("entries").jsonArray.first().jsonObject
            assertEquals("w", rankOne.getValue("lemma").jsonPrimitive.content)
            assertEquals("", rankOne.getValue("id").jsonPrimitive.content)
            assertEquals("false", rankOne.getValue("available").jsonPrimitive.content)
            assertEquals("false", rankOne.getValue("selected").jsonPrimitive.content)

            bridge.dispatch("filter", "mine")
            val mine = Json.parseToJsonElement(bridge.currentSnapshot()).jsonObject.getValue("entries").jsonArray.single().jsonObject
            assertEquals(id, mine.getValue("id").jsonPrimitive.content)
            assertEquals("true", mine.getValue("available").jsonPrimitive.content)
            assertEquals("true", mine.getValue("selected").jsonPrimitive.content)

            val exported = VocabularyCodec.decode(assertNotNull(bridge.exportJson()))
            assertEquals(imported.selectedIds, exported.selectedIds)
            assertEquals(imported.cards[cardKey], exported.cards[cardKey])
        } finally {
            bridge.close()
            if (prior == null) defaults.removeObjectForKey(key) else defaults.setObject(prior, forKey = key)
            defaults.synchronize()
        }
    }
}
