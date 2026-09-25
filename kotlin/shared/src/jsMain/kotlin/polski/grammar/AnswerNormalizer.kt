package polski.grammar

actual fun normalizeNfc(value: String): String = js("value.normalize('NFC')")

actual fun polishLowercase(value: String): String = js("value.toLocaleLowerCase('pl-PL')")

actual fun polishUppercase(value: String): String = js("value.toLocaleUpperCase('pl-PL')")
