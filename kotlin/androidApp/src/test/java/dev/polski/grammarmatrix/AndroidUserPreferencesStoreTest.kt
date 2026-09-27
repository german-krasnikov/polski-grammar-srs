package dev.polski.grammarmatrix

import android.content.Context
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import polski.preferences.PreferencesLoad
import polski.preferences.PreferencesSave
import polski.preferences.UserPreferencesCodec
import polski.preferences.UserPreferencesV2
import polski.preferences.Appearance

@RunWith(RobolectricTestRunner::class)
class AndroidUserPreferencesStoreTest {
    private lateinit var context: Context

    @Before fun clear() {
        context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("polski-preferences", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun tintPersistsThroughV2Codec() = runBlocking {
        val repository = AndroidUserPreferencesStore(context)
        val value = (repository.load() as PreferencesLoad.Loaded).value.copy(glassTintPercent = 83)
        assertEquals(PreferencesSave.Saved, repository.save(value))
        assertEquals(83, (AndroidUserPreferencesStore(context).load() as PreferencesLoad.Loaded).value.glassTintPercent)
    }

    @Test fun changingAppearancePreservesLegacyTint() = runBlocking {
        val repository = AndroidUserPreferencesStore(context)
        val original = (repository.load() as PreferencesLoad.Loaded).value.copy(glassTintPercent = 83)
        assertEquals(PreferencesSave.Saved, repository.save(original))
        assertEquals(PreferencesSave.Saved, repository.save(original.copy(appearance = Appearance.Dark)))
        val restored = (AndroidUserPreferencesStore(context).load() as PreferencesLoad.Loaded).value
        assertEquals(Appearance.Dark, restored.appearance)
        assertEquals(83, restored.glassTintPercent)
    }

    @Test fun malformedPreferencesRemainAvailableForRecovery() = runBlocking {
        val raw = "{\"schemaVersion\":2,\"glassTintPercent\":101}"
        context.getSharedPreferences("polski-preferences", Context.MODE_PRIVATE).edit()
            .putString("document", raw).commit()
        val repository = AndroidUserPreferencesStore(context)
        assertTrue(repository.load() is PreferencesLoad.RecoveryRequired)
        assertTrue(repository.save(UserPreferencesV2()) is PreferencesSave.WriteFailed)
        assertEquals(raw, repository.recoveryRaw())
    }

    @Test fun legacyMethodMigratesIntoVersionedDocument() = runBlocking {
        context.getSharedPreferences("polski-preferences", Context.MODE_PRIVATE).edit()
            .putString("explanationMethod", "Situations").commit()
        val repository = AndroidUserPreferencesStore(context)
        val loaded = (repository.load() as PreferencesLoad.Loaded).value
        assertEquals("SituationFirst", loaded.styleId.name)
        assertTrue(UserPreferencesCodec.encode(loaded).contains("\"schemaVersion\":3"))
    }

    @Test fun v1DocumentMigratesBeforeTintChange() = runBlocking {
        context.getSharedPreferences("polski-preferences", Context.MODE_PRIVATE).edit()
            .putString("document", "{\"schemaVersion\":1,\"appearance\":\"Dark\"}").commit()
        val repository = AndroidUserPreferencesStore(context)
        val loaded = (repository.load() as PreferencesLoad.Loaded).value
        assertEquals("Dark", loaded.appearance.name)
        assertEquals(50, loaded.glassTintPercent)
        assertEquals(PreferencesSave.Saved, repository.save(loaded.copy(glassTintPercent = 10)))
        val restored = (AndroidUserPreferencesStore(context).load() as PreferencesLoad.Loaded).value
        assertEquals("Dark", restored.appearance.name)
        assertEquals(10, restored.glassTintPercent)
    }
}
