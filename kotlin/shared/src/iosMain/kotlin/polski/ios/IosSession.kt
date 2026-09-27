package polski.ios

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Clock
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import polski.presentation.AnswerMode
import polski.presentation.AppAction
import polski.presentation.AppTab
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

/** SwiftUI's one scene-owned entry point. Call [close] when the scene owner is released. */
class IosSession(private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val scheduler = FsrsScheduler()
    private val repository = IosProgressRepository(scheduler, defaults)
    private var nextId = 0L
    private var store = newStore()
    private var observer: Job? = null
    private var closed = false
    var onState: ((String) -> Unit)? = null
        set(value) {
            field = value
            if (value != null) value(snapshot(store.state.value))
        }

    /**
     * Fires once per accepted [AppAction.Rate] with a [polski.presentation.CardEffect] name
     * (`Remembered`/`Again`), for the host's decorative Rive overlay
     * (`Plans/Kotlin/FlipCardRivePlan.md` FC-01/FC-17/FC-20). Never stored in the snapshot state:
     * this is a one-shot visual cue, not domain state. Computed here — the exact point where the
     * iOS host dispatches `AppAction.Rate` — rather than in Swift, so [cardEffectFor] stays the
     * single source of truth and Swift never needs its own copy of the rating→effect mapping.
     */
    var onEffect: ((String) -> Unit)? = null

    init { observeAndStart() }

    fun currentSnapshot(): String = snapshot(store.state.value)

    /** Commands are semantic host actions; malformed values are ignored without changing session state. */
    fun dispatch(command: String, value: String = "") {
        if (closed) return
        val action: AppAction = when (command) {
            "tab" -> AppTab.entries.firstOrNull { it.name == value }?.let(AppAction::SelectTab)
            "chain" -> AppAction.StartChain()
            "seed" -> value.toIntOrNull()?.let(AppAction::SelectChainSeed)
            "schedule" -> AppAction.StartSchedule
            "skillPicker" -> AppAction.OpenSkillPicker
            "skill" -> AppAction.ChooseSkill(value)
            "answerMode" -> AnswerMode.entries.firstOrNull { it.name == value }?.let {
                defaults.setObject(value, forKey = "answerMode")
                AppAction.SetAnswerMode(it)
            }
            // Swift still speaks the pre-UC-10 2-value wire vocabulary (see [legacyStyleWireValue]).
            "explanationMethod" -> legacyStyleWireValue(value)?.let {
                defaults.setObject(value, forKey = "explanationMethod")
                AppAction.SetStyle(it)
            }
            // StylesBlueprint.md S1: the native 4-value wire, alongside the legacy one above —
            // switching never creates a review nor touches draft/frozenAnswer (TrainingStore.SetStyle).
            "styleId" -> StyleId.entries.firstOrNull { it.name == value }?.let {
                defaults.setObject(value, forKey = "styleId")
                AppAction.SetStyle(it)
            }
            "draft" -> AppAction.EditAnswer(value)
            "continueIntroduction" -> AppAction.ContinueIntroduction
            "reveal" -> store.state.value.exerciseId?.let(AppAction::Reveal)
            "rate" -> {
                val id = store.state.value.exerciseId
                val rating = Rating.entries.firstOrNull { it.name == value }
                if (id != null && rating != null) AppAction.Rate(id, rating) else null
            }
            "reference" -> AppAction.ToggleReference
            "matrixSection" -> MatrixSection.entries.firstOrNull { it.name == value }?.let(AppAction::SelectMatrixSection)
            "matrixNoun" -> AppAction.SetMatrixSelection(store.state.value.matrixSelection.copy(nounId = value))
            "matrixAdjective" -> AppAction.SetMatrixSelection(store.state.value.matrixSelection.copy(adjectiveId = value))
            "matrixOwner" -> AppAction.SetMatrixSelection(store.state.value.matrixSelection.copy(ownerId = value))
            "matrixNumber" -> AppAction.SetMatrixSelection(store.state.value.matrixSelection.copy(numberId = value))
            "matrixVerb" -> AppAction.SetMatrixSelection(store.state.value.matrixSelection.copy(verbId = value))
            "matrixGender" -> AppAction.SetMatrixSelection(store.state.value.matrixSelection.copy(feminineGroup = value == "f"))
            "export" -> AppAction.RequestExport
            "reset" -> AppAction.RequestReset
            "refresh" -> AppAction.RefreshTime
            else -> null
        } ?: return
        val rateAction = action as? AppAction.Rate
        store.dispatch(action)
        // A rate is accepted iff the store actually moved off the rated card (it may become null on
        // ChainComplete/NoDue) — the same guard TrainingStore.rate itself uses, so a rate the domain
        // silently drops (stale/duplicate exerciseId, wrong phase) never fires a spurious effect.
        if (rateAction != null && store.state.value.exerciseId != rateAction.exerciseId) {
            onEffect?.invoke(cardEffectFor(rateAction.rating).name)
        }
    }

    fun acknowledgeEffect(id: Long, outcome: String) {
        if (!closed) store.dispatch(AppAction.EffectAcknowledged(id, if (outcome == "completed") EffectOutcome.Completed else EffectOutcome.Skipped))
    }

    fun decideReset(id: Long, confirmed: Boolean) {
        if (!closed) store.dispatch(AppAction.ResetDecision(id, confirmed))
    }

    /** Completion is called on the main dispatcher after validation, backup and reload. */
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
        ExerciseFactory(RandomSource { Random.nextDouble() }, ExerciseIdFactory { "ios-${++nextId}" }),
        TimeSource {
            val now = Clock.System.now()
            val formatter = NSDateFormatter().apply {
                dateFormat = "yyyy-MM-dd"
                locale = NSLocale(localeIdentifier = "en_US_POSIX")
            }
            TimeCapture(now, formatter.stringFromDate(NSDate(timeIntervalSinceReferenceDate = now.toEpochMilliseconds() / 1000.0 - 978307200.0)))
        },
        scope,
        // The native 4-value key wins when present; the legacy 2-value key covers a session that
        // last stored its style before S1 (no migration write — ADR-6, iOS storage is pre-release).
        defaults.stringForKey("styleId")?.let { name -> StyleId.entries.firstOrNull { it.name == name } }
            ?: legacyStyleWireValue(defaults.stringForKey("explanationMethod") ?: "") ?: StyleId.RuleFirst,
        if (defaults.stringForKey("answerMode") == "Typed") AnswerMode.Typed else AnswerMode.Oral,
    )
}
