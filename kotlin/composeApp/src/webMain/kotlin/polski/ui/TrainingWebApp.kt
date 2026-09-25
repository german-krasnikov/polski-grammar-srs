package polski.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.HtmlElementView
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import kotlin.random.Random
import kotlin.time.Clock
import polski.platform.BrowserLocalDayProvider
import polski.platform.browserFormatDate
import polski.platform.WebProgressRepository
import polski.platform.WebAppearance
import polski.preferences.PreferredAnswerMode
import polski.preferences.PreferredMethod
import polski.presentation.*
import polski.srs.FsrsScheduler
import polski.srs.Rating
import polski.training.ExerciseFactory
import polski.training.ExerciseIdFactory
import polski.training.RandomSource

/** One browser session, one DOM host and one listener/timer set per Compose mount. */
@Composable
@OptIn(ExperimentalComposeUiApi::class, kotlin.js.ExperimentalWasmJsInterop::class)
fun TrainingWebApp() {
    val scope = rememberCoroutineScope()
    val preferences = remember { WebPreferencesController() }
    val routes = remember { WebRouteController() }
    val appearance = remember { WebAppearance() }
    var route by remember { mutableStateOf(routes.current) }
    val store = remember {
        val scheduler = FsrsScheduler()
        var nextId = 0L
        TrainingStore(
            WebProgressRepository(scheduler), scheduler,
            ExerciseFactory(RandomSource { Random.nextDouble() }, ExerciseIdFactory { "web-${++nextId}" }),
            TimeSource {
                val at = Clock.System.now()
                TimeCapture(at, BrowserLocalDayProvider().localDay(at))
            },
            scope,
            if (preferences.value.explanationMethod == PreferredMethod.Situations) ExplanationMethod.Situations else ExplanationMethod.Logic,
            if (preferences.value.answerMode == PreferredAnswerMode.Typed) AnswerMode.Typed else AnswerMode.Oral,
        )
    }
    val state by store.state.collectAsState()
    val attemptedEffects = remember { mutableSetOf<Long>() }
    val renderer = remember { TrainingDomRenderer() }

    DisposableEffect(renderer) { onDispose { renderer.close() } }

    LaunchedEffect(store) { store.start() }
    LaunchedEffect(state.phase, state.exerciseId, preferences.value.answerMode) { preferences.applyPendingAnswerMode(store) }
    DisposableEffect(routes, appearance) {
        routes.start { destination ->
            route = destination
            destination.tab?.let { store.dispatch(AppAction.SelectTab(it)) }
        }
        routes.current.tab?.let { store.dispatch(AppAction.SelectTab(it)) }
        appearance.start()
        onDispose { routes.close(); appearance.close() }
    }
    appearance.update(preferences.value.appearance, preferences.value.motion)
    DisposableEffect(store) {
        val compositionStart: (Event) -> Unit = { renderer.composing = true }
        val compositionEnd: (Event) -> Unit = {
            renderer.composing = false
            store.dispatch(AppAction.RefreshTime)
        }
        val refresh: (Event) -> Unit = { store.dispatch(AppAction.RefreshTime) }
        val keyboard: (Event) -> Unit = keyboard@{ raw ->
            val event = raw as? KeyboardEvent ?: return@keyboard
            val current = store.state.value
            if (renderer.composing || event.isComposing || event.repeat || event.altKey || event.ctrlKey || event.metaKey ||
                route != WebRoute.Training || current.tab != AppTab.Training || current.loadStatus != LoadStatus.Ready ||
                current.phase !in setOf(CardPhase.Question, CardPhase.Revealed) || editableTarget(event.target as? Element)
            ) return@keyboard
            val id = current.exerciseId ?: return@keyboard
            if (event.code == "Space" && current.phase == CardPhase.Question) {
                event.preventDefault()
                store.dispatch(if (current.introPending) AppAction.ContinueIntroduction else AppAction.Reveal(id))
            } else if (current.phase == CardPhase.Revealed) {
                val rating = when (event.key) {
                    "1" -> Rating.Again
                    "2" -> Rating.Good
                    else -> null
                }
                if (rating != null) {
                    event.preventDefault()
                    store.dispatch(AppAction.Rate(id, rating))
                }
            }
        }
        document.addEventListener("compositionstart", compositionStart)
        document.addEventListener("compositionend", compositionEnd)
        document.addEventListener("visibilitychange", refresh)
        window.addEventListener("focus", refresh)
        window.addEventListener("keydown", keyboard)
        val timer = window.setInterval({ store.dispatch(AppAction.RefreshTime); null }, 30_000)
        onDispose {
            window.clearInterval(timer)
            document.removeEventListener("compositionstart", compositionStart)
            document.removeEventListener("compositionend", compositionEnd)
            document.removeEventListener("visibilitychange", refresh)
            window.removeEventListener("focus", refresh)
            window.removeEventListener("keydown", keyboard)
            store.close()
        }
    }

    HtmlElementView(
        factory = {
            (document.createElement("div") as HTMLDivElement).apply {
                className = "training-web-host"
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { root ->
            val dispatch: (AppAction) -> Unit = { action ->
                when (action) {
                    is AppAction.SelectTab -> routes.navigate(WebRoute.forTab(action.tab))
                    is AppAction.SetExplanationMethod -> preferences.setMethod(action.method, store)
                    is AppAction.SetAnswerMode -> preferences.setAnswerMode(action.mode, store)
                    else -> {
                        store.dispatch(action)
                        if (action is AppAction.ChooseSkill) routes.navigate(WebRoute.Training)
                    }
                }
            }
            renderer.render(root, state, route, routes.returnTo, preferences, store, { routes.navigate(it) }, dispatch)
            for (effect in state.pendingEffects) {
                if (attemptedEffects.add(effect.id)) {
                    if (!executeEffect(root, state, effect, store, routes::navigate)) attemptedEffects.remove(effect.id)
                }
            }
        },
    )
}

private fun editableTarget(target: Element?): Boolean {
    val tag = target?.tagName?.uppercase()
    return tag in setOf("INPUT", "TEXTAREA", "SELECT", "BUTTON", "A") ||
        target?.closest("[contenteditable]") != null
}

private fun executeEffect(root: HTMLElement, state: AppUiState, effect: UiEffect, store: TrainingStore,
                          navigate: (WebRoute) -> Unit): Boolean {
    when (effect) {
        is UiEffect.FocusReveal -> {
            if (state.exerciseId == effect.exerciseId && state.phase == CardPhase.Question && state.tab == AppTab.Training) {
                if (state.introPending) return false
                val reveal = root.querySelector("#training-reveal") as? HTMLElement ?: return false
                reveal.focus()
                store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Completed))
            } else store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Skipped))
        }
        is UiEffect.ConfirmReset -> {
            val previousRevision = store.state.value.revision
            val confirmed = window.confirm(effect.prompt)
            store.dispatch(AppAction.ResetDecision(effect.id, confirmed))
            if (confirmed && store.state.value.revision > previousRevision && store.state.value.tab == AppTab.Training) {
                navigate(WebRoute.Training)
            }
        }
        is UiEffect.DownloadJson -> {
            try {
                val anchor = document.createElement("a") as HTMLAnchorElement
                anchor.href = "data:application/json;charset=utf-8,${percentEncode(effect.json)}"
                anchor.download = effect.filename
                document.body?.appendChild(anchor)
                anchor.click()
                anchor.remove()
                store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Completed))
            } catch (error: Throwable) {
                store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Failed(error.message ?: "Экспорт не удался")))
            }
        }
    }
    return true
}

