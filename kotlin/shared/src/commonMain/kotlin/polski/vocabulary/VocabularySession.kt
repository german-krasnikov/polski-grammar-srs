package polski.vocabulary

import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import polski.data.VocabularyItem
import polski.srs.Rating
import polski.srs.Scheduler

/** The host owns atomic persistence and backups; the session never touches grammar progress. */
interface VocabularyRepository {
    suspend fun loadRaw(): String?
    suspend fun saveRaw(value: String, backupCurrent: Boolean = false)
}

enum class VocabularyLoadStatus { Loading, Ready, RecoveryRequired, Unavailable }

data class VocabularyUiState(
    val loadStatus: VocabularyLoadStatus = VocabularyLoadStatus.Loading,
    val document: VocabularyDocument = VocabularyDocument(),
    val direction: StudyDirection = StudyDirection.RussianToPolish,
    val filter: String = "A1",
    val currentId: String? = null,
    val revealed: Boolean = false,
    val typed: Boolean = false,
    val draft: String = "",
    val busy: Boolean = false,
    val error: String? = null,
    val recoveryRaw: String? = null,
)

/** Serializes review and catalog mutations; a failed write leaves the prior document visible. */
class VocabularySession(
    private val repository: VocabularyRepository,
    private val scheduler: Scheduler,
    private val now: () -> Instant,
    private val newId: () -> String,
) {
    private val lock = Mutex()
    private var savedRaw: String? = null
    private val mutableState = MutableStateFlow(VocabularyUiState())
    val state: StateFlow<VocabularyUiState> = mutableState.asStateFlow()

    suspend fun start() = lock.withLock {
        try {
            val raw = repository.loadRaw()
            savedRaw = raw
            // EnRuAcceptance-2026-09-28.md §7 item 4: was the literal `StudyDirection.RussianToPolish`
            // — wrong for a pack whose pair isn't pl-ru (its wire, "ru-pl", isn't even one of an
            // en-ru session's own 2 directions). [activeStudyDirections] reads the active pack fresh,
            // so pl-ru keeps starting on the exact same "ru-pl" direction as before.
            val initialDirection = activeStudyDirections.first()
            if (raw == null) {
                mutableState.value = VocabularyUiState(loadStatus = VocabularyLoadStatus.Ready, direction = initialDirection)
            } else {
                val decoded = runCatching { VocabularyCodec.decode(raw) }
                mutableState.value = decoded.fold(
                    onSuccess = { document -> VocabularyUiState(
                        loadStatus = VocabularyLoadStatus.Ready, document = document, direction = initialDirection,
                        currentId = nextId(document, initialDirection),
                    ) },
                    onFailure = { error -> VocabularyUiState(
                        loadStatus = VocabularyLoadStatus.RecoveryRequired, recoveryRaw = raw,
                        error = "Словарь нужно восстановить: ${error.message}",
                    ) },
                )
            }
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            mutableState.value = VocabularyUiState(loadStatus = VocabularyLoadStatus.Unavailable,
                error = "Не удалось открыть словарь: ${error.message}")
        }
    }

    fun setDirection(direction: StudyDirection) {
        mutableState.update { current ->
            if (current.direction == direction) current else current.copy(
                direction = direction, currentId = nextId(current.document, direction), revealed = false, draft = "",
            )
        }
    }

    fun setFilter(filter: String) {
        require(filter in listOf("A1", "A2", "B1", "100", "500", "1000", "mine"))
        mutableState.update { it.copy(filter = filter) }
    }

    fun setTyped(typed: Boolean) { mutableState.update { it.copy(typed = typed) } }
    fun setDraft(draft: String) { mutableState.update { it.copy(draft = draft) } }

    fun reveal(): Boolean {
        val current = state.value
        if (current.loadStatus != VocabularyLoadStatus.Ready || current.currentId == null || current.busy) return false
        mutableState.update { it.copy(revealed = true) }
        return true
    }

    fun refresh() {
        mutableState.update { current ->
            if (current.loadStatus != VocabularyLoadStatus.Ready || current.revealed) current
            else current.copy(currentId = nextId(current.document, current.direction))
        }
    }

    suspend fun select(id: String, checked: Boolean): Boolean = commit(
        transform = { VocabularyCodec.select(it.document, id, checked) },
        resetCard = true,
    )

    suspend fun rate(rating: Rating): Boolean = if (rating !in listOf(Rating.Again, Rating.Good)) false else commit(
            transform = { current -> VocabularyCodec.review(current.document, current.currentId!!,
                current.direction, rating, scheduler, now()) },
            resetCard = true,
            allowed = { it.revealed && it.currentId != null },
        )

    suspend fun saveCustom(
        lemma: String, translation: String, form: String, example: String, level: String, editingId: String? = null,
    ): Boolean = commit(transform = { current ->
        val document = current.document
        val id = editingId ?: newId()
        require(editingId == null || document.custom.any { it.id == editingId }) { "Слово не найдено" }
        val item = VocabularyItem(id, lemma.trim(), translation.trim(), form.trim(), example.trim(), level, null, true)
        require((polski.data.vocabularyItems + document.custom).none {
            it.id != id && it.lemma.lowercase() == item.lemma.lowercase()
        }) { "Это польское слово уже есть в словаре" }
        val custom = if (editingId == null) document.custom + item
            else document.custom.map { if (it.id == editingId) item else it }
        document.copy(custom = custom)
    })

    /** The host asks for deletion confirmation before invoking this method. */
    suspend fun deleteCustom(id: String): Boolean = commit(transform = { current ->
        val document = current.document
        require(document.custom.any { it.id == id }) { "Слово не найдено" }
        document.copy(custom = document.custom.filterNot { it.id == id },
            selectedIds = document.selectedIds.filterNot { it == id })
    }, resetCard = true)

    suspend fun importJson(raw: String): Boolean = lock.withLock {
        val previous = state.value
        if (previous.loadStatus !in listOf(VocabularyLoadStatus.Ready, VocabularyLoadStatus.RecoveryRequired)
            || previous.busy) return@withLock false
        var pendingDocument: VocabularyDocument? = null
        var pendingRaw: String? = null
        try {
            // Every UTF-16 code unit encodes to at least one UTF-8 byte, so raw.length is a lower
            // bound on the exact byte count. This cheap check rejects grossly oversized input
            // (e.g. a synthetic 10 MB string) before paying for encodeToByteArray, which is slow
            // enough on Kotlin/JS to blow past test timeouts.
            require(raw.length <= 10_000_000) { "Файл словаря слишком большой" }
            require(raw.encodeToByteArray().size <= 10_000_000) { "Файл словаря слишком большой" }
            val imported = VocabularyCodec.decode(raw)
            val current = state.value
            val merged = if (current.loadStatus == VocabularyLoadStatus.Ready)
                VocabularyCodec.merge(current.document, imported) else imported
            val serialized = VocabularyCodec.encode(merged)
            pendingDocument = merged
            pendingRaw = serialized
            mutableState.update { it.copy(busy = true) }
            repository.saveRaw(serialized, backupCurrent = true)
            check(repository.loadRaw() == serialized) { "Словарь не прошёл проверку записи" }
            completeWrite(merged, serialized, resetCard = true)
            true
        } catch (error: Throwable) {
            val result = if (state.value.busy && pendingDocument != null && pendingRaw != null)
                reconcile(previous, pendingDocument, pendingRaw, error, resetCard = true)
            else {
                mutableState.update { it.copy(error = error.message ?: "Импорт не удался") }
                false
            }
            if (error is CancellationException) throw error
            result
        } finally {
            if (state.value.busy) mutableState.update { it.copy(busy = false,
                loadStatus = VocabularyLoadStatus.Unavailable, error = "Не удалось проверить запись словаря") }
        }
    }

    fun exportJson(): String? {
        val current = state.value
        return current.recoveryRaw ?: if (current.loadStatus == VocabularyLoadStatus.Ready)
            VocabularyCodec.encode(current.document) else null
    }

    private suspend fun commit(transform: (VocabularyUiState) -> VocabularyDocument, resetCard: Boolean = false,
                               allowed: (VocabularyUiState) -> Boolean = { true }): Boolean =
        lock.withLock {
            val current = state.value
            if (current.loadStatus != VocabularyLoadStatus.Ready || current.busy || !allowed(current)) return@withLock false
            var pendingDocument: VocabularyDocument? = null
            var pendingRaw: String? = null
            try {
                val next = transform(current)
                val serialized = VocabularyCodec.encode(next)
                pendingDocument = next
                pendingRaw = serialized
                mutableState.update { it.copy(busy = true) }
                repository.saveRaw(serialized)
                check(repository.loadRaw() == serialized) { "Словарь не прошёл проверку записи" }
                completeWrite(next, serialized, resetCard)
                true
            } catch (error: Throwable) {
                val result = if (state.value.busy && pendingDocument != null && pendingRaw != null)
                    reconcile(current, pendingDocument, pendingRaw, error, resetCard)
                else {
                    mutableState.update { it.copy(error = error.message ?: "Не удалось сохранить словарь") }
                    false
                }
                if (error is CancellationException) throw error
                result
            } finally {
                if (state.value.busy) mutableState.update { it.copy(busy = false,
                    loadStatus = VocabularyLoadStatus.Unavailable, error = "Не удалось проверить запись словаря") }
            }
        }

    private fun completeWrite(document: VocabularyDocument, raw: String, resetCard: Boolean) {
        savedRaw = raw
        val latest = state.value
        mutableState.value = latest.copy(loadStatus = VocabularyLoadStatus.Ready, document = document,
            currentId = nextId(document, latest.direction), revealed = if (resetCard) false else latest.revealed,
            draft = if (resetCard) "" else latest.draft, busy = false, error = null, recoveryRaw = null)
    }

    private suspend fun reconcile(previous: VocabularyUiState, document: VocabularyDocument, raw: String,
                                  cause: Throwable, resetCard: Boolean): Boolean {
        val observed = withContext(NonCancellable) { runCatching { repository.loadRaw() } }
        return observed.fold(onSuccess = { actual ->
            when (actual) {
                raw -> { completeWrite(document, raw, resetCard); true }
                savedRaw -> {
                    mutableState.update { it.copy(busy = false,
                        error = cause.message ?: "Запись словаря не завершилась") }
                    false
                }
                else -> {
                    savedRaw = actual
                    mutableState.update { it.copy(loadStatus = VocabularyLoadStatus.RecoveryRequired,
                        recoveryRaw = actual, busy = false,
                        error = "Состояние словаря изменилось во время записи. Экспортируйте JSON перед восстановлением.") }
                    false
                }
            }
        }, onFailure = { readFailure ->
            mutableState.update { it.copy(loadStatus = VocabularyLoadStatus.Unavailable,
                recoveryRaw = previous.recoveryRaw ?: savedRaw, busy = false,
                error = "Не удалось проверить словарь после записи: ${readFailure.message}") }
            false
        })
    }

    private fun nextId(document: VocabularyDocument, direction: StudyDirection): String? =
        VocabularyCodec.dueIds(document, direction, scheduler, now()).firstOrNull()
}
