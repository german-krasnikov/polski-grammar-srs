package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import polski.presentation.LifehackStatus

/**
 * R1.18: the real lifehacks of both packs, from the rows of courses-biome/<pair>/lifehacks.core-data (en-ru through the generated copy of the pl-ru mapper): what the old TS tests lifehacks-pl-ru and
 * lifehacks-en-ru checked of the files, now on the typed values every host reads. That a skill exists and an id is not used twice is `biome data check`; the rows against the old JSON are the vitest test course-lifehacks.
 */
class LifehackDataTest {
    // (pair, the pack, the least number of tips each skill has: pl-ru's EN-20 follow-up wrote two for each of its 16 skills, en-ru's EN-19 at least one)
    private val packs = listOf(Triple("pl-ru", PlRuTypedPack, 2), Triple("en-ru", EnRuTypedPack, 1))

    @Test fun eachPackHas32TipsWithTheIdsOfItsOwnPairUsedOnce() {
        for ((pair, pack, _) in packs) {
            val ids = pack.lifehacks.map { it.id }
            assertEquals(if (pair == "pl-ru") 33 else 32, ids.size, pair)
            assertEquals(ids.size, ids.toSet().size, pair)
            assertTrue(ids.all { it.startsWith("$pair.") }, pair)
        }
    }

    @Test fun everyTipIsForASkillOfItsOwnPackAndEverySkillHasTheLeastNumberOfTips() {
        for ((pair, pack, least) in packs) {
            val skillIds = pack.skills.map { it.id }
            assertEquals(if (pair == "pl-ru") 17 else 16, skillIds.size, pair)
            val counts = pack.lifehacks.groupingBy { it.skillId }.eachCount()
            assertEquals(skillIds.toSet(), counts.keys, pair)
            for (id in skillIds) {
                val minimum = if (id == "degree.comparison") 1 else least
                assertTrue(counts.getValue(id) >= minimum, "$pair $id has ${counts[id]} tips, wanted $minimum")
            }
            assertTrue(pack.lifehacks.all { it.topic == null }, pair)
        }
    }

    @Test fun everyCitationIsMoreThanTenCharactersAndEveryTipIsEditorialSinceTheVotingStatusIsReserved() {
        for ((pair, pack, _) in packs) for (tip in pack.lifehacks) {
            assertTrue(tip.source.citation.trim().length > 10, "$pair ${tip.id}: ${tip.source.citation}")
            assertEquals(LifehackStatus.Editorial, tip.status, tip.id)
        }
    }
}
