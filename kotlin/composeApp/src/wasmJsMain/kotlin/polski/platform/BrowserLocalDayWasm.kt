package polski.platform

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
internal actual fun browserLocalDay(epochMilliseconds: Double): String =
    js("new Date(epochMilliseconds).getFullYear().toString().padStart(4, '0') + '-' + (new Date(epochMilliseconds).getMonth() + 1).toString().padStart(2, '0') + '-' + new Date(epochMilliseconds).getDate().toString().padStart(2, '0')")
