package polski.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import polski.core.RecipeSet
import polski.core.engine.ConstructionTemplate
import polski.core.generatedExerciseRecipesJsonByLang
import polski.core.generatedFormsGeneratedJsonByLang
import polski.core.generatedRealizationJsonByLang
import polski.core.model.FeatureBundle
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.core.model.SkillSpec
import polski.core.parseConstructionTemplates
import polski.core.parseRecipes
import polski.training.parseCurriculum

/**
 * R1.13 to R1.17 only: the engine inputs of en-ru are still the four JSON files of `courses/lang/en`, parsed on first access, while pl-ru's come from rows.
 * R1.14 to R1.16 type them; R1.17 points EnRuTypedPack at the rows and deletes this file, the four parsers it calls and the JSON.
 */
internal val bridgedEnRuSkillSpecs: List<SkillSpec> by lazy { parseCurriculum(generatedCurriculumJsonByLang.getValue("en")) }
internal val bridgedEnRuRecipes: RecipeSet by lazy { parseRecipes(generatedExerciseRecipesJsonByLang.getValue("en")) }
internal val bridgedEnRuConstructionTemplates: Map<String, ConstructionTemplate> by lazy { parseConstructionTemplates(generatedRealizationJsonByLang.getValue("en")) }
internal val bridgedEnRuFormTable: Map<String, Map<FeatureBundle, String>> by lazy { parseForms(generatedFormsGeneratedJsonByLang.getValue("en")) }

private fun parseForms(json: String): Map<String, Map<FeatureBundle, String>> =
    Json.parseToJsonElement(json).jsonObject["forms"]!!.jsonObject.mapValues { (_, entries) ->
        entries.jsonArray.associate { entry ->
            val bundle: FeatureBundle = entry.jsonObject["bundle"]!!.jsonObject
                .mapKeys { (k, _) -> FeatureKey(k) }
                .mapValues { (_, v) -> FeatureValue(v.jsonPrimitive.content) }
            bundle to entry.jsonObject["form"]!!.jsonPrimitive.content
        }
    }
