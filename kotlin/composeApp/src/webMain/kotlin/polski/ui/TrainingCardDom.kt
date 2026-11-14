package polski.ui

import androidx.compose.runtime.getValue
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import polski.data.activeCoursePackId
import polski.data.availableCoursePacks
import polski.platform.browserFormatDate
import polski.presentation.*
import polski.srs.Rating

/** Active target language follows the course selected by Settings. */
private fun activeTargetLangCode(): String = availableCoursePacks.firstOrNull { it.pairId == activeCoursePackId }?.target ?: "pl"
private fun activeTargetAdverb(): String = polski.presentation.UiCopy.languageAdverb(activeTargetLangCode())

internal class TrainingCardDom(
    private val riveOverlay: RiveEffectOverlay,
    private val isComposing: () -> Boolean,
) {
    // SB-6 blueprint §2.5: "which picker/error panel is open" is a pure web display concern, not
    // cross-host learning state (TileBuilderState carries none) — two instance vars on this
    // renderer, which already outlives individual renders. Reset to null whenever the exercise
    // changes (never survives into the next card); see [resetTileUiStateIfNewExercise].
    private var openTilePicker: Int? = null
    private var openTileError: Int? = null
    private var tileUiExerciseId: String? = null

    private fun resetTileUiStateIfNewExercise(exerciseId: String?) {
        if (exerciseId == tileUiExerciseId) return
        tileUiExerciseId = exerciseId
        openTilePicker = null
        openTileError = null
    }

    fun renderTraining(root: HTMLElement, app: HTMLElement, state: AppUiState, swipeRatingEnabled: Boolean, enteringChainComplete: Boolean, justRevealed: Boolean, justShowedReference: Boolean, dispatch: (AppAction) -> Unit) {
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
        val studyModel = trainingCardModel(state)
        studyModel.header.modes.forEach { option ->
            val label = if (option.mode == TrainingMode.Schedule) "${option.label} · ${studyModel.header.dueCount}" else option.label
            val btn = button(label, active = studyModel.header.selected == option.mode, pressed = studyModel.header.selected == option.mode) { dispatch(option.action) }
            if (option.mode == TrainingMode.Focused) btn.setAttribute("aria-expanded", state.showSkillPicker.toString())
            modes.appendChild(btn)
        }
        val options = node("div", "study-options")
        toolbar.appendChild(options)
        val methodLabel = node("label", text = "Подача")
        options.appendChild(methodLabel)
        val methodSelect = document.createElement("select") as HTMLSelectElement
        methodSelect.id = "explanation-method"
        methodSelect.setAttribute("aria-label", "Подача объяснений")
        builtInStyleIds.forEach { styleId ->
            methodSelect.appendChild(node("option", text = styleLabel(styleId)).apply { setAttribute("value", styleId.value) })
        }
        methodSelect.value = state.styleId.value
        // UC-10: a compact quick switch over the same 4 styles as Settings — dispatching SetStyle
        // is a display choice only (TrainingStore.SetStyle), never creates a review and never
        // touches draft/frozenAnswer/exercise/phase.
        methodSelect.addEventListener("change", {
            dispatch(AppAction.SetStyle(StyleId(methodSelect.value)))
        })
        methodLabel.appendChild(methodSelect)
        val model = trainingCardModel(state)
        if (state.caseReferenceAvailable) {
            options.appendChild(button(model.referenceToggle.label) {
                dispatch(AppAction.ToggleReference)
            }.apply {
                disabled = !model.referenceToggle.enabled
                setAttribute("aria-expanded", model.referenceToggle.expanded.toString())
            })
        }
        if (state.showSkillPicker) renderSkillPicker(main, state, dispatch)
        if (state.mode == TrainingMode.Chain) renderChainHeader(main, state, dispatch)
        if (state.mode == TrainingMode.Daily) renderDailyProgress(main, state)
        val layout = node("div", if (model.referenceToggle.expanded) "study-layout with-reference" else "study-layout")
        main.appendChild(layout)
        val card = node("section", "flashcard card")
        card.setAttribute("aria-label", "Учебная карточка")
        layout.appendChild(card)
        when (state.phase) {
            CardPhase.ChainComplete -> renderChainComplete(root, card, state, dispatch, enteringChainComplete)
            CardPhase.DailyComplete -> renderDailyComplete(card, state, dispatch)
            CardPhase.NoDue -> renderNoDue(card, state, dispatch)
            CardPhase.Question, CardPhase.Revealed -> renderCard(root, card, state, swipeRatingEnabled, justRevealed, dispatch)
        }
        if (model.referenceToggle.expanded) {
            // UX4-20/22: animates in (fade + slight rise) exactly on the render where it just
            // turned on; a routine rebuild of an already-shown panel (timer, rating, …) must not
            // replay it. Hiding stays instant, matching this panel's existing conditional-mount
            // design — see TrainingWebApp.kt's own doc comment on `justShowedReference`.
            val reference = node("aside", "card reference-panel" + if (justShowedReference && !motionInstantActive()) " panel-reveal" else "")
            layout.appendChild(reference)
            renderCaseReferenceWeb(reference, state, dispatch)
        }
        // W4/U3: a coarse (touch/stylus) pointer has no physical keyboard, so the hint must name
        // swipe instead of Space/arrows/digits — checked live, not just at narrow widths, per
        // UX4-11's own precedent one screen up (a resizable fine-pointer desktop window can be
        // just as narrow as a phone).
        val action = if (state.predictionPending) "продолжить" else if (state.introPending) "перейти к заданию" else "показать ответ"
        val help = if (window.matchMedia("(pointer: coarse)").matches) "Коснитесь — $action · Свайп влево/вправо — оценить"
        else "Пробел — $action · ← → или 1–2 — оценить"
        main.appendChild(node("p", "study-help", help))
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
            option.textContent = polski.data.nounLabel(seed.nounId)
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

    private fun renderChainComplete(root: HTMLElement, card: HTMLElement, state: AppUiState, dispatch: (AppAction) -> Unit, justCompleted: Boolean) {
        val complete = node("div", "session-complete")
        card.appendChild(complete)
        complete.appendChild(node("h2", text = polski.data.courseChainPresentation.completion.title))
        complete.appendChild(node("p", text = polski.data.courseChainPresentation.completion.webBody))
        val review = node("div", "chain-review")
        complete.appendChild(review)
        state.chain.forEachIndexed { index, exercise ->
            review.appendChild(node("div").apply {
                appendChild(node("small", text = "${index + 1} · ${polski.data.skillById(exercise.primarySkill).title}"))
                appendChild(node("p", text = exercise.expected).apply { setAttribute("lang", activeTargetLangCode()) })
            })
        }
        complete.appendChild(node("div", "actions").apply {
            appendChild(button("Следующий набор слов", primary = true) {
                dispatch(AppAction.StartChain((state.seedIndex + 1) % polski.training.sentenceSeeds.size))
            })
            appendChild(button("К повторениям по расписанию") { dispatch(AppAction.StartSchedule) })
        })
        // v2 FC2-10 (R3): a one-shot "Tada" celebration exactly when the chain finishes, never
        // replayed on a routine re-render while this screen stays on view (e.g. the 30s timer).
        if (justCompleted) riveOverlay.triggerChainComplete(root, complete)
    }

    private fun renderNoDue(card: HTMLElement, state: AppUiState, dispatch: (AppAction) -> Unit) {
        card.appendChild(node("div", "session-complete").apply {
            appendChild(node("h2", text = if (state.mode == TrainingMode.Daily) "Сегодня нечего тренировать" else "Повторения на сейчас завершены"))
            appendChild(node("p", text = state.nextDue?.let {
                "Следующее: ${browserFormatDate(it.toEpochMilliseconds().toDouble())}"
            } ?: UiCopy.NoDueTitle))
            appendChild(button("Потренировать цепочку") { dispatch(AppAction.StartChain()) })
        })
    }

    /** M3 blueprint: a `role="progressbar"` element above the card, next to [renderChainHeader]'s
     *  own position — "`done` / `planned`" plus "· продолжение" once [DailySessionState.extended]. */
    private fun renderDailyProgress(main: HTMLElement, state: AppUiState) {
        val daily = state.daily ?: return
        val progress = node("div", "daily-progress")
        progress.setAttribute("role", "progressbar")
        progress.setAttribute("aria-valuenow", daily.history.size.toString())
        progress.setAttribute("aria-valuemin", "0")
        progress.setAttribute("aria-valuemax", daily.planned.toString())
        progress.textContent = "${daily.history.size} / ${daily.planned}" + if (daily.extended) " · продолжение" else ""
        main.appendChild(progress)
    }

    /** M3 blueprint: summary + "Ещё одно задание" (continue) / "К повторениям по расписанию"
     *  (escape hatch, same as [renderChainComplete]'s own). */
    private fun renderDailyComplete(card: HTMLElement, state: AppUiState, dispatch: (AppAction) -> Unit) {
        val complete = node("div", "session-complete")
        card.appendChild(complete)
        complete.appendChild(node("h2", text = "Сессия на сегодня готова"))
        complete.appendChild(node("p", text = dailySummaryText(state.daily?.history ?: emptyList())))
        complete.appendChild(node("div", "actions").apply {
            appendChild(button("Ещё одно задание", primary = true) { dispatch(AppAction.ContinueDaily) })
            appendChild(button("К повторениям по расписанию") { dispatch(AppAction.StartSchedule) })
        })
    }

    private fun renderCard(root: HTMLElement, card: HTMLElement, state: AppUiState, swipeRatingEnabled: Boolean, justRevealed: Boolean, dispatch: (AppAction) -> Unit) {
        val exercise = state.exercise ?: return
        resetTileUiStateIfNewExercise(exercise.id)
        val skill = polski.data.skillById(exercise.primarySkill)
        val presentation = polski.data.presentationBySkillId(exercise.primarySkill)
        val method = if (state.styleId == StyleId.SituationFirst) presentation.situations else presentation.logic
        card.appendChild(node("div", "card-meta").apply {
            appendChild(node("span", text = when (state.mode) {
                TrainingMode.Chain -> "Цепочка · ${state.chainIndex + 1} / ${state.chain.size}"
                TrainingMode.Schedule -> "Повторение по расписанию"
                TrainingMode.Focused -> "Тренировка навыка"
                // M3 blueprint ("дополнительная практика не маскируется под тест памяти"): honest
                // per-step label by real DailyStepKind, not a generic "Сегодня".
                TrainingMode.Daily -> if (state.daily?.currentStep?.kind == DailyStepKind.Practice) {
                    "Короткая проверка применения"
                } else {
                    "Сегодня · шаг ${(state.daily?.history?.size ?: 0) + 1} / ${state.daily?.planned ?: 0}"
                }
            }))
            appendChild(node("span", text = "${skill.level} · ${skill.title}"))
        })
        // M7 blueprint: prediction always precedes introduction for a pilot skill's first exposure.
        if (state.predictionPending && state.phase == CardPhase.Question) {
            card.appendChild(node("div", "card-front method-predict").apply {
                appendChild(node("span", "eyebrow", "Прежде чем узнать правило"))
                appendChild(node("p", "source-sentence").apply {
                    setAttribute("lang", activeTargetLangCode())
                    appendContrastParts(this, sentenceHighlightParts(exercise.source, exercise.changes, ChangeSide.Before), "change-before")
                })
                appendChild(node("p", text = skill.predictionQuestion.orEmpty()))
                appendChild(button("Что скажешь? Дальше", primary = true) {
                    dispatch(AppAction.ContinuePrediction)
                })
            })
            return
        }
        if (state.introPending && state.phase == CardPhase.Question) {
            card.appendChild(node("div", "card-front method-introduce").apply {
                appendChild(node("span", "eyebrow", if (state.styleId == StyleId.SituationFirst) "Сцена и намерение" else "Признаки и операция"))
                renderLifehackBadge(this, skill.id)
                appendChild(node("p", "source-sentence").apply {
                    setAttribute("lang", activeTargetLangCode())
                    appendContrastParts(this, sentenceHighlightParts(exercise.source, exercise.changes, ChangeSide.Before), "change-before")
                })
                appendChild(node("p", text = method.introduce))
                appendChild(button("Перейти к заданию", primary = true) {
                    dispatch(AppAction.ContinueIntroduction)
                })
            })
            return
        }
        val (effective, content) = resolveEffectiveStyleAndContent(state, skill.id)
        val frontBlocks = StyleComposer.compose(effective, StylePhase.Front, exercise, skill, presentation, content)
        val front = node("div", "card-front").apply {
            appendChild(node("span", "eyebrow", "Исходное предложение"))
            renderLifehackBadge(this, skill.id)
            state.activeRetrySkillId?.let { appendChild(node("span", "retry-badge", "Ещё раз: ${skill.title}")) }
            appendChild(node("p", "source-sentence").apply {
                setAttribute("lang", activeTargetLangCode())
                appendContrastParts(this, sentenceHighlightParts(exercise.source, exercise.changes, ChangeSide.Before), "change-before")
            })
            appendChild(node("div", "operation").apply {
                appendChild(node("span", text = if (state.styleId == StyleId.SituationFirst) "Ситуация" else "Преобразуй"))
                appendChild(node("h2", text = exercise.prompt))
                appendChild(node("p", "method-retrieve", method.retrieve))
                appendChild(node("small", "method-lead", method.promptLead))
            })
            // UC-10 web S2: which blocks show here (and in which order) comes entirely from the
            // resolved style's recipe — a style switch changes this list, never a hardcoded branch.
            renderCardBlocks(this, frontBlocks, "card-blocks-front", activeTargetLangCode())
        }
        if (state.phase == CardPhase.Question) {
            card.appendChild(front)
            // v3/A: clicking anywhere on the question card is a reveal trigger too, alongside the
            // existing button/Space (see the study-help hint below and the global keydown
            // handler) — excluding interactive descendants (the reveal button itself, the typed-
            // answer textarea/mode buttons) via the same tap-vs-drag gesture the vocabulary flip
            // uses, so a text-selection drag on the source sentence never fires it.
            if (!state.introPending && state.answerMode != AnswerMode.TileBuilder) installTapGesture(front) { state.exerciseId?.let { dispatch(AppAction.Reveal(it)) } }
            renderAnswerArea(card, state, dispatch)
            return
        }
        // v3/A: the answer expands downward below the still-visible question — no flip. The wrap
        // is only ever created once phase == Revealed, so the answer is never in the DOM (let
        // alone the accessibility tree) before that; `applyExpand` handles the one-shot animation
        // exactly at the reveal moment.
        card.appendChild(front)
        val wrap = node("div", "card-answer-wrap")
        card.appendChild(wrap)
        val back = node("div", "card-back")
        back.setAttribute("aria-live", "polite")
        wrap.appendChild(back)
        renderAnswerBack(root, card, wrap, back, state, swipeRatingEnabled, dispatch)
        applyExpand(wrap, isRevealEvent = justRevealed)
    }

    /**
     * Expands [wrap]'s grid row from 0fr to 1fr (the standard trick for animating to/from an
     * intrinsic "auto" height — a plain `height` transition can't do it) when [isRevealEvent] is
     * true and motion isn't instant; otherwise jumps straight to expanded — a routine DOM rebuild
     * of an already-revealed card (e.g. the 30s refresh timer) must never replay the reveal.
     * `will-change` in `training.css` promotes the compositor layer ahead of time (v3/D); the
     * forced layout read below is the same FC2-01 fix ported from the flip: a freshly created
     * node has no previously-painted frame to transition from, so without it the very first
     * reveal on a page would snap instead of animating even though every LATER one already did.
     */
    private fun applyExpand(wrap: HTMLElement, isRevealEvent: Boolean) {
        wrap.classList.add("expanded")
        if (!isRevealEvent || motionInstantActive()) {
            wrap.style.setProperty("transition", "none")
            wrap.style.setProperty("grid-template-rows", "1fr")
            return
        }
        wrap.classList.add("revealing") // gates the CSS stagger fade-in for this render only
        // The rating row moves while the answer expands. A pointer can press the button at its
        // old position and release over the card after it has moved, losing the click in WebKit.
        // Restore pointer hits when the row finishes or is canceled (e.g. Motion changes).
        // The bound fallback also releases a detached/paused row without depending on CSS events.
        val fallback = window.setTimeout({ wrap.classList.remove("revealing"); null }, 750)
        val releaseWhenRatingsFinish: (Event) -> Unit = { event ->
            if ((event.target as? HTMLElement)?.classList?.contains("ratings") == true) {
                window.clearTimeout(fallback)
                wrap.classList.remove("revealing")
            }
        }
        wrap.addEventListener("animationend", releaseWhenRatingsFinish)
        wrap.addEventListener("animationcancel", releaseWhenRatingsFinish)
        wrap.style.setProperty("transition", "none")
        wrap.style.setProperty("grid-template-rows", "0fr")
        wrap.getBoundingClientRect() // force layout: commits the collapsed frame before animating
        wrap.style.setProperty("transition", "")
        wrap.style.setProperty("grid-template-rows", "")
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
        // SB6-AC1/AC2: disabled exactly when this exercise has no derived TileBuilderState
        // (TrainingStore's own fail-soft — AppUiState.tileBuilder is `null`); the UI never
        // re-derives eligibility itself.
        modes.appendChild(button(
            "Собрать из слов",
            active = state.answerMode == AnswerMode.TileBuilder,
            pressed = state.answerMode == AnswerMode.TileBuilder,
        ) { dispatch(AppAction.SetAnswerMode(AnswerMode.TileBuilder)) }.apply { disabled = state.tileBuilder == null })
        val tileBuilder = state.tileBuilder
        val exerciseId = state.exerciseId
        when {
            state.answerMode == AnswerMode.Typed -> {
                val input = document.createElement("textarea") as HTMLTextAreaElement
                input.id = "training-answer"
                input.setAttribute("aria-label", "Ответ ${activeTargetAdverb()}")
                input.placeholder = "Напиши целое предложение…"
                input.value = state.draft
                input.addEventListener("input", { dispatch(AppAction.EditAnswer(input.value)) })
                input.addEventListener("keydown", { raw ->
                    val event = raw as KeyboardEvent
                    if (event.key == "Enter" && !event.shiftKey && !event.isComposing && !isComposing()) {
                        event.preventDefault()
                        state.exerciseId?.let { dispatch(AppAction.Reveal(it)) }
                    }
                })
                area.appendChild(input)
            }
            // SB6-AC2: a stale TileBuilder selection on a card that opts out (e.g. a restored
            // session) falls back to the Oral prompt below, never a broken/empty tile row.
            state.answerMode == AnswerMode.TileBuilder && tileBuilder != null && exerciseId != null -> {
                val checkButton = renderTileBuilderSection(
                    area, exerciseId, tileBuilder, openTilePicker, openTileError,
                    activeTargetLangCode(), { openTilePicker = it }, { openTileError = it }, dispatch,
                )
                area.appendChild(checkButton)
                return
            }
            else -> area.appendChild(node("p", "muted", "Произнеси целое предложение, затем переверни карточку."))
        }
        area.appendChild(button(if (state.answerMode == AnswerMode.Typed) UiCopy.CheckAndShowAnswer else UiCopy.ShowAnswer, primary = true) {
            state.exerciseId?.let { dispatch(AppAction.Reveal(it)) }
        }.apply { id = "training-reveal"; className += " reveal-button" })
    }

    private fun renderAnswerBack(root: HTMLElement, card: HTMLElement, wrap: HTMLElement, back: HTMLElement, state: AppUiState, swipeRatingEnabled: Boolean, dispatch: (AppAction) -> Unit) {
        val exercise = state.exercise ?: return
        val model = trainingCardModel(state)
        back.appendChild(node("span", "eyebrow", UiCopy.BackSideReference))
        back.appendChild(node("p", "answer-sentence").apply {
            setAttribute("lang", activeTargetLangCode())
            appendContrastParts(this, sentenceHighlightParts(exercise.expected, exercise.changes, ChangeSide.After), "change-after")
        })
        if (exercise.accepted.isNotEmpty()) {
            back.appendChild(node("p", "accepted", "Также: ${exercise.accepted.joinToString(" / ")}").apply { setAttribute("lang", activeTargetLangCode()) })
        }
        if (state.answerMode == AnswerMode.Typed) {
            val result = state.evaluation?.correct == true
            back.appendChild(node("div", if (result) "typed-result correct" else "typed-result incorrect").apply {
                appendChild(node("strong", text = if (result) UiCopy.Matches else UiCopy.CompareAnswer))
                appendChild(node("p", text = state.frozenAnswer?.takeIf(String::isNotEmpty) ?: UiCopy.NoAnswerEntered))
            })
        }
        // SB-6 blueprint §2.2/§2.3: the Revealed face of a TileBuilder card — read-only
        // green/red tile row, the tapped error's mini-rule panel, and its own correctness banner
        // (appended by renderTileBuilderSection itself). The returned Check button is unused here
        // — Check is only live in the Question-phase renderAnswerArea.
        val tileBuilder = state.tileBuilder
        if (state.answerMode == AnswerMode.TileBuilder && tileBuilder != null) {
            renderTileBuilderSection(
                back, exercise.id, tileBuilder, openTilePicker, openTileError,
                activeTargetLangCode(), { openTilePicker = it }, { openTileError = it }, dispatch,
            )
        }
        val mf = model.back.filterIsInstance<CardSection.MethodFeedback>().firstOrNull()
        if (mf != null) back.appendChild(node("div", "method-feedback").apply {
            appendChild(node("h3", text = mf.heading))
            appendChild(node("p", text = mf.feedback))
            if (mf.explanation.isNotEmpty()) appendChild(node("p", text = mf.explanation))
        })
        val styledBlocks = model.back.filterIsInstance<CardSection.Styled>().map { it.block }
        renderCardBlocks(back, styledBlocks, "card-blocks-back", activeTargetLangCode())
        val lifehacks = model.back.filterIsInstance<CardSection.LifehackSection>().firstOrNull()
        if (lifehacks != null) renderLifehackBlock(back, exercise.primarySkill)
        back.appendChild(node("div", "rating-label", UiCopy.WhenToReview))
        val reviewHint = model.back.filterIsInstance<CardSection.ReviewHint>().firstOrNull()
        if (reviewHint != null) back.appendChild(node("p", "method-review", reviewHint.text))
        val ratings = node("div", "ratings")
        back.appendChild(ratings)
        val ratingHints = mapOf(Rating.Again to "Ошибка или не уверен", Rating.Good to "Воспроизвёл сам")
        model.ratingButtons.forEachIndexed { index, rb ->
            ratings.appendChild(button("${index + 1} ${rb.label}", extraClass = "rating-${rb.rating.name.lowercase()}") {
                riveOverlay.trigger(root, back, cardEffectFor(rb.rating))
                dispatch(AppAction.Rate(exercise.id, rb.rating))
            }.apply {
                appendChild(node("small", text = ratingHints[rb.rating].orEmpty()))
                appendChild(node("span", text = rb.intervalLabel))
            })
        }
        back.appendChild(node("p", "muted small", "Оценка планирует следующее повторение навыка."))
        if (swipeRatingEnabled) {
            // v4/UX4-08/09: the whole revealed back-face is the element that tilts/translates
            // with the finger/mouse — this label is a purely visual direction hint, not the
            // gesture target itself. The gesture LISTENERS live one level up, on the stable
            // `.card-answer-wrap` (never itself transformed), not on `back` — see
            // `installSwipeCard`'s own doc comment for why: `back` briefly leaves its own resting
            // hitbox while snapping back/flying out, which could otherwise make a second gesture
            // started right at its edge within that ~220ms window miss it entirely.
            back.appendChild(node("p", "vocabulary-swipe-zone muted small", "← Повторить · Вспомнил →").apply {
                setAttribute("aria-hidden", "true")
            })
            appendSwipeLabels(back)
            // P1-8: `card` (the whole `.flashcard` section: meta + question + answer) is now the
            // element that tilts/translates — `back` alone used to move outside `.flashcard`'s own
            // `overflow:hidden` and get clipped, while the question stayed put; the request itself
            // asks for the whole panel to move, matching the vocabulary card's whole-object flip.
            installSwipeCard(wrap, card) { remembered ->
                val rating = if (remembered) Rating.Good else Rating.Again
                riveOverlay.trigger(root, back, cardEffectFor(rating))
                dispatch(AppAction.Rate(exercise.id, rating))
            }
        }
    }
}

/**
 * UX4-20/21: the bidirectional sibling of [TrainingCardDom.applyExpand] — shared by content
 * that already exists in the DOM before and after collapsing (the vocabulary catalog, the
 * case-reference panel), unlike the answer reveal ([TrainingCardDom.applyExpand]'s only
 * caller), which never collapses back and whose target face isn't mounted at all until the
 * reveal. Toggling the same `grid-template-rows` CSS class both ways lets the one stylesheet
 * transition animate whichever direction is current — no separate collapse-only code path.
 * `inert`/`aria-hidden` track the collapsed state directly (unlike the reveal case, this content
 * was already live/focusable before collapsing, so it must leave the tab order and a11y tree
 * exactly when collapsed, not merely never having entered them).
 */
internal fun applyCollapsible(wrap: HTMLElement, expanded: Boolean, isToggleEvent: Boolean) {
    wrap.classList.toggle("expanded", expanded)
    if (expanded) { wrap.removeAttribute("inert"); wrap.removeAttribute("aria-hidden") }
    else { wrap.setAttribute("inert", ""); wrap.setAttribute("aria-hidden", "true") }
    if (!isToggleEvent || motionInstantActive()) {
        wrap.style.setProperty("transition", "none")
        wrap.style.setProperty("grid-template-rows", if (expanded) "1fr" else "0fr")
        return
    }
    wrap.style.setProperty("transition", "none")
    wrap.style.setProperty("grid-template-rows", if (expanded) "0fr" else "1fr") // the OLD state
    wrap.getBoundingClientRect() // force layout: commits the "from" frame before animating
    wrap.style.setProperty("transition", "")
    wrap.style.setProperty("grid-template-rows", "")
}

/**
 * UC-10 web S2: the [StyleRecipe] a card actually composes from — [StyleId.NativeContrast]
 * without authored [polski.data.SkillStyleContent.nativeParallel] for this skill resolves to its
 * declared fallback ([StyleComposer.resolveEffectiveStyle]), same rule Front and Back both use.
 */
private fun resolveEffectiveStyleAndContent(state: AppUiState, skillId: String): Pair<StyleRecipe, polski.data.SkillStyleContent> {
    val content = polski.data.styleContentBySkillId(skillId)
    val recipe = StyleRegistry.recipes.getValue(state.styleId)
    val effectiveId = StyleComposer.resolveEffectiveStyle(recipe, content, StyleRegistry.recipes)
    return StyleRegistry.recipes.getValue(effectiveId) to content
}

// UX4-13: intervalLabel now shared as polski.presentation.intervalLabel (IntervalLabel.kt).
// The wildcard import `polski.presentation.*` at the top of this file brings it in.

// UC-10 web S2: shared with CardBlocksWeb.kt's block renderers (Table/Contrast/Changes reuse the
// exact same ending-highlight markup this file has always used), so file-private isn't enough.
// [changedClass] is only the fallback for a part with no [EndingPart.side] of its own (none of
// today's callers construct one, but nothing here requires they do): a part that does carry a
// side is a mixed-role block's own signal for which role it plays (Emphasis contract: "before" =
// warm/dashed, "after" = cool/solid) and always wins over the caller's single class, since one
// block's running text (Formula/Rule/Scene/Examples/WhyOnDemand/NativeParallel-target) can hold a
// literal focus.before span and a literal focus.after span side by side (W3 correction, blocker 2).
internal fun appendContrastParts(container: HTMLElement, parts: List<EndingPart>, changedClass: String) {
    parts.forEach { part ->
        if (part.isChanged) {
            val cls = when (part.side) {
                ChangeSide.Before -> "change-before"
                ChangeSide.After -> "change-after"
                null -> changedClass
            }
            container.appendChild(node("span", if (part.isEnding && cls == "change-after") "$cls ending-highlight" else cls, part.text))
        } else container.appendChild(document.createTextNode(part.text))
    }
}

internal fun node(tag: String, className: String = "", text: String? = null): HTMLElement =
    (document.createElement(tag) as HTMLElement).apply {
        this.className = className
        if (text != null) textContent = text
    }

internal fun button(
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
