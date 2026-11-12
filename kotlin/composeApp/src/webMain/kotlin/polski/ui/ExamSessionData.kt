package polski.ui

import polski.exam.ExamModule
import polski.exam.ExamCatalog
import polski.exam.session.ExamModuleConfig
import polski.exam.session.ExamSessionConfig

/** Certified module covers, keyed by the existing route IDs. */
internal val examSessionData: Map<String, ExamSessionConfig> by lazy {
    val rows = ExamCatalog.entries.associate { exam ->
        exam.routeId to exam.modules.map { module ->
            ModuleCover(exam.sourceExamId, module.module.name.lowercase(), module.order,
                module.durationSeconds, module.maximumQuarter)
        }
    }
    rows.mapNotNull { (routeId, covers) ->
        val listening = examListeningData[routeId]
        val reading = examReadingData[routeId]
        val grammar = examGrammarData[routeId]
        val writing = examWritingData[routeId]
        if (covers.size != 4 || covers.map { it.order }.sorted() != listOf(1, 2, 3, 4) ||
            covers.map { it.examRef }.distinct().size != 1 || covers.any { it.durationSeconds <= 0 || it.maximumQuarter <= 0 } ||
            covers.map { it.module }.toSet() != setOf("listening", "reading", "grammar", "writing") ||
            listening == null || reading == null || grammar == null || writing == null ||
            listening.tasks.isEmpty() || listening.audioUrl.isBlank() || listening.tasks.any { it.endMs <= it.startMs } ||
            reading.passages.isEmpty() || reading.questions.isEmpty() || grammar.isEmpty() || writing.tasks.isEmpty() ||
            covers.first { it.module == "listening" }.maximumQuarter != listening.tasks.sumOf { task -> task.questions.sumOf { it.pointsQuarter } } ||
            covers.first { it.module == "reading" }.maximumQuarter != reading.questions.sumOf { it.pointsQuarter } ||
            covers.first { it.module == "grammar" }.maximumQuarter != grammar.sumOf { test -> test.items.sumOf { it.maximumQuarter } } ||
            covers.first { it.module == "writing" }.maximumQuarter != writing.maximumQuarter) null
        else routeId to ExamSessionConfig(
            covers.first().examRef,
            covers.sortedBy { it.order }.map { ExamModuleConfig(ExamModule.entries.first { module -> module.name.lowercase() == it.module }, it.durationSeconds, it.maximumQuarter) },
            listening, reading, grammar, writing,
        )
    }.toMap()
}

private data class ModuleCover(val examRef: String, val module: String, val order: Int, val durationSeconds: Int, val maximumQuarter: Int)
