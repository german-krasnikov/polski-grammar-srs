package polski.vocabulary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import polski.data.VocabularyItem

/** M8 blueprint: [lexemeId]/[checkTyped] are pure and stateless, no I/O. */
class VocabularyUsageTest {
    private val wife = VocabularyItem(
        id = "noun.wife", lemma = "żona", translation = "жена", form = "żona, żony",
        example = "To jest moja żona.", level = "A1", frequencyRank = null,
    )
    private val runVerb = VocabularyItem(
        id = "verb.run", lemma = "biec", translation = "бежать", form = "biec",
        example = "Biegnę szybko.", level = "A2", frequencyRank = null,
    )
    private val custom = VocabularyItem(
        id = "user.abc", lemma = "coś", translation = "что-то", form = "coś",
        example = "Coś się stało.", level = "—", frequencyRank = null, custom = true,
    )

    @Test fun lexemeIdStripsTheNounPrefix() {
        assertEquals("wife", wife.lexemeId())
    }

    @Test fun lexemeIdIsNullForNonNounIds() {
        assertNull(runVerb.lexemeId())
        assertNull(custom.lexemeId())
    }

    @Test fun checkTypedComparesLemmaWhenRecallingTheTarget() {
        val direction = StudyDirection.nativeToTarget("pl", "ru")
        assertTrue(checkTyped(wife, direction, "pl", "ru", "żona"))
        assertFalse(checkTyped(wife, direction, "pl", "ru", "жена"))
    }

    @Test fun checkTypedComparesTranslationWhenRecallingNative() {
        val direction = StudyDirection.targetToNative("pl", "ru")
        assertTrue(checkTyped(wife, direction, "pl", "ru", "жена"))
        assertFalse(checkTyped(wife, direction, "pl", "ru", "żona"))
    }

    @Test fun checkTypedNormalizesBeforeComparing() {
        val direction = StudyDirection.nativeToTarget("pl", "ru")
        assertTrue(checkTyped(wife, direction, "pl", "ru", "  Żona  "))
    }
}
