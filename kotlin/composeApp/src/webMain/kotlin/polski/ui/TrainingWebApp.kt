package polski.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.HtmlElementView
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import kotlin.random.Random
import kotlin.time.Clock
import polski.data.activeCoursePackId
import polski.data.availableCoursePacks
import polski.platform.BrowserLocalDayProvider
import polski.platform.browserFormatDate
import polski.platform.WebProgressRepository
import polski.platform.WebAppearance
import polski.preferences.PreferredAnswerMode
import polski.preferences.PreferredStyle
import polski.presentation.*
import polski.srs.FsrsScheduler
import polski.srs.Rating
import polski.training.PlExerciseEngine
import polski.core.engine.ExerciseIdFactory
import polski.core.engine.RandomSource

/** One browser session, one DOM host and one listener/timer set per Compose mount. */
@Composable
@OptIn(ExperimentalComposeUiApi::class, kotlin.js.ExperimentalWasmJsInterop::class)
fun TrainingWebApp() {
    val scope = rememberCoroutineScope()
    val preferences = remember { WebPreferencesController() }
    val routes = remember { WebRouteController() }
    val appearance = remember { WebAppearance() }
    var route by remember { mutableStateOf(routes.current) }
    // EnRuAcceptance-2026-09-28.md §7 item 3: keyed on the active pack's pairId so a Settings pack
    // switch (`WebPreferencesController.setCourse`) rebuilds a fresh `PlExerciseEngine`/store bound
    // to the newly active pack on the very next recomposition — the same "rebuild the training
    // bridge on pack switch" fix ADR-37 already applied to macOS/iOS, done here the idiomatic
    // Compose way instead of a manual dirty-check on every dispatch.
    val store = remember(activeCoursePackId) {
        val scheduler = FsrsScheduler()
        var nextId = 0L
        TrainingStore(
            WebProgressRepository(scheduler), scheduler,
            PlExerciseEngine(RandomSource { Random.nextDouble() }, ExerciseIdFactory { "web-${++nextId}" }),
            TimeSource {
                val at = Clock.System.now()
                TimeCapture(at, BrowserLocalDayProvider().localDay(at))
            },
            scope,
            StyleId(preferences.value.styleId.name),
            if (preferences.value.answerMode == PreferredAnswerMode.Typed) AnswerMode.Typed else AnswerMode.Oral,
        )
    }
    val state by store.state.collectAsState()
    val attemptedEffects = remember { mutableSetOf<Long>() }
    // Deliberately NOT keyed on activeCoursePackId (unlike [store]): this class owns a long-lived,
    // hand-rolled DOM diff (`previous`/`previousRoute`/RouteSlider) directly against `root`'s real
    // children — rebuilding it mid-session would reset that diff state while the actual DOM stayed
    // exactly as the old instance left it, so the next render's "first paint" branch would populate
    // a second copy into it instead of replacing the first. Its own [VocabularyWebController]
    // instead self-heals on pack switch (see its own reload-on-mismatch check).
    val renderer = remember { TrainingDomRenderer() }

    DisposableEffect(renderer) { onDispose { renderer.close() } }

    LaunchedEffect(store) { store.start() }
    LaunchedEffect(state.phase, state.exerciseId, preferences.value.answerMode) { preferences.applyPendingAnswerMode(store) }
    // v3/D: warm the Rive runtime once the first frame has committed, not on the critical path of
    // the first reveal/flip; re-runs (cheaply, idempotently) whenever Animations is toggled, so
    // turning it on later still prewarms lazily at that point.
    LaunchedEffect(preferences.value.animationsEnabled) { prewarmRiveIfEnabled(preferences.value.animationsEnabled) }
    DisposableEffect(routes, appearance) {
        routes.start { destination ->
            route = destination
            destination.tab?.let { store.dispatch(AppAction.SelectTab(it)) }
        }
        routes.current.tab?.let { store.dispatch(AppAction.SelectTab(it)) }
        appearance.start()
        onDispose { routes.close(); appearance.close() }
    }
    appearance.update(preferences.value.appearance, preferences.value.motion, preferences.value.animationsEnabled)
    DisposableEffect(store) {
        val compositionStart: (Event) -> Unit = { renderer.composing = true }
        val compositionEnd: (Event) -> Unit = {
            renderer.composing = false
            store.dispatch(AppAction.RefreshTime)
        }
        val refresh: (Event) -> Unit = { store.dispatch(AppAction.RefreshTime) }
        // UX4-14: ArrowLeft/ArrowRight rate on both the training and vocabulary cards, alongside
        // the existing "1"/"2" — this single handler (with its already-solved composing/IME/
        // repeat/modifier/editableTarget guard) now branches on `route` instead of only ever
        // firing for Training, rather than growing a second copy of that guard for Vocabulary.
        val keyboard: (Event) -> Unit = keyboard@{ raw ->
            val event = raw as? KeyboardEvent ?: return@keyboard
            val current = store.state.value
            if (renderer.composing || event.isComposing || event.repeat || event.altKey || event.ctrlKey || event.metaKey ||
                current.loadStatus != LoadStatus.Ready || editableTarget(event.target as? Element)
            ) return@keyboard
            when (route) {
                WebRoute.Training -> {
                    if (current.tab != AppTab.Training || current.phase !in setOf(CardPhase.Question, CardPhase.Revealed)) return@keyboard
                    val id = current.exerciseId ?: return@keyboard
                    if (event.code == "Space" && current.phase == CardPhase.Question) {
                        event.preventDefault()
                        store.dispatch(if (current.introPending) AppAction.ContinueIntroduction else AppAction.Reveal(id))
                    } else if (current.phase == CardPhase.Revealed) {
                        val rating = when (event.key) {
                            "1", "ArrowLeft" -> Rating.Again
                            "2", "ArrowRight" -> Rating.Good
                            else -> null
                        }
                        if (rating != null) {
                            event.preventDefault()
                            store.dispatch(AppAction.Rate(id, rating))
                        }
                    }
                }
                WebRoute.Vocabulary -> {
                    // UX5: Space OR Enter reveals (once) or flips back and forth, exactly like a
                    // click on the card now does — the vocabulary card no longer has its own
                    // "Показать ответ" <button>, so this is how a keyboard user activates the
                    // focused role="button" prompt block instead (the browser does not auto-wire
                    // Enter/Space activation for a custom role — the ARIA button pattern requires
                    // both keys to be handled by the page, matching native <button> behavior).
                    // Real <button>/<a>/etc. targets never reach here (`editableTarget` above).
                    if (event.code == "Space" || event.key == "Enter") {
                        event.preventDefault()
                        renderer.spaceVocabulary { store.dispatch(AppAction.RefreshTime) }
                        return@keyboard
                    }
                    val rating = when (event.key) {
                        "1", "ArrowLeft" -> Rating.Again
                        "2", "ArrowRight" -> Rating.Good
                        else -> null
                    } ?: return@keyboard
                    event.preventDefault()
                    renderer.rateVocabularyIfRevealed(rating) { store.dispatch(AppAction.RefreshTime) }
                }
                else -> return@keyboard
            }
        }
        document.addEventListener("compositionstart", compositionStart)
        document.addEventListener("compositionend", compositionEnd)
        document.addEventListener("visibilitychange", refresh)
        window.addEventListener("focus", refresh)
        window.addEventListener("keydown", keyboard)
        val timer = window.setInterval({ store.dispatch(AppAction.RefreshTime); null }, 30_000)
        onDispose {
            window.clearInterval(timer)
            document.removeEventListener("compositionstart", compositionStart)
            document.removeEventListener("compositionend", compositionEnd)
            document.removeEventListener("visibilitychange", refresh)
            window.removeEventListener("focus", refresh)
            window.removeEventListener("keydown", keyboard)
            store.close()
        }
    }

    HtmlElementView(
        factory = {
            (document.createElement("div") as HTMLDivElement).apply {
                className = "training-web-host"
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { root ->
            val dispatch: (AppAction) -> Unit = { action ->
                when (action) {
                    is AppAction.SelectTab -> routes.navigate(WebRoute.forTab(action.tab))
                    is AppAction.SetStyle -> preferences.setStyle(action.styleId, store)
                    is AppAction.SetAnswerMode -> preferences.setAnswerMode(action.mode, store)
                    else -> {
                        store.dispatch(action)
                        if (action is AppAction.ChooseSkill) routes.navigate(WebRoute.Training)
                    }
                }
            }
            renderer.render(root, state, route, routes.returnTo, preferences, store, { routes.navigate(it) }, dispatch)
            for (effect in state.pendingEffects) {
                if (attemptedEffects.add(effect.id)) {
                    if (!executeEffect(root, state, effect, store, routes::navigate)) attemptedEffects.remove(effect.id)
                }
            }
            // Bound the set to still-pending ids; acknowledged/removed effects must not accumulate forever.
            attemptedEffects.retainAll(state.pendingEffects.mapTo(mutableSetOf()) { it.id })
        },
    )
}

// Uses closest(), not just the exact target's own tag: a click on a <button> often lands on one
// of its child nodes (e.g. the rating buttons' <small>/<span> hint text), and callers here rely on
// interactive ANCESTORS being excluded too (e.g. the card-flip tap handler must never toggle while
// a rating button is mid-click).
internal fun editableTarget(target: Element?): Boolean =
    target?.closest("input, textarea, select, button, a, [contenteditable]") != null

/**
 * UX5: moves focus to the freshly built route's own heading (its first `h1`/`h2`, wherever it is
 * nested — every route has exactly one) so a screen reader announces the new screen right when it
 * appears, not the tab bar it was clicked from. A route with no heading of its own (Training, whose
 * content starts directly with its mode toolbar) falls back to the route container itself — still
 * a real, announced focus move, just without a heading to name it. At the moment this runs the
 * target may still be mid-slide (translated off-screen, see [RouteSlider]) — `transform` never
 * moves an element's own layout box, only its paint position, so this never needs to scroll the
 * PAGE to bring the target's box into view. It can still scroll the target's own box INTO the
 * page's current viewport, though (reviewer note, §19.7): the grid-stack the two sliding layers
 * share (training.css) auto-sizes to whichever layer is taller, and a still-mid-slide `incoming`
 * heading can therefore land below the fold for one frame — `focus()`'s default scroll-into-view
 * would jump the page under the still-animating slide to chase it. `preventScroll: true` heads
 * that off; it isn't in either binding this project has for `focus()` (kotlinx-browser and
 * kotlin-dom-api-compat both omit `FocusOptions`), so [focusPreventScroll] reaches it through the
 * one `js()` snippet both the `js()` and `wasmJs()` targets compile identically.
 */
internal fun focusRouteHeading(content: HTMLElement) {
    val target = (content.querySelector("h1, h2") as? HTMLElement) ?: content
    if (!target.hasAttribute("tabindex")) target.setAttribute("tabindex", "-1")
    focusPreventScroll(target)
}

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun focusPreventScroll(element: HTMLElement): Unit = js("element.focus({ preventScroll: true })")

private fun executeEffect(root: HTMLElement, state: AppUiState, effect: UiEffect, store: TrainingStore,
                          navigate: (WebRoute) -> Unit): Boolean {
    when (effect) {
        is UiEffect.FocusReveal -> {
            if (state.exerciseId == effect.exerciseId && state.phase == CardPhase.Question && state.tab == AppTab.Training) {
                if (state.introPending) return false
                val reveal = root.querySelector("#training-reveal") as? HTMLElement ?: return false
                reveal.focus()
                store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Completed))
            } else store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Skipped))
        }
        is UiEffect.ConfirmReset -> {
            val previousRevision = store.state.value.revision
            val confirmed = window.confirm(effect.prompt)
            store.dispatch(AppAction.ResetDecision(effect.id, confirmed))
            if (confirmed && store.state.value.revision > previousRevision && store.state.value.tab == AppTab.Training) {
                navigate(WebRoute.Training)
            }
        }
        is UiEffect.DownloadJson -> {
            try {
                val anchor = document.createElement("a") as HTMLAnchorElement
                anchor.href = "data:application/json;charset=utf-8,${percentEncode(effect.json)}"
                anchor.download = effect.filename
                document.body?.appendChild(anchor)
                anchor.click()
                anchor.remove()
                store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Completed))
            } catch (error: Throwable) {
                store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Failed(error.message ?: "Экспорт не удался")))
            }
        }
    }
    return true
}

