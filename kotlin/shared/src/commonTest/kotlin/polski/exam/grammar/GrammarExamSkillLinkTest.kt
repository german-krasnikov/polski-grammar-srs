package polski.exam.grammar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import polski.data.skills
import polski.exam.data.published.GRAMMAR_ITEM_ROW_ROWS
import polski.exam.grammar.data.EXAM_SKILL_LINK_ROW_ROWS

/** M10 blueprint: [skillIdFor]/[verificationItemFor] lookup + the "only one linked item" degrade
 *  case, plus AC1's own check (every authored link references a real exam item and a real skill)
 *  — done here in Kotlin rather than via a biome `ref`, since `itemId` is a plain string (see
 *  `dsl/exam-skill-links.data-contract`'s own header for why). */
class GrammarExamSkillLinkTest {
    @Test fun everyAuthoredLinkReferencesARealItemAndARealSkill() {
        val realItemIds = GRAMMAR_ITEM_ROW_ROWS.map { it.itemId }.toSet()
        val realSkillIds = skills.map { it.id }.toSet()
        EXAM_SKILL_LINK_ROW_ROWS.forEach { link ->
            assertTrue(link.itemId in realItemIds, "Unknown exam item ${link.itemId} in link ${link.linkId}")
            assertTrue(link.skillId in realSkillIds, "Unknown skill ${link.skillId} in link ${link.linkId}")
        }
    }

    @Test fun skillIdForReturnsTheAuthoredSkillForALinkedItem() {
        assertEquals("case.loc", skillIdFor("B1-2017-g1-2"))
        assertEquals("case.inst", skillIdFor("B1-2017-g1-3"))
    }

    @Test fun skillIdForIsNullForAnUnlinkedItem() {
        assertNull(skillIdFor("B1-2017-g1-1"))
    }

    @Test fun verificationItemForReturnsADifferentItemSharingTheSameSkill() {
        val verification = verificationItemFor("case.loc", excludeItemId = "B1-2017-g1-2")
        assertEquals("B1-2017-g1-5", verification)
        // Symmetric: excluding the other item returns the first one, never null, never itself.
        assertEquals("B1-2017-g1-2", verificationItemFor("case.loc", excludeItemId = "B1-2017-g1-5"))
    }

    @Test fun verificationItemForDegradesToNullWhenOnlyOneItemIsLinked() {
        assertNull(verificationItemFor("case.inst", excludeItemId = "B1-2017-g1-3"))
    }

    @Test fun verificationItemForIsNullForAnUnlinkedSkill() {
        assertNull(verificationItemFor("case.acc.f", excludeItemId = "anything"))
    }
}
