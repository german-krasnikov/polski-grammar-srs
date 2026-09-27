package polski.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import polski.platform.WebPreferencesRepository
import polski.preferences.Appearance
import polski.preferences.Motion
import polski.preferences.PreferencesLoad
import polski.preferences.PreferencesSave
import polski.preferences.PreferredAnswerMode
import polski.preferences.PreferredStyle
import polski.preferences.UserPreferencesCodec
import polski.preferences.UserPreferencesV2
import polski.presentation.AnswerMode
import polski.presentation.AppAction
import polski.presentation.CardPhase
import polski.presentation.StyleId
import polski.presentation.TrainingStore

/** One web-session writer for settings shown on both the card and Settings page. */
internal class WebPreferencesController(private val repository: WebPreferencesRepository = WebPreferencesRepository()) {
    private val initial = repository.load()
    var value by mutableStateOf((initial as? PreferencesLoad.Loaded)?.value ?: UserPreferencesV2())
        private set
    var status by mutableStateOf(when (initial) {
        is PreferencesLoad.RecoveryRequired -> "Настройки требуют восстановления: ${initial.reason}"
        is PreferencesLoad.Unavailable -> initial.reason
        else -> ""
    })
        private set

    val recoveryRaw: String? get() = repository.recoveryRaw()
    fun exportRaw(): String = recoveryRaw ?: UserPreferencesCodec.encode(value)
    fun report(message: String) { status = message }

    fun setStyle(styleId: StyleId, store: TrainingStore) {
        if (save(value.copy(styleId = PreferredStyle.valueOf(styleId.name))))
            store.dispatch(AppAction.SetStyle(styleId))
    }

    fun setAnswerMode(mode: AnswerMode, store: TrainingStore) {
        if (save(value.copy(answerMode = if (mode == AnswerMode.Oral) PreferredAnswerMode.Oral else PreferredAnswerMode.Typed)) && store.state.value.phase == CardPhase.Question)
            store.dispatch(AppAction.SetAnswerMode(mode))
    }

    fun applyPendingAnswerMode(store: TrainingStore) {
        if (store.state.value.phase != CardPhase.Question) return
        val mode = if (value.answerMode == PreferredAnswerMode.Typed) AnswerMode.Typed else AnswerMode.Oral
        if (store.state.value.answerMode != mode) store.dispatch(AppAction.SetAnswerMode(mode))
    }

    fun setAppearance(appearance: Appearance) = save(value.copy(appearance = appearance))
    fun setMotion(motion: Motion) = save(value.copy(motion = motion))
    fun setSwipeRating(enabled: Boolean) = save(value.copy(swipeRatingEnabled = enabled))
    fun setAnimationsEnabled(enabled: Boolean) = save(value.copy(animationsEnabled = enabled))

    fun import(raw: String, store: TrainingStore) {
        when (val result = repository.import(raw)) {
            PreferencesSave.Saved -> {
                value = (UserPreferencesCodec.decode(raw) as polski.preferences.PreferencesDecode.Loaded).value
                status = "Настройки импортированы"
                store.dispatch(AppAction.SetStyle(StyleId.valueOf(value.styleId.name)))
                applyPendingAnswerMode(store)
            }
            is PreferencesSave.WriteFailed -> status = result.reason
        }
    }

    private fun save(next: UserPreferencesV2): Boolean {
        if (recoveryRaw != null) {
            status = "Настройки требуют восстановления. Сохраните исходный JSON, затем импортируйте валидный файл."
            return false
        }
        value = next
        status = when (val result = repository.save(next)) {
            PreferencesSave.Saved -> "Настройки сохранены"
            is PreferencesSave.WriteFailed -> "Не сохранено: ${result.reason}. Выбор доступен для экспорта."
        }
        return true
    }
}
