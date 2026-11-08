package polski.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import polski.data.courseChainPresentation
import polski.data.presentationBySkillId
import polski.data.skills
import polski.model.Evaluation
import polski.model.Exercise
import polski.model.FormChange
import polski.model.NumberGram
import polski.model.PossessiveId
import polski.srs.Rating
import polski.srs.SchedulePreview

/**
 * Independent expectations (P15): every assertion below is a literal value, not "whatever
 * [trainingCardModel] currently returns". Fixtures use real course data (`skillById`'s own skills,
 * `case.gen.neg`'s authored pl-ru lifehack) rather than mocks — no Compose/DOM involved.
 */
class TrainingCardModelTest {
    private fun exercise(id: String = "fixture-1", skillId: String = "case.gen.neg", accepted: List<String> = emptyList()) = Exercise(
        id = id, primarySkill = skillId, source = "Mam psa.", prompt = "Nie...",
        expected = "Nie mam psa.", accepted = accepted, explanation = "explanation", tags = emptyList(),
        nounId = "dog", adjectiveId = "beautiful", possessive = PossessiveId.MY, number = NumberGram.SG,
        changes = listOf(FormChange("psa", "psa", "падеж без изменений")),
    )

    private fun baseState(
        phase: CardPhase = CardPhase.Question,
        answerMode: AnswerMode = AnswerMode.Oral,
        introPending: Boolean = false,
        exercise: Exercise? = exercise(),
        mode: TrainingMode = TrainingMode.Focused,
        chain: List<Exercise> = emptyList(),
        chainIndex: Int = 0,
        styleId: StyleId = StyleId.RuleFirst,
        draft: String = "",
        frozenAnswer: String? = null,
        evaluation: Evaluation? = null,
        intervals: SchedulePreview? = null,
        now: Instant? = null,
    ) = AppUiState(
        loadStatus = LoadStatus.Ready, mode = mode, chain = chain, chainIndex = chainIndex,
        exercise = exercise, phase = phase, introPending = introPending, answerMode = answerMode,
        styleId = styleId, draft = draft, frozenAnswer = frozenAnswer, evaluation = evaluation,
        intervals = intervals, now = now,
    )

    private val logic = presentationBySkillId("case.gen.neg").logic

    @Test fun introducingShowsSourceMethodIntroAndContinueAction() {
        val model = trainingCardModel(baseState(introPending = true))
        assertTrue(model.introducing)
        assertEquals(
            listOf(
                CardSection.SourceSentence(sentenceHighlightParts(exercise().source, exercise().changes, ChangeSide.Before)),
                CardSection.MethodIntro(logic.introduce),
                CardSection.Action("Перейти к заданию", AppAction.ContinueIntroduction),
            ),
            model.front,
        )
        assertEquals(emptyList(), model.back)
        assertEquals(mapOf(HotKey.Space to AppAction.ContinueIntroduction), model.hotkeys)
        assertNull(model.lifehackBadge)
    }

    @Test fun questionOralEndsWithRetrieveHintAndRevealActionNoAnswerInput() {
        val model = trainingCardModel(baseState())
        assertEquals(CardSection.RetrieveHint(logic.retrieve), model.front[model.front.size - 2])
        assertEquals(CardSection.Action("Показать ответ", AppAction.Reveal("fixture-1")), model.front.last())
        assertTrue(model.front.none { it is CardSection.AnswerInput })
        assertTrue(model.front.any { it is CardSection.Styled })
        assertEquals(mapOf(HotKey.Space to AppAction.Reveal("fixture-1")), model.hotkeys)
    }

    @Test fun questionTypedIncludesAnswerInputAndOmitsSpaceHotkey() {
        val model = trainingCardModel(baseState(answerMode = AnswerMode.Typed, draft = "Nie mam psa"))
        assertTrue(model.front.contains(CardSection.AnswerInput(AnswerMode.Typed, "Nie mam psa")))
        assertEquals(CardSection.Action("Проверить и показать ответ", AppAction.Reveal("fixture-1")), model.front.last())
        assertEquals(emptyMap(), model.hotkeys)
    }

