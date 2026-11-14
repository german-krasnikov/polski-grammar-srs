package polski.cloze

import polski.core.engine.RandomSource
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.core.engine.TableMorphology
import polski.core.model.CoreExercise
import polski.core.model.TilePhrase
import polski.exam.QuestionType
import polski.exam.grammar.GrammarItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** AC1-AC5 and AC8-AC9 from CLOZE-EXERCISE-PLAN.md */
class ClozeGeneratorTest {
    private fun bundle(vararg pairs: Pair<String, String>) =
        pairs.associate { (k, v) -> FeatureKey(k) to FeatureValue(v) }

    private val morphology = TableMorphology(
        mapOf(
            "noun:wife" to mapOf(
                bundle("Number" to "sg", "Case" to "nom") to "żona",
                bundle("Number" to "sg", "Case" to "acc") to "żonę",
                bundle("Number" to "sg", "Case" to "gen") to "żony",
            ),
            "noun:tiny" to mapOf(
                // Only one form — cannot yield 2 distractors
                bundle("Number" to "sg", "Case" to "nom") to "mała",
            ),
        ),
    )

    private val deterministicRandom = RandomSource { 0.0 }
    private val generator = ClozeGenerator(morphology, deterministicRandom)

    // --- fromExamItem ---

    private fun examItem(
        questionType: QuestionType = QuestionType.ChooseForm,
        correctAnswer: String = "żonę",
        options: List<String> = listOf("żona", "żonę", "żony"),
        itemId: String = "test-1",
    ) = GrammarItem(
        itemId = itemId,
        questionType = questionType,
        context = "Widzę ___ tutaj.",
        gap = "___",
        options = options,
        correctAnswer = correctAnswer,
        points = 1,
    )

    @Test fun fromExamItem_nullForNonChooseForm() {
        // AC1
        assertNull(generator.fromExamItem(examItem(questionType = QuestionType.FillGap)))
    }

    @Test fun fromExamItem_correctFormAndOptionsMatchItem() {
        // AC2
        val ex = generator.fromExamItem(examItem())
        assertNotNull(ex)
        assertEquals("żonę", ex.correctForm)
        assertTrue(ex.correctForm in ex.options, "correctForm must be in options")
        assertEquals(3, ex.options.size)
        assertEquals(3, ex.options.distinct().size, "all options must be distinct")
    }

    @Test fun fromExamItem_nullWhenFewerThanThreeDistinctOptions() {
        // correctAnswer == all options (edge case: not enough distractors)
        val item = GrammarItem(
            itemId = "t", questionType = QuestionType.ChooseForm,
            context = "___", gap = "___", options = listOf("żonę"), correctAnswer = "żonę", points = 1,
        )
        assertNull(generator.fromExamItem(item))
    }

    // --- fromCoreExercise ---

    private val tilePhrase = TilePhrase(
        construction = "test",
        bundle = mapOf("Number" to "sg", "Case" to "acc"),
        text = "żonę",
        slotSpans = mapOf("noun" to (0 until 4)),
    )

    private fun coreExercise(
        expected: String = "Widzę żonę tutaj.",
        tileTargetSlots: List<String> = listOf("noun"),
        phrase: TilePhrase? = tilePhrase,
        slots: Map<String, String> = mapOf("noun" to "wife"),
    ) = CoreExercise(
        id = "ex-1", primarySkill = "case.acc", source = "s", prompt = "p",
        expected = expected, explanation = "e", tags = emptyList(), changes = emptyList(),
        slots = slots, tileTargetSlots = tileTargetSlots, tilePhrase = phrase,
    )

    @Test fun fromCoreExercise_nullWhenEmptyTileTargetSlots() {
        // AC3
        assertNull(generator.fromCoreExercise(coreExercise(tileTargetSlots = emptyList())))
    }

    @Test fun fromCoreExercise_validExerciseHasCorrectStructure() {
        // AC4
        val ex = generator.fromCoreExercise(coreExercise())
        assertNotNull(ex)
        assertEquals(3, ex.options.size, "must have 3 options")
        assertTrue(ex.correctForm in ex.options, "correctForm must be in options")
        assertEquals(3, ex.options.distinct().size, "all options must be distinct")
        assertTrue(ex.displaySentence.contains("___"), "display sentence must contain gap")
        assertEquals("żonę", ex.correctForm)
    }

    @Test fun fromCoreExercise_displaysGapAtCorrectPosition() {
        // AC4 continued: exact display sentence shape
        val ex = generator.fromCoreExercise(coreExercise())!!
        assertEquals("Widzę ___ tutaj.", ex.displaySentence)
        assertEquals("żonę", ex.correctForm)
    }

    @Test fun fromCoreExercise_nullWhenParadigmYieldsTooFewDistractors() {
        // AC5: "noun:tiny" has only one form — no 2 distractors possible
        val phrase = TilePhrase("test", mapOf("Number" to "sg"), "mała", mapOf("noun" to (0 until 4)))
        assertNull(
            generator.fromCoreExercise(
                coreExercise(
                    expected = "Widzę mała tutaj.",
                    phrase = phrase,
                    slots = mapOf("noun" to "tiny"),
                ),
            ),
        )
    }

    // --- reduceClozeSession ---

    private fun exercise(id: String) = ClozeExercise(
        id = id, displaySentence = "___", correctForm = "żona",
        options = listOf("żona", "żonę", "żony"), source = ClozeSource.ExamItem("x"), gapLexemeKey = "k",
    )

    @Test fun reduce_chooseRecordsAnswerOnce() {
        // AC8
        val state = ClozeSessionState(exercises = listOf(exercise("e1")))
        val after = reduceClozeSession(state, ClozeSessionAction.Choose("e1", "żona"))
        assertEquals("żona", after.answers["e1"])
        // second Choose on same id is ignored
        val again = reduceClozeSession(after, ClozeSessionAction.Choose("e1", "żonę"))
        assertEquals("żona", again.answers["e1"])
    }

    @Test fun reduce_nextAndPreviousGuardBounds() {
        // AC9
        val state = ClozeSessionState(exercises = listOf(exercise("e1"), exercise("e2")), currentIndex = 0)
        assertEquals(0, reduceClozeSession(state, ClozeSessionAction.Previous).currentIndex, "Previous at 0 is no-op")
        val next = reduceClozeSession(state, ClozeSessionAction.Next)
        assertEquals(1, next.currentIndex)
        assertEquals(1, reduceClozeSession(next, ClozeSessionAction.Next).currentIndex, "Next at last is no-op")
    }

    @Test fun reduce_restartZerosIndexAndAnswers() {
        val state = ClozeSessionState(exercises = listOf(exercise("e1")), currentIndex = 0, answers = mapOf("e1" to "żona"))
        val restarted = reduceClozeSession(state, ClozeSessionAction.Restart)
        assertEquals(0, restarted.currentIndex)
        assertTrue(restarted.answers.isEmpty())
    }
}
