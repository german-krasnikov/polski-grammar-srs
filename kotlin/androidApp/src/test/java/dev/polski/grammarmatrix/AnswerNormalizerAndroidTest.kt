package dev.polski.grammarmatrix

import org.junit.Assert.assertEquals
import org.junit.Test
import polski.grammar.normalizeNfc
import polski.grammar.polishLowercase
import polski.grammar.polishUppercase

class AnswerNormalizerAndroidTest {
    @Test fun preservesPolishDiacriticsAndComposesInput() {
        assertEquals("żółć", normalizeNfc("z\u0307o\u0301łc\u0301"))
        assertEquals("łódź", polishLowercase("ŁÓDŹ"))
        assertEquals("ŻÓŁĆ", polishUppercase("żółć"))
    }
}
