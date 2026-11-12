package polski.ios

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import polski.exam.ExamPartScore
import polski.exam.ExamCatalog
import polski.exam.ExamModule
import polski.exam.OfficialExamEntry
import polski.exam.formatQuarterPoints
import polski.exam.officialGrammarData
import polski.exam.officialListeningData
import polski.exam.officialReadingData
import polski.exam.officialWritingData
import polski.exam.grammar.mergeGrammarTests
import polski.exam.grammar.GrammarBankToken
import polski.exam.grammar.GrammarExamAction
import polski.exam.grammar.GrammarExamScorer
import polski.exam.grammar.GrammarExamState
import polski.exam.grammar.GrammarItem
import polski.exam.grammar.GrammarTest
import polski.exam.grammar.reduceGrammarExam
import polski.exam.listening.*
import polski.exam.reading.*
import polski.exam.writing.*
import polski.exam.session.*

/** Scene-owned native exam state. Swift receives presentation snapshots and sends semantic actions;
 *  shared Kotlin owns answers and grades. A temporary tab switch leaves this session intact. */
class IosExamSession {
    private var selectedExam: OfficialExamEntry? = null
    private var grammarTests: List<GrammarTest> = emptyList()
    private var grammarState: GrammarExamState? = null
    private var readingState: ReadingExamState? = null
    private var listeningState: ListeningExamState? = null
    private var writingState: WritingExamState? = null
    private var selectedModule: ExamModule? = null
    private var timedState: ExamSessionState? = null
    private var lastNowEpochMs: Long = 0
    private val selectedBankTokenByItemId = mutableMapOf<String, String>()
    private var errorCode: String? = null

    fun selectExam(routeId: String) {
        val exam = ExamCatalog.find(routeId)
        if (exam == null) { errorCode = "unknown_exam"; return }
        selectedExam = exam
        grammarTests = officialGrammarData.getValue(routeId)
        grammarState = null
        readingState = null; listeningState = null; writingState = null
        selectedModule = null; timedState = null
        selectedBankTokenByItemId.clear()
        errorCode = null
    }

    fun selectModule(module: String) {
        if (selectedExam == null) { errorCode = "no_exam"; return }
        if (timedState != null) { errorCode = "timed_sitting_active"; return }
        val selected = ExamModule.entries.firstOrNull { it.name.lowercase() == module }
            ?: run { errorCode = "unknown_module"; return }
        selectedModule = selected
        val routeId = requireNotNull(selectedExam).routeId
        when (selected) {
            ExamModule.Grammar -> if (grammarState == null || grammarState?.submitted == true) grammarState = GrammarExamState(mergeGrammarTests(requireNotNull(selectedExam).routeId, grammarTests))
            ExamModule.Reading -> if (readingState == null || readingState?.submitted == true) readingState = ReadingExamState(officialReadingData.getValue(routeId))
            ExamModule.Listening -> if (listeningState == null || listeningState?.submitted == true) listeningState = ListeningExamState(officialListeningData.getValue(routeId))
            ExamModule.Writing -> if (writingState == null || writingState?.submitted == true) writingState = WritingExamState(officialWritingData.getValue(routeId))
        }
        selectedBankTokenByItemId.clear()
        errorCode = null
    }

    fun startTimed(nowEpochMs: Long) {
        val exam = selectedExam ?: run { errorCode = "no_exam"; return }
        if (timedState == null) {
            selectedBankTokenByItemId.clear()
            val config = ExamSessionConfig(
                exam.routeId,
                exam.modules.map { ExamModuleConfig(it.module, it.durationSeconds, it.maximumQuarter) },
                officialListeningData.getValue(exam.routeId), officialReadingData.getValue(exam.routeId),
                grammarTests, officialWritingData.getValue(exam.routeId),
            )
            timedState = reduceExamSession(ExamSessionState(config), ExamSessionAction.Start(nowEpochMs))
        }
        lastNowEpochMs = nowEpochMs
        errorCode = null
    }

