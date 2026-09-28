package dev.polski.grammarmatrix

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.random.Random
import kotlin.time.Clock
import polski.presentation.AppAction
import polski.presentation.EffectOutcome
import polski.presentation.StyleId
import polski.presentation.TimeCapture
import polski.presentation.TimeSource
import polski.presentation.TrainingStore
import polski.presentation.UiEffect
import polski.progress.DecodeResult
import polski.progress.ProgressCodec
import polski.srs.FsrsScheduler
import polski.training.PlExerciseEngine
import polski.core.engine.ExerciseIdFactory
import polski.core.engine.RandomSource
import polski.vocabulary.VocabularySession
import polski.preferences.Appearance
import polski.preferences.Motion
import polski.preferences.PreferencesLoad
import polski.preferences.PreferencesSave
import polski.preferences.PreferredStyle
import polski.preferences.UserPreferencesV2
import polski.data.selectActiveCoursePack

class AndroidSessionViewModel(context: Context) : ViewModel() {
    private val appContext = context.applicationContext
    private val scheduler = FsrsScheduler()
    private val preferencesStore = AndroidUserPreferencesStore(appContext)
    // EN-22: must run before anything below touches course data (`store`'s own initializer, next)
    // — every `packRegistry.active`-derived global in :shared is cached for the process's whole
    // lifetime on first read, so a pack switch has to land before that first read, not after.
    init { preferencesStore.peekTargetNative()?.let { (target, native) -> selectActiveCoursePack("$target-$native") } }
    private var savedPreferences = UserPreferencesV2()
    var preferences by mutableStateOf(UserPreferencesV2())
        private set
    var preferencesError by mutableStateOf<String?>(null)
        private set
    val repository = AndroidProgressRepository(appContext, scheduler)
    val vocabulary = VocabularySession(AndroidVocabularyRepository(appContext), scheduler,
        { Clock.System.now() }, { "user.${UUID.randomUUID()}" })
    private var nextId = 0L
    private val startedEffectIds = mutableSetOf<Long>()
    var notice by mutableStateOf<String?>(null)
        private set
    var store by mutableStateOf(newStore())
        private set
    var pendingExport: UiEffect.DownloadJson? = null
        private set

    init {
        viewModelScope.launch {
            when (val loaded = preferencesStore.load()) {
                is PreferencesLoad.Loaded -> {
                    preferences = loaded.value
                    savedPreferences = loaded.value
                    preferencesError = null
                    val preferred = StyleId(loaded.value.styleId.name)
                    if (store.state.value.styleId != preferred) {
                        store.dispatch(AppAction.SetStyle(preferred))
                    }
                }
                is PreferencesLoad.RecoveryRequired -> preferencesError = "Настройки требуют восстановления; исходный JSON сохранён"
                is PreferencesLoad.Unavailable -> preferencesError = loaded.reason
                else -> Unit
            }
        }
        viewModelScope.launch { store.start() }
        viewModelScope.launch { vocabulary.start() }
    }

