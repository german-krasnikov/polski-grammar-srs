package polski.grammar

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.model.Exercise
import polski.model.NumberGram
import polski.model.PossessiveId
import polski.training.evaluate

class AnswerNormalizerTest {
    @Test
    fun decomposedPolishCharactersComposeAndRemainDistinct() {
        assertEquals("żółć", normalize("  z\u0307o\u0301łc\u0301?!  "))
        assertEquals("zołc", normalize("ZOŁC"))
    }

    @Test
    fun whitespaceAndTerminalPunctuationFollowBaselineOrder() {
        assertEquals("widzę moją żonę", normalize("  WIDZĘ\t MOJĄ\nŻONĘ... "))
        assertEquals("czy widzę? żonę", normalize("Czy widzę? żonę!"))
    }

    @Test
    fun ownerUnicodeWhitespaceAnswersKeepExactEvaluation() {
        val cases = listOf(
            "AG-U01" to "widzę\u00a0żonę",
            "AG-U02" to "\u00a0widzę żonę\u00a0",
            "AG-U03" to "widzę\u2009żonę",
            "AG-U04" to "widzę\u202fżonę",
            "AG-U05" to "widzę\u3000żonę",
            "AG-U06" to "\u3000widzę żonę\u3000",
            "AG-U07" to "\ufeffwidzę żonę",
            "AG-U08" to "widzę żonę\ufeff",
            "AG-U09" to "widzę żonę.\u00a0",
            "AG-W05" to "Widzę\u00a0moją piękną żonę.",
            "AG-W06" to "\u00a0Widzę moją piękną żonę.\u00a0",
            "AG-W07" to "\ufeffWidzę moją piękną żonę.",
        )
        cases.forEach { (id, answer) ->
            val expected = if (id.startsWith("AG-U")) "widzę żonę" else "Widzę moją piękną żonę."
            val normalized = if (id.startsWith("AG-U")) "widzę żonę" else "widzę moją piękną żonę"
            val exercise = Exercise(id, "test", "", "", expected, explanation = "", tags = emptyList(),
                nounId = "wife", adjectiveId = "beautiful", possessive = PossessiveId.fromId("my"),
                number = NumberGram.fromId("sg"), changes = emptyList())
            val actual = evaluate(answer, exercise)
            assertEquals(true, actual.correct, id)
            assertEquals(normalized, actual.normalized, id)
            assertEquals(expected, actual.expected, id)
            assertEquals(0, actual.distance, id)
        }
    }
}
