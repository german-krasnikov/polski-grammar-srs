package polski.macos

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUUID
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile
import platform.posix.rename
import polski.preferences.PreferencesDecode
import polski.preferences.UserPreferencesCodec
import polski.preferences.UserPreferencesV2

/** Versioned preferences beside the legacy JVM preview documents; invalid input is never overwritten. */
@OptIn(ExperimentalForeignApi::class)
class MacPreferencesRepository(private val directory: String) {
    private val file = "$directory/preferences-v1.json"
    fun raw(): String? = read(file)
    fun load(): PreferencesDecode = raw()?.let(UserPreferencesCodec::decode)
        ?: PreferencesDecode.Loaded(UserPreferencesV2())

    fun save(value: UserPreferencesV2): String? = replace(UserPreferencesCodec.encode(value), false)
    fun importJson(raw: String): String? = replace(raw, true)

    private fun replace(raw: String, backup: Boolean): String? {
        if (raw.encodeToByteArray().size > 1_000_000) return "Файл настроек слишком большой"
        when (val decoded = UserPreferencesCodec.decode(raw)) {
            is PreferencesDecode.RecoveryRequired -> return decoded.reason
            is PreferencesDecode.Loaded -> Unit
        }
        return try {
            val old = read(file)
            if (old != null) {
                if (backup) writeVerified("$directory/backups/preferences-${NSUUID().UUIDString}.json", old)
                else if (UserPreferencesCodec.decode(old) !is PreferencesDecode.Loaded) return "Настройки требуют восстановления"
            }
            writeVerified(file, raw)
            null
        } catch (error: Throwable) { error.message ?: "Не удалось сохранить настройки" }
    }

    private fun read(path: String): String? {
        if (!NSFileManager.defaultManager.fileExistsAtPath(path)) return null
        return NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)
            ?: error("Unreadable UTF-8 preferences file: $path")
    }

    private fun writeVerified(path: String, raw: String) {
        val parent = path.substringBeforeLast('/')
        check(NSFileManager.defaultManager.createDirectoryAtPath(parent, true, null, null))
        val temp = "$path.tmp-${NSUUID().UUIDString}"
        try {
            check((raw as NSString).writeToFile(temp, true, NSUTF8StringEncoding, null))
            check(read(temp) == raw)
            check(rename(temp, path) == 0)
            check(read(path) == raw)
        } finally { NSFileManager.defaultManager.removeItemAtPath(temp, null) }
    }
}