private fun percentEncode(value: String): String {
    val digits = "0123456789ABCDEF"
    return buildString {
        for (byte in value.encodeToByteArray()) {
            val unsigned = byte.toInt() and 255
            append('%')
            append(digits[unsigned shr 4])
            append(digits[unsigned and 15])
        }
    }
}

private class TrainingDomRenderer {
    private var previous: AppUiState? = null
    private var previousRoute: WebRoute? = null
    private var previousPreferences: polski.preferences.UserPreferencesV2? = null
    private var previousStatus: String? = null
    var composing = false
    private val vocabulary = VocabularyWebController()
    private var navigation: WebNav? = null

    fun close() { navigation?.close(); navigation = null }

    fun render(root: HTMLElement, state: AppUiState, route: WebRoute, returnTo: WebRoute, preferences: WebPreferencesController, store: TrainingStore, navigate: (WebRoute) -> Unit, dispatch: (AppAction) -> Unit) {
        val old = previous
        if (old != null && previousRoute == route && previousPreferences == preferences.value && previousStatus == preferences.status && old.copy(draft = state.draft) == state) {
            previous = state
            return // Keep the live textarea, selection and IME composition intact.
        }
        if (composing && old != null && previousRoute == route && previousPreferences == preferences.value && old.exerciseId == state.exerciseId && old.phase == CardPhase.Question) {
            previous = state
            return // A timer/visibility refresh must not replace an active IME editor.
        }
        val focusId = (document.activeElement as? HTMLElement)?.id.orEmpty()
        val scroll = root.scrollTop
        val innerScroll = root.querySelectorAll("[data-scroll-key]")
        val scrollPositions = buildMap {
            for (index in 0 until innerScroll.length) {
                val node = innerScroll.item(index) as? HTMLElement ?: continue
                val key = node.getAttribute("data-scroll-key") ?: continue
                put(key, node.scrollLeft to node.scrollTop)
            }
        }
        val app = (root.querySelector(":scope > .app") as? HTMLElement)
            ?: node("div", "app").also(root::appendChild)
        val nav = navigation ?: WebNav(app, navigate).also { navigation = it }
        nav.update(route)
        app.querySelector(":scope > header.top")?.remove()
        val headerHolder = node("div")
        renderHeader(headerHolder, state)
        headerHolder.firstChild?.let { app.insertBefore(it, nav.shell) }
        val content = (app.querySelector(":scope > .route-content") as? HTMLElement)
            ?: node("div", "route-content").also(app::appendChild)
        content.textContent = ""
        if (state.error != null) {
            content.appendChild(node("p", "notice", state.error).apply { setAttribute("role", "alert") })
        }
        when (route) {
            WebRoute.Training -> renderTraining(content, state, preferences.value.swipeRatingEnabled, dispatch)
            WebRoute.Vocabulary -> vocabulary.render(node("main", "vocabulary-page").also(content::appendChild), preferences.value.swipeRatingEnabled) {
                previous = null
                render(root, state, route, returnTo, preferences, store, navigate, dispatch)
            }
            WebRoute.Matrix -> renderMatrixWeb(node("main", "matrix-page").also(content::appendChild), state, dispatch)
            WebRoute.Progress -> renderProgressWeb(node("main", "progress-page").also(content::appendChild), state, dispatch)
            WebRoute.Settings -> renderSettingsWeb(node("main", "settings-page").also(content::appendChild), preferences, store, returnTo, navigate)
        }
        content.appendChild(node("footer", text = "Прогресс сохраняется в этом браузере. Интервальные повторения — FSRS."))
        root.scrollTop = scroll
        for ((key, position) in scrollPositions) {
            val node = root.querySelector("[data-scroll-key='$key']") as? HTMLElement ?: continue
            node.scrollLeft = position.first
            node.scrollTop = position.second
        }
        val restoredFocus = if (focusId.isNotEmpty()) document.getElementById(focusId) as? HTMLElement else null
        if (restoredFocus != null) {
            restoredFocus.focus()
        } else if (previousRoute != null && previousRoute != route) {
            // Rebuilding the page removes route-local controls. Give keyboard users a stable
            // focus destination when returning from Settings (or another section).
            (document.getElementById("nav-${route.slug}") as? HTMLElement)?.focus()
        }
        if (route == WebRoute.Training && old?.phase == CardPhase.Question && state.phase == CardPhase.Revealed) {
            val feedback = content.querySelector(".change-list h3") as? HTMLElement
            if (feedback != null) {
                val clearance = nav.shell.getBoundingClientRect().top - feedback.getBoundingClientRect().bottom
                if (clearance < 12.0) root.scrollTop += 12.0 - clearance
            }
        }
        previous = state
        previousRoute = route
        previousPreferences = preferences.value
        previousStatus = preferences.status
    }

