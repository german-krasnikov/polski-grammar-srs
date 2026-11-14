package polski.ui

import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.time.Clock
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import polski.mission.MissionOutcome
import polski.mission.MissionSession
import polski.mission.data.MISSION_ROW_ROWS
import polski.mission.data.MissionNodeRowOutcome
import polski.platform.WebMissionRepository

/**
 * M9 blueprint MVP: a mission list, one node (situation/prompt/choices) at a time, and a
 * completion screen — the session/document logic all lives in shared `polski.mission`; this
 * controller is pure DOM rendering plus the one coroutine scope [MissionSession]'s suspend
 * `start`/`complete` need (its own, closed with the controller — same ownership shape
 * `TrainingStore`'s own child scope already has).
 */
internal class MissionWebController {
    private val scope = CoroutineScope(SupervisorJob())
    private val session = MissionSession(WebMissionRepository()) { Clock.System.now() }
    private var selectedMissionId: String? = null
    private var selectedChoiceId: String? = null
    private var draft: String = ""
    private var closed = false

    fun close() {
        closed = true
        scope.cancel()
    }

    /** Resets to the mission list — called whenever the host navigates away from this route, same
     *  "deactivate on route change" shape `VocabularyWebController.deactivate` already has. */
    fun deactivate() {
        selectedMissionId = null
        selectedChoiceId = null
        draft = ""
    }

    fun render(container: HTMLElement, refresh: () -> Unit) {
        val missionId = selectedMissionId
        if (missionId == null) {
            renderMissionList(container, refresh)
            return
        }
        val state = session.state.value
        state.error?.let { error ->
            container.appendChild(node("p", "notice", error).apply { setAttribute("role", "alert") })
        }
        if (state.missionId != missionId) {
            // start() is still in flight (or failed) — nothing more to render yet.
            container.appendChild(node("p", "muted", "Загружаем миссию…"))
            return
        }
        if (state.completed) renderCompletion(container, refresh) else renderNode(container, refresh)
    }

    private fun renderMissionList(container: HTMLElement, refresh: () -> Unit) {
        container.appendChild(node("h2", "mission-title", "Мини-миссии"))
        container.appendChild(node("p", "muted", "Короткий диалог с целью: разговор продвигается по смыслу твоего ответа, форма проверяется отдельно."))
        container.appendChild(node("div", "mission-list").apply {
            MISSION_ROW_ROWS.forEach { mission ->
                appendChild(node("div", "mission-card").apply {
                    appendChild(node("div", "mission-card-title", mission.title))
                    appendChild(node("p", "mission-card-goal", mission.goal))
                    appendChild(missionButton("Начать", primary = true) { selectMission(mission.missionId, refresh) })
                })
            }
        })
    }

    private fun selectMission(missionId: String, refresh: () -> Unit) {
        if (closed) return
        selectedMissionId = missionId
        selectedChoiceId = null
        draft = ""
        scope.launch {
            session.start(missionId)
            refresh()
        }
    }

    private fun renderNode(container: HTMLElement, refresh: () -> Unit) {
        val state = session.state.value
        container.appendChild(node("h2", "mission-title", "Миссия"))
        state.lastResult?.let { renderLastResult(container, it) }
        container.appendChild(node("p", "mission-situation", state.situation).apply { setAttribute("lang", "pl") })
        container.appendChild(node("p", "mission-prompt", state.prompt))
        val choiceId = selectedChoiceId
        if (choiceId == null) {
            container.appendChild(node("div", "mission-choices").apply {
                state.choices.forEach { choice ->
                    appendChild(missionButton(choice.label, cls = "mission-choice-btn") {
                        selectedChoiceId = choice.choiceId
                        refresh()
                    })
                }
            })
        } else {
            val input = (document.createElement("input") as HTMLInputElement).apply {
                type = "text"; id = "mission-answer-input"; className = "exam-input"
                setAttribute("aria-label", "Ответ по-польски"); setAttribute("lang", "pl")
                value = draft
                addEventListener("input", { draft = this.value })
            }
            container.appendChild(node("div", "mission-answer-wrap").apply {
                appendChild(input)
                appendChild(missionButton("Проверить", primary = true, cls = "mission-check-btn") {
                    session.choose(choiceId, draft)
                    selectedChoiceId = null
                    draft = ""
                    refresh()
                })
            })
        }
    }

    private fun renderLastResult(container: HTMLElement, result: MissionOutcome) {
        container.appendChild(node("div", "mission-result").apply {
            setAttribute("role", "status")
            appendChild(node(
                "span", "mission-result-meaning " + (if (result.meaningCorrect) "correct" else "incorrect"),
                if (result.meaningCorrect) "Смысл: приближает к цели" else "Смысл: не приближает к цели",
            ))
            appendChild(node(
                "span", "mission-result-form " + (if (result.formCorrect) "correct" else "incorrect"),
                if (result.formCorrect) "Форма: верно" else "Форма: неверно",
            ))
        })
    }

    private fun renderCompletion(container: HTMLElement, refresh: () -> Unit) {
        val state = session.state.value
        val reachedGoal = state.outcome == MissionNodeRowOutcome.GOAL_REACHED
        container.appendChild(node("h2", "mission-title", if (reachedGoal) "Цель достигнута" else "Миссия окончена"))
        container.appendChild(node("p", "mission-situation", state.situation).apply { setAttribute("lang", "pl") })
        container.appendChild(node("p", "mission-outcome", if (reachedGoal) "Ты добрался до цели." else "Не в этот раз — попробуй другой путь."))
        container.appendChild(missionButton("Готово", primary = true) {
            scope.launch {
                session.complete()
                deactivate()
                refresh()
            }
        })
    }

    private fun missionButton(label: String, primary: Boolean = false, cls: String = "", action: () -> Unit): HTMLButtonElement =
        (document.createElement("button") as HTMLButtonElement).apply {
            type = "button"
            className = "exam-nav-btn $cls" + (if (primary) " primary" else "")
            textContent = label
            addEventListener("click", { action() })
        }
}
