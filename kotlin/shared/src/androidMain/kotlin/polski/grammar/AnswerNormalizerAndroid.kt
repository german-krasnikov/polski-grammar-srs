package polski.grammar

import java.text.Normalizer
import java.util.Locale

private val polishLocale = Locale.forLanguageTag("pl-PL")

actual fun normalizeNfc(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFC)
actual fun polishLowercase(value: String): String = value.lowercase(polishLocale)
actual fun polishUppercase(value: String): String = value.uppercase(polishLocale)
