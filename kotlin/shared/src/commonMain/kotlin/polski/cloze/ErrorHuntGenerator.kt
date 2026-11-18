package polski.cloze

import polski.cloze.policy.Policy
import polski.core.engine.MorphologyParadigm
import polski.core.engine.RandomSource
import polski.core.engine.pickDistractors
import polski.core.model.CoreExercise
import polski.grammar.normalize

/**
 * Generates [ErrorHuntExercise]s from core exercises. [theoryFor] supplies per-skill rule text
 * from the active course pack without coupling this class to the pack globals.
 * Fail-soft: returns null when preconditions fail. Known limitation: multi-word slots are skipped.
 * Syncretism may slip through the validity guard; noted here as a known open issue.
 */
class ErrorHuntGenerator(
    private val morphology: MorphologyParadigm,
    private val random: RandomSource,
    private val theoryFor: (skillId: String) -> String? = { null },
) {
    fun fromCoreExercise(exercise: CoreExercise): ErrorHuntExercise? {
        val slot = extractSlot(exercise) ?: return null
        if (slot.correctForm.contains(' ')) return null  // multi-word: MVP skip

        val candidates = pickDistractors(morphology, slot.lexemeKey, slot.correctForm, 2, random)
        if (candidates.size < 2) return null

        val valid = candidates.filter { isValid(it, slot, exercise) }
        if (valid.size < 2) return null

        val errorForm = valid[0]
        val option3 = valid[1]

        val errorSentence = exercise.expected.substring(0, slot.absStart) + errorForm + exercise.expected.substring(slot.absEnd)
        val tokens = errorSentence.split(Regex("\\s+")).filter { it.isNotEmpty() }

        val prefixTokenCount = exercise.expected.substring(0, slot.absStart)
            .split(Regex("\\s+")).filter { it.isNotEmpty() }.size
        if (prefixTokenCount !in tokens.indices) return null
        if (!tokens[prefixTokenCount].contains(errorForm)) return null

        val originalTokens = exercise.expected.split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (prefixTokenCount !in originalTokens.indices) return null

        val ruleText = exercise.explanation.takeIf { it.isNotBlank() }
            ?: theoryFor(exercise.primarySkill)

        return ErrorHuntExercise(
            id = "hunt:${exercise.id}",
            tokens = tokens,
            errorTokenIndex = prefixTokenCount,
            correctToken = originalTokens[prefixTokenCount],
            errorForm = errorForm,
            correctForm = slot.correctForm,
            options = shuffled(listOf(slot.correctForm, errorForm, option3)),
            gapLexemeKey = slot.lexemeKey,
            skillId = exercise.primarySkill,
            ruleText = ruleText,
            sourceExerciseId = exercise.id,
        )
    }

    private fun isValid(distractor: String, slot: SlotData, exercise: CoreExercise): Boolean {
        val equalsCorrect = normalize(distractor) == normalize(slot.correctForm)
        val sentenceWithD = exercise.expected.substring(0, slot.absStart) + distractor + exercise.expected.substring(slot.absEnd)
        val normalSentence = normalize(sentenceWithD)
        val validSentences = (listOf(exercise.expected) + exercise.accepted).map { normalize(it) }
        val inAccepted = normalSentence in validSentences
        return Policy.distractorValid(Policy.DistractorValidInput(equalsCorrect, inAccepted)) ==
            Policy.DistractorValidValues.Valid
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
