package polski.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.nio.file.Files
import java.nio.file.Path
import java.time.ZoneId
import java.util.UUID
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random
import kotlin.time.Clock
import polski.data.nounById
import polski.data.skillById
import polski.data.skills
import polski.presentation.AnswerMode
import polski.presentation.AppAction
import polski.presentation.AppTab
import polski.presentation.AppUiState
import polski.presentation.CardPhase
import polski.presentation.EffectOutcome
import polski.presentation.StyleId
import polski.presentation.LoadStatus
import polski.presentation.TimeCapture
import polski.presentation.TimeSource
import polski.presentation.TrainingMode
import polski.presentation.TrainingStore
import polski.presentation.UiEffect
import polski.srs.FsrsScheduler
import polski.srs.Rating
import polski.training.ExerciseFactory
import polski.training.ExerciseIdFactory
import polski.training.RandomSource
import polski.training.sentenceSeeds
import polski.ui.screens.MatrixScreen
import polski.ui.screens.CaseReferenceScreen
import polski.ui.screens.ProgressScreen
import polski.ui.screens.TrainingScreen
import polski.ui.screens.RecoveryScreen
import polski.ui.screens.TabButton
import polski.ui.screens.VocabularyScreen
import polski.vocabulary.VocabularySession
import polski.preferences.PreferredStyle
import polski.preferences.PreferredAnswerMode
import polski.preferences.Appearance
import polski.preferences.Motion
import polski.ui.screens.DesktopSettingsScreen
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.input.key.KeyShortcut

fun main() = application {
    val scheduler = remember { FsrsScheduler() }
    val repository = remember { DesktopProgressRepository(desktopDataDirectory(), scheduler) }
    val preferences = remember { DesktopPreferencesController(DesktopPreferencesRepository(desktopDataDirectory())) }
    val macSystem = remember { MacSystemPreferences() }
    var generation by remember { mutableIntStateOf(0) }
    Window(
        onCloseRequest = ::exitApplication,
        title = "Polski Grammar Matrix",
        state = rememberWindowState(width = 1180.dp, height = 800.dp),
    ) {
        DisposableEffect(macSystem) {
            macSystem.start()
            onDispose { macSystem.close() }
        }
        var settingsOpen by remember { mutableStateOf(false) }
        MenuBar {
            Menu("Polski Grammar Matrix") {
                Item("Settings…", onClick = { settingsOpen = true }, shortcut = KeyShortcut(Key.Comma, meta = true))
            }
        }
        DesktopTheme(preferences.value.appearance, macSystem.status) {
            DesktopSession(repository, scheduler, preferences, macSystem.status, generation, settingsOpen,
                onSettings = { settingsOpen = it }, onImported = { generation++ })
        }
    }
}

private fun desktopDataDirectory(): Path = Path.of(
    System.getProperty("user.home"), "Library", "Application Support", "Polski Grammar Matrix",
)

