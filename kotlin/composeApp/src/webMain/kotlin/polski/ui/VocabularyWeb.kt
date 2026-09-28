package polski.ui

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.StorageEvent
import org.w3c.dom.events.Event
import kotlin.random.Random
import kotlin.time.Clock
import polski.data.activeCoursePackId
import polski.data.availableCoursePacks
import polski.data.courseVocabularyInstructions
import polski.data.courseVocabularyUnavailableLabel
import polski.data.VocabularyItem
import polski.data.frequencyItems
import polski.data.vocabularyItems
import polski.presentation.cardEffectFor
import polski.srs.FsrsScheduler
import polski.srs.Rating
import polski.vocabulary.StudyDirection
import polski.vocabulary.VocabularyCodec
import polski.vocabulary.VocabularyDocument

/** EnRuAcceptance-2026-09-28.md §7 item 4: one backup slot per active pack — was a single literal
 *  pl-ru key shared by every pack, so an en-ru import backup would silently overwrite pl-ru's (or
 *  vice versa) instead of getting its own, same reasoning as [VocabularyCodec.key] itself. */
private val VOCABULARY_BACKUP_KEY: String get() = "polski-vocabulary-$activeCoursePackId-v1-backup"

/** EnRuAcceptance-2026-09-28.md §7 item 3/4: the active pack's own target/native language, for
 *  every label below that used to hardcode "польский"/"русский". Lowercase nominative adjective
 *  forms (matching "Эталон · польский"'s own agreement) coincide with the capitalized noun label
 *  for every language this build embeds. */
private val languageAdjective: Map<String, String> = mapOf("pl" to "польский", "en" to "английский", "ru" to "русский")
private val languageAdverb: Map<String, String> = mapOf("pl" to "по-польски", "en" to "по-английски", "ru" to "по-русски")
private val languageBcp47: Map<String, String> = mapOf("pl" to "pl", "en" to "en", "ru" to "ru")

private data class ActivePackLanguages(val targetCode: String, val nativeCode: String) {
    val targetAdjective get() = languageAdjective[targetCode] ?: targetCode
    val nativeAdjective get() = languageAdjective[nativeCode] ?: nativeCode
    val targetAdverb get() = languageAdverb[targetCode] ?: targetCode
    val nativeAdverb get() = languageAdverb[nativeCode] ?: nativeCode
    val targetLang get() = languageBcp47[targetCode] ?: targetCode
    val nativeLang get() = languageBcp47[nativeCode] ?: nativeCode
}

private fun activePackLanguages(): ActivePackLanguages {
    val pack = availableCoursePacks.firstOrNull { it.pairId == activeCoursePackId }
    return ActivePackLanguages(pack?.target ?: "pl", pack?.native ?: "ru")
}