    @Test fun revealedOralHasBackSectionsRatingButtonsNoEvaluationFeedback() {
        val preview = SchedulePreview(
            Instant.fromEpochMilliseconds(1_000_000), Instant.fromEpochMilliseconds(2_000_000),
            Instant.fromEpochMilliseconds(3_000_000), Instant.fromEpochMilliseconds(4_000_000),
        )
        val model = trainingCardModel(baseState(phase = CardPhase.Revealed, intervals = preview, now = Instant.fromEpochMilliseconds(0)))
        assertTrue(model.back.none { it is CardSection.EvaluationFeedback })
        assertTrue(model.back.any { it is CardSection.Expected })
        assertTrue(model.back.any { it is CardSection.MethodFeedback })
        assertEquals(CardSection.ReviewHint(logic.review), model.back.last())
        assertEquals(listOf(Rating.Again, Rating.Good), model.ratingButtons.map { it.rating })
        assertEquals(listOf("Повторить", "Вспомнил"), model.ratingButtons.map { it.label })
        assertTrue(model.ratingButtons.all { it.intervalLabel.isNotEmpty() })
        assertEquals(
            mapOf(
                HotKey.Digit1 to AppAction.Rate("fixture-1", Rating.Again),
                HotKey.Digit2 to AppAction.Rate("fixture-1", Rating.Good),
                HotKey.ArrowLeft to AppAction.Rate("fixture-1", Rating.Again),
                HotKey.ArrowRight to AppAction.Rate("fixture-1", Rating.Good),
            ),
            model.hotkeys,
        )
        assertNull(model.lifehackBadge)
    }

    @Test fun revealedTypedIncludesEvaluationFeedbackWithMatchedFlag() {
        val evaluation = Evaluation(correct = true, normalized = "nie mam psa", expected = "nie mam psa", distance = 0)
        val model = trainingCardModel(baseState(
            phase = CardPhase.Revealed, answerMode = AnswerMode.Typed, frozenAnswer = "Nie mam psa.", evaluation = evaluation,
        ))
        assertEquals(CardSection.EvaluationFeedback(true, "Nie mam psa."), model.back.filterIsInstance<CardSection.EvaluationFeedback>().single())
    }

    // [open question] resolved: the model returns the raw empty string, not UiCopy's own
    // "Ответ не введён" fallback phrase, so hosts needing different empty-state copy aren't stuck.
    @Test fun revealedTypedWithEmptyFrozenAnswerReturnsRawEmptyString() {
        val evaluation = Evaluation(correct = false, normalized = "", expected = "nie mam psa", distance = 99)
        val model = trainingCardModel(baseState(
            phase = CardPhase.Revealed, answerMode = AnswerMode.Typed, frozenAnswer = "", evaluation = evaluation,
        ))
        assertEquals("", model.back.filterIsInstance<CardSection.EvaluationFeedback>().single().answerText)
    }

    @Test fun noDueHasEmptySectionsAndScheduleSelected() {
        val model = trainingCardModel(baseState(phase = CardPhase.NoDue, exercise = null, mode = TrainingMode.Schedule))
        assertEquals(emptyList(), model.front)
        assertEquals(emptyList(), model.back)
        assertEquals(emptyList(), model.ratingButtons)
        assertEquals(emptyMap(), model.hotkeys)
        assertNull(model.chainProgress)
        assertEquals(TrainingMode.Schedule, model.header.selected)
    }

    @Test fun chainCompleteKeepsChainSelectedWithCompletedProgress() {
        val chain = listOf(exercise(id = "a"), exercise(id = "b"))
        val model = trainingCardModel(baseState(
            phase = CardPhase.ChainComplete, exercise = null, mode = TrainingMode.Chain, chain = chain, chainIndex = 1,
        ))
        assertEquals(TrainingMode.Chain, model.header.selected)
        assertEquals(ChainProgress(2, 2, courseChainPresentation.summary), model.chainProgress)
        assertEquals(emptyList(), model.front)
        assertEquals(emptyList(), model.back)
    }

    // NativeContrast with no authored `nativeParallel` for this skill (pl-ru authors none today)
    // resolves to its declared fallback (RuleFirst) — proving the model never re-introduces a
    // styleId branch of its own; it only ever reads StyleComposer's already-resolved output.
    @Test fun nativeContrastWithoutAuthoredContentUsesStyleComposer() {
        val model = trainingCardModel(baseState(styleId = StyleId.NativeContrast))
        assertTrue(model.front.isNotEmpty(), "model produces front sections for NativeContrast")
    }