@Composable
private fun DesktopSession(
    repository: DesktopProgressRepository,
    scheduler: FsrsScheduler,
    preferences: DesktopPreferencesController,
    macSystemStatus: MacSystemStatus,
    generation: Int,
    settingsOpen: Boolean,
    onSettings: (Boolean) -> Unit,
    onImported: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val store = remember(generation) {
        var nextId = 0L
        TrainingStore(
            repository, scheduler,
            ExerciseFactory(RandomSource { Random.nextDouble() }, ExerciseIdFactory { "desktop-${++nextId}" }),
            TimeSource {
                val at = Clock.System.now()
                val localDay = java.time.Instant.ofEpochMilli(at.toEpochMilliseconds())
                    .atZone(ZoneId.systemDefault()).toLocalDate().toString()
                TimeCapture(at, localDay)
            },
            scope,
            StyleId(preferences.value.styleId.name),
            if (preferences.value.answerMode == PreferredAnswerMode.Typed) AnswerMode.Typed else AnswerMode.Oral,
        )
    }
    val state by store.state.collectAsState()
    val vocabulary = remember {
        VocabularySession(DesktopVocabularyRepository(desktopDataDirectory()), scheduler,
            { Clock.System.now() }, { "user.${UUID.randomUUID()}" })
    }
    val focusReveal = remember { FocusRequester() }
    val settingsEntryFocus = remember { FocusRequester() }
    val attemptedEffects = remember(store) { mutableSetOf<Long>() }
    var notice by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(store) {
        store.start()
        while (true) {
            delay(30_000)
            store.dispatch(AppAction.RefreshTime)
            vocabulary.refresh()
        }
    }
    LaunchedEffect(vocabulary) { vocabulary.start() }
    LaunchedEffect(state.phase, state.exerciseId, preferences.value.answerMode) {
        preferences.applyPendingAnswerMode(state.phase, state.answerMode, store::dispatch)
    }
    DisposableEffect(store) { onDispose { store.close() } }
    LaunchedEffect(settingsOpen) {
        if (!settingsOpen) runCatching { settingsEntryFocus.requestFocus() }
    }

    for (effect in state.pendingEffects) {
        LaunchedEffect(store, effect.id) {
            if (!attemptedEffects.add(effect.id)) return@LaunchedEffect
            when (effect) {
                is UiEffect.FocusReveal -> {
                    if (state.tab == AppTab.Training && state.phase == CardPhase.Question && state.exerciseId == effect.exerciseId) {
                        runCatching { focusReveal.requestFocus() }
                            .onSuccess { store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Completed)) }
                            .onFailure { store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Skipped)) }
                    } else store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Skipped))
                }
                is UiEffect.ConfirmReset -> {
                    val confirmed = JOptionPane.showConfirmDialog(
                        null, effect.prompt, "Polski Grammar Matrix", JOptionPane.YES_NO_OPTION,
                    ) == JOptionPane.YES_OPTION
                    store.dispatch(AppAction.ResetDecision(effect.id, confirmed))
                }
                is UiEffect.DownloadJson -> {
                    val chooser = JFileChooser().apply { selectedFile = java.io.File(effect.filename) }
                    if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                        try {
                            val file = chooser.selectedFile.toPath()
                            val replace = !Files.exists(file) || JOptionPane.showConfirmDialog(
                                null, "Заменить существующий файл ${file.fileName}?", "Экспорт JSON", JOptionPane.YES_NO_OPTION,
                            ) == JOptionPane.YES_OPTION
                            if (replace) {
                                withContext(Dispatchers.IO) { writeDesktopJsonAtomically(file, effect.json) }
                                store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Completed))
                                notice = "JSON экспортирован: ${file.fileName}"
                            } else store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Skipped))
                        } catch (error: Exception) {
                            store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Failed(error.message ?: "Экспорт не удался")))
                        }
                    } else store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Skipped))
                }
            }
        }
    }

    fun importFromFile() {
        if (state.revision != state.savedRevision) {
            notice = "Дождитесь сохранения прогресса или экспортируйте JSON перед импортом."
            return
        }
        val chooser = JFileChooser()
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return
        if (state.progress != null && JOptionPane.showConfirmDialog(
                null,
                "Заменить текущий прогресс? Перед заменой сохранится его копия.",
                "Импорт прогресса",
                JOptionPane.YES_NO_OPTION,
            ) != JOptionPane.YES_OPTION
        ) return
        scope.launch {
            try {
                val file = chooser.selectedFile.toPath()
                val raw = withContext(Dispatchers.IO) {
                    require(Files.size(file) <= 10_000_000) { "Файл слишком большой" }
                    Files.readString(file)
                }
                when (val result = repository.importJson(raw)) {
                    DesktopImportResult.Imported -> onImported()
                    is DesktopImportResult.Invalid -> notice = "Неподходящий JSON: ${result.reason}"
                    is DesktopImportResult.Unsupported -> notice = "Неподдерживаемая версия ${result.version}"
                    is DesktopImportResult.Failed -> notice = result.cause.message ?: "Импорт не удался"
                }
            } catch (error: Exception) {
                notice = error.message ?: "Не удалось прочитать файл"
            }
        }
    }

    fun importVocabulary() {
        val chooser = JFileChooser()
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return
        scope.launch {
            try {
                val raw = withContext(Dispatchers.IO) {
                    val file = chooser.selectedFile.toPath()
                    require(Files.size(file) <= 10_000_000) { "Файл слишком большой" }
                    Files.readString(file)
                }
                vocabulary.importJson(raw)
            } catch (error: Exception) {
                notice = error.message ?: "Не удалось прочитать словарь"
            }
        }
    }

    fun exportVocabulary() {
        val raw = vocabulary.exportJson() ?: return
        val chooser = JFileChooser().apply { selectedFile = java.io.File("vocabulary-v1.json") }
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return
        val file = chooser.selectedFile.toPath()
        if (Files.exists(file) && JOptionPane.showConfirmDialog(null,
                "Заменить существующий файл ${file.fileName}?", "Экспорт словаря",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return
        scope.launch {
            try {
                withContext(Dispatchers.IO) { writeDesktopJsonAtomically(file, raw) }
                notice = "Словарь экспортирован: ${file.fileName}"
            } catch (error: Exception) {
                notice = error.message ?: "Экспорт словаря не удался"
            }
        }
    }

    fun importPreferences() {
        val chooser = JFileChooser()
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return
        scope.launch {
            try {
                val raw = withContext(Dispatchers.IO) {
                    val file = chooser.selectedFile.toPath()
                    require(Files.size(file) <= 10_000_000) { "Файл настроек слишком большой" }
                    Files.readString(file)
                }
                preferences.importJson(raw, state.phase, state.answerMode, store::dispatch)
            } catch (error: Exception) { notice = error.message ?: "Не удалось импортировать настройки" }
        }
    }

    fun exportPreferences() {
        val raw = preferences.exportRaw()
        val filename = if (preferences.recoveryRaw != null) "preferences-recovery.json" else "preferences-v1.json"
        val chooser = JFileChooser().apply { selectedFile = java.io.File(filename) }
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return
        val file = chooser.selectedFile.toPath()
        if (Files.exists(file) && JOptionPane.showConfirmDialog(null,
                "Заменить существующий файл ${file.fileName}?", "Экспорт настроек", JOptionPane.YES_NO_OPTION,
            ) != JOptionPane.YES_OPTION) return
        scope.launch {
            try {
                withContext(Dispatchers.IO) { writeDesktopJsonAtomically(file, raw) }
                notice = "Настройки экспортированы: ${file.fileName}"
            } catch (error: Exception) { notice = error.message ?: "Экспорт настроек не удался" }
        }
    }

    val dispatch: (AppAction) -> Unit = { action ->
        when (action) {
            is AppAction.SetStyle -> preferences.setStyle(action.styleId, store::dispatch)
            is AppAction.SetAnswerMode -> preferences.setAnswerMode(action.mode, state.phase, store::dispatch)
            else -> store.dispatch(action)
        }
    }

    androidx.compose.foundation.layout.BoxWithConstraints(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    ) {
        val wide = maxWidth >= 900.dp
        Row(Modifier.fillMaxSize()) {
            if (wide) {
                Column(Modifier.width(190.dp).fillMaxHeight().padding(start = 18.dp, top = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DesktopNavigation(state.tab, true, settingsOpen, settingsEntryFocus,
                        onTab = { onSettings(false); store.dispatch(AppAction.SelectTab(it)) }, onSettings = { onSettings(true) })
                    OutlinedButton(onClick = ::importFromFile) { Text("Импорт JSON") }
                }
            }
            Column(
                Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())
                    .padding(horizontal = if (wide) 30.dp else 18.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("POLSKI  /  GRAMMAR MATRIX", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("От мысли — к точной форме", style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    }
                    if (wide) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DesktopMetric(state.dueCount, "к повторению")
                        DesktopMetric(state.todayCount, "сегодня")
                    }
                }
                if (!wide) {
                    DesktopNavigation(state.tab, false, settingsOpen, settingsEntryFocus,
                        onTab = { onSettings(false); store.dispatch(AppAction.SelectTab(it)) }, onSettings = { onSettings(true) })
                }
                notice?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (state.tab == AppTab.Vocabulary && state.loadStatus == LoadStatus.Ready) {
                    Box(if (settingsOpen) Modifier.height(0.dp).alpha(0f) else Modifier.fillMaxWidth()) {
                        VocabularyScreen(vocabulary, ::importVocabulary, ::exportVocabulary,
                            launchMutation = { mutation -> scope.launch { mutation() } })
                    }
                }
                when {
                    settingsOpen -> DesktopSettingsScreen(preferences, macSystemStatus, onClose = { onSettings(false) },
                        onStyle = { preferences.setStyle(it, store::dispatch) },
                        onAnswerMode = { preferences.setAnswerMode(it, state.phase, store::dispatch) },
                        onAppearance = preferences::setAppearance, onMotion = preferences::setMotion,
                        onProgressImport = ::importFromFile, onProgressExport = { store.dispatch(AppAction.RequestExport) },
                        onVocabularyImport = ::importVocabulary, onVocabularyExport = ::exportVocabulary,
                        onPreferencesImport = ::importPreferences, onPreferencesExport = ::exportPreferences)
                    state.loadStatus != LoadStatus.Ready -> RecoveryScreen(state) { store.dispatch(AppAction.RequestExport) }
                    state.tab == AppTab.Training -> Box(Modifier.widthIn(max = 780.dp)) {
                        TrainingScreen(state, dispatch, focusReveal, ::desktopDate)
                    }
                    state.tab == AppTab.Vocabulary -> Unit
                    state.tab == AppTab.Matrix -> MatrixScreen(state, dispatch)
                    else -> ProgressScreen(state, ::desktopDate, dispatch)
                }
            }
        }
    }

}

@Composable
private fun DesktopNavigation(tab: AppTab, vertical: Boolean, settingsOpen: Boolean,
                              settingsFocus: FocusRequester, onTab: (AppTab) -> Unit, onSettings: () -> Unit) {
    val entries = listOf(AppTab.Training to "Карточки", AppTab.Vocabulary to "Слова",
        AppTab.Matrix to "Таблицы и схема", AppTab.Progress to "Прогресс")
    if (vertical) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            entries.forEach { (destination, label) ->
                TabButton(label, tab == destination && !settingsOpen) { onTab(destination) }
            }
            OutlinedButton(onClick = onSettings, modifier = Modifier.focusRequester(settingsFocus)) { Text("Настройки") }
        }
    } else {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            entries.forEach { (destination, label) ->
                TabButton(label, tab == destination && !settingsOpen) { onTab(destination) }
            }
            OutlinedButton(onClick = onSettings, modifier = Modifier.focusRequester(settingsFocus)) { Text("Настройки") }
        }
    }
}

@Composable
private fun DesktopMetric(value: Int, label: String) {
    Column(Modifier.background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(16.dp)).padding(horizontal = 18.dp, vertical = 12.dp)) {
        Text(value.toString(), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

internal fun desktopDate(millis: Long): String = java.time.Instant.ofEpochMilli(millis)
    .atZone(ZoneId.systemDefault()).toLocalDateTime().toString().replace('T', ' ')
