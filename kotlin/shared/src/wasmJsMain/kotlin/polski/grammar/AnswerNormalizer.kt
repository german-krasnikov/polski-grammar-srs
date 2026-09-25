package polski.grammar

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
actual fun normalizeNfc(value: String): String = js("value.normalize('NFC')")

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
actual fun polishLowercase(value: String): String = js("value.toLocaleLowerCase('pl-PL')")

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
actual fun polishUppercase(value: String): String = js("value.toLocaleUpperCase('pl-PL')")
