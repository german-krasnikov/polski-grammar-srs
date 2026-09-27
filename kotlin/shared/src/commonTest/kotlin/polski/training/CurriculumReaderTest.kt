package polski.training

import polski.core.model.FeatureFocus
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.core.model.LexicalFilter
import polski.data.skillById
import polski.model.Gender
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * UniversalCorePlan.md §12 UC-06: `lang/pl/curriculum.json`'s 16 [polski.core.model.SkillSpec]
 * entries must carry the same `focus`/`fixed`/`lexicalFilter` as `ExerciseFactory.kt`'s
 * `when(skillId)` branches (:49-136) they are derived from — ExerciseFactory itself stays the
 * live path; this only proves the data is a faithful, checkable copy of what those branches do,
 * so a future generic engine (UC-07) can be verified against it instead of against prose.
 *
 * `level`/`prerequisites` are cross-checked against the shipped pack ([skillById]) rather than a
 * second hardcoded copy, so a `course.json` edit cannot silently drift from this file unnoticed.
 * The gender/nounId filter sets are cross-checked against real enum ids ([Gender]) or pinned
 * against the literal `setOf(...)` in `ExerciseFactory.kt:54` where no such enum exists.
 */
class CurriculumReaderTest {
    private val curriculum = plCurriculum
    private val byId = curriculum.associateBy { it.id }
    private val filteredNounIds = listOf("wife", "husband", "friendM", "son", "child") // ExerciseFactory.kt:54

    @Test fun coversAllSixteenPolishSkillIdsExactlyOnce() {
        assertEquals(polishSkillIds, curriculum.map { it.id }.toSet())
        assertEquals(polishSkillIds.size, curriculum.size)
    }

    @Test fun levelAndPrerequisitesMatchTheShippedPack() {
        for (spec in curriculum) {
            val skill = skillById(spec.id)
            assertEquals(skill.level, spec.level, "level for ${spec.id}")
            assertEquals(skill.prerequisites, spec.prerequisites, "prerequisites for ${spec.id}")
        }
    }

    // ExerciseFactory.kt:50-52,63-69 — Case Nom→Acc, candidates filtered by the noun's gender.
    @Test fun accusativeGenderSkillsFocusOnCaseAndFilterByGender() {
        val expected = mapOf(
            "case.acc.f" to listOf(Gender.F.id),
            "case.acc.n" to listOf(Gender.N.id),
            "case.acc.m" to Gender.entries.filter { it.id.startsWith("m-") }.map { it.id },
        )
        for ((id, genders) in expected) {
            val spec = byId.getValue(id)
            assertEquals(FeatureFocus(FeatureKey("Case"), FeatureValue("Nom"), FeatureValue("Acc")), spec.focus, id)
            assertEquals(LexicalFilter("noun", mapOf("gender" to genders)), spec.lexicalFilter, id)
        }
    }

    // ExerciseFactory.kt:71-72 — no candidate filter (falls to the `else` branch); Polarity Pos→Neg.
    @Test fun genitiveNegationFocusesOnPolarityAndDrawsFromEverySeed() {
        val spec = byId.getValue("case.gen.neg")
        assertEquals(FeatureFocus(FeatureKey("Polarity"), FeatureValue("Pos"), FeatureValue("Neg")), spec.focus)
        assertNull(spec.lexicalFilter)
    }

    // ExerciseFactory.kt:53-54 filters case.inst/case.dat to filteredNounIds; case.loc is NOT in
    // that set and stays unfiltered — a real asymmetry in the code, not an omission here.
    @Test fun governedCaseSkillsFocusOnCaseAccToTargetWithMatchingCandidateFilter() {
        val cases = listOf(
            Triple("case.inst", "Inst", true),
            Triple("case.dat", "Dat", true),
            Triple("case.loc", "Loc", false),
        )
        for ((id, to, filtered) in cases) {
            val spec = byId.getValue(id)
            assertEquals(FeatureFocus(FeatureKey("Case"), FeatureValue("Acc"), FeatureValue(to)), spec.focus, id)
            val expectedFilter = if (filtered) LexicalFilter("noun", mapOf("nounId" to filteredNounIds)) else null
            assertEquals(expectedFilter, spec.lexicalFilter, id)
        }
    }

