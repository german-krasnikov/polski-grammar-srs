package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import polski.data.rows.plru.*
import polski.model.GramCase

/** R1.9: the hand-written part of the reference path, on hand-built rows with a different text in every slot. */
class PlRuReferenceRowsTest {
    @Test fun aCaseRowKeepsItsCaseAndAnAbsentSkillStaysAbsent() {
        assertEquals(CaseReferenceRow(GramCase.GEN, "pl", "ru", "q", "t", "case.gen.neg"), RefCaseRow("gen", "pl", "ru", "q", "t", "case.gen.neg").toCase())
        assertNull(RefCaseRow("nom", "pl", "ru", "q", "t").toCase().skill)
    }

    @Test fun aCaseThatIsNoCaseIdIsRefused() {
        assertFailsWith<IllegalStateException> { RefCaseRow("accc", "pl", "ru", "q", "t").toCase() }
    }

    @Test fun theGenderNamesAreKeyedByTheIdsOfTheModelInItsOrder() {
        val names = GenderNamesRow("mp", "ma", "mi", "f", "n").toNames()
        assertEquals(listOf("m-personal" to "mp", "m-animate" to "ma", "m-inanimate" to "mi", "f" to "f", "n" to "n"), names.toList())
    }

    @Test fun anAspectRowKeepsItsFiveTextsAndAnAbsentPresentIsNull() {
        assertEquals(ReferenceAspectRow("label", "from", "present", "past", "future"), RefAspectRow("label", "from", "present", "past", "future").toAspect())
        assertNull(RefAspectRow("label", "from", null, "past", "future").toAspect().present)
    }

    @Test fun aMaleAccusativeRowKeepsItsExamplesInOrderWithEveryTextInItsPlace() {
        val row = RefMaleAccRow("id", "label", "title", listOf(RefMaleAccExampleRow("f1", "t1", "s1"), RefMaleAccExampleRow("f2", "t2", "s2")), "rule").toMaleAcc()
        assertEquals(MaleAccRow("id", "label", "title", listOf(MaleAccExample("f1", "t1", "s1"), MaleAccExample("f2", "t2", "s2")), "rule"), row)
    }
}
