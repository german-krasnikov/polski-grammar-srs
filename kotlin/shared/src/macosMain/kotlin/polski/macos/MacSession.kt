package polski.macos

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.random.Random
import kotlin.time.Clock
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import polski.presentation.AnswerMode
import polski.presentation.AppAction
import polski.presentation.AppTab
import polski.presentation.CardPhase
import polski.presentation.EffectOutcome
import polski.presentation.StyleId
import polski.presentation.legacyStyleWireValue
import polski.presentation.MatrixSection
import polski.presentation.TimeCapture
import polski.presentation.TimeSource
import polski.presentation.TrainingStore
import polski.presentation.cardEffectFor
import polski.srs.FsrsScheduler
import polski.srs.Rating
import polski.training.ExerciseFactory
import polski.training.ExerciseIdFactory
import polski.training.RandomSource

/** One SwiftUI window's semantic bridge. Swift never calculates ratings or FSRS dates. */
class MacSession(directory: String) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val scheduler = FsrsScheduler()
    private val repository = MacProgressRepository(directory, scheduler)
    private var nextId = 0L
    private var store = newStore()
    private var observer: Job? = null
    private var closed = false

    var onState: ((String) -> Unit)? = null
        set(value) {
            field = value
            if (value != null && !closed) value(snapshot(store.state.value))
        }

    /**
     * Fires once per accepted [AppAction.Rate] with a [polski.presentation.CardEffect] name
     * (`Remembered`/`Again`), for the macOS host's decorative Rive overlay
     * (`Plans/Kotlin/FlipCardRivePlan.md` FC-01/FC-17/FC-20). Never stored in the snapshot: this is
     * a one-shot visual cue, not domain state. Computed here — the exact point where the macOS host
     * dispatches `AppAction.Rate` — mirroring [polski.ios.IosSession.onEffect], so [cardEffectFor]
     * stays the single source of truth and Swift never needs its own copy of the mapping.
     */
    var onEffect: ((String) -> Unit)? = null

    init { observeAndStart() }

    fun currentSnapshot(): String = snapshot(store.state.value)
    fun exportJson(): String? = store.state.value.progress?.let { progress ->
        polski.progress.ProgressCodec.encodeLegacyV1(polski.progress.ProgressDocument(progress))
    }

    /** Values carrying a card ID reject stale taps after the question changes. */
    fun dispatch(command: String, value: String = "") {
        if (closed) return
        val current = store.state.value
        val action: AppAction = when (command) {
            "tab" -> AppTab.entries.firstOrNull { it.name == value }?.let(AppAction::SelectTab)
            "chain" -> AppAction.StartChain()
            "schedule" -> AppAction.StartSchedule
            "matrixSection" -> MatrixSection.entries.firstOrNull { it.name == value }?.let(AppAction::SelectMatrixSection)
            // Swift still speaks the pre-UC-10 2-value wire vocabulary ("Logic"/"Situations");
            // the other 2 styles have no Swift UI yet (Plans/Kotlin/StylesBlueprint.md §5/§6).
            "method" -> legacyStyleWireValue(value)?.let(AppAction::SetStyle)
            "reference" -> AppAction.ToggleReference
            "reset" -> AppAction.RequestReset
            "resetDecision" -> {
                val parts = value.split('|')
                val id = parts.getOrNull(0)?.toLongOrNull()
                if (parts.size == 2 && id != null) AppAction.ResetDecision(id, parts[1] == "true") else null
            }
            "continueIntroduction" -> AppAction.ContinueIntroduction
            "answerMode" -> AnswerMode.entries.firstOrNull { it.name == value }?.let(AppAction::SetAnswerMode)
            "draft" -> AppAction.EditAnswer(value)
            "reveal" -> value.takeIf { it == current.exerciseId }?.let(AppAction::Reveal)
            "rate" -> {
                val parts = value.split('|')
                val rating = parts.getOrNull(1)?.let { name -> Rating.entries.firstOrNull { it.name == name } }
                if (parts.size == 2 && parts[0] == current.exerciseId && rating != null)
                    AppAction.Rate(parts[0], rating) else null
            }
            "export" -> AppAction.RequestExport
            "refresh" -> AppAction.RefreshTime
            else -> null
        } ?: return
        val rateAction = action as? AppAction.Rate
        store.dispatch(action)
        // A rate is accepted iff the store actually moved off the rated card (it may become null
        // on ChainComplete/NoDue) — the same guard TrainingStore.rate itself uses and IosSession
        // mirrors, so a rate the domain silently drops (stale/duplicate exerciseId, wrong phase)
        // never fires a spurious effect.
        if (rateAction != null && store.state.value.exerciseId != rateAction.exerciseId) {
            onEffect?.invoke(cardEffectFor(rateAction.rating).name)
        }
    }

    fun acknowledgeEffect(id: Long, outcome: String) {
        if (!closed) store.dispatch(AppAction.EffectAcknowledged(id,
            if (outcome == "completed") EffectOutcome.Completed else EffectOutcome.Skipped))
    }

    fun importJson(raw: String, completion: (String?) -> Unit) {
        if (closed) { completion("Сессия закрыта"); return }
        if (store.state.value.revision != store.state.value.savedRevision) {
            completion("Дождитесь сохранения прогресса или экспортируйте JSON")
            return
        }
        scope.launch {
            val error = repository.importJson(raw)
            if (error == null && !closed) {
                observer?.cancel()
                store.close()
                store = newStore()
                observeAndStart()
            }
            completion(error)
        }
    }

    fun recoveryRaw(): String? = repository.recoveryRaw()

    fun close() {
        if (closed) return
        closed = true
        observer?.cancel()
        store.close()
        onState = null
        onEffect = null
        scope.cancel()
    }

    private fun observeAndStart() {
        observer = scope.launch { store.state.collect { onState?.invoke(snapshot(it)) } }
        scope.launch { store.start() }
    }

    private fun newStore(): TrainingStore = TrainingStore(
        repository, scheduler,
        ExerciseFactory(RandomSource { Random.nextDouble() }, ExerciseIdFactory { "mac-${++nextId}" }),
        TimeSource {
            val now = Clock.System.now()
            val formatter = NSDateFormatter().apply {
                dateFormat = "yyyy-MM-dd"
                locale = NSLocale(localeIdentifier = "en_US_POSIX")
            }
            TimeCapture(now, formatter.stringFromDate(NSDate(timeIntervalSinceReferenceDate =
                now.toEpochMilliseconds() / 1000.0 - 978307200.0)))
        },
        scope,
    )
}
