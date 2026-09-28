package dev.polski.grammarmatrix

import android.content.Context
import android.content.ContextWrapper
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import polski.preferences.Motion
import polski.preferences.PreferencesLoad
import polski.preferences.UserPreferencesV2
import polski.presentation.StyleId

/** M9: Android settings gained Motion and swipe-rating toggles bound to the shared preferences document. */
@RunWith(RobolectricTestRunner::class)
class AndroidSessionViewModelPreferencesTest {
    @get:Rule val temp = TemporaryFolder()

    private fun isolatedContext(): Context {
        val base = RuntimeEnvironment.getApplication()
        base.getSharedPreferences("polski-preferences", Context.MODE_PRIVATE).edit().clear().commit()
        return object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getFilesDir(): File = temp.root
        }
    }

    @Test fun settingMotionReflectsImmediatelyAndPersistsOffMainThread() = runBlocking {
        val context = isolatedContext()
        val session = AndroidSessionViewModel(context)
        session.setMotion(Motion.Reduced)
        assertEquals(Motion.Reduced, session.preferences.motion)
        awaitSaved(context) { it.motion == Motion.Reduced }
    }

    @Test fun disablingSwipeRatingReflectsImmediatelyAndPersistsOffMainThread() = runBlocking {
        val context = isolatedContext()
        val session = AndroidSessionViewModel(context)
        assertEquals(true, session.preferences.swipeRatingEnabled)
        session.setSwipeRatingEnabled(false)
        assertEquals(false, session.preferences.swipeRatingEnabled)
        awaitSaved(context) { !it.swipeRatingEnabled }
    }

    // UC-01 correction: StyleId is open (any StyleRegistry recipe id), but PreferredStyle stays the
    // closed 4-value enum — persistStyle must not crash when StyleRegistry loads a 5th recipe
    // whose id PreferredStyle doesn't know, since AppUiState.styleId (and this LaunchedEffect
    // call) accepts any StyleId regardless of which picker is on screen.
    @Test fun persistingAnUnknownStyleIdDoesNotCrashAndLeavesPreferencesUnchanged() = runBlocking {
        val context = isolatedContext()
        val session = AndroidSessionViewModel(context)
        val before = session.preferences.styleId
        session.persistStyle(StyleId("FutureStyle"))
        assertEquals(before, session.preferences.styleId)
    }

    // EN-22: selecting the pack the app is already on (pl-ru, the only usable one today —
    // usableCourseSelections filters en-ru out until CoursePack's schema is generalized, its own
    // shared-module test) persists nothing and never calls restart — same-pack is a no-op exactly
    // like every other picker here (persistStyle above), not a needless relaunch.
    @Test fun selectingTheAlreadyActiveCourseNeitherPersistsNorRestarts() = runBlocking {
        val context = isolatedContext()
        val session = AndroidSessionViewModel(context)
        var restarted = false
        session.persistCourseSelectionAndRestart("pl", "ru") { restarted = true }
        assertEquals(false, restarted)
    }

    // Cold start reads a persisted target/native (via peekTargetNative) before touching any
    // course-pack-derived global — this proves that read doesn't crash/misbehave for the ordinary
    // case (nothing persisted yet, a fresh install) and the session still boots to pl-ru.
    @Test fun aFreshInstallWithNoPersistedCourseStillBootsToPlRu() = runBlocking {
        val context = isolatedContext()
        val session = AndroidSessionViewModel(context)
        assertEquals("pl", session.preferences.target)
        assertEquals("ru", session.preferences.native)
    }

    /** The save happens on a background dispatcher launched by the ViewModel, so poll for it like the file it writes to. */
    private suspend fun awaitSaved(context: Context, matches: (UserPreferencesV2) -> Boolean) {
        withTimeout(2_000) {
            var loaded = (AndroidUserPreferencesStore(context).load() as? PreferencesLoad.Loaded)?.value
            while (loaded == null || !matches(loaded)) {
                delay(20)
                loaded = (AndroidUserPreferencesStore(context).load() as? PreferencesLoad.Loaded)?.value
            }
        }
    }
}
