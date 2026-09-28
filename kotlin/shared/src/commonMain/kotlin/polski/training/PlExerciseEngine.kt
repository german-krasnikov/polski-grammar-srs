package polski.training

import polski.core.engine.ExerciseIdFactory
import polski.core.engine.RandomSource
import polski.core.model.CoreExercise
import polski.core.plExerciseGenerator
import polski.core.plChainSteps
import polski.data.courseSentenceSeeds
import polski.model.Exercise
import polski.model.FormChange
import polski.model.NumberGram
import polski.model.PossessiveId
import polski.model.SentenceSeed

val sentenceSeeds: List<SentenceSeed> by lazy { courseSentenceSeeds }

/**
 * UniversalCorePlan.md §5.3/§12 UC-08: the live pl-ru exercise generator — `:core-engine`'s
 * generic `ExerciseGenerator` (`polski.core.plExerciseGenerator`), wired to pl's own pack data,
 * exposed with the exact shape `TrainingStore`/every host already calls. Replaces the deleted
 * `ExerciseFactory`'s hand-written `when(skillId)` branches; `TrainingParityTest` (pinned literal
 * `Exercise` values from the React oracle) is this replacement's permanent byte-parity guard.
 */
class PlExerciseEngine(random: RandomSource, ids: ExerciseIdFactory) {
    private val generator = plExerciseGenerator(random, ids)

    fun generateForSkill(skillId: String, preferredSeed: SentenceSeed? = null): Exercise =
        generator.generateForSkill(skillId, preferredSeed?.toSlots()).toExercise()

    fun generateChain(seed: SentenceSeed = sentenceSeeds.first()): List<Exercise> =
        generator.generateChain(plChainSteps(seed.nounId), seed.toSlots()).map { it.toExercise() }
}

private fun SentenceSeed.toSlots(): Map<String, String> = mapOf("noun" to nounId, "adjective" to adjectiveId)

private fun CoreExercise.toExercise(): Exercise = Exercise(
    id = id, primarySkill = primarySkill, source = source, prompt = prompt, expected = expected,
    accepted = accepted, explanation = explanation, tags = tags,
    nounId = slots.getValue("noun"), adjectiveId = slots.getValue("adjective"),
    possessive = PossessiveId.fromId(slots.getValue("owner")), number = NumberGram.fromId(slots.getValue("number")),
    changes = changes.map { FormChange(it.from, it.to, it.reason) },
)
