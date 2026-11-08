package polski.ios

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import polski.exam.ExamCatalog
import polski.exam.ExamModule
import polski.exam.OfficialExamEntry
import polski.exam.formatQuarterPoints
import polski.exam.officialGrammarData
import polski.exam.grammar.GrammarBankToken
import polski.exam.grammar.GrammarExamAction
import polski.exam.grammar.GrammarExamScorer
import polski.exam.grammar.GrammarExamState
import polski.exam.grammar.GrammarTest
import polski.exam.grammar.reduceGrammarExam

/** Scene-owned native exam state. Swift receives presentation snapshots and sends semantic actions;
 *  shared Kotlin owns answers and grades. A temporary tab switch leaves this session intact. */
class IosExamSession {
    private var selectedExam: OfficialExamEntry? = null
    private var grammarTests: List<GrammarTest> = emptyList()
    private var grammarState: GrammarExamState? = null
    private val selectedBankTokenByItemId = mutableMapOf<String, String>()
    private var errorCode: String? = null

    fun selectExam(routeId: String) {
        val exam = ExamCatalog.find(routeId)
        if (exam == null) { errorCode = "unknown_exam"; return }
        selectedExam = exam
        grammarTests = officialGrammarData.getValue(routeId)
        grammarState = null
        selectedBankTokenByItemId.clear()
        errorCode = null
    }

    /** The first native slice admits Grammar only. Other source modules stay visible and disabled. */
    fun selectModule(module: String) {
        if (selectedExam == null) { errorCode = "no_exam"; return }
        if (module != "grammar") { errorCode = "unavailable_module"; return }
        grammarState = GrammarExamState(mergedGrammar(requireNotNull(selectedExam), grammarTests))
        selectedBankTokenByItemId.clear()
        errorCode = null
    }

    fun answer(itemId: String, value: String) {
        val state = grammarState ?: run { errorCode = "no_grammar"; return }
        if (state.test.items.none { it.itemId == itemId }) { errorCode = "unknown_item"; return }
        grammarState = reduceGrammarExam(state, GrammarExamAction.Answer(itemId, value))
        errorCode = null
    }

    /** Keeps token identity even when a source bank contains repeated visible words. */
    fun answerBankToken(itemId: String, tokenId: String) {
        val sourceTest = grammarTests.singleOrNull { test -> test.items.any { it.itemId == itemId } }
        val token = sourceTest?.bankTokens?.singleOrNull { it.tokenId == tokenId }
        if (token == null) { errorCode = "unknown_token"; return }
        val before = grammarState
        answer(itemId, token.text)
        if (errorCode == null && grammarState !== before) selectedBankTokenByItemId[itemId] = tokenId
    }

    fun next() = update(GrammarExamAction.Next)
    fun previous() = update(GrammarExamAction.Previous)
    fun submit() = update(GrammarExamAction.Submit)

    private fun update(action: GrammarExamAction) {
        val state = grammarState ?: run { errorCode = "no_grammar"; return }
        grammarState = reduceGrammarExam(state, action)
        errorCode = null
    }

    /** Explicitly leave a part; selecting another app tab does not call this. */
    fun exitGrammar() { grammarState = null; selectedBankTokenByItemId.clear(); errorCode = null }

    /** Explicit scene disposal or new sitting. */
    fun close() { selectedExam = null; grammarTests = emptyList(); grammarState = null; selectedBankTokenByItemId.clear(); errorCode = null }

