package polski.cloze

import polski.core.engine.RandomSource
import polski.exam.QuestionType
import polski.exam.listening.ListeningExam
import polski.exam.listening.ListeningQuestion
import polski.exam.listening.ListeningTask

class AudioClozeGenerator(private val random: RandomSource) {

    fun fromListeningQuestion(
        examRouteId: String,
        task: ListeningTask,
        question: ListeningQuestion,
        audioUrl: String,
    ): AudioClozeExercise? {
        if (question.type != QuestionType.ChooseForm) return null
        if (!question.questionText.contains("___")) return null
        val correct = question.correctAnswer
        val distractors = question.options.filter { it != correct }.distinct()
        if (distractors.size < 2) return null
        val three = listOf(correct) + distractors.take(2)
        return AudioClozeExercise(
            id = "listening:${question.id}",
            displaySentence = question.questionText,
            correctForm = correct,
            options = shuffled(three),
            audioUrl = audioUrl,
            startMs = task.startMs,
            endMs = task.endMs,
            source = AudioClozeSource.ListeningQuestion(examRouteId, task.id, question.id),
            gapLexemeKey = "listening:${question.id}",
        )
    }

    fun fromExam(examRouteId: String, exam: ListeningExam): List<AudioClozeExercise> =
        exam.tasks.flatMap { task ->
            task.questions.mapNotNull { q ->
                fromListeningQuestion(examRouteId, task, q, exam.audioUrl)
            }
        }

    private fun shuffled(items: List<String>): List<String> {
        val pool = items.toMutableList()
        for (i in pool.lastIndex downTo 1) {
            val j = (random.nextDouble() * (i + 1)).toInt().coerceIn(0, i)
            val tmp = pool[i]; pool[i] = pool[j]; pool[j] = tmp
        }
        return pool
    }
}
