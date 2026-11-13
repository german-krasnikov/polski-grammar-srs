package polski.ui

import androidx.compose.runtime.collectAsState
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.HTMLInputElement
import kotlin.time.Clock
import polski.presentation.*
import polski.srs.Rating

private const val ROUTE_FOOTER_TEXT = "Прогресс сохраняется в этом браузере. Интервальные повторения — FSRS."

/** UX5 perf: delay before the idle Matrix prewarm runs (see [TrainingDomRenderer.scheduleMatrixPrewarm]).
 *  There is no `requestIdleCallback` binding in either DOM binding this project has (kotlinx-browser/
 *  kotlin-dom-api-compat), so a plain timer stands in for "idle enough" — long enough to sit well
 *  after the very first paint/layout it must never compete with, short enough to still land well
 *  before a real first click could plausibly happen. */
private const val MATRIX_PREWARM_DELAY_MS = 1500

/** Only the Matrix idle timer is replaceable; route and card DOM remain real in web tests. */
internal interface MatrixPrewarmTimer {
    fun schedule(delayMs: Int, callback: () -> Unit): Int
    fun cancel(id: Int)
}

internal object WindowMatrixPrewarmTimer : MatrixPrewarmTimer {
    override fun schedule(delayMs: Int, callback: () -> Unit): Int = window.setTimeout({ callback(); null }, delayMs)
    override fun cancel(id: Int) { window.clearTimeout(id) }
}

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
internal class TrainingDomRenderer(private val prewarmTimer: MatrixPrewarmTimer = WindowMatrixPrewarmTimer) {
    private var previous: AppUiState? = null
    private var previousRoute: WebRoute? = null
    private var previousPreferences: polski.preferences.UserPreferencesV2? = null
    private var previousStatus: String? = null
    var composing = false
    private val vocabulary = VocabularyWebController()
    private val exam = ExamWebController()
    private var navigation: WebNav? = null
    private val riveOverlay = RiveEffectOverlay()
    private val cardDom = TrainingCardDom(riveOverlay) { composing }
    private val slider = RouteSlider()

    // Correction round (reviewer finding — see FlipCardRivePlan.md §19.11): this used to also keep
    // a persistent, deps-keyed cache of the built Matrix `.route-content` alive across visits. Two
    // problems killed that design: (1) one of its own call sites (a same-route update landing mid-
    // slide, once RouteSlider already had a live incoming) mutated the live cached node's content
    // without updating the cache's recorded deps, so a LATER fresh entry landing on those stale
    // recorded deps got served whatever had been mutated in — a real stale/wrong-state bug, not a
    // hypothetical; (2) re-measuring honestly (stashing this file back to its pre-cache commit and
    // rebuilding — see `before_head_fb4cdcd` in tables-hitch.json) showed a REPEAT visit was already
    // just as cheap with NO caching at all, brand-new DOM every time: the ~60ms long task the idle
    // prewarm below targets is a one-time cost the browser's layout/style engine pays once per PAGE
    // LIFETIME the first time it lays out this particular heavy subtree (several large tables) —
    // not a per-node cost caching could ever have saved on a repeat visit. The persistent cache was
    // carrying real correctness risk for zero measured benefit beyond what the prewarm alone gives;
    // dropped entirely rather than patched, which is also why Matrix no longer needs any special
    // case in [render] below — it now takes the exact same `node("div", "route-content").also(::populate)`
    // path every other route always has.
    private var matrixPrewarmScheduled = false
    private var matrixPrewarmTimerId: Int? = null
    private var closed = false

    fun close() {
        if (closed) return
        closed = true
        matrixPrewarmTimerId?.let(prewarmTimer::cancel)
        matrixPrewarmTimerId = null
        slider.close()
        navigation?.close()
        navigation = null
        riveOverlay.close()
        vocabulary.close()
        exam.close()
    }

    /** UX4-14: delegates to the Vocabulary controller for the shared keydown handler. */
    fun rateVocabularyIfRevealed(rating: Rating, refresh: () -> Unit) = vocabulary.rateCurrentIfRevealed(rating, refresh)

    /** UX5: delegates Space/Enter (reveal-or-flip) to the Vocabulary controller for the shared keydown handler. */
    fun spaceVocabulary(refresh: () -> Unit) = vocabulary.spaceReveal(refresh)

