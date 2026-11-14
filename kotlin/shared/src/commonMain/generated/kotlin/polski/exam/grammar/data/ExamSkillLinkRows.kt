// Generated from dsl/exam-skill-links.data-contract, courses-biome/pl-ru/exam-skill-links.core-data
// source-sha256=c27a1ec548a902f2d15cee980dd0245f6a0c89e2131b03b61a4a9550b45857de
// Edit the source, then run biome generate.

package polski.exam.grammar.data

data class ExamSkillLinkRow(
    val linkId: String,
    val itemId: String,
    val skillId: String,
    val note: String? = null,
)

val EXAM_SKILL_LINK_ROW_ROWS: List<ExamSkillLinkRow> = listOf(
    ExamSkillLinkRow(
        linkId = "b1-2017-g1-2-case-loc",
        itemId = "B1-2017-g1-2",
        skillId = "case.loc",
        note = "w którym późnym latem — locative after a time preposition",
    ),
    ExamSkillLinkRow(
        linkId = "b1-2017-g1-5-case-loc",
        itemId = "B1-2017-g1-5",
        skillId = "case.loc",
        note = "W naszym mieście — locative after w",
    ),
    ExamSkillLinkRow(
        linkId = "b1-2017-g1-3-case-inst",
        itemId = "B1-2017-g1-3",
        skillId = "case.inst",
        note = "wypić z nimi — instrumental after z; only one linked item authored so far (AC4 degrade case)",
    ),
)
