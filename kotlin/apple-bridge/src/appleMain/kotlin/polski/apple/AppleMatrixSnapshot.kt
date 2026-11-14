package polski.apple

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import polski.data.adjectives
import polski.data.courseAspectNoPresent
import polski.data.courseMatrixIntroduction
import polski.data.maleAccRows
import polski.data.nouns
import polski.data.possessives
import polski.data.referenceCaseTeaching
import polski.data.referencePipeline
import polski.data.referencePronounTeaching
import polski.data.referenceRussianSupport
import polski.data.referenceSystemCards
import polski.data.referenceVerbTeaching
import polski.data.verbs
import polski.grammar.caseRows
import polski.model.Aspect
import polski.model.Tense
import polski.presentation.AppUiState
import polski.presentation.ChangeSide
import polski.presentation.ContrastPair
import polski.presentation.changeHighlightParts
import polski.presentation.choice
import polski.presentation.contrastPairJson

/**
 * Fields byte-identical between `IosMatrixSnapshot.kt`'s and `MacMatrixSnapshot.kt`'s own
 * `matrixSnapshot(state)` (`Plans/DSL-DEDUP-MIGRATION.md` §3) — selection ids, the choice arrays,
 * pipeline/support/case copy, `systemCards`, `maleAccRows`, `comparisonCases`, the verb-gender/tense
 * label copy, and the pronoun/possessive intro copy. `cases`/`comparison`/`verbsRows`/`pronouns`/
 * `possessives`/`chainRows`/`tenseRows`/`aspectRows`/English-matrix fields stay platform-specific:
 * each host reads a genuinely different wire shape or computation path for those (iOS via
 * `MatrixTableEngine`+`contrastPairJson`, macOS via `changeHighlightParts` directly, or the newer
 * `MatrixTableViewModel.toJson()` tables macOS also exposes) — not duplication, so this function
 * does not touch them.
 */
internal fun JsonObjectBuilder.putCommonMatrixFields(state: AppUiState) {
    val selection = state.matrixSelection
    put("section", selection.section.name)
    put("matrixIntroduction", courseMatrixIntroduction)
    put("aspectNoPresent", courseAspectNoPresent.ios)
    put("nounId", selection.nounId)
    put("adjectiveId", selection.adjectiveId)
    put("ownerId", selection.ownerId)
    put("numberId", selection.numberId)
    put("verbId", selection.verbId)
    put("feminineGroup", selection.feminineGroup)
    put("nouns", JsonArray(nouns.map { choice(it.id, "${it.lemma} — ${it.meaning}") }))
    put("adjectives", JsonArray(adjectives.map { choice(it.id, "${it.lemma} — ${it.meaning}") }))
    put("owners", JsonArray(possessives.map { choice(it.id.id, it.label) }))
    put("numbers", JsonArray(listOf(choice("sg", "Единственное"), choice("pl", "Множественное"))))
    put("verbs", JsonArray(verbs.filter { it.aspect == Aspect.IMPERFECTIVE }.map { choice(it.id, "${it.lemma} — ${it.meaning}") }))
    put("pipelineTitle", referencePipeline.title)
    put("pipelineSummary", referencePipeline.compactSummary)
    put("pipelineExample", referencePipeline.compactExample)
    put("supportTitle", referenceRussianSupport.compactTitle)
    put("supportLines", JsonArray(referenceRussianSupport.rows.map { JsonPrimitive(it.mobileLine) }))
    put("supportRows", JsonArray(referenceRussianSupport.rows.map { row -> buildJsonObject {
        put("line", row.mobileLine)
        put("comparisons", JsonArray(row.comparisons.map(::contrastPairJson)))
    } }))
    put("caseNote", referenceCaseTeaching.compactNote)
    put("systemCards", JsonArray(referenceSystemCards.map { card -> buildJsonObject {
        put("id", card.id); put("title", card.title)
        put("explanation", card.explanation); put("example", card.example)
        put("steps", JsonArray(card.steps.zipWithNext { from, to -> contrastPairJson(ContrastPair.generated(from, to)) }))
    } }))
    put("maleAccRows", JsonArray(maleAccRows.map { row -> buildJsonObject {
        put("label", row.label); put("title", row.title); put("rule", row.rule)
        put("examples", JsonArray(row.examples.map { example -> buildJsonObject {
            put("label", "Форма в Bierniku")
            put("from", example.from); put("to", example.to); put("change", example.sentence)
            put("beforeParts", JsonArray(changeHighlightParts(example.from, example.to, ChangeSide.Before).map { part -> buildJsonObject {
                put("text", part.text); put("changed", part.isChanged)
            } }))
            put("afterParts", JsonArray(changeHighlightParts(example.from, example.to, ChangeSide.After).map { part -> buildJsonObject {
                put("text", part.text); put("changed", part.isChanged)
            } }))
        } }))
    } }))
    put("comparisonCases", JsonArray(caseRows.map { choice(it.id.name, it.pl) }))
    put("verbGenderControlLabel", referenceVerbTeaching.genderControlLabel.compact)
    put("verbGenderOptions", JsonArray(referenceVerbTeaching.genderOptions.map { choice(it.id, it.label.compact) }))
    put("verbFutureExplanation", referenceVerbTeaching.compactFutureExplanation)
    put("verbTenseLabels", buildJsonObject {
        Tense.entries.forEach { tense -> put(tense.id, referenceVerbTeaching.tenseLabels.getValue(tense).compact) }
    })
    put("pronounIntro", referencePronounTeaching.compactIntro)
    put("pronounFooter", referencePronounTeaching.nativeFooter)
    put("pronounContexts", JsonArray(referencePronounTeaching.contexts.map { context -> buildJsonObject {
        put("id", context.id.id)
        put("iosCue", context.cue.ios)
    } }))
    put("possessiveTitle", referencePronounTeaching.possessiveTitle)
    put("possessiveCases", JsonArray(referencePronounTeaching.demo.cases.map { row -> buildJsonObject {
        put("id", row.id.id)
        put("iosCaption", row.ios)
    } }))
}