    private fun renderHeader(app: HTMLElement, state: AppUiState) {
        val header = node("header", "top")
        app.appendChild(header)
        header.appendChild(node("div").apply {
            appendChild(node("h1", text = "POLSKI Grammar Matrix"))
            appendChild(node("p", text = "Предложение → преобразование → новое предложение"))
        })
        header.appendChild(node("div", "topstats").apply {
            appendChild(stat(state.dueCount.toString(), "к повторению"))
            appendChild(stat(state.todayCount.toString(), "сегодня"))
        })
    }

    private fun stat(value: String, label: String): HTMLElement = node("div").apply {
        appendChild(node("b", text = value))
        appendChild(node("span", text = label))
    }

    private fun renderTraining(app: HTMLElement, state: AppUiState, swipeRatingEnabled: Boolean, dispatch: (AppAction) -> Unit) {
        val main = node("main", "study-page")
        app.appendChild(main)
        if (state.loadStatus != LoadStatus.Ready) {
            renderLoadStatus(main, state, dispatch)
            return
        }
        val toolbar = node("div", "study-toolbar")
        main.appendChild(toolbar)
        val modes = node("div", "subnav")
        modes.setAttribute("aria-label", "Режим тренировки")
        toolbar.appendChild(modes)
        modes.appendChild(button("Цепочка предложений", active = state.mode == TrainingMode.Chain, pressed = state.mode == TrainingMode.Chain) { dispatch(AppAction.StartChain()) })
        modes.appendChild(button("По расписанию · ${state.dueCount}", active = state.mode == TrainingMode.Schedule, pressed = state.mode == TrainingMode.Schedule) { dispatch(AppAction.StartSchedule) })
        modes.appendChild(button("Отдельный навык", active = state.mode == TrainingMode.Focused, pressed = state.mode == TrainingMode.Focused) { dispatch(AppAction.OpenSkillPicker) }.apply {
            setAttribute("aria-expanded", state.showSkillPicker.toString())
        })
        val options = node("div", "study-options")
        toolbar.appendChild(options)
        val methodLabel = node("label", text = "Подача")
        options.appendChild(methodLabel)
        val methodSelect = document.createElement("select") as HTMLSelectElement
        methodSelect.id = "explanation-method"
        methodSelect.setAttribute("aria-label", "Подача объяснений")
        listOf("logic" to "Схемы и логика", "situations" to "Живые ситуации").forEach { (value, title) ->
            methodSelect.appendChild(node("option", text = title).apply { setAttribute("value", value) })
        }
        methodSelect.value = if (state.explanationMethod == ExplanationMethod.Logic) "logic" else "situations"
        methodSelect.addEventListener("change", {
            val method = if (methodSelect.value == "situations") ExplanationMethod.Situations else ExplanationMethod.Logic
            dispatch(AppAction.SetExplanationMethod(method))
        })
        methodLabel.appendChild(methodSelect)
        options.appendChild(button(if (state.showReference && !state.introPending) "Скрыть таблицу" else "Таблица под рукой") {
            dispatch(AppAction.ToggleReference)
        }.apply {
            disabled = state.introPending
            setAttribute("aria-expanded", (state.showReference && !state.introPending).toString())
        })
        if (state.showSkillPicker) renderSkillPicker(main, state, dispatch)
        if (state.mode == TrainingMode.Chain) renderChainHeader(main, state, dispatch)
        val layout = node("div", if (state.showReference && !state.introPending) "study-layout with-reference" else "study-layout")
        main.appendChild(layout)
        val card = node("section", "flashcard card")
        card.setAttribute("aria-label", "Учебная карточка")
        layout.appendChild(card)
        when (state.phase) {
            CardPhase.ChainComplete -> renderChainComplete(card, state, dispatch)
            CardPhase.NoDue -> renderNoDue(card, state, dispatch)
            CardPhase.Question, CardPhase.Revealed -> renderCard(card, state, swipeRatingEnabled, dispatch)
        }
        if (state.showReference && !state.introPending) {
            val reference = node("aside", "card reference-panel")
            layout.appendChild(reference)
            renderCaseReferenceWeb(reference, state, dispatch)
        }
        main.appendChild(node("p", "study-help", "Пробел — ${if (state.introPending) "перейти к заданию" else "показать ответ"} · 1–2 — оценить"))
    }

