package polski.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import polski.model.FormChange

class EndingHighlightTest {
    @Test
    fun invalidExplicitPartsKeepLiteralFormWithoutColour() {
        val pair = ContrastPair(
            "żona", "żonie",
            listOf(EndingPart("żona", false, true)),
            listOf(EndingPart("żony", false, true)),
        )
        assertEquals(listOf(EndingPart("żonie", false)), pair.parts(ChangeSide.After))
    }

    @Test
    fun marksThreeAccusativeEndingsWithoutChangingThePhrase() {
        val parts = endingHighlightParts("moja piękna żona", "moją piękną żonę")
        assertEquals("moją piękną żonę", parts.joinToString("") { it.text })
        assertEquals(listOf("ą", "ą", "ę"), parts.filter(EndingPart::isEnding).map(EndingPart::text))
    }

    @Test
    fun doesNotCallAReplacementOrStemChangeAnEnding() {
        assertEquals(emptyList(), endingHighlightParts("mojej", "ich").filter(EndingPart::isEnding))
        assertEquals(emptyList(), endingHighlightParts("Widzę", "Widziałem").filter(EndingPart::isEnding))
        assertEquals(emptyList(), endingHighlightParts("kupuję", "kupiłem / kupiłam").filter(EndingPart::isEnding))
    }

    @Test
    fun marksOldAndNewSuffixesInTheirSentenceContext() {
        val changes = listOf(FormChange("moja piękna żona", "moją piękną żonę", "Biernik"))
        val source = sentenceHighlightParts("To jest moja piękna żona.", changes, ChangeSide.Before)
        val answer = sentenceHighlightParts("Widzę moją piękną żonę.", changes, ChangeSide.After)
        assertEquals("To jest moja piękna żona.", source.joinToString("") { it.text })
        assertEquals(listOf("a", "a", "a"), source.filter(EndingPart::isChanged).map(EndingPart::text))
        assertEquals(listOf("ą", "ą", "ę"), answer.filter(EndingPart::isChanged).map(EndingPart::text))
    }

    @Test
    fun labelsWholeReplacementAsChangedButNotEnding() {
        assertEquals(listOf("mojej"), changeHighlightParts("mojej", "ich", ChangeSide.Before)
            .filter(EndingPart::isChanged).map(EndingPart::text))
        assertEquals(listOf("ich"), changeHighlightParts("mojej", "ich", ChangeSide.After)
            .filter(EndingPart::isChanged).map(EndingPart::text))
        assertEquals(emptyList(), endingHighlightParts("mojej", "ich").filter(EndingPart::isEnding))
    }

    @Test
    fun marksAmbiguousAlternativesAsWholeForms() {
        val old = changeHighlightParts("robić", "robiłem / robiłam", ChangeSide.Before)
        val new = changeHighlightParts("robić", "robiłem / robiłam", ChangeSide.After)
        assertEquals(listOf("robić"), old.filter(EndingPart::isChanged).map(EndingPart::text))
        assertEquals(listOf("robiłem / robiłam"), new.filter(EndingPart::isChanged).map(EndingPart::text))
        assertEquals(emptyList(), new.filter(EndingPart::isEnding))
    }

    @Test
    fun stripsTrailingPunctuationBeforeEndingDiff() {
        listOf(".", "?", "!").forEach { punct ->
            val parts = endingHighlightParts("żona$punct", "żonę$punct")
            assertEquals("żonę$punct", parts.joinToString("") { it.text })
            assertEquals(listOf("ę"), parts.filter(EndingPart::isEnding).map(EndingPart::text))
            assertEquals(punct, parts.last().text)
            assertFalse(parts.last().isChanged)
        }
    }

    @Test
    fun rejectsASubstringInsideALongerWord() {
        val parts = sentenceHighlightParts("Moja żona mówi.",
            listOf(FormChange("ona", "nią", "Biernik")), ChangeSide.Before)
        assertEquals("Moja żona mówi.", parts.joinToString("") { it.text })
        assertEquals(emptyList(), parts.filter(EndingPart::isChanged))
    }

    // S2: a pack-declared alternation lets a regular stem change (ó~o) resolve to an ending
    // diff instead of the whole-word fallback, without the core knowing it is Polish.
    @Test
    fun aDeclaredAlternationTurnsAStemChangeIntoAReliableEnding() {
        val óToO = listOf(StemAlternation('ó', 'o'))
        assertEquals(listOf("ego"), changeHighlightParts("mój", "mojego", ChangeSide.After, óToO)
            .filter(EndingPart::isEnding).map(EndingPart::text))
        assertEquals(listOf("ą"), changeHighlightParts("mój", "moją", ChangeSide.After, óToO)
            .filter(EndingPart::isEnding).map(EndingPart::text))
        assertEquals(emptyList(), changeHighlightParts("mój", "mojego", ChangeSide.Before, óToO)
            .filter(EndingPart::isEnding))
        // The old form is left stable (no old ending survives into the new stem+ending split).
        assertEquals(emptyList(), changeHighlightParts("mój", "mojego", ChangeSide.Before, óToO)
            .filter(EndingPart::isChanged))
    }

    @Test
    fun withoutADeclaredAlternationTheStemChangeStaysAWholeWordFallback() {
        assertEquals(emptyList(), changeHighlightParts("mój", "mojego", ChangeSide.After, emptyList())
            .filter(EndingPart::isEnding))
        assertEquals(listOf("mojego"), changeHighlightParts("mój", "mojego", ChangeSide.After, emptyList())
            .filter(EndingPart::isChanged).map(EndingPart::text))
    }

    // ST-06 (behavioral genericity): an invented, non-Polish alternation pair drives the exact
    // same reliability logic — nothing in EndingHighlight.kt is coupled to ó/ą/o/e specifically.
    @Test
    fun stemAlternationRulesAreGenericNotPolishSpecific() {
        val invented = listOf(StemAlternation('x', 'y'))
        assertEquals(listOf("zzz"), changeHighlightParts("abx", "abyzzz", ChangeSide.After, invented)
            .filter(EndingPart::isEnding).map(EndingPart::text))
        assertEquals(emptyList(), changeHighlightParts("abx", "abyzzz", ChangeSide.After)
            .filter(EndingPart::isEnding))
    }

    // End-to-end: the active course pack (not shared code) supplies the alternation, so callers
    // that never pass one — every host today — still get it through the default.
    @Test
    fun theActivePackAlternationAppliesWithoutCallersPassingIt() {
        assertEquals(listOf("ego"), endingHighlightParts("mój", "mojego").filter(EndingPart::isEnding).map(EndingPart::text))
    }
}
