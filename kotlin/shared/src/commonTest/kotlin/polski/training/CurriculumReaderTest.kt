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
 * `focus` and `lexicalFilter` are checked against actual branch parameters/conditions for all 16
 * skills. `fixed` is a genuine branch parameter — and checked as one — only for `verb.present`/
 * `verb.past`/`verb.future` (`verbForm(..., Person.THIRD, NumberGram.SG, ...)`, :101-102),
 * `sentence.plural`'s `Case` and `agreement.my`/`pronouns`' `Case` (all three the literal
 * `GramCase.ACC` passed to [ExerciseFactory.phrase] at :127, :93 and — via
 * `personalPronouns[...].getValue(GramCase.ACC)` — :119). For the other 10 non-empty `fixed`
 * entries — `case.acc.*`, `case.gen.neg`, `case.inst`/`case.loc`/`case.dat`, `agreement.my`,
 * `pronouns`, `sentence.question`, `aspect` — `ExerciseFactory`'s branch calls
 * `nounPhrase`/`caseSentence`/`phrase`/`sentence`, none of which take a Tense/Person/Number
 * argument at all, so those axes are not something the branch fixes; the `Tense: Pres, Person: 1,
 * Number: Sing` triple is read off the hardcoded `"seenAcc"` course pattern ("Widzę {acc}.",
 * `course.json:3609`) that those branches render as their source/expected sentence —
 * `sentence.question` instead reads `Person: 2, Tense: Pres, Number: Sing` off its own
 * `"questionSource"` pattern ("Widzisz {acc}.", :123), and `aspect`'s `Person: 1, Number: Sing` off
 * its hardcoded `aspectFrom`/`aspectTo` copy strings ("kupuję"/"kupiłem", `course.json:3574-3575`).
 * [templateInferredFixedSkillsMatchTheHardcodedPatternTheyAreReadFrom] pins that inferred group
 * against the literal patterns instead of leaving it unverified; it is not a code-branch-parameter
 * check, and is not claimed to be one.
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
    // :101-102 also pass Person.THIRD/NumberGram.SG to verbForm literally, so `fixed` is a real
    // branch parameter here (unlike the template-inferred group below).
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
            assertEquals(FeatureValue("3"), spec.fixed[FeatureKey("Person")], id)
            assertEquals(FeatureValue("Sing"), spec.fixed[FeatureKey("Number")], id)
        }
    }

    // course.json:3609,3574-3575 — the "seenAcc" pattern ("Widzę {acc}.") and the `aspect` skill's
    // own hardcoded copy strings are both 1st-person-singular-present Polish, which is what these
    // 9 skills' `fixed` triple is actually read off. None of ExerciseFactory.kt's branches for
    // these skills (case.acc.*/case.gen.neg/case.inst/case.loc/case.dat/agreement.my/pronouns call
    // `nounPhrase`/`caseSentence`/`phrase`; `aspect` returns hardcoded exerciseCopy strings) takes a
    // Tense/Person/Number argument, so this pins the data against the literal template it was read
    // from rather than against a branch parameter that doesn't exist. `agreement.my`/`pronouns`'
    // `Case: Acc` is NOT checked here — ExerciseFactory.kt:93/:119 pass the literal `GramCase.ACC`
    // to `phrase`/`personalPronouns[...].getValue`, so that axis is a real branch parameter and is
    // checked alongside it in skillsWithoutASingleFeatureFlipHaveNoFocus/
    // pronounsIsCandidateFilteredButHasNoSingleFeatureFlip instead.
    @Test fun templateInferredFixedSkillsMatchTheHardcodedPatternTheyAreReadFrom() {
        val seenAccGroup = listOf("case.acc.n", "case.acc.f", "case.acc.m", "case.gen.neg", "case.inst", "case.loc", "case.dat", "agreement.my", "pronouns")
        for (id in seenAccGroup) {
            val fixed = byId.getValue(id).fixed
            assertEquals(FeatureValue("Pres"), fixed[FeatureKey("Tense")], id)
            assertEquals(FeatureValue("1"), fixed[FeatureKey("Person")], id)
            assertEquals(FeatureValue("Sing"), fixed[FeatureKey("Number")], id)
        }
        val aspectFixed = byId.getValue("aspect").fixed
        assertEquals(FeatureValue("1"), aspectFixed[FeatureKey("Person")])
        assertEquals(FeatureValue("Sing"), aspectFixed[FeatureKey("Number")])
        // ExerciseFactory.kt:123-125 (sentence.question) takes no Tense/Person/Number argument
        // either — its "questionSource" pattern ("Widzisz {acc}.") is 2nd-person-singular-present,
        // so Person here is 2 rather than the seenAcc group's 1, but it's template-inferred the
        // same way, not a branch fact (see the class KDoc and questionFocusesOnMoodAtSecondPersonPresent).
        val questionFixed = byId.getValue("sentence.question").fixed
        assertEquals(FeatureValue("Pres"), questionFixed[FeatureKey("Tense")])
        assertEquals(FeatureValue("2"), questionFixed[FeatureKey("Person")])
        assertEquals(FeatureValue("Sing"), questionFixed[FeatureKey("Number")])
    }

    // ExerciseFactory.kt:117-121 — pronouns is in the filtered branch (:53) too, but substitutes a
    // pronoun for the noun phrase rather than flipping a single feature value: focus is null.
    // :119 (`personalPronouns[key].getValue(GramCase.ACC)`) passes the literal `GramCase.ACC`, so
    // `Case: Acc` is a real branch parameter here (unlike the template-inferred Tense/Person/Number
    // checked in templateInferredFixedSkillsMatchTheHardcodedPatternTheyAreReadFrom).
    @Test fun pronounsIsCandidateFilteredButHasNoSingleFeatureFlip() {
        val spec = byId.getValue("pronouns")
        assertNull(spec.focus)
        assertEquals(LexicalFilter("noun", mapOf("nounId" to filteredNounIds)), spec.lexicalFilter)
        assertEquals(FeatureValue("Acc"), spec.fixed[FeatureKey("Case")])
    }

    // ExerciseFactory.kt:123-125 — source pattern "Widzisz {acc}." is 2nd person present; Mood Decl→YesNoQ.
    // The branch (:123-125) calls only `sentence("questionSource", ...)`/`exerciseCopy(...)` — no
    // verbForm call, no Person/Tense/Number argument anywhere. So `Person: 2` is exactly as
    // template-inferred as the seenAcc group's `Person: 1` (both are just a property of a
    // hardcoded course.json pattern string), not a real branch fact; `fixed` is checked in
    // templateInferredFixedSkillsMatchTheHardcodedPatternTheyAreReadFrom instead.
    @Test fun questionFocusesOnMoodAtSecondPersonPresent() {
        val spec = byId.getValue("sentence.question")
        assertEquals(FeatureFocus(FeatureKey("Mood"), FeatureValue("Decl"), FeatureValue("YesNoQ")), spec.focus)
        assertNull(spec.lexicalFilter)
    }

    // ExerciseFactory.kt:126-131 — Number Sing→Plur, Case stays Acc (`phrase(seed, GramCase.ACC, ...)`,
    // a real branch parameter). `Tense`/`Person` below are read off the "seenAcc" source pattern
    // (:128), same template-inferred kind as templateInferredFixedSkillsMatchTheHardcodedPatternTheyAreReadFrom.
    @Test fun pluralFocusesOnNumberWithCaseHeldFixedAtAcc() {
        val spec = byId.getValue("sentence.plural")
        assertEquals(FeatureFocus(FeatureKey("Number"), FeatureValue("Sing"), FeatureValue("Plur")), spec.focus)
        assertEquals(FeatureValue("Acc"), spec.fixed[FeatureKey("Case")])
        assertEquals(FeatureValue("Pres"), spec.fixed[FeatureKey("Tense")])
        assertEquals(FeatureValue("1"), spec.fixed[FeatureKey("Person")])
        assertNull(spec.lexicalFilter)
    }

    // ExerciseFactory.kt:88-97 (random owner among 6), :113-116 (hardcoded copy pair, two axes at
    // once), :132-136 (composite of two FormChanges): none is a single feature-value flip.
    @Test fun skillsWithoutASingleFeatureFlipHaveNoFocus() {
        for (id in listOf("agreement.my", "aspect", "mixed")) {
            assertNull(byId.getValue(id).focus, id)
            assertNull(byId.getValue(id).lexicalFilter, id)
        }
        // "mixed" changes both a verb form and a case at once (ExerciseFactory.kt:132-136), so no
        // single axis is held fixed either.
        assertEquals(emptyMap(), byId.getValue("mixed").fixed)
        // ExerciseFactory.kt:93 (`phrase(seed, GramCase.ACC, owner)`) passes the literal
        // `GramCase.ACC`, so `Case: Acc` is a real branch parameter for agreement.my (unlike its
        // template-inferred Tense/Person/Number, checked in
        // templateInferredFixedSkillsMatchTheHardcodedPatternTheyAreReadFrom).
        assertEquals(FeatureValue("Acc"), byId.getValue("agreement.my").fixed[FeatureKey("Case")])
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
