package polski.ui

import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import polski.core.model.TileSegment
import polski.presentation.AppAction
import polski.presentation.TileAction
import polski.presentation.TileBuilderState
import polski.presentation.UiCopy

/**
 * SB-6 blueprint §2: renders one [TileBuilderState] card — the tile row, its single inline
 * picker/mini-rule panel, and (Revealed only) the correctness banner. Called from
 * `TrainingCardDom.renderAnswerArea` (Question, [TileBuilderState.result] is `null`, the returned
 * "Проверить" button is appended by the caller) and from `renderAnswerBack` (Revealed,
 * [TileBuilderState.result] is non-null, the returned button is unused — Check is no longer live).
 *
 * [openPicker]/[openError] are display-only "which panel is open" state owned by `TrainingCardDom`
 * (not [TileBuilderState] — SB-4's shared contract has no such field, correctly, since it's cross-
 * host learning state that must not carry this per-render web concern). Toggling happens entirely
 * in the DOM via the existing `applyCollapsible` 0fr/1fr trick (no store dispatch, no full
 * re-render) — [onTogglePicker]/[onToggleError] only report the new value back so a LATER full
 * rebuild (e.g. a Place/Check dispatch) restores the same open panel.
 */
internal fun renderTileBuilderSection(
    area: HTMLElement,
    exerciseId: String,
    builder: TileBuilderState,
    openPicker: Int?,
    openError: Int?,
    langCode: String,
    onTogglePicker: (Int?) -> Unit,
    onToggleError: (Int?) -> Unit,
    dispatch: (AppAction) -> Unit,
): HTMLElement {
    val result = builder.result
    val segments = builder.exercise.segments.sortedBy { it.position }
    val choices = segments.filterIsInstance<TileSegment.Choice>()

    val row = node("div", "tile-row")
    row.setAttribute("aria-label", "Собери предложение")
    row.setAttribute("lang", langCode)
    area.appendChild(row)

    // The one inline panel beneath the row: form candidates (Question) or the tapped mini-rule
    // (Revealed) — only one ever exists, matching SB6-AC3's "only one picker open at a time".
    val panel = node("div", if (result == null) "tile-picker collapsible" else "collapsible")
    area.appendChild(panel)
    val panelInner = node("div")
    panel.appendChild(panelInner)

    var currentOpenPicker = openPicker
    var currentOpenError = openError
    val choiceButtons = mutableMapOf<Int, HTMLButtonElement>()
    val errorButtons = mutableMapOf<Int, HTMLButtonElement>()

    fun renderPickerCandidates(choice: TileSegment.Choice) {
        panelInner.textContent = ""
        // SB-6 blueprint §2.1: the shared state's own order — generated once by TileGenerator,
        // never reshuffled here, so re-renders of the same open picker never reorder under a tap.
        (listOf(choice.correctForm) + choice.alternatives).forEach { form ->
            panelInner.appendChild(button(form, extraClass = "tile tile-option") {
                dispatch(AppAction.Tile(exerciseId, TileAction.Place(choice.position, form)))
                currentOpenPicker = null
                onTogglePicker(null)
                choiceButtons[choice.position]?.setAttribute("aria-expanded", "false")
                applyCollapsible(panel, expanded = false, isToggleEvent = true)
            })
        }
    }

    fun togglePicker(choice: TileSegment.Choice) {
        val opening = currentOpenPicker != choice.position
        choiceButtons.values.forEach { it.setAttribute("aria-expanded", "false") }
        currentOpenPicker = if (opening) choice.position else null
        onTogglePicker(currentOpenPicker)
        if (opening) {
            renderPickerCandidates(choice)
            choiceButtons[choice.position]?.setAttribute("aria-expanded", "true")
        }
        applyCollapsible(panel, expanded = opening, isToggleEvent = true)
    }

    fun toggleError(position: Int) {
        val opening = currentOpenError != position
        currentOpenError = if (opening) position else null
        onToggleError(currentOpenError)
        errorButtons.forEach { (pos, btn) -> btn.setAttribute("aria-expanded", (pos == currentOpenError).toString()) }
        if (opening) {
            dispatch(AppAction.Tile(exerciseId, TileAction.TapError(position)))
        } else {
            panelInner.textContent = ""
            applyCollapsible(panel, expanded = false, isToggleEvent = true)
        }
    }

    segments.forEach { segment ->
        when (segment) {
            is TileSegment.Locked -> row.appendChild(node("span", "tile tile-locked", segment.text))
            is TileSegment.Choice -> {
                val placedForm = builder.placed.firstOrNull { it.position == segment.position }?.selectedForm
                val error = result?.errors?.firstOrNull { it.position == segment.position }
                when {
                    result == null -> {
                        val tile = button(
                            placedForm ?: "___",
                            extraClass = "tile tile-choice" + if (placedForm == null) " tile-empty" else "",
                        ) { togglePicker(segment) }
                        tile.setAttribute("aria-expanded", (currentOpenPicker == segment.position).toString())
                        choiceButtons[segment.position] = tile
                        row.appendChild(tile)
                    }
                    error == null && placedForm != null -> row.appendChild(node("span", "tile tile-correct", placedForm).apply {
                        setAttribute("aria-label", "$placedForm — верно")
                    })
                    error == null -> row.appendChild(node("span", "tile tile-choice tile-empty", "___"))
                    else -> {
                        val tile = button(placedForm.orEmpty(), extraClass = "tile tile-incorrect") { toggleError(segment.position) }
                        tile.setAttribute("aria-expanded", (currentOpenError == segment.position).toString())
                        tile.setAttribute("aria-label", "$placedForm — неверно, нажми для правила")
                        errorButtons[segment.position] = tile
                        row.appendChild(tile)
                    }
                }
            }
        }
    }

    if (result == null) {
        val openChoice = choices.firstOrNull { it.position == currentOpenPicker }
        if (openChoice != null) renderPickerCandidates(openChoice)
        applyCollapsible(panel, expanded = openChoice != null, isToggleEvent = false)
    } else {
        val explanationOpen = currentOpenError != null && builder.errorExplanation != null
        if (explanationOpen) renderCardBlocks(panelInner, builder.errorExplanation.orEmpty(), "card-blocks-tile-error", langCode)
        applyCollapsible(panel, expanded = explanationOpen, isToggleEvent = false)
        // SB6-AC5: mirrors Typed mode's own banner classes/markup exactly (TrainingCardDom.renderAnswerBack).
        area.appendChild(node("div", if (result.correct) "typed-result correct" else "typed-result incorrect").apply {
            appendChild(node("strong", text = if (result.correct) UiCopy.Matches else UiCopy.CompareAnswer))
        })
    }

    // SB-6 blueprint §2.4: distinct from the shared `#training-reveal` button; disabled exactly
    // when accepts()'s own Check guard (TrainingStore.kt §1.2) would reject it.
    val checkButton = button("Проверить", primary = true) {
        dispatch(AppAction.Tile(exerciseId, TileAction.Check))
    }
    checkButton.className += " reveal-button"
    checkButton.disabled = choices.any { c -> builder.placed.none { it.position == c.position } }
    return checkButton
}
