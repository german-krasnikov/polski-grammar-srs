package polski.ui

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import org.w3c.dom.HTMLElement
import polski.presentation.AppUiState
import kotlin.coroutines.resume
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun newProbe(): JsAny = js("new MutationObserver(function () {})")
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun observeBody(probe: JsAny, body: HTMLElement): Unit = js("probe.observe(body, { childList: true })")
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun addedMatrixNodes(probe: JsAny): Int = js("probe.takeRecords().reduce((count, record) => count + Array.from(record.addedNodes).filter(node => node.nodeType === 1 && node.matches('main.matrix-page')).length, 0)")
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun disconnect(probe: JsAny): Unit = js("probe.disconnect()")

// Retain callbacks after cancellation to model a browser callback already dequeued at disposal.
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun captureBrowserWork(): JsAny = js("""(() => {
    const originalTimeout = window.setTimeout;
    const originalClearTimeout = window.clearTimeout;
    const originalRaf = window.requestAnimationFrame;
    const originalCancelRaf = window.cancelAnimationFrame;
    const timers = [];
    const frames = [];
    window.setTimeout = callback => { timers.push(callback); return timers.length; };
    window.clearTimeout = () => {};
    window.requestAnimationFrame = callback => { frames.push(callback); return frames.length; };
    window.cancelAnimationFrame = () => {};
    return {
      timerCount: () => timers.length,
      frameCount: () => frames.length,
      deliverTimer: index => timers[index](),
      deliverFrame: index => frames[index](0),
      restore: () => {
        window.setTimeout = originalTimeout;
        window.clearTimeout = originalClearTimeout;
        window.requestAnimationFrame = originalRaf;
        window.cancelAnimationFrame = originalCancelRaf;
      }
    };
  })()""")
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun timerCount(work: JsAny): Int = js("work.timerCount()")
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun frameCount(work: JsAny): Int = js("work.frameCount()")
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun deliverTimer(work: JsAny, index: Int): Unit = js("work.deliverTimer(index)")
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun deliverFrame(work: JsAny, index: Int): Unit = js("work.deliverFrame(index)")
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun restoreBrowserWork(work: JsAny): Unit = js("work.restore()")

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun captureResizeListeners(): JsAny = js("""(() => {
    const add = window.addEventListener;
    const remove = window.removeEventListener;
    let added = 0;
    let removed = 0;
    window.addEventListener = function(type, listener, options) {
      if (type === 'resize') added++;
      return add.call(this, type, listener, options);
    };
    window.removeEventListener = function(type, listener, options) {
      if (type === 'resize') removed++;
      return remove.call(this, type, listener, options);
    };
    return { added: () => added, removed: () => removed, restore: () => {
      window.addEventListener = add;
      window.removeEventListener = remove;
    }};
  })()""")
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun resizeAdded(probe: JsAny): Int = js("probe.added()")
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun resizeRemoved(probe: JsAny): Int = js("probe.removed()")
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun restoreResizeListeners(probe: JsAny): Unit = js("probe.restore()")

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private suspend fun waitForBrowserTimer() = suspendCancellableCoroutine<Unit> { continuation ->
    val timer = window.setTimeout({ continuation.resume(Unit); null }, 80)
    continuation.invokeOnCancellation { window.clearTimeout(timer) }
}

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
class TrainingDomRendererLifecycleTest {
    private class RecordingTimer : MatrixPrewarmTimer {
        var scheduledDelay: Int? = null
        var callback: (() -> Unit)? = null
        var pendingId: Int? = null
        var cancellations = 0

        override fun schedule(delayMs: Int, callback: () -> Unit): Int {
            scheduledDelay = delayMs
            this.callback = callback
            pendingId = 1
            return 1
        }

        override fun cancel(id: Int) {
            assertEquals(1, id)
            cancellations++
            pendingId = null
        }

        fun deliverEvenIfCancelled() { callback?.invoke() }
    }

