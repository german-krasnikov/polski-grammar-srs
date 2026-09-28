package polski.core

import polski.core.engine.CasePrefix
import polski.core.engine.ConstructionRealizer
import polski.core.engine.ExerciseGenerator
import polski.core.engine.ExerciseIdFactory
import polski.core.engine.LexemeFeatures
import polski.core.engine.PackCopy
import polski.core.engine.PackPattern
import polski.core.engine.PronounForms
import polski.core.engine.RandomSource
import polski.core.engine.TableMorphology
import polski.core.engine.TextCase
import polski.core.model.CoreExercise
import polski.core.model.FeatureBundle
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.data.generatedCurriculumJsonByLang
import polski.model.Exercise
import polski.model.NumberGram
import polski.model.PossessiveId
import polski.presentation.ChangeSide
import polski.presentation.EndingPart
import polski.presentation.changeHighlightParts
import polski.presentation.sentenceHighlightParts
import polski.training.evaluate
import polski.training.parseCurriculum
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * EnRuPackPlan.md task EN-23: the emphasis contract (ContrastHighlightPlan.md §1/§7,
 * `EndingHighlightTest`'s own convention) manually verified against the *real*, embedded
 * `lang/en/exercise-recipes.json` (EN-14) run through the real [ExerciseGenerator] — not a
 * synthetic fixture. Three gap-A/D-specific cases the plan names explicitly (§2.3): do-support
 * insertion (`do`/`does`/`did`/`not`) must highlight only the inserted word(s), never render as an
 * `ending`; an irregular verb (`see`→`saw`, EN-17's own deliberate choice over the regular
 * `walk`→`walked`) must highlight as a `whole` change, never an `ending`; a contraction
 * (`don't`/`didn't`) must be a typed-answer `accepted` alternative only — [AnswerEvaluator.evaluate]
 * must accept it — and must never appear anywhere a [changes]-driven diff is rendered
 * ([sentenceHighlightParts]/[changeHighlightParts] only ever consume `exercise.changes`,
 * `polski.training.AnswerEvaluator.evaluate` only ever consumes `exercise.accepted`/`expected` —
 * the two are structurally disjoint call sites, verified here on real en-ru data rather than
 * asserted from reading the source).
 */
class EnEmphasisContractTest {
    private class FixedDraws(private val values: List<Double>) : RandomSource {
        private var index = 0
        override fun nextDouble(): Double = values[(index++).coerceAtMost(values.lastIndex)]
    }

    private fun bundle(vararg pairs: Pair<String, String>): FeatureBundle = pairs.associate { (k, v) -> FeatureKey(k) to FeatureValue(v) }
    private val empty: FeatureBundle = emptyMap()

    private val seeds = listOf(mapOf("noun" to "house", "adjective" to "beautiful"))

    // Same fixture convention as EnExerciseGeneratorTest/EnRealizationTest (EN-13/EN-14):
    // hand-transcribed from the real lang/en/lexicon.json + prepositions.json, no
    // forms.generated.json exists for en yet (documented gap, out of this task's scope).
    private val morphology = TableMorphology(
        mapOf(
            "noun:house" to mapOf(bundle("Number" to "sg") to "house", bundle("Number" to "pl") to "houses"),
            "adjective:beautiful" to mapOf(empty to "beautiful"),
            "possessive:my" to mapOf(empty to "my"),
            "possessive:your" to mapOf(empty to "your"),
            "possessive:his" to mapOf(empty to "his"),
            "possessive:her" to mapOf(empty to "her"),
            "possessive:its" to mapOf(empty to "its"),
            "possessive:our" to mapOf(empty to "our"),
            "possessive:their" to mapOf(empty to "their"),
            "pronoun:you" to mapOf(empty to "you"),
            "neg:not" to mapOf(empty to "not"),
            "prep:role" to mapOf(
                bundle("Case" to "Subj") to "", bundle("Case" to "Obj") to "",
                bundle("Case" to "In") to "in", bundle("Case" to "With") to "with", bundle("Case" to "To") to "to",
            ),
            "verb:walk" to mapOf(
                empty to "walk",
                bundle("Tense" to "Past") to "walked",
                bundle("Tense" to "Fut") to "walk",
                bundle("Aspect" to "Continuous") to "walking",
                bundle("Tense" to "Pres", "Person" to "3", "Number" to "sg") to "walks",
                bundle("Tense" to "Pres", "Person" to "1", "Number" to "sg") to "walk",
            ),
            "aux:do" to mapOf(
                bundle("Tense" to "Pres", "Person" to "1", "Number" to "sg") to "do",
                bundle("Tense" to "Past") to "did",
                bundle("Tense" to "Pres", "Person" to "2", "Number" to "sg") to "do",
            ),
            "aux:will" to mapOf(bundle("Tense" to "Fut", "Person" to "3", "Number" to "sg") to "will"),
            "aux:be" to mapOf(bundle("Tense" to "Pres", "Person" to "3", "Number" to "sg") to "is"),
        ),
    )

    private val lexemeFeatures = LexemeFeatures { emptyMap() }
    private val templates = parseConstructionTemplates(generatedRealizationJsonByLang.getValue("en"))
    private val recipeSet = parseRecipes(generatedExerciseRecipesJsonByLang.getValue("en"))
    private val skills = parseCurriculum(generatedCurriculumJsonByLang.getValue("en")).associateBy { it.id }
    private val realizer = ConstructionRealizer(templates, morphology, lexemeFeatures)

    // Real pairs/en-ru/pair.json `exercisePatterns` (EN-17), transcribed verbatim — the actual
    // English surface text these skills show a learner, not a placeholder.
    private val patterns = mapOf(
        "seenObj" to "I see {obj}.",
        "verbSentence" to "{subj} {verb}.",
        "subjectVerb" to "I {verb}.",
        "roleDrill" to "{start} {target}.",
        "chainPast" to "I saw {obj}.",
        "chainNeg" to "I did not see {obj}.",
    )

    private fun generator(): ExerciseGenerator = ExerciseGenerator(
        realizer = realizer,
        morphology = morphology,
        lexemeFeatures = lexemeFeatures,
        skills = skills,
        recipes = recipeSet.skills,
        seeds = seeds,
        random = FixedDraws(listOf(0.0)),
        ids = ExerciseIdFactory { "test-id" },
        // Real pairs/en-ru/pair.json exerciseCopy for the two keys this test's assertions touch
        // (EN-17: mixedVerbFrom/mixedVerbTo — "see"→"saw", the deliberately irregular verb);
        // every other key stays a placeholder, matching EnExerciseGeneratorTest's convention.
        copy = PackCopy { key ->
            when (key) {
                "mixedVerbFrom" -> "see"
                "mixedVerbTo" -> "saw"
                else -> "[$key]"
            }
        },
        pattern = PackPattern { key, values ->
            val template = patterns[key] ?: error("EnEmphasisContractTest fixture: no exercisePattern for \"$key\"")
            values.entries.fold(template) { text, (slot, value) -> text.replace("{$slot}", value) }
        },
        casePrefix = CasePrefix { caseId, numberId ->
            when (caseId) {
                "Subj" -> if (numberId == "pl") "Here are" else "Here is"
                else -> error("EnEmphasisContractTest fixture: no case prefix for \"$caseId\"")
            }
        },
        pronouns = PronounForms { key, caseId ->
            if (key == "it" && caseId == "Obj") "it" else error("EnEmphasisContractTest fixture: no pronoun form for \"$key\"/\"$caseId\"")
        },
        textCase = TextCase { value -> value.replaceFirstChar { it.uppercaseChar() } },
        defaultOwnerLexeme = recipeSet.defaultOwnerLexeme,
        verbLexeme = recipeSet.verbLexeme,
        constantSlots = recipeSet.constantSlots,
    )

    private fun generate(skillId: String): CoreExercise = generator().generateForSkill(skillId, preferredSeed = seeds.first())

    // --- Case 1: do-support insertion (en:mood.question) must highlight only "Do"/"do", never
    // treat the shared "you"/"walk" words as changed and never call any part of it an `ending`. ---
    @Test
    fun moodQuestionDoSupportHighlightsOnlyTheInsertedAuxiliary() {
        val exercise = generate("en:mood.question")
        assertEquals("You walk.", exercise.source)
        assertEquals("Do you walk?", exercise.expected)
        assertEquals(1, exercise.changes.size)

        val before = sentenceHighlightParts(exercise.source, exercise.changes.map { polski.model.FormChange(it.from, it.to, it.reason) }, ChangeSide.Before)
        val after = sentenceHighlightParts(exercise.expected, exercise.changes.map { polski.model.FormChange(it.from, it.to, it.reason) }, ChangeSide.After)

        assertEquals("You walk.", before.joinToString("") { it.text })
        assertEquals("Do you walk?", after.joinToString("") { it.text })
        assertEquals(emptyList(), before.filter(EndingPart::isChanged), "declarative source must show no change before reveal")
        assertEquals(listOf("Do"), after.filter(EndingPart::isChanged).map(EndingPart::text))
        assertEquals(emptyList(), after.filter(EndingPart::isEnding), "do-support is a whole inserted word, never a suffix")
    }

    // --- Case 2: irregular verb (en:mixed's own "see"→"saw", EN-17) is a suppletive whole-word
    // change, exactly like pl's "robić"→"zrobić" (EndingHighlightTest), never an `ending`. ---
    @Test
    fun mixedSkillIrregularVerbHighlightsAsAWholeWordNeverAnEnding() {
        val exercise = generate("en:mixed")
        val verbChange = exercise.changes.first()
        assertEquals("see", verbChange.from)
        assertEquals("saw", verbChange.to)

        val before = changeHighlightParts(verbChange.from, verbChange.to, ChangeSide.Before)
        val after = changeHighlightParts(verbChange.from, verbChange.to, ChangeSide.After)
        assertEquals(listOf("see"), before.filter(EndingPart::isChanged).map(EndingPart::text))
        assertEquals(listOf("saw"), after.filter(EndingPart::isChanged).map(EndingPart::text))
        assertEquals(emptyList(), before.filter(EndingPart::isEnding))
        assertEquals(emptyList(), after.filter(EndingPart::isEnding))
    }

    // --- Case 3: a regular ending change (en:verb.presentSimple, Tense Past→Pres) still highlights
    // only the changed suffix, confirming EN-02's generalized `lexicalSlotsFor` did not regress the
    // plain-ending path any English skill also needs. ---
    @Test
    fun verbPresentSimpleHighlightsOnlyTheChangedSuffix() {
        val exercise = generate("en:verb.presentSimple")
        val verbChange = exercise.changes.first()
        assertEquals("walked", verbChange.from)
        assertEquals("walks", verbChange.to)
        val after = changeHighlightParts(verbChange.from, verbChange.to, ChangeSide.After)
        assertEquals(listOf("s"), after.filter(EndingPart::isEnding).map(EndingPart::text))
    }

    // --- Case 4: the do-support contraction is a typed-answer `accepted` alternative, never a
    // `FormChange` — structurally verified two ways: (a) the real data declares it; (b) the real,
    // shared grading path (`polski.training.evaluate`, used unchanged for every pack) accepts it. ---
    @Test
    fun negationContractionIsAnAcceptedTypedAlternativeNeverAFormChange() {
        val present = generate("en:polarity.present")
        assertEquals("I do not walk.", present.expected)
        assertEquals(listOf("I don't walk."), present.accepted)
        assertFalse(present.changes.any { it.from.contains("don't") || it.to.contains("don't") })
        assertTrue(evaluate("I don't walk.", present.asExercise()).correct)
        assertTrue(evaluate("i don't walk", present.asExercise()).correct, "grading ignores case/trailing punctuation like every other pack")

        val past = generate("en:polarity.past")
        assertEquals("I did not walk.", past.expected)
        assertEquals(listOf("I didn't walk."), past.accepted)
        assertFalse(past.changes.any { it.from.contains("didn't") || it.to.contains("didn't") })
        assertTrue(evaluate("I didn't walk.", past.asExercise()).correct)
    }

    /** Minimal, field-accurate lift into the legacy `polski.model.Exercise` shape `evaluate()`
     *  takes — `PossessiveId`/`NumberGram`'s own wire ids ("my"/"sg") are already English-language
     *  words shared by both packs (`CourseData.kt`), so no invented value is needed for en. */
    private fun CoreExercise.asExercise(): Exercise = Exercise(
        id = id, primarySkill = primarySkill, source = source, prompt = prompt, expected = expected,
        accepted = accepted, explanation = explanation, tags = tags,
        nounId = slots["noun"] ?: "house", adjectiveId = slots["adjective"] ?: "beautiful",
        possessive = PossessiveId.MY, number = NumberGram.SG,
        changes = changes.map { polski.model.FormChange(it.from, it.to, it.reason) },
    )

    @Test
    fun sampleOutputsForAllGapADCasesEnEvidence() {
        println("=== EN-23 emphasis-contract evidence (real en-ru data, live ExerciseGenerator) ===")
        listOf("en:mood.question", "en:polarity.present", "en:polarity.past", "en:mixed", "en:verb.presentSimple").forEach { id ->
            val e = generate(id)
            println("$id\n  source:   ${e.source}\n  expected: ${e.expected}\n  accepted: ${e.accepted}\n  changes:  ${e.changes}")
        }
    }
}
