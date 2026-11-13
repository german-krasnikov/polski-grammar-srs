package polski.presentation

import kotlin.time.Instant
import polski.model.Skill
import polski.progress.Progress
import polski.progress.SkillQueue
import polski.srs.Rating
import polski.srs.Scheduler

/** M3 blueprint (`Plans/Architecture/DAILY-SESSION-BLUEPRINT.md`): the kind of one [DailyStep]. */
enum class DailyStepKind { Review, New, Practice }

/** One planner-selected step: which [kind] of session work, for which [skillId]. */
data class DailyStep(val kind: DailyStepKind, val skillId: String)

/** How a rated [DailyStep] turned out, classified by [classifyDailyOutcome]. */
enum class DailyOutcome { Independent, Assisted, Errored }

/** One completed step, recorded into [DailySessionState.history]. */
data class DailyStepOutcome(val kind: DailyStepKind, val skillId: String, val outcome: DailyOutcome)

/** Soft per-kind budget for one planned session; `planned` is the total shown before the
 *  voluntary-continue prompt. Caps are advisory — [DailySessionPlanner] backfills from another
 *  kind when one runs out, so a learner with no due reviews still gets a full session. */
data class DailyMix(val reviews: Int = 4, val new: Int = 1, val practice: Int = 1) {
    val planned: Int get() = reviews + new + practice
}

/** Session-only — never part of [polski.progress.ProgressDocument]; reset on every
 *  [AppAction.StartDaily], same lifetime class as [AppUiState.chain]/[AppUiState.chainIndex]. */
data class DailySessionState(
    val mix: DailyMix = DailyMix(),
    val currentStep: DailyStep? = null,
    val history: List<DailyStepOutcome> = emptyList(),
    val extended: Boolean = false,
) {
    val planned: Int get() = mix.planned
}

/**
 * Pure selection, called by [TrainingStore] only. Priority 1 a due review of an *already
 * reviewed* skill (`reviews > 0` — a brand-new skill's freshly scheduled card is due too,
 * but that is [DailyStepKind.New]'s job, not a review); priority 2 a not-yet-reviewed skill whose
 * [Skill.prerequisites] are all already reviewed; priority 3 the lowest-accuracy already-reviewed
 * skill, meant for [AnswerMode.TileBuilder] practice. Each capped by [mix] for one planned
 * session; once a cap is hit OR a kind has nothing left, the remaining slot backfills from the
 * next kind in the same order. Returns null only when every kind is empty — the fail-soft
 * "nothing to do" case (`Plans/Learning/Plan.md` §M3 "если нет ничего — сообщить").
 */
object DailySessionPlanner {
    fun nextStep(
        progress: Progress,
        scheduler: Scheduler,
        skillQueue: SkillQueue,
        skills: List<Skill>,
        at: Instant,
        completed: List<DailyStepKind>,
        mix: DailyMix = DailyMix(),
    ): DailyStep? {
        val stats = progress.stats
        fun reviewsOf(skillId: String): Int = stats[skillId]?.reviews ?: 0

        fun reviewCandidate(): DailyStep? =
            skillQueue.due(progress.cards, scheduler, at)
                .filter { reviewsOf(it.skillId) > 0 }
                .minByOrNull { it.card.due }
                ?.let { DailyStep(DailyStepKind.Review, it.skillId) }

        fun newCandidate(): DailyStep? =
            skills.firstOrNull { skill ->
                reviewsOf(skill.id) == 0 && skill.prerequisites.all { reviewsOf(it) > 0 }
            }?.let { DailyStep(DailyStepKind.New, it.id) }

        fun practiceCandidate(): DailyStep? =
            skills.filter { reviewsOf(it.id) > 0 }
                .minByOrNull { skill ->
                    val s = stats.getValue(skill.id)
                    s.correct.toDouble() / s.reviews
                }
                ?.let { DailyStep(DailyStepKind.Practice, it.id) }

        val done = DailyStepKind.entries.associateWith { kind -> completed.count { it == kind } }
        val caps = mapOf(DailyStepKind.Review to mix.reviews, DailyStepKind.New to mix.new, DailyStepKind.Practice to mix.practice)
        val order = listOf<Pair<DailyStepKind, () -> DailyStep?>>(
            DailyStepKind.Review to ::reviewCandidate,
            DailyStepKind.New to ::newCandidate,
            DailyStepKind.Practice to ::practiceCandidate,
        )
        for ((kind, candidate) in order) {
            if ((done[kind] ?: 0) >= (caps[kind] ?: 0)) continue
            candidate()?.let { return it }
        }
        return null
    }
}

/** Pure, used by [TrainingStore.rate] right before advancing — never persisted, never changes
 *  FSRS: every Daily rating still calls [polski.progress.ReviewReducer.recordReview] exactly like
 *  Schedule/Focused today, so a Daily review is a real exposure, not a second bookkeeping path
 *  (`Plans/Learning/Plan.md` §M3 "дополнительная практика не маскируется под тест памяти"). A
 *  rating of [Rating.Again] is always [DailyOutcome.Errored]; otherwise the recorded correctness
 *  for [AnswerMode.Typed]/[AnswerMode.TileBuilder] (`false` = the learner needed the shown answer,
 *  i.e. [DailyOutcome.Assisted]) decides, and [AnswerMode.Oral] (no graded correctness signal)
 *  defaults to [DailyOutcome.Independent] on any non-[Rating.Again] rating. */
fun classifyDailyOutcome(current: AppUiState, rating: Rating): DailyOutcome {
    if (rating == Rating.Again) return DailyOutcome.Errored
    val correctness = when (current.answerMode) {
        AnswerMode.Typed -> current.evaluation?.correct
        AnswerMode.TileBuilder -> current.tileBuilder?.result?.correct
        AnswerMode.Oral -> null
    }
    return if (correctness == false) DailyOutcome.Assisted else DailyOutcome.Independent
}
