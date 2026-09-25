package polski.platform

import kotlin.time.Instant
import kotlinx.browser.window
import polski.progress.LocalDayProvider
import polski.progress.ProgressRepository
import polski.progress.RawKeyValueStore
import polski.progress.RawProgressRepository
import polski.srs.Scheduler

/** LocalStorage adapter for the current origin. Migration and reset use isolated KMP keys. */
class WebProgressRepository(scheduler: Scheduler) : ProgressRepository by RawProgressRepository(
    BrowserKeyValueStore(), scheduler,
)

class BrowserLocalDayProvider : LocalDayProvider {
    override fun localDay(at: Instant): String = browserLocalDay(at.toEpochMilliseconds().toDouble())
}

private class BrowserKeyValueStore : RawKeyValueStore {
    override fun get(key: String): String? = window.localStorage.getItem(key)
    override fun put(key: String, value: String) { window.localStorage.setItem(key, value) }
}

internal expect fun browserLocalDay(epochMilliseconds: Double): String
