package polski.ui

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import polski.exam.ExamModule
import polski.exam.ExamCatalog
import polski.exam.grammar.GrammarExamAction
import polski.exam.listening.ListeningExamAction
import polski.exam.reading.ReadingExamAction
import polski.exam.writing.WritingExamAction
import polski.exam.session.*
import polski.presentation.UiCopy
import kotlin.time.Clock

internal data class ExamEntry(
    val id: String, val label: String, val level: String, val year: String,
) {
    val grammarCount: Int get() = examGrammarData[id]?.sumOf { it.items.size } ?: 0
    val readingCount: Int get() = examReadingData[id]?.questions?.size ?: 0
    val listeningCount: Int get() = examListeningData[id]?.tasks?.sumOf { task -> task.questions.sumOf { it.answerSlotCount } } ?: 0
    val writingCount: Int get() = examWritingData[id]?.tasks?.size ?: 0
}

private val exams = ExamCatalog.entries.map { ExamEntry(it.routeId, it.displayTitle, it.level, it.year.toString()) }

internal enum class ExamScreen { List, Modules, Grammar, Reading, Listening, Writing, Timed }

internal class ExamWebController {
    var immediateFeedbackEnabled: () -> Boolean = { true }
    /** M10 blueprint: invoked with (examRouteId, itemId, skillId) when the review screen's
     *  "Потренировать навык" button is clicked. */
    var onTrainSkill: ((examRouteId: String, itemId: String, skillId: String) -> Unit)? = null
    private var screen = ExamScreen.List
    private var selectedExam: ExamEntry? = null
    private var grammarCtrl: ExamGrammarController? = null
    private var readingCtrl: ExamReadingController? = null
    private var listeningCtrl: ExamListeningController? = null
    private var writingCtrl: ExamWritingController? = null
    private var timedState: ExamSessionState? = null
    private var timedInterval: Int? = null
    private val timedRefresh: (Event) -> Unit = { refreshTimed() }
    private var root: HTMLElement? = null
    /** M10 blueprint AC3/AC4: set by [openVerification], consumed once by [renderExamList]. */
    private var verificationNotice: String? = null

    fun close() {
        stopTimed()
        screen = ExamScreen.List
    }

    /**
     * M10 blueprint: the host's reaction to `UiEffect.ExamVerificationReady` — a non-null
     * [verificationItemId] jumps straight into that exam's grammar review on that item; `null`
     * (AC4's degrade case, an under-authored skill) instead shows an explicit "not enough linked
     * questions yet" notice on the exam list, never a silent repeat of the original item.
     */
    fun openVerification(examRouteId: String, skillId: String, verificationItemId: String?) {
        val entry = exams.firstOrNull { it.id == examRouteId }
        stopTimed()
        if (verificationItemId == null || entry == null) {
            selectedExam = null
            screen = ExamScreen.List
            verificationNotice = "Для навыка «$skillId» пока не хватает связанных вопросов для проверки — скоро добавим новый вопрос."
            // No `redraw()` here: at this moment `root` (if set at all) is whatever Exam-route
            // container was live the LAST time this route was rendered — possibly already
            // detached (the host navigated away to Training in between). Mutating `screen`/
            // `verificationNotice` is enough; the host's own pending `navigate(WebRoute.Exam)`
            // (called around this same effect) drives the real `render(container)` + `redraw()`
            // once Compose recomposes onto a live container. Redrawing eagerly here would let
            // `renderExamList`'s own "show once, then clear" consume `verificationNotice` against
            // a detached node nobody ever sees, leaving nothing to show on the real redraw.
            return
        }
        selectedExam = entry
        grammarCtrl = ExamGrammarController(
            examRouteId, onBack = { grammarCtrl?.close(); grammarCtrl = null; screen = ExamScreen.Modules; redraw() },
            immediateFeedbackEnabled = immediateFeedbackEnabled, startAtItemId = verificationItemId,
            onTrainSkill = { itemId, linkedSkillId -> onTrainSkill?.invoke(examRouteId, itemId, linkedSkillId) },
        )
        screen = ExamScreen.Grammar
    }

    fun render(container: HTMLElement) { root = container; redraw() }

    private fun redraw() {
        val c = root ?: return; c.innerHTML = ""
        when (screen) {
            ExamScreen.List -> renderExamList(c)
            ExamScreen.Modules -> renderModuleList(c)
            ExamScreen.Grammar -> grammarCtrl?.render(c)
            ExamScreen.Reading -> readingCtrl?.render(c)
            ExamScreen.Listening -> listeningCtrl?.render(c)
            ExamScreen.Writing -> writingCtrl?.render(c)
            ExamScreen.Timed -> renderTimed(c)
        }
    }