    fun currentSnapshot(): String = buildJsonObject {
        put("screen", if (grammarState?.submitted == true) "grammarResult" else if (grammarState != null) "grammar"
            else if (selectedExam != null) "modules" else "catalog")
        put("errorCode", errorCode?.let(::JsonPrimitive) ?: JsonNull)
        put("catalog", JsonArray(ExamCatalog.entries.map { exam -> buildJsonObject {
            put("routeId", exam.routeId); put("sourceExamId", exam.sourceExamId)
            put("provider", exam.provider); put("targetLanguage", exam.targetLanguage)
            put("level", exam.level); put("year", exam.year)
            put("grammarItemCount", exam.grammarItemCount)
        } }))
        put("selectedExamId", selectedExam?.routeId?.let(::JsonPrimitive) ?: JsonNull)
        put("modules", JsonArray(selectedExam?.modules.orEmpty().map { cover -> buildJsonObject {
            put("module", cover.module.name.lowercase()); put("order", cover.order)
            put("durationSeconds", cover.durationSeconds); put("maximumQuarter", cover.maximumQuarter)
            put("availableOnIpad", cover.module == ExamModule.Grammar)
        } }))
        val state = grammarState
        if (state != null) {
            val item = state.test.items[state.currentIndex]
            val sourceTest = grammarTests.single { source -> source.items.any { it.itemId == item.itemId } }
            put("grammar", buildJsonObject {
                put("testId", sourceTest.testId); put("instruction", sourceTest.instruction)
                put("currentIndex", state.currentIndex); put("itemCount", state.test.items.size)
                put("submitted", state.submitted); put("isFirst", state.currentIndex == 0)
                put("isLast", state.currentIndex == state.test.items.lastIndex)
                put("itemId", item.itemId); put("questionType", item.questionType.tag)
                put("context", item.context); put("gap", item.gap?.let(::JsonPrimitive) ?: JsonNull)
                put("questionTarget", item.questionTarget?.let(::JsonPrimitive) ?: JsonNull)
                put("options", JsonArray(item.options.map(::JsonPrimitive)))
                put("bankTokens", JsonArray(sourceTest.bankTokens.map(::bankTokenJson)))
                put("answer", state.answers[item.itemId].orEmpty())
                put("selectedBankTokenId", selectedBankTokenByItemId[item.itemId]?.let(::JsonPrimitive) ?: JsonNull)
                put("maximumQuarter", item.maximumQuarter)
                val result = state.result
                if (result != null) {
                    put("confirmedQuarter", result.earnedQuarterPoints)
                    put("manualPendingQuarter", result.manualPendingQuarterPoints)
                    put("possibleQuarter", result.possibleQuarterPoints)
                    put("confirmedPoints", formatQuarterPoints(result.earnedQuarterPoints))
                    put("pendingPoints", formatQuarterPoints(result.manualPendingQuarterPoints))
                    put("possiblePoints", formatQuarterPoints(result.possibleQuarterPoints))
                    put("final", result.isFinal)
                    put("percentage", if (result.isFinal) JsonPrimitive(result.percentage.toInt()) else JsonNull)
                    put("review", JsonArray(state.test.items.map { reviewItem ->
                        val grade = GrammarExamScorer.gradeInAttempt(state.test, reviewItem, state.answers)
                        buildJsonObject {
                            put("itemId", reviewItem.itemId); put("context", reviewItem.context)
                            put("answer", state.answers[reviewItem.itemId].orEmpty())
                            put("status", grade.status.name)
                            put("confirmedQuarter", grade.confirmedQuarter)
                            put("pendingQuarter", grade.pendingQuarter)
                            put("maximumQuarter", reviewItem.maximumQuarter)
                            put("officialExamples", JsonArray((listOf(reviewItem.correctAnswer) + reviewItem.scoredVariants.map { it.answer }).map(::JsonPrimitive)))
                        }
                    }))
                }
            })
        } else put("grammar", JsonNull)
    }.toString()
}

private fun mergedGrammar(exam: OfficialExamEntry, tests: List<GrammarTest>): GrammarTest {
    val tokens = tests.flatMap { it.bankTokens }.mapIndexed { index, token -> token.copy(position = index + 1) }
    return GrammarTest(exam.routeId, tests.first().instruction, tests.flatMap { it.items }, tokens)
}

private fun bankTokenJson(token: GrammarBankToken) = buildJsonObject {
    put("tokenId", token.tokenId); put("position", token.position); put("text", token.text)
}