    fun tick(nowEpochMs: Long) {
        lastNowEpochMs = nowEpochMs
        timedState = timedState?.let { reduceExamSession(it, ExamSessionAction.Tick(nowEpochMs)) }
    }

    fun answer(itemId: String, value: String) {
        answerAt(itemId, value, lastNowEpochMs)
    }

    fun answerAt(itemId: String, value: String, nowEpochMs: Long) {
        if (timedState != null) { errorCode = "origin_required"; return }
        val module = selectedModule ?: run { errorCode = "no_module"; return }
        answerInModule(module.name.lowercase(), itemId, value, nowEpochMs)
    }

    /** The module is the control's rendered origin, even if a deadline has since advanced. */
    fun answerInModule(module: String, itemId: String, value: String, nowEpochMs: Long) {
        lastNowEpochMs = nowEpochMs
        val origin = moduleFromTag(module) ?: return
        if (!knownItem(origin, itemId)) { errorCode = "unknown_item"; return }
        val timed = timedState
        if (timed != null) {
            val action = when (origin) {
                ExamModule.Grammar -> ExamPartAction.Grammar(GrammarExamAction.Answer(itemId, value))
                ExamModule.Reading -> ExamPartAction.Reading(ReadingExamAction.Answer(itemId, value))
                ExamModule.Listening -> ExamPartAction.Listening(ListeningExamAction.Answer(itemId, value))
                ExamModule.Writing -> ExamPartAction.Writing(WritingExamAction.Answer(itemId, value))
            }
            timedState = reduceExamSession(timed, ExamSessionAction.PartAction(origin, action, nowEpochMs))
            errorCode = null
            return
        }
        if (selectedModule != origin) { errorCode = "stale_module"; return }
        when (origin) {
            ExamModule.Reading -> { readingState = readingState?.let { reduceReadingExam(it, ReadingExamAction.Answer(itemId, value)) }; errorCode = null; return }
            ExamModule.Listening -> { listeningState = listeningState?.let { reduceListeningExam(it, ListeningExamAction.Answer(itemId, value)) }; errorCode = null; return }
            ExamModule.Writing -> { writingState = writingState?.let { reduceWritingExam(it, WritingExamAction.Answer(itemId, value)) }; errorCode = null; return }
            ExamModule.Grammar -> Unit
        }
        val state = grammarState ?: run { errorCode = "no_grammar"; return }
        if (state.test.items.none { it.itemId == itemId }) { errorCode = "unknown_item"; return }
        grammarState = reduceGrammarExam(state, GrammarExamAction.Answer(itemId, value))
        errorCode = null
    }

    /** Keeps token identity even when a source bank contains repeated visible words. */
    fun answerBankToken(itemId: String, tokenId: String) {
        answerBankTokenAt(itemId, tokenId, lastNowEpochMs)
    }

    fun answerBankTokenAt(itemId: String, tokenId: String, nowEpochMs: Long) {
        val sourceTest = grammarTests.singleOrNull { test -> test.items.any { it.itemId == itemId } }
        val token = sourceTest?.bankTokens?.singleOrNull { it.tokenId == tokenId }
        if (token == null) { errorCode = "unknown_token"; return }
        val before = timedState?.grammar ?: grammarState
        answerInModule("grammar", itemId, token.text, nowEpochMs)
        if (errorCode == null && (timedState?.grammar ?: grammarState) !== before) selectedBankTokenByItemId[itemId] = tokenId
    }

    fun next() = navigate(true, lastNowEpochMs)
    fun previous() = navigate(false, lastNowEpochMs)
    fun submit() = submitAt(lastNowEpochMs)

    fun nextAt(nowEpochMs: Long) = navigate(true, nowEpochMs)
    fun previousAt(nowEpochMs: Long) = navigate(false, nowEpochMs)
    fun submitAt(nowEpochMs: Long) {
        if (timedState != null) { errorCode = "origin_required"; return }
        val module = selectedModule ?: run { errorCode = "no_module"; return }
        submitInModule(module.name.lowercase(), nowEpochMs)
    }

