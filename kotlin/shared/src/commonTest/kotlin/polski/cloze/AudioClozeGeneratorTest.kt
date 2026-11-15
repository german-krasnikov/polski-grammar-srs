package polski.cloze

import polski.core.engine.RandomSource
import polski.exam.listening.ListeningExam
import polski.exam.listening.ListeningTask
import polski.exam.officialListeningData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AudioClozeGeneratorTest {
    private val deterministicRandom = RandomSource { 0.0 }
    private val generator = AudioClozeGenerator(deterministicRandom)

    private val sampleTranscript =
        "Moja siostra poszła do sklepu po chleb i mleko. " +
        "Kupuje zawsze dobre produkty z miejscowego rynku. " +
        "Polska kuchnia jest smaczna oraz zdrowa dla każdego."

    private fun taskWithTranscript(id: String = "t1", transcript: String? = sampleTranscript) = ListeningTask(
        id = id, instruction = "Listen", questions = emptyList(),
        startMs = 1000, endMs = 5000, transcript = transcript,
    )

    // 1. Null transcript → empty
    @Test fun fromTranscript_null_transcript_returns_empty() {
        val result = generator.fromTranscript("r1", taskWithTranscript(transcript = null), "audio.mp3")
        assertTrue(result.isEmpty())
    }

    // 2. Short transcript → empty (< 20 chars, < 4 tokens)
    @Test fun fromTranscript_short_transcript_returns_empty() {
        val result = generator.fromTranscript("r1", taskWithTranscript(transcript = "To."), "audio.mp3")
        assertTrue(result.isEmpty())
    }

    // 3. Real transcript produces exercises
    @Test fun fromTranscript_produces_exercises() {
        val result = generator.fromTranscript("r1", taskWithTranscript(), "audio.mp3")
        assertTrue(result.isNotEmpty(), "Expected exercises from real transcript")
    }

    // 4. Generated exercise has correct structure
    @Test fun fromTranscript_exercise_structure() {
        val exercises = generator.fromTranscript("r1", taskWithTranscript(), "audio.mp3")
        val ex = exercises.first()
        assertTrue(ex.correctForm in ex.options)
        assertEquals(3, ex.options.size)
        assertEquals(3, ex.options.distinct().size)
        assertEquals("audio.mp3", ex.audioUrl)
        assertTrue(ex.startMs >= 1000L, "Sentence start should be >= task start")
        assertTrue(ex.endMs <= 5000L, "Sentence end should be <= task end")
        assertTrue(ex.endMs - ex.startMs < 5000L - 1000L, "Sentence segment should be shorter than full task")
        assertTrue(ex.source is AudioClozeSource.ListeningTranscript)
    }

    // 5. Gap word is a content word (displayed sentence contains "___")
    @Test fun fromTranscript_gap_is_content_word() {
        val exercises = generator.fromTranscript("r1", taskWithTranscript(), "audio.mp3")
        for (ex in exercises) {
            assertTrue(ex.displaySentence.contains("___"), "Display sentence must contain gap: ${ex.displaySentence}")
            // correctForm should be a word >= 3 chars (content word requirement)
            assertTrue(ex.correctForm.length >= 3, "Gap word should be content word: ${ex.correctForm}")
        }
    }

    // 6. Distractors are distinct from correct form
    @Test fun fromTranscript_distractors_distinct_from_correct() {
        val exercises = generator.fromTranscript("r1", taskWithTranscript(), "audio.mp3")
        for (ex in exercises) {
            val distractors = ex.options.filter { it != ex.correctForm }
            assertEquals(2, distractors.size)
            assertTrue(distractors.none { it == ex.correctForm })
        }
    }

    // 7. Pool.drawFrom filters by exam
    @Test fun pool_drawFrom_filters_by_exam() {
        val exam1 = ListeningExam(listOf(taskWithTranscript("t1")), "audio1.mp3")
        val exam2 = ListeningExam(listOf(taskWithTranscript("t2")), "audio2.mp3")
        val pool = DefaultAudioClozePool(mapOf("r1" to exam1, "r2" to exam2), generator)
        val fromR1 = pool.drawFrom("r1", 100, deterministicRandom)
        assertTrue(fromR1.isNotEmpty())
        assertTrue(fromR1.all { it.id.startsWith("transcript:t1:") })
    }

    // 8. availableExams returns exams with transcripts
    @Test fun pool_availableExams_non_empty() {
        val exam1 = ListeningExam(listOf(taskWithTranscript("t1")), "audio1.mp3")
        val emptyExam = ListeningExam(listOf(taskWithTranscript("t2", transcript = null)), "audio2.mp3")
        val pool = DefaultAudioClozePool(mapOf("r1" to exam1, "r2" to emptyExam), generator)
        val available = pool.availableExams()
        assertTrue("r1" in available)
        assertTrue("r2" !in available)
    }

    // 9. Integration: pool from officialListeningData produces exercises (50 transcripts exist)
    @Test fun pool_officialData_produces_exercises() {
        val pool = DefaultAudioClozePool(officialListeningData, AudioClozeGenerator(RandomSource { 0.5 }))
        val drawn = pool.draw(10, RandomSource { 0.5 })
        assertTrue(drawn.isNotEmpty(), "Expected exercises from official data with transcripts")
        assertEquals(10, drawn.size.coerceAtMost(10))
    }

    // 10. Reducer SelectExam works
    @Test fun reducer_selectExam() {
        val ex = AudioClozeExercise(
            id = "e1", displaySentence = "Moja ___ poszła.", correctForm = "siostra",
            options = listOf("siostra", "brat", "matka"),
            audioUrl = "a.mp3", startMs = 0, endMs = 1000,
            source = AudioClozeSource.ListeningTranscript("r", "t"),
            gapLexemeKey = "k",
        )
        val state = AudioClozeSessionState(exercises = listOf(ex))
        val after = reduceAudioClozeSession(state, AudioClozeSessionAction.SelectExam("b2-2019"))
        assertEquals("b2-2019", after.selectedExamRouteId)
        val cleared = reduceAudioClozeSession(after, AudioClozeSessionAction.SelectExam(null))
        assertEquals(null, cleared.selectedExamRouteId)
    }

    @Test fun reducer_choose_idempotent() {
        val ex = AudioClozeExercise(
            id = "e1", displaySentence = "Moja ___ poszła.", correctForm = "siostra",
            options = listOf("siostra", "brat", "matka"),
            audioUrl = "a.mp3", startMs = 0, endMs = 1000,
            source = AudioClozeSource.ListeningTranscript("r", "t"),
            gapLexemeKey = "k",
        )
        val state = AudioClozeSessionState(exercises = listOf(ex))
        val after = reduceAudioClozeSession(state, AudioClozeSessionAction.Choose("e1", "siostra"))
        assertEquals("siostra", after.answers["e1"])
        val again = reduceAudioClozeSession(after, AudioClozeSessionAction.Choose("e1", "brat"))
        assertEquals("siostra", again.answers["e1"])
    }

    @Test fun reducer_recordReplay_increments() {
        val ex = AudioClozeExercise(
            id = "e1", displaySentence = "___", correctForm = "poszła",
            options = listOf("poszła", "pójdzie", "idzie"),
            audioUrl = "a.mp3", startMs = 0, endMs = 1000,
            source = AudioClozeSource.ListeningTranscript("r", "t"),
            gapLexemeKey = "k",
        )
        val state = AudioClozeSessionState(exercises = listOf(ex))
        val after1 = reduceAudioClozeSession(state, AudioClozeSessionAction.RecordReplay("e1"))
        assertEquals(1, after1.replayCount["e1"])
        val after2 = reduceAudioClozeSession(after1, AudioClozeSessionAction.RecordReplay("e1"))
        assertEquals(2, after2.replayCount["e1"])
    }

    @Test fun reducer_restart_clears_all() {
        val ex = AudioClozeExercise(
            id = "e1", displaySentence = "___", correctForm = "poszła",
            options = listOf("poszła", "pójdzie", "idzie"),
            audioUrl = "a.mp3", startMs = 0, endMs = 1000,
            source = AudioClozeSource.ListeningTranscript("r", "t"),
            gapLexemeKey = "k",
        )
        val state = AudioClozeSessionState(
            exercises = listOf(ex), currentIndex = 0,
            answers = mapOf("e1" to "poszła"), replayCount = mapOf("e1" to 3),
        )
        val restarted = reduceAudioClozeSession(state, AudioClozeSessionAction.Restart)
        assertEquals(0, restarted.currentIndex)
        assertTrue(restarted.answers.isEmpty())
        assertTrue(restarted.replayCount.isEmpty())
    }

    @Test fun exercise_init_rejects_wrong_options() {
        assertFailsWith<IllegalArgumentException> {
            AudioClozeExercise(
                id = "x", displaySentence = "___", correctForm = "wrong",
                options = listOf("poszła", "pójdzie", "idzie"),
                audioUrl = "a.mp3", startMs = 0, endMs = 1000,
                source = AudioClozeSource.ListeningTranscript("r", "t"),
                gapLexemeKey = "k",
            )
        }
    }
}
