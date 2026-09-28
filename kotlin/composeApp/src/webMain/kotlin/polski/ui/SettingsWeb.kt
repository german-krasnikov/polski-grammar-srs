package polski.ui

import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.files.FileReader
import polski.data.availableCoursePacks
import polski.preferences.Appearance
import polski.preferences.Motion
import polski.preferences.PreferredAnswerMode
import polski.preferences.PreferredStyle
import polski.presentation.AnswerMode
import polski.presentation.AppAction
import polski.presentation.StyleId
import polski.presentation.builtInStyleIds
import polski.presentation.TrainingStore

/** EnRuAcceptance-2026-09-28.md §7 item 3: a language code's Russian display name for the
 *  target/native pickers — every code [availableCoursePacks] can report today or once a further
 *  pack is registered (matching the macOS/iOS Settings bridges' own `languageDisplayNames`). */
private val languageDisplayNames: Map<String, String> = mapOf("pl" to "Польский", "en" to "Английский", "ru" to "Русский")
private fun languageLabel(code: String): String = languageDisplayNames[code] ?: code

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
internal fun renderSettingsWeb(
    root: HTMLElement,
    preferences: WebPreferencesController,
    store: TrainingStore,
    returnTo: WebRoute,
    navigate: (WebRoute) -> Unit,
) {
    root.appendChild(settingsNode("h2", text = "Настройки обучения"))
    root.appendChild(settingsButton("Вернуться к карточке") { navigate(returnTo) }.apply { id = "settings-return" })
    if (preferences.status.isNotEmpty()) root.appendChild(settingsNode("p", "settings-status", preferences.status).apply {
        setAttribute("role", "status")
        setAttribute("aria-live", "polite")
    })
    val course = settingsNode("section", "card settings-panel")
    course.appendChild(settingsNode("h3", text = "Курс"))
    root.appendChild(course)
    val packs = availableCoursePacks
    val activePack = packs.firstOrNull { it.target == preferences.value.target && it.native == preferences.value.native } ?: packs.first()
    val targetOptions = packs.map { it.target }.distinct()
    val nativeOptionsForTarget = packs.filter { it.target == activePack.target }.map { it.native }.distinct()
    course.appendChild(settingsSelect("Изучаемый язык", "settings-target", targetOptions.map { it to languageLabel(it) }, activePack.target) { selected ->
        val native = packs.filter { it.target == selected }.map { it.native }.firstOrNull() ?: activePack.native
        preferences.setCourse(selected, native)
    })
    course.appendChild(settingsSelect("Родной язык", "settings-native", nativeOptionsForTarget.map { it to languageLabel(it) }, activePack.native) { selected ->
        preferences.setCourse(activePack.target, selected)
    })
    course.appendChild(settingsNode("p", "muted", "Доступны только сочетания языков, которые уже можно полностью пройти: тренировку, стили, лайфхаки, матрицу и словарь."))

    val learning = settingsNode("section", "card settings-panel")
    learning.appendChild(settingsNode("h3", text = "Обучение"))
    root.appendChild(learning)
    val currentStyleId = StyleId(preferences.value.styleId.name)
    learning.appendChild(settingsSelect("Стиль объяснений", "settings-method", builtInStyleIds.map { it.value to styleLabel(it) }, currentStyleId.value) { selected ->
        preferences.setStyle(StyleId(selected), store)
    })
    learning.appendChild(settingsNode("p", "muted settings-style-description", styleDescription(currentStyleId)))
    nativeContrastFallbackHint(store.state.value.exercise?.primarySkill)?.let { hint ->
        learning.appendChild(settingsNode("p", "muted settings-style-fallback-hint", hint))
    }
    learning.appendChild(settingsSelect("Способ ответа", "settings-answer-mode", listOf("Oral" to "Вслух или про себя", "Typed" to "Напечатать"), preferences.value.answerMode.name) { selected ->
        preferences.setAnswerMode(if (selected == "Typed") AnswerMode.Typed else AnswerMode.Oral, store)
    })
    learning.appendChild(settingsNode("p", "muted", "Переход к печатному ответу после раскрытия применяется к следующему вопросу."))
    learning.appendChild(settingsToggle("Оценивать карточки свайпом", "settings-swipe-rating", preferences.value.swipeRatingEnabled) {
        preferences.setSwipeRating(it)
    })
    learning.appendChild(settingsNode("p", "muted", "Для грамматики и слов после раскрытия: влево — «Повторить», вправо — «Вспомнил». На компьютере также кнопки и ← →."))

    val display = settingsNode("section", "card settings-panel")
    display.appendChild(settingsNode("h3", text = "Внешний вид"))
    root.appendChild(display)
    display.appendChild(settingsSelect("Тема", "settings-appearance", listOf("System" to "Как в системе", "Light" to "Светлая", "Dark" to "Тёмная"), preferences.value.appearance.name) {
        preferences.setAppearance(Appearance.valueOf(it))
    })
    display.appendChild(settingsSelect("Движение", "settings-motion", listOf("System" to "Как в системе", "Reduced" to "Меньше движения"), preferences.value.motion.name) {
        preferences.setMotion(Motion.valueOf(it))
    })
    display.appendChild(settingsToggle("Анимации", "settings-animations", preferences.value.animationsEnabled) {
        preferences.setAnimationsEnabled(it)
    })
    display.appendChild(settingsNode("p", "muted", "Выключение анимаций отключает раскрытие ответа, оборот карточек слов и все эффекты Rive — они не загружаются из сети."))
    val data = settingsNode("section", "card settings-panel")
    data.appendChild(settingsNode("h3", text = "Данные"))
    root.appendChild(data)
    data.appendChild(settingsButton("Сохранить настройки JSON") { downloadPreferences(preferences.exportRaw()) })
    val importLabel = settingsNode("label", text = "Выбрать JSON настроек")
    val input = document.createElement("input") as HTMLInputElement
    input.id = "settings-import"
    input.type = "file"
    input.accept = "application/json,.json"
    input.setAttribute("aria-label", "Файл JSON настроек для импорта")
    importLabel.appendChild(input)
    data.appendChild(importLabel)
    data.appendChild(settingsButton("Импортировать настройки") {
        val file = input.files?.item(0)
        if (file == null) preferences.report("Выберите файл JSON настроек")
        else {
            val reader = FileReader()
            reader.onload = { preferences.import(reader.result?.toString().orEmpty(), store) }
            reader.onerror = { preferences.report("Не удалось прочитать файл настроек") }
            reader.readAsText(file)
        }
    })
    data.appendChild(settingsButton("Открыть прогресс и экспорт JSON") { navigate(WebRoute.Progress) })
    data.appendChild(settingsButton("Открыть словарь и импорт или экспорт JSON") { navigate(WebRoute.Vocabulary) })
    data.appendChild(settingsNode("p", "muted", "Прогресс и словарь сохраняются отдельно от настроек. Их JSON доступен в соответствующих разделах."))

    val reminders = settingsNode("section", "card settings-panel")
    reminders.appendChild(settingsNode("h3", text = "Напоминания"))
    reminders.appendChild(settingsNode("p", text = "В браузерной версии напоминания после закрытия страницы недоступны. Для надёжной доставки нужна отдельная поддержка Push/PWA."))
    root.appendChild(reminders)
}

