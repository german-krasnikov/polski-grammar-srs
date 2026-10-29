package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import polski.data.rows.plru.*
import polski.presentation.StemAlternation

/** R1.8b: the hand-written part of the vocabulary path, on hand-built rows with a different text in every slot. */
class PlRuVocabularyRowsTest {
    private fun card(id: String, rank: Int? = 7) = VocabularyItemRow(id, "lemma-$id", "translation", "form", "example", "A1", rank)
    private fun vocabulary(vararg cards: VocabularyItemRow) =
        VocabularyRow("source", cards.toList(), VocabularyInstructionsRow("r", "w", "n", "i"), "unavailable")

    @Test fun aCardKeepsEveryFieldInItsPlaceAndAnAbsentRankStaysAbsent() {
        val items = vocabulary(card("a"), card("b", rank = null)).toItems()
        assertEquals(VocabularyItem("a", "lemma-a", "translation", "form", "example", "A1", 7), items[0])
        assertEquals(null, items[1].frequencyRank)
        assertEquals(listOf("a", "b"), items.map { it.id })
    }

    @Test fun aCardIdUsedTwiceIsRefused() {
        assertFailsWith<IllegalArgumentException> { vocabulary(card("a"), card("b"), card("a")).toItems() }
    }

    @Test fun aStemAlternationIsTwoSingleLetters() {
        assertEquals(StemAlternation('ó', 'o'), StemAlternationRow("ó", "o").toStemAlternation())
        for (row in listOf(StemAlternationRow("ób", "o"), StemAlternationRow("ó", ""), StemAlternationRow("", "o"))) {
            assertFailsWith<RuntimeException> { row.toStemAlternation() }
        }
    }

    private fun training(vararg ids: String) =
        TrainingRow(ChainPresentationRow(ids.map { ChainStepRow(it, "label-$it") }, ChainCompletionRow("title", "eyebrow", "react", "web")))

    @Test fun theChainKeepsItsStepsInOrderAndTheCompletionInItsPlaces() {
        val chain = training("acc", "past").toChain()
        assertEquals(listOf(ChainStep("acc", "label-acc"), ChainStep("past", "label-past")), chain.steps)
        assertEquals(ChainCompletion("title", "eyebrow", "react", "web"), chain.completion)
    }

    @Test fun aChainStepIdUsedTwiceIsRefused() {
        assertFailsWith<IllegalArgumentException> { training("acc", "past", "acc").toChain() }
    }

    @Test fun theChainOfTheRealTrainingHasFiveStepsInOrder() {
        assertEquals(listOf("acc", "past", "neg", "owner", "loc"), generatedPlRuChainPresentation.steps.map { it.id })
    }
}
