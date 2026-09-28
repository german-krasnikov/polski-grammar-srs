package polski.desktop

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import polski.data.skillById
import polski.data.referenceRussianSupport
import polski.data.presentationBySkillId
import polski.grammar.nounPhrase
import polski.model.GramCase
import polski.model.NumberGram
import polski.model.PossessiveId
import polski.model.SentenceSeed
import polski.presentation.AppAction
import polski.presentation.AppTab
import polski.presentation.AppUiState
import polski.presentation.AnswerMode
import polski.presentation.CardPhase
import polski.presentation.StyleId
import polski.presentation.LoadStatus
import polski.presentation.MatrixSection
import polski.presentation.MatrixSelection
import polski.progress.ProgressCodec
import polski.srs.FsrsScheduler
import polski.srs.Rating
import polski.training.PlExerciseEngine
import polski.core.engine.ExerciseIdFactory
import polski.core.engine.RandomSource
import polski.training.sentenceSeeds
import polski.ui.screens.MatrixScreen
import polski.ui.screens.ProgressScreen
import polski.ui.screens.TrainingScreen

class DesktopScreenTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun spaceOnFocusedMethodButtonDoesNotRevealCard() = runComposeUiTest {
        val exercise = PlExerciseEngine(RandomSource { 0.1 }, ExerciseIdFactory { "desktop-space-focus" })
            .generateChain(sentenceSeeds.first()).first()
        val actions = mutableListOf<AppAction>()
        setContent {
            MaterialTheme {
                TrainingScreen(
                    AppUiState(loadStatus = LoadStatus.Ready, exercise = exercise, chain = listOf(exercise)),
                    actions::add,
                    FocusRequester(),
                    { "test-date" },
                )
            }
        }

