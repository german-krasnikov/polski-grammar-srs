package polski.platform

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import polski.preferences.Appearance
import polski.preferences.Motion
import polski.ui.disposeRiveOnDisable

/** Media listeners are mounted once and removed with the Compose host. */
internal class WebAppearance {
    private val darkMedia = window.matchMedia("(prefers-color-scheme: dark)")
    private val motionMedia = window.matchMedia("(prefers-reduced-motion: reduce)")
    private var appearance = Appearance.System
    private var motion = Motion.System
    private var animationsEnabled = true
    private var lastAppliedAnimationsEnabled = true
    private val changed: (Event) -> Unit = { apply() }

    fun start() {
        darkMedia.addEventListener("change", changed)
        motionMedia.addEventListener("change", changed)
        apply()
    }

    fun update(appearance: Appearance, motion: Motion, animationsEnabled: Boolean) {
        this.appearance = appearance
        this.motion = motion
        this.animationsEnabled = animationsEnabled
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
        root.setAttribute("data-animations", if (animationsEnabled) "on" else "off")
        root.style.setProperty("color-scheme", if (dark) "dark" else "light")
        document.querySelector("meta[name='theme-color']")?.setAttribute("content", if (dark) "#0A1019" else "#F5F7FB")
        // v3/C: dispose any live Rive instance exactly on the enabled→disabled edge, not on every
        // apply() call (a dark-mode media change while already disabled must not re-dispose).
        disposeRiveOnDisable(wasEnabled = lastAppliedAnimationsEnabled, isEnabled = animationsEnabled)
        lastAppliedAnimationsEnabled = animationsEnabled
    }

}
