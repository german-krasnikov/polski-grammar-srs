package polski.progress

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Instant
import polski.srs.Scheduler

sealed interface LoadResult {
    data object Missing : LoadResult
    /** A React save exists; the caller must request migration before writing preview. */
    data class LegacyAvailable(val raw: String) : LoadResult
    data class Loaded(val document: ProgressDocument) : LoadResult
    data class Invalid(val raw: String, val reason: String) : LoadResult
    data class Unsupported(val raw: String, val version: Int) : LoadResult
    data class RecoveryRequired(val raw: String, val reason: String) : LoadResult
    data class Unavailable(val cause: Throwable) : LoadResult
}

sealed interface SaveResult {
    data object Saved : SaveResult
    data class WriteFailed(val cause: Throwable) : SaveResult
}

sealed interface MigrationResult {
    data class Migrated(val document: ProgressDocument) : MigrationResult
    data class AlreadyMigrated(val document: ProgressDocument) : MigrationResult
    data object Missing : MigrationResult
    data class RecoveryRequired(val raw: String, val reason: String) : MigrationResult
    data class Unavailable(val cause: Throwable) : MigrationResult
}

/** The caller owns the active in-memory document and serializes revisions before saving. */
interface ProgressRepository {
    suspend fun load(): LoadResult
    suspend fun save(document: ProgressDocument): SaveResult
    suspend fun migrateLegacy(at: Instant, localDay: String, skillIds: List<String>): MigrationResult
    suspend fun resetConfirmed(at: Instant, localDay: String, skillIds: List<String>): SaveResult
}

fun interface LocalDayProvider {
    fun localDay(at: Instant): String
}

/** Synchronous raw browser-like store; test doubles can fail any individual operation. */
interface RawKeyValueStore {
    fun get(key: String): String?
    fun put(key: String, value: String)
}