    private fun renderLoadStatus(main: HTMLElement, state: AppUiState, dispatch: (AppAction) -> Unit) {
        val section = node("section", "card session-complete")
        main.appendChild(section)
        val title = when (state.loadStatus) {
            LoadStatus.Loading -> "Загружаем прогресс"
            LoadStatus.MigrationAvailable -> "Найден прежний прогресс"
            LoadStatus.RecoveryRequired -> "Нужна копия прогресса"
            LoadStatus.Unavailable -> "Хранилище недоступно"
            LoadStatus.Ready -> return
        }
        section.appendChild(node("h2", text = title))
        if (state.loadStatus == LoadStatus.MigrationAvailable) {
            section.appendChild(node("p", text = "Экспортируй исходный JSON или перенеси прогресс в эту версию."))
            section.appendChild(button("Перенести прогресс") { dispatch(AppAction.RequestMigration) })
        }
        if (state.loadStatus == LoadStatus.RecoveryRequired) {
            section.appendChild(node("p", text = "Сохрани исходный JSON перед повторной попыткой. Неподходящие данные не будут перезаписаны."))
            section.appendChild(button("Повторить перенос") { dispatch(AppAction.RequestMigration) })
        }
        if (state.loadStatus == LoadStatus.MigrationAvailable || state.loadStatus == LoadStatus.RecoveryRequired) {
            section.appendChild(button("Экспорт JSON") { dispatch(AppAction.RequestExport) })
        }
    }

