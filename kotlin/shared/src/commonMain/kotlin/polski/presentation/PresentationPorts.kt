package polski.presentation

import kotlin.time.Instant

/** One captured instant and the host's current local calendar day for a transition. */
data class TimeCapture(val at: Instant, val localDay: String)

fun interface TimeSource {
    fun capture(): TimeCapture
}

sealed interface EffectOutcome {
    data object Completed : EffectOutcome
    data object Skipped : EffectOutcome
    data class Failed(val reason: String) : EffectOutcome
}

/**
 * Host effects stay in state until acknowledged by ID. The host performs [FocusReveal] only after
 * the new button is attached and only while its exercise ID is current; otherwise it acknowledges
 * [EffectOutcome.Skipped]. A failed download remains pending for an explicit retry.
 */
sealed interface UiEffect {
    val id: Long

    data class FocusReveal(override val id: Long, val exerciseId: String) : UiEffect
    data class DownloadJson(override val id: Long, val filename: String, val json: String) : UiEffect
    data class ConfirmReset(override val id: Long, val prompt: String) : UiEffect
}
