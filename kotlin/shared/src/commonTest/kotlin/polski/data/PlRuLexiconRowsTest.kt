package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import polski.data.rows.plru.AdjectiveFormsRow
import polski.data.rows.plru.AdjectiveRow
import polski.data.rows.plru.CaseFormsRow
import polski.data.rows.plru.GenderFormsRow
import polski.data.rows.plru.NounFormsRow
import polski.data.rows.plru.NounRow
import polski.data.rows.plru.PastStemRow
import polski.data.rows.plru.PersonFormsRow
import polski.data.rows.plru.PresentRow
import polski.data.rows.plru.VerbRow
import polski.data.rows.plru.VerbRowAspect
import polski.data.rows.plru.VerbRowFutureType
import polski.model.Aspect
import polski.model.FutureType
import polski.model.Gender
import polski.model.GramCase
import polski.model.NumberGram
import polski.model.Person

/** R1.5: the hand-written part of the lexicon path, on hand-built rows with a different text in every slot, so a swapped pair is a failed assert. */
class PlRuLexiconRowsTest {
    private fun cases(tag: String) = CaseFormsRow("$tag-nom", "$tag-gen", "$tag-dat", "$tag-acc", "$tag-inst", "$tag-loc", "$tag-voc")
    private val sevenCases = GramCase.entries.map { it.id }

    @Test fun aNounKeepsItsGenderAndTheSevenCasesOfBothNumbersInOrder() {
        val noun = NounRow("n", "l", "m", "m-personal", NounFormsRow(cases("sg"), cases("pl"))).toNoun()
        assertEquals(Gender.M_PERSONAL, noun.gender)
        assertEquals(listOf(NumberGram.SG, NumberGram.PL), noun.forms.keys.toList())
        assertEquals(sevenCases.map { "sg-$it" }, noun.forms.getValue(NumberGram.SG).values.toList())
        assertEquals(sevenCases.map { "pl-$it" }, noun.forms.getValue(NumberGram.PL).values.toList())
        assertEquals(GramCase.entries, noun.forms.getValue(NumberGram.SG).keys.toList())
    }

    @Test fun aGenderThatIsNoIdOfTheModelIsRefused() {
        assertFailsWith<IllegalStateException> { NounRow("n", "l", "m", "m_personal", NounFormsRow(cases("sg"), cases("pl"))).toNoun() }
    }

    @Test fun anAdjectiveHasEveryGenderUnderItsOwnKeyInTheModelsOrder() {
        val genders = GenderFormsRow(cases("mp"), cases("ma"), cases("mi"), cases("f"), cases("n"))
        val forms = AdjectiveRow("a", "l", "m", AdjectiveFormsRow(genders, genders)).toAdjective().forms.getValue(NumberGram.PL)
        assertEquals(listOf(Gender.M_PERSONAL, Gender.M_ANIMATE, Gender.M_INANIMATE, Gender.F, Gender.N), forms.keys.toList())
        assertEquals(listOf("mp", "ma", "mi", "f", "n"), forms.values.map { it.getValue(GramCase.ACC).substringBefore('-') })
    }

    private fun verb(aspect: VerbRowAspect, future: VerbRowFutureType, pair: String? = null) = VerbRow(
        "v", "l", "m", aspect, future, pair, PresentRow(PersonFormsRow("s1", "s2", "s3"), PersonFormsRow("p1", "p2", "p3")),
        PastStemRow("m", "f", "n", "mp", "np"),
    ).toVerb()

    @Test fun aVerbKeepsThePersonsOfBothNumbersThePastStemAndTheOptionalPair() {
        val verb = verb(VerbRowAspect.IMPERFECTIVE, VerbRowFutureType.COMPOUND, "pair")
        assertEquals(listOf("s1", "s2", "s3"), verb.present!!.getValue(NumberGram.SG).values.toList())
        assertEquals(listOf(Person.FIRST, Person.SECOND, Person.THIRD), verb.present.getValue(NumberGram.PL).keys.toList())
        assertEquals(listOf("m", "f", "n", "mp", "np"), verb.pastStem.let { listOf(it.m, it.f, it.n, it.mp, it.np) })
        assertEquals("pair", verb.perfectivePair)
        assertEquals(null, verb(VerbRowAspect.IMPERFECTIVE, VerbRowFutureType.COMPOUND).perfectivePair)
    }

    @Test fun aspectAndFutureTypeMapOneToOne() {
        assertEquals(Aspect.PERFECTIVE to FutureType.PRESENT, verb(VerbRowAspect.PERFECTIVE, VerbRowFutureType.PRESENT).let { it.aspect to it.futureType })
        assertEquals(Aspect.IMPERFECTIVE to FutureType.COMPOUND, verb(VerbRowAspect.IMPERFECTIVE, VerbRowFutureType.COMPOUND).let { it.aspect to it.futureType })
    }
}