private fun percentEncode(value: String): String {
    val digits = "0123456789ABCDEF"
    return buildString {
        for (byte in value.encodeToByteArray()) {
            val unsigned = byte.toInt() and 255
            append('%')
            append(digits[unsigned shr 4])
            append(digits[unsigned and 15])
        }
    }
}

private const val ROUTE_FOOTER_TEXT = "Прогресс сохраняется в этом браузере. Интервальные повторения — FSRS."

/** EnRuAcceptance-2026-09-28.md §7 item 3/4: the active pack's own target-language `lang` attribute
 *  code and Russian adverb ("по-английски" for en, matching pl's already-shipped "по-польски") —
 *  every place this file used to hardcode `"pl"`/"по-польски" regardless of the active pack. */
private fun activeTargetLangCode(): String = availableCoursePacks.firstOrNull { it.pairId == activeCoursePackId }?.target ?: "pl"
private val targetAdverbs: Map<String, String> = mapOf("pl" to "по-польски", "en" to "по-английски")
private fun activeTargetAdverb(): String = targetAdverbs[activeTargetLangCode()] ?: activeTargetLangCode()

/** UX5 perf: delay before the idle Matrix prewarm runs (see [TrainingDomRenderer.scheduleMatrixPrewarm]).
 *  There is no `requestIdleCallback` binding in either DOM binding this project has (kotlinx-browser/
 *  kotlin-dom-api-compat), so a plain timer stands in for "idle enough" — long enough to sit well
 *  after the very first paint/layout it must never compete with, short enough to still land well
 *  before a real first click could plausibly happen. */
