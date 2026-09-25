package polski.platform

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import polski.preferences.Appearance
import polski.preferences.Motion

/** Media listeners are mounted once and removed with the Compose host. */
internal class WebAppearance {
    private val darkMedia = window.matchMedia("(prefers-color-scheme: dark)")
    private val motionMedia = window.matchMedia("(prefers-reduced-motion: reduce)")
    private var appearance = Appearance.System
    private var motion = Motion.System
    private val changed: (Event) -> Unit = { apply() }

    fun start() {
        darkMedia.addEventListener("change", changed)
        motionMedia.addEventListener("change", changed)
        apply()
    }

    fun update(appearance: Appearance, motion: Motion) {
        this.appearance = appearance
        this.motion = motion
        apply()
    }

    fun close() {
        darkMedia.removeEventListener("change", changed)
        motionMedia.removeEventListener("change", changed)
    }

    private fun apply() {
        val dark = when (appearance) {
            Appearance.System -> darkMedia.matches
            Appearance.Light -> false
            Appearance.Dark -> true
        }
        val reduce = motion == Motion.Reduced || (motion == Motion.System && motionMedia.matches)
        val root = document.documentElement as? HTMLElement ?: return
        root.setAttribute("data-theme", if (dark) "dark" else "light")
        root.setAttribute("data-motion", if (reduce) "reduced" else "normal")
        root.style.setProperty("color-scheme", if (dark) "dark" else "light")
        document.querySelector("meta[name='theme-color']")?.setAttribute("content", if (dark) "#0A1019" else "#F5F7FB")
    }

}
