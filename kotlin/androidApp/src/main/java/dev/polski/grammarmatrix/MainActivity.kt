package dev.polski.grammarmatrix

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.view.WindowCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import polski.presentation.AppAction
import polski.preferences.Appearance
import polski.presentation.AppTab
import polski.presentation.CardPhase
import polski.presentation.EffectOutcome
import polski.presentation.UiEffect
import polski.ui.screens.AndroidContent
import polski.ui.screens.VocabularyScreen

private val darkPalette = darkColorScheme(
    primary = Color(0xFFFFC48B),
    onPrimary = Color(0xFF35200D),
    secondary = Color(0xFFB8D8CE),
    background = Color(0xFF101B21),
    surface = Color(0xFF18262D),
    surfaceContainer = Color(0xFF203139),
)
private val lightPalette = lightColorScheme(
    primary = Color(0xFF81511E),
    onPrimary = Color.White,
    secondary = Color(0xFF32685A),
    background = Color(0xFFF7F4EC),
    surface = Color(0xFFFFFCF6),
    surfaceContainer = Color(0xFFEFEAE0),
)

private fun darkStudyColors(base: ColorScheme): ColorScheme = base.copy(
    onSurface = Color(0xFFE8F1F3),
    onSurfaceVariant = Color(0xFFC1CDD1),
    surfaceContainerLow = Color(0xFF1B2A32),
    surfaceContainerHigh = Color(0xFF29404A),
    primaryContainer = Color(0xFF305467),
    onPrimaryContainer = Color(0xFFF1F9FC),
    secondaryContainer = Color(0xFF284840),
    onSecondaryContainer = Color(0xFFE8F5EF),
    tertiaryContainer = Color(0xFF4B3C50),
    onTertiaryContainer = Color(0xFFF8EDF6),
)

class MainActivity : ComponentActivity() {
    private lateinit var session: AndroidSessionViewModel

