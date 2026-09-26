package polski.ui

import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement

/** Stable browser navigation shared by all routes in one mounted web host. */
internal class WebNav(app: HTMLElement, navigate: (WebRoute) -> Unit) {
    val shell: HTMLElement = document.createElement("nav") as HTMLElement
    // UX4-16: a decorative bar that slides under the active destination, positioned in real
    // pixels (not a CSS-only `:nth-child` trick) since the buttons aren't equal width.
    private val indicator: HTMLElement = (document.createElement("span") as HTMLElement).apply {
        className = "nav-indicator"
        setAttribute("aria-hidden", "true")
    }
    private val destinations = listOf(
        WebRoute.Training to "Карточки",
        WebRoute.Vocabulary to "Слова",
        WebRoute.Matrix to "Таблицы и схема",
        WebRoute.Progress to "Прогресс",
        WebRoute.Settings to "Настройки",
    )
    // P2-10: "Таблицы и схема" wraps onto two lines inside the mobile nav's fixed-height buttons;
    // a shorter label swaps in there via CSS (`.nav-label-short`/`.nav-label-full`) while
    // `aria-label` keeps carrying the full name on every viewport.
    private val shortLabels = mapOf(WebRoute.Matrix to "Таблицы")
    private val buttons = destinations.associate { (route, label) ->
        route to (document.createElement("button") as HTMLButtonElement).apply {
            type = "button"
            id = "nav-${route.slug}"
            setAttribute("aria-label", label)
            val short = shortLabels[route]
            if (short == null) textContent = label
            else {
                appendChild((document.createElement("span") as HTMLElement).apply { className = "nav-label-full"; textContent = label })
                appendChild((document.createElement("span") as HTMLElement).apply { className = "nav-label-short"; textContent = short })
            }
            addEventListener("click", { navigate(route) })
            shell.appendChild(this)
        }
    }
    private var positioned = false

    init {
        shell.className = "primary-nav"
        shell.setAttribute("aria-label", "Основные разделы")
        shell.insertBefore(indicator, shell.firstChild)
        app.appendChild(shell)
    }

    fun update(route: WebRoute) {
        buttons.forEach { (destination, button) ->
            val selected = destination == route
            button.classList.toggle("active", selected)
            button.setAttribute("aria-pressed", selected.toString())
            if (selected) button.setAttribute("aria-current", "page") else button.removeAttribute("aria-current")
        }
        val active = buttons[route] ?: return
        val shellRect = shell.getBoundingClientRect()
        val activeRect = active.getBoundingClientRect()
        if (!positioned) { indicator.style.setProperty("transition", "none"); positioned = true }
        else indicator.style.removeProperty("transition")
        // A thin underline, not a filled pill: only x-position and width move — every button
        // keeps its own resting background, so nothing needs to turn transparent for this to read.
        indicator.style.setProperty("transform", "translateX(${activeRect.left - shellRect.left}px)")
        indicator.style.setProperty("width", "${activeRect.width}px")
    }

    fun close() { shell.remove() }
}
