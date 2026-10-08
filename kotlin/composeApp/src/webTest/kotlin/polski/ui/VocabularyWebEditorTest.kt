package polski.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import polski.data.VocabularyItem
import polski.data.vocabularyItems
import polski.vocabulary.VocabularyCodec

/** S1 on web: the editor trims like JS String.trim() (BOM/NBSP included) before the duplicate check and save. */
class VocabularyWebEditorTest {
    private class Saved(val raw: String?, val custom: List<VocabularyItem>, val text: String)

    private fun saveThroughEditor(lemma: String, translation: String = "перевод"): Saved {
        val original = window.localStorage.getItem(VocabularyCodec.key)
        window.localStorage.removeItem(VocabularyCodec.key)
        val controller = VocabularyWebController()
        try {
            val root = document.createElement("div") as HTMLElement
            controller.render(root, root, false) {}
            val inputs = root.querySelectorAll(".vocabulary-editor input")
            listOf(lemma, translation, " forma\uFEFF", "\u00A0przykład ").forEachIndexed { index, value ->
                val input = inputs.item(index) as HTMLInputElement
                input.value = value
                input.dispatchEvent(Event("input"))
            }
            val buttons = root.querySelectorAll(".vocabulary-editor button")
            (0 until buttons.length).map { buttons.item(it) as HTMLElement }
                .first { it.textContent == "Добавить слово" }.click()
            val after = document.createElement("div") as HTMLElement
            controller.render(after, after, false) {}
            val raw = window.localStorage.getItem(VocabularyCodec.key)
            val custom = raw?.let { runCatching { VocabularyCodec.decode(it).custom }
                .getOrElse { error("stored vocabulary no longer decodes: $raw") } }.orEmpty()
            return Saved(raw, custom, after.textContent ?: "")
        } finally {
            controller.close()
            if (original == null) window.localStorage.removeItem(VocabularyCodec.key)
            else window.localStorage.setItem(VocabularyCodec.key, original)
        }
    }

    @Test
    fun bomOrNbspAroundAnExistingLemmaIsADuplicateNotASave() {
        val existing = vocabularyItems.first().lemma
        listOf("\uFEFF$existing", "$existing\u00A0", "\u2003${existing.uppercase()}\uFEFF").forEach { lemma ->
            val saved = saveThroughEditor(lemma)
            assertEquals(emptyList<VocabularyItem>(), saved.custom, "saved duplicate for ${lemma.map { it.code }}")
            assertTrue("уже есть в словаре" in saved.text, saved.text)
        }
    }

    // Control: green on the old code too, because encode() already normalized the stored text.
    @Test
    fun allFourFieldsAreSavedTrimmedLikeJs() {
        val saved = saveThroughEditor("\uFEFF zzzunikat\u00A0", "\u3000перевод\uFEFF")
        assertEquals(listOf(listOf("zzzunikat", "перевод", "forma", "przykład")),
            saved.custom.map { listOf(it.lemma, it.translation, it.form, it.example) })
        assertTrue(saved.raw!!.none { it == '\uFEFF' || it == '\u00A0' || it == '\u3000' }, "raw keeps Unicode spaces: ${saved.raw}")
    }

    @Test
    fun aFieldThatTrimsToEmptyIsRefusedInsteadOfSaved() {
        val saved = saveThroughEditor("zzzunikat", "\uFEFF\u00A0")
        assertEquals(emptyList<VocabularyItem>(), saved.custom)
        assertTrue("Заполни слово, перевод, форму и пример (до 240 символов)." in saved.text, saved.text)
    }
}