    fun submitInModule(module: String, nowEpochMs: Long) {
        lastNowEpochMs = nowEpochMs
        val origin = moduleFromTag(module) ?: return
        val timed = timedState
        if (timed != null) {
            timedState = reduceExamSession(timed, ExamSessionAction.FinishPart(origin, nowEpochMs))
            errorCode = null
            return
        }
        if (selectedModule != origin) { errorCode = "stale_module"; return }
        when (origin) {
            ExamModule.Reading -> readingState = readingState?.let { reduceReadingExam(it, ReadingExamAction.Submit) }
            ExamModule.Listening -> listeningState = listeningState?.let { reduceListeningExam(it, ListeningExamAction.Submit) }
            ExamModule.Writing -> writingState = writingState?.let { reduceWritingExam(it, WritingExamAction.Submit) }
            ExamModule.Grammar -> update(GrammarExamAction.Submit)
        }
        errorCode = null
    }

    fun selectWritingSet(setId: String?) {
        selectWritingSetAt(setId, lastNowEpochMs)
    }

    fun selectWritingSetAt(setId: String?, nowEpochMs: Long) {
        lastNowEpochMs = nowEpochMs
        val exam = selectedExam ?: run { errorCode = "no_exam"; return }
        if (setId != null && setId !in officialWritingData.getValue(exam.routeId).sets) {
            errorCode = "invalid_set"; return
        }
        val timed = timedState
        if (timed != null) {
            timedState = reduceExamSession(timed, ExamSessionAction.PartAction(ExamModule.Writing,
                ExamPartAction.Writing(WritingExamAction.SelectSet(setId)), nowEpochMs))
        } else if (selectedModule == ExamModule.Writing) {
            writingState = writingState?.let { reduceWritingExam(it, WritingExamAction.SelectSet(setId)) }
        } else { errorCode = "stale_module"; return }
        errorCode = null
    }

    private fun navigate(forward: Boolean, nowEpochMs: Long) {
        if (timedState != null) { errorCode = "origin_required"; return }
        val module = selectedModule ?: run { errorCode = "no_module"; return }
        navigateInModule(module.name.lowercase(), forward, nowEpochMs)
    }

    fun nextInModule(module: String, nowEpochMs: Long) = navigateInModule(module, true, nowEpochMs)
    fun previousInModule(module: String, nowEpochMs: Long) = navigateInModule(module, false, nowEpochMs)

    private fun navigateInModule(module: String, forward: Boolean, nowEpochMs: Long) {
        lastNowEpochMs = nowEpochMs
        val origin = moduleFromTag(module) ?: return
        val timed = timedState
        if (timed != null) {
            val action = when (origin) {
                ExamModule.Grammar -> ExamPartAction.Grammar(if (forward) GrammarExamAction.Next else GrammarExamAction.Previous)
                ExamModule.Reading -> ExamPartAction.Reading(if (forward) ReadingExamAction.Next else ReadingExamAction.Previous)
                ExamModule.Listening -> ExamPartAction.Listening(if (forward) ListeningExamAction.Next else ListeningExamAction.Previous)
                ExamModule.Writing -> ExamPartAction.Writing(if (forward) WritingExamAction.Next else WritingExamAction.Previous)
            }
            timedState = reduceExamSession(timed, ExamSessionAction.PartAction(origin, action, nowEpochMs))
            errorCode = null
            return
        }
        if (selectedModule != origin) { errorCode = "stale_module"; return }
        when (origin) {
            ExamModule.Grammar -> update(if (forward) GrammarExamAction.Next else GrammarExamAction.Previous)
            ExamModule.Reading -> readingState = readingState?.let { reduceReadingExam(it, if (forward) ReadingExamAction.Next else ReadingExamAction.Previous) }
            ExamModule.Listening -> listeningState = listeningState?.let { reduceListeningExam(it, if (forward) ListeningExamAction.Next else ListeningExamAction.Previous) }
            ExamModule.Writing -> writingState = writingState?.let { reduceWritingExam(it, if (forward) WritingExamAction.Next else WritingExamAction.Previous) }
        }
        errorCode = null
    }

