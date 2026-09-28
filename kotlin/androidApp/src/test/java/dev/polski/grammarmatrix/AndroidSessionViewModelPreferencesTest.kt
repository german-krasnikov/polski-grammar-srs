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
import org.robolectric.Shadows.shadowOf
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

    // EN-22: selecting the pack the app is already on (pl-ru, the default) persists nothing and
    // never calls restart — same-pack is a no-op exactly like every other picker here (persistStyle
    // above), not a needless relaunch.
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

    // EnRuAcceptance-2026-09-28.md §7 item 2 (Android lane): en-ru now passes usableCourseSelections
    // (core generalized CoursePack's schema), so a persisted target=en/native=ru cold-starts by
    // actually selecting en-ru — but courses/lang/en/forms.generated.json only has verb forms (a
    // content gap outside this lane's allowed paths; see AI/decisions.md ADR-37), so
    // TrainingStore's unconditional `initialChain = exerciseEngine.generateChain()` throws while
    // building any en-ru exercise chain. Before a fix this crashes AndroidSessionViewModel's own
    // eager `store` field at cold start, and crash-loops forever (every restart re-reads the same
    // persisted target=en). The session must degrade the same way MacSession/IosSession already do
    // for the identical gap (ADR-37/38): catch the failure, roll back the active pack to one that
    // actually builds, and self-correct the persisted document so the next cold start doesn't
    // retry the broken pack.
    @Test fun aPersistedTargetThatFailsToBuildATrainingChainRollsBackInsteadOfCrashing() = runBlocking {
        val context = isolatedContext()
        AndroidUserPreferencesStore(context).save(UserPreferencesV2(target = "en", native = "ru"))
        val session = AndroidSessionViewModel(context)
        assertEquals("pl", polski.data.activeCoursePackId.substringBefore("-"))
        assertEquals("pl", session.preferences.target)
        assertEquals("ru", session.preferences.native)
        assertEquals(true, session.store.state.value.chain.isNotEmpty())
        // Pumps the cold-start `preferencesStore.load()` coroutine (Main dispatcher, launched in
        // the ViewModel's own init block) so its self-correcting save actually runs in this test —
        // repeatedly, since load()'s own IO hop resumes back onto Main asynchronously.
        awaitSaved(context, pumpMainLooper = true) { it.target == "pl" && it.native == "ru" }
    }

    /** The save happens on a background dispatcher launched by the ViewModel, so poll for it like
     *  the file it writes to. [pumpMainLooper] also idles Robolectric's main looper on every
     *  retry, for a save chained off a `viewModelScope.launch` coroutine that started during
     *  construction (its own IO hop resumes back onto Main asynchronously, unlike a save launched
     *  by a later, in-test method call). */
    private suspend fun awaitSaved(context: Context, pumpMainLooper: Boolean = false, matches: (UserPreferencesV2) -> Boolean) {
        withTimeout(2_000) {
            var loaded = (AndroidUserPreferencesStore(context).load() as? PreferencesLoad.Loaded)?.value
            while (loaded == null || !matches(loaded)) {
                if (pumpMainLooper) shadowOf(android.os.Looper.getMainLooper()).idle()
                delay(20)
                loaded = (AndroidUserPreferencesStore(context).load() as? PreferencesLoad.Loaded)?.value
            }
        }
    }
}
