// Generated from dsl/feature/appearance-settings.feature
// source-sha256=7b64f7be0ceadcb151f5a5cbf9d1957399903ebdfec1f231152d555b180940c4
// Edit the source, then run biome generate.

package polski.generated.settings.appearance

import java.io.Closeable

/** Subscribes the View to the Model (and the View's intents to the Model's commands) and owns both subscriptions. */
class AppearanceSettingsPresenter(private val model: AppearanceSettingsModel, private val view: AppearanceSettingsView) : Closeable {
    private var disposed = false
    private val owned = ArrayList<Closeable>()

    init {
        try {
            owned += model.subscribe { render() }
            owned += view.onIntent { handle(it) }
            render()
        } catch (e: Throwable) { close(); throw e }
    }

    override fun close() {
        if (disposed) return
        disposed = true
        val errors = owned.asReversed().mapNotNull { runCatching { it.close() }.exceptionOrNull() }
        owned.clear()
        if (errors.isNotEmpty()) throw IllegalStateException("cleanup failed").also { e -> errors.forEach(e::addSuppressed) }
    }

    private fun render() {
        if (disposed) return
        val s = model.state
        view.render(AppearanceSettingsViewModel(appearance = s.appearance))
    }

    private fun handle(intent: AppearanceSettingsIntent) {
        if (disposed) return
        when (intent) {
            is AppearanceSettingsIntent.Choose -> model.choose(intent.value)
        }
    }
}

fun mountAppearanceSettings(view: AppearanceSettingsView, ports: AppearanceSettingsPorts, decisions: AppearanceSettingsDecisions): Closeable = AppearanceSettingsPresenter(AppearanceSettingsModel(ports, decisions), view)