/** Sequential preview-key protocol. It never writes or removes the React legacy key. */
class RawProgressRepository(
    private val storage: RawKeyValueStore,
    private val scheduler: Scheduler,
) : ProgressRepository {
    companion object {
        const val LEGACY_KEY = "polski-grammar-srs-v1"
        const val PREVIEW_KEY = "polski-grammar-srs-kmp-preview-v1"
        const val BACKUP_KEY = "polski-grammar-srs-kmp-legacy-backup-v1"
        const val MARKER_KEY = "polski-grammar-srs-kmp-migrated-v1"
        private const val STAMP_FIELD = "__kmpMigration"
    }

    override suspend fun load(): LoadResult {
        val keys = try {
            listOf(storage.get(PREVIEW_KEY), storage.get(LEGACY_KEY), storage.get(BACKUP_KEY), storage.get(MARKER_KEY))
        } catch (error: Throwable) {
            error.rethrowCancellation()
            return LoadResult.Unavailable(error)
        }
        val (raw, legacy, backup, marker) = keys
        if (raw == null) {
            if (backup != null || marker != null) {
                return LoadResult.RecoveryRequired(backup ?: legacy ?: marker ?: "", "Partial migration without preview")
            }
            return if (legacy != null) LoadResult.LegacyAvailable(legacy) else LoadResult.Missing
        }
        val decoded = ProgressCodec.decode(raw)
        if (marker != null || backup != null || legacy != null) {
            if (marker == null || backup == null || parseStamp(marker, backup) == null ||
                (legacy != null && legacy != backup) || decoded !is DecodeResult.Valid || decoded.document.stamp() != marker) {
                return LoadResult.RecoveryRequired(raw, "Preview migration is incomplete or mismatched")
            }
        }
        return when (decoded) {
            is DecodeResult.Valid -> LoadResult.Loaded(decoded.document)
            is DecodeResult.Invalid -> LoadResult.Invalid(raw, decoded.reason)
            is DecodeResult.Unsupported -> LoadResult.Unsupported(raw, decoded.version)
        }
    }

    override suspend fun save(document: ProgressDocument): SaveResult {
        return try {
            when (val current = load()) {
                LoadResult.Missing, is LoadResult.Loaded -> Unit
                is LoadResult.Unavailable -> return SaveResult.WriteFailed(current.cause)
                else -> return SaveResult.WriteFailed(IllegalStateException("Progress requires migration or recovery before save"))
            }
            val raw = ProgressCodec.encodeLegacyV1(document)
            storage.put(PREVIEW_KEY, raw)
            check(storage.get(PREVIEW_KEY) == raw) { "Preview read-back mismatch" }
            SaveResult.Saved
        } catch (error: Throwable) {
            error.rethrowCancellation()
            SaveResult.WriteFailed(error)
        }
    }

    override suspend fun migrateLegacy(at: Instant, localDay: String, skillIds: List<String>): MigrationResult {
        try {
            val legacy = storage.get(LEGACY_KEY)
            var backup = storage.get(BACKUP_KEY)
            val preview = storage.get(PREVIEW_KEY)
            val marker = storage.get(MARKER_KEY)
            if (marker != null) {
                if (backup == null || preview == null) {
                    return MigrationResult.RecoveryRequired(legacy ?: backup ?: preview ?: "", "Incomplete migration marker")
                }
                if (parseStamp(marker, backup) == null) {
                    return MigrationResult.RecoveryRequired(backup, "Marker does not match backup")
                }
                if (legacy != null && legacy != backup) {
                    return MigrationResult.RecoveryRequired(legacy, "Legacy changed after backup")
                }
                val decoded = ProgressCodec.decode(preview)
                if (decoded !is DecodeResult.Valid || decoded.document.stamp() != marker) {
                    return MigrationResult.RecoveryRequired(preview, "Marked preview is invalid or mismatched")
                }
                return MigrationResult.AlreadyMigrated(decoded.document)
            }
            if (legacy == null) {
                return if (backup == null && preview == null) MigrationResult.Missing
                else MigrationResult.RecoveryRequired(backup ?: preview ?: "", "Partial migration without legacy")
            }
            if (backup == null) {
                storage.put(BACKUP_KEY, legacy)
                backup = storage.get(BACKUP_KEY)
                if (backup != legacy) return MigrationResult.RecoveryRequired(legacy, "Backup read-back mismatch")
            } else if (backup != legacy) {
                return MigrationResult.RecoveryRequired(legacy, "Legacy differs from immutable backup")
            }
            val stamp = if (preview == null) makeStamp(legacy, at, localDay) else {
                val decoded = ProgressCodec.decode(preview)
                if (decoded !is DecodeResult.Valid) return MigrationResult.RecoveryRequired(preview, "Unmarked preview is invalid")
                decoded.document.stamp()?.takeIf { parseStamp(it, legacy) != null }
                    ?: return MigrationResult.RecoveryRequired(preview, "Unmarked preview has no matching migration stamp")
            }
            val stampValues = parseStamp(stamp, legacy)
                ?: return MigrationResult.RecoveryRequired(legacy, "Invalid migration stamp")
            val decodedLegacy = ProgressCodec.decode(legacy)
            val original = when (decodedLegacy) {
                is DecodeResult.Valid -> decodedLegacy.document
                is DecodeResult.Invalid -> return MigrationResult.RecoveryRequired(legacy, decodedLegacy.reason)
                is DecodeResult.Unsupported -> return MigrationResult.RecoveryRequired(legacy, "Unsupported version ${decodedLegacy.version}")
            }
            val complete = ProgressCodec.completeKnownSkills(original, skillIds, stampValues.at, scheduler)
            val normalized = ProgressCodec.normalizeDay(complete, stampValues.day)
            val expected = normalized.copy(source = JsonObject(normalized.source + (STAMP_FIELD to JsonPrimitive(stamp))))
            val expectedRaw = ProgressCodec.encodeLegacyV1(expected)
            if (storage.get(LEGACY_KEY) != legacy) {
                return MigrationResult.RecoveryRequired(legacy, "Legacy changed during migration")
            }
            if (preview == null) storage.put(PREVIEW_KEY, expectedRaw)
            val readBack = storage.get(PREVIEW_KEY)
                ?: return MigrationResult.RecoveryRequired(legacy, "Preview read-back missing")
            val decodedPreview = ProgressCodec.decode(readBack)
            if (decodedPreview !is DecodeResult.Valid || !sameJson(readBack, expectedRaw)) {
                return MigrationResult.RecoveryRequired(readBack, "Preview read-back mismatch")
            }
            storage.put(MARKER_KEY, stamp)
            if (storage.get(MARKER_KEY) != stamp) {
                return MigrationResult.RecoveryRequired(readBack, "Marker read-back mismatch")
            }
            return MigrationResult.Migrated(decodedPreview.document)
        } catch (error: Throwable) {
            error.rethrowCancellation()
            return MigrationResult.Unavailable(error)
        }
    }

    override suspend fun resetConfirmed(at: Instant, localDay: String, skillIds: List<String>): SaveResult {
        return try {
            when (val current = load()) {
                LoadResult.Missing, is LoadResult.Loaded -> Unit
                is LoadResult.Unavailable -> return SaveResult.WriteFailed(current.cause)
                else -> return SaveResult.WriteFailed(IllegalStateException("Progress requires migration or recovery before reset"))
            }
            val marker = storage.get(MARKER_KEY)
            val fresh = ProgressCodec.fresh(skillIds, at, localDay, scheduler)
            val document = if (marker == null) fresh else fresh.copy(
                source = JsonObject(mapOf(STAMP_FIELD to JsonPrimitive(marker))),
            )
            save(document)
        } catch (error: Throwable) {
            error.rethrowCancellation()
            SaveResult.WriteFailed(error)
        }
    }

    private fun ProgressDocument.stamp(): String? = (source[STAMP_FIELD] as? JsonPrimitive)
        ?.takeIf { it.isString }?.content

    private data class Stamp(val at: Instant, val day: String)

    private fun makeStamp(raw: String, at: Instant, day: String): String =
        "v1|${raw.length}|${fingerprint(raw)}|${at.toEpochMilliseconds()}|$day"

    private fun parseStamp(value: String, backup: String): Stamp? {
        val parts = value.split('|')
        if (parts.size != 5 || parts[0] != "v1" || parts[1].toIntOrNull() != backup.length ||
            parts[2] != fingerprint(backup)) return null
        val millis = parts[3].toLongOrNull() ?: return null
        val at = Instant.fromEpochMilliseconds(millis)
        val day = parts[4]
        if (!ProgressCodec.validDay(day)) return null
        return Stamp(at, day)
    }

    private fun fingerprint(raw: String): String {
        var hash = -3750763034362895579L
        for (char in raw) hash = (hash xor char.code.toLong()) * 1099511628211L
        return hash.toULong().toString(16)
    }

    private fun sameJson(left: String, right: String): Boolean =
        Json.parseToJsonElement(left) == Json.parseToJsonElement(right)

    private fun Throwable.rethrowCancellation() {
        if (this is CancellationException) throw this
    }
}