    private fun renderSkillPicker(main: HTMLElement, state: AppUiState, dispatch: (AppAction) -> Unit) {
        val picker = node("section", "skill-picker")
        picker.setAttribute("aria-label", "Выбор навыка")
        main.appendChild(picker)
        polski.data.skills.forEach { skill ->
            picker.appendChild(button("${skill.title} · ${skill.level}", active = state.focusedSkillId == skill.id) {
                dispatch(AppAction.ChooseSkill(skill.id))
            })
        }
    }

    private fun renderChainHeader(main: HTMLElement, state: AppUiState, dispatch: (AppAction) -> Unit) {
        val header = node("div", "chain-header")
        main.appendChild(header)
        val label = node("label", text = "Один набор слов")
        header.appendChild(label)
        val select = document.createElement("select") as HTMLSelectElement
        select.id = "training-seed"
        select.setAttribute("aria-label", "Слова для цепочки")
        polski.training.sentenceSeeds.forEachIndexed { index, seed ->
            val option = document.createElement("option") as org.w3c.dom.HTMLOptionElement
            option.value = index.toString()
            option.textContent = "${polski.data.nounById(seed.nounId).lemma} — ${polski.data.nounById(seed.nounId).meaning}"
            select.appendChild(option)
        }
        select.value = state.seedIndex.toString()
        select.addEventListener("change", { dispatch(AppAction.SelectChainSeed(select.value.toInt())) })
        label.appendChild(select)
        val steps = node("ol")
        steps.setAttribute("aria-label", "Шаги цепочки")
        header.appendChild(steps)
        polski.data.courseChainPresentation.steps.forEachIndexed { index, step ->
            val item = node("li", text = "${index + 1} ${step.label}")
            item.className = when {
                state.chainComplete || index < state.chainIndex -> "done"
                index == state.chainIndex -> "current"
                else -> ""
            }
            if (!state.chainComplete && index == state.chainIndex) item.setAttribute("aria-current", "step")
            steps.appendChild(item)
        }
    }

    private fun renderChainComplete(card: HTMLElement, state: AppUiState, dispatch: (AppAction) -> Unit) {
        val complete = node("div", "session-complete")
        card.appendChild(complete)
        complete.appendChild(node("h2", text = polski.data.courseChainPresentation.completion.title))
        complete.appendChild(node("p", text = polski.data.courseChainPresentation.completion.webBody))
        val review = node("div", "chain-review")
        complete.appendChild(review)
        state.chain.forEachIndexed { index, exercise ->
            review.appendChild(node("div").apply {
                appendChild(node("small", text = "${index + 1} · ${polski.data.skillById(exercise.primarySkill).title}"))
                appendChild(node("p", text = exercise.expected).apply { setAttribute("lang", "pl") })
            })
        }
        complete.appendChild(node("div", "actions").apply {
            appendChild(button("Следующий набор слов", primary = true) {
                dispatch(AppAction.StartChain((state.seedIndex + 1) % polski.training.sentenceSeeds.size))
            })
            appendChild(button("К повторениям по расписанию") { dispatch(AppAction.StartSchedule) })
        })
    }

