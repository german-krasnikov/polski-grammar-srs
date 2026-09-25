package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import polski.model.GramCase
import polski.model.PossessiveId

class CoursePronounTeachingTest {
    @Test fun personalContextsAndAndroidLocativeUseOnePreposition() {
        val teaching = referencePronounTeaching
        assertEquals(listOf("ja", "ty", "on", "ona", "ono", "my", "wy", "oni", "one"), teaching.pronounIds)
        assertEquals(listOf(GramCase.GEN, GramCase.DAT, GramCase.ACC, GramCase.INST, GramCase.LOC), teaching.contexts.map { it.id })
        val expected = listOf(
            listOf("mnie", "mi", "mnie", "ze mną", "o mnie"),
            listOf("ciebie", "ci", "ciebie", "z tobą", "o tobie"),
            listOf("go", "mu", "go", "z nim", "o nim"),
            listOf("jej", "jej", "ją", "z nią", "o niej"),
            listOf("go", "mu", "je", "z nim", "o nim"),
            listOf("nas", "nam", "nas", "z nami", "o nas"),
            listOf("was", "wam", "was", "z wami", "o was"),
            listOf("ich", "im", "ich", "z nimi", "o nich"),
            listOf("ich", "im", "je", "z nimi", "o nich"),
        )
        assertEquals(expected, teaching.pronounIds.map { id ->
            teaching.contexts.map { it.value(id, personalPronouns.getValue(id)) }
        })
        val locative = teaching.contexts.last()
        teaching.pronounIds.forEach { id ->
            val line = locative.androidLine(id, personalPronouns.getValue(id))
            assertEquals("Mówię o… ${personalPronouns.getValue(id).getValue(GramCase.LOC)}", line)
            assertFalse("o… o " in line)
        }
    }

    @Test fun ownerDemoComputesAllThreePhrasesWithoutStoredPolishTails() {
        val demo = referencePronounTeaching.demo
        assertEquals(listOf(GramCase.NOM, GramCase.ACC, GramCase.GEN), demo.cases.map { it.id })
        val expected = listOf(
            listOf("moja piękna żona", "moją piękną żonę", "mojej pięknej żony"),
            listOf("twoja piękna żona", "twoją piękną żonę", "twojej pięknej żony"),
            listOf("jego piękna żona", "jego piękną żonę", "jego pięknej żony"),
            listOf("jej piękna żona", "jej piękną żonę", "jej pięknej żony"),
            listOf("nasza piękna żona", "naszą piękną żonę", "naszej pięknej żony"),
            listOf("wasza piękna żona", "waszą piękną żonę", "waszej pięknej żony"),
            listOf("ich piękna żona", "ich piękną żonę", "ich pięknej żony"),
        )
        assertEquals(expected, possessives.map { owner -> demo.cases.map { demo.phrase(owner.id, it.id) } })
        assertEquals("Владелец не склоняется", demo.rule(PossessiveId.THEIR))
        assertEquals("Согласуется с żona", demo.rule(PossessiveId.MY))
    }
}
