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

private const val VOCABULARY_BACKUP_KEY = "polski-vocabulary-pl-ru-v1-backup"

/** Browser adapter for the shared vocabulary document; it never writes legacy grammar progress. */
internal class VocabularyWebController {
    private val scheduler = FsrsScheduler()
    private var document: VocabularyDocument = VocabularyDocument()
    private var recoveryRaw: String? = null
    private var error: String? = null
    private var direction = StudyDirection.RussianToPolish
    private var filter = "A1"
    private var catalogVisible = true
    private var revealed = false
    // v3/B: real 3D flip, reusing the exact same tested behaviour as the vocabulary flip's own
    // extracted [FlipCard] (originally the training card's, see CardFlip.kt) — purely visual,
    // never touches `revealed`/FSRS review state (same contract the training card's flip had).
    private val flipCard = FlipCard()
    private var flipped = false
    private var flippedItemId: String? = null
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
        root.className = "vocabulary-page"
        val page = root.add("section", cls = "vocabulary-view")
        page.setAttribute("aria-label", "Тренировка слов")
        val heading = page.add("div", cls = "vocabulary-heading")
        heading.add("div").apply {
            add("h2", "Слова и выражения")
            add("p", "Отмечай слова в каталоге. Узнавание и воспроизведение повторяются по отдельным расписаниям.")
        }
        val directionLabel = heading.add("label", "Направление")
        val directionSelect = directionLabel.select("Направление карточки", listOf(
            StudyDirection.RussianToPolish.wire to "Русский → польский",
            StudyDirection.PolishToRussian.wire to "Польский → русский",
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
        renderCard(layout.add("section", cls = "card vocabulary-card"), outerRoot, swipeRatingEnabled, refresh)
        renderCatalog(layout.add("section", cls = "card vocabulary-catalog"), refresh)
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

    private fun renderCard(section: HTMLElement, outerRoot: HTMLElement, swipeRatingEnabled: Boolean, refresh: () -> Unit) {
        section.setAttribute("aria-label", "Карточка слова")
        val id = VocabularyCodec.dueIds(document, direction, scheduler, Clock.System.now()).firstOrNull()
        val item = id?.let { VocabularyCodec.item(document, it) }
        if (item == null) {
            section.add("h3", if (document.selectedIds.isEmpty()) "Выбери слова для тренировки" else "На сейчас всё повторено")
            section.add("p", "Отметь готовые карточки в каталоге. История каждого направления сохраняется отдельно.")
            return
        }
        // The `flipped` visual state is host-local and keyed to the item on screen — a brand-new
        // due item (after a rating advances, or the direction toggle swaps decks) always starts
        // question-side-up, never inheriting the previous item's face.
        if (id != flippedItemId) { flipped = false; flippedItemId = id; flipCard.reset() }
        val front = detachedElement("div", "card-front")
        front.add("span", if (direction == StudyDirection.RussianToPolish) "Вспомни по-польски" else "Вспомни по-русски", "eyebrow")
        front.add("p", if (direction == StudyDirection.RussianToPolish) item.translation else item.lemma, "vocabulary-prompt")
            .setAttribute("lang", if (direction == StudyDirection.RussianToPolish) "ru" else "pl")
        if (!revealed) {
            section.appendChild(front)
            val modes = section.add("div", cls = "answer-mode")
            modes.button("Ответ вслух / про себя") { typed = false; refresh() }.setAttribute("aria-pressed", (!typed).toString())
            modes.button("Напечатать ответ") { typed = true; refresh() }.setAttribute("aria-pressed", typed.toString())
            if (typed) {
                val input = section.add("textarea") as HTMLTextAreaElement
                input.id = "vocabulary-answer"
                input.setAttribute("aria-label", "Ответ на карточку слова")
                input.value = draft
                input.addEventListener("input", { draft = input.value })
                input.addEventListener("keydown", { event ->
                    val key = event as org.w3c.dom.events.KeyboardEvent
                    if (key.key == "Enter" && !key.shiftKey && !key.isComposing) {
                        key.preventDefault(); revealed = true; flipped = true; justRevealedFlip = true; refresh()
                    }
                })
            }
            section.button("Показать ответ", "primary reveal-button") { revealed = true; flipped = true; justRevealedFlip = true; refresh() }
            return
        }
        // v3/B: revealed — both faces mount at once in a real 3D flip wrapper, reusing the exact
        // helpers the training card's flip used (FlipCard/mountRings/setRingsExpanded). Click
        // toggles which face is forward, purely visually; swipe on the back rates (unchanged from
        // before this card had a flip at all).
        front.classList.add("card-face")
        val answer = detachedElement("div", "vocabulary-answer card-back card-face")
        answer.setAttribute("aria-live", "polite")
        renderRevealedAnswer(answer, outerRoot, item, swipeRatingEnabled, refresh)
        val flip = detachedElement("div", "card-flip")
        val ringsLayer = mountRings(flip)
        val inner = detachedElement("div", "card-flip-inner")
        flip.appendChild(inner)
        inner.appendChild(front)
        inner.appendChild(answer)
        section.appendChild(flip)
        val isFlipEvent = justRevealedFlip
        justRevealedFlip = false
        flipCard.apply(inner, front, answer, ringsLayer, toFlipped = flipped, isFlipEvent = isFlipEvent)
        flipCard.installTap(flip) {
            flipped = !flipped
            flipCard.apply(inner, front, answer, ringsLayer, toFlipped = flipped, isFlipEvent = true)
        }
    }

    private fun renderRevealedAnswer(answer: HTMLElement, outerRoot: HTMLElement, item: VocabularyItem, swipeRatingEnabled: Boolean, refresh: () -> Unit) {
        answer.add("span", if (direction == StudyDirection.RussianToPolish) "Эталон · польский" else "Эталон · русский", "eyebrow")
        answer.add("p", if (direction == StudyDirection.RussianToPolish) item.lemma else item.translation)
            .setAttribute("lang", if (direction == StudyDirection.RussianToPolish) "pl" else "ru")
        val description = answer.add("dl")
        description.detail("Перевод", item.translation)
        description.detail("Форма", item.form, "pl")
        description.detail("В предложении", item.example, "pl")
        if (typed) answer.add("p", "Твой ответ: ${draft.ifBlank { "не введён" }}. Сравни сам и выбери оценку.")
        if (swipeRatingEnabled) {
            answer.add("p", "← Повторить · Вспомнил →", "vocabulary-swipe-zone muted small").apply {
                setAttribute("aria-hidden", "true")
                installTouchSwipeRating(this) { remembered ->
                    val rating = if (remembered) Rating.Good else Rating.Again
                    riveOverlay.trigger(outerRoot, answer, cardEffectFor(rating))
                    rate(item.id, rating, refresh)
                }
            }
        }
        val ratings = answer.add("div", cls = "ratings")
        ratings.button("Повторить") { riveOverlay.trigger(outerRoot, answer, cardEffectFor(Rating.Again)); rate(item.id, Rating.Again, refresh) }
        ratings.button("Вспомнил") { riveOverlay.trigger(outerRoot, answer, cardEffectFor(Rating.Good)); rate(item.id, Rating.Good, refresh) }
    }

    private fun renderCatalog(section: HTMLElement, refresh: () -> Unit) {
        val header = section.add("div", cls = "catalog-toolbar")
        header.add("h3", "Мой словарь · ${document.selectedIds.size}")
        header.button(if (catalogVisible) "Скрыть каталог" else "Открыть каталог") {
            catalogVisible = !catalogVisible; refresh()
        }.setAttribute("aria-expanded", catalogVisible.toString())
        if (!catalogVisible) return
        val filterLabel = section.add("label", "Подборка")
        val filters = filterLabel.select("Подборка слов", listOf(
            "A1" to "A1 · готовые карточки", "A2" to "A2 · готовые карточки", "B1" to "B1 · готовые карточки",
            "100" to "Топ-100 по частоте", "500" to "Топ-500 по частоте", "1000" to "Топ-1000 по частоте",
            "mine" to "Мои слова",
        ), filter)
        filters.addEventListener("change", { filter = filters.value; refresh() })
        section.add("p", courseVocabularyInstructions.web, "muted small")
        val list = section.add("div", cls = "catalog-list")
        val all = vocabularyItems + document.custom
        val byLemma = vocabularyItems.associateBy(VocabularyItem::lemma)
        val rows = when (filter) {
            "100", "500", "1000" -> frequencyItems.take(filter.toInt()).map { Triple(it.rank, it.lemma, byLemma[it.lemma]) }
            "mine" -> document.custom.map { Triple(it.frequencyRank, it.lemma, it) }
            else -> all.filter { it.level == filter }.map { Triple(it.frequencyRank, it.lemma, it) }
        }
        if (filter in listOf("100", "500", "1000")) {
            val ready = rows.count { (_, _, item) -> item?.custom == false }
            section.add("p", "Готово $ready/${rows.size} · недоступно ${rows.size - ready}", "muted small")
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
                add("b", lemma).setAttribute("lang", "pl")
                add("small", item?.translation ?: courseVocabularyUnavailableLabel)
            }
            if (rank != null) row.add("small", "№ $rank")
            if (item?.custom == true) row.button("Изменить") {
                editing = item.id
                editor = EditorDraft(item.lemma, item.translation, item.form, item.example, item.level)
                refresh()
            }
        }
        renderEditor(section, refresh)
        val actions = section.add("div", cls = "actions")
        actions.button("Экспорт словаря JSON") { download("polski-vocabulary-pl-ru.json", VocabularyCodec.encode(document)) }
        val importLabel = section.add("label", "Импорт JSON")
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