    private fun renderNoDue(card: HTMLElement, state: AppUiState, dispatch: (AppAction) -> Unit) {
        card.appendChild(node("div", "session-complete").apply {
            appendChild(node("h2", text = "Повторения на сейчас завершены"))
            appendChild(node("p", text = state.nextDue?.let {
                "Следующее: ${browserFormatDate(it.toEpochMilliseconds().toDouble())}"
            } ?: "Новых повторений пока нет."))
            appendChild(button("Потренировать цепочку") { dispatch(AppAction.StartChain()) })
        })
    }

    private fun renderCard(card: HTMLElement, state: AppUiState, swipeRatingEnabled: Boolean, dispatch: (AppAction) -> Unit) {
        val exercise = state.exercise ?: return
        val skill = polski.data.skillById(exercise.primarySkill)
        val presentation = polski.data.presentationBySkillId(exercise.primarySkill)
        val method = if (state.explanationMethod == ExplanationMethod.Logic) presentation.logic else presentation.situations
        card.appendChild(node("div", "card-meta").apply {
            appendChild(node("span", text = when (state.mode) {
                TrainingMode.Chain -> "Цепочка · ${state.chainIndex + 1} / ${state.chain.size}"
                TrainingMode.Schedule -> "Повторение по расписанию"
                TrainingMode.Focused -> "Тренировка навыка"
            }))
            appendChild(node("span", text = "${skill.level} · ${skill.title}"))
        })
        if (state.introPending && state.phase == CardPhase.Question) {
            card.appendChild(node("div", "card-front method-introduce").apply {
                appendChild(node("span", "eyebrow", if (state.explanationMethod == ExplanationMethod.Situations) "Сцена и намерение" else "Признаки и операция"))
                appendChild(node("p", "source-sentence").apply {
                    setAttribute("lang", "pl")
                    appendContrastParts(this, sentenceHighlightParts(exercise.source, exercise.changes, ChangeSide.Before), "change-before")
                })
                appendChild(node("p", text = method.introduce))
                appendChild(button("Перейти к заданию", primary = true) {
                    dispatch(AppAction.ContinueIntroduction)
                })
            })
            return
        }
        card.appendChild(node("div", "card-front").apply {
            appendChild(node("span", "eyebrow", "Исходное предложение"))
            appendChild(node("p", "source-sentence").apply {
                setAttribute("lang", "pl")
                appendContrastParts(this, sentenceHighlightParts(exercise.source, exercise.changes, ChangeSide.Before), "change-before")
            })
            appendChild(node("div", "operation").apply {
                appendChild(node("span", text = if (state.explanationMethod == ExplanationMethod.Logic) "Преобразуй" else "Ситуация"))
                appendChild(node("h2", text = exercise.prompt))
                appendChild(node("p", "method-retrieve", method.retrieve))
                appendChild(node("small", "method-lead", method.promptLead))
            })
        })
        if (state.phase == CardPhase.Question) renderAnswerArea(card, state, dispatch)
        else renderAnswerBack(card, state, swipeRatingEnabled, dispatch)
    }