    /** UX5 perf: once, ~[MATRIX_PREWARM_DELAY_MS] after the very first render (and only if that
     *  first render's route isn't already Matrix — a real visit already paid this cost itself),
     *  builds the Matrix route's heavy table subtree purely to force the browser's OWN one-time
     *  layout/style pass over it early, then throws the node away — nothing here is ever stored or
     *  reused. RED (measured — FlipCardRivePlan.md §19.10/§19.11/tables-hitch.json): building it
     *  off-DOM only (no attach at all) did NOT stop the real click's long task from reappearing, but
     *  a genuine no-cache repeat visit (brand-new DOM) already WAS cheap — proving the cost is paid
     *  once per page lifetime by the engine itself, not per node/per cache-hit. So a throwaway
     *  build+attach+layout+discard here is enough to make the real first click just as cheap as any
     *  later one, without keeping anything alive to ever go stale. `visibility:hidden` (not
     *  `display:none`, which skips layout entirely) and `pointer-events:none` keep it inert and
     *  unhittable for the one synchronous moment it's attached (`position:absolute` avoids a
     *  transient height/scrollbar change); nothing yields between appendChild and remove, so nothing
     *  else ever observes it there. GREEN: the real click after this window shows zero long tasks,
     *  same as a repeat visit (measured — same file). Never touches `vocabulary`. */
    internal fun scheduleMatrixPrewarm(state: AppUiState, dispatch: (AppAction) -> Unit) {
        if (closed || matrixPrewarmScheduled) return
        matrixPrewarmScheduled = true
        matrixPrewarmTimerId = prewarmTimer.schedule(MATRIX_PREWARM_DELAY_MS) {
            matrixPrewarmTimerId = null
            if (closed) return@schedule
            val content = node("main", "matrix-page")
            renderMatrixWeb(content, state, dispatch)
            content.style.setProperty("position", "absolute")
            content.style.setProperty("visibility", "hidden")
            content.style.setProperty("pointer-events", "none")
            document.body?.appendChild(content)
            content.getBoundingClientRect() // forces the one-time layout/style pass early
            content.remove()
        }
    }

