// Generated from dsl/feature/appearance-settings.feature
// source-sha256=7b64f7be0ceadcb151f5a5cbf9d1957399903ebdfec1f231152d555b180940c4
// Edit the source, then run biome generate.

package polski.generated.settings.appearance

import java.io.Closeable

interface AppearanceSettingsPorts {
    fun setAppearance(appearance: AppearanceSettingsAppearance)
}

data class AppearanceSettingsState(val appearance: AppearanceSettingsAppearance)

object AppearanceSettingsDefaults {
    val value = AppearanceSettingsState(AppearanceSettingsAppearance.System)
}


/** Implemented by the project: a thin binding of the generated Logic Policy (biome logic --kotlin) or a native port; the shared examples check it. */
interface AppearanceSettingsDecisions {

}



/** Owns the state. Only the declared actions change it; every change notifies the subscribers. */
class AppearanceSettingsModel(private val io: AppearanceSettingsPorts, private val decisions: AppearanceSettingsDecisions) {
    var state: AppearanceSettingsState = AppearanceSettingsDefaults.value
        private set
    private val listeners = LinkedHashSet<() -> Unit>()


    fun subscribe(listener: () -> Unit): Closeable {
        listeners += listener
        return Closeable { listeners -= listener }
    }

    fun choose(value: AppearanceSettingsAppearance) {
        val s = state
        commit(s.copy(appearance = value)) { next -> io.setAppearance(next.appearance) }
    }

    private fun commit(next: AppearanceSettingsState, calls: (AppearanceSettingsState) -> Unit) {
        val prev = state
        if (prev != next) {
            state = next
            listeners.toList().forEach { it() }
        }
        calls(next)

    }
}
