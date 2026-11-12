package polski.ios

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import polski.data.adjectives
import polski.data.activeCoursePackOption
import polski.data.comparisonNounIds
import polski.data.courseAspectNoPresent
import polski.data.courseMatrixIntroduction
import polski.data.enPersonalPronouns
import polski.data.enVerbs
import polski.data.maleAccRows
import polski.data.nounById
import polski.data.nouns
import polski.data.personalPronouns
import polski.data.possessives
import polski.data.referenceAspectRows
import polski.data.referenceCaseTeaching
import polski.data.referenceChainRows
import polski.data.referencePipeline
import polski.data.referencePronounTeaching
import polski.data.referenceRussianSupport
import polski.data.referenceSystemCards
import polski.data.referenceTenseRows
import polski.data.referenceVerbTeaching
import polski.data.verbs
import polski.grammar.caseRows
import polski.model.Aspect
import polski.model.GramCase
import polski.model.Tense
import polski.presentation.AppUiState
import polski.presentation.ChangeSide
import polski.presentation.ContrastPair
import polski.presentation.casesFullTable
import polski.presentation.changeHighlightParts
import polski.presentation.choice
import polski.presentation.comparisonTable
import polski.presentation.contrastPairJson
import polski.presentation.enDoSupportTable
import polski.presentation.enVerbsTable
import polski.presentation.possessivesTable
import polski.presentation.toViewModel
import polski.presentation.verbsTable
import polski.core.engine.MatrixColumn
import polski.core.engine.MatrixTableEngine

