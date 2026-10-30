package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import polski.data.rows.plru.LifehackRow
import polski.data.rows.plru.LifehackRowStatus
import polski.data.rows.plru.LifehackSourceRow
import polski.data.rows.plru.LifehackSourceRowKind
import polski.presentation.Lifehack
import polski.presentation.LifehackSource
import polski.presentation.LifehackSourceKind
import polski.presentation.LifehackStatus

/**
 * R1.18: the lifehack mapper on hand-built rows with a different text in every slot. The real rows of both packs are LifehackDataTest's, the sealed comparison with the old JSON and the contract's own
 * refusals (a skill that is none, an id twice, a kind or a status that is none, a `votes` field) the vitest test course-lifehacks; what is here is what the contract cannot state.
 */
class LifehackRowsTest {
    private fun row(
        id: String = "pl-ru.a", skillId: String? = "s", topic: String? = null, text: String = "text", citation: String = "a citation", url: String? = null,
        kind: LifehackSourceRowKind = LifehackSourceRowKind.RESEARCH, status: LifehackRowStatus = LifehackRowStatus.EDITORIAL,
    ) = LifehackRow(id, skillId, topic, text, LifehackSourceRow(kind, citation, url), status)

    private fun refused(message: String, row: LifehackRow) {
        assertEquals(message, assertFailsWith<IllegalArgumentException>(message) { listOf(row).toLifehacks() }.message?.substringAfter(": "))
    }

    @Test fun aRowBecomesALifehackWithItsKindStatusAndSourceAsWrittenAndTheCitationIsNotTrimmed() {
        val rows = listOf(
            row("pl-ru.r", "s1", text = "t-r", citation = " Swan (2002) ", url = "https://doi.org/x"),
            row("pl-ru.t", "s2", text = "t-t", kind = LifehackSourceRowKind.TEACHING_PRACTICE, status = LifehackRowStatus.COMMUNITY),
            row("pl-ru.p", "s3", text = "t-p", kind = LifehackSourceRowKind.PROJECT_AUTHORED),
        )
        assertEquals(
            listOf(
                Lifehack("pl-ru.r", "s1", null, "t-r", LifehackSource(LifehackSourceKind.Research, " Swan (2002) ", "https://doi.org/x"), LifehackStatus.Editorial),
                Lifehack("pl-ru.t", "s2", null, "t-t", LifehackSource(LifehackSourceKind.TeachingPractice, "a citation", null), LifehackStatus.Community),
                Lifehack("pl-ru.p", "s3", null, "t-p", LifehackSource(LifehackSourceKind.ProjectAuthored, "a citation", null), LifehackStatus.Editorial),
            ),
            rows.toLifehacks(),
        )
    }

    @Test fun aTipForNoSkillKeepsItsTopicHasNoSkillAndTheOrderOfTheRowsIsKept() {
        val tips = listOf(row("pl-ru.z", null, topic = "word order"), row("pl-ru.a", "s")).toLifehacks()
        assertEquals(listOf("pl-ru.z", "pl-ru.a"), tips.map { it.id })
        assertEquals(listOf(null, "s"), tips.map { it.skillId })
        assertEquals("word order", tips[0].topic)
        assertEquals(emptyList(), emptyList<LifehackRow>().toLifehacks())
    }

    @Test fun anIdWithoutItsPairPrefixIsRefused() {
        refused("an id is prefixed with its pair, \"pl-ru.\"", row("en-ru.a"))
        refused("an id is prefixed with its pair, \"pl-ru.\"", row("not-prefixed"))
    }

    @Test fun aTipForNoSkillNeedsATopicAndATipForASkillHasNone() {
        refused("a tip for no skill needs a topic", row(skillId = null))
        refused("a tip for no skill needs a topic", row(skillId = null, topic = "   "))
        refused("a tip for a skill has no topic", row(skillId = "s", topic = "t"))
    }

    @Test fun aBlankTextIsRefused() {
        refused("no text", row(text = ""))
        refused("no text", row(text = " \n "))
    }

    @Test fun anEmptyOrPlaceholderCitationIsRefusedAndOneThatOnlyContainsAPlaceholderWordIsNot() {
        for (bad in listOf("", "   ", "TBD", "todo", "N/A", "Unknown", "placeholder", " tba ", "?", "???")) {
            refused("the citation is empty or a placeholder", row(citation = bad))
        }
        for (good in listOf("Swan (2002)", "tbd: see Swan", "unknown author, 1999", "n/a in the source")) listOf(row(citation = good)).toLifehacks()
    }

    @Test fun anAddressThatIsNotHttpsIsRefused() {
        for (bad in listOf("http://x.org", "ftp://x.org", "https://", "https://a b", "doi:10.1/x")) refused("the url is not an https address", row(url = bad))
        listOf(row(url = "https://doi.org/10.1016/S0346-251X(96)00063-8")).toLifehacks()
    }
}
