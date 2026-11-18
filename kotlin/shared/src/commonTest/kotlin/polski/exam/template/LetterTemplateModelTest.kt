package polski.exam.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import polski.data.rows.plru.lettertemplate.LETTER_TASK_LINK_ROW_ROWS
import polski.data.rows.plru.lettertemplate.LetterRegister
import polski.data.rows.plru.lettertemplate.LetterVerificationStatus
import polski.exam.officialWritingData

/** AC-1 native remainder tests (classified P8/P25): cross-field predicates and ordering invariants. */
class LetterTemplateModelTest {

    private val model get() = plRuLetterTemplates

    @Test
    fun verifiedRowHasNonEmptySourceNote() {
        // Cross-field predicate the data-contract cannot express; checked on raw rows because the domain drops sourceNote.
        val badRows = polski.data.rows.plru.lettertemplate.LETTER_PHRASE_ROW_ROWS.filter { r ->
            r.verificationStatus == LetterVerificationStatus.VERIFIED && r.sourceNote.isNullOrBlank()
        } + polski.data.rows.plru.lettertemplate.LETTER_MISTAKE_ROW_ROWS.filter { r ->
            r.verificationStatus == LetterVerificationStatus.VERIFIED && r.sourceNote.isNullOrBlank()
        } + polski.data.rows.plru.lettertemplate.MODEL_LETTER_ROW_ROWS.filter { r ->
            r.verificationStatus == LetterVerificationStatus.VERIFIED && r.sourceNote.isNullOrBlank()
        }
        assertTrue(badRows.isEmpty(),
            "verified rows without sourceNote: ${badRows.take(3).map { it::class.simpleName }}")
    }

    @Test
    fun eachTemplateSectionsAreOrderedWithoutGaps() {
        for (template in model.all()) {
            val orders = template.sections.map { it.order }
            val expected = (1..orders.size).toList()
            assertEquals(expected, orders.sorted(),
                "${template.id}: sections must be ordered 1..N without gaps")
        }
    }

    @Test
    fun forTaskReturnsLinkedTemplates() {
        // B2-2024-02 task b is linked to complaint — known from letter_task_link.core-data
        val results = model.forTask("certyfikat-polski-B2-2024-02-write-ii-b")
        assertEquals(1, results.size)
        assertEquals("complaint", results.first().id)
    }

    @Test
    fun forTaskReturnsEmptyForUnlinkedTask() {
        // RED test: an unknown taskRef must return empty — demonstrates the check catches a bug
        val results = model.forTask("this-task-id-does-not-exist-in-links")
        assertTrue(results.isEmpty(), "unknown task should return no templates")
    }

    @Test
    fun formalTemplateHasOnlyFormalPhrases() {
        for (template in model.all().filter { it.register == LetterRegister.FORMAL }) {
            val salutation = template.sections.find { it.sectionKey == SectionKey.SALUTATION } ?: continue
            val badPhrases = salutation.phrases.filter { it.register != LetterRegister.FORMAL }
            assertTrue(badPhrases.isEmpty(),
                "${template.id} salutation has non-formal phrases: ${badPhrases.map { it.id }}")
        }
        val informalTemplate = model.all().find { it.register == LetterRegister.INFORMAL }
        assertNotNull(informalTemplate, "need at least one informal template")
        val salutation = informalTemplate.sections.find { it.sectionKey == SectionKey.SALUTATION }
        assertNotNull(salutation)
        val badPhrases = salutation.phrases.filter { it.register != LetterRegister.INFORMAL }
        assertTrue(badPhrases.isEmpty(),
            "${informalTemplate.id} salutation has non-informal phrases: ${badPhrases.map { it.id }}")
    }

    @Test
    fun everyTaskLinkRefExistsInOfficialWritingData() {
        val knownTaskIds = officialWritingData.values.flatMap { exam -> exam.tasks.map { it.id } }.toSet()
        val badRefs = LETTER_TASK_LINK_ROW_ROWS.filter { it.taskRef !in knownTaskIds }.map { it.taskRef }
        assertTrue(badRefs.isEmpty(), "letter_task_link.core-data taskRef(s) not in officialWritingData: $badRefs")
    }

    @Test
    fun modelLetterPresentForAllTemplates() {
        val missing = model.all().filter { it.modelLetter == null }.map { it.id }
        assertTrue(missing.isEmpty(), "templates without model letter: $missing")
        assertEquals(6, model.all().size)
    }
}
