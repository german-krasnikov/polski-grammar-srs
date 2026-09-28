package polski.core

import polski.core.engine.ConstructionRealizer
import polski.core.engine.LexemeFeatures
import polski.core.engine.TableMorphology
import polski.core.model.FeatureBundle
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * EnRuPackPlan.md task EN-13: manually runs `lang/en/realization.json`'s real, embedded content
 * (parsed by the same [parseConstructionTemplates] `PlEngine.kt` uses for pl, EnRuPackPlan.md §5
 * gaps A/D) through the real [ConstructionRealizer], with a hand-authored [TableMorphology]
 * fixture standing in for en's own lexicon/prepositions.json values (no generated
 * `forms.generated.json` exists for en yet — gap G/EN-04..EN-10, out of this task's scope) —
 * covering every one of the 16 `lang/en/curriculum.json` skills' construction+feature-bundle
 * combinations and asserting grammatically correct English output.
 */
class EnRealizationTest {
    private fun bundle(vararg pairs: Pair<String, String>): FeatureBundle = pairs.associate { (k, v) -> FeatureKey(k) to FeatureValue(v) }
    private fun bundleFrom(spec: String): FeatureBundle =
        if (spec.isEmpty()) emptyMap() else spec.split(",").associate { pair -> val (k, v) = pair.split("="); FeatureKey(k) to FeatureValue(v) }

    private val morphology = TableMorphology(
        mapOf(
            "noun:house" to mapOf(bundle("Number" to "sg") to "house", bundle("Number" to "pl") to "houses"),
            "adjective:beautiful" to mapOf(bundleFrom("") to "beautiful"),
            "possessive:my" to mapOf(bundleFrom("") to "my"),
            "pronoun:you" to mapOf(bundleFrom("") to "you"),
            "neg:not" to mapOf(bundleFrom("") to "not"),
            "prep:role" to mapOf(
                bundle("Case" to "Subj") to "", bundle("Case" to "Obj") to "",
                bundle("Case" to "In") to "in", bundle("Case" to "With") to "with",
                bundle("Case" to "To") to "to", bundle("Case" to "About") to "about", bundle("Case" to "Of") to "of",
            ),
            "verb:see" to mapOf(
                bundleFrom("") to "see",
                bundle("Tense" to "Pres", "Person" to "1", "Number" to "sg") to "see",
                bundle("Tense" to "Pres", "Person" to "3", "Number" to "sg") to "sees",
                bundle("Tense" to "Past") to "saw",
                bundle("Tense" to "Fut") to "see",
                bundle("Aspect" to "Continuous") to "seeing",
            ),
            "aux:do" to mapOf(
                bundle("Tense" to "Pres", "Person" to "1", "Number" to "sg") to "do",
                bundle("Tense" to "Pres", "Person" to "2", "Number" to "sg") to "do",
                bundle("Tense" to "Pres", "Person" to "3", "Number" to "sg") to "does",
                bundle("Tense" to "Past") to "did",
            ),
            "aux:be" to mapOf(bundle("Tense" to "Pres", "Person" to "3", "Number" to "sg") to "is"),
            "aux:will" to mapOf(bundle("Tense" to "Fut", "Person" to "3", "Number" to "sg") to "will"),
        ),
    )

    private val templates = parseConstructionTemplates(generatedRealizationJsonByLang.getValue("en"))
    private val realizer = ConstructionRealizer(templates, morphology, LexemeFeatures { emptyMap() })

    // en:role.object / role.location / role.instrument / role.recipient (core.argument.case-role):
    // Case selects whether `prep` is even supplied — Subj/Obj carry the role by word order alone.
    @Test fun caseRolePhraseHasNoPrepositionForSubjectOrObject() {
        val subj = realizer.realize("core.argument.case-role", bundle("Number" to "sg", "Case" to "Subj"), mapOf("owner" to "my", "adjective" to "beautiful", "noun" to "house"))
        assertEquals("my beautiful house", subj.text)
        val obj = realizer.realize("core.argument.case-role", bundle("Number" to "sg", "Case" to "Obj"), mapOf("owner" to "my", "adjective" to "beautiful", "noun" to "house"))
        assertEquals("my beautiful house", obj.text)
    }

    @Test fun caseRolePhrasePrependsTheRolesPrepositionForNonCoreRoles() {
        fun phraseFor(case: String) = realizer.realize(
            "core.argument.case-role", bundle("Number" to "sg", "Case" to case),
            mapOf("prep" to "role", "owner" to "my", "adjective" to "beautiful", "noun" to "house"),
        ).text
        assertEquals("in my beautiful house", phraseFor("In"))
        assertEquals("with my beautiful house", phraseFor("With"))
        assertEquals("to my beautiful house", phraseFor("To"))
        assertEquals("about my beautiful house", phraseFor("About"))
    }

    // en:possessive.my (core.agreement.possessive): owner is the drilled focus, always present.
    @Test fun possessiveConstructionAlwaysRealizesTheOwner() {
        val realized = realizer.realize("core.agreement.possessive", bundle("Number" to "sg"), mapOf("owner" to "my", "adjective" to "beautiful", "noun" to "house"))
        assertEquals("my beautiful house", realized.text)
    }