    private fun renderAnswerArea(card: HTMLElement, state: AppUiState, dispatch: (AppAction) -> Unit) {
        val area = node("div", "answer-area")
        card.appendChild(area)
        val modes = node("div", "answer-mode")
        modes.setAttribute("aria-label", "Как отвечать")
        area.appendChild(modes)
        modes.appendChild(button("Ответ вслух / про себя", active = state.answerMode == AnswerMode.Oral, pressed = state.answerMode == AnswerMode.Oral) {
            dispatch(AppAction.SetAnswerMode(AnswerMode.Oral))
        })
        modes.appendChild(button("Напечатать ответ", active = state.answerMode == AnswerMode.Typed, pressed = state.answerMode == AnswerMode.Typed) {
            dispatch(AppAction.SetAnswerMode(AnswerMode.Typed))
        })
        if (state.answerMode == AnswerMode.Typed) {
            val input = document.createElement("textarea") as HTMLTextAreaElement
            input.id = "training-answer"
            input.setAttribute("aria-label", "Ответ по-польски")
            input.placeholder = "Напиши целое предложение…"
            input.value = state.draft
            input.addEventListener("input", { dispatch(AppAction.EditAnswer(input.value)) })
            input.addEventListener("keydown", { raw ->
                val event = raw as KeyboardEvent
                if (event.key == "Enter" && !event.shiftKey && !event.isComposing && !composing) {
                    event.preventDefault()
                    state.exerciseId?.let { dispatch(AppAction.Reveal(it)) }
                }
            })
            area.appendChild(input)
        } else area.appendChild(node("p", "muted", "Произнеси целое предложение, затем переверни карточку."))
        area.appendChild(button(if (state.answerMode == AnswerMode.Typed) "Проверить и показать ответ" else "Показать ответ", primary = true) {
            state.exerciseId?.let { dispatch(AppAction.Reveal(it)) }
        }.apply { id = "training-reveal"; className += " reveal-button" })
    }

    private fun renderAnswerBack(card: HTMLElement, state: AppUiState, swipeRatingEnabled: Boolean, dispatch: (AppAction) -> Unit) {
        val exercise = state.exercise ?: return
        val back = node("div", "card-back")
        back.setAttribute("aria-live", "polite")
        card.appendChild(back)
        back.appendChild(node("span", "eyebrow", "Обратная сторона · эталон"))
        back.appendChild(node("p", "answer-sentence").apply {
            setAttribute("lang", "pl")
            appendContrastParts(this, sentenceHighlightParts(exercise.expected, exercise.changes, ChangeSide.After), "change-after")
        })
        if (exercise.accepted.isNotEmpty()) {
            back.appendChild(node("p", "accepted", "Также: ${exercise.accepted.joinToString(" / ")}").apply { setAttribute("lang", "pl") })
        }
        if (state.answerMode == AnswerMode.Typed) {
            val result = state.evaluation?.correct == true
            back.appendChild(node("div", if (result) "typed-result correct" else "typed-result incorrect").apply {
                appendChild(node("strong", text = if (result) "Совпадает с правильным вариантом" else "Сравни свой ответ с эталоном"))
                appendChild(node("p", text = state.frozenAnswer?.takeIf(String::isNotEmpty) ?: "Ответ не введён"))
            })
        }
        val feedbackMethod = polski.data.presentationBySkillId(exercise.primarySkill).let { presentation ->
            if (state.explanationMethod == ExplanationMethod.Logic) presentation.logic else presentation.situations
        }
        back.appendChild(node("div", "method-feedback").apply {
            appendChild(node("h3", text = if (state.explanationMethod == ExplanationMethod.Situations) "Сравни смысл и форму" else "Разбор изменений"))
            appendChild(node("p", text = feedbackMethod.feedback))
            if (state.explanationMethod == ExplanationMethod.Situations) appendChild(node("p", text = exercise.explanation))
        })
        back.appendChild(node("div", "change-list").apply {
            appendChild(node("h3", text = "Что изменилось"))
            exercise.changes.forEach { change ->
                appendChild(node("div").apply {
                    appendChild(node("div", "change-pair").apply {
                        appendChild(node("span").apply {
                            appendContrastParts(this, changeHighlightParts(change.from, change.to, ChangeSide.Before), "change-before")
                        })
                        appendChild(node("b", text = "→"))
                        appendChild(node("strong").apply {
                            appendContrastParts(this, changeHighlightParts(change.from, change.to, ChangeSide.After), "change-after")
                        })
                    })
                    appendChild(node("p", text = change.reason))
                })
            }
        })
        val rule = node("section", "rule-focus")
        rule.setAttribute("aria-label", "Ключевое правило")
        back.appendChild(rule)
        rule.appendChild(node("small", text = "ЗАПОМНИ"))
        rule.appendChild(node("strong", text = polski.data.skillById(exercise.primarySkill).formula))
        val presentation = polski.data.presentationBySkillId(exercise.primarySkill)
        val method = if (state.explanationMethod == ExplanationMethod.Logic) presentation.logic else presentation.situations
        rule.appendChild(node("p", text = method.introduction))
        if (state.explanationMethod == ExplanationMethod.Logic) rule.appendChild(node("p", text = exercise.explanation))
        rule.appendChild(node("div", "rule-contrast").apply {
            setAttribute("lang", "pl")
            appendChild(node("span", "form-contrast").apply {
                setAttribute("aria-label", "Было: ${presentation.focusBefore}. Стало: ${presentation.focusAfter}")
                appendChild(node("span", "form-contrast-before").apply {
                    setAttribute("aria-hidden", "true")
                    appendContrastParts(this, changeHighlightParts(presentation.focusBefore, presentation.focusAfter, ChangeSide.Before), "change-before")
                })
                appendChild(node("span", "form-contrast-arrow", "→").apply { setAttribute("aria-hidden", "true") })
                appendChild(node("strong", "form-contrast-after").apply {
                    setAttribute("aria-hidden", "true")
                    appendContrastParts(this, changeHighlightParts(presentation.focusBefore, presentation.focusAfter, ChangeSide.After), "change-after")
                })
            })
        })
        back.appendChild(node("div", "rating-label", "Когда повторить?"))
        back.appendChild(node("p", "method-review", method.review))
        val ratings = node("div", "ratings")
        back.appendChild(ratings)
        listOf(
            Triple(Rating.Again, "Повторить", "Ошибка или не уверен"),
            Triple(Rating.Good, "Вспомнил", "Воспроизвёл сам"),
        ).forEachIndexed { index, (rating, label, hint) ->
            ratings.appendChild(button("${index + 1} $label", extraClass = "rating-${rating.name.lowercase()}") {
                dispatch(AppAction.Rate(exercise.id, rating))
            }.apply {
                appendChild(node("small", text = hint))
                appendChild(node("span", text = state.intervals?.get(rating)?.let { due ->
                    intervalLabel(due.toEpochMilliseconds(), state.now?.toEpochMilliseconds() ?: due.toEpochMilliseconds())
                }.orEmpty()))
            })
        }
        back.appendChild(node("p", "muted small", "Оценка планирует следующее повторение навыка."))
        if (swipeRatingEnabled) {
            back.appendChild(node("p", "vocabulary-swipe-zone muted small", "← Повторить · Вспомнил →").apply {
                setAttribute("aria-hidden", "true")
                installTouchSwipeRating(this) { remembered ->
                    dispatch(AppAction.Rate(exercise.id, if (remembered) Rating.Good else Rating.Again))
                }
            })
        }
    }
}

