package polski.desktop

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.SwingUtilities

internal data class MacSystemAppearance(
    val isDark: Boolean,
    val reduceMotion: Boolean,
    val increaseContrast: Boolean,
)

internal object MacDisplayProtocol {
    private val snapshot = Regex("v1 dark=([01]) motion=([01]) contrast=([01])")

    fun parse(line: String): MacSystemAppearance? {
        val match = snapshot.matchEntire(line) ?: return null
        return MacSystemAppearance(match.groupValues[1] == "1", match.groupValues[2] == "1",
            match.groupValues[3] == "1")
    }
}

internal sealed interface MacSystemStatus {
    data object Loading : MacSystemStatus
    data class Available(val appearance: MacSystemAppearance) : MacSystemStatus
    data class Unavailable(val reason: String) : MacSystemStatus
}

/** Owns one macOS observer child process for the lifetime of a desktop window. */
internal class MacSystemPreferences(
    private val launch: () -> Process = {
        val executable = observerExecutable()
        require(Files.isRegularFile(executable) && Files.isExecutable(executable)) {
            "Mac display observer is missing or not executable: $executable"
        }
        ProcessBuilder(executable.toString()).redirectError(ProcessBuilder.Redirect.INHERIT).start()
    },
) : AutoCloseable {
    @Volatile private var closed = false
    private var started = false
    private var process: Process? = null
    private var reader: Thread? = null

    var status by mutableStateOf<MacSystemStatus>(MacSystemStatus.Loading)
        private set

    @Synchronized fun start() {
        if (started || closed) return
        started = true
        try {
            val child = launch()
            process = child
            reader = Thread({ readSnapshots(child) }, "polski-mac-display-observer").apply {
                isDaemon = true
                start()
            }
        } catch (error: Exception) {
            publish(MacSystemStatus.Unavailable(error.message ?: "Unable to start Mac display observer"))
        }
    }

    private fun readSnapshots(child: Process) {
        try {
            child.inputStream.bufferedReader().use { source ->
                while (!closed) {
                    val line = source.readLine() ?: break
                    val value = if (line.length <= 120) MacDisplayProtocol.parse(line) else null
                    if (value == null) {
                        publish(MacSystemStatus.Unavailable("Invalid Mac display observer snapshot"))
                        child.destroy()
                        return
                    }
                    publish(MacSystemStatus.Available(value))
                }
            }
            if (!closed) publish(MacSystemStatus.Unavailable("Mac display observer stopped"))
        } catch (error: Exception) {
            if (!closed) publish(MacSystemStatus.Unavailable(error.message ?: "Mac display observer failed"))
        }
    }

    private fun publish(next: MacSystemStatus) {
        SwingUtilities.invokeLater { if (!closed) status = next }
    }

    @Synchronized override fun close() {
        if (closed) return
        closed = true
        reader?.interrupt()
        process?.destroy()
        runCatching { process?.inputStream?.close() }
        runCatching { process?.outputStream?.close() }
        runCatching { process?.errorStream?.close() }
    }
}

private fun observerExecutable(): Path {
    val explicit = System.getProperty("polski.mac.observer.path")
    if (!explicit.isNullOrBlank()) return Path.of(explicit)
    val resources = System.getProperty("compose.application.resources.dir")
        ?: error("Packaged macOS resources directory is unavailable")
    return Path.of(resources, "MacDisplayObserver")
}
