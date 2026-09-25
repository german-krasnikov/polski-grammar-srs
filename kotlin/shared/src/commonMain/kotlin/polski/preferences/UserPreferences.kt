package polski.preferences

enum class PreferredMethod { Logic, Situations }
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

/** Portable preferences only. Progress, FSRS cards, and host permission state live elsewhere. */
data class UserPreferencesV2(
    val schemaVersion: Int = 2,
    val coursePair: String = "pl-ru",
    val explanationMethod: PreferredMethod = PreferredMethod.Logic,
    val answerMode: PreferredAnswerMode = PreferredAnswerMode.Oral,
    val appearance: Appearance = Appearance.System,
    val motion: Motion = Motion.System,
    val swipeRatingEnabled: Boolean = true,
    val reminder: ReminderPreferences = ReminderPreferences(),
    /** App-level web/Android tint control; Apple hosts retain it for portable JSON only. */
    val glassTintPercent: Int = 50,
)

/** Source compatibility for native hosts while their preferences boundary migrates to v2. */
typealias UserPreferencesV1 = UserPreferencesV2
