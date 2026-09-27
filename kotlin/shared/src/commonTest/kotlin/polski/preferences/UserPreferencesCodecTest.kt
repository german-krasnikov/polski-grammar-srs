package polski.preferences

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UserPreferencesCodecTest {
    @Test fun defaultsAndExplicitChoicesRoundTrip() {
        val chosen = UserPreferencesV2(
            styleId = PreferredStyle.SituationFirst,
            answerMode = PreferredAnswerMode.Typed,
            appearance = Appearance.Dark,
            motion = Motion.Reduced,
            swipeRatingEnabled = false,
            reminder = ReminderPreferences(enabled = true, localTime = "17:30", days = listOf(1, 3, 5)),
            glassTintPercent = 100,
            animationsEnabled = false,
        )
        assertEquals(chosen, assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode(UserPreferencesCodec.encode(chosen))).value)
        assertEquals(UserPreferencesV2(), assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode("{\"schemaVersion\":1}")).value)
        assertEquals(3, assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode(UserPreferencesCodec.encode(chosen))).value.schemaVersion)
    }

    // ST-07: v1/v2 `explanationMethod: "Logic"/"Situations"` decodes to `styleId: RuleFirst/SituationFirst`;
    // v3 reads `styleId` directly with all 4 names; an unknown value never crashes, never silently defaults.
    @Test fun styleIdTolerantDecodeAcrossSchemaVersions() {
        assertEquals(PreferredStyle.RuleFirst, assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode("""{"schemaVersion":1,"explanationMethod":"Logic"}""")).value.styleId)
        assertEquals(PreferredStyle.SituationFirst, assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode("""{"schemaVersion":2,"explanationMethod":"Situations"}""")).value.styleId)
        assertEquals(PreferredStyle.RuleFirst, assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode("""{"schemaVersion":2}""")).value.styleId)
        for (style in PreferredStyle.entries) {
            val raw = """{"schemaVersion":3,"styleId":"${style.name}"}"""
            assertEquals(style, assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode(raw)).value.styleId)
        }
        assertIs<PreferencesDecode.RecoveryRequired>(UserPreferencesCodec.decode("""{"schemaVersion":1,"explanationMethod":"NativeContrast"}"""))
        assertIs<PreferencesDecode.RecoveryRequired>(UserPreferencesCodec.decode("""{"schemaVersion":3,"styleId":"Logic"}"""))
        assertIs<PreferencesDecode.RecoveryRequired>(UserPreferencesCodec.decode("""{"schemaVersion":3,"explanationMethod":"Logic"}"""))
        val encoded = UserPreferencesCodec.encode(UserPreferencesV2(styleId = PreferredStyle.MinimalTheory))
        assertTrue(encoded.contains("\"styleId\":\"MinimalTheory\""))
        assertTrue(!encoded.contains("explanationMethod"))
    }

    @Test fun animationsEnabledMissingMeansEnabled() {
        assertEquals(true, UserPreferencesV2().animationsEnabled)
        val v2WithoutField = """{"schemaVersion":2,"coursePair":"pl-ru"}"""
        assertEquals(true, assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode(v2WithoutField)).value.animationsEnabled)
        val v1 = """{"schemaVersion":1}"""
        assertEquals(true, assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode(v1)).value.animationsEnabled)
        val off = UserPreferencesV2(animationsEnabled = false)
        assertEquals(false, assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode(UserPreferencesCodec.encode(off))).value.animationsEnabled)
        val invalid = """{"schemaVersion":2,"animationsEnabled":"nope"}"""
        assertIs<PreferencesDecode.RecoveryRequired>(UserPreferencesCodec.decode(invalid))
    }

    @Test fun legacyV1MigratesWithoutLosingSettingsAndExportsV2() {
        val legacy = """{"schemaVersion":1,"appearance":"Dark","answerMode":"Typed","reminder":{"days":[1,3]}}"""
        val loaded = assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode(legacy)).value
        assertEquals(Appearance.Dark, loaded.appearance)
        assertEquals(PreferredAnswerMode.Typed, loaded.answerMode)
        assertEquals(listOf(1, 3), loaded.reminder.days)
        assertEquals(50, loaded.glassTintPercent)
        assertEquals(loaded, assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode(UserPreferencesCodec.encode(loaded))).value)
    }

    @Test fun tintEndpointsAndInvalidValues() {
        for (percent in listOf(0, 100)) {
            val value = UserPreferencesV2(glassTintPercent = percent)
            assertEquals(value, assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode(UserPreferencesCodec.encode(value))).value)
        }
        for (literal in listOf("-1", "101", "50.5", "\"50\"", "null")) {
            val raw = """{"schemaVersion":2,"glassTintPercent":$literal}"""
            assertEquals(raw, assertIs<PreferencesDecode.RecoveryRequired>(UserPreferencesCodec.decode(raw)).raw)
        }
    }

    @Test fun futureAndMalformedDocumentsRetainOriginalBytes() {
        for (raw in listOf("{broken", "{\"schemaVersion\":4}", "{\"schemaVersion\":\"1\"}", "{\"schemaVersion\":1,\"future\":true}", "{\"schemaVersion\":1,\"glassTintPercent\":30}", "{\"schemaVersion\":2,\"future\":true}", "{\"schemaVersion\":3,\"future\":true}", "{\"schemaVersion\":1,\"appearance\":\"Blue\"}", "{\"schemaVersion\":1,\"swipeRatingEnabled\":\"true\"}", "{\"schemaVersion\":1,\"reminder\":{\"days\":[8]}}")) {
            val result = assertIs<PreferencesDecode.RecoveryRequired>(UserPreferencesCodec.decode(raw))
            assertEquals(raw, result.raw)
            assertTrue(result.reason.isNotBlank())
        }
    }
}
