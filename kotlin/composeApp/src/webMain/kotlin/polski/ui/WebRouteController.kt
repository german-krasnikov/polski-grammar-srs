package polski.ui

import kotlinx.browser.window
import org.w3c.dom.events.Event
import polski.presentation.AppTab

internal enum class WebRoute(val slug: String, val tab: AppTab?) {
    Training("training", AppTab.Training),
    Vocabulary("vocabulary", AppTab.Vocabulary),
    Matrix("matrix", AppTab.Matrix),
    Progress("progress", AppTab.Progress),
    Settings("settings", null);

    companion object {
        fun fromHash(hash: String): WebRoute = entries.firstOrNull { hash == "#/${it.slug}" } ?: Training
        fun forTab(tab: AppTab): WebRoute = entries.first { it.tab == tab }
    }
}

/** Owns only browser history. The TrainingStore remains mounted across destinations. */
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
internal class WebRouteController {
    var current: WebRoute = WebRoute.fromHash(window.location.hash)
        private set
    var returnTo: WebRoute = WebRoute.Training
        private set
    private var listener: ((WebRoute) -> Unit)? = null
    private val historyChanged: (Event) -> Unit = { syncFromLocation() }

    fun start(onChange: (WebRoute) -> Unit) {
        listener = onChange
        window.addEventListener("popstate", historyChanged)
        window.addEventListener("hashchange", historyChanged)
        syncFromLocation()
    }

    fun navigate(destination: WebRoute) {
        if (destination == current) return
        if (destination == WebRoute.Settings && current != WebRoute.Settings) returnTo = current
        current = destination
        window.history.pushState(null, "", "#/${destination.slug}")
        listener?.invoke(destination)
    }

    fun close() {
        window.removeEventListener("popstate", historyChanged)
        window.removeEventListener("hashchange", historyChanged)
        listener = null
    }

    private fun syncFromLocation() {
        val resolved = WebRoute.fromHash(window.location.hash)
        if (resolved == current) return
        if (resolved == WebRoute.Settings && current != WebRoute.Settings) returnTo = current
        current = resolved
        listener?.invoke(resolved)
    }
}
