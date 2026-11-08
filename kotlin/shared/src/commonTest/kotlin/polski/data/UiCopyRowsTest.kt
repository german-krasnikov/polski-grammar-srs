package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import polski.data.oracle.uicopy.UI_COPY_CASE_ROW_ROWS
import polski.presentation.UiCopy

class UiCopyRowsTest {
    @Test
    fun bothPacksExposeSameCompleteKeysAndAuthoredOracleValues() {
        try {
            selectCoursePack("pl-ru")
            val pl = UiCopy.all()
            selectCoursePack("en-ru")
            val en = UiCopy.all()
            assertEquals(202, pl.size)
            assertEquals(202, en.size)
            assertEquals(pl.keys, en.keys)
            assertTrue(pl.values.all(String::isNotBlank))
            assertTrue(en.values.all(String::isNotBlank))
            val placeholder = Regex("\\{([A-Za-z]+)\\}")
            for (case in UI_COPY_CASE_ROW_ROWS) {
                assertEquals(case.plRu, pl.getValue(case.key), "pl-ru ${case.key}")
                assertEquals(case.enRu, en.getValue(case.key), "en-ru ${case.key}")
                val names = case.placeholders.split('|').filter(String::isNotBlank).toSet()
                assertEquals(names, placeholder.findAll(case.plRu).map { it.groupValues[1] }.toSet())
                assertEquals(names, placeholder.findAll(case.enRu).map { it.groupValues[1] }.toSet())
            }
        } finally {
            selectCoursePack("pl-ru")
        }
    }

    @Test
    fun everyUsedTemplateHasTheExpectedNamedFieldsInBothPacks() {
        val expected = mapOf(
            "modeTemplate" to setOf("mode"),
            "alsoAcceptedTemplate" to setOf("answers"),
            "reviewsCountTemplate" to setOf("reviews", "correct", "streak"),
            "nextDueTemplate" to setOf("date"),
            "myVocabularyTemplate" to setOf("count"),
            "lifehackSourceTemplate" to setOf("status"),
            "recallInTemplate" to setOf("language"),
            "selfReviewTemplate" to setOf("answer"),
        )
        val placeholder = Regex("\\{([A-Za-z]+)\\}")
        try {
            for (pack in listOf("pl-ru", "en-ru")) {
                selectCoursePack(pack)
                val all = UiCopy.all()
                val actual = all.filterValues { placeholder.containsMatchIn(it) }
                    .mapValues { (_, value) -> placeholder.findAll(value).map { it.groupValues[1] }.toSet() }
                assertEquals(expected, actual, pack)
            }
        } finally {
            selectCoursePack("pl-ru")
        }
    }

    @Test
    fun malformedEntriesAreRejectedBeforeRendering() {
        assertFailsWith<IllegalArgumentException> { uiCopyRowsToMap(listOf("" to "text")) }
        assertFailsWith<IllegalArgumentException> { uiCopyRowsToMap(listOf("key" to " ")) }
        assertFailsWith<IllegalArgumentException> { uiCopyRowsToMap(listOf("key" to "first", "key" to "second")) }
    }
}