/** Browser adapter for the shared vocabulary document; it never writes legacy grammar progress. */
internal class VocabularyWebController {
    private val scheduler = FsrsScheduler()
    private var document: VocabularyDocument = VocabularyDocument()
    private var recoveryRaw: String? = null
    private var error: String? = null
    private var direction = StudyDirection.RussianToPolish
    private var filter = "A1"
    private var catalogVisible = true
    // v4/UX4-20: true only for the render() call right after the catalog toggle button was
    // clicked — a routine render (e.g. a rating that leaves the catalog exactly as it was) must
    // resume the current collapsed/expanded state instantly, never replay the transition. Consumed
    // (reset to false) by the very next renderCatalog() call, same pattern as `justRevealedFlip`.
    private var catalogToggledThisRender = false
    private var revealed = false
    // v3/B: real 3D flip, reusing the exact same tested behaviour as the vocabulary flip's own
    // extracted [FlipCard] (originally the training card's, see CardFlip.kt) — purely visual,
    // never touches `revealed`/FSRS review state (same contract the training card's flip had).
    private val flipCard = FlipCard()
    private var flipped = false
    private var flippedItemId: String? = null
    // UX5: the currently-mounted (inner, front, answer) triple of the revealed flip, set at the
    // end of every revealed `renderCard` — lets keyboard (Space) toggle the same already-mounted
    // flip a click already can, without a full page rebuild (matching the click handler's own
    // direct-DOM-mutation, no-refresh design; see `toggleFlip`).
    private var flipNodes: Triple<HTMLElement, HTMLElement, HTMLElement>? = null
    // True only for the render() call right after the reveal button/Enter was pressed — a
    // routine refresh() of an already-revealed card (e.g. a rating that leaves the same item due
    // again, or a cross-tab storage reload) must resume the current face instantly, never replay
    // the flip animation. Consumed (reset to false) by the very next renderCard() call.
    private var justRevealedFlip = false
    private val riveOverlay = RiveEffectOverlay()
    private var typed = false
    private var draft = ""
    private var editing: String? = null
    private var editor = EditorDraft()
    private var importText = ""
    private var pendingRefresh: (() -> Unit)? = null
    // EnRuAcceptance-2026-09-28.md §7 item 3/4: which pack [document]/[direction] were last loaded
    // for — this controller is a long-lived singleton (`TrainingDomRenderer`'s own field, never
    // rebuilt on a Settings pack switch, see that class's own KDoc), so [render] below self-heals
    // by reloading whenever the active pack no longer matches, the same "detect the switch at the
    // entry point" pattern ADR-37 already applied to the macOS/iOS session bridges.
    private var loadedForPackId: String = activeCoursePackId
    private val storageListener: (Event) -> Unit = { raw ->
        // Fires only in OTHER tabs of this origin; the writing tab never sees its own event.
        val event = raw as? StorageEvent
        if (event == null || event.key == VocabularyCodec.key || event.key == null) reload()
    }

    init {
        reload()
        window.addEventListener("storage", storageListener)
    }

    /** Stop listening for cross-tab writes; call once when the host unmounts. */
    fun close() {
        window.removeEventListener("storage", storageListener)
    }

    /**
     * Drop the render callback captured by the last [render] call. The host calls this once it
     * has navigated away from the Vocabulary route, so a later cross-tab [reload] cannot replay a
     * closure that still captures the old route/state and would redraw the wrong page.
     */
    fun deactivate() {
        pendingRefresh = null
    }

    /** See [loadedForPackId]'s own KDoc. Resets the per-pack UI state a routine cross-tab [reload]
     *  must NOT touch (a plain storage-event reload keeps whichever card/direction is on screen). */
    private fun reloadForActivePack() {
        loadedForPackId = activeCoursePackId
        direction = StudyDirection.RussianToPolish
        revealed = false; flipped = false; flippedItemId = null; draft = ""
        reload()
    }

    private fun reload() {
        val raw = runCatching { window.localStorage.getItem(VocabularyCodec.key) }.getOrElse {
            error = "Не удалось открыть хранилище словаря: ${it.message}"
            null
        }
        if (raw != null) runCatching { VocabularyCodec.decode(raw) }.onSuccess {
            document = it
            recoveryRaw = null
            error = null
        }.onFailure {
            recoveryRaw = raw
            error = "Словарь нужно восстановить: ${it.message}"
        }
        pendingRefresh?.invoke()
    }

