// Generated from kotlin/shared/src/commonMain/kotlin/polski/data/PlRuLifehackRows.kt by dsl/derive-enru-mappers.mjs: the en-ru copy of that file. Edit the pl-ru file, then run biome generate.
package polski.data

import polski.data.rows.enru.LIFEHACK_ROW_ROWS
import polski.data.rows.enru.LifehackRow
import polski.data.rows.enru.LifehackRowStatus
import polski.data.rows.enru.LifehackSourceRowKind
import polski.presentation.Lifehack
import polski.presentation.LifehackSource
import polski.presentation.LifehackSourceKind
import polski.presentation.LifehackStatus

/** Declared before [generatedEnRuLifehacks] on purpose: a top-level value is set in the order of the file, and that one uses this while it is being set. */
private val placeholderCitation = Regex("tbd|todo|n/a|unknown|placeholder|tba|\\?+", RegexOption.IGNORE_CASE)

/**
 * R1.18: the en-ru lifehacks (pair-scoped tips about the traps of Russian, ADR-15), from the rows of courses-biome/en-ru/lifehacks.core-data in the order of the file. A row is refused, not repaired,
 * for what the contract cannot state and the old validator checked: an id without its pair prefix, a tip for no skill without a topic (or for a skill with one), a blank text, and a citation that is empty
 * or only a placeholder (ADR-15: no invented citations; whether a citation is real is a linguist's call, this catches the mechanical half). That a skill exists and an id is not used twice are `biome data check`.
 */
internal val generatedEnRuLifehacks: List<Lifehack> = LIFEHACK_ROW_ROWS.toLifehacks()

internal fun List<LifehackRow>.toLifehacks(): List<Lifehack> = map { row ->
    require(row.id.startsWith("en-ru.")) { "${row.id}: an id is prefixed with its pair, \"en-ru.\"" }
    require(row.skillId != null || !row.topic.isNullOrBlank()) { "${row.id}: a tip for no skill needs a topic" }
    require(row.skillId == null || row.topic == null) { "${row.id}: a tip for a skill has no topic" }
    require(row.text.isNotBlank()) { "${row.id}: no text" }
    val citation = row.source.citation.trim()
    require(citation.isNotEmpty() && !placeholderCitation.matches(citation)) { "${row.id}: the citation is empty or a placeholder" }
    require(row.source.url == null || Regex("https://\\S+").matches(row.source.url)) { "${row.id}: the url is not an https address" }
    Lifehack(row.id, row.skillId, row.topic, row.text, LifehackSource(row.source.kind.toKind(), row.source.citation, row.source.url), row.status.toStatus())
}

private fun LifehackSourceRowKind.toKind() = when (this) {
    LifehackSourceRowKind.RESEARCH -> LifehackSourceKind.Research
    LifehackSourceRowKind.TEACHING_PRACTICE -> LifehackSourceKind.TeachingPractice
    LifehackSourceRowKind.PROJECT_AUTHORED -> LifehackSourceKind.ProjectAuthored
}

private fun LifehackRowStatus.toStatus() = when (this) {
    LifehackRowStatus.EDITORIAL -> LifehackStatus.Editorial
    LifehackRowStatus.COMMUNITY -> LifehackStatus.Community
}
