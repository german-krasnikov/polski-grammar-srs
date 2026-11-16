package polski.cloze

import polski.core.engine.RandomSource
import polski.exam.listening.ListeningExam
import polski.exam.listening.ListeningTask

class AudioClozeGenerator(private val random: RandomSource) {

    fun fromTranscript(examRouteId: String, task: ListeningTask, audioUrl: String): List<AudioClozeExercise> {
        val transcript = task.transcript ?: return emptyList()
        val sentences = splitSentences(transcript)
        if (sentences.isEmpty()) return emptyList()
        val allWords = sentences.flatMap { tokenize(it) }
            .filter { isContentWord(it) }.map { it.lowercase() }.distinct()
        val aligned = TranscriptTimings.data[task.id]
        val totalChars = sentences.sumOf { it.length }
        val taskDuration = task.endMs - task.startMs
        var charOffset = 0
        return sentences.mapIndexedNotNull { sentIdx, sentence ->
            val timing = aligned?.getOrNull(sentIdx)
            val sentStartMs = timing?.first ?: (task.startMs + charOffset.toLong() * taskDuration / totalChars)
            charOffset += sentence.length
            val sentEndMs = timing?.second ?: (task.startMs + charOffset.toLong() * taskDuration / totalChars)
            buildExercise(examRouteId, task, audioUrl, sentence, sentIdx, allWords, sentStartMs, sentEndMs)
        }
    }

    fun fromExam(examRouteId: String, exam: ListeningExam): List<AudioClozeExercise> =
        exam.tasks.flatMap { task -> fromTranscript(examRouteId, task, exam.audioUrl) }

    private fun buildExercise(
        examRouteId: String,
        task: ListeningTask,
        audioUrl: String,
        sentence: String,
        sentenceIndex: Int,
        wordPool: List<String>,
        sentStartMs: Long,
        sentEndMs: Long,
    ): AudioClozeExercise? {
        val tokens = tokenize(sentence)
        val candidates = tokens.withIndex().filter { (_, w) -> isContentWord(w) && w.length >= 3 }.toList()
        if (candidates.isEmpty()) return null
        val pick = candidates[(random.nextDouble() * candidates.size).toInt().coerceIn(0, candidates.lastIndex)]
        val gapWord = pick.value
        val displayTokens = tokens.toMutableList()
        displayTokens[pick.index] = "___"
        val displaySentence = displayTokens.joinToString(" ")
        val distractors = shuffled(wordPool.filter { it != gapWord.lowercase() && it.length >= 2 }).take(2)
        if (distractors.size < 2) return null
        val options = shuffled(listOf(gapWord) + distractors)
        return AudioClozeExercise(
            id = "transcript:${task.id}:$sentenceIndex",
            displaySentence = displaySentence,
            correctForm = gapWord,
            options = options,
            audioUrl = audioUrl,
            startMs = sentStartMs,
            endMs = sentEndMs,
            source = AudioClozeSource.ListeningTranscript(examRouteId, task.id),
            gapLexemeKey = "transcript:${task.id}:$gapWord",
        )
    }

    private fun splitSentences(text: String): List<String> =
        text.split(Regex("(?<=[.!?])\\s+"))
            .map { it.trim() }
            .filter { it.length >= 20 && tokenize(it).size >= 4 }

    private fun tokenize(sentence: String): List<String> =
        sentence.split(Regex("\\s+"))
            .map { it.trim(',', '.', '!', '?', ':', ';', '(', ')', '"', '„', '"', '«', '»', '–', '—', '-') }
            .filter { it.isNotEmpty() }

    private fun isContentWord(word: String): Boolean {
        if (word.length < 3) return false
        return word.lowercase() !in STOP_WORDS
    }

    private fun shuffled(items: List<String>): List<String> {
        val pool = items.toMutableList()
        for (i in pool.lastIndex downTo 1) {
            val j = (random.nextDouble() * (i + 1)).toInt().coerceIn(0, i)
            val tmp = pool[i]; pool[i] = pool[j]; pool[j] = tmp
        }
        return pool
    }

    companion object {
        private val STOP_WORDS = setOf(
            "ale", "ani", "bez", "czy", "dla", "gdy", "jak", "już",
            "lub", "nad", "nie", "pod", "tak", "ten",
            "też", "tym", "być", "jest", "się", "które", "który",
            "która", "tego", "tej", "tych", "ich", "jego",
            "jej", "nas", "was", "niej", "nim", "nich",
            "oraz", "jako", "więc", "jednak", "bardzo",
            "może", "tylko", "przez", "przy", "będzie",
            "było", "była", "byli", "były", "jedno", "jedna",
            "jeden", "jednym", "jednej", "między",
            "roku",
        )
    }
}