    fun render(root: HTMLElement, outerRoot: HTMLElement, swipeRatingEnabled: Boolean, refresh: () -> Unit) {
        pendingRefresh = refresh
        if (loadedForPackId != activeCoursePackId) reloadForActivePack()
        root.className = "vocabulary-page"
        val page = root.add("section", cls = "vocabulary-view")
        page.setAttribute("aria-label", "Тренировка слов")
        val heading = page.add("div", cls = "vocabulary-heading")
        heading.add("div").apply {
            add("h2", "Слова и выражения")
            add("p", "Отмечай слова в каталоге. Узнавание и воспроизведение повторяются по отдельным расписаниям.")
        }
        val languages = activePackLanguages()
        val directionLabel = heading.add("label", "Направление")
        val directionSelect = directionLabel.select("Направление карточки", listOf(
            StudyDirection.RussianToPolish.wire to "${languages.nativeAdjective.replaceFirstChar(Char::uppercase)} → ${languages.targetAdjective}",
            StudyDirection.PolishToRussian.wire to "${languages.targetAdjective.replaceFirstChar(Char::uppercase)} → ${languages.nativeAdjective}",
        ), direction.wire)
        directionSelect.addEventListener("change", {
            direction = if (directionSelect.value == "pl-ru") StudyDirection.PolishToRussian else StudyDirection.RussianToPolish
            revealed = false; draft = ""; refresh()
        })
        error?.let { page.add("p", it, "notice").setAttribute("role", "alert") }
        recoveryRaw?.let { raw ->
            page.button("Сохранить исходный JSON") { download("vocabulary-recovery.json", raw) }
        }
        val layout = page.add("div", cls = "vocabulary-layout")
        renderCard(layout.add("section", cls = "card vocabulary-card"), outerRoot, swipeRatingEnabled, languages, refresh)
        renderCatalog(layout.add("section", cls = "card vocabulary-catalog"), languages, refresh)
        page.add("p", cls = "muted small").apply {
            add("span", "Частотные ранги и counts: ")
            add("a", "Leksjo / NKJP, CC BY 4.0").apply {
                setAttribute("href", "https://github.com/KubaCiolo/leksjo-dane/blob/01782aa92cc842d0d3199079eba47ecbf05879e1/dane/nkjp-frekwencja.csv")
                setAttribute("target", "_blank")
                setAttribute("rel", "noreferrer")
            }
            add("span", ". Изменения: взяты первые 1000 лемм, рангов и counts.")
        }
    }

    private fun renderCard(section: HTMLElement, outerRoot: HTMLElement, swipeRatingEnabled: Boolean, languages: ActivePackLanguages, refresh: () -> Unit) {
        section.setAttribute("aria-label", "Карточка слова")
        val id = VocabularyCodec.dueIds(document, direction, scheduler, Clock.System.now()).firstOrNull()
        val item = id?.let { VocabularyCodec.item(document, it) }
        if (item == null) {
            // P0-2: give the empty state a panel too — a bare heading/paragraph directly on the
            // page background is the same "no panel" bug as the unrevealed front used to have.
            val face = section.add("div", cls = "card-front card-face")
            face.add("h3", if (document.selectedIds.isEmpty()) "Выбери слова для тренировки" else "На сейчас всё повторено")
            face.add("p", "Отметь готовые карточки в каталоге. История каждого направления сохраняется отдельно.")
            return
        }
        // The `flipped` visual state is host-local and keyed to the item on screen — a brand-new
        // due item (after a rating advances, or the direction toggle swaps decks) always starts
        // question-side-up, never inheriting the previous item's face.
        if (id != flippedItemId) { flipped = false; flippedItemId = id; flipCard.reset() }
        // P0-2: the flip wrapper (and the front face's own panel chrome) exists from the very
        // first unrevealed render, not only once revealed — otherwise the panel visibly springs
        // into existence the moment the card is revealed instead of the same object rotating.
        val flip = detachedElement("div", "card-flip")
        val inner = detachedElement("div", "card-flip-inner")
        flip.appendChild(inner)
        val front = detachedElement("div", "card-front card-face")
        // UX5: the reveal target is only the prompt block, never `front` as a whole — `front`
        // always also holds the two real mode-switch buttons (and, in typed mode, the textarea),
        // so giving IT `role="button"` too would nest a custom button role around other real
        // interactive controls. `front` itself stays a plain container; ITS tap gesture (below)
        // is the mouse/touch convenience that reveals from a click anywhere else on the card, and
        // is not itself an accessibility affordance (it excludes editable/button descendants via
        // `installTapGesture`'s own `editableTarget` guard, same as training's question card).
        val promptBlock = front.add("div", cls = "vocabulary-prompt-block")
        promptBlock.add("span", "Вспомни ${if (direction == StudyDirection.RussianToPolish) languages.targetAdverb else languages.nativeAdverb}", "eyebrow")
        promptBlock.add("p", if (direction == StudyDirection.RussianToPolish) item.translation else item.lemma, "vocabulary-prompt")
            .setAttribute("lang", if (direction == StudyDirection.RussianToPolish) languages.nativeLang else languages.targetLang)
        if (!revealed) {
            // UX5: no "Показать ответ" button in oral mode any more — the whole card reveals on a
            // click/tap (installTapGesture below) or Space (VocabularyWebController.spaceReveal);
            // `promptBlock` carries the keyboard-focusable/screen-reader affordance that used to
            // live on that button, under the same accessible name.
            promptBlock.setAttribute("role", "button")
            promptBlock.setAttribute("tabindex", "0")
            promptBlock.setAttribute("aria-label", "Показать ответ")
            val modes = front.add("div", cls = "answer-mode")
            modes.button("Ответ вслух / про себя") { typed = false; refresh() }.apply {
                setAttribute("aria-pressed", (!typed).toString())
                classList.toggle("active", !typed) // UX4/P2-12: same selected-state look as training's mode buttons
            }
            modes.button("Напечатать ответ") { typed = true; refresh() }.apply {
                setAttribute("aria-pressed", typed.toString())
                classList.toggle("active", typed)
            }
            if (typed) {
                val input = front.add("textarea") as HTMLTextAreaElement
                input.id = "vocabulary-answer"
                input.setAttribute("aria-label", "Ответ на карточку слова")
                input.value = draft
                input.addEventListener("input", { draft = input.value })
                input.addEventListener("keydown", { event ->
                    val key = event as org.w3c.dom.events.KeyboardEvent
                    if (key.key == "Enter" && !key.shiftKey && !key.isComposing) {
                        key.preventDefault(); revealAndFlip(refresh)
                    }
                })
                // UX5: typed mode keeps an explicit submit action ("Проверить") — a click/tap
                // elsewhere on the card (not in the textarea/mode buttons) also reveals, exactly
                // like oral mode, but typing an answer is a deliberate enough action that it gets
                // its own named control too, not just the ambient tap.
                front.button("Проверить", "primary reveal-button") { revealAndFlip(refresh) }
            }
            // Click/tap anywhere on the front except the mode buttons/textarea reveals — the same
            // tap-vs-drag gesture training's question card and this card's own flip-back use, so a
            // text-selection drag never fires it and clicks inside controls never flip (editableTarget).
            installTapGesture(front) { revealAndFlip(refresh) }
            inner.appendChild(front)
            section.appendChild(flip)
            return
        }
        // v3/B: revealed — both faces mount at once in a real 3D flip wrapper, reusing the exact
        // `FlipCard` helper the training card's flip used. Click toggles which face is forward,
        // purely visually; swipe/keyboard on the revealed face rates (v4/UX4-08/09/14).
        val answer = detachedElement("div", "vocabulary-answer card-back card-face")
        answer.setAttribute("aria-live", "polite")
        renderRevealedAnswer(flip, answer, outerRoot, item, swipeRatingEnabled, languages, refresh)
        inner.appendChild(front)
        inner.appendChild(answer)
        section.appendChild(flip)
        val isFlipEvent = justRevealedFlip
        justRevealedFlip = false
        flipCard.apply(inner, front, answer, toFlipped = flipped, isFlipEvent = isFlipEvent)
        flipNodes = Triple(inner, front, answer)
        flipCard.installTap(flip) { toggleFlip() }
    }

