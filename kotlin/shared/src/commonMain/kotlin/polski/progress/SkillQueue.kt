package polski.progress

import kotlin.time.Instant
import polski.srs.Scheduler
import polski.srs.StoredCard

/**
 * Selects which stored cards the active pack may turn into a due exercise.
 *
 * UniversalCorePlan.md §6: a saved [ProgressDocument] can retain a card for a skillId the active
 * pack no longer knows (a renamed/removed skill today, a foreign pack's skillId once multiple
 * packs coexist). Before this existed, [polski.presentation.TrainingStore] picked the earliest-due
 * card from *all* stored cards and handed its skillId straight to `ExerciseFactory`, which throws
 * on any id it does not recognize — one stray card crashed the whole session. Filtering by
 * [activePackFilter] before every due/next-due computation keeps a foreign card visible only as
 * inert data (round-tripped unchanged on save) and never as something the UI tries to present.
 */
class SkillQueue(private val activePackFilter: (String) -> Boolean) {
    /** [cards] restricted to the active pack and due at [at], per [scheduler]. */
    fun due(cards: List<StoredCard>, scheduler: Scheduler, at: Instant): List<StoredCard> =
        cards.filter { activePackFilter(it.skillId) && scheduler.isDue(it, at) }

    /** The active pack's earliest-due card among [due], or null if none is due. */
    fun next(cards: List<StoredCard>, scheduler: Scheduler, at: Instant): StoredCard? =
        due(cards, scheduler, at).minByOrNull { it.card.due }

    /** The active pack's earliest still-future due instant among [cards], for "next due" display. */
    fun nextDueAt(cards: List<StoredCard>, at: Instant): Instant? =
        cards.filter { activePackFilter(it.skillId) && it.card.due > at }.minOfOrNull { it.card.due }
}