        onNodeWithText("Живые ситуации")
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .performKeyInput { pressKey(Key.Spacebar) }
        assertEquals(listOf<AppAction>(AppAction.SetStyle(StyleId.SituationFirst)), actions)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun narrowLargeTextStillExposesExplicitReveal() = runComposeUiTest {
        val exercise = PlExerciseEngine(RandomSource { 0.1 }, ExerciseIdFactory { "desktop-narrow" })
            .generateChain(sentenceSeeds.first()).first()
        val actions = mutableListOf<AppAction>()
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 1.3f)) {
                MaterialTheme {
                    Column(Modifier.width(320.dp).verticalScroll(rememberScrollState())) {
                        TrainingScreen(
                            AppUiState(loadStatus = LoadStatus.Ready, exercise = exercise, chain = listOf(exercise)),
                            actions::add,
                            FocusRequester(),
                            { "test-date" },
                        )
                    }
                }
            }
        }
        onNodeWithText(exercise.expected).assertDoesNotExist()
        onNodeWithText("Показать ответ").performScrollTo().assertIsEnabled().performClick()
        assertEquals(AppAction.Reveal(exercise.id), actions.single())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun typedDraftSurvivesMethodSwitchUntilExplicitReveal() = runComposeUiTest {
        val exercise = PlExerciseEngine(RandomSource { 0.1 }, ExerciseIdFactory { "desktop-typed" })
            .generateChain(sentenceSeeds.first()).first()
        var state by mutableStateOf(AppUiState(
            loadStatus = LoadStatus.Ready,
            exercise = exercise,
            chain = listOf(exercise),
            answerMode = AnswerMode.Typed,
        ))
        val actions = mutableListOf<AppAction>()
        setContent {
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    TrainingScreen(state, { action ->
                        actions += action
                        when (action) {
                            is AppAction.EditAnswer -> state = state.copy(draft = action.text)
                            is AppAction.SetStyle -> state = state.copy(styleId = action.styleId)
                            else -> Unit
                        }
                    }, FocusRequester(), { "test-date" })
                }
            }
        }
        onNodeWithText("Ответ по-польски").performTextInput("Moja próba")
        waitForIdle()
        assertEquals("Moja próba", state.draft)
        onNodeWithText("Живые ситуации").performScrollTo().performClick()
        waitForIdle()
        assertEquals(StyleId.SituationFirst, state.styleId)
        assertEquals("Moja próba", state.draft)
        onNodeWithText(exercise.expected).assertDoesNotExist()
        onNodeWithText("Проверить и показать ответ").performScrollTo().performClick()
        assertEquals(AppAction.Reveal(exercise.id), actions.last())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun firstEncounterShowsSourceAndIntroductionBeforeSameExercise() = runComposeUiTest {
        val exercise = PlExerciseEngine(RandomSource { 0.1 }, ExerciseIdFactory { "desktop-method-cycle" })
            .generateChain(sentenceSeeds.first()).first()
        var state by mutableStateOf(AppUiState(loadStatus = LoadStatus.Ready, exercise = exercise,
            chain = listOf(exercise), introPending = true, showReference = true, phase = CardPhase.Question))
        val actions = mutableListOf<AppAction>()
        setContent { MaterialTheme { TrainingScreen(state, actions::add, FocusRequester(), { "test-date" }) } }
        val method = presentationBySkillId(exercise.primarySkill).logic
        onNodeWithText(method.introduce).assertExists()
        onNodeWithText(exercise.source).assertExists()
        onNodeWithText(exercise.expected).assertDoesNotExist()
        onNodeWithText("Таблица этого предложения").assertDoesNotExist()
        onNodeWithText("Таблица под рукой").assertIsNotEnabled()
        onNodeWithText("Скрыть таблицу").assertDoesNotExist()
        onNodeWithText("Показать ответ").assertDoesNotExist()
        onNodeWithText("Перейти к заданию").performClick()
        assertEquals(1, actions.size)
        assertEquals(AppAction.ContinueIntroduction, actions.single())

        state = state.copy(introPending = false, draft = "moja próba")
        waitForIdle()
        onNodeWithText(method.retrieve).assertExists()
        onNodeWithText(exercise.prompt, substring = true).assertExists()
        onNodeWithText("Показать ответ").assertExists()
        onNodeWithText(exercise.expected).assertDoesNotExist()
        onNodeWithText("Таблица этого предложения").assertExists()
        onNodeWithText("Скрыть таблицу").assertIsEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun matrixExposesAuthoredSupportAndGeneratedCaseComparisons() = runComposeUiTest {
        var state by mutableStateOf(AppUiState(loadStatus = LoadStatus.Ready, tab = AppTab.Matrix))
        setContent { MaterialTheme { MatrixScreen(state) {} } }
        val support = referenceRussianSupport.rows.first().comparisons.first()
        onNodeWithContentDescription("Было: ${support.from}. Стало: ${support.to}").assertExists()

        state = state.copy(matrixSelection = state.matrixSelection.copy(section = MatrixSection.Cases))
        waitForIdle()
        val before = nounPhrase("wife", GramCase.NOM, NumberGram.SG, "beautiful", PossessiveId.MY)
        val after = nounPhrase("wife", GramCase.ACC, NumberGram.SG, "beautiful", PossessiveId.MY)
        onNodeWithContentDescription("Было: $before. Стало: $after").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun chainHeaderAndCompletionUseNativeCopyWithFiveAnswers() = runComposeUiTest {
        val chain = PlExerciseEngine(RandomSource { 0.1 }, ExerciseIdFactory { "desktop-chain-step" })
            .generateChain(sentenceSeeds.first())
        var state by mutableStateOf(AppUiState(loadStatus = LoadStatus.Ready, tab = AppTab.Training,
            chain = chain, exercise = chain.first(), phase = CardPhase.Question))
        setContent {
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    TrainingScreen(state, {}, FocusRequester(), { "test-date" })
                }
            }
        }
        onNodeWithText("0 / 5 · Вижу → Прошлое → Отрицание → Владелец → Говорю о").assertExists()
        // The store retains the last zero-based step index when completion begins.
        state = state.copy(phase = CardPhase.ChainComplete, chainIndex = 4, exercise = null)
        waitForIdle()
        onNodeWithText("5 / 5 · Вижу → Прошлое → Отрицание → Владелец → Говорю о").assertExists()
        onAllNodes(SemanticsMatcher.expectValue(
            SemanticsProperties.ProgressBarRangeInfo,
            ProgressBarRangeInfo(1f, 0f..1f),
        )).assertCountEquals(1)
        onNodeWithText("Цепочка завершена").assertExists()
        chain.forEachIndexed { index, exercise -> onNodeWithText("${index + 1}. ${exercise.expected}").assertExists() }
        onNodeWithText("5 преобразований").assertDoesNotExist()
        onNodeWithText("Пять преобразований завершены. Оценки сохранены в расписании повторений.").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun pronounMatrixShowsDesktopCopyAndStartsTheWifeOwnerDrill() = runComposeUiTest {
        val actions = mutableListOf<AppAction>()
        setContent {
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    MatrixScreen(AppUiState(loadStatus = LoadStatus.Ready, tab = AppTab.Matrix,
                        matrixSelection = MatrixSelection(section = MatrixSection.Pronouns)), actions::add)
                }
            }
        }
        onNodeWithText("После предлогов у местоимений третьего лица появляется n-.").assertExists()
        onNodeWithText("Nie widzę…").assertExists()
        onNodeWithText("Idę z…").assertExists()
        onNodeWithText("Mówię o…").assertExists()
        onNodeWithContentDescription("Было: ja. Стало: ze mną").assertExists()
        onNodeWithContentDescription("Было: ona. Стало: o niej").assertExists()
        onNodeWithText("После предлога: do niego, do niej, do nich, dla mnie, dla ciebie.").assertExists()
        onNodeWithText("Mianownik").assertExists()
        onNodeWithText("Biernik").assertExists()
        onNodeWithText("Dopełniacz").assertExists()
        onNodeWithContentDescription("Было: moja piękna żona. Стало: moją piękną żonę").assertExists()
        onNodeWithContentDescription("Было: jego piękna żona. Стало: jego pięknej żony").assertExists()
        onNodeWithText("Тренировать смену владельца").performScrollTo().performClick()
        assertEquals(AppAction.ChooseSkill("agreement.my", SentenceSeed("wife", "beautiful")), actions.single())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun progressScreenExposesCurrentTotalsAndExportAction() = runComposeUiTest {
        val scheduler = FsrsScheduler()
        val document = ProgressCodec.fresh(
            listOf("case.acc.f"), Instant.parse("2026-02-03T12:00:00Z"), "2026-02-03", scheduler,
        )
        val actions = mutableListOf<AppAction>()
        setContent {
            MaterialTheme {
                ProgressScreen(
                    AppUiState(loadStatus = LoadStatus.Ready, progress = document.progress),
                    { "test-date" },
                    actions::add,
                )
            }
        }
        onNodeWithText("Прогресс").assertExists()
        onNodeWithText("0 всего карточек    0 сегодня    0 к повторению").assertExists()
        onNodeWithText("Экспорт JSON").performClick()
        assertEquals(1, actions.size)
        assertEquals(AppAction.RequestExport, actions.single())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun matrixSwitchesSectionsAndDrillsTheSelectedChain() = runComposeUiTest {
        var state by mutableStateOf(AppUiState(loadStatus = LoadStatus.Ready, tab = AppTab.Matrix))
        val actions = mutableListOf<AppAction>()
        setContent {
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    MatrixScreen(state) { action ->
                        if (action is AppAction.SelectMatrixSection) {
                            state = state.copy(matrixSelection = state.matrixSelection.copy(section = action.section))
                        } else actions += action
                    }
                }
            }
        }
        onNodeWithText("Тренировать эту цепочку").performScrollTo().performClick()
        waitForIdle()
        assertEquals(AppAction.ChooseSkill("chain", sentenceSeeds.first()), actions.single())
        onNodeWithText("Грамматическая матрица").performScrollTo()
        onNodeWithText("Падежи и окончания").performScrollTo().performClick()
        waitForIdle()
        assertEquals(MatrixSection.Cases, state.matrixSelection.section)
        onNodeWithText("Времена и лица").performScrollTo().performClick()
        waitForIdle()
        assertEquals(MatrixSection.Verbs, state.matrixSelection.section)
    }

    // EN-24 (UC-09 part 2/2 minimum, Plans/Kotlin/EnRuPackPlan.md §6, macOS slice — this compose
    // desktop preview is the macOS host's JVM preview target): the one live English matrix table,
    // real irregular `forms.generated.json`(en) values through `MatrixTableViewModel`, mirroring
    // `tests/browser/kotlin-en-matrix.spec.ts`'s web assertions for the same table.
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun verbsMatrixShowsEnglishPersonTenseAndDoSupportTablesWithRealIrregularForms() = runComposeUiTest {
        var state by mutableStateOf(AppUiState(loadStatus = LoadStatus.Ready, tab = AppTab.Matrix,
            matrixSelection = MatrixSelection(section = MatrixSection.Verbs)))
        setContent {
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    MatrixScreen(state) { action ->
                        if (action is AppAction.SelectMatrixSection) {
                            state = state.copy(matrixSelection = state.matrixSelection.copy(section = action.section))
                        }
                    }
                }
            }
        }
        onNodeWithText("English: лицо × время (\"see\")").performScrollTo().assertExists()
        // Past/future are invariant across all 7 subjects; present splits "see"/"sees" 4-vs-3.
        onAllNodesWithContentDescription("Было: see. Стало: saw").assertCountEquals(7)
        onAllNodesWithContentDescription("Было: see. Стало: will see").assertCountEquals(7)
        onAllNodesWithContentDescription("Было: see. Стало: sees").assertCountEquals(3)
        onNodeWithText("do-support: вопрос и отрицание").performScrollTo().assertExists()
        onAllNodesWithContentDescription("Было: do. Стало: does").assertCountEquals(3)
        onAllNodesWithContentDescription("Было: do. Стало: did").assertCountEquals(7)
        onAllNodesWithText("не нужен — только will").assertCountEquals(7)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun trainingFrontKeepsAnswerHiddenUntilRevealAction() = runComposeUiTest {
        val exercise = PlExerciseEngine(RandomSource { 0.1 }, ExerciseIdFactory { "desktop-ui-card" })
            .generateChain(sentenceSeeds.first()).first()
        val actions = mutableListOf<AppAction>()
        setContent {
            MaterialTheme {
                TrainingScreen(
                    AppUiState(loadStatus = LoadStatus.Ready, exercise = exercise, chain = listOf(exercise)),
                    actions::add,
                    FocusRequester(),
                    { "test-date" },
                )
            }
        }
        onNodeWithText(exercise.expected).assertDoesNotExist()
        onNodeWithText("Показать ответ").performClick()
        assertEquals(AppAction.Reveal(exercise.id), actions.single())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun revealedTrainingHighlightsRuleAndOffersOnlyTwoRatings() = runComposeUiTest {
        val exercise = PlExerciseEngine(RandomSource { 0.1 }, ExerciseIdFactory { "desktop-ui-card" })
            .generateChain(sentenceSeeds.first()).first()
        val actions = mutableListOf<AppAction>()
        setContent {
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    TrainingScreen(
                        AppUiState(loadStatus = LoadStatus.Ready, phase = CardPhase.Revealed,
                            exercise = exercise, chain = listOf(exercise)),
                        actions::add,
                        FocusRequester(),
                        { "test-date" },
                    )
                }
            }
        }
        onNodeWithText("ЗАПОМНИ").assertExists()
        onNodeWithText(skillById(exercise.primarySkill).formula).assertExists()
        onNodeWithText(exercise.expected, substring = true).assertExists()
        onNodeWithText("3 Хорошо").assertDoesNotExist()
        onNodeWithText("4 Легко").assertDoesNotExist()
        onNodeWithText("1 Повторить").performScrollTo().performSemanticsAction(SemanticsActions.OnClick)
        waitForIdle()
        assertEquals(AppAction.Rate(exercise.id, Rating.Again), actions.single())
    }

    // EN-21 (`Plans/Kotlin/EnRuPackPlan.md` §4.2/§4.3): `case.inst`'s original EN-20 record (still
    // first — the §4.4/§7 full-coverage follow-up only ever appends) is collapsed by default, with
    // the source attribution as the toggle's own always-visible label. `case.inst` now has 2
    // authored records, so the toggle/citation matchers below use `onAllNodesWithText(...)[0]`.
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun revealedTrainingShowsACollapsedLifehackForASkillThatHasOne() = runComposeUiTest {
        val exercise = PlExerciseEngine(RandomSource { 0.1 }, ExerciseIdFactory { "desktop-lifehack-shown" })
            .generateForSkill("case.inst")
        val actions = mutableListOf<AppAction>()
        setContent {
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    TrainingScreen(
                        AppUiState(loadStatus = LoadStatus.Ready, phase = CardPhase.Revealed,
                            exercise = exercise, chain = listOf(exercise)),
                        actions::add,
                        FocusRequester(),
                        { "test-date" },
                    )
                }
            }
        }
        onAllNodesWithText("Лайфхак · источник: editorial").assertCountEquals(2)
        onNodeWithText("Bielec, D. (1998)", substring = true).assertDoesNotExist()
        onAllNodesWithText("Лайфхак · источник: editorial")[0].performScrollTo().performClick()
        onNodeWithText("Bielec, D. (1998)", substring = true).assertExists()
    }

    // Full 16-skill pl-ru coverage (EnRuPackPlan.md §4.4/§7 follow-up) means no real skill is empty
    // any more — `case.acc.n` now has 2 authored records instead of 0. Genuinely empty rendering
    // (`renderLifehackBlock`'s early return) stays covered at the provider layer by
    // `LifehackTest.staticProviderReturnsEmptyForASkillWithNoAuthoredLifehack`, a fixture-id test.
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun revealedTrainingShowsBothLifehacksForASkillWithMoreThanOne() = runComposeUiTest {
        val exercise = PlExerciseEngine(RandomSource { 0.1 }, ExerciseIdFactory { "desktop-lifehack-two" })
            .generateForSkill("case.acc.n")
        val actions = mutableListOf<AppAction>()
        setContent {
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    TrainingScreen(
                        AppUiState(loadStatus = LoadStatus.Ready, phase = CardPhase.Revealed,
                            exercise = exercise, chain = listOf(exercise)),
                        actions::add,
                        FocusRequester(),
                        { "test-date" },
                    )
                }
            }
        }
        onAllNodesWithText("Лайфхак · источник: editorial").assertCountEquals(2)
    }
}
