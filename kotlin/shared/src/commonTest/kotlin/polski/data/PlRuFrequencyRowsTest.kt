package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals

/** R1.8a: the pl-ru frequency list against values read off the sealed source (`frequency-top1000.json`, sha256 4549d27b...) before it was transcribed. */
class PlRuFrequencyRowsTest {
    @Test fun theListHasThousandItemsInRankOrder() {
        assertEquals(1000, generatedPlRuFrequency.size)
        assertEquals((1..1000).toList(), generatedPlRuFrequency.map { it.rank })
    }

    @Test fun theFirstAndLastItemsAreTheOnesOfTheSource() {
        assertEquals(listOf(FrequencyItem(1, "w", 31620), FrequencyItem(2, "być", 28380), FrequencyItem(3, "i", 21991)), generatedPlRuFrequency.take(3))
        assertEquals(listOf(FrequencyItem(999, "walczyć", 128), FrequencyItem(1000, "wymienić", 128)), generatedPlRuFrequency.takeLast(2))
    }
}