    @Test
    fun closeCancelsPrewarmAndStaleCallbackCannotTouchDomButNewMountCan() {
        val body = document.body as HTMLElement
        val probe = newProbe()
        observeBody(probe, body)
        try {
            val firstTimer = RecordingTimer()
            val first = TrainingDomRenderer(firstTimer)
            var dispatches = 0
            first.scheduleMatrixPrewarm(AppUiState()) { dispatches++ }
            first.scheduleMatrixPrewarm(AppUiState()) { dispatches++ }
            assertEquals(1500, firstTimer.scheduledDelay)
            assertEquals(1, firstTimer.pendingId)

            first.close()
            first.close()
            assertEquals(1, firstTimer.cancellations)
            assertNull(firstTimer.pendingId)
            firstTimer.deliverEvenIfCancelled()
            assertEquals(0, addedMatrixNodes(probe))
            assertEquals(0, dispatches)

            val secondTimer = RecordingTimer()
            val second = TrainingDomRenderer(secondTimer)
            second.scheduleMatrixPrewarm(AppUiState()) { dispatches++ }
            secondTimer.deliverEvenIfCancelled()
            assertEquals(1, addedMatrixNodes(probe))
            assertNull(body.querySelector("main.matrix-page"))
            assertEquals(0, dispatches)
            second.close()
        } finally {
            disconnect(probe)
        }
    }

    @Test
    fun closingPendingSlideCancelsItsDeferredBuildWithoutCompletionCallbacks() = runTest {
        val viewport = document.createElement("div") as HTMLElement
        val outgoing = document.createElement("div") as HTMLElement
        outgoing.className = "route-content"
        viewport.appendChild(outgoing)
        document.body?.appendChild(viewport)
        try {
            var builds = 0
            var mounts = 0
            var settles = 0
            val slider = RouteSlider()
            slider.start(viewport, {
                builds++
                (document.createElement("div") as HTMLElement).apply { className = "route-content" }
            }, direction = 1, instant = false, onMounted = { mounts++ }, onSettled = { settles++ })
            assertTrue(slider.isPending())
            slider.close()
            slider.close()
            assertFalse(slider.isPending())
            waitForBrowserTimer()
            assertEquals(0, builds)
            assertEquals(0, mounts)
            assertEquals(0, settles)
            assertEquals(1, viewport.querySelectorAll(":scope > .route-content").length)
            assertSame(outgoing, viewport.firstChild)
        } finally {
            viewport.remove()
        }
    }