private fun settingsSelect(label: String, id: String, options: List<Pair<String, String>>, selected: String, onChange: (String) -> Unit): HTMLElement {
    val wrapper = settingsNode("label", text = label)
    val select = document.createElement("select") as HTMLSelectElement
    select.id = id
    select.setAttribute("aria-label", label)
    options.forEach { (value, title) -> select.appendChild(settingsNode("option", text = title).apply { setAttribute("value", value) }) }
    select.value = selected
    select.addEventListener("change", { onChange(select.value) })
    wrapper.appendChild(select)
    return wrapper
}

private fun settingsToggle(label: String, id: String, checked: Boolean, onChange: (Boolean) -> Unit): HTMLElement {
    val wrapper = settingsNode("label", "settings-toggle", label)
    val input = document.createElement("input") as HTMLInputElement
    input.id = id
    input.type = "checkbox"
    input.checked = checked
    input.setAttribute("role", "switch")
    input.setAttribute("aria-label", label)
    input.addEventListener("change", { onChange(input.checked) })
    wrapper.appendChild(input)
    return wrapper
}

private fun settingsButton(label: String, onClick: () -> Unit): HTMLButtonElement =
    (settingsNode("button", text = label) as HTMLButtonElement).apply {
        type = "button"
        addEventListener("click", { onClick() })
    }

private fun settingsNode(tag: String, classes: String = "", text: String? = null): HTMLElement =
    (document.createElement(tag) as HTMLElement).apply {
        className = classes
        if (text != null) textContent = text
    }

private fun downloadPreferences(raw: String) {
    val encoded = raw.encodeToByteArray().joinToString("") { byte ->
        val value = byte.toInt() and 255
        "%" + "0123456789ABCDEF"[value shr 4] + "0123456789ABCDEF"[value and 15]
    }
    val anchor = document.createElement("a") as HTMLAnchorElement
    anchor.href = "data:application/json;charset=utf-8,$encoded"
    anchor.download = "polski-preferences-v2.json"
    document.body?.appendChild(anchor)
    anchor.click()
    anchor.remove()
}
