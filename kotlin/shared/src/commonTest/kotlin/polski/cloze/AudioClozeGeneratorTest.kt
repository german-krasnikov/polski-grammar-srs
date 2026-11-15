package polski.cloze

import polski.core.engine.RandomSource
import polski.exam.ExamGradingMode
import polski.exam.QuestionType
import polski.exam.listening.ListeningExam
import polski.exam.listening.ListeningQuestion
import polski.exam.listening.ListeningTask
import polski.exam.officialListeningData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AudioClozeGeneratorTest {
    private val deterministicRandom = RandomSource { 0.0 }
    private val generator = AudioClozeGenerator(deterministicRandom)

    private fun task(id: String = "t1", startMs: Long = 1000, endMs: Long = 5000) = ListeningTask(
        id = id, instruction = "Listen", questions = emptyList(),
        startMs = startMs, endMs = endMs,
    )

    private fun question(
        id: String = "q1",
        type: QuestionType = QuestionType.ChooseForm,
        text: String = "Moja siostra ___ do sklepu.",
        options: List<String> = listOf("poszła", "pójdzie", "idzie"),
        correct: String = "poszła",
    ) = ListeningQuestion(
        id = id, type = type, questionText = text, options = options,
        correctAnswer = correct, pointsQuarter = 4, gradingMode = ExamGradingMode.Exact,
    )

    @Test fun generator_skips_nonChooseForm() {
        assertNull(generator.fromListeningQuestion("r1", task(), question(type = QuestionType.FillGap), "audio.mp3"))
    }

    @Test fun generator_skips_missing_gap_marker() {
        assertNull(generator.fromListeningQuestion("r1", task(), question(text = "Moja siostra poszła do sklepu."), "audio.mp3"))
    }

    @Test fun generator_skips_fewer_than_two_distractors() {
        val q = question(options = listOf("poszła"), correct = "poszła")
        assertNull(generator.fromListeningQuestion("r1", task(), q, "audio.mp3"))
    }

    @Test fun generator_happy_path() {
        val ex = generator.fromListeningQuestion("route1", task(), question(), "audio.mp3")
        assertNotNull(ex)
        assertEquals("Moja siostra ___ do sklepu.", ex.displaySentence)
        assertEquals("poszła", ex.correctForm)
        assertTrue(ex.correctForm in ex.options)
        assertEquals(3, ex.options.size)
        assertEquals(3, ex.options.distinct().size)
        assertEquals("audio.mp3", ex.audioUrl)
        assertEquals(1000L, ex.startMs)
        assertEquals(5000L, ex.endMs)
        assertEquals(AudioClozeSource.ListeningQuestion("route1", "t1", "q1"), ex.source)
    }

    @Test fun pool_draw_returns_subset() {
        val exam = ListeningExam(
            tasks = (1..5).map { i ->
                ListeningTask(
                    id = "t$i", instruction = "L", startMs = 0, endMs = 1000,
                    questions = listOf(question(id = "q$i")),
                )
            },
            audioUrl = "audio.mp3",
        )
        val pool = DefaultAudioClozePool(mapOf("r1" to exam), generator)
        val drawn = pool.draw(3, deterministicRandom)
        assertEquals(3, drawn.size)
    }

    @Test fun pool_draw_returns_all_when_limit_exceeds_size() {
        val exam = ListeningExam(
            tasks = (1..2).map { i ->
                ListeningTask(
                    id = "t$i", instruction = "L", startMs = 0, endMs = 1000,
                    questions = listOf(question(id = "q$i")),
                )
            },
            audioUrl = "audio.mp3",
        )
        val pool = DefaultAudioClozePool(mapOf("r1" to exam), generator)
        val drawn = pool.draw(100, deterministicRandom)
        assertEquals(2, drawn.size)
    }

    private fun exercise(id: String = "e1") = AudioClozeExercise(
        id = id, displaySentence = "___", correctForm = "poszła",
        options = listOf("poszła", "pójdzie", "idzie"),
        audioUrl = "a.mp3", startMs = 0, endMs = 1000,
        source = AudioClozeSource.ListeningQuestion("r", "t", "q"),
        gapLexemeKey = "k",
    )

    @Test fun reducer_choose_idempotent() {
        val state = AudioClozeSessionState(exercises = listOf(exercise()))
        val after = reduceAudioClozeSession(state, AudioClozeSessionAction.Choose("e1", "poszła"))
        assertEquals("poszła", after.answers["e1"])
        val again = reduceAudioClozeSession(after, AudioClozeSessionAction.Choose("e1", "pójdzie"))
        assertEquals("poszła", again.answers["e1"])
    }

    @Test fun reducer_recordReplay_increments() {
        val state = AudioClozeSessionState(exercises = listOf(exercise()))
        val after1 = reduceAudioClozeSession(state, AudioClozeSessionAction.RecordReplay("e1"))
        assertEquals(1, after1.replayCount["e1"])
        val after2 = reduceAudioClozeSession(after1, AudioClozeSessionAction.RecordReplay("e1"))
        assertEquals(2, after2.replayCount["e1"])
    }

    @Test fun reducer_restart_clears_all() {
        val state = AudioClozeSessionState(
            exercises = listOf(exercise()),
            currentIndex = 0,
            answers = mapOf("e1" to "poszła"),
            replayCount = mapOf("e1" to 3),
        )
        val restarted = reduceAudioClozeSession(state, AudioClozeSessionAction.Restart)
        assertEquals(0, restarted.currentIndex)
        assertTrue(restarted.answers.isEmpty())
        assertTrue(restarted.replayCount.isEmpty())
    }

    @Test fun pool_draw_ids_are_distinct() {
        val exam = ListeningExam(
            tasks = (1..5).map { i ->
                ListeningTask(
                    id = "t$i", instruction = "L", startMs = 0, endMs = 1000,
                    questions = listOf(question(id = "q$i")),
                )
            },
            audioUrl = "audio.mp3",
        )
        val pool = DefaultAudioClozePool(mapOf("r1" to exam), generator)
        val drawn = pool.draw(10, deterministicRandom)
        assertEquals(drawn.size, drawn.map { it.id }.distinct().size)
    }

    @Test fun poolDoesNotCrashOnOfficialData() {
        // Official published data contains only MultipleChoice/FillGap — no ChooseForm+gap questions —
        // so the pool is legitimately empty. The test proves the generator handles the full dataset without crashing.
        val random = RandomSource { 0.0 }
        val pool = DefaultAudioClozePool(officialListeningData, AudioClozeGenerator(random))
        val drawn = pool.draw(10, random)
        assertTrue(drawn.size <= 10)
    }

    @Test fun exercise_init_rejects_wrong_options() {
        assertFailsWith<IllegalArgumentException> {
            AudioClozeExercise(
                id = "x", displaySentence = "___", correctForm = "wrong",
                options = listOf("poszła", "pójdzie", "idzie"),
                audioUrl = "a.mp3", startMs = 0, endMs = 1000,
                source = AudioClozeSource.ListeningQuestion("r", "t", "q"),
                gapLexemeKey = "k",
            )
        }
    }
}