    @Test
    fun closeDuringMountedSlideIgnoresDequeuedFramesAndTimerAndLeavesNewRootIntact() {
        val oldViewport = document.createElement("div") as HTMLElement
        val outgoing = (document.createElement("div") as HTMLElement).apply { className = "route-content" }
        oldViewport.appendChild(outgoing)
        document.body?.appendChild(oldViewport)
        val newViewport = document.createElement("div") as HTMLElement
        document.body?.appendChild(newViewport)
        val work = captureBrowserWork()
        try {
            var oldMounts = 0
            var oldSettles = 0
            var oldBuilds = 0
            val first = RouteSlider()
            first.start(oldViewport, {
                oldBuilds++
                (document.createElement("div") as HTMLElement).apply { className = "route-content" }
            }, direction = 1, instant = false, onMounted = { oldMounts++ }, onSettled = { oldSettles++ })
            assertEquals(1, timerCount(work))
            deliverTimer(work, 0) // The incoming layer is mounted; first rAF is pending.
            assertEquals(2, oldViewport.querySelectorAll(":scope > .route-content").length)
            assertEquals("true", outgoing.getAttribute("aria-hidden"))
            assertEquals(1, frameCount(work))
            first.close()
            oldViewport.remove() // Compose removes the disposed host's DOM.

            val fresh = RouteSlider()
            var freshMounts = 0
            var freshSettles = 0
            fresh.start(newViewport, {
                (document.createElement("div") as HTMLElement).apply {
                    className = "route-content"
                    textContent = "new mount"
                }
            }, direction = 1, instant = true, onMounted = { freshMounts++ }, onSettled = { freshSettles++ })
            deliverFrame(work, 0) // A cancelled first rAF still arrives after remount.
            deliverTimer(work, 0) // A cancelled build callback is delivered again.
            assertEquals(1, oldBuilds)
            assertEquals(0, oldMounts)
            assertEquals(0, oldSettles)
            assertEquals(1, frameCount(work)) // The old first rAF cannot queue a second.
            assertEquals(1, newViewport.querySelectorAll(":scope > .route-content").length)
            assertEquals("new mount", newViewport.textContent)
            assertEquals(1, freshMounts)
            assertEquals(1, freshSettles)
            fresh.close()

            val secondViewport = document.createElement("div") as HTMLElement
            val secondOutgoing = (document.createElement("div") as HTMLElement).apply { className = "route-content" }
            secondViewport.appendChild(secondOutgoing)
            document.body?.appendChild(secondViewport)
            try {
                val second = RouteSlider()
                var secondMounts = 0
                var secondSettles = 0
                second.start(secondViewport, {
                    (document.createElement("div") as HTMLElement).apply { className = "route-content" }
                }, direction = 1, instant = false, onMounted = { secondMounts++ }, onSettled = { secondSettles++ })
                deliverTimer(work, 1)
                deliverFrame(work, 1)
                assertEquals(3, frameCount(work))
                deliverFrame(work, 2) // Both rAF stages complete; finish timer is pending.
                assertEquals(1, secondMounts)
                assertEquals(2, secondViewport.querySelectorAll(":scope > .route-content").length)
                assertEquals(3, timerCount(work))
                second.close()
                assertEquals(1, secondViewport.querySelectorAll(":scope > .route-content").length)
                assertSame(secondOutgoing, secondViewport.firstChild)
                assertFalse(secondOutgoing.hasAttribute("inert"))
                assertFalse(secondOutgoing.hasAttribute("aria-hidden"))
                deliverTimer(work, 2) // The cancelled finish callback cannot settle after close.
                deliverFrame(work, 2) // Nor can a dequeued second rAF repeat its mount callback.
                assertEquals(1, secondMounts)
                assertEquals(0, secondSettles)
                assertEquals(1, secondViewport.querySelectorAll(":scope > .route-content").length)
                assertEquals("new mount", newViewport.textContent)
            } finally {
                secondViewport.remove()
            }

            val thirdViewport = document.createElement("div") as HTMLElement
            thirdViewport.appendChild((document.createElement("div") as HTMLElement).apply { className = "route-content" })
            document.body?.appendChild(thirdViewport)
            try {
                val third = RouteSlider()
                var lateMounts = 0
                third.start(thirdViewport, {
                    (document.createElement("div") as HTMLElement).apply { className = "route-content" }
                }, direction = 1, instant = false, onMounted = { lateMounts++ }, onSettled = { })
                deliverTimer(work, 3)
                deliverFrame(work, 3) // Stage one has queued stage two.
                assertEquals(5, frameCount(work))
                third.close()
                deliverFrame(work, 4) // Cancelled second rAF arrives after close.
                assertEquals(0, lateMounts)
                assertEquals(1, thirdViewport.querySelectorAll(":scope > .route-content").length)
                assertEquals("new mount", newViewport.textContent)
            } finally {
                thirdViewport.remove()
            }
        } finally {
            restoreBrowserWork(work)
            oldViewport.remove()
            newViewport.remove()
        }
    }

    @Test
    fun webNavCloseRemovesResizeListenerAndIgnoresQueuedInitialFrames() {
        val app = document.createElement("div") as HTMLElement
        document.body?.appendChild(app)
        val work = captureBrowserWork()
        val listeners = captureResizeListeners()
        try {
            val old = WebNav(app) { }
            assertEquals(1, resizeAdded(listeners))
            assertEquals(1, frameCount(work))
            old.close()
            old.close()
            assertEquals(1, resizeRemoved(listeners))
            assertEquals(0, app.querySelectorAll("nav.primary-nav").length)
            val fresh = WebNav(app) { }
            assertEquals(2, resizeAdded(listeners))
            deliverFrame(work, 0)
            assertEquals(2, frameCount(work)) // Old rAF cannot schedule its second stage.
            window.dispatchEvent(org.w3c.dom.events.Event("resize"))
            assertEquals(1, app.querySelectorAll("nav.primary-nav").length)
            fresh.close()
            assertEquals(2, resizeRemoved(listeners))
        } finally {
            restoreResizeListeners(listeners)
            restoreBrowserWork(work)
            app.remove()
        }
    }
}
