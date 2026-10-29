package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import polski.training.plCurriculum

/**
 * R1.4b: the SkillSpecs of the rows against the `lang/pl/curriculum.json` the engine still parses, and the prerequisite cycle check.
 * R1.13 makes the engine read the rows and removes the JSON path; the parity test goes with it.
 */
class PlRuSkillSpecsTest {
    @Test fun theSpecsOfTheRowsEqualTheShippedCurriculumInOrder() {
        assertEquals(16, generatedPlRuSkillSpecs.size)
        assertEquals(plCurriculum, generatedPlRuSkillSpecs)
    }

    @Test fun aSkillAndItsSpecAreOneRowSoTheyAgreeOnLevelAndPrerequisites() {
        assertEquals(
            generatedPlRuSkills.map { Triple(it.id, it.level, it.prerequisites) },
            generatedPlRuSkillSpecs.map { Triple(it.id, it.level, it.prerequisites) },
        )
    }

    @Test fun aSharedPrerequisiteIsNotACycle() {
        requireAcyclic(mapOf("a" to emptyList(), "b" to listOf("a"), "c" to listOf("a"), "d" to listOf("b", "c")))
    }

    @Test fun aSelfLoopATwoCycleAndACycleBehindAnAcyclicTailAreRefused() {
        val graphs = listOf(
            mapOf("a" to listOf("a")),
            mapOf("a" to listOf("b"), "b" to listOf("a")),
            mapOf("tail" to listOf("a"), "a" to listOf("b"), "b" to listOf("c"), "c" to listOf("a")),
        )
        for (graph in graphs) assertFailsWith<IllegalStateException>("$graph") { requireAcyclic(graph) }
    }

    /** A key that counts the hash-map operations done on it: every get and put of the check hashes its key once. */
    private class Counted(val n: Int, val steps: IntArray) {
        override fun hashCode() = n.also { steps[0]++ }
        override fun equals(other: Any?) = other is Counted && other.n == n
    }

    /** Steps per skill of the check on a graph where each skill needs the three before it. */
    private fun stepsPerSkill(size: Int): Double {
        val steps = IntArray(1)
        val keys = List(size) { Counted(it, steps) }
        val graph = keys.mapIndexed { i, key -> key to keys.subList(maxOf(0, i - 3), i) }.toMap()
        steps[0] = 0
        requireAcyclic(graph)
        return steps[0].toDouble() / size
    }

    @Test fun workGrowsLinearlyWithTheNumberOfSkills() {
        val perSkill = listOf(10, 100, 1000).map(::stepsPerSkill)
        assertTrue(perSkill.last() <= 10, "N=1000: at most 10 steps per skill, got ${perSkill.last()}")
        assertTrue(perSkill.last() <= perSkill[1] * 1.25, "per-skill steps at N=1000 stay within 25% of N=100: $perSkill")
    }
}