    private fun renderRevealedAnswer(flip: HTMLElement, answer: HTMLElement, outerRoot: HTMLElement, item: VocabularyItem, swipeRatingEnabled: Boolean, languages: ActivePackLanguages, refresh: () -> Unit) {
        answer.add("span", "Эталон · ${if (direction == StudyDirection.RussianToPolish) languages.targetAdjective else languages.nativeAdjective}", "eyebrow")
        answer.add("p", if (direction == StudyDirection.RussianToPolish) item.lemma else item.translation)
            .setAttribute("lang", if (direction == StudyDirection.RussianToPolish) languages.targetLang else languages.nativeLang)
        val description = answer.add("dl")
        description.detail("Перевод", item.translation)
        description.detail("Форма", item.form, languages.targetLang)
        description.detail("В предложении", item.example, languages.targetLang)
        if (typed) answer.add("p", "Твой ответ: ${draft.ifBlank { "не введён" }}. Сравни сам и выбери оценку.")
        if (swipeRatingEnabled) {
            // v4/UX4-08/09: the whole revealed face is the element that tilts/translates with the
            // finger/mouse — this label is a purely visual direction hint, not the gesture target
            // itself. The gesture LISTENERS live on the stable `.card-flip` (never itself
            // transformed — only `.card-flip-inner`'s rotateY drives the flip), not on `answer` —
            // see `installSwipeCard`'s own doc comment for why, and `baseTransform` for how this
            // face's own resting `rotateY(180deg)` (undoing the flip's mirroring) survives every
            // drag/settle transform this sets rather than being clobbered by it.
            answer.add("p", "← Повторить · Вспомнил →", "vocabulary-swipe-zone muted small").apply {
                setAttribute("aria-hidden", "true")
            }
            appendSwipeLabels(answer)
            installSwipeCard(flip, answer, baseTransform = "rotateY(180deg)") { remembered ->
                val rating = if (remembered) Rating.Good else Rating.Again
                riveOverlay.trigger(outerRoot, answer, cardEffectFor(rating))
                rate(item.id, rating, refresh)
            }
        }
        // UX4-13: the same hint+interval structure the training card's rating buttons already
        // show — `VocabularyCodec.preview` routes through the real scheduler without writing
        // anything, exactly mirroring what a real review would schedule (see its own doc comment).
        val at = Clock.System.now()
        val preview = VocabularyCodec.preview(document, item.id, direction, scheduler, at)
        val ratings = answer.add("div", cls = "ratings")
        ratings.button("Повторить", hint = "Ошибка или не уверен", interval = intervalLabel(preview[Rating.Again].toEpochMilliseconds(), at.toEpochMilliseconds()), cls = "rating-again") {
            riveOverlay.trigger(outerRoot, answer, cardEffectFor(Rating.Again)); rate(item.id, Rating.Again, refresh)
        }
        ratings.button("Вспомнил", hint = "Воспроизвёл сам", interval = intervalLabel(preview[Rating.Good].toEpochMilliseconds(), at.toEpochMilliseconds()), cls = "rating-good") {
            riveOverlay.trigger(outerRoot, answer, cardEffectFor(Rating.Good)); rate(item.id, Rating.Good, refresh)
        }
    }

