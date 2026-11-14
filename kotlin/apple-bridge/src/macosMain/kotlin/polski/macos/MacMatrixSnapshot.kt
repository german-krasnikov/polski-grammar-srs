package polski.macos

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import polski.data.comparisonNounIds
import polski.data.nounById
import polski.data.personalPronouns
import polski.data.possessives
import polski.data.referenceAspectRows
import polski.data.referenceChainRows
import polski.data.referencePronounTeaching
import polski.data.referenceTenseRows
import polski.data.referenceVerbTeaching
import polski.data.verbs
import polski.grammar.verbForm
import polski.model.GramCase
import polski.model.NumberGram
import polski.model.Tense
import polski.apple.putCommonMatrixFields
import polski.presentation.AppUiState
import polski.presentation.ChangeSide
import polski.presentation.ContrastPair
import polski.presentation.casesFullTable
import polski.presentation.changeHighlightParts
import polski.presentation.comparisonTable
import polski.presentation.contrastPairJson
import polski.presentation.enDoSupportTable
import polski.presentation.enVerbsTable
import polski.presentation.personalPronounsTable
import polski.presentation.possessivesTable
import polski.presentation.toJson
import polski.presentation.verbsTable

// UC-09 part 2/2 (UniversalCorePlan.md §5.3.3, ADR-21): [casesTable]/[comparisonTable]/[verbsTable]/
// [enVerbsTable]/[enDoSupportTable]/[personalPronounsTable]/[possessivesTable] replace what used to
// be hand-built per field below (`cases`/`comparison`/`verbsRows`/`pronouns`/`possessives`) with the
// shared engine's [polski.presentation.MatrixTableViewModel], serialized once by [toJson]. The older
// per-field arrays stay below unchanged for now, so nothing already reading them breaks (W3.2 keeps
// this pre-existing split as-is; iOS's own `IosMatrixSnapshot.kt` is the one that reimplemented these
// builders by hand and now calls the same shared functions instead).
internal fun matrixSnapshot(state: AppUiState): JsonElement {
    val selection = state.matrixSelection
    val number = NumberGram.fromId(selection.numberId)
    return buildJsonObject {
        putCommonMatrixFields(state)
        // UC-09 part 2/2 (UniversalCorePlan.md §5.3.3, ADR-21): these five tables replace what used
        // to be hand-built per field below (`cases`/`comparison`/`verbsRows`/`pronouns`/`possessives`)
        // with the shared engine's [MatrixTableViewModel], serialized once by [toJson]. The older
        // per-field arrays stay below unchanged for now, so nothing already reading them breaks.
        put("casesTable", casesFullTable(selection).toJson())
        put("comparisonTable", comparisonTable(selection).toJson())
        put("verbsTable", verbsTable(selection).toJson())
        // EN-24 (UC-09 part 2/2 minimum, Plans/Kotlin/EnRuPackPlan.md §6, macOS slice): the one
        // live English matrix on this host, read from `forms.generated.json`(en) through the same
        // `enMorphology`/`MatrixTableEngine`/`MatrixTableViewModel` path the web host already
        // uses — not a mock and not a second ad hoc table-building path.
        put("enVerbsTable", enVerbsTable().toJson())
        put("enDoSupportTable", enDoSupportTable().toJson())
        put("personalPronounsTable", personalPronounsTable().toJson())
        put("possessivesTable", possessivesTable().toJson())
        put("chainRows", JsonArray(referenceChainRows.map { row -> buildJsonObject {
            put("label", row.label); put("from", row.from); put("to", row.to); put("change", row.change)
            put("beforeParts", JsonArray(changeHighlightParts(row.from, row.to, ChangeSide.Before).map { part -> buildJsonObject {
                put("text", part.text); put("changed", part.isChanged)
            } }))
            put("afterParts", JsonArray(changeHighlightParts(row.from, row.to, ChangeSide.After).map { part -> buildJsonObject {
                put("text", part.text); put("changed", part.isChanged)
            } }))
        } }))
        put("tenseRows", JsonArray(referenceTenseRows.map { row -> buildJsonObject {
            put("label", row.label); put("from", row.from); put("to", row.to)
            put("beforeParts", JsonArray(changeHighlightParts(row.from, row.to, ChangeSide.Before).map { part -> buildJsonObject {
                put("text", part.text); put("changed", part.isChanged)
            } }))
            put("afterParts", JsonArray(changeHighlightParts(row.from, row.to, ChangeSide.After).map { part -> buildJsonObject {
                put("text", part.text); put("changed", part.isChanged)
            } }))
        } }))
        put("aspectRows", JsonArray(referenceAspectRows.map { row -> buildJsonObject {
            put("label", row.label); put("from", row.from)
            put("entries", JsonArray(listOf(
                "Настоящее" to row.present, "Прошедшее" to row.past, "Будущее" to row.future,
            ).map { (label, form) -> buildJsonObject {
                put("label", label); put("available", form != null)
                if (form != null) {
                    put("from", row.from); put("to", form)
                    put("beforeParts", JsonArray(changeHighlightParts(row.from, form, ChangeSide.Before).map { part -> buildJsonObject {
                        put("text", part.text); put("changed", part.isChanged)
                    } }))
                    put("afterParts", JsonArray(changeHighlightParts(row.from, form, ChangeSide.After).map { part -> buildJsonObject {
                        put("text", part.text); put("changed", part.isChanged)
                    } }))
                }
            } }))
        } }))
        put("comparison", JsonArray(comparisonNounIds.map { id ->
            val noun = nounById(id)
            buildJsonObject {
                put("title", noun.lemma)
                GramCase.entries.forEach { gramCase ->
                    val form = noun.forms.getValue(number).getValue(gramCase)
                    put(gramCase.name, form)
                    put("${gramCase.name}pair", contrastPairJson(ContrastPair.generated(
                        noun.forms.getValue(number).getValue(GramCase.NOM), form)))
                }
            }
        }))
        put("verbsRows", JsonArray(referenceVerbTeaching.subjects.map { subject -> buildJsonObject {
            put("title", subject.label.compact)
            listOf(Tense.PRESENT, Tense.PAST, Tense.FUTURE).forEach { tense ->
                val form = verbForm(selection.verbId, tense, subject.person, subject.number, subject.gender(selection.feminineGroup))
                put(tense.id, form)
                put("${tense.id}Pair", contrastPairJson(ContrastPair.generated(verbs.first { it.id == selection.verbId }.lemma, form)))
            }
        } }))
        put("pronouns", JsonArray(referencePronounTeaching.pronounIds.map { id ->
            val forms = personalPronouns.getValue(id)
            buildJsonObject {
            put("title", id)
            referencePronounTeaching.contexts.forEach { context ->
                val form = if (context.id == GramCase.LOC) forms.getValue(GramCase.LOC) else context.value(id, forms)
                put(context.id.id, form)
                put("${context.id.id}pair", contrastPairJson(ContrastPair.generated(id, form)))
            }
        } }))
        put("possessives", JsonArray(possessives.map { possessive -> buildJsonObject {
            put("title", possessive.label)
            referencePronounTeaching.demo.cases.forEach { row ->
                val phrase = referencePronounTeaching.demo.phrase(possessive.id, row.id)
                put(row.id.id, phrase)
                put("${row.id.id}pair", contrastPairJson(ContrastPair.generated(
                    referencePronounTeaching.demo.phrase(possessive.id, GramCase.NOM), phrase)))
            }
        } }))
    }
}
