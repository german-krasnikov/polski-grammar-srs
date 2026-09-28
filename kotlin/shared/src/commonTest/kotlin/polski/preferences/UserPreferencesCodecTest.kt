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
        for (raw in listOf("{broken", "{\"schemaVersion\":5}", "{\"schemaVersion\":\"1\"}", "{\"schemaVersion\":1,\"future\":true}", "{\"schemaVersion\":1,\"glassTintPercent\":30}", "{\"schemaVersion\":2,\"future\":true}", "{\"schemaVersion\":3,\"future\":true}", "{\"schemaVersion\":4,\"future\":true}", "{\"schemaVersion\":1,\"appearance\":\"Blue\"}", "{\"schemaVersion\":1,\"swipeRatingEnabled\":\"true\"}", "{\"schemaVersion\":1,\"reminder\":{\"days\":[8]}}")) {
            val result = assertIs<PreferencesDecode.RecoveryRequired>(UserPreferencesCodec.decode(raw))
            assertEquals(raw, result.raw)
            assertTrue(result.reason.isNotBlank())
        }
    }

    // EN-08 (Plans/Kotlin/EnRuPackPlan.md §6): `coursePair: String` -> `CourseSelection(target, native, style)`.
    @Test fun courseSelectionGroupsTargetNativeAndStyle() {
        val value = UserPreferencesV2(styleId = PreferredStyle.NativeContrast)
        assertEquals(CourseSelection("pl", "ru", PreferredStyle.NativeContrast), value.course)
    }

    @Test fun legacyCoursePairSplitsIntoTargetAndNative() {
        val loaded = assertIs<PreferencesDecode.Loaded>(
            UserPreferencesCodec.decode("""{"schemaVersion":3,"coursePair":"pl-ru","styleId":"RuleFirst"}""")
        ).value
        assertEquals("pl", loaded.target)
        assertEquals("ru", loaded.native)
    }

    // Golden: an existing default-shaped pl-ru v3 document (React export or KMP) round-trips
    // byte-identically through the new codec — the wire format itself has not changed.
    @Test fun defaultV3DocumentIsGoldenByteIdentical() {
        val golden = "{\"schemaVersion\":3,\"coursePair\":\"pl-ru\",\"styleId\":\"RuleFirst\",\"answerMode\":\"Oral\"," +
            "\"appearance\":\"System\",\"motion\":\"System\",\"swipeRatingEnabled\":true,\"glassTintPercent\":50," +
            "\"animationsEnabled\":true,\"reminder\":{\"enabled\":false,\"localTime\":\"19:00\",\"days\":[1,2,3,4,5,6,7]," +
            "\"quietStart\":\"22:00\",\"quietEnd\":\"08:00\"}}"
        assertEquals(golden, UserPreferencesCodec.encode(UserPreferencesV2()))
        assertEquals(UserPreferencesV2(), assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode(golden)).value)
    }

    @Test fun v1AndV2CoursePairDefaultingStillWorks() {
        assertEquals("pl", assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode("""{"schemaVersion":1}""")).value.target)
        val v2 = """{"schemaVersion":2,"coursePair":"pl-ru"}"""
        assertEquals("ru", assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode(v2)).value.native)
    }

    @Test fun v4DecodesExplicitTargetAndNativeTolerantly() {
        val v4 = """{"schemaVersion":4,"target":"pl","native":"ru","styleId":"MinimalTheory"}"""
        val loaded = assertIs<PreferencesDecode.Loaded>(UserPreferencesCodec.decode(v4)).value
        assertEquals(CourseSelection("pl", "ru", PreferredStyle.MinimalTheory), loaded.course)
        // encode() still only ever writes v3 (no host has a second pack to pick yet).
        assertEquals(3, loaded.schemaVersion)
    }

    @Test fun mismatchedCoursePairOrTargetNativeIsRecoveryRequired() {
        for (raw in listOf(
            """{"schemaVersion":3,"coursePair":"en-ru"}""",
            """{"schemaVersion":3,"coursePair":"pl-en"}""",
            """{"schemaVersion":3,"coursePair":"pl"}""",
            """{"schemaVersion":4,"target":"en","native":"ru"}""",
        )) {
            assertIs<PreferencesDecode.RecoveryRequired>(UserPreferencesCodec.decode(raw))
        }
    }

    // EN-22: a persisted target/native switch (e.g. en-ru) makes decode() itself return
    // RecoveryRequired (the test right above this one) until the active pack really is en-ru —
    // peekTargetNative is the one entrypoint a host's cold start can call first, before it selects
    // that pack, to know which one to select. It skips every other field's validation on purpose.
    @Test fun peekTargetNativeReadsBothWireShapesWithoutValidatingAgainstTheActivePack() {
        assertEquals("pl" to "ru", UserPreferencesCodec.peekTargetNative("""{"schemaVersion":3,"coursePair":"pl-ru"}"""))
        assertEquals("en" to "ru", UserPreferencesCodec.peekTargetNative("""{"schemaVersion":3,"coursePair":"en-ru"}"""))
        assertEquals("en" to "ru", UserPreferencesCodec.peekTargetNative("""{"schemaVersion":4,"target":"en","native":"ru"}"""))
    }

    @Test fun peekTargetNativeIsNullForAnyMissingOrMalformedDocument() {
        assertEquals(null, UserPreferencesCodec.peekTargetNative("not json"))
        assertEquals(null, UserPreferencesCodec.peekTargetNative("""{"schemaVersion":1}"""))
        assertEquals(null, UserPreferencesCodec.peekTargetNative("""{"schemaVersion":1,"coursePair":"pl"}"""))
        assertEquals(null, UserPreferencesCodec.peekTargetNative("""{"schemaVersion":4,"target":"en"}"""))
        assertEquals(null, UserPreferencesCodec.peekTargetNative("""{}"""))
    }
}