    private fun renderCatalog(section: HTMLElement, languages: ActivePackLanguages, refresh: () -> Unit) {
        val header = section.add("div", cls = "catalog-toolbar")
        header.add("h3", "Мой словарь · ${document.selectedIds.size}")
        val toggleId = "vocabulary-catalog-toggle"
        val collapsibleId = "vocabulary-catalog-collapsible"
        header.button(if (catalogVisible) "Скрыть каталог" else "Открыть каталог") {
            // UX4-21/22: explicitly move focus onto the toggle BEFORE its content becomes inert,
            // never after. Chromium/Firefox focus a clicked <button> by default, but WebKit does
            // not — WebKit only blurs whatever was previously focused (e.g. an open <select>
            // inside the collapsing content) as part of handling the click, leaving focus on
            // `body`. Relying on the default leaves nothing to reclaim once the whole subtree
            // rebuilds, so blur the old target and focus the toggle ourselves either way.
            (kotlinx.browser.document.activeElement as? HTMLElement)?.takeIf { it.id != toggleId }?.blur()
            (kotlinx.browser.document.getElementById(toggleId) as? HTMLElement)?.focus()
            catalogVisible = !catalogVisible; catalogToggledThisRender = true; refresh()
        }.apply { id = toggleId; setAttribute("aria-expanded", catalogVisible.toString()) }
        val collapsible = section.add("div", cls = "collapsible catalog-collapsible")
        collapsible.id = collapsibleId
        // The 0fr/1fr grid trick clips exactly one row track; a single inner wrapper (rather than
        // the several sibling elements below living directly in that track) is what lets every
        // one of them collapse together, not just whichever the auto-placement grid put first.
        val inner = collapsible.add("div")
        val filterLabel = inner.add("label", "Подборка")
        val filters = filterLabel.select("Подборка слов", listOf(
            "A1" to "A1 · готовые карточки", "A2" to "A2 · готовые карточки", "B1" to "B1 · готовые карточки",
            "100" to "Топ-100 по частоте", "500" to "Топ-500 по частоте", "1000" to "Топ-1000 по частоте",
            "mine" to "Мои слова",
        ), filter)
        filters.addEventListener("change", { filter = filters.value; refresh() })
        inner.add("p", courseVocabularyInstructions.web, "muted small")
        val list = inner.add("div", cls = "catalog-list")
        val all = vocabularyItems + document.custom
        val byLemma = vocabularyItems.associateBy(VocabularyItem::lemma)
        val rows = when (filter) {
            "100", "500", "1000" -> frequencyItems.take(filter.toInt()).map { Triple(it.rank, it.lemma, byLemma[it.lemma]) }
            "mine" -> document.custom.map { Triple(it.frequencyRank, it.lemma, it) }
            else -> all.filter { it.level == filter }.map { Triple(it.frequencyRank, it.lemma, it) }
        }
        if (filter in listOf("100", "500", "1000")) {
            val ready = rows.count { (_, _, item) -> item?.custom == false }
            inner.add("p", "Готово $ready/${rows.size} · недоступно ${rows.size - ready}", "muted small")
                .setAttribute("role", "status")
        }
        rows.forEach { (rank, lemma, item) ->
            val row = list.add("label", cls = "catalog-row")
            val checkbox = row.add("input") as HTMLInputElement
            checkbox.type = "checkbox"
            checkbox.checked = item != null && item.id in document.selectedIds
            checkbox.disabled = item == null || recoveryRaw != null
            checkbox.addEventListener("change", {
                if (item != null) commit(VocabularyCodec.select(document, item.id, checkbox.checked), refresh)
            })
            row.add("span").apply {
                add("b", lemma).setAttribute("lang", languages.targetLang)
                add("small", item?.translation ?: courseVocabularyUnavailableLabel)
            }
            if (rank != null) row.add("small", "№ $rank")
            if (item?.custom == true) row.button("Изменить") {
                editing = item.id
                editor = EditorDraft(item.lemma, item.translation, item.form, item.example, item.level)
                refresh()
            }
        }
        renderEditor(inner, refresh)
        val actions = inner.add("div", cls = "actions")
        actions.button("Экспорт словаря JSON") { download("polski-vocabulary-$activeCoursePackId.json", VocabularyCodec.encode(document)) }
        val importLabel = inner.add("label", "Импорт JSON")
        val input = importLabel.add("textarea") as HTMLTextAreaElement
        input.setAttribute("aria-label", "JSON словаря для импорта")
        input.placeholder = "Вставь содержимое экспортированного JSON"
        input.value = importText
        input.addEventListener("input", { importText = input.value })
        actions.button("Добавить данные из JSON") {
            runCatching {
                val imported = VocabularyCodec.decode(importText)
                val merged = if (recoveryRaw != null) imported else VocabularyCodec.merge(document, imported)
                if (writeDocument(merged, backupCurrent = true)) { recoveryRaw = null; importText = "" }
            }.onFailure { error = it.message ?: "Не удалось импортировать словарь" }
            refresh()
        }
        applyCollapsible(collapsible, expanded = catalogVisible, isToggleEvent = catalogToggledThisRender)
        catalogToggledThisRender = false
    }

