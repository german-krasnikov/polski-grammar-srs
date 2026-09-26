package polski.ui

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
internal actual fun withViewTransition(run: () -> Unit) {
    js("if (typeof document.startViewTransition === 'function') { document.startViewTransition(run); } else { run(); }")
}