    private fun moduleFromTag(tag: String): ExamModule? = ExamModule.entries
        .firstOrNull { it.name.lowercase() == tag }
        .also { if (it == null) errorCode = "unknown_module" }

    private fun knownItem(module: ExamModule, itemId: String): Boolean {
        val routeId = selectedExam?.routeId ?: return false
        return when (module) {
            ExamModule.Grammar -> grammarTests.any { test -> test.items.any { it.itemId == itemId } }
            ExamModule.Reading -> officialReadingData.getValue(routeId).questions.any { it.id == itemId }
            ExamModule.Listening -> officialListeningData.getValue(routeId).tasks.any { task -> task.questions.any { it.id == itemId } }
            ExamModule.Writing -> officialWritingData.getValue(routeId).tasks.any { it.id == itemId }
        }
    }

    private fun update(action: GrammarExamAction) {
        val state = grammarState ?: run { errorCode = "no_grammar"; return }
        grammarState = reduceGrammarExam(state, action)
        errorCode = null
    }

    /** Explicitly leave a part; selecting another app tab does not call this. */
    fun exitGrammar() { selectedModule = null; selectedBankTokenByItemId.clear(); errorCode = null }

    /** Explicit scene disposal or new sitting. */
    fun close() { selectedExam = null; grammarTests = emptyList(); grammarState = null; readingState = null; listeningState = null; writingState = null; selectedModule = null; timedState = null; selectedBankTokenByItemId.clear(); errorCode = null }

    fun currentSnapshot(): String = buildJsonObject {
        val timed = timedState
        val activeModule = if (timed == null) selectedModule else timed.config.modules.getOrNull(timed.currentModuleIndex)?.module
        val activeGrammar = timed?.grammar ?: grammarState
        put("screen", when {
            timed?.completed == true -> "sittingResult"
            activeModule == ExamModule.Grammar && activeGrammar?.submitted == true -> "grammarResult"
            activeModule == ExamModule.Grammar -> "grammar"
            activeModule != null -> "part"
            selectedExam != null -> "modules"
            else -> "catalog"
        })
        put("errorCode", errorCode?.let(::JsonPrimitive) ?: JsonNull)
        put("catalog", JsonArray(ExamCatalog.entries.map { exam -> buildJsonObject {
            put("routeId", exam.routeId); put("sourceExamId", exam.sourceExamId)
            put("provider", exam.provider); put("targetLanguage", exam.targetLanguage)
            put("level", exam.level); put("year", exam.year)
            put("displayTitle", exam.displayTitle)
            put("grammarItemCount", exam.grammarItemCount)
        } }))
        put("selectedExamId", selectedExam?.routeId?.let(::JsonPrimitive) ?: JsonNull)
        put("modules", JsonArray(selectedExam?.modules.orEmpty().map { cover -> buildJsonObject {
            put("module", cover.module.name.lowercase()); put("order", cover.order)
            put("durationSeconds", cover.durationSeconds); put("maximumQuarter", cover.maximumQuarter)
            put("availableOnIpad", true)
        } }))
        put("sitting", if (timed == null) JsonNull else buildJsonObject {
            val card = examSessionCardModel(timed, lastNowEpochMs)
            put("module", card.currentModule?.name?.lowercase()?.let(::JsonPrimitive) ?: JsonNull)
            put("position", card.position); put("completed", timed.completed)
            put("deadlineEpochMs", timed.deadlineEpochMs?.let(::JsonPrimitive) ?: JsonNull)
            put("remainingMs", card.remainingMs)
            put("confirmedQuarter", card.confirmedQuarter); put("manualPendingQuarter", card.manualPendingQuarter)
            put("possibleQuarter", card.possibleQuarter)
            put("isFinal", card.isFinal); put("finalPercent", card.finalPercent?.let(::JsonPrimitive) ?: JsonNull)
            put("frozenModules", JsonArray(timed.frozenResults.keys.map { JsonPrimitive(it.name.lowercase()) }))
            put("partScores", JsonArray(timed.config.modules.mapNotNull { cover ->
                timed.frozenResults[cover.module]?.let { score -> buildJsonObject {
                    put("module", cover.module.name.lowercase())
                    put("confirmedQuarter", score.confirmedQuarter)
                    put("manualPendingQuarter", score.manualPendingQuarter)
                    put("possibleQuarter", score.possibleQuarter)
                    put("isFinal", score.isFinal)
                    put("finalPercent", score.finalPercent?.let(::JsonPrimitive) ?: JsonNull)
                } }
            }))
        })
        val part = when (activeModule) {
            ExamModule.Reading -> (timed?.reading ?: readingState)?.let(::readingJson)
            ExamModule.Listening -> (timed?.listening ?: listeningState)?.let(::listeningJson)
            ExamModule.Writing -> (timed?.writing ?: writingState)?.let(::writingJson)
            else -> null
        }
        put("part", part ?: JsonNull)
        val state = if (activeModule == ExamModule.Grammar) activeGrammar else null
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
                        grammarReviewItemJson(state.test, reviewItem, state.answers)
                    }))
                }
            })
        } else put("grammar", JsonNull)
    }.toString()
}