    private fun renderExamList(c: HTMLElement) {
        c.appendChild(node("h2", "exam-title", UiCopy.SelectExam))
        verificationNotice?.let { notice ->
            c.appendChild(node("p", "exam-verification-notice", notice).apply { setAttribute("role", "status") })
            verificationNotice = null
        }
        c.appendChild(node("div", "exam-list").apply {
            exams.forEach { exam ->
                appendChild(node("div", "exam-card").apply {
                    appendChild(node("div", "exam-card-header").apply {
                        appendChild(node("span", "exam-level-badge", exam.level))
                        appendChild(node("span", "exam-card-year", exam.year))
                    })
                    appendChild(node("div", "exam-card-title", exam.label))
                    val counts = buildList {
                        if (exam.grammarCount > 0) add("${UiCopy.Grammar}: ${exam.grammarCount}")
                        if (exam.readingCount > 0) add("${UiCopy.Reading}: ${exam.readingCount}")
                        if (exam.listeningCount > 0) add("${UiCopy.Listening}: ${exam.listeningCount}")
                        if (exam.writingCount > 0) add("${UiCopy.Writing}: ${exam.writingCount}")
                    }
                    appendChild(node("div", "exam-card-counts", counts.joinToString(" · ")))
                    appendChild((document.createElement("button") as HTMLButtonElement).apply {
                        type = "button"; className = "exam-nav-btn primary"; textContent = UiCopy.Start
                        addEventListener("click", { selectExam(exam) })
                    })
                })
            }
        })
    }

    private fun selectExam(exam: ExamEntry) { selectedExam = exam; screen = ExamScreen.Modules; redraw() }

    private fun renderModuleList(c: HTMLElement) {
        val exam = selectedExam ?: return
        c.appendChild(node("h2", "exam-title", exam.label))
        c.appendChild(node("div", "exam-module-list").apply {
            if (examSessionData.containsKey(exam.id)) {
                appendChild((document.createElement("button") as HTMLButtonElement).apply {
                    type = "button"; className = "exam-nav-btn primary"; textContent = UiCopy.ExamTimedStart
                    addEventListener("click", { startTimed(exam.id) })
                })
            }
            moduleRow(this, "I. Rozumienie ze słuchu", exam.listeningCount, exam.listeningCount > 0) {
                close()
                listeningCtrl = ExamListeningController(exam.id, onBack = { close(); screen = ExamScreen.Modules; redraw() }, immediateFeedbackEnabled = immediateFeedbackEnabled)
                screen = ExamScreen.Listening; redraw()
            }
            moduleRow(this, "II. Rozumienie tekstów pisanych", exam.readingCount, exam.readingCount > 0) {
                readingCtrl = ExamReadingController(exam.id, onBack = { readingCtrl?.close(); screen = ExamScreen.Modules; redraw() }, immediateFeedbackEnabled = immediateFeedbackEnabled)
                screen = ExamScreen.Reading; redraw()
            }
            moduleRow(this, "III. Poprawność gramatyczna", exam.grammarCount, exam.grammarCount > 0) {
                grammarCtrl?.close()
                grammarCtrl = ExamGrammarController(
                    exam.id, onBack = { grammarCtrl?.close(); screen = ExamScreen.Modules; redraw() },
                    immediateFeedbackEnabled = immediateFeedbackEnabled,
                    onTrainSkill = { itemId, skillId -> onTrainSkill?.invoke(exam.id, itemId, skillId) },
                )
                screen = ExamScreen.Grammar; redraw()
            }
            moduleRow(this, "IV. Pisanie", exam.writingCount, exam.writingCount > 0) {
                writingCtrl = ExamWritingController(exam.id, onBack = { screen = ExamScreen.Modules; redraw() })
                screen = ExamScreen.Writing; redraw()
            }
        })
        c.appendChild(node("div", "exam-nav").apply {
            appendChild((document.createElement("button") as HTMLButtonElement).apply {
                type = "button"; className = "exam-nav-btn"; textContent = "← ${UiCopy.SelectExam}"
                addEventListener("click", { screen = ExamScreen.List; redraw() })
            })
        })
    }

    private fun nowEpochMs(): Long = Clock.System.now().toEpochMilliseconds()

    private fun stopTimed() {
        timedInterval?.let(window::clearInterval)
        timedInterval = null
        document.removeEventListener("visibilitychange", timedRefresh)
        window.removeEventListener("focus", timedRefresh)
        timedState = null
        listeningCtrl?.close()
        listeningCtrl = null
        readingCtrl?.close()
        readingCtrl = null
        grammarCtrl?.close()
        grammarCtrl = null
        writingCtrl?.close()
        writingCtrl = null
    }

    private fun startTimed(examId: String) {
        val config = examSessionData[examId] ?: return
        stopTimed()
        timedState = reduceExamSession(ExamSessionState(config), ExamSessionAction.Start(nowEpochMs()))
        screen = ExamScreen.Timed
        timedInterval = window.setInterval({ refreshTimed(); null }, 1_000)
        document.addEventListener("visibilitychange", timedRefresh)
        window.addEventListener("focus", timedRefresh)
        redraw()
    }

