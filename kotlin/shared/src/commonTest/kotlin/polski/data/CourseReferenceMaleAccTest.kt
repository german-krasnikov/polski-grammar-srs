package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.grammar.nounPhrase
import polski.model.GramCase
import polski.model.NumberGram
import polski.model.PossessiveId

class CourseReferenceMaleAccTest {
    @Test
    fun authoredMaleAccusativeExamplesAgreeWithMorphology() {
        val nouns = listOf("husband", "friendM", "dog", "car")
        val adjectives = listOf("good", "good", "good", "new")
        val examples = maleAccRows.flatMap { it.examples }
        assertEquals(nouns.size, examples.size)
        examples.forEachIndexed { index, example ->
            assertEquals(nounPhrase(nouns[index], GramCase.NOM, NumberGram.SG, adjectives[index], PossessiveId.MY), example.from)
            assertEquals(nounPhrase(nouns[index], GramCase.ACC, NumberGram.SG, adjectives[index], PossessiveId.MY), example.to)
        }
    }
}
