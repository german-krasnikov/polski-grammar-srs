package polski.ios

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUUID
import polski.vocabulary.VocabularyCodec
import polski.vocabulary.VocabularyRepository

/** App-private vocabulary document, independent of grammar progress. */
class IosVocabularyRepository(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : VocabularyRepository {
    private val lock = Mutex()
    private val key = "polski-vocabulary-pl-ru-v1"

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
                    val backupKey = "polski-vocabulary-import-backup-${NSUUID().UUIDString}"
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