    private fun updateTimed(action: ExamSessionAction) {
        val before = timedState ?: return
        val after = reduceExamSession(before, action)
        if (after === before) return
        timedState = after
        if (after.currentModuleIndex != before.currentModuleIndex || after.completed != before.completed) {
            if (after.completed) {
                timedInterval?.let(window::clearInterval)
                timedInterval = null
                document.removeEventListener("visibilitychange", timedRefresh)
                window.removeEventListener("focus", timedRefresh)
            }
            listeningCtrl?.close(); listeningCtrl = null
            readingCtrl?.close(); readingCtrl = null
            grammarCtrl?.close(); grammarCtrl = null
            writingCtrl?.close(); writingCtrl = null
            redraw()
        } else updateTimedClock()
    }

    private fun refreshTimed() {
        val state = timedState ?: return
        updateTimed(ExamSessionAction.Tick(nowEpochMs()))
        if (timedState === state) updateTimedClock()
    }

    private fun updateTimedClock() {
        val state = timedState ?: return
        val card = examSessionCardModel(state, nowEpochMs())
        root?.querySelector(".exam-timed-clock")?.textContent =
            "${card.remainingMs / 60_000}:${(card.remainingMs / 1_000 % 60).toString().padStart(2, '0')}"
    }

    private fun timedPartAction(module: ExamModule, action: ExamPartAction) {
        updateTimed(ExamSessionAction.PartAction(module, action, nowEpochMs()))
    }

    private fun finishTimedPart(module: ExamModule) {
        updateTimed(ExamSessionAction.FinishPart(module, nowEpochMs()))
    }

    private fun renderTimed(c: HTMLElement) {
        val state = timedState ?: return
        val card = examSessionCardModel(state, nowEpochMs())
        if (state.completed) {
            c.appendChild(node("h2", "exam-title", UiCopy.ExamTimedComplete))
            c.appendChild(node("div", "exam-score", "${UiCopy.ExamConfirmedPoints}: ${polski.exam.formatQuarterPoints(card.confirmedQuarter)} / ${polski.exam.formatQuarterPoints(card.possibleQuarter)}; ${UiCopy.ExamPendingPoints}: ${polski.exam.formatQuarterPoints(card.manualPendingQuarter)}"))
            c.appendChild((document.createElement("button") as HTMLButtonElement).apply {
                type = "button"; className = "exam-nav-btn"; textContent = UiCopy.ExamBackToModules
                addEventListener("click", { stopTimed(); screen = ExamScreen.Modules; redraw() })
            })
            return
        }
        c.appendChild(node("div", "exam-progress", "${UiCopy.ExamTimedPart} ${card.position} / 4"))
        c.appendChild(node("div", "exam-timed-clock").apply { setAttribute("role", "timer") })
        c.appendChild((document.createElement("button") as HTMLButtonElement).apply {
            type = "button"; className = "exam-nav-btn"; textContent = UiCopy.ExamBackToModules
            addEventListener("click", { stopTimed(); screen = ExamScreen.Modules; redraw() })
        })
        updateTimedClock()
        val part = node("div", "exam-timed-part")
        c.appendChild(part)
        val examId = selectedExam?.id ?: return
        when (card.currentModule) {
            ExamModule.Listening -> {
                val controller = ExamListeningController(examId, {}, { requireNotNull(timedState).listening },
                    { timedPartAction(ExamModule.Listening, ExamPartAction.Listening(it)) }, { finishTimedPart(ExamModule.Listening) })
                listeningCtrl = controller; controller.render(part)
            }
            ExamModule.Reading -> {
                val controller = ExamReadingController(examId, {}, { requireNotNull(timedState).reading },
                    { timedPartAction(ExamModule.Reading, ExamPartAction.Reading(it)) }, { finishTimedPart(ExamModule.Reading) })
                readingCtrl = controller; controller.render(part)
            }
            ExamModule.Grammar -> {
                val controller = ExamGrammarController(examId, {}, { requireNotNull(timedState).grammar },
                    { timedPartAction(ExamModule.Grammar, ExamPartAction.Grammar(it)) }, { finishTimedPart(ExamModule.Grammar) })
                grammarCtrl = controller; controller.render(part)
            }
            ExamModule.Writing -> {
                val controller = ExamWritingController(examId, {}, { requireNotNull(timedState).writing },
                    { timedPartAction(ExamModule.Writing, ExamPartAction.Writing(it)) }, { finishTimedPart(ExamModule.Writing) })
                writingCtrl = controller; controller.render(part)
            }
            null -> Unit
        }
    }

    private fun moduleRow(parent: HTMLElement, label: String, count: Int, available: Boolean, onClick: () -> Unit) {
        parent.appendChild(node("div", "exam-module-card").apply {
            appendChild(node("div", "exam-module-name", label))
            if (count > 0) appendChild(node("div", "exam-module-count", "$count"))
            appendChild((document.createElement("button") as HTMLButtonElement).apply {
                type = "button"
                className = if (available) "exam-nav-btn primary" else "exam-nav-btn"
                textContent = if (available) UiCopy.Start else UiCopy.ComingSoon
                disabled = !available
                addEventListener("click", { if (available) onClick() })
            })
        })
    }
}
