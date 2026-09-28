package polski.preferences

import polski.data.packRegistry

/** UC-10: a display preference, not a diagnosis — задания, FSRS и оценки одинаковы во всех стилях. */
enum class PreferredStyle { RuleFirst, SituationFirst, NativeContrast, MinimalTheory }
enum class PreferredAnswerMode { Oral, Typed }
enum class Appearance { System, Light, Dark }
enum class Motion { System, Reduced }

data class ReminderPreferences(
    val enabled: Boolean = false,
    val localTime: String = "19:00",
    val days: List<Int> = (1..7).toList(),
    val quietStart: String = "22:00",
    val quietEnd: String = "08:00",
)

/**
 * EN-08 (gap G): what to study, as its own value — [target]/[native] language ids plus the
 * presentation [style], for the three pickers (`Plans/Kotlin/EnRuPackPlan.md`§6) to grow into.
 * A pure read view over [UserPreferencesV2]; [style] mirrors [UserPreferencesV2.styleId] rather
 * than replacing it, since that field's name and shape are still read directly by every host.
 */
data class CourseSelection(val target: String, val native: String, val style: PreferredStyle)

/**
 * Portable preferences only. Progress, FSRS cards, and host permission state live elsewhere.
 * The class name predates schema v3 (UC-10 renamed `explanationMethod` to [styleId]); it is kept
 * to avoid an unrelated rename across every host adapter — [schemaVersion] is the real version.
 */
data class UserPreferencesV2(
    val schemaVersion: Int = 3,
    /** UC-04/EN-08: was the flat `"pl-ru"` [polski.data.CoursePack.pairId] string (`coursePair`);
     * split into its two halves so a future second pack has real target/native pickers to bind
     * to, not a delimiter-joined id. Defaults to the active pack's own languages. */
    val target: String = packRegistry.active.targetLanguage,
    val native: String = packRegistry.active.nativeLanguage,
    val styleId: PreferredStyle = PreferredStyle.RuleFirst,
    val answerMode: PreferredAnswerMode = PreferredAnswerMode.Oral,
    val appearance: Appearance = Appearance.System,
    val motion: Motion = Motion.System,
    val swipeRatingEnabled: Boolean = true,
    val reminder: ReminderPreferences = ReminderPreferences(),
    /** App-level web/Android tint control; Apple hosts retain it for portable JSON only. */
    val glassTintPercent: Int = 50,
    /** Master motion switch (flip/expand-reveal transitions and all Rive effects). A missing
     *  field on decode means enabled — see [UserPreferencesCodec]. Off keeps Rive entirely
     *  unloaded (no prewarm, no network requests) and all remaining CSS motion instant. */
    val animationsEnabled: Boolean = true,
) {
    /** [target]/[native]/[styleId] as one [CourseSelection], for the picker code in EN-22. */
    val course: CourseSelection get() = CourseSelection(target, native, styleId)
}

/** Source compatibility for native hosts while their preferences boundary migrates to v2. */
typealias UserPreferencesV1 = UserPreferencesV2
