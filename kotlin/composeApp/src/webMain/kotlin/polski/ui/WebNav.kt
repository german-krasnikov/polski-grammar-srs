package polski.ui

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

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
    private var currentRoute: WebRoute? = null
    // W5: the first `update()` used to measure `getBoundingClientRect()` synchronously right after
    // mount, before the stylesheet (and the web font it loads) was necessarily ready — a browser
    // still on fallback-font/UA-default metrics at that instant lays the buttons out narrower than
    // their real themed size, so the indicator snapped to a tiny sliver instead of the active tab's
    // width and never got corrected afterwards (nothing re-measured once real layout settled).
    // Two settle-time re-measurements cover the ways that first layout can still be wrong: a
    // double `requestAnimationFrame` (one to let the browser finish the layout/paint already
    // queued — e.g. a stylesheet that just finished loading — a second so a font swap that
    // *that* first frame triggers has itself been laid out before this reads it) and a `resize`
    // listener keeps it correct afterwards (window resize, orientation change). Neither
    // `ResizeObserver` nor `document.fonts` are bound in this project's `org.w3c.dom` for both the
    // js and wasmJs targets this source set compiles for, so plain `requestAnimationFrame`/`resize`
    // are the portable choice here.
    private val onResize: (Event) -> Unit = { reposition(animate = false) }

    init {
        shell.className = "primary-nav"
        shell.setAttribute("aria-label", "Основные разделы")
        shell.insertBefore(indicator, shell.firstChild)
        app.appendChild(shell)
        window.requestAnimationFrame { window.requestAnimationFrame { reposition(animate = false) } }
        window.addEventListener("resize", onResize)
    }

    fun update(route: WebRoute) {
        currentRoute = route
        buttons.forEach { (destination, button) ->
            val selected = destination == route
            button.classList.toggle("active", selected)
            button.setAttribute("aria-pressed", selected.toString())
            if (selected) button.setAttribute("aria-current", "page") else button.removeAttribute("aria-current")
        }
        // Only an actual navigation (this call) may animate the slide; the settle-time corrections
        // above (`reposition(animate = false)` from the double rAF and from `resize`) must not, or
        // a still-settling first paint would visibly slide the indicator in right after mount.
        reposition(animate = positioned)
        positioned = true
    }

    private fun reposition(animate: Boolean) {
        val route = currentRoute ?: return
        val active = buttons[route] ?: return
        val shellRect = shell.getBoundingClientRect()
        val activeRect = active.getBoundingClientRect()
        if (animate) indicator.style.removeProperty("transition") else indicator.style.setProperty("transition", "none")
        // A thin underline, not a filled pill: only x-position and width move — every button
        // keeps its own resting background, so nothing needs to turn transparent for this to read.
        indicator.style.setProperty("transform", "translateX(${activeRect.left - shellRect.left}px)")
        indicator.style.setProperty("width", "${activeRect.width}px")
    }

    fun close() { window.removeEventListener("resize", onResize); shell.remove() }
}