    private fun renderEditor(section: HTMLElement, refresh: () -> Unit) {
        val editorSection = section.add("div", cls = "vocabulary-editor")
        editorSection.add("h4", if (editing == null) "Добавить своё слово" else "Изменить своё слово")
        val fields = editorSection.add("div", cls = "editor-fields")
        fields.field("Польское слово", editor.lemma) { editor.lemma = it }
        fields.field("Перевод", editor.translation) { editor.translation = it }
        fields.field("Форма", editor.form) { editor.form = it }
        fields.field("Пример в предложении", editor.example) { editor.example = it }
        val levelLabel = fields.add("label", "Уровень")
        val level = levelLabel.select("Уровень", listOf("—", "A1", "A2", "B1", "B2", "C1", "C2").map { it to it }, editor.level)
        level.addEventListener("change", { editor.level = level.value })
        val actions = editorSection.add("div", cls = "actions")
        actions.button(if (editing == null) "Добавить слово" else "Сохранить изменения") { saveOwn(refresh) }
        editing?.let { id ->
            actions.button("Отмена") { editing = null; editor = EditorDraft(); refresh() }
            actions.button("Удалить слово") {
                if (window.confirm("Удалить своё слово из каталога? История повторений останется в экспортируемых данных.")) {
                    if (commit(document.copy(custom = document.custom.filterNot { it.id == id },
                        selectedIds = document.selectedIds.filterNot { it == id }), refresh)) {
                        editing = null; editor = EditorDraft(); refresh()
                    }
                }
            }
        }
    }

