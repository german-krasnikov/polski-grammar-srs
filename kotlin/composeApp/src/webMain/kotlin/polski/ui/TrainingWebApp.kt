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
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import kotlin.random.Random
import kotlin.time.Clock
import polski.data.activeCoursePackId
import polski.platform.BrowserLocalDayProvider
import polski.platform.WebProgressRepository
import polski.platform.WebAppearance
import polski.preferences.PreferredAnswerMode
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
                        store.dispatch(
                            if (current.predictionPending) AppAction.ContinuePrediction
                            else if (current.introPending) AppAction.ContinueIntroduction
                            else AppAction.Reveal(id),
                        )
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
