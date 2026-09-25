package polski.ui

import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement

/** Stable browser navigation shared by all routes in one mounted web host. */
internal class WebNav(app: HTMLElement, navigate: (WebRoute) -> Unit) {
    val shell: HTMLElement = document.createElement("nav") as HTMLElement
    private val destinations = listOf(
        WebRoute.Training to "Карточки",
        WebRoute.Vocabulary to "Слова",
        WebRoute.Matrix to "Таблицы и схема",
        WebRoute.Progress to "Прогресс",
        WebRoute.Settings to "Настройки",
    )
    private val buttons = destinations.associate { (route, label) ->
        route to (document.createElement("button") as HTMLButtonElement).apply {
            type = "button"
            id = "nav-${route.slug}"
            textContent = label
            setAttribute("aria-label", label)
            addEventListener("click", { navigate(route) })
            shell.appendChild(this)
        }
    }

    init {
        shell.className = "primary-nav"
        shell.setAttribute("aria-label", "Основные разделы")
        app.appendChild(shell)
    }

    fun update(route: WebRoute) {
        buttons.forEach { (destination, button) ->
            val selected = destination == route
            button.classList.toggle("active", selected)
            button.setAttribute("aria-pressed", selected.toString())
            if (selected) button.setAttribute("aria-current", "page") else button.removeAttribute("aria-current")
        }
    }

    fun close() { shell.remove() }
}