private fun bankTokenJson(token: GrammarBankToken) = buildJsonObject {
    put("tokenId", token.tokenId); put("position", token.position); put("text", token.text)
}

/** Pure source-backed review row; called only after the shared session submits a part. */
internal fun grammarReviewItemJson(test: GrammarTest, item: GrammarItem, answers: Map<String, String>): JsonObject {
    val grade = GrammarExamScorer.gradeInAttempt(test, item, answers)
    return buildJsonObject {
        put("itemId", item.itemId); put("context", item.context)
        put("answer", answers[item.itemId].orEmpty())
        put("status", grade.status.name)
        put("confirmedQuarter", grade.confirmedQuarter)
        put("pendingQuarter", grade.pendingQuarter)
        put("maximumQuarter", item.maximumQuarter)
        put("officialExamples", JsonArray((listOf(item.correctAnswer) + item.scoredVariants.map { it.answer }).map(::JsonPrimitive)))
        if (item.rubricComponents.isNotEmpty()) {
            put("rubricComponents", JsonArray(item.rubricComponents.sortedBy { it.order }.map { component ->
                buildJsonObject {
                    put("componentId", component.componentId)
                    put("order", component.order)
                    put("criterionText", component.criterionText)
                    put("creditQuarter", component.creditQuarter)
                    put("creditPoints", formatQuarterPoints(component.creditQuarter))
                }
            }))
        }
    }
}

private fun scoreJson(score: ExamPartScore?) = buildJsonObject {
    put("confirmedQuarter", score?.confirmedQuarter ?: 0)
    put("manualPendingQuarter", score?.manualPendingQuarter ?: 0)
    put("possibleQuarter", score?.possibleQuarter ?: 0)
    put("isFinal", score?.isFinal ?: false)
    put("finalPercent", score?.finalPercent?.let(::JsonPrimitive) ?: JsonNull)
}

