package polski.ios

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import platform.Foundation.NSUserDefaults
import polski.vocabulary.VocabularyCodec
import polski.vocabulary.VocabularyRepository

/** App-private vocabulary document, independent of grammar progress. */
class IosVocabularyRepository(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : VocabularyRepository {
    private val lock = Mutex()
    // EnRuAcceptance-2026-09-28.md §7 item 2: was the literal "polski-vocabulary-pl-ru-v1" —
    // now [VocabularyCodec.key], read fresh on every call so a pack switch loads/saves that
    // pack's own, separately-namespaced document (pl-ru's own key is the exact same bytes as
    // before; en-ru gets its own "polski-vocabulary-en-ru-v1").
    private val key: String get() = VocabularyCodec.key
    private val backupKey = "polski-vocabulary-import-backup-latest"

    override suspend fun loadRaw(): String? = withContext(Dispatchers.Default) {
        lock.withLock { defaults.stringForKey(key) }
    }

    override suspend fun saveRaw(value: String, backupCurrent: Boolean) = withContext(Dispatchers.Default) {
        lock.withLock {
            require(value.encodeToByteArray().size <= 10_000_000) { "Словарь слишком большой" }
            VocabularyCodec.decode(value)
            val current = defaults.stringForKey(key)
            if (current != null) {
                if (backupCurrent) {
                    // Only the latest pre-import document is kept as a backup.
                    put(backupKey, current)
                    check(defaults.stringForKey(backupKey) == current) { "Резервная копия словаря не сохранилась" }
                } else VocabularyCodec.decode(current)
            }
            put(key, value)
            check(defaults.stringForKey(key) == value) { "Словарь не прошёл проверку записи" }
        }
    }

    private fun put(key: String, value: String) {
        defaults.setObject(value, forKey = key)
        check(defaults.synchronize()) { "Словарь не сохранился" }
    }
}

