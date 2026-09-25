package polski.platform

internal actual fun browserFormatDate(epochMilliseconds: Double): String =
    kotlin.js.Date(epochMilliseconds).toLocaleString("ru-RU")
