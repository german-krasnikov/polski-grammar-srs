package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

/**
 * EN-22 (Plans/Kotlin/EnRuPackPlan.md §6): [availableCoursePacks] is what a host's target/native
 * picker lists; [selectCoursePack] is what it calls on selection. EnRuAcceptance §7 item 1
 * generalized [CoursePack] enough that en-ru now passes [usableCourseSelections]'s probe too, so
 * both registered packs are real, selectable options — restored to pl-ru at the end of every test
 * that switches away, since [packRegistry] is a process-wide singleton shared with every other
 * test in this binary.
 */
class CoursePackSwitchTest {
    @Test
    fun availablePacksListPlRuFirstThenEnRu() {
        assertEquals(
            listOf(CoursePackOption("pl-ru", target = "pl", native = "ru"), CoursePackOption("en-ru", target = "en", native = "ru")),
            availableCoursePacks,
        )
    }

    @Test
    fun plRuIsTheActivePackWithoutAnySelection() {
        assertEquals("pl-ru", activeCoursePackId)
    }

    @Test
    fun selectingAnUnregisteredPairIdThrowsInsteadOfSilentlySwitching() {
        assertFails { selectCoursePack("de-ru") }
        assertEquals("pl-ru", activeCoursePackId, "a failed select must not have changed the active pack")
    }

    @Test
    fun selectingThePackAlreadyActiveIsANoOp() {
        selectCoursePack("pl-ru")
        assertEquals("pl-ru", activeCoursePackId)
    }

    @Test
    fun selectingEnRuSwitchesTheActivePack() {
        try {
            selectCoursePack("en-ru")
            assertEquals("en-ru", activeCoursePackId)
        } finally {
            selectCoursePack("pl-ru")
        }
    }
}
