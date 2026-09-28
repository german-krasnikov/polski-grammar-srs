package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

/**
 * EN-22 (Plans/Kotlin/EnRuPackPlan.md §6): [availableCoursePacks] is what a host's target/native
 * picker lists; [selectCoursePack] is what it calls on selection. Only pl-ru is safely registered
 * today — wiring en-ru in throws `IllegalStateException: Missing course field gender` from
 * [CoursePack.nouns] (see `packRegistry`'s own KDoc for the real, separate schema gap this
 * surfaced) — so this pins today's honest single-pack reality, not a second pack that would crash.
 */
class CoursePackSwitchTest {
    @Test
    fun availablePacksListsPlRuWithItsRealLanguages() {
        assertEquals(listOf(CoursePackOption("pl-ru", target = "pl", native = "ru")), availableCoursePacks)
    }

    @Test
    fun plRuIsTheActivePackWithoutAnySelection() {
        assertEquals("pl-ru", activeCoursePackId)
    }

    @Test
    fun selectingAnUnregisteredPairIdThrowsInsteadOfSilentlySwitching() {
        assertFails { selectCoursePack("en-ru") }
        assertEquals("pl-ru", activeCoursePackId, "a failed select must not have changed the active pack")
    }

    @Test
    fun selectingThePackAlreadyActiveIsANoOp() {
        selectCoursePack("pl-ru")
        assertEquals("pl-ru", activeCoursePackId)
    }
}
