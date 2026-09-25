package polski.platform

internal actual fun browserLocalDay(epochMilliseconds: Double): String {
    val date = kotlin.js.Date(epochMilliseconds)
    val year = date.getFullYear().toString().padStart(4, '0')
    val month = (date.getMonth() + 1).toString().padStart(2, '0')
    val day = date.getDate().toString().padStart(2, '0')
    return "$year-$month-$day"
}
