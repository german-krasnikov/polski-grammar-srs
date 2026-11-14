// Generated from dsl/feature/appearance-settings.feature
// source-sha256=7b64f7be0ceadcb151f5a5cbf9d1957399903ebdfec1f231152d555b180940c4
// Edit the source, then run biome generate.

package polski.generated.settings.appearance

import java.io.Closeable

enum class AppearanceSettingsAppearance(val text: String) { System("system"), Light("light"), Dark("dark") }

data class AppearanceSettingsViewModel(val appearance: AppearanceSettingsAppearance)

sealed interface AppearanceSettingsIntent {
    data class Choose(val value: AppearanceSettingsAppearance) : AppearanceSettingsIntent
}

/** `render` draws the model; `onIntent` subscribes raw input and returns the subscription to close. */
interface AppearanceSettingsView {
    fun render(viewModel: AppearanceSettingsViewModel)
    fun onIntent(handler: (AppearanceSettingsIntent) -> Unit): Closeable
}
