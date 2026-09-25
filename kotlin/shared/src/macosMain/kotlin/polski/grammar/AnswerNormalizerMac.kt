package polski.grammar

import platform.Foundation.NSLocale
import platform.Foundation.NSString
import platform.Foundation.lowercaseStringWithLocale
import platform.Foundation.precomposedStringWithCanonicalMapping
import platform.Foundation.uppercaseStringWithLocale

private val polishLocale = NSLocale(localeIdentifier = "pl_PL")

actual fun normalizeNfc(value: String): String = (value as NSString).precomposedStringWithCanonicalMapping
actual fun polishLowercase(value: String): String = (value as NSString).lowercaseStringWithLocale(polishLocale)
actual fun polishUppercase(value: String): String = (value as NSString).uppercaseStringWithLocale(polishLocale)
