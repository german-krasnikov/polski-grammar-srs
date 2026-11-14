package polski.cloze

import polski.core.engine.MorphologyParadigm
import polski.core.engine.RandomSource
import polski.core.engine.pickDistractors
import polski.core.model.CoreExercise
import polski.exam.QuestionType
import polski.exam.grammar.GrammarItem

/** Builds [ClozeExercise]s from two sources: authored exam items (uses authored options directly)
 *  and core exercises (derives distractors from [MorphologyParadigm]). Fail-soft: returns null
 *  when preconditions are not met rather than throwing. */
class ClozeGenerator(
    private val morphology: MorphologyParadigm,
    private val random: RandomSource,
) {
    /**
     * Returns null when item.questionType != ChooseForm or item.options.size < 3.
     * Uses authored options directly — no morphology lookup needed.
     */
    fun fromExamItem(item: GrammarItem): ClozeExercise? {
        if (item.questionType != QuestionType.ChooseForm) return null
        if (item.context.split("___").size != 2) return null
        val correct = item.correctAnswer
        val others = item.options.filter { it != correct }
        if (others.size < 2) return null
        val three = listOf(correct) + others.take(2)
        return ClozeExercise(
            id = "exam:${item.itemId}",
            displaySentence = item.context,
            correctForm = correct,
            options = shuffled(three),
            source = ClozeSource.ExamItem(item.itemId),
            gapLexemeKey = "exam:${item.itemId}",
        )
    }

    /**
     * Returns null when exercise.tileTargetSlots is empty, tilePhrase is null, phrase text is not
     * found in expected, no eligible slot yields 2+ distractors from the paradigm, or the slot
     * lexeme is unknown.
     * Builds displaySentence by replacing the slot's surface span in exercise.expected with "___".
     */
    fun fromCoreExercise(exercise: CoreExercise): ClozeExercise? {
        val phrase = exercise.tilePhrase ?: return null
        if (exercise.tileTargetSlots.isEmpty()) return null
        val offset = exercise.expected.indexOf(phrase.text)
        if (offset < 0) return null
        val slotName = exercise.tileTargetSlots.firstOrNull { it in phrase.slotSpans } ?: return null
        val span = phrase.slotSpans[slotName] ?: return null
        val absStart = span.first + offset
        val absEnd = span.last + 1 + offset
        val correctForm = exercise.expected.substring(absStart, absEnd)
        val lexeme = exercise.slots[slotName] ?: return null
        val lexemeKey = "$slotName:$lexeme"
        val distractors = pickDistractors(morphology, lexemeKey, correctForm, 2, random)
        if (distractors.size < 2) return null
        val displaySentence = exercise.expected.substring(0, absStart) + "___" + exercise.expected.substring(absEnd)
        return ClozeExercise(
            id = "course:${exercise.id}",
            displaySentence = displaySentence,
            correctForm = correctForm,
            options = shuffled(listOf(correctForm) + distractors),
            source = ClozeSource.CourseExercise(exercise.id, exercise.primarySkill),
            gapLexemeKey = lexemeKey,
        )
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
