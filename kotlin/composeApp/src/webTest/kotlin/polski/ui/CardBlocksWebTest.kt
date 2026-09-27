package polski.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import polski.presentation.Block
import polski.presentation.ChangeSide
import polski.presentation.EndingPart

/**
 * W3 correction (blocker 2, EmphasisUXAudit E7/S4/Emphasis contract §5): a style block's own
 * prose (Formula/Rule/Scene/Examples/WhyOnDemand/NativeParallel-target) can hold a literal
 * `focus.before` span and a literal `focus.after` span side by side in the *same* running text
 * (e.g. [polski.presentation.styleTextHighlightParts] on "See mojej become ich here." with
 * before="mojej"/after="ich") — a fixture no current course skill happens to produce (verified
 * against courses/pl-ru/course.json), so this exercises the render path directly rather than
 * waiting on content that may never exist. Each span must keep its own role's class
 * (`change-before` warm/dashed vs. `change-after` cool/solid), never one caller-supplied class
 * for the whole block.
 */
class CardBlocksWebTest {
    @Test
    fun formulaRendersBeforeAndAfterRoleSpansWithTheirOwnClassInOneRunningText() {
        val parts = listOf(
            EndingPart("See ", false),
            EndingPart("mojej", false, isChanged = true, side = ChangeSide.Before),
            EndingPart(" become ", false),
            EndingPart("ich", false, isChanged = true, side = ChangeSide.After),
            EndingPart(" here.", false),
        )
        val container = document.createElement("div") as HTMLElement
        renderCardBlocks(container, listOf(Block.Formula("See mojej become ich here.", parts)), "card-front")

        val before = container.querySelectorAll(".change-before")
        assertEquals(1, before.length)
        assertEquals("mojej", before.item(0)?.textContent)

        val after = container.querySelectorAll(".change-after")
        assertEquals(1, after.length)
        assertEquals("ich", after.item(0)?.textContent)

        assertEquals("See mojej become ich here.", container.querySelector("strong")?.textContent)
    }
}