private const val MATRIX_PREWARM_DELAY_MS = 1500

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private class TrainingDomRenderer {
    private var previous: AppUiState? = null
    private var previousRoute: WebRoute? = null
    private var previousPreferences: polski.preferences.UserPreferencesV2? = null
    private var previousStatus: String? = null
    var composing = false
    private val vocabulary = VocabularyWebController()
    private var navigation: WebNav? = null
    private val riveOverlay = RiveEffectOverlay()
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

    fun close() { navigation?.close(); navigation = null; vocabulary.close() }

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
    private fun scheduleMatrixPrewarm(state: AppUiState, dispatch: (AppAction) -> Unit) {
        if (matrixPrewarmScheduled) return
        matrixPrewarmScheduled = true
        window.setTimeout({
            val content = node("main", "matrix-page")
            renderMatrixWeb(content, state, dispatch)
            content.style.setProperty("position", "absolute")
            content.style.setProperty("visibility", "hidden")
            content.style.setProperty("pointer-events", "none")
            document.body?.appendChild(content)
            content.getBoundingClientRect() // forces the one-time layout/style pass early
            content.remove()
            null
        }, MATRIX_PREWARM_DELAY_MS)
    }

    fun render(root: HTMLElement, state: AppUiState, route: WebRoute, returnTo: WebRoute, preferences: WebPreferencesController, store: TrainingStore, navigate: (WebRoute) -> Unit, dispatch: (AppAction) -> Unit) {
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
        if (composing && old != null && previousRoute == route && previousPreferences == preferences.value && old.exerciseId == state.exerciseId && old.phase == CardPhase.Question) {
            previous = state
            return // A timer/visibility refresh must not replace an active IME editor.
        }
        val focusId = (document.activeElement as? HTMLElement)?.id.orEmpty()
        // A rebuild always recreates the focused element, so caret and selection would otherwise
        // reset to the end of the text every time (e.g. the periodic RefreshTime timer).
        val focusedTextArea = document.activeElement as? HTMLTextAreaElement
        val focusedSelection = focusedTextArea?.let { Triple(it.selectionStart, it.selectionEnd, it.scrollTop) }
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
            when (route) {
                WebRoute.Training -> renderTraining(root, content, state, preferences.value.swipeRatingEnabled, enteringChainComplete, justRevealed, justShowedReference, dispatch)
                WebRoute.Vocabulary -> vocabulary.render(node("main", "vocabulary-page").also(content::appendChild), root, preferences.value.swipeRatingEnabled) {
                    previous = null
                    render(root, state, route, returnTo, preferences, store, navigate, dispatch)
                }
                WebRoute.Matrix -> renderMatrixWeb(node("main", "matrix-page").also(content::appendChild), state, dispatch)
                WebRoute.Progress -> renderProgressWeb(node("main", "progress-page").also(content::appendChild), state, dispatch)
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

    private fun renderTraining(root: HTMLElement, app: HTMLElement, state: AppUiState, swipeRatingEnabled: Boolean, enteringChainComplete: Boolean, justRevealed: Boolean, justShowedReference: Boolean, dispatch: (AppAction) -> Unit) {
        val main = node("main", "study-page")
        app.appendChild(main)
        if (state.loadStatus != LoadStatus.Ready) {
            renderLoadStatus(main, state, dispatch)
            return
        }
        val toolbar = node("div", "study-toolbar")
        main.appendChild(toolbar)
        val modes = node("div", "subnav")
        modes.setAttribute("aria-label", "Режим тренировки")
        toolbar.appendChild(modes)
        modes.appendChild(button("Цепочка предложений", active = state.mode == TrainingMode.Chain, pressed = state.mode == TrainingMode.Chain) { dispatch(AppAction.StartChain()) })
        modes.appendChild(button("По расписанию · ${state.dueCount}", active = state.mode == TrainingMode.Schedule, pressed = state.mode == TrainingMode.Schedule) { dispatch(AppAction.StartSchedule) })
        modes.appendChild(button("Отдельный навык", active = state.mode == TrainingMode.Focused, pressed = state.mode == TrainingMode.Focused) { dispatch(AppAction.OpenSkillPicker) }.apply {
            setAttribute("aria-expanded", state.showSkillPicker.toString())
        })
        val options = node("div", "study-options")
        toolbar.appendChild(options)
        val methodLabel = node("label", text = "Подача")
        options.appendChild(methodLabel)
        val methodSelect = document.createElement("select") as HTMLSelectElement
        methodSelect.id = "explanation-method"
        methodSelect.setAttribute("aria-label", "Подача объяснений")
        builtInStyleIds.forEach { styleId ->
            methodSelect.appendChild(node("option", text = styleLabel(styleId)).apply { setAttribute("value", styleId.value) })
        }
        methodSelect.value = state.styleId.value
        // UC-10: a compact quick switch over the same 4 styles as Settings — dispatching SetStyle
        // is a display choice only (TrainingStore.SetStyle), never creates a review and never
        // touches draft/frozenAnswer/exercise/phase.
        methodSelect.addEventListener("change", {
            dispatch(AppAction.SetStyle(StyleId(methodSelect.value)))
        })
        methodLabel.appendChild(methodSelect)
        options.appendChild(button(if (state.showReference && !state.introPending) "Скрыть таблицу" else "Таблица под рукой") {
            dispatch(AppAction.ToggleReference)
        }.apply {
            disabled = state.introPending
            setAttribute("aria-expanded", (state.showReference && !state.introPending).toString())
        })
        if (state.showSkillPicker) renderSkillPicker(main, state, dispatch)
        if (state.mode == TrainingMode.Chain) renderChainHeader(main, state, dispatch)
        val layout = node("div", if (state.showReference && !state.introPending) "study-layout with-reference" else "study-layout")
        main.appendChild(layout)
        val card = node("section", "flashcard card")
        card.setAttribute("aria-label", "Учебная карточка")
        layout.appendChild(card)
        when (state.phase) {
            CardPhase.ChainComplete -> renderChainComplete(root, card, state, dispatch, enteringChainComplete)
            CardPhase.NoDue -> renderNoDue(card, state, dispatch)
            CardPhase.Question, CardPhase.Revealed -> renderCard(root, card, state, swipeRatingEnabled, justRevealed, dispatch)
        }
        if (state.showReference && !state.introPending) {
            // UX4-20/22: animates in (fade + slight rise) exactly on the render where it just
            // turned on; a routine rebuild of an already-shown panel (timer, rating, …) must not
            // replay it. Hiding stays instant, matching this panel's existing conditional-mount
            // design — see TrainingWebApp.kt's own doc comment on `justShowedReference`.
            val reference = node("aside", "card reference-panel" + if (justShowedReference && !motionInstantActive()) " panel-reveal" else "")
            layout.appendChild(reference)
            renderCaseReferenceWeb(reference, state, dispatch)
        }
        // W4/U3: a coarse (touch/stylus) pointer has no physical keyboard, so the hint must name
        // swipe instead of Space/arrows/digits — checked live, not just at narrow widths, per
        // UX4-11's own precedent one screen up (a resizable fine-pointer desktop window can be
        // just as narrow as a phone).
        val action = if (state.introPending) "перейти к заданию" else "показать ответ"
        val help = if (window.matchMedia("(pointer: coarse)").matches) "Коснитесь — $action · Свайп влево/вправо — оценить"
        else "Пробел — $action · ← → или 1–2 — оценить"
        main.appendChild(node("p", "study-help", help))
    }

    private fun renderLoadStatus(main: HTMLElement, state: AppUiState, dispatch: (AppAction) -> Unit) {
        val section = node("section", "card session-complete")
        main.appendChild(section)
        val title = when (state.loadStatus) {
            LoadStatus.Loading -> "Загружаем прогресс"
            LoadStatus.MigrationAvailable -> "Найден прежний прогресс"
            LoadStatus.RecoveryRequired -> "Нужна копия прогресса"
            LoadStatus.Unavailable -> "Хранилище недоступно"
            LoadStatus.Ready -> return
        }
        section.appendChild(node("h2", text = title))
        if (state.loadStatus == LoadStatus.MigrationAvailable) {
            section.appendChild(node("p", text = "Экспортируй исходный JSON или перенеси прогресс в эту версию."))
            section.appendChild(button("Перенести прогресс") { dispatch(AppAction.RequestMigration) })
        }
        if (state.loadStatus == LoadStatus.RecoveryRequired) {
            section.appendChild(node("p", text = "Сохрани исходный JSON перед повторной попыткой. Неподходящие данные не будут перезаписаны."))
            section.appendChild(button("Повторить перенос") { dispatch(AppAction.RequestMigration) })
        }
        if (state.loadStatus == LoadStatus.MigrationAvailable || state.loadStatus == LoadStatus.RecoveryRequired) {
            section.appendChild(button("Экспорт JSON") { dispatch(AppAction.RequestExport) })
        }
    }

    private fun renderSkillPicker(main: HTMLElement, state: AppUiState, dispatch: (AppAction) -> Unit) {
        val picker = node("section", "skill-picker")
        picker.setAttribute("aria-label", "Выбор навыка")
        main.appendChild(picker)
        polski.data.skills.forEach { skill ->
            picker.appendChild(button("${skill.title} · ${skill.level}", active = state.focusedSkillId == skill.id) {
                dispatch(AppAction.ChooseSkill(skill.id))
            })
        }
    }

    private fun renderChainHeader(main: HTMLElement, state: AppUiState, dispatch: (AppAction) -> Unit) {
        val header = node("div", "chain-header")
        main.appendChild(header)
        val label = node("label", text = "Один набор слов")
        header.appendChild(label)
        val select = document.createElement("select") as HTMLSelectElement
        select.id = "training-seed"
        select.setAttribute("aria-label", "Слова для цепочки")
        polski.training.sentenceSeeds.forEachIndexed { index, seed ->
            val option = document.createElement("option") as org.w3c.dom.HTMLOptionElement
            option.value = index.toString()
            option.textContent = polski.data.nounLabel(seed.nounId)
            select.appendChild(option)
        }
        select.value = state.seedIndex.toString()
        select.addEventListener("change", { dispatch(AppAction.SelectChainSeed(select.value.toInt())) })
        label.appendChild(select)
        val steps = node("ol")
        steps.setAttribute("aria-label", "Шаги цепочки")
        header.appendChild(steps)
        polski.data.courseChainPresentation.steps.forEachIndexed { index, step ->
            val item = node("li", text = "${index + 1} ${step.label}")
            item.className = when {
                state.chainComplete || index < state.chainIndex -> "done"
                index == state.chainIndex -> "current"
                else -> ""
            }
            if (!state.chainComplete && index == state.chainIndex) item.setAttribute("aria-current", "step")
            steps.appendChild(item)
        }
    }

    private fun renderChainComplete(root: HTMLElement, card: HTMLElement, state: AppUiState, dispatch: (AppAction) -> Unit, justCompleted: Boolean) {
        val complete = node("div", "session-complete")
        card.appendChild(complete)
        complete.appendChild(node("h2", text = polski.data.courseChainPresentation.completion.title))
        complete.appendChild(node("p", text = polski.data.courseChainPresentation.completion.webBody))
        val review = node("div", "chain-review")
        complete.appendChild(review)
        state.chain.forEachIndexed { index, exercise ->
            review.appendChild(node("div").apply {
                appendChild(node("small", text = "${index + 1} · ${polski.data.skillById(exercise.primarySkill).title}"))
                appendChild(node("p", text = exercise.expected).apply { setAttribute("lang", activeTargetLangCode()) })
            })
        }
        complete.appendChild(node("div", "actions").apply {
            appendChild(button("Следующий набор слов", primary = true) {
                dispatch(AppAction.StartChain((state.seedIndex + 1) % polski.training.sentenceSeeds.size))
            })
            appendChild(button("К повторениям по расписанию") { dispatch(AppAction.StartSchedule) })
        })
        // v2 FC2-10 (R3): a one-shot "Tada" celebration exactly when the chain finishes, never
        // replayed on a routine re-render while this screen stays on view (e.g. the 30s timer).
        if (justCompleted) riveOverlay.triggerChainComplete(root, complete)
    }

    private fun renderNoDue(card: HTMLElement, state: AppUiState, dispatch: (AppAction) -> Unit) {
        card.appendChild(node("div", "session-complete").apply {
            appendChild(node("h2", text = "Повторения на сейчас завершены"))
            appendChild(node("p", text = state.nextDue?.let {
                "Следующее: ${browserFormatDate(it.toEpochMilliseconds().toDouble())}"
            } ?: "Новых повторений пока нет."))
            appendChild(button("Потренировать цепочку") { dispatch(AppAction.StartChain()) })
        })
    }

    private fun renderCard(root: HTMLElement, card: HTMLElement, state: AppUiState, swipeRatingEnabled: Boolean, justRevealed: Boolean, dispatch: (AppAction) -> Unit) {
        val exercise = state.exercise ?: return
        val skill = polski.data.skillById(exercise.primarySkill)
        val presentation = polski.data.presentationBySkillId(exercise.primarySkill)
        val method = if (state.styleId == StyleId.SituationFirst) presentation.situations else presentation.logic
        card.appendChild(node("div", "card-meta").apply {
            appendChild(node("span", text = when (state.mode) {
                TrainingMode.Chain -> "Цепочка · ${state.chainIndex + 1} / ${state.chain.size}"
                TrainingMode.Schedule -> "Повторение по расписанию"
                TrainingMode.Focused -> "Тренировка навыка"
            }))
            appendChild(node("span", text = "${skill.level} · ${skill.title}"))
        })
        if (state.introPending && state.phase == CardPhase.Question) {
            card.appendChild(node("div", "card-front method-introduce").apply {
                appendChild(node("span", "eyebrow", if (state.styleId == StyleId.SituationFirst) "Сцена и намерение" else "Признаки и операция"))
                renderLifehackBadge(this, skill.id)
                appendChild(node("p", "source-sentence").apply {
                    setAttribute("lang", activeTargetLangCode())
                    appendContrastParts(this, sentenceHighlightParts(exercise.source, exercise.changes, ChangeSide.Before), "change-before")
                })
                appendChild(node("p", text = method.introduce))
                appendChild(button("Перейти к заданию", primary = true) {
                    dispatch(AppAction.ContinueIntroduction)
                })
            })
            return
        }
        val (effective, content) = resolveEffectiveStyleAndContent(state, skill.id)
        val frontBlocks = StyleComposer.compose(effective, StylePhase.Front, exercise, skill, presentation, content)
        val front = node("div", "card-front").apply {
            appendChild(node("span", "eyebrow", "Исходное предложение"))
            renderLifehackBadge(this, skill.id)
            appendChild(node("p", "source-sentence").apply {
                setAttribute("lang", activeTargetLangCode())
                appendContrastParts(this, sentenceHighlightParts(exercise.source, exercise.changes, ChangeSide.Before), "change-before")
            })
            appendChild(node("div", "operation").apply {
                appendChild(node("span", text = if (state.styleId == StyleId.SituationFirst) "Ситуация" else "Преобразуй"))
                appendChild(node("h2", text = exercise.prompt))
                appendChild(node("p", "method-retrieve", method.retrieve))
                appendChild(node("small", "method-lead", method.promptLead))
            })
            // UC-10 web S2: which blocks show here (and in which order) comes entirely from the
            // resolved style's recipe — a style switch changes this list, never a hardcoded branch.
            renderCardBlocks(this, frontBlocks, "card-blocks-front", activeTargetLangCode())
        }
        if (state.phase == CardPhase.Question) {
            card.appendChild(front)
            // v3/A: clicking anywhere on the question card is a reveal trigger too, alongside the
            // existing button/Space (see the study-help hint below and the global keydown
            // handler) — excluding interactive descendants (the reveal button itself, the typed-
            // answer textarea/mode buttons) via the same tap-vs-drag gesture the vocabulary flip
            // uses, so a text-selection drag on the source sentence never fires it.
            if (!state.introPending) installTapGesture(front) { state.exerciseId?.let { dispatch(AppAction.Reveal(it)) } }
            renderAnswerArea(card, state, dispatch)
            return
        }
        // v3/A: the answer expands downward below the still-visible question — no flip. The wrap
        // is only ever created once phase == Revealed, so the answer is never in the DOM (let
        // alone the accessibility tree) before that; `applyExpand` handles the one-shot animation
        // exactly at the reveal moment.
        card.appendChild(front)
        val wrap = node("div", "card-answer-wrap")
        card.appendChild(wrap)
        val back = node("div", "card-back")
        back.setAttribute("aria-live", "polite")
        wrap.appendChild(back)
        renderAnswerBack(root, card, wrap, back, state, swipeRatingEnabled, dispatch)
        applyExpand(wrap, isRevealEvent = justRevealed)
    }

    /**
     * Expands [wrap]'s grid row from 0fr to 1fr (the standard trick for animating to/from an
     * intrinsic "auto" height — a plain `height` transition can't do it) when [isRevealEvent] is
     * true and motion isn't instant; otherwise jumps straight to expanded — a routine DOM rebuild
     * of an already-revealed card (e.g. the 30s refresh timer) must never replay the reveal.
     * `will-change` in `training.css` promotes the compositor layer ahead of time (v3/D); the
     * forced layout read below is the same FC2-01 fix ported from the flip: a freshly created
     * node has no previously-painted frame to transition from, so without it the very first
     * reveal on a page would snap instead of animating even though every LATER one already did.
     */
    private fun applyExpand(wrap: HTMLElement, isRevealEvent: Boolean) {
        wrap.classList.add("expanded")
        if (!isRevealEvent || motionInstantActive()) {
            wrap.style.setProperty("transition", "none")
            wrap.style.setProperty("grid-template-rows", "1fr")
            return
        }
        wrap.classList.add("revealing") // gates the CSS stagger fade-in for this render only
        wrap.style.setProperty("transition", "none")
        wrap.style.setProperty("grid-template-rows", "0fr")
        wrap.getBoundingClientRect() // force layout: commits the collapsed frame before animating
        wrap.style.setProperty("transition", "")
        wrap.style.setProperty("grid-template-rows", "")
    }

    private fun renderAnswerArea(card: HTMLElement, state: AppUiState, dispatch: (AppAction) -> Unit) {
        val area = node("div", "answer-area")
        card.appendChild(area)
        val modes = node("div", "answer-mode")
        modes.setAttribute("aria-label", "Как отвечать")
        area.appendChild(modes)
        modes.appendChild(button("Ответ вслух / про себя", active = state.answerMode == AnswerMode.Oral, pressed = state.answerMode == AnswerMode.Oral) {
            dispatch(AppAction.SetAnswerMode(AnswerMode.Oral))
        })
        modes.appendChild(button("Напечатать ответ", active = state.answerMode == AnswerMode.Typed, pressed = state.answerMode == AnswerMode.Typed) {
            dispatch(AppAction.SetAnswerMode(AnswerMode.Typed))
        })
        if (state.answerMode == AnswerMode.Typed) {
            val input = document.createElement("textarea") as HTMLTextAreaElement
            input.id = "training-answer"
            input.setAttribute("aria-label", "Ответ ${activeTargetAdverb()}")
            input.placeholder = "Напиши целое предложение…"
            input.value = state.draft
            input.addEventListener("input", { dispatch(AppAction.EditAnswer(input.value)) })
            input.addEventListener("keydown", { raw ->
                val event = raw as KeyboardEvent
                if (event.key == "Enter" && !event.shiftKey && !event.isComposing && !composing) {
                    event.preventDefault()
                    state.exerciseId?.let { dispatch(AppAction.Reveal(it)) }
                }
            })
            area.appendChild(input)
        } else area.appendChild(node("p", "muted", "Произнеси целое предложение, затем переверни карточку."))
        area.appendChild(button(if (state.answerMode == AnswerMode.Typed) "Проверить и показать ответ" else "Показать ответ", primary = true) {
            state.exerciseId?.let { dispatch(AppAction.Reveal(it)) }
        }.apply { id = "training-reveal"; className += " reveal-button" })
    }

    private fun renderAnswerBack(root: HTMLElement, card: HTMLElement, wrap: HTMLElement, back: HTMLElement, state: AppUiState, swipeRatingEnabled: Boolean, dispatch: (AppAction) -> Unit) {
        val exercise = state.exercise ?: return
        back.appendChild(node("span", "eyebrow", "Обратная сторона · эталон"))
        back.appendChild(node("p", "answer-sentence").apply {
            setAttribute("lang", activeTargetLangCode())
            appendContrastParts(this, sentenceHighlightParts(exercise.expected, exercise.changes, ChangeSide.After), "change-after")
        })
        if (exercise.accepted.isNotEmpty()) {
            back.appendChild(node("p", "accepted", "Также: ${exercise.accepted.joinToString(" / ")}").apply { setAttribute("lang", activeTargetLangCode()) })
        }
        if (state.answerMode == AnswerMode.Typed) {
            val result = state.evaluation?.correct == true
            back.appendChild(node("div", if (result) "typed-result correct" else "typed-result incorrect").apply {
                appendChild(node("strong", text = if (result) "Совпадает с правильным вариантом" else "Сравни свой ответ с эталоном"))
                appendChild(node("p", text = state.frozenAnswer?.takeIf(String::isNotEmpty) ?: "Ответ не введён"))
            })
        }
        val skill = polski.data.skillById(exercise.primarySkill)
        val presentation = polski.data.presentationBySkillId(exercise.primarySkill)
        val method = if (state.styleId == StyleId.SituationFirst) presentation.situations else presentation.logic
        back.appendChild(node("div", "method-feedback").apply {
            appendChild(node("h3", text = if (state.styleId == StyleId.SituationFirst) "Сравни смысл и форму" else "Разбор изменений"))
            appendChild(node("p", text = method.feedback))
            if (state.styleId == StyleId.SituationFirst) appendChild(node("p", text = exercise.explanation))
        })
        // UC-10 web S2: Changes/Formula/Rule/Contrast used to be hardcoded here per 2-way style
        // check — now they (and Table/NativeParallel/Scene/Examples/WhyOnDemand) come entirely
        // from the resolved style's composed Back blocks; a style switch changes which of these
        // sections show, never a branch in this function.
        val (effective, content) = resolveEffectiveStyleAndContent(state, skill.id)
        val backBlocks = StyleComposer.compose(effective, StylePhase.Back, exercise, skill, presentation, content)
        renderCardBlocks(back, backBlocks, "card-blocks-back", activeTargetLangCode())
        // EN-21 (EnRuPackPlan.md §4.3): the lifehack block is deliberately outside StyleComposer's
        // output — same for every style, so it always renders after the back blocks, never inside
        // whichever ones the active style composed.
        renderLifehackBlock(back, skill.id)
        back.appendChild(node("div", "rating-label", "Когда повторить?"))
        back.appendChild(node("p", "method-review", method.review))
        val ratings = node("div", "ratings")
        back.appendChild(ratings)
        listOf(
            Triple(Rating.Again, "Повторить", "Ошибка или не уверен"),
            Triple(Rating.Good, "Вспомнил", "Воспроизвёл сам"),
        ).forEachIndexed { index, (rating, label, hint) ->
            ratings.appendChild(button("${index + 1} $label", extraClass = "rating-${rating.name.lowercase()}") {
                riveOverlay.trigger(root, back, cardEffectFor(rating))
                dispatch(AppAction.Rate(exercise.id, rating))
            }.apply {
                appendChild(node("small", text = hint))
                appendChild(node("span", text = state.intervals?.get(rating)?.let { due ->
                    intervalLabel(due.toEpochMilliseconds(), state.now?.toEpochMilliseconds() ?: due.toEpochMilliseconds())
                }.orEmpty()))
            })
        }
        back.appendChild(node("p", "muted small", "Оценка планирует следующее повторение навыка."))
        if (swipeRatingEnabled) {
            // v4/UX4-08/09: the whole revealed back-face is the element that tilts/translates
            // with the finger/mouse — this label is a purely visual direction hint, not the
            // gesture target itself. The gesture LISTENERS live one level up, on the stable
            // `.card-answer-wrap` (never itself transformed), not on `back` — see
            // `installSwipeCard`'s own doc comment for why: `back` briefly leaves its own resting
            // hitbox while snapping back/flying out, which could otherwise make a second gesture
            // started right at its edge within that ~220ms window miss it entirely.
            back.appendChild(node("p", "vocabulary-swipe-zone muted small", "← Повторить · Вспомнил →").apply {
                setAttribute("aria-hidden", "true")
            })
            appendSwipeLabels(back)
            // P1-8: `card` (the whole `.flashcard` section: meta + question + answer) is now the
            // element that tilts/translates — `back` alone used to move outside `.flashcard`'s own
            // `overflow:hidden` and get clipped, while the question stayed put; the request itself
            // asks for the whole panel to move, matching the vocabulary card's whole-object flip.
            installSwipeCard(wrap, card) { remembered ->
                val rating = if (remembered) Rating.Good else Rating.Again
                riveOverlay.trigger(root, back, cardEffectFor(rating))
                dispatch(AppAction.Rate(exercise.id, rating))
            }
        }
    }
}

/**
 * UX4-20/21: the bidirectional sibling of [TrainingDomRenderer.applyExpand] — shared by content
 * that already exists in the DOM before and after collapsing (the vocabulary catalog, the
 * case-reference panel), unlike the answer reveal ([TrainingDomRenderer.applyExpand]'s only
 * caller), which never collapses back and whose target face isn't mounted at all until the
 * reveal. Toggling the same `grid-template-rows` CSS class both ways lets the one stylesheet
 * transition animate whichever direction is current — no separate collapse-only code path.
 * `inert`/`aria-hidden` track the collapsed state directly (unlike the reveal case, this content
 * was already live/focusable before collapsing, so it must leave the tab order and a11y tree
 * exactly when collapsed, not merely never having entered them).
 */
internal fun applyCollapsible(wrap: HTMLElement, expanded: Boolean, isToggleEvent: Boolean) {
    wrap.classList.toggle("expanded", expanded)
    if (expanded) { wrap.removeAttribute("inert"); wrap.removeAttribute("aria-hidden") }
    else { wrap.setAttribute("inert", ""); wrap.setAttribute("aria-hidden", "true") }
    if (!isToggleEvent || motionInstantActive()) {
        wrap.style.setProperty("transition", "none")
        wrap.style.setProperty("grid-template-rows", if (expanded) "1fr" else "0fr")
        return
    }
    wrap.style.setProperty("transition", "none")
    wrap.style.setProperty("grid-template-rows", if (expanded) "0fr" else "1fr") // the OLD state
    wrap.getBoundingClientRect() // force layout: commits the "from" frame before animating
    wrap.style.setProperty("transition", "")
    wrap.style.setProperty("grid-template-rows", "")
}

/**
 * UC-10 web S2: the [StyleRecipe] a card actually composes from — [StyleId.NativeContrast]
 * without authored [polski.data.SkillStyleContent.nativeParallel] for this skill resolves to its
 * declared fallback ([StyleComposer.resolveEffectiveStyle]), same rule Front and Back both use.
 */
private fun resolveEffectiveStyleAndContent(state: AppUiState, skillId: String): Pair<StyleRecipe, polski.data.SkillStyleContent> {
    val content = polski.data.styleContentBySkillId(skillId)
    val recipe = StyleRegistry.recipes.getValue(state.styleId)
    val effectiveId = StyleComposer.resolveEffectiveStyle(recipe, content, StyleRegistry.recipes)
    return StyleRegistry.recipes.getValue(effectiveId) to content
}

// UX4-13: shared with the vocabulary card's rating buttons, not private to this file any more.
internal fun intervalLabel(dueMillis: Long, nowMillis: Long): String {
    val minutes = maxOf(1L, (dueMillis - nowMillis + 30_000L) / 60_000L)
    return when {
        minutes < 60 -> "$minutes мин"
        minutes < 2_880 -> "${(minutes + 30) / 60} ч"
        else -> "${(minutes + 720) / 1_440} дн"
    }
}

// UC-10 web S2: shared with CardBlocksWeb.kt's block renderers (Table/Contrast/Changes reuse the
// exact same ending-highlight markup this file has always used), so file-private isn't enough.
// [changedClass] is only the fallback for a part with no [EndingPart.side] of its own (none of
// today's callers construct one, but nothing here requires they do): a part that does carry a
// side is a mixed-role block's own signal for which role it plays (Emphasis contract: "before" =
// warm/dashed, "after" = cool/solid) and always wins over the caller's single class, since one
// block's running text (Formula/Rule/Scene/Examples/WhyOnDemand/NativeParallel-target) can hold a
// literal focus.before span and a literal focus.after span side by side (W3 correction, blocker 2).
internal fun appendContrastParts(container: HTMLElement, parts: List<EndingPart>, changedClass: String) {
    parts.forEach { part ->
        if (part.isChanged) {
            val cls = when (part.side) {
                ChangeSide.Before -> "change-before"
                ChangeSide.After -> "change-after"
                null -> changedClass
            }
            container.appendChild(node("span", if (part.isEnding && cls == "change-after") "$cls ending-highlight" else cls, part.text))
        } else container.appendChild(document.createTextNode(part.text))
    }
}

internal fun node(tag: String, className: String = "", text: String? = null): HTMLElement =
    (document.createElement(tag) as HTMLElement).apply {
        this.className = className
        if (text != null) textContent = text
    }

internal fun button(
    label: String,
    active: Boolean = false,
    pressed: Boolean? = null,
    primary: Boolean = false,
    extraClass: String = "",
    onClick: () -> Unit,
): HTMLButtonElement = (document.createElement("button") as HTMLButtonElement).apply {
    type = "button"
    textContent = label
    className = listOfNotNull(if (active) "active" else null, if (primary) "primary" else null, extraClass.takeIf(String::isNotEmpty)).joinToString(" ")
    if (pressed != null) setAttribute("aria-pressed", pressed.toString())
    addEventListener("click", { onClick() })
}
