package polski.grammar

import kotlin.test.Test
import kotlin.test.assertEquals

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
}
