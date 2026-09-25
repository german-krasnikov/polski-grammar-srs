package polski.desktop

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MacSystemPreferencesTest {
    @Test
    fun exactVersionedSnapshotParsesAllThreeSignals() {
        assertEquals(MacSystemAppearance(true, false, true),
            MacDisplayProtocol.parse("v1 dark=1 motion=0 contrast=1"))
        assertEquals(MacSystemAppearance(false, true, false),
            MacDisplayProtocol.parse("v1 dark=0 motion=1 contrast=0"))
    }

    @Test
    fun malformedAndUnknownSnapshotsAreRejected() {
        listOf("", "v2 dark=1 motion=0 contrast=0", "v1 dark=2 motion=0 contrast=0",
            "v1 dark=1 motion=0", "v1 dark=1 motion=0 contrast=0 extra=1")
            .forEach { assertNull(MacDisplayProtocol.parse(it)) }
    }

    @Test
    fun childChangesMalformedEofAndCloseHaveExplicitLifecycle() {
        val child = FakeObserverProcess()
        val observer = MacSystemPreferences { child }
        observer.start()
        observer.start()
        child.send("v1 dark=0 motion=0 contrast=0")
        await { observer.status is MacSystemStatus.Available }
        child.send("v1 dark=1 motion=1 contrast=1")
        await { (observer.status as? MacSystemStatus.Available)?.appearance == MacSystemAppearance(true, true, true) }
        child.send("bad")
        await { observer.status is MacSystemStatus.Unavailable }
        observer.close()
        assertTrue(child.destroyed)
        assertIs<MacSystemStatus.Unavailable>(observer.status)

        val eofChild = FakeObserverProcess()
        val eofObserver = MacSystemPreferences { eofChild }
        eofObserver.start()
        eofChild.finish()
        await { eofObserver.status is MacSystemStatus.Unavailable }
        eofObserver.close()
    }

    private fun await(condition: () -> Boolean) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3)
        while (!condition() && System.nanoTime() < deadline) Thread.sleep(10)
        assertTrue(condition(), "Observer did not reach expected status")
    }

    private class FakeObserverProcess : Process() {
        private val source = PipedInputStream()
        private val writer = PipedOutputStream(source)
        var destroyed = false
            private set
        fun send(line: String) { writer.write("$line\n".toByteArray()); writer.flush() }
        fun finish() { writer.close() }
        override fun getInputStream() = source
        override fun getOutputStream() = ByteArrayOutputStream()
        override fun getErrorStream() = ByteArrayInputStream(byteArrayOf())
        override fun waitFor(): Int = 0
        override fun exitValue(): Int = 0
        override fun destroy() { destroyed = true; runCatching { writer.close() } }
    }
}
