package polski.progress

import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

sealed interface AttemptLogLoadResult {
    data object Missing : AttemptLogLoadResult
    data class Loaded(val document: AttemptLogDocument) : AttemptLogLoadResult
    data class Invalid(val raw: String, val reason: String) : AttemptLogLoadResult
    data class Unsupported(val raw: String, val version: Int) : AttemptLogLoadResult
    data class Unavailable(val cause: Throwable) : AttemptLogLoadResult
}

sealed interface AttemptLogSaveResult {
    data object Saved : AttemptLogSaveResult
    data class WriteFailed(val cause: Throwable) : AttemptLogSaveResult
}

/** P1 blueprint §P1.1: its own storage key, its own schema version, read/written independently of
 *  [ProgressDocument] — a corrupt or missing attempt log must never block scheduling. */
interface AttemptLogRepository {
    suspend fun load(): AttemptLogLoadResult

    /** Reads, prunes events older than [AttemptLogRetention.RETENTION_DAYS], appends [event], and
     *  writes — one tested, central place for retention (never in the caller). Never throws: a
     *  write failure surfaces as [AttemptLogSaveResult.WriteFailed], so a caller like
     *  `TrainingStore.rate()` can log it without rolling back the review that already succeeded. */
    suspend fun append(event: AttemptEvent): AttemptLogSaveResult
}

/** P1 blueprint §P1.1 retention policy: age-based (not count-based) — a count cap could silently
 *  drop exactly the D7/D30 probe attempts a heavy practice day pushes out. Default covers D30±2
 *  plus a safety margin; same "tune during the pilot" status as `RETRY_DELAY`/
 *  `TilePromotionThresholds`. */
const val ATTEMPT_LOG_RETENTION_DAYS = 37

/** Default for a host that has not yet wired a real [AttemptLogRepository] (P1 blueprint: "a
 *  corrupt or missing attempt log must never block scheduling" — the same stance extends to a
 *  host that has not wired one at all yet). Every appended event is silently dropped; acceptable
 *  only as a placeholder default, never a host's deliberate choice once it wires a real one. */
object NoopAttemptLogRepository : AttemptLogRepository {
    override suspend fun load(): AttemptLogLoadResult = AttemptLogLoadResult.Missing
    override suspend fun append(event: AttemptEvent): AttemptLogSaveResult = AttemptLogSaveResult.Saved
}

/** Reuses the same [RawKeyValueStore] port [RawProgressRepository] already takes — one more key,
 *  no new storage abstraction per host. [now] supplies the clock pruning measures age against. */
class RawAttemptLogRepository(
    private val storage: RawKeyValueStore,
    private val now: () -> Instant,
) : AttemptLogRepository {
    companion object {
        const val ATTEMPT_LOG_KEY = "polski-grammar-srs-kmp-attempts-v1"
    }

    override suspend fun load(): AttemptLogLoadResult {
        val raw = try {
            storage.get(ATTEMPT_LOG_KEY)?.takeIf(String::isNotEmpty)
        } catch (error: Throwable) {
            error.rethrowCancellation()
            return AttemptLogLoadResult.Unavailable(error)
        } ?: return AttemptLogLoadResult.Missing
        return when (val decoded = AttemptLogCodec.decodeText(raw.encodeToByteArray())) {
            is AttemptLogDecodeResult.Valid -> AttemptLogLoadResult.Loaded(decoded.document)
            is AttemptLogDecodeResult.Invalid -> AttemptLogLoadResult.Invalid(raw, decoded.reason)
            is AttemptLogDecodeResult.Unsupported -> AttemptLogLoadResult.Unsupported(raw, decoded.version)
        }
    }

    override suspend fun append(event: AttemptEvent): AttemptLogSaveResult {
        return try {
            val current = when (val result = load()) {
                AttemptLogLoadResult.Missing -> AttemptLogDocument(AttemptLog())
                is AttemptLogLoadResult.Loaded -> result.document
                is AttemptLogLoadResult.Unavailable -> return AttemptLogSaveResult.WriteFailed(result.cause)
                is AttemptLogLoadResult.Invalid -> return AttemptLogSaveResult.WriteFailed(IllegalStateException("Attempt log is invalid: ${result.reason}"))
                is AttemptLogLoadResult.Unsupported -> return AttemptLogSaveResult.WriteFailed(IllegalStateException("Unsupported attempt log version ${result.version}"))
            }
            val pruned = prune(current.log.events, now())
            val updated = current.copy(log = current.log.copy(events = pruned + event))
            val raw = AttemptLogCodec.encodeText(updated).decodeToString()
            storage.put(ATTEMPT_LOG_KEY, raw)
            check(storage.get(ATTEMPT_LOG_KEY) == raw) { "Attempt log read-back mismatch" }
            AttemptLogSaveResult.Saved
        } catch (error: Throwable) {
            error.rethrowCancellation()
            AttemptLogSaveResult.WriteFailed(error)
        }
    }

    /** Drops any event whose own [AttemptEvent.at] is older than
     *  [ATTEMPT_LOG_RETENTION_DAYS] relative to [at]. A single unparsable timestamp is dropped
     *  (fail-soft) rather than invalidating the whole prune pass. */
    private fun prune(events: List<AttemptEvent>, at: Instant): List<AttemptEvent> {
        val cutoff = at - ATTEMPT_LOG_RETENTION_DAYS.days
        return events.filter { event ->
            val eventAt = try { Instant.parse(event.at) } catch (_: IllegalArgumentException) { return@filter false }
            eventAt >= cutoff
        }
    }

    private fun Throwable.rethrowCancellation() {
        if (this is CancellationException) throw this
    }
}
