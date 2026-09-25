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
