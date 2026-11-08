package polski.exam

/** An official source paper. Route IDs preserve the existing web URLs. */
data class OfficialExamEntry(
    val routeId: String,
    val sourceExamId: String,
    val provider: String,
    val targetLanguage: String,
    val level: String,
    val year: Int,
    val modules: List<OfficialExamModule>,
    val grammarItemCount: Int,
)

/** The source-certified module cover; duration is in seconds, maximum in quarter points. */
data class OfficialExamModule(
    val module: ExamModule,
    val order: Int,
    val durationSeconds: Int,
    val maximumQuarter: Int,
)

/** Six published papers from generated DSL rows. The web-only sample is deliberately excluded. */
object ExamCatalog {
    val entries: List<OfficialExamEntry> by lazy {
        listOf(
            entry("b1-2017", polski.exam.data.b1y2017.EXAM_ROW_ROWS.single().let { SourceExam(it.examId, it.provider, it.targetLanguage, it.level, it.year) },
                polski.exam.data.b1y2017.EXAM_MODULE_ROW_ROWS.map { cover(it.examRef, it.module.tag, it.order, it.durationSeconds, it.maximumQuarter) }),
            entry("b1-2019", polski.exam.data.b1y2019.EXAM_ROW_ROWS.single().let { SourceExam(it.examId, it.provider, it.targetLanguage, it.level, it.year) },
                polski.exam.data.b1y2019.EXAM_MODULE_ROW_ROWS.map { cover(it.examRef, it.module.tag, it.order, it.durationSeconds, it.maximumQuarter) }),
            entry("b1-2020", polski.exam.data.b1y2020.EXAM_ROW_ROWS.single().let { SourceExam(it.examId, it.provider, it.targetLanguage, it.level, it.year) },
                polski.exam.data.b1y2020.EXAM_MODULE_ROW_ROWS.map { cover(it.examRef, it.module.tag, it.order, it.durationSeconds, it.maximumQuarter) }),
            entry("b2-2017", polski.exam.data.b2y2017.EXAM_ROW_ROWS.single().let { SourceExam(it.examId, it.provider, it.targetLanguage, it.level, it.year) },
                polski.exam.data.b2y2017.EXAM_MODULE_ROW_ROWS.map { cover(it.examRef, it.module.tag, it.order, it.durationSeconds, it.maximumQuarter) }),
            entry("b2-2019", polski.exam.data.b2y2019.EXAM_ROW_ROWS.single().let { SourceExam(it.examId, it.provider, it.targetLanguage, it.level, it.year) },
                polski.exam.data.b2y2019.EXAM_MODULE_ROW_ROWS.map { cover(it.examRef, it.module.tag, it.order, it.durationSeconds, it.maximumQuarter) }),
            entry("b2-2020", polski.exam.data.b2y2020.EXAM_ROW_ROWS.single().let { SourceExam(it.examId, it.provider, it.targetLanguage, it.level, it.year) },
                polski.exam.data.b2y2020.EXAM_MODULE_ROW_ROWS.map { cover(it.examRef, it.module.tag, it.order, it.durationSeconds, it.maximumQuarter) }),
        )
    }

    fun find(routeId: String): OfficialExamEntry? = entries.firstOrNull { it.routeId == routeId }
}

private data class SourceExam(val id: String, val provider: String, val language: String, val level: String, val year: Int?)
private data class SourceCover(val examRef: String, val module: OfficialExamModule)

private fun cover(examRef: String, tag: String, order: Int, durationSeconds: Int, maximumQuarter: Int): SourceCover =
    SourceCover(examRef, OfficialExamModule(ExamModule.entries.single { it.name.lowercase() == tag }, order, durationSeconds, maximumQuarter))

private fun entry(routeId: String, source: SourceExam, covers: List<SourceCover>): OfficialExamEntry {
    val year = requireNotNull(source.year) { "Official $routeId has no year" }
    require(routeId == "${source.level.lowercase()}-$year") { "Route/source mismatch for $routeId" }
    require(source.id.isNotBlank() && source.provider.isNotBlank() && source.language.isNotBlank())
    require(covers.size == 4 && covers.all { it.examRef == source.id }) { "Missing source modules for $routeId" }
    val modules = covers.map { it.module }.sortedBy { it.order }
    require(modules.map { it.order } == listOf(1, 2, 3, 4) && modules.map { it.module }.toSet() == ExamModule.entries.toSet())
    require(modules.all { it.durationSeconds > 0 && it.maximumQuarter > 0 })
    val grammar = requireNotNull(officialGrammarData[routeId]) { "Missing grammar for $routeId" }
    val grammarQuarter = grammar.sumOf { test -> test.items.sumOf { it.maximumQuarter } }
    require(modules.single { it.module == ExamModule.Grammar }.maximumQuarter == grammarQuarter) { "Grammar cover mismatch for $routeId" }
    return OfficialExamEntry(routeId, source.id, source.provider, source.language, source.level, year,
        modules, grammar.sumOf { it.items.size })
}
