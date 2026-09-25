package polski.grammar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import polski.data.nounById
import polski.data.skillById
import polski.model.*

class GrammarEngineTest {
    @Test
    fun masculinePersonalNounEndingInAAgreesAcrossAccusative() {
        assertEquals(
            "mojego dobrego kolegę",
            nounPhrase("friendM", GramCase.ACC, adjectiveId = "good", possessive = PossessiveId.MY),
        )
    }

    @Test
    fun perfectiveVerbHasFutureButNoPresent() {
        assertEquals("zrobię", verbForm("doDone", Tense.FUTURE, Person.FIRST, NumberGram.SG))
        assertFailsWith<IllegalStateException> {
            verbForm("doDone", Tense.PRESENT, Person.FIRST, NumberGram.SG)
        }
    }

    @Test
    fun unknownDictionaryIdsFailExplicitly() {
        assertEquals("Unknown noun absent", assertFailsWith<IllegalStateException> { nounById("absent") }.message)
        assertEquals("Unknown skill absent", assertFailsWith<IllegalStateException> { skillById("absent") }.message)
    }

    @Test
    fun referenceKeepsSevenOrderedCasesAndHeaders() {
        assertEquals(GramCase.entries, caseRows.map { it.id })
        assertEquals("мужской · человек", genderNames.getValue(Gender.M_PERSONAL))
        assertEquals("nie widzę / nie mam / bez / do", caseRows[1].trigger)
    }
}
