package polski.exam

import polski.exam.data.published.EXAM_MODULE_ROW_ROWS
import polski.exam.data.published.EXAM_PUBLICATION_ROW_ROWS
import polski.exam.data.published.EXAM_ROW_ROWS
import polski.exam.data.published.ExamModuleRow
import polski.exam.data.published.ExamPublicationRow
import polski.exam.data.published.ExamRow

/** An official source paper. Route IDs preserve existing browser URLs and progress keys. */
data class OfficialExamEntry(
    val routeId: String,
    val sourceExamId: String,
    val provider: String,
    val targetLanguage: String,
    val level: String,
    val year: Int,
    val modules: List<OfficialExamModule>,
    val grammarItemCount: Int,
    val displayTitle: String,
)

/** The source-certified module cover; duration is in seconds, maximum in quarter points. */
data class OfficialExamModule(
    val module: ExamModule,
    val order: Int,
    val durationSeconds: Int,
    val maximumQuarter: Int,
)

/** Published papers from typed DSL rows. The web-only sample is deliberately excluded. */
object ExamCatalog {
    val entries: List<OfficialExamEntry> by lazy {
        val sources = EXAM_ROW_ROWS.associateBy(ExamRow::examId)
        val covers = EXAM_MODULE_ROW_ROWS.groupBy(ExamModuleRow::examRef)
        EXAM_PUBLICATION_ROW_ROWS.map { publication ->
            entry(publication, requireNotNull(sources[publication.examRef]) {
                "Missing source exam ${publication.examRef}"
            }, covers[publication.examRef].orEmpty())
        }
    }

    fun find(routeId: String): OfficialExamEntry? = entries.firstOrNull { it.routeId == routeId }
}

private fun entry(publication: ExamPublicationRow, source: ExamRow, coverRows: List<ExamModuleRow>): OfficialExamEntry {
    val routeId = publication.routeId
    val year = requireNotNull(source.year) { "Official $routeId has no year" }
    require(routeId.isNotBlank() && publication.displayTitle.isNotBlank())
    require(source.examId.isNotBlank() && source.provider.isNotBlank() && source.targetLanguage.isNotBlank())
    require(coverRows.size == 4) { "Missing source modules for $routeId" }
    val modules = coverRows.map { row ->
        require(row.examRef == source.examId)
        OfficialExamModule(
            ExamModule.entries.single { it.name.lowercase() == row.module.tag },
            row.order,
            row.durationSeconds,
            row.maximumQuarter,
        )
    }.sortedBy { it.order }
    require(modules.map { it.order } == listOf(1, 2, 3, 4) && modules.map { it.module }.toSet() == ExamModule.entries.toSet())
    require(modules.all { it.durationSeconds > 0 && it.maximumQuarter > 0 })
    val grammar = requireNotNull(officialGrammarData[routeId]) { "Missing grammar for $routeId" }
    val grammarQuarter = grammar.sumOf { test -> test.items.sumOf { it.maximumQuarter } }
    require(modules.single { it.module == ExamModule.Grammar }.maximumQuarter == grammarQuarter) { "Grammar cover mismatch for $routeId" }
    return OfficialExamEntry(
        routeId, source.examId, source.provider, source.targetLanguage, source.level, year,
        modules, grammar.sumOf { it.items.size }, publication.displayTitle,
    )
}
