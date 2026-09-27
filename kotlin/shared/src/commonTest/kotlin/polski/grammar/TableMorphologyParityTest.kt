package polski.grammar

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import polski.core.engine.TableMorphology
import polski.core.model.FeatureBundle
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.data.adjectives
import polski.data.nouns
import polski.data.possessives
import polski.data.verbs
import polski.model.Aspect
import polski.model.CaseFeature
import polski.model.Gender
import polski.model.GenderFeature
import polski.model.GramCase
import polski.model.NumberFeature
import polski.model.NumberGram
import polski.model.Person
import polski.model.PersonFeature
import polski.model.Tense
import polski.model.TenseFeature
import polski.model.toFeatureValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Manual JsonElement navigation, not @Serializable/decodeFromString — matches
// `polski.data.CourseData`'s convention (no kotlinx-serialization compiler plugin is applied).
private fun parseForms(json: String): Map<String, Map<FeatureBundle, String>> =
    Json.parseToJsonElement(json).jsonObject["forms"]!!.jsonObject.mapValues { (_, entries) ->
        entries.jsonArray.associate { entry ->
            val bundle: FeatureBundle = entry.jsonObject["bundle"]!!.jsonObject
                .mapKeys { (k, _) -> FeatureKey(k) }
                .mapValues { (_, v) -> FeatureValue(v.jsonPrimitive.content) }
            bundle to entry.jsonObject["form"]!!.jsonPrimitive.content
        }
    }

private fun entryCount(json: String): Int =
    Json.parseToJsonElement(json).jsonObject["forms"]!!.jsonObject.values.sumOf { it.jsonArray.size }

/**
 * UniversalCorePlan.md §12 UC-05 acceptance: "verb/noun/adjective/possessive forms from
 * TableMorphology equal GrammarEngine for every lexeme × feature bundle (exhaustive test) and
 * match tests/fixtures/core-golden". [TableMorphology] here is built from `forms.generated.json`
 * itself (embedded at build time by `generateFormsFixtureSource`, `shared/build.gradle.kts` —
 * scripts/build-pack.mjs's actual output, not a re-derivation), so this proves both that the
 * converter's data loads correctly through the real :core-engine contract, and that every
 * materialized form agrees with [GrammarEngine]'s computed form. `scripts/build-pack.mjs --check`
 * separately proves forms.generated.json agrees with tests/fixtures/core-golden/grammar.json
 * byte-for-byte, so this test doesn't re-load that fixture — GrammarEngine's own agreement with
 * core-golden is already `GrammarParityTest`'s job.
 */
class TableMorphologyParityTest {
    private val morphology: TableMorphology = TableMorphology(parseForms(generatedFormsFixtureJson))

    @Test fun tableMorphologyLoadsEveryEntryFromTheGeneratedFixture() {
        val count = entryCount(generatedFormsFixtureJson)
        assertTrue(count > 1500, "expected the full pl-ru forms table, got $count entries")
    }

    @Test fun nounFormsMatchForEveryNounNumberAndCase() {
        for (noun in nouns) {
            for (number in NumberGram.entries) {
                for (gramCase in GramCase.entries) {
                    val bundle = mapOf(NumberFeature to number.toFeatureValue(), CaseFeature to gramCase.toFeatureValue())
                    assertEquals(
                        nounForm(noun.id, gramCase, number),
                        morphology.form("noun:${noun.id}", bundle),
                        "noun:${noun.id} $number/$gramCase",
                    )
                }
            }
        }
    }

    @Test fun adjectiveFormsMatchForEveryAdjectiveNumberGenderAndCase() {
        for (adjective in adjectives) {
            for (number in NumberGram.entries) {
                for (gender in Gender.entries) {
                    for (gramCase in GramCase.entries) {
                        val bundle = mapOf(
                            NumberFeature to number.toFeatureValue(),
                            GenderFeature to gender.toFeatureValue(),
                            CaseFeature to gramCase.toFeatureValue(),
                        )
                        assertEquals(
                            adjectiveForm(adjective.id, gender, gramCase, number),
                            morphology.form("adjective:${adjective.id}", bundle),
                            "adjective:${adjective.id} $number/$gender/$gramCase",
                        )
                    }
                }
            }
        }
    }

    @Test fun possessiveFormsMatchForEveryOwnerNumberGenderAndCase() {
        for (possessive in possessives) {
            for (number in NumberGram.entries) {
                for (gender in Gender.entries) {
                    for (gramCase in GramCase.entries) {
                        val bundle = mapOf(
                            NumberFeature to number.toFeatureValue(),
                            GenderFeature to gender.toFeatureValue(),
                            CaseFeature to gramCase.toFeatureValue(),
                        )
                        assertEquals(
                            possessiveForm(possessive.id, gender, number, gramCase),
                            morphology.form("possessive:${possessive.id.id}", bundle),
                            "possessive:${possessive.id.id} $number/$gender/$gramCase",
                        )
                    }
                }
            }
        }
    }

    // GrammarEngine.verbForm never varies present/future by gender, and past never varies by
    // m-inanimate (identical to m-personal/m-animate) — scripts/build-pack.mjs materializes
    // exactly these combinations, matching tests/fixtures/core-golden/grammar.json's G-VERB-* cases.
    private val pastGenders = listOf(Gender.M_PERSONAL, Gender.F, Gender.N, Gender.M_ANIMATE)

    @Test fun verbPresentFormsMatchForEveryImperfectiveVerbPersonAndNumber() {
        for (verb in verbs.filter { it.aspect == Aspect.IMPERFECTIVE }) {
            for (number in NumberGram.entries) {
                for (person in Person.entries) {
                    val bundle = mapOf(
                        TenseFeature to Tense.PRESENT.toFeatureValue(),
                        PersonFeature to person.toFeatureValue(),
                        NumberFeature to number.toFeatureValue(),
                    )
                    assertEquals(
                        verbForm(verb.id, Tense.PRESENT, person, number),
                        morphology.form("verb:${verb.id}", bundle),
                        "verb:${verb.id} present $person/$number",
                    )
                }
            }
        }
    }

    @Test fun verbPastFormsMatchForEveryVerbPersonNumberAndGender() {
        for (verb in verbs) {
            for (number in NumberGram.entries) {
                for (person in Person.entries) {
                    for (gender in pastGenders) {
                        val bundle = mapOf(
                            TenseFeature to Tense.PAST.toFeatureValue(),
                            PersonFeature to person.toFeatureValue(),
                            NumberFeature to number.toFeatureValue(),
                            GenderFeature to gender.toFeatureValue(),
                        )
                        assertEquals(
                            verbForm(verb.id, Tense.PAST, person, number, gender),
                            morphology.form("verb:${verb.id}", bundle),
                            "verb:${verb.id} past $person/$number/$gender",
                        )
                    }
                }
            }
        }
    }

    @Test fun verbFutureFormsMatchForEveryVerbPersonAndNumber() {
        for (verb in verbs) {
            for (number in NumberGram.entries) {
                for (person in Person.entries) {
                    val bundle = mapOf(
                        TenseFeature to Tense.FUTURE.toFeatureValue(),
                        PersonFeature to person.toFeatureValue(),
                        NumberFeature to number.toFeatureValue(),
                    )
                    assertEquals(
                        verbForm(verb.id, Tense.FUTURE, person, number),
                        morphology.form("verb:${verb.id}", bundle),
                        "verb:${verb.id} future $person/$number",
                    )
                }
            }
        }
    }
}
