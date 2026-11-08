package polski.presentation

/**
 * "5 мин" / "3 ч" / "2 дн" — how long until [dueMillis] (epoch millis), relative to [nowMillis].
 * The one copy of a body that used to be duplicated in `TrainingWebApp.kt` and
 * `TrainingScreen.kt` (desktop); verified byte-identical before merging here.
 */
fun intervalLabel(dueMillis: Long, nowMillis: Long): String {
    val minutes = maxOf(1L, (dueMillis - nowMillis + 30_000L) / 60_000L)
    return when {
        minutes < 60 -> "$minutes мин"
        minutes < 2_880 -> "${(minutes + 30) / 60} ч"
        else -> "${(minutes + 720) / 1_440} дн"
    }
}