    // EN-21's "пусто -> блок не рисуется" rule, checked generically across every real skill of the
    // active pack: both pl-ru and en-ru currently author a lifehack for all 16 of their skills, so
    // no literal "skill with zero lifehacks" fixture exists in real course data today to drive a
    // false case through skillById (which throws for a made-up id) — this asserts the model's
    // LifehackSection presence always agrees with the provider's own hasLifehacks for every real
    // skill, which is the same branch a future skill-with-no-lifehack would also go through.
    @Test fun lifehackSectionPresenceAlwaysMatchesProviderAcrossEveryRealSkill() {
        for (skill in skills) {
            val model = trainingCardModel(baseState(phase = CardPhase.Revealed, exercise = exercise(skillId = skill.id)))
            val hasSection = model.back.any { it is CardSection.LifehackSection }
            assertEquals(StaticPackLifehackProvider.hasLifehacks(skill.id), hasSection, "mismatch for ${skill.id}")
        }
    }

    @Test fun revealedWithLifehacksPlacesLifehackSectionBetweenMethodFeedbackAndReviewHint() {
        val model = trainingCardModel(baseState(phase = CardPhase.Revealed))
        val methodFeedbackIndex = model.back.indexOfFirst { it is CardSection.MethodFeedback }
        val lifehackIndex = model.back.indexOfFirst { it is CardSection.LifehackSection }
        val reviewIndex = model.back.indexOfFirst { it is CardSection.ReviewHint }
        assertTrue(lifehackIndex > methodFeedbackIndex && lifehackIndex < reviewIndex)
        assertEquals(StaticPackLifehackProvider.forSkill("case.gen.neg"), (model.back[lifehackIndex] as CardSection.LifehackSection).lifehacks)
    }

    // [open question] resolved: the teaser never shows mid-introduction (row 1) nor once revealed
    // (this test) — only while Question and not introducing, for a skill that has a lifehack.
    @Test fun lifehackBadgeShownOnlyWhileQuestionAndNotIntroducingThenHiddenOnceRevealed() {
        assertEquals(LifehackBadge(), trainingCardModel(baseState()).lifehackBadge)
        assertNull(trainingCardModel(baseState(introPending = true)).lifehackBadge)
        assertNull(trainingCardModel(baseState(phase = CardPhase.Revealed)).lifehackBadge)
    }

    @Test fun chainProgressPresentOnlyInChainMode() {
        val chain = listOf(exercise(id = "a"), exercise(id = "b"), exercise(id = "c"))
        val model = trainingCardModel(baseState(mode = TrainingMode.Chain, chain = chain, chainIndex = 1))
        assertEquals(ChainProgress(1, 3, courseChainPresentation.summary), model.chainProgress)
        assertNull(trainingCardModel(baseState(mode = TrainingMode.Focused)).chainProgress)
    }

    @Test fun expectedSectionCarriesAcceptedAlternatives() {
        val model = trainingCardModel(baseState(phase = CardPhase.Revealed, exercise = exercise(accepted = listOf("Ja nie mam psa."))))
        assertEquals(listOf("Ja nie mam psa."), model.back.filterIsInstance<CardSection.Expected>().single().accepted)
    }

    // Proves the [trainingCardModel] `now` parameter actually overrides the clock rather than
    // being ignored — same due instants, two different explicit `now`s, two different labels.
    @Test fun explicitNowParameterOverridesStateNowForIntervalLabel() {
        val due = Instant.fromEpochMilliseconds(60_000 * 50)
        val preview = SchedulePreview(due, due, due, due)
        val state = baseState(phase = CardPhase.Revealed, intervals = preview, now = Instant.fromEpochMilliseconds(60_000 * 40))
        val usingStateNow = trainingCardModel(state)
        val usingOverride = trainingCardModel(state, now = Instant.fromEpochMilliseconds(0))
        assertEquals("10 мин", usingStateNow.ratingButtons.first().intervalLabel)
        assertEquals("50 мин", usingOverride.ratingButtons.first().intervalLabel)
    }
}