    fun render(root: HTMLElement, state: AppUiState, route: WebRoute, returnTo: WebRoute, preferences: WebPreferencesController, store: TrainingStore, navigate: (WebRoute) -> Unit, dispatch: (AppAction) -> Unit) {
        if (closed) return
        val old = previous
        // v3/A: true only for the render() call where `phase` just became Revealed — every OTHER
        // render that rebuilds an already-revealed card's DOM (e.g. the 30s RefreshTime timer)
        // must show the answer already expanded, not replay the expand-reveal animation.
        val justRevealed = old != null && old.phase == CardPhase.Question && state.phase == CardPhase.Revealed
        val enteringChainComplete = old != null && old.phase != CardPhase.ChainComplete && state.phase == CardPhase.ChainComplete
        // UX4-20/22: true only for the render() call where the case-reference panel just turned
        // on — every other render (timer, rating, etc.) that rebuilds it while already shown must
        // not replay the reveal animation. Only the appearing direction animates (see
        // renderTraining); hiding it is instant, matching its existing conditional-mount design.
        val justShowedReference = old != null && !old.showReference && state.showReference
        if (old != null && previousRoute == route && previousPreferences == preferences.value && previousStatus == preferences.status && old.copy(draft = state.draft) == state) {
            previous = state
            return // Keep the live textarea, selection and IME composition intact.
        }
        if (route == WebRoute.Exam && old != null && previousRoute == route &&
            previousPreferences == preferences.value && previousStatus == preferences.status && !slider.isPending() &&
            old.copy(
                progress = state.progress, dueCount = state.dueCount, todayCount = state.todayCount,
                nextDue = state.nextDue, intervals = state.intervals, now = state.now, localDay = state.localDay,
            ) == state
        ) {
            // The exam controller owns its live editor. Clock/focus refreshes update the header
            // without replacing that editor, which would interrupt typing and IME composition.
            root.querySelector(".topstats div:first-child b")?.textContent = state.dueCount.toString()
            root.querySelector(".topstats div:nth-child(2) b")?.textContent = state.todayCount.toString()
            previous = state
            return
        }
        if (composing && old != null && previousRoute == route && previousPreferences == preferences.value && old.exerciseId == state.exerciseId && old.phase == CardPhase.Question) {
            previous = state
            return // A timer/visibility refresh must not replace an active IME editor.
        }
        val focusId = (document.activeElement as? HTMLElement)?.id.orEmpty()
        // A rebuild always recreates the focused element, so caret and selection would otherwise
        // reset to the end of the text every time (e.g. the periodic RefreshTime timer).
        val focusedEditor = document.activeElement as? HTMLElement
        val focusedSelection = when (focusedEditor) {
            is HTMLTextAreaElement -> Triple(focusedEditor.selectionStart, focusedEditor.selectionEnd, focusedEditor.scrollTop)
            is HTMLInputElement -> if (focusedEditor.type in setOf("text", "search", "tel", "url", "password")) {
                Triple(focusedEditor.selectionStart, focusedEditor.selectionEnd, focusedEditor.scrollTop)
            } else null
            else -> null
        }
        val scroll = root.scrollTop
        val innerScroll = root.querySelectorAll("[data-scroll-key]")
        val scrollPositions = buildMap {
            for (index in 0 until innerScroll.length) {
                val node = innerScroll.item(index) as? HTMLElement ?: continue
                val key = node.getAttribute("data-scroll-key") ?: continue
                put(key, node.scrollLeft to node.scrollTop)
            }
        }
        // UX5: a genuine route change (not the 30s timer, not a rating re-render) pages the
        // content sideways instead of rebuilding in place — everything below this point only
        // decides WHICH of those two happens; `populate` itself is identical either way.
        val routeChangedFrom = previousRoute
        val routeChanged = old != null && routeChangedFrom != null && routeChangedFrom != route
        if (routeChanged) {
            if (routeChangedFrom == WebRoute.Training) riveOverlay.cancel()
            if (routeChangedFrom == WebRoute.Vocabulary) vocabulary.deactivate()
        }
        val app = (root.querySelector(":scope > .app") as? HTMLElement)
            ?: node("div", "app").also(root::appendChild)
        val nav = navigation ?: WebNav(app, navigate).also { navigation = it }
        nav.update(route)
        app.querySelector(":scope > header.top")?.remove()
        val headerHolder = node("div")
        renderHeader(headerHolder, state)
        headerHolder.firstChild?.let { app.insertBefore(it, nav.shell) }
        val viewport = (app.querySelector(":scope > .route-viewport") as? HTMLElement)
            ?: node("div", "route-viewport").also(app::appendChild)

        fun populate(content: HTMLElement) {
            if (state.error != null) {
                content.appendChild(node("p", "notice", state.error).apply { setAttribute("role", "alert") })
            }
            // A cross-tab storage write must not replay a Vocabulary render callback captured for a
            // route this host has since navigated away from.
            if (route != WebRoute.Vocabulary) vocabulary.deactivate()
            if (route != WebRoute.Exam) exam.close()
            when (route) {
                WebRoute.Training -> cardDom.renderTraining(root, content, state, preferences.value.swipeRatingEnabled, enteringChainComplete, justRevealed, justShowedReference, dispatch)
                WebRoute.Vocabulary -> vocabulary.render(node("main", "vocabulary-page").also(content::appendChild), root, preferences.value.swipeRatingEnabled) {
                    previous = null
                    render(root, state, route, returnTo, preferences, store, navigate, dispatch)
                }
                WebRoute.Matrix -> renderMatrixWeb(node("main", "matrix-page").also(content::appendChild), state, dispatch)
                WebRoute.Progress -> renderProgressWeb(node("main", "progress-page").also(content::appendChild), state, dispatch)
                WebRoute.Exam -> {
                    exam.immediateFeedbackEnabled = { preferences.value.immediateExamFeedbackEnabled }
                    exam.render(node("main", "exam-page").also(content::appendChild))
                }
                WebRoute.Settings -> renderSettingsWeb(node("main", "settings-page").also(content::appendChild), preferences, store, returnTo, navigate)
            }
            content.appendChild(node("footer", text = ROUTE_FOOTER_TEXT))
        }

        // Only a routine same-route rebuild (timer, rating, IME…) ever restores the element that
        // was already focused before it — a real route change always moves focus to the new
        // screen's heading instead (`onMounted` below), regardless of what was focused on the OLD
        // screen (which is very often a still-live `nav-<slug>` button: nav buttons are never
        // recreated, so a naive `getElementById(focusId)` would keep re-focusing the tab bar).
        fun restorePreviousFocus() {
            val restoredFocus = if (focusId.isNotEmpty()) document.getElementById(focusId) as? HTMLElement else null
            if (restoredFocus == null) return
            restoredFocus.focus()
            if (restoredFocus is HTMLTextAreaElement && focusedSelection != null) {
                val (start, end, scrollTop) = focusedSelection
                val length = restoredFocus.value.length
                restoredFocus.selectionStart = start?.coerceAtMost(length)
                restoredFocus.selectionEnd = end?.coerceAtMost(length)
                restoredFocus.scrollTop = scrollTop
            }
            if (restoredFocus is HTMLInputElement &&
                restoredFocus.type in setOf("text", "search", "tel", "url", "password") && focusedSelection != null
            ) {
                val (start, end, scrollTop) = focusedSelection
                val length = restoredFocus.value.length
                restoredFocus.selectionStart = start?.coerceAtMost(length)
                restoredFocus.selectionEnd = end?.coerceAtMost(length)
                restoredFocus.scrollTop = scrollTop
            }
        }

        // UX5, RED (see FlipCardRivePlan.md §19's evidence log): `previous`/`previousRoute`/…
        // must update the INSTANT this render() call commits to a route change, not once the
        // slide's animation has actually finished settling. `slider.start` defers the real DOM
        // work by a macrotask plus two rAFs plus the full transition duration — if these fields
        // waited for that (as they harmlessly could when a View Transition's own callback ran
        // near-synchronously), any OTHER render() call arriving in that whole window (e.g. a
        // Settings toggle fired right after navigating to Settings) still saw the OLD route in
        // `previousRoute`, read itself as ANOTHER route change, and started a second slide
        // stacking a second `.route-content` on top of the first one's still-in-flight incoming
        // layer — leaving two of everything (e.g. two `#settings-return` buttons) forever, since
        // nothing ever again finishes what became an orphaned first slide.
        fun commitRouteBookkeeping() {
            previous = state
            previousRoute = route
            previousPreferences = preferences.value
            previousStatus = preferences.status
        }

        fun finalizeContent(content: HTMLElement) {
            for ((key, position) in scrollPositions) {
                val scrolled = content.querySelector("[data-scroll-key='$key']") as? HTMLElement ?: continue
                scrolled.scrollLeft = position.first
                scrolled.scrollTop = position.second
            }
            if (route == WebRoute.Training && old?.phase == CardPhase.Question && state.phase == CardPhase.Revealed) {
                val feedback = content.querySelector(".change-list h3") as? HTMLElement
                if (feedback != null) {
                    val clearance = nav.shell.getBoundingClientRect().top - feedback.getBoundingClientRect().bottom
                    if (clearance < 12.0) root.scrollTop += 12.0 - clearance
                }
            }
        }

        if (routeChanged) {
            // UX5: which way the two layers slide — forward along the nav order into the new tab,
            // backward out of it. Relies on WebRoute's declared order matching the nav's
            // left-to-right order (same assumption the old View Transition direction used).
            val direction = if (route.ordinal >= routeChangedFrom.ordinal) 1 else -1
            commitRouteBookkeeping() // see the comment above — must happen before slider.start, not in onSettled
            root.scrollTop = 0.0 // P1-9: a route change starts its new page at the top.
            slider.start(
                viewport,
                buildIncoming = { node("div", "route-content").also(::populate) },
                direction = direction,
                instant = motionInstantActive(),
                onMounted = { incoming -> focusRouteHeading(incoming) },
                onSettled = ::finalizeContent,
            )
        } else {
            if (slider.isPending()) {
                // RED (see RouteSlider.isPending/refresh's own doc comments): a route change's
                // slide toward THIS same route is still deferred/animating. This call might be
                // Compose's own routine post-navigation echo, or it might be a real, later
                // interaction (a rating, a Matrix sub-section, a Settings toggle) that must still
                // land — `refresh` updates whichever of the two the slide currently has (the not-
                // yet-built closure, or the already-mounted incoming layer's content in place)
                // without touching its animation. Scroll/focus restoration is meaningless here —
                // there is no settled content to scroll or focus yet; the slide's own `onMounted`/
                // `onSettled` (already scheduled) still runs once it actually gets there.
                slider.refresh {
                    // RED: this is NOT a rare edge case on the web target — Compose's
                    // `collectAsState()` delivers the store's post-`SelectTab` state (with
                    // `matrixSelection` freshly reset) one recomposition AFTER the route change
                    // itself lands, so entering Matrix always re-enters `render()` a second time
                    // while this exact slide is still pending, landing right here. Whether
                    // RouteSlider merges this fresh node's children into an already-live incoming
                    // or swaps it in directly (see `refresh`'s own doc comment) is entirely its own
                    // concern now — this closure is the same plain builder every other route uses,
                    // with nothing of its own left to keep in sync with which branch runs.
                    node("div", "route-content").also(::populate)
                }
                previous = state
                previousPreferences = preferences.value
                previousStatus = preferences.status
            } else {
                val content = (viewport.querySelector(":scope > .route-content") as? HTMLElement)
                    ?: node("div", "route-content").also(viewport::appendChild)
                content.textContent = ""
                populate(content)
                root.scrollTop = scroll
                restorePreviousFocus()
                finalizeContent(content)
                commitRouteBookkeeping()
            }
        }
        if (old == null && route != WebRoute.Matrix) scheduleMatrixPrewarm(state, dispatch)
    }

    private fun renderHeader(app: HTMLElement, state: AppUiState) {
        val header = node("header", "top")
        app.appendChild(header)
        header.appendChild(node("div").apply {
            appendChild(node("h1", text = "POLSKI Grammar Matrix"))
            appendChild(node("p", text = "Предложение → преобразование → новое предложение"))
        })
        header.appendChild(node("div", "topstats").apply {
            appendChild(stat(state.dueCount.toString(), "к повторению"))
            appendChild(stat(state.todayCount.toString(), "сегодня"))
        })
    }

    private fun stat(value: String, label: String): HTMLElement = node("div").apply {
        appendChild(node("b", text = value))
        appendChild(node("span", text = label))
    }


}