    // en:number.plural (core.number): only the noun's Number-driven form changes.
    @Test fun numberConstructionPluralizesOnlyTheNoun() {
        val singular = realizer.realize("core.number", bundle("Number" to "sg"), mapOf("owner" to "my", "adjective" to "beautiful", "noun" to "house"))
        assertEquals("my beautiful house", singular.text)
        val plural = realizer.realize("core.number", bundle("Number" to "pl"), mapOf("owner" to "my", "adjective" to "beautiful", "noun" to "house"))
        assertEquals("my beautiful houses", plural.text)
    }

    // en:verb.presentSimple / verb.pastSimple (core.verb.tense, Aspect=Simple): person-3 present
    // takes -s; past is invariant and takes no `aux`.
    @Test fun verbTenseSimpleAspectConjugatesPresentAndCollapsesPast() {
        val present = realizer.realize("core.verb.tense", bundle("Tense" to "Pres", "Person" to "3", "Number" to "sg", "Aspect" to "Simple"), mapOf("verb" to "see"))
        assertEquals("sees", present.text)
        val past = realizer.realize("core.verb.tense", bundle("Tense" to "Past", "Person" to "3", "Number" to "sg", "Aspect" to "Simple"), mapOf("verb" to "see"))
        assertEquals("saw", past.text)
    }

    // en:verb.futureSimple (core.verb.tense, Tense=Fut): periphrastic "will" + bare verb.
    @Test fun verbTenseFutureUsesWillPlusTheBareVerb() {
        val future = realizer.realize("core.verb.tense", bundle("Tense" to "Fut", "Person" to "3", "Number" to "sg", "Aspect" to "Simple"), mapOf("aux" to "will", "verb" to "see"))
        assertEquals("will see", future.text)
    }

    // en:verb.presentContinuous (core.verb.tense, Aspect=Continuous): "be" + -ing.
    @Test fun verbTenseContinuousUsesBePlusIngForm() {
        val continuous = realizer.realize("core.verb.tense", bundle("Tense" to "Pres", "Person" to "3", "Number" to "sg", "Aspect" to "Continuous"), mapOf("aux" to "be", "verb" to "see"))
        assertEquals("is seeing", continuous.text)
    }

    // en:polarity.present / polarity.past (core.sentence.polarity): do-support inserts `aux`+`neg`
    // and reverts the verb to its bare form only when Polarity=Neg.
    @Test fun polarityRealizesFullyConjugatedVerbWhenPositive() {
        val positivePresent = realizer.realize("core.sentence.polarity", bundle("Tense" to "Pres", "Person" to "1", "Number" to "sg", "Polarity" to "Pos"), mapOf("verb" to "see"))
        assertEquals("see", positivePresent.text)
        val positivePast = realizer.realize("core.sentence.polarity", bundle("Tense" to "Past", "Person" to "1", "Number" to "sg", "Polarity" to "Pos"), mapOf("verb" to "see"))
        assertEquals("saw", positivePast.text)
    }

    @Test fun polarityInsertsDoSupportAndBareVerbWhenNegative() {
        val negativePresent = realizer.realize(
            "core.sentence.polarity", bundle("Tense" to "Pres", "Person" to "1", "Number" to "sg", "Polarity" to "Neg"),
            mapOf("aux" to "do", "neg" to "not", "verb" to "see"),
        )
        assertEquals("do not see", negativePresent.text)
        val negativePast = realizer.realize(
            "core.sentence.polarity", bundle("Tense" to "Past", "Person" to "1", "Number" to "sg", "Polarity" to "Neg"),
            mapOf("aux" to "do", "neg" to "not", "verb" to "see"),
        )
        assertEquals("did not see", negativePast.text)
    }

    // en:mood.question (core.sentence.mood, EnRuPackPlan.md §5 gap D): declarative keeps `aux`
    // absent and subject-first; YesNoQ's orderWhen inserts `aux` before the subject.
    @Test fun moodDeclarativeHasNoAuxiliaryAndKeepsSubjectVerbOrder() {
        val declarative = realizer.realize("core.sentence.mood", bundle("Tense" to "Pres", "Person" to "2", "Number" to "sg", "Mood" to "Decl"), mapOf("subject" to "you", "verb" to "see"))
        assertEquals("you see", declarative.text)
    }

    @Test fun moodYesNoQuestionMovesTheAuxiliaryBeforeTheSubject() {
        val question = realizer.realize(
            "core.sentence.mood", bundle("Tense" to "Pres", "Person" to "2", "Number" to "sg", "Mood" to "YesNoQ"),
            mapOf("subject" to "you", "aux" to "do", "verb" to "see"),
        )
        assertEquals("do you see", question.text)
        assertEquals(0 until 2, question.slotSpans.getValue("aux"))
        assertEquals(3 until 6, question.slotSpans.getValue("subject"))
        assertEquals(7 until 10, question.slotSpans.getValue("verb"))
    }
}
