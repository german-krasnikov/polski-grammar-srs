package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * EN-22 (Plans/Kotlin/EnRuPackPlan.md §6): the production [packRegistry] must actually contain
 * the en-ru pack (reconstructed from its v2 `lang/en/lexicon.json` + `pairs/en-ru/pair.json`
 * layers by [CoursePackLoader], the same way [CoursePackLoaderTest] already proves for pl-ru) so
 * a host's target/native picker has a second real pack to name — before this task, [packRegistry]
 * embedded only [embeddedCoursePackSources] (v1 `course.json` packs), and en-ru has none.
 * [selectActiveCoursePack]/[availableCourseSelections]/[usableCourseSelections] are the public
 * entrypoints every host (EN-22) calls; [packRegistry]/[PackRegistry]/[CoursePack] stay
 * `internal` to `:shared`.
 *
 * None of these tests mutate [packRegistry] (the one real process-wide singleton, unlike
 * [PackRegistryTest]'s isolated fixtures) — [selectActiveCoursePack] never actually switches to
 * en-ru today, since [usableCourseSelections] correctly refuses it (see that val's own KDoc for
 * why: [CoursePack]'s pl-shaped schema, not missing en-ru content).
 */
class EnRuPackSwitchTest {
    @Test fun productionRegistryEmbedsBothPacksWithPlRuDefaultAndFirst() {
        assertEquals("pl-ru", packRegistry.active.pairId)
        assertEquals(listOf("pl" to "ru", "en" to "ru"), availableCourseSelections)
    }

    @Test fun enRuIsEmbeddedButNotYetSafeToMakeActive() {
        // CoursePack's v1 schema requires grammatical gender/case English genuinely doesn't have
        // (Noun.gender, case-keyed possessiveForms/futureAuxiliary/reference rows) — a real schema
        // gap, not missing en-ru content; this stays red until that schema is generalized.
        assertEquals(listOf("pl" to "ru"), usableCourseSelections)
    }

    @Test fun selectingEnRuIsANoOpUntilItPassesTheUsabilityProbe() {
        selectActiveCoursePack("en-ru")
        assertEquals("pl-ru", packRegistry.active.pairId)
    }

    @Test fun selectingAnUnknownPairIdIsANoOpNotACrash() {
        selectActiveCoursePack("xx-yy")
        assertEquals("pl-ru", packRegistry.active.pairId)
    }

    @Test fun selectingTheAlreadyActivePairIdIsANoOp() {
        selectActiveCoursePack("pl-ru")
        assertEquals("pl-ru", packRegistry.active.pairId)
    }
}
