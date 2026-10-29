package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import polski.data.rows.plru.*
import polski.model.GramCase
import polski.model.NumberGram
import polski.model.PossessiveId
import polski.presentation.ContrastPair
import polski.presentation.EndingPart

/** R1.10b: the hand-written part of the pronoun teaching and Russian support path, on hand-built rows with a different text in every slot. */
class PlRuPronounTeachingRowsTest {
    private fun context(caseId: String = "inst", prefix: String? = "z ", special: List<SpecialValueRow>? = listOf(SpecialValueRow("ja", "ze mną"))) =
        PronounContextRow("ctx", caseId, CueRow("full", "compact", "ios"), "Narzędnik", prefix, special)

    @Test fun aContextIsReadByItsCaseIdAndKeepsItsCueNamePrefixAndSpecialValues() {
        assertEquals(PronounContext(GramCase.INST, PronounCue("full", "compact", "ios"), "Narzędnik", "z ", mapOf("ja" to "ze mną")), context().toContext())
    }

    @Test fun anAbsentPrefixIsEmptyAndAbsentSpecialValuesAreNone() {
        val plain = context(prefix = null, special = null).toContext()
        assertEquals("" to emptyMap(), plain.valuePrefix to plain.specialValues)
    }

    @Test fun aCaseIdThatIsNoCaseIsRefused() {
        assertFailsWith<IllegalStateException> { context(caseId = "instr").toContext() }
    }

    private fun demo(number: String = "sg", owners: List<String> = listOf("his", "their")) = PossessiveDemoRow(
        "wife", "beautiful", number, listOf(DemoCaseRow("acc", CaptionRow("w", "d", "i"))), owners, DemoRuleRow("inv", "var"),
    )

    @Test fun theDemoKeepsNumberCasesCaptionsOwnersAndRule() {
        assertEquals(
            PossessiveDemo("wife", "beautiful", NumberGram.SG, listOf(PossessiveDemoCase(GramCase.ACC, "w", "d", "i")), setOf(PossessiveId.HIS, PossessiveId.THEIR), "inv", "var"),
            demo().toDemo(),
        )
    }

    @Test fun aNumberOrAnOwnerThatIsNoIdOfTheModelIsRefused() {
        assertFailsWith<IllegalStateException> { demo(number = "dual").toDemo() }
        assertFailsWith<IllegalStateException> { demo(owners = listOf("hiss")).toDemo() }
    }

    @Test fun thePronounTeachingKeepsEveryTextInItsPlace() {
        val row = PronounTeachingRow(
            PersonalTeachingRow("title", ReactCompactRow("introReact", "introCompact"), FooterRow("fReact", "fWeb", "fNative"), listOf("ja", "ty")),
            listOf(context()), PossessiveTeachingRow("possTitle", demo()),
        )
        val teaching = row.toTeaching()
        assertEquals(listOf("title", "introReact", "introCompact", "fReact", "fWeb", "fNative"), with(teaching) { listOf(personalTitle, reactIntro, compactIntro, reactFooter, webFooter, nativeFooter) })
        assertEquals(listOf("ja", "ty"), teaching.pronounIds)
        assertEquals("possTitle", teaching.possessiveTitle)
    }

    @Test fun aRussianSupportRowKeepsItsTablesLineAndEndingPartsWithTheFlagsInTheirOrder() {
        val parts = listOf(EndingPartRow("moj", isChanged = false, isEnding = false), EndingPartRow("a", isChanged = true, isEnding = true), EndingPartRow("x", isChanged = true, isEnding = false))
        val support = RussianSupportSectionRow(
            FullCompactRow("full", "compact"), listOf("c1", "c2"),
            listOf(SupportRow("id", "cue", ConstructionCheckRow("r1", "r2"), ConstructionCheckRow("w1", "w2"), ConstructionCheckRow("d1", "d2"), "line", listOf(ContrastRow("from", "to", parts, parts.reversed())))),
        ).toSupport()
        assertEquals(listOf("full", "compact"), listOf(support.fullTitle, support.compactTitle))
        assertEquals(listOf("c1", "c2"), support.columns)
        val row = support.rows.single()
        assertEquals(listOf("r1" to "r2", "w1" to "w2", "d1" to "d2"), listOf(row.react, row.web, row.desktop).map { it.construction to it.check })
        assertEquals("cue" to "line", row.cue to row.mobileLine)
        val pair = row.comparisons.single()
        assertEquals(ContrastPair("from", "to", listOf(EndingPart("moj", false, false), EndingPart("a", true, true), EndingPart("x", false, true)), pair.afterParts), pair)
        assertEquals(listOf("x", "a", "moj"), pair.afterParts.map { it.text })
    }
}
