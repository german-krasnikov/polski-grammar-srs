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

    // C2 correction (EmphasisUXAudit blocker on ef66020): a system-card step transition like
    // "Widzę…" → "Nie widzę…" inserts one word into an otherwise identical multi-word phrase.
    // ContrastPair.generated must highlight only the inserted particle after reveal, with no
    // before-side leak — the same insertion contract already proven for FormChange("", "Nie").
    @Test
    fun aSingleInsertedWordInAMultiWordPhraseHighlightsOnlyThatWordAfterReveal() {
        val before = changeHighlightParts("Widzę…", "Nie widzę…", ChangeSide.Before)
        val after = changeHighlightParts("Widzę…", "Nie widzę…", ChangeSide.After)
        assertEquals("Widzę…", before.joinToString("") { it.text })
        assertEquals("Nie widzę…", after.joinToString("") { it.text })
        assertEquals(emptyList(), before.filter(EndingPart::isChanged))
        assertEquals(listOf("Nie"), after.filter(EndingPart::isChanged).map(EndingPart::text))
    }

    // Mirror of the insertion case: removing one word highlights it only on the before side.
    @Test
    fun aSingleDeletedWordInAMultiWordPhraseHighlightsOnlyThatWordBeforeReveal() {
        val before = changeHighlightParts("Nie widzę…", "widzę…", ChangeSide.Before)
        val after = changeHighlightParts("Nie widzę…", "widzę…", ChangeSide.After)
        assertEquals("Nie widzę…", before.joinToString("") { it.text })
        assertEquals("widzę…", after.joinToString("") { it.text })
        assertEquals(listOf("Nie"), before.filter(EndingPart::isChanged).map(EndingPart::text))
        assertEquals(emptyList(), after.filter(EndingPart::isChanged))
    }

    // C2 correction: "Nie widzę…" → "Czy widzę…?" changes the particle (whole-word replacement,
    // correctly highlighted) but the second word only gains a trailing "?" — punctuation never
    // participates in the diff, even when the letters on both sides are byte-identical.
    @Test
    fun trailingPunctuationOnlyDifferenceStaysUnhighlightedEvenWhenTheCoreIsIdentical() {
        val before = changeHighlightParts("widzę…", "widzę…?", ChangeSide.Before)
        val after = changeHighlightParts("widzę…", "widzę…?", ChangeSide.After)
        assertEquals("widzę…", before.joinToString("") { it.text })
        assertEquals("widzę…?", after.joinToString("") { it.text })
        assertEquals(emptyList(), before.filter(EndingPart::isChanged))
        assertEquals(emptyList(), after.filter(EndingPart::isChanged))
    }

    // S4 (EmphasisUXAudit E7): a style block's own prose (formula/rule/scene/examples/why) is
    // highlighted only from the skill's explicit `focus.before → focus.after` pair — never parsed
    // heuristically. A literal occurrence of either phrase gets the matching role; free prose that
    // contains neither phrase verbatim stays a single unmarked part.
    @Test
    fun styleTextHighlightsOnlyLiteralOccurrencesOfTheExplicitPair() {
        val parts = styleTextHighlightParts("Widzę mojej siostry, a nie ich.", "mojej", "ich")
        assertEquals("Widzę mojej siostry, a nie ich.", parts.joinToString("") { it.text })
        assertEquals(listOf("mojej", "ich"), parts.filter(EndingPart::isChanged).map(EndingPart::text))
    }

    @Test
    fun styleTextWithoutTheExplicitPairStaysWhollyUnmarked() {
        val parts = styleTextHighlightParts("Прямой объект после глаголов действия обычно стоит в винительном.", "moja siostra", "moją siostrę")
        assertEquals(listOf(EndingPart("Прямой объект после глаголов действия обычно стоит в винительном.", false)), parts)
    }

    @Test
    fun styleTextWithAnUnchangedPairNeverHighlightsIt() {
        val parts = styleTextHighlightParts("widzę moje duże morze", "moje duże morze", "moje duże morze")
        assertEquals(emptyList(), parts.filter(EndingPart::isChanged))
    }
}
