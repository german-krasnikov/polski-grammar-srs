package polski.platform

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
internal actual fun browserFormatDate(epochMilliseconds: Double): String =
    js("new Date(epochMilliseconds).toLocaleString('ru-RU')")
