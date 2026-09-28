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
 * Every test that actually switches [packRegistry] (the one real process-wide singleton, unlike
 * [PackRegistryTest]'s isolated fixtures) restores it to pl-ru afterwards, pass or fail, so it
 * never leaks into another test sharing this binary.
 */
class EnRuPackSwitchTest {
    @Test fun productionRegistryEmbedsBothPacksWithPlRuDefaultAndFirst() {
        assertEquals("pl-ru", packRegistry.active.pairId)
        assertEquals(listOf("pl" to "ru", "en" to "ru"), availableCourseSelections)
    }

    /**
     * EnRuAcceptance §7 item 1: [CoursePack]'s v1 schema no longer hardcodes pl's grammatical
     * gender/case onto every pack (`Noun.gender`, case-keyed `possessiveForms`/`futureAuxiliary`/
     * reference rows are each derived from what the pack's own JSON declares) — en-ru's genuinely
     * caseless/genderless data now parses completely too.
     */
    @Test fun enRuIsNowSafeToMakeActive() {
        assertEquals(listOf("pl" to "ru", "en" to "ru"), usableCourseSelections)
    }

    @Test fun selectingEnRuActuallySwitchesTheActivePack() {
        try {
            selectActiveCoursePack("en-ru")
            assertEquals("en-ru", packRegistry.active.pairId)
        } finally {
            selectActiveCoursePack("pl-ru")
        }
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
