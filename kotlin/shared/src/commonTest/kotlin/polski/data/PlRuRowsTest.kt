package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.data.rows.plru.FeatureFocusRow
import polski.data.rows.plru.FixedRow
import polski.data.rows.plru.FocusRow
import polski.data.rows.plru.LexicalFilterRow
import polski.data.rows.plru.MethodRow
import polski.data.rows.plru.MethodsRow
import polski.data.rows.plru.NativeParallelRow
import polski.data.rows.plru.SkillRow
import polski.data.rows.plru.SkillRowLevel
import polski.data.rows.plru.StyleContentRow
import polski.model.Skill
import polski.presentation.NativeParallelPair

/** R1.4a/R1.4b: the hand-written part of the typed skill path, on hand-built rows. The generated rows are checked by `biome data` and by the pilot test. */
class PlRuRowsTest {
    private val edge = "  Zażółć\ngęślą 😀 \"q\" \\ \$x \${y}\té \n"

    private fun method(tag: String) = MethodRow("$tag-i", "$tag-p", "$tag-in", "$tag-r", "$tag-f", "$tag-rv")

    private fun row(
        style: StyleContentRow? = null,
        featureFocus: FeatureFocusRow? = null,
        fixed: List<FixedRow> = emptyList(),
        filter: LexicalFilterRow? = null,
    ) = SkillRow(
        "s", edge, "G", SkillRowLevel.B1, "f", "t", "h", listOf("p"), FocusRow("b", "a"), MethodsRow(method("l"), method("s")),
        "core.c", featureFocus, fixed, filter, style,
    )

    @Test fun textsPassThroughUnchanged() {
        assertEquals(Skill("s", edge, "G", "B1", "f", "t", "h", listOf("p")), row().toSkill())
    }

    @Test fun presentationKeepsTheFocusAndBothMethods() {
        val presentation = row().toPresentation()
        assertEquals("b" to "a", presentation.focusBefore to presentation.focusAfter)
        assertEquals(MethodPresentation("l-i", "l-p", "l-in", "l-r", "l-f", "l-rv"), presentation.logic)
        assertEquals(MethodPresentation("s-i", "s-p", "s-in", "s-r", "s-f", "s-rv"), presentation.situations)
    }

    @Test fun anAbsentStyleContentIsTheDomainDefault() {
        assertEquals(SkillStyleContent(), row().toStyleContent())
        assertEquals(SkillStyleContent(), row(StyleContentRow()).toStyleContent())
    }

    @Test fun styleContentKeepsRuleWhyExamplesAndPairsWithoutHighlightParts() {
        val style = StyleContentRow(rule = "r", nativeParallel = listOf(NativeParallelRow("n", "t", "note", false)), examples = listOf("e1", "e2"), why = "w")
        val expected = SkillStyleContent(
            rule = "r", nativeParallel = listOf(NativeParallelPair("n", "t", "note", false, emptyList())), examples = listOf("e1", "e2"), why = "w",
        )
        assertEquals(expected, row(style).toStyleContent())
    }

    @Test fun anEmptyTextIsDataNotAbsence() {
        val content = row(StyleContentRow(rule = "", why = "")).toStyleContent()
        assertEquals("" to "", content.rule to content.why)
    }
}
