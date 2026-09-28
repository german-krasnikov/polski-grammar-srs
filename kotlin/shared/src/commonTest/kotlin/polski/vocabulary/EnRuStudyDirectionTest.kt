package polski.vocabulary

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import polski.data.selectCoursePack

/**
 * EnRuAcceptance-2026-09-28.md §7 item 4: [StudyDirection] itself was already opened to any
 * pack's pair (EN-09), but nothing yet read the *active* pack's own pair instead of pl-ru's
 * hardcoded [StudyDirection.RussianToPolish]/[StudyDirection.PolishToRussian] — so a vocabulary
 * session still offered/defaulted to `"ru-pl"/"pl-ru"` even while en-ru was the active pack.
 * [activeStudyDirections] and [VocabularySession.start]'s own default now read
 * `packRegistry.active`, generalized from its own `targetLanguage`/`nativeLanguage`, not a
 * hand-written pl literal — never a pack-data change, matching every other pack switch already
 * proved by [polski.data.CoursePackSwitchTest]. Every test that switches the active pack restores
 * it to pl-ru afterwards, pass or fail — the same rule that file documents.
 */
class EnRuStudyDirectionTest {
    private val at = Instant.parse("2026-09-28T12:00:00Z")

    @Test fun activeStudyDirectionsIsPlRuFirstUnchanged() {
        assertEquals(listOf(StudyDirection.RussianToPolish, StudyDirection.PolishToRussian), activeStudyDirections)
    }

    @Test fun activeStudyDirectionsFollowsAnActivePackSwitch() {
        try {
            selectCoursePack("en-ru")
            assertEquals(listOf(StudyDirection("ru-en"), StudyDirection("en-ru")), activeStudyDirections)
        } finally {
            selectCoursePack("pl-ru")
        }
    }

    @Test fun freshSessionDefaultsToTheActivePacksOwnFirstDirection() = runTest {
        try {
            selectCoursePack("en-ru")
            val session = VocabularySession(EmptyRepository(), polski.srs.FsrsScheduler(), { at }, { "user.x" })
            session.start()
            assertEquals(StudyDirection("ru-en"), session.state.value.direction)
        } finally {
            selectCoursePack("pl-ru")
        }
    }

    private class EmptyRepository : VocabularyRepository {
        override suspend fun loadRaw(): String? = null
        override suspend fun saveRaw(value: String, backupCurrent: Boolean) {}
    }
}