// UniversalCorePlan.md §5.3.3/§12 UC-09 (host wiring, iOS) + W3.2 (architect finding #5, 2026-11):
// every table below either reads straight off the same `:shared` `MatrixTables.kt` builders
// MacSnapshot.kt already calls ([casesFullTable], [comparisonTable], [verbsTable], [enVerbsTable],
// [enDoSupportTable], [possessivesTable]) or — for `chainRows`/`tenseRows`/`aspectRows`, and for
// `pronouns` (whose LOC column keeps the raw locative form instead of [MatrixColumn]'s normal
// preposition-prefixed value, same override Android's `androidLine` applies) — builds its own
// [MatrixTableEngine] table, because no shared builder with that behavior exists yet. The wire
// JSON shape Swift already binds to (named per-section fields, not a generic grid) is unchanged on
// purpose: that shape is covered by IosMatrixSnapshotTest and the SwiftUI screens/screenshots.
internal fun matrixSnapshot(state: AppUiState): JsonElement {
    val selection = state.matrixSelection
    val targetLanguage = activeCoursePackOption.target
    return buildJsonObject {
        put("section", selection.section.name)
        put("targetLanguage", targetLanguage)
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
        val chainTable = MatrixTableEngine.build(
            rowAxis = referenceChainRows,
            rowHeaderLabel = "Операция",
            rowHeader = { it.label },
            columns = listOf(
                MatrixColumn("Целое предложение", { it.to }, contrastFrom = { it.from }),
                MatrixColumn("Что изменилось", { it.change }),
            ),
        ).toViewModel()
        put("chainRows", JsonArray(referenceChainRows.mapIndexed { i, row -> buildJsonObject {
            put("label", row.label); put("from", row.from); put("to", row.to); put("change", row.change)
            val pairJson = contrastPairJson(requireNotNull(chainTable.rows[i].cells[0].contrast))
            put("beforeParts", pairJson.getValue("beforeParts")); put("afterParts", pairJson.getValue("afterParts"))
        } }))
        val tenseTable = MatrixTableEngine.build(
            rowAxis = referenceTenseRows,
            rowHeaderLabel = "Операция",
            rowHeader = { it.label },
            columns = listOf(MatrixColumn("Предложение", { it.to }, contrastFrom = { it.from })),
        ).toViewModel()
        put("tenseRows", JsonArray(referenceTenseRows.mapIndexed { i, row -> buildJsonObject {
            put("label", row.label); put("from", row.from); put("to", row.to)
            val pairJson = contrastPairJson(requireNotNull(tenseTable.rows[i].cells[0].contrast))
            put("beforeParts", pairJson.getValue("beforeParts")); put("afterParts", pairJson.getValue("afterParts"))
        } }))
        val aspectTable = MatrixTableEngine.build(
            rowAxis = referenceAspectRows,
            rowHeaderLabel = "Смысл",
            rowHeader = { it.label },
            columns = listOf(
                MatrixColumn("Настоящее", { it.present ?: "" }, contrastFrom = { row -> if (row.present == null) null else row.from }),
                MatrixColumn("Прошедшее", { it.past }, contrastFrom = { it.from }),
                MatrixColumn("Будущее", { it.future }, contrastFrom = { it.from }),
            ),
        ).toViewModel()
        put("aspectRows", JsonArray(referenceAspectRows.mapIndexed { i, row -> buildJsonObject {
            put("label", row.label); put("from", row.from)
            put("entries", JsonArray(listOf("Настоящее", "Прошедшее", "Будущее").mapIndexed { colIndex, label ->
                val cell = aspectTable.rows[i].cells[colIndex]
                buildJsonObject {
                    put("label", label); put("available", cell.contrast != null)
                    val contrast = cell.contrast
                    if (contrast != null) {
                        val pairJson = contrastPairJson(contrast)
                        put("from", contrast.from); put("to", cell.value)
                        put("beforeParts", pairJson.getValue("beforeParts")); put("afterParts", pairJson.getValue("afterParts"))
                    }
                }
            }))
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
        // W3.2: cases/comparison/verbs/English verbs & do-support/possessives now read their cell
        // values and contrasts off the shared `:shared` MatrixTables.kt builders (same ones
        // MacSnapshot.kt's matrixSnapshot calls) instead of each re-running its own
        // MatrixTableEngine.build(...) with the same row axis and columns by hand.
        val casesTableView = casesFullTable(selection)
        put("cases", JsonArray(caseRows.mapIndexed { i, row -> buildJsonObject {
            put("title", "${row.pl} · ${row.ru}")
            put("question", row.question); put("trigger", row.trigger)
            val phraseCell = casesTableView.rows[i].cells[1]
            val sentenceCell = casesTableView.rows[i].cells[2]
            put("phrase", phraseCell.value); put("sentence", sentenceCell.value)
            put("phrasePair", contrastPairJson(requireNotNull(phraseCell.contrast)))
            put("sentencePair", contrastPairJson(requireNotNull(sentenceCell.contrast)))
        } }))
        put("comparisonCases", JsonArray(caseRows.map { choice(it.id.name, it.pl) }))
        val comparisonTableView = comparisonTable(selection)
        put("comparison", JsonArray(comparisonNounIds.mapIndexed { colIndex, id ->
            val noun = nounById(id)
            buildJsonObject {
                put("title", noun.lemma)
                caseRows.forEachIndexed { rowIndex, row ->
                    val cell = comparisonTableView.rows[rowIndex].cells[colIndex]
                    put(row.id.name, cell.value)
                    put("${row.id.name}pair", contrastPairJson(requireNotNull(cell.contrast)))
                }
            }
        }))
        put("verbGenderControlLabel", referenceVerbTeaching.genderControlLabel.compact)
        put("verbGenderOptions", JsonArray(referenceVerbTeaching.genderOptions.map { choice(it.id, it.label.compact) }))
        put("verbFutureExplanation", referenceVerbTeaching.compactFutureExplanation)
        put("verbTenseLabels", buildJsonObject {
            Tense.entries.forEach { tense -> put(tense.id, referenceVerbTeaching.tenseLabels.getValue(tense).compact) }
        })
        val verbTenses = listOf(Tense.PRESENT, Tense.PAST, Tense.FUTURE)
        val verbsTableView = verbsTable(selection)
        put("verbsRows", JsonArray(referenceVerbTeaching.subjects.mapIndexed { i, subject -> buildJsonObject {
            put("title", subject.label.compact)
            verbTenses.forEachIndexed { colIndex, tense ->
                val cell = verbsTableView.rows[i].cells[colIndex]
                put(tense.id, cell.value)
                put("${tense.id}Pair", contrastPairJson(requireNotNull(cell.contrast)))
            }
        } }))
        // EN-24 (UC-09 part 2/2, ios lane, EnRuPackPlan.md §5 gap H / §6): mirrors the web host's
        // `renderEnglishVerbMatrix` (ADR-28) — the one live English matrix (Present/Past/Future ×
        // person, fixed example verb "see") plus a do-support table, both read through the shared
        // `enVerbsTable`/`enDoSupportTable` builders (`:shared` commonMain) every pl table above
        // already uses via MatrixTableEngine/MatrixTableViewModel.
        if (targetLanguage == "en") {
            val englishExampleVerbId = "see"
            val englishExampleLemma = enVerbs.first { it.id == englishExampleVerbId }.lemma
            put("englishExampleLemma", englishExampleLemma)
            val englishTenses = listOf(Tense.PRESENT, Tense.PAST, Tense.FUTURE)
            val englishVerbsTableView = enVerbsTable(englishExampleVerbId)
            put("englishVerbsRows", JsonArray(enPersonalPronouns.mapIndexed { i, pronoun -> buildJsonObject {
                put("title", pronoun.subject)
                englishTenses.forEachIndexed { colIndex, tense ->
                    val cell = englishVerbsTableView.rows[i].cells[colIndex]
                    put(tense.id, cell.value)
                    put("${tense.id}Pair", contrastPairJson(requireNotNull(cell.contrast)))
                }
            } }))
            // Future is built with "will" alone and has no do-support pair.
            val englishDoSupportTableView = enDoSupportTable()
            put("englishDoSupportRows", JsonArray(enPersonalPronouns.mapIndexed { i, pronoun -> buildJsonObject {
                put("title", pronoun.subject)
                listOf(Tense.PRESENT, Tense.PAST).forEachIndexed { colIndex, tense ->
                    val cell = englishDoSupportTableView.rows[i].cells[colIndex]
                    put(tense.id, cell.value)
                    put("${tense.id}Pair", contrastPairJson(requireNotNull(cell.contrast)))
                }
            } }))
        }
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
        // The LOC context's own column keeps the raw locative form instead of `context.value`'s
        // preposition prefix — the same override Android's `androidLine` applies (CourseData.kt)
        // — so this host-specific choice lives here rather than in the shared [MatrixTables.kt]
        // `personalPronounsTable()` builder, which every other host's pronoun column reads as-is.
        val pronounsTable = MatrixTableEngine.build(
            rowAxis = referencePronounTeaching.pronounIds,
            rowHeaderLabel = "Кто",
            rowHeader = { it },
            columns = referencePronounTeaching.contexts.map { context ->
                MatrixColumn(
                    context.id.id,
                    { id -> if (context.id == GramCase.LOC) personalPronouns.getValue(id).getValue(GramCase.LOC) else context.value(id, personalPronouns.getValue(id)) },
                    contrastFrom = { id -> id },
                )
            },
        ).toViewModel()
        put("pronouns", JsonArray(referencePronounTeaching.pronounIds.mapIndexed { i, id -> buildJsonObject {
            put("title", id)
            referencePronounTeaching.contexts.forEachIndexed { colIndex, context ->
                val cell = pronounsTable.rows[i].cells[colIndex]
                put(context.id.id, cell.value)
                put("${context.id.id}pair", contrastPairJson(requireNotNull(cell.contrast)))
            }
        } }))
        val possessivesTableView = possessivesTable()
        put("possessives", JsonArray(possessives.mapIndexed { i, possessive -> buildJsonObject {
            put("title", possessive.label)
            referencePronounTeaching.demo.cases.forEachIndexed { colIndex, row ->
                val cell = possessivesTableView.rows[i].cells[colIndex]
                put(row.id.id, cell.value)
                put("${row.id.id}pair", contrastPairJson(requireNotNull(cell.contrast)))
            }
        } }))
    }
}