    private fun saveOwn(refresh: () -> Unit) {
        val id = editing ?: "user.${randomUuid()}"
        val item = VocabularyItem(id, editor.lemma.trim(), editor.translation.trim(), editor.form.trim(),
            editor.example.trim(), editor.level, null, true)
        if ((vocabularyItems + document.custom).any { it.id != id && it.lemma.lowercase() == item.lemma.lowercase() }) {
            error = "Это польское слово уже есть в словаре."; refresh(); return
        }
        val custom = if (editing == null) document.custom + item else document.custom.map { if (it.id == id) item else it }
        if (commit(document.copy(custom = custom), refresh)) { editing = null; editor = EditorDraft(); refresh() }
    }

    /**
     * UX4-14: a thin wrapper over the existing [rate], reachable from the global keydown handler
     * (`TrainingWebApp.kt`, `ArrowLeft`/`ArrowRight`/`1`/`2` on the Vocabulary route) — reads the
     * current due id the same way [renderCard] does and does nothing before reveal or when
     * nothing is currently due; never publishes [revealed]/[document] themselves.
     */
    fun rateCurrentIfRevealed(rating: Rating, refresh: () -> Unit) {
        if (!revealed) return
        val id = VocabularyCodec.dueIds(document, direction, scheduler, Clock.System.now()).firstOrNull() ?: return
        rate(id, rating, refresh)
    }

    /**
     * UX5: the global Space/Enter handler on the Vocabulary route — mirrors exactly what a click on
     * the card already does: reveals (and flips to face the answer) when the card isn't revealed yet,
     * otherwise flips it back and forth, without a full page rebuild for the toggle (see
     * [toggleFlip]). A no-op when nothing is due (same guard [renderCard] uses).
     */
    fun spaceReveal(refresh: () -> Unit) {
        if (!revealed) { revealAndFlip(refresh); return }
        toggleFlip()
    }

    /** Reveals the answer once and flips to face it — shared by the click-to-reveal tap gesture,
     *  Enter/"Проверить" in typed mode, and [spaceReveal]. A no-op once already revealed, so a
     *  stray double-trigger (e.g. Enter racing a tap) can never re-flip or replay the animation. */
    private fun revealAndFlip(refresh: () -> Unit) {
        if (revealed) return
        revealed = true; flipped = true; justRevealedFlip = true; refresh()
    }

    /** Flips the already-revealed card back and forth in place, purely visually — the exact same
     *  direct DOM mutation the click handler already did, just reachable from [spaceReveal] too. */
    private fun toggleFlip() {
        val (inner, front, answer) = flipNodes ?: return
        flipped = !flipped
        flipCard.apply(inner, front, answer, toFlipped = flipped, isFlipEvent = true)
    }

    private fun rate(id: String, rating: Rating, refresh: () -> Unit) {
        if (!revealed) return
        if (commit(VocabularyCodec.review(document, id, direction, rating, scheduler, Clock.System.now()), refresh)) {
            revealed = false; draft = ""; refresh()
        }
    }

    private fun commit(next: VocabularyDocument, refresh: () -> Unit): Boolean {
        if (recoveryRaw != null) { error = "Сначала сохрани исходный JSON и восстанови словарь."; refresh(); return false }
        val ok = writeDocument(next, backupCurrent = false)
        refresh()
        return ok
    }

