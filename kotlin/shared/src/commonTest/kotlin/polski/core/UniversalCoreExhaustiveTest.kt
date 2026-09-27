package polski.core

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.data.courseSentenceSeeds
import polski.model.SentenceSeed
import polski.training.plCurriculum

/**
 * UniversalCorePlan.md §5.3/§12 UC-07 acceptance: "for every skill × seed × RNG draw the generic
 * generator produces exercises byte-identical to the current ExerciseFactory". [UniversalCoreParityTest]
 * (generated from `tests/fixtures/core-golden`) covers the pinned representative cases; this covers
 * the full cross product — all 16 [plCurriculum] skills × all 12 [courseSentenceSeeds] × several
 * RNG draws (including draws that force the candidate-filter fallback path) — comparing the live
 * `ExerciseFactory` to the live `ExerciseGenerator` for each one.
 */
class UniversalCoreExhaustiveTest {
    private val draws = listOf(0.0, 0.01, 0.15, 0.33, 0.5, 0.67, 0.82, 0.999999)

    @Test fun everySkillEverySeedEveryDrawMatchesTheLiveFactoryByteForByte() {
        var checked = 0
        for (skill in plCurriculum) {
            for (seed in courseSentenceSeeds) {
                for (draw in draws) {
                    val legacy = legacyFactory(listOf(draw)).generateForSkill(skill.id, SentenceSeed(seed.nounId, seed.adjectiveId))
                    val actual = uc07Generator(listOf(draw))
                        .generateForSkill(skill.id, mapOf("noun" to seed.nounId, "adjective" to seed.adjectiveId))
                        .toLegacyExercise()
                    assertEquals(legacy, actual, "${skill.id} / ${seed.nounId}+${seed.adjectiveId} / draw=$draw")
                    checked += 1
                }
            }
        }
        assertEquals(16 * 12 * draws.size, checked)
    }

    @Test fun everySeedsFullChainMatchesTheLiveFactoryByteForByte() {
        for (seed in courseSentenceSeeds) {
            val legacySeed = SentenceSeed(seed.nounId, seed.adjectiveId)
            val newSeed = mapOf("noun" to seed.nounId, "adjective" to seed.adjectiveId)
            for (draw in listOf(0.1, 0.4, 0.9)) {
                val legacy = legacyFactory(listOf(draw)).generateChain(legacySeed)
                val actual = uc07Generator(listOf(draw)).generateChain(uc07ChainSteps(seed.nounId), newSeed)
                assertEquals(legacy.size, actual.size, seed.nounId)
                for (i in legacy.indices) assertEquals(legacy[i], actual[i].toLegacyExercise(), "${seed.nounId}[$i]/draw=$draw")
            }
        }
    }
}
