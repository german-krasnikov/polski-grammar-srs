package polski.platform

import kotlin.time.Clock
import polski.progress.AttemptLogRepository
import polski.progress.RawAttemptLogRepository

/** P1 blueprint §P1.1: localStorage adapter for the current origin, under its own key
 *  ([RawAttemptLogRepository.ATTEMPT_LOG_KEY]) — entirely separate from [WebProgressRepository]'s
 *  own `PREVIEW_KEY`/migration keys, so a corrupt or missing journal never blocks Progress
 *  load/save, and clearing/exporting one never touches the other. */
class WebAttemptLogRepository : AttemptLogRepository by RawAttemptLogRepository(
    BrowserKeyValueStore(), { Clock.System.now() },
)