private fun intervalLabel(dueMillis: Long, nowMillis: Long): String {
    val minutes = maxOf(1L, (dueMillis - nowMillis + 30_000L) / 60_000L)
    return when {
        minutes < 60 -> "$minutes мин"
        minutes < 2_880 -> "${(minutes + 30) / 60} ч"
        else -> "${(minutes + 720) / 1_440} дн"
    }
}

private fun appendContrastParts(container: HTMLElement, parts: List<EndingPart>, changedClass: String) {
    parts.forEach { part ->
        if (part.isChanged) container.appendChild(node("span", if (part.isEnding && changedClass == "change-after") "$changedClass ending-highlight" else changedClass, part.text))
        else container.appendChild(document.createTextNode(part.text))
    }
}

private fun node(tag: String, className: String = "", text: String? = null): HTMLElement =
    (document.createElement(tag) as HTMLElement).apply {
        this.className = className
        if (text != null) textContent = text
    }

private fun button(
    label: String,
    active: Boolean = false,
    pressed: Boolean? = null,
    primary: Boolean = false,
    extraClass: String = "",
    onClick: () -> Unit,
): HTMLButtonElement = (document.createElement("button") as HTMLButtonElement).apply {
    type = "button"
    textContent = label
    className = listOfNotNull(if (active) "active" else null, if (primary) "primary" else null, extraClass.takeIf(String::isNotEmpty)).joinToString(" ")
    if (pressed != null) setAttribute("aria-pressed", pressed.toString())
    addEventListener("click", { onClick() })
}