    // ExerciseFactory.kt:98-102 — fromTense flips relative to Tense.PRESENT; :53 filters all three.
    @Test fun tenseSkillsFocusOnTenseWithTheCodesFromToFlip() {
        val tenses = listOf(
            Triple("verb.present", "Past", "Pres"),
            Triple("verb.past", "Pres", "Past"),
            Triple("verb.future", "Pres", "Fut"),
        )
        for ((id, from, to) in tenses) {
            val spec = byId.getValue(id)
            assertEquals(FeatureFocus(FeatureKey("Tense"), FeatureValue(from), FeatureValue(to)), spec.focus, id)
            assertEquals(LexicalFilter("noun", mapOf("nounId" to filteredNounIds)), spec.lexicalFilter, id)
        }
    }

    // ExerciseFactory.kt:117-121 — pronouns is in the filtered branch (:53) too, but substitutes a
    // pronoun for the noun phrase rather than flipping a single feature value: focus is null.
    @Test fun pronounsIsCandidateFilteredButHasNoSingleFeatureFlip() {
        val spec = byId.getValue("pronouns")
        assertNull(spec.focus)
        assertEquals(LexicalFilter("noun", mapOf("nounId" to filteredNounIds)), spec.lexicalFilter)
    }

    // ExerciseFactory.kt:123-125 — source pattern "Widzisz {acc}." is 2nd person present; Mood Decl→YesNoQ.
    @Test fun questionFocusesOnMoodAtSecondPersonPresent() {
        val spec = byId.getValue("sentence.question")
        assertEquals(FeatureFocus(FeatureKey("Mood"), FeatureValue("Decl"), FeatureValue("YesNoQ")), spec.focus)
        assertEquals(FeatureValue("2"), spec.fixed[FeatureKey("Person")])
        assertNull(spec.lexicalFilter)
    }

    // ExerciseFactory.kt:126-131 — Number Sing→Plur, Case stays Acc (`phrase(seed, GramCase.ACC, ...)`).
    @Test fun pluralFocusesOnNumberWithCaseHeldFixedAtAcc() {
        val spec = byId.getValue("sentence.plural")
        assertEquals(FeatureFocus(FeatureKey("Number"), FeatureValue("Sing"), FeatureValue("Plur")), spec.focus)
        assertEquals(FeatureValue("Acc"), spec.fixed[FeatureKey("Case")])
        assertNull(spec.lexicalFilter)
    }

    // ExerciseFactory.kt:88-97 (random owner among 6), :113-116 (hardcoded copy pair, two axes at
    // once), :132-136 (composite of two FormChanges): none is a single feature-value flip.
    @Test fun skillsWithoutASingleFeatureFlipHaveNoFocus() {
        for (id in listOf("agreement.my", "aspect", "mixed")) {
            assertNull(byId.getValue(id).focus, id)
            assertNull(byId.getValue(id).lexicalFilter, id)
        }
    }

    @Test fun everyLexicalFilterCandidateListIsNonEmpty() {
        for (spec in curriculum) {
            spec.lexicalFilter?.where?.values?.forEach { values -> assertTrue(values.isNotEmpty(), "${spec.id}: empty lexicalFilter value list") }
        }
    }
}

private val polishSkillIds = setOf(
    "case.acc.n", "case.acc.f", "case.acc.m", "case.gen.neg", "case.inst", "case.loc", "case.dat",
    "agreement.my", "verb.present", "verb.past", "verb.future", "aspect", "pronouns", "mixed",
    "sentence.question", "sentence.plural",
)
