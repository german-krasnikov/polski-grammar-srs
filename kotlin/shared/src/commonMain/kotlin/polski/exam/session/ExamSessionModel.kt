package polski.exam.session

import polski.exam.ExamModule
import polski.exam.grammar.GrammarTest
import polski.exam.listening.ListeningExam
import polski.exam.reading.ReadingExam
import polski.exam.writing.WritingExam

/** Certified cover duration in seconds and official maximum in quarter points. */
data class ExamModuleConfig(val module: ExamModule, val durationSeconds: Int, val maximumQuarter: Int)

/** One sitting with four source-ordered written modules and their mapped parts. */
data class ExamSessionConfig(
    val examId: String, val modules: List<ExamModuleConfig>,
    val listening: ListeningExam, val reading: ReadingExam,
    val grammar: List<GrammarTest>, val writing: WritingExam,
) {
    init {
        require(examId.isNotBlank())
        require(modules.size == 4 && modules.map { it.module }.toSet() == ExamModule.entries.toSet())
        require(modules.all { it.durationSeconds > 0 && it.maximumQuarter > 0 })
        require(listening.tasks.isNotEmpty() && listening.audioUrl.isNotBlank())
        require(reading.passages.isNotEmpty() && reading.questions.isNotEmpty())
        require(grammar.isNotEmpty() && writing.tasks.isNotEmpty())
        require(modules.first { it.module == ExamModule.Listening }.maximumQuarter == listening.tasks.sumOf { task -> task.questions.sumOf { it.pointsQuarter } })
        require(modules.first { it.module == ExamModule.Reading }.maximumQuarter == reading.questions.sumOf { it.pointsQuarter })
        require(modules.first { it.module == ExamModule.Grammar }.maximumQuarter == grammar.sumOf { test -> test.items.sumOf { it.pointsQuarter ?: it.points * 4 } })
        require(writing.maximumQuarter == modules.first { it.module == ExamModule.Writing }.maximumQuarter)
    }
}