    private fun newStore() = TrainingStore(
        repository,
        scheduler,
        PlExerciseEngine(RandomSource { Random.nextDouble() }, ExerciseIdFactory { "android-${++nextId}" }),
        TimeSource {
            val now = Clock.System.now()
            TimeCapture(now, SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date(now.toEpochMilliseconds())))
        },
        viewModelScope,
        StyleId(preferences.styleId.name),
    )

    fun setAppearance(appearance: Appearance) {
        if (preferences.appearance == appearance) return
        updatePreferences(preferences.copy(appearance = appearance))
    }

    // UC-01 correction: StyleId is open (any StyleRegistry recipe id), but PreferredStyle is
    // still the closed 4-value enum backing the persisted document — an id outside its entries
    // (e.g. a 5th-style fixture) is a no-op here rather than an unguarded valueOf() crash.
    fun persistStyle(styleId: StyleId) {
        val preferred = PreferredStyle.entries.firstOrNull { it.name == styleId.value } ?: return
        if (preferences.styleId == preferred) return
        updatePreferences(preferences.copy(styleId = preferred))
    }

    /**
     * EN-22: unlike every other setting here, a course switch can't just update in-memory state —
     * it changes hundreds of process-wide cached values (every `by lazy { packRegistry.active.* }`
     * global CourseData.kt declares), which only re-evaluate on a fresh process. So this persists
     * [target]/[native], and only on a successful write calls [restart] (an app relaunch the
     * caller performs — see `MainActivity.restartApp`); a failed write leaves the current pack
     * untouched and reports [preferencesError], same as [updatePreferences]. A restart's own cold
     * start is what actually calls [selectActiveCoursePack] (via this class's own early `init`),
     * before anything touches course data.
     */
    fun persistCourseSelectionAndRestart(target: String, native: String, restart: () -> Unit) {
        if (preferences.target == target && preferences.native == native) return
        val next = preferences.copy(target = target, native = native)
        val previous = preferences
        preferences = next
        viewModelScope.launch {
            when (val result = preferencesStore.save(next)) {
                PreferencesSave.Saved -> restart()
                is PreferencesSave.WriteFailed -> {
                    preferences = previous
                    preferencesError = result.reason
                }
            }
        }
    }

    fun setMotion(motion: Motion) {
        if (preferences.motion == motion) return
        updatePreferences(preferences.copy(motion = motion))
    }

    fun setSwipeRatingEnabled(enabled: Boolean) {
        if (preferences.swipeRatingEnabled == enabled) return
        updatePreferences(preferences.copy(swipeRatingEnabled = enabled))
    }

    /** D5: off disposes/never loads Rive and makes all motion instant (see `motionReduced` in MainActivity). */
    fun setAnimationsEnabled(enabled: Boolean) {
        if (preferences.animationsEnabled == enabled) return
        updatePreferences(preferences.copy(animationsEnabled = enabled))
    }

    /** Reflects [next] immediately for a responsive UI, then persists off the main thread; a failed write rolls back. */
    private fun updatePreferences(next: UserPreferencesV2) {
        val previous = preferences
        preferences = next
        viewModelScope.launch {
            when (val result = preferencesStore.save(next)) {
                PreferencesSave.Saved -> {
                    savedPreferences = next
                    preferencesError = null
                }
                is PreferencesSave.WriteFailed -> {
                    preferences = previous
                    preferencesError = result.reason
                }
            }
        }
    }

    fun claimEffect(id: Long): Boolean = startedEffectIds.add(id)

    fun prepareExport(effect: UiEffect.DownloadJson) { pendingExport = effect }

    fun finishExport(uri: Uri?) {
        val effect = pendingExport ?: return
        pendingExport = null
        if (uri == null) {
            store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Skipped))
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val stream = appContext.contentResolver.openOutputStream(uri, "wt")
                        ?: error("Не удалось открыть файл для экспорта")
                    stream.use { it.write(effect.json.toByteArray(Charsets.UTF_8)) }
                }
                store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Completed))
                notice = "Прогресс экспортирован"
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Failed(error.message ?: "Экспорт не удался")))
                notice = error.message ?: "Экспорт не удался"
            }
        }
    }

    fun importFrom(uri: Uri) {
        val oldStore = store
        if (oldStore.state.value.revision != oldStore.state.value.savedRevision) {
            notice = "Дождитесь сохранения прогресса или экспортируйте JSON перед импортом"
            return
        }
        viewModelScope.launch {
            try {
                val raw = withContext(Dispatchers.IO) {
                    val stream = appContext.contentResolver.openInputStream(uri) ?: error("Не удалось открыть JSON")
                    stream.use(::readUtf8Limited)
                }
                when (val decoded = ProgressCodec.decode(raw)) {
                    is DecodeResult.Invalid -> {
                        notice = "Неподходящий JSON: ${decoded.reason}"
                        return@launch
                    }
                    is DecodeResult.Unsupported -> {
                        notice = "Неподдерживаемая версия ${decoded.version}"
                        return@launch
                    }
                    is DecodeResult.Valid -> Unit
                }
                if (oldStore.state.value.revision != oldStore.state.value.savedRevision) {
                    notice = "Дождитесь сохранения прогресса или экспортируйте JSON перед импортом"
                    return@launch
                }
                oldStore.close()
                val result = repository.importJson(raw)
                startedEffectIds.clear()
                store = newStore()
                store.start()
                when (result) {
                    ImportResult.Imported -> notice = "Прогресс импортирован"
                    is ImportResult.Invalid -> notice = "Неподходящий JSON: ${result.reason}"
                    is ImportResult.Unsupported -> notice = "Неподдерживаемая версия ${result.version}"
                    is ImportResult.Failed -> notice = result.cause.message ?: "Импорт не удался"
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                notice = error.message ?: "Не удалось прочитать файл"
            }
        }
    }

    fun showNotice(value: String?) { notice = value }

    fun launchVocabularyMutation(mutation: suspend () -> Unit) {
        viewModelScope.launch { mutation() }
    }

    fun importVocabularyFrom(uri: Uri) {
        viewModelScope.launch {
            try {
                val raw = withContext(Dispatchers.IO) {
                    val stream = appContext.contentResolver.openInputStream(uri) ?: error("Не удалось открыть словарь")
                    stream.use(::readUtf8Limited)
                }
                if (vocabulary.importJson(raw)) notice = "Словарь импортирован"
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                notice = error.message ?: "Импорт словаря не удался"
            }
        }
    }

    fun exportVocabularyTo(uri: Uri?) {
        if (uri == null) return
        val raw = vocabulary.exportJson() ?: return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val stream = appContext.contentResolver.openOutputStream(uri, "wt")
                        ?: error("Не удалось открыть файл словаря")
                    stream.use { it.write(raw.toByteArray(Charsets.UTF_8)) }
                }
                notice = "Словарь экспортирован"
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                notice = error.message ?: "Экспорт словаря не удался"
            }
        }
    }

    override fun onCleared() {
        store.close()
        super.onCleared()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(AndroidSessionViewModel::class.java))
            return AndroidSessionViewModel(context) as T
        }
    }
}