    private val importPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) session.importFrom(uri)
    }
    private val exportPicker = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        session.finishExport(uri)
    }
    private val vocabularyImportPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) session.importVocabularyFrom(uri)
    }
    private val vocabularyExportPicker = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        session.exportVocabularyTo(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = ViewModelProvider(this, AndroidSessionViewModel.Factory(applicationContext))[AndroidSessionViewModel::class.java]
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    session.store.dispatch(AppAction.RefreshTime)
                    delay(30_000)
                }
            }
        }
        setContent {
            val systemDark = isSystemInDarkTheme()
            val dark = resolveDarkAppearance(session.preferences.appearance, systemDark)
            SideEffect {
                window.navigationBarColor = if (dark) android.graphics.Color.rgb(16, 27, 33)
                    else android.graphics.Color.rgb(247, 244, 236)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) window.isNavigationBarContrastEnforced = false
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
            val baseColors = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && dark -> dynamicDarkColorScheme(this)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(this)
                dark -> darkPalette
                else -> lightPalette
            }
            val colors = if (dark) darkStudyColors(baseColors) else baseColors
            MaterialTheme(colorScheme = colors) {
                Surface(color = colors.background, contentColor = colors.onBackground, modifier = Modifier.fillMaxSize()) {
                    AndroidScreen(session,
                        onImport = { importPicker.launch("application/json") }, onExport = { effect ->
                        session.prepareExport(effect)
                        exportPicker.launch(effect.filename)
                    }, onVocabularyImport = { vocabularyImportPicker.launch("application/json") },
                        onVocabularyExport = { vocabularyExportPicker.launch("vocabulary-v1.json") })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AndroidScreen(
    session: AndroidSessionViewModel,
    onImport: () -> Unit,
    onExport: (UiEffect.DownloadJson) -> Unit,
    onVocabularyImport: () -> Unit,
    onVocabularyExport: () -> Unit,
) {
    val store = session.store
    val state by store.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.explanationMethod) { session.persistExplanationMethod(state.explanationMethod) }
    val focusReveal = remember(store) { FocusRequester() }
    var confirmImport by remember { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val studyScroll = rememberScrollState()
    val settingsScroll = rememberScrollState()
    val resetEffect = state.pendingEffects.filterIsInstance<UiEffect.ConfirmReset>().firstOrNull()

    BackHandler(enabled = (showSettings || state.tab != AppTab.Training) && !confirmImport && resetEffect == null) {
        if (showSettings) showSettings = false else store.dispatch(AppAction.SelectTab(AppTab.Training))
    }

    state.pendingEffects.forEach { effect ->
        LaunchedEffect(store, effect.id) {
            if (effect is UiEffect.ConfirmReset || !session.claimEffect(effect.id)) return@LaunchedEffect
            when (effect) {
                is UiEffect.FocusReveal -> {
                    if (state.tab == AppTab.Training && state.phase == CardPhase.Question && state.exerciseId == effect.exerciseId) {
                        runCatching { focusReveal.requestFocus() }
                            .onSuccess { store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Completed)) }
                            .onFailure { store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Skipped)) }
                    } else store.dispatch(AppAction.EffectAcknowledged(effect.id, EffectOutcome.Skipped))
                }
                is UiEffect.DownloadJson -> onExport(effect)
                is UiEffect.ConfirmReset -> Unit
            }
        }
    }

    if (resetEffect != null) {
        AlertDialog(
            onDismissRequest = { store.dispatch(AppAction.ResetDecision(resetEffect.id, false)) },
            title = { Text("Сбросить прогресс?") },
            text = { Text(resetEffect.prompt) },
            confirmButton = { TextButton(onClick = { store.dispatch(AppAction.ResetDecision(resetEffect.id, true)) }) { Text("Сбросить") } },
            dismissButton = { TextButton(onClick = { store.dispatch(AppAction.ResetDecision(resetEffect.id, false)) }) { Text("Отмена") } },
        )
    }
    if (confirmImport) {
        AlertDialog(
            onDismissRequest = { confirmImport = false },
            title = { Text("Импортировать прогресс?") },
            text = { Text("Текущий прогресс будет сохранён в резервную копию перед заменой. Выберите совместимый JSON-файл.") },
            confirmButton = { TextButton(onClick = { confirmImport = false; onImport() }) { Text("Выбрать файл") } },
            dismissButton = { TextButton(onClick = { confirmImport = false }) { Text("Отмена") } },
        )
    }

    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val wideLayout = LocalConfiguration.current.screenWidthDp >= 600
    Row(Modifier.fillMaxSize()) {
        if (wideLayout && !showSettings && !imeVisible) AppNavigationRail(
            selected = state.tab,
            onSelect = { store.dispatch(AppAction.SelectTab(it)) },
        )
    Scaffold(
        modifier = Modifier.weight(1f).fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("POLSKI", style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Black)
                        Text("Grammar Matrix", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                actions = {
                    if (showSettings) TextButton(onClick = { showSettings = false }) { Text("Назад") }
                    else if (state.tab != AppTab.Vocabulary) TextButton(onClick = {
                        if (state.revision == state.savedRevision) confirmImport = true
                        else session.showNotice("Дождитесь сохранения прогресса или экспортируйте JSON перед импортом")
                    }) { Text("Импорт") }
                    if (!showSettings) TextButton(onClick = { showSettings = true }) { Text("Настройки") }
                },
            )
        },
        bottomBar = {
            if (!wideLayout && !imeVisible && !showSettings) AppNavigationBar(
                selected = state.tab,
                onSelect = { store.dispatch(AppAction.SelectTab(it)) },
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding).clipToBounds()) {
            Column(
                Modifier.align(Alignment.TopCenter).widthIn(max = 720.dp).fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(if (showSettings) settingsScroll else studyScroll)
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                session.notice?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (showSettings) AndroidSettingsScreen(session)
                else if (state.tab == AppTab.Vocabulary) {
                    VocabularyScreen(session.vocabulary,
                        onImport = onVocabularyImport, onExport = onVocabularyExport,
                        launchMutation = session::launchVocabularyMutation, enableSwipeRating = true)
                }
                else AndroidContent(state, store::dispatch, focusReveal, ::androidDate)
                Spacer(Modifier.height(32.dp))
            }
        }
    }
    }
}

@Composable
private fun AndroidSettingsScreen(session: AndroidSessionViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Настройки", style = MaterialTheme.typography.headlineSmall)
        Text("Оформление", style = MaterialTheme.typography.titleMedium)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            Appearance.entries.forEachIndexed { index, appearance ->
                SegmentedButton(
                    selected = session.preferences.appearance == appearance,
                    onClick = { session.setAppearance(appearance) },
                    shape = SegmentedButtonDefaults.itemShape(index, Appearance.entries.size),
                    enabled = session.preferencesError == null,
                ) {
                    Text(when (appearance) {
                        Appearance.System -> "Система"
                        Appearance.Light -> "Светлая"
                        Appearance.Dark -> "Тёмная"
                    })
                }
            }
        }
        session.preferencesError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

internal fun resolveDarkAppearance(appearance: Appearance, systemDark: Boolean): Boolean = when (appearance) {
    Appearance.System -> systemDark
    Appearance.Light -> false
    Appearance.Dark -> true
}

private fun androidDate(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(millis))