private fun readingJson(state: ReadingExamState) = buildJsonObject {
    val card = readingExamCardModel(state)
    put("module", "reading"); put("id", card.passage.id)
    put("instruction", card.passage.instruction); put("text", card.passage.text)
    put("position", card.position); put("count", card.count)
    put("canPrevious", card.canPrevious); put("canNext", card.canNext)
    put("submitted", card.submitted); put("score", scoreJson(card.result))
    put("finalPercent", card.result?.finalPercent?.let(::JsonPrimitive) ?: JsonNull)
    put("questions", JsonArray(card.questions.map { question -> buildJsonObject {
        put("id", question.id); put("type", question.type.tag)
        put("text", question.questionText)
        put("options", JsonArray(question.options.map(::JsonPrimitive)))
        put("answerMode", question.answerMode.tag)
        put("answerSlotCount", question.answerSlotCount)
        put("answer", card.answers[question.id].orEmpty())
        if (card.submitted) {
            val grade = card.result?.evaluations?.get(question.id)
            put("status", grade?.status?.name?.let(::JsonPrimitive) ?: JsonNull)
            put("confirmedQuarter", grade?.confirmedQuarter ?: 0)
            put("pendingQuarter", grade?.pendingQuarter ?: 0)
            put("officialExamples", JsonArray((listOf(question.correctAnswer) + question.scoredVariants.map { it.answer }).map(::JsonPrimitive)))
        }
    } }))
}

private fun listeningJson(state: ListeningExamState) = buildJsonObject {
    val card = listeningExamCardModel(state)
    put("module", "listening"); put("id", card.task.id)
    put("instruction", card.task.instruction)
    put("position", card.position); put("count", card.count)
    put("canPrevious", card.canPrevious); put("canNext", card.canNext)
    put("submitted", card.submitted); put("score", scoreJson(card.result))
    put("finalPercent", card.result?.finalPercent?.let(::JsonPrimitive) ?: JsonNull)
    put("audioAssetId", state.exam.audioUrl.substringAfterLast('/').substringBeforeLast('.'))
    put("segmentStartMs", card.task.startMs); put("segmentEndMs", card.task.endMs)
    put("promptImage", card.task.promptImageUrl?.let(::JsonPrimitive) ?: JsonNull)
    put("promptImageAlt", card.task.promptImageAlt?.let(::JsonPrimitive) ?: JsonNull)
    put("questions", JsonArray(card.task.questions.map { question -> buildJsonObject {
        put("id", question.id); put("type", question.type.tag)
        put("text", question.questionText)
        put("options", JsonArray(question.options.map(::JsonPrimitive)))
        put("answerMode", question.answerMode.tag)
        put("answerSlotCount", question.answerSlotCount)
        put("answer", card.answers[question.id].orEmpty())
        if (card.submitted) {
            val grade = card.result?.evaluations?.get(question.id)
            put("status", grade?.status?.name?.let(::JsonPrimitive) ?: JsonNull)
            put("confirmedQuarter", grade?.confirmedQuarter ?: 0)
            put("pendingQuarter", grade?.pendingQuarter ?: 0)
            put("officialExamples", JsonArray((listOf(question.correctAnswer) + question.scoredVariants.map { it.answer }).map(::JsonPrimitive)))
        }
    } }))
}

private fun writingJson(state: WritingExamState) = buildJsonObject {
    val card = writingExamCardModel(state)
    put("module", "writing"); put("sets", JsonArray(card.sets.map(::JsonPrimitive)))
    put("selectedSet", state.selectedSetId?.let(::JsonPrimitive) ?: JsonNull)
    put("position", card.position); put("count", card.count)
    put("canPrevious", state.currentIndex > 0); put("canNext", state.currentIndex < card.count - 1)
    put("submitted", card.submitted); put("score", scoreJson(card.result))
    put("finalPercent", card.result?.finalPercent?.let(::JsonPrimitive) ?: JsonNull)
    put("taskId", card.task?.id?.let(::JsonPrimitive) ?: JsonNull)
    put("instruction", card.task?.instruction?.let(::JsonPrimitive) ?: JsonNull)
    put("text", card.task?.promptText?.let(::JsonPrimitive) ?: JsonNull)
    put("wordLimit", card.task?.wordLimit?.let(::JsonPrimitive) ?: JsonNull)
    put("promptImage", card.task?.promptImageUrl?.let(::JsonPrimitive) ?: JsonNull)
    put("promptImageAlt", card.task?.promptImageAlt?.let(::JsonPrimitive) ?: JsonNull)
    put("draft", card.draft)
    put("wordCount", card.draft.trim().split(Regex("\\s+")).count { it.isNotBlank() })
}
