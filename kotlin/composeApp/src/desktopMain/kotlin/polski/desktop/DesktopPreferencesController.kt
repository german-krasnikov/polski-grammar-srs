package polski.desktop

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import polski.data.activeCoursePackId
import polski.data.availableCoursePacks
import polski.data.selectCoursePack
import polski.preferences.Appearance
import polski.preferences.Motion
import polski.preferences.PreferencesDecode
import polski.preferences.PreferencesLoad
import polski.preferences.PreferencesSave
import polski.preferences.PreferredAnswerMode
import polski.preferences.PreferredStyle
import polski.preferences.UserPreferencesCodec
import polski.preferences.UserPreferencesV1
import polski.presentation.AnswerMode
import polski.presentation.AppAction
import polski.presentation.CardPhase
import polski.presentation.StyleId

internal sealed interface DesktopPreferencesStatus {
    data object Loaded : DesktopPreferencesStatus
    data class RecoveryRequired(val reason: String) : DesktopPreferencesStatus
    data class Unavailable(val reason: String) : DesktopPreferencesStatus
    data class WriteFailed(val reason: String) : DesktopPreferencesStatus
}

/** One window's preference writer. Review data and transient card state remain in their owners. */
internal class DesktopPreferencesController(private val repository: DesktopPreferencesRepository) {
    private val initial = repository.load()
    var value by mutableStateOf((initial as? PreferencesLoad.Loaded)?.value ?: UserPreferencesV1())
        private set
    var status by mutableStateOf(when (initial) {
        is PreferencesLoad.Loaded, PreferencesLoad.Missing -> DesktopPreferencesStatus.Loaded
        is PreferencesLoad.RecoveryRequired -> DesktopPreferencesStatus.RecoveryRequired(initial.reason)
        is PreferencesLoad.Unavailable -> DesktopPreferencesStatus.Unavailable(initial.reason)
    })
        private set

    init { syncActivePack() }

    val recoveryRaw: String? get() = repository.recoveryRaw()
    fun exportRaw(): String = recoveryRaw ?: UserPreferencesCodec.encode(value)

    /** EN-22: target/native pickers next to the style picker — a real, known [availableCoursePacks]
     *  language switches `polski.data.packRegistry`'s active pack; an unknown one is a no-op, same
     *  guard `MacPreferencesSession.set` uses for the native host's own version of this picker. */
    fun setTarget(target: String) { if (availableCoursePacks.any { it.target == target }) { save(value.copy(target = target)); syncActivePack() } }
    fun setNative(native: String) { if (availableCoursePacks.any { it.native == native }) { save(value.copy(native = native)); syncActivePack() } }

    private fun syncActivePack() {
        val pairId = "${value.target}-${value.native}"
        if (availableCoursePacks.any { it.pairId == pairId }) selectCoursePack(pairId)
    }

    /**
     * EnRuAcceptance-2026-09-28.md §7 item 2 parity (ADR-38), Desktop-preview lane:
     * `Main.kt`'s `buildTrainingStoreOrRollback` can roll `polski.data.packRegistry`'s shared
     * active pack back to a pack this controller never chose (its own [availableCoursePacks] check
     * in [setTarget]/[setNative] only probes that a pack *parses*, not that its engine can actually
     * build a session) — that rollback never reaches this controller's own persisted [value], so
     * Settings would otherwise keep reporting the picked-but-broken pack forever while Training
     * silently serves the rolled-back one with no explanation. Call right after a store rebuild:
     * when persisted target/native no longer names the pack that is really active, corrects the
     * persisted document to match reality (survives restart, same write path as [setTarget]) and
     * returns a one-shot explanation to show; returns null when nothing is out of sync.
     */
    fun reconcileWithActivePack(): String? {
        val requestedPairId = "${value.target}-${value.native}"
        val actualPairId = activeCoursePackId
        if (requestedPairId == actualPairId) return null
        val actual = availableCoursePacks.firstOrNull { it.pairId == actualPairId } ?: return null
        save(value.copy(target = actual.target, native = actual.native))
        return "Пакет «$requestedPairId» пока не может обучать — вернулись к «${actual.pairId}»"
    }

    fun setStyle(styleId: StyleId, dispatch: (AppAction) -> Unit) {
        if (save(value.copy(styleId = PreferredStyle.valueOf(styleId.value)))) {
            dispatch(AppAction.SetStyle(styleId))
        }
    }

    fun setAnswerMode(mode: AnswerMode, phase: CardPhase, dispatch: (AppAction) -> Unit) {
        if (save(value.copy(answerMode =
                if (mode == AnswerMode.Oral) PreferredAnswerMode.Oral else PreferredAnswerMode.Typed)) &&
            phase == CardPhase.Question) dispatch(AppAction.SetAnswerMode(mode))
    }

    fun applyPendingAnswerMode(phase: CardPhase, current: AnswerMode, dispatch: (AppAction) -> Unit) {
        if (phase != CardPhase.Question) return
        val desired = if (value.answerMode == PreferredAnswerMode.Typed) AnswerMode.Typed else AnswerMode.Oral
        if (current != desired) dispatch(AppAction.SetAnswerMode(desired))
    }

    fun setAppearance(appearance: Appearance) { save(value.copy(appearance = appearance)) }
    fun setMotion(motion: Motion) { save(value.copy(motion = motion)) }

    fun importJson(raw: String, phase: CardPhase, currentAnswerMode: AnswerMode, dispatch: (AppAction) -> Unit) {
        when (val saved = repository.importJson(raw)) {
            PreferencesSave.Saved -> {
                value = (UserPreferencesCodec.decode(raw) as PreferencesDecode.Loaded).value
                status = DesktopPreferencesStatus.Loaded
                dispatch(AppAction.SetStyle(StyleId(value.styleId.name)))
                applyPendingAnswerMode(phase, currentAnswerMode, dispatch)
            }
            is PreferencesSave.WriteFailed -> status = DesktopPreferencesStatus.WriteFailed(saved.reason)
        }
    }

    private fun save(next: UserPreferencesV1): Boolean {
        if (recoveryRaw != null) {
            status = DesktopPreferencesStatus.RecoveryRequired("Сохраните исходный JSON и импортируйте исправленные настройки")
            return false
        }
        value = next
        status = when (val result = repository.save(next)) {
            PreferencesSave.Saved -> DesktopPreferencesStatus.Loaded
            is PreferencesSave.WriteFailed -> DesktopPreferencesStatus.WriteFailed(result.reason)
        }
        return true
    }
}
