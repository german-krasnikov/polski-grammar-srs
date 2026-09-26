package polski.ui

import kotlinx.browser.document

internal actual fun withViewTransition(run: () -> Unit) {
    val dynamicDocument: dynamic = document
    val startViewTransition = dynamicDocument.startViewTransition
    if (startViewTransition != null) {
        dynamicDocument.startViewTransition(run)
    } else {
        run()
    }
}
