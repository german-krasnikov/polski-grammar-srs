package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import polski.presentation.ChangeSide

class CourseRussianSupportTest {
    @Test
    fun authoredSupportKeepsFourOrderedHostVariants() {
        val support = referenceRussianSupport
        assertEquals("Опора на русский: что переносится, а что проверить", support.fullTitle)
        assertEquals("Опора на русский", support.compactTitle)
        assertEquals(listOf("Русская опора", "Польская конструкция", "Проверка"), support.columns)
        assertEquals(listOf("accusative", "instrumental", "locative", "possessive"), support.rows.map { it.id })
        assertEquals(listOf("вижу кого? что?", "с моей женой", "говорю о жене", "мой / его / их"), support.rows.map { it.cue })
        assertEquals(listOf(
            RussianSupportTable("Widzę moją żonę.", "Логика винительного знакома; польские окончания нужно менять во всей группе."),
            RussianSupportTable("z moją żoną", "Польское женское -ą соответствует здесь творительному; это же окончание есть у прилагательного в Bierniku."),
            RussianSupportTable("mówię o żonie", "Местный падеж требует предлога; żona → żonie."),
            RussianSupportTable("moją żonę / jego żonę / ich żonę", "jego, jej, ich не склоняются. Формы mojego и mojej зависят от предмета обладания."),
        ), support.rows.map { it.react })
        assertEquals(listOf(
            RussianSupportTable("Widzę moją żonę.", "Польские окончания меняются во всей группе."),
            RussianSupportTable("z moją żoną", "Женское -ą здесь соответствует творительному."),
            RussianSupportTable("mówię o żonie", "Местный падеж требует предлога; żona → żonie."),
            RussianSupportTable("moją żonę / jego żonę / ich żonę", "jego, jej, ich не склоняются."),
        ), support.rows.map { it.web })
        assertEquals(listOf(
            RussianSupportTable("Widzę moją żonę.", "Окончания меняются во всей группе"),
            RussianSupportTable("z moją żoną", "Женское -ą — творительный"),
            RussianSupportTable("mówię o żonie", "Местный требует предлога"),
            RussianSupportTable("moją / jego / ich żonę", "jego, jej, ich не склоняются"),
        ), support.rows.map { it.desktop })
        assertEquals(listOf(
            "вижу кого? что? → Widzę moją żonę. Окончания меняются во всей группе.",
            "с моей женой → z moją żoną. Женское -ą — творительный.",
            "говорю о жене → mówię o żonie. Местный требует предлога.",
            "мой / его / их → moją / jego / ich żonę. jego, jej, ich не склоняются.",
        ), support.rows.map { it.mobileLine })
        assertEquals(listOf(
            listOf("moja żona" to "moją żonę"),
            listOf("moja żona" to "moją żoną"),
            listOf("żona" to "żonie"),
            listOf("moją żonę" to "jego żonę", "moją żonę" to "ich żonę"),
        ), support.rows.map { row -> row.comparisons.map { it.from to it.to } })
        support.rows.flatMap { it.comparisons }.forEach { pair ->
            assertEquals(pair.from, pair.parts(ChangeSide.Before).joinToString("") { it.text })
            assertEquals(pair.to, pair.parts(ChangeSide.After).joinToString("") { it.text })
            assertTrue(pair.beforeParts.any { it.isChanged })
            assertTrue(pair.afterParts.any { it.isChanged })
        }
    }
}