    /**
     * Writes [next] and reads it back to catch a silently truncated or evicted write; on failure
     * the previous value is restored. With [backupCurrent] the value in place before the write is
     * copied to [VOCABULARY_BACKUP_KEY] and read back first, so a bad import never destroys the
     * only copy of the current document. Does not consult [recoveryRaw]; callers gate that.
     */
    private fun writeDocument(next: VocabularyDocument, backupCurrent: Boolean): Boolean {
        val serialized = VocabularyCodec.encode(next)
        var previous: String? = null
        return runCatching {
            previous = window.localStorage.getItem(VocabularyCodec.key)
            if (backupCurrent && previous != null) {
                window.localStorage.setItem(VOCABULARY_BACKUP_KEY, previous)
                check(window.localStorage.getItem(VOCABULARY_BACKUP_KEY) == previous) { "Резервная копия словаря не подтверждена" }
            }
            window.localStorage.setItem(VocabularyCodec.key, serialized)
            check(window.localStorage.getItem(VocabularyCodec.key) == serialized) { "Словарь не прошёл проверку записи" }
            document = next
            error = null
        }.onFailure {
            previous?.let { raw -> runCatching { window.localStorage.setItem(VocabularyCodec.key, raw) } }
            error = it.message ?: "Не удалось сохранить словарь"
        }.isSuccess
    }
}

private data class EditorDraft(var lemma: String = "", var translation: String = "", var form: String = "",
                               var example: String = "", var level: String = "—")

// Top-level (not a class member) so `document` here always resolves to kotlinx.browser.document —
// inside VocabularyWebController itself the simple name `document` resolves to its own
// `VocabularyDocument` field instead, shadowing the browser global (pre-existing naming; the
// class's own methods below always create elements through this or the `add`/`button`/`select`
// extensions rather than calling `document.createElement` directly).
private fun detachedElement(tag: String, cls: String): HTMLElement =
    (document.createElement(tag) as HTMLElement).apply { className = cls }

private fun HTMLElement.add(tag: String, text: String? = null, cls: String? = null): HTMLElement =
    (document.createElement(tag) as HTMLElement).also { child ->
        if (text != null) child.textContent = text
        if (cls != null) child.className = cls
        appendChild(child)
    }

private fun HTMLElement.button(label: String, cls: String = "", action: () -> Unit): HTMLElement =
    add("button", label, cls).apply { addEventListener("click", { action() }) }

/** UX4-13: same `<small>hint</small><span>interval</span>` structure the training card's rating
 *  buttons already use — "не пустые высокие кнопки" is a direct consequence of having this.
 *  [cls] (P2-13) carries the same `rating-again`/`rating-good` colour accent training's own
 *  rating buttons get. */
private fun HTMLElement.button(label: String, hint: String, interval: String, cls: String = "", action: () -> Unit): HTMLElement =
    add("button", label, cls).apply {
        add("small", hint)
        add("span", interval)
        addEventListener("click", { action() })
    }

private fun HTMLElement.select(label: String, options: List<Pair<String, String>>, chosen: String): HTMLSelectElement =
    (add("select") as HTMLSelectElement).also { select ->
        select.setAttribute("aria-label", label)
        options.forEach { (value, title) -> select.add("option", title).setAttribute("value", value) }
        select.value = chosen
    }

private fun HTMLElement.field(label: String, value: String, onInput: (String) -> Unit) {
    val wrapper = add("label", label)
    val input = wrapper.add("input") as HTMLInputElement
    input.value = value
    input.addEventListener("input", { onInput(input.value) })
}

private fun HTMLElement.detail(title: String, value: String, language: String? = null) {
    add("div").apply {
        add("dt", title)
        add("dd", value).apply { if (language != null) setAttribute("lang", language) }
    }
}

private fun randomUuid(): String {
    val bytes = Random.nextBytes(16)
    bytes[6] = ((bytes[6].toInt() and 15) or 0x40).toByte()
    bytes[8] = ((bytes[8].toInt() and 0x3f) or 0x80).toByte()
    val hex = "0123456789abcdef"
    return buildString {
        bytes.forEachIndexed { index, byte ->
            if (index in listOf(4, 6, 8, 10)) append('-')
            val value = byte.toInt() and 255
            append(hex[value shr 4]); append(hex[value and 15])
        }
    }
}

private fun download(filename: String, raw: String) {
    val bytes = raw.encodeToByteArray()
    val digits = "0123456789ABCDEF"
    val encoded = buildString {
        bytes.forEach { byte ->
            val value = byte.toInt() and 255
            append('%'); append(digits[value shr 4]); append(digits[value and 15])
        }
    }
    val link = document.createElement("a") as HTMLAnchorElement
    link.href = "data:application/json;charset=utf-8,$encoded"
    link.download = filename
    link.click()
}
