// Generated from dsl/feature/appearance-settings.feature
// source-sha256=7b64f7be0ceadcb151f5a5cbf9d1957399903ebdfec1f231152d555b180940c4
// Edit the source, then run biome generate.

package polski.generated.settings.appearance.examples

import java.io.Closeable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import polski.generated.settings.appearance.*

class AppearanceSettingsFakeView : AppearanceSettingsView {
    val renders = ArrayList<AppearanceSettingsViewModel>()
    var onRender: ((AppearanceSettingsViewModel) -> Unit)? = null
    private val recorded = ArrayList<(AppearanceSettingsIntent) -> Unit>()
    private val handlers = ArrayList<(AppearanceSettingsIntent) -> Unit>()

    val active: List<(AppearanceSettingsIntent) -> Unit> get() = handlers
    val last: AppearanceSettingsViewModel get() = renders.last()

    override fun render(viewModel: AppearanceSettingsViewModel) { onRender?.invoke(viewModel); renders += viewModel }
    override fun onIntent(handler: (AppearanceSettingsIntent) -> Unit): Closeable {
        recorded += handler
        handlers += handler
        return Closeable { handlers.remove(handler) }
    }

    fun send(intent: AppearanceSettingsIntent) = handlers.toList().forEach { it(intent) }
    /** Hostile late delivery: through a handler recorded earlier, even after it was unsubscribed. */
    fun sendStale(intent: AppearanceSettingsIntent) = recorded.toList().forEach { it(intent) }
}

class AppearanceSettingsFakePorts : AppearanceSettingsPorts {
    val calls = ArrayList<String>()
    override fun setAppearance(appearance: AppearanceSettingsAppearance) { calls += "setAppearance(${appearance.text})" }
}

class AppearanceSettingsExamplesTest {
    @Test
    fun mounts_with_the_default() {
        val view = AppearanceSettingsFakeView()
        val ports = AppearanceSettingsFakePorts()
        val handle = mountAppearanceSettings(view, ports, AppearanceSettingsLogicDecisions())
        assertEquals(AppearanceSettingsAppearance.System, view.last.appearance)
        assertEquals(1, view.renders.size)
        assertEquals(1, view.active.size)
        assertEquals(listOf<String>(), ports.calls)
    }

    @Test
    fun choosing_dark_calls_the_port() {
        val view = AppearanceSettingsFakeView()
        val ports = AppearanceSettingsFakePorts()
        val handle = mountAppearanceSettings(view, ports, AppearanceSettingsLogicDecisions())
        view.send(AppearanceSettingsIntent.Choose(AppearanceSettingsAppearance.Dark))
        assertEquals(AppearanceSettingsAppearance.Dark, view.last.appearance)
        assertEquals(2, view.renders.size)
        assertEquals(listOf<String>("setAppearance(dark)"), ports.calls)
    }

    @Test
    fun choosing_the_same_value_again_still_calls_the_port() {
        val view = AppearanceSettingsFakeView()
        val ports = AppearanceSettingsFakePorts()
        val handle = mountAppearanceSettings(view, ports, AppearanceSettingsLogicDecisions())
        view.send(AppearanceSettingsIntent.Choose(AppearanceSettingsAppearance.Dark))
        assertEquals(2, view.renders.size)
        assertEquals(listOf<String>("setAppearance(dark)"), ports.calls)
        view.send(AppearanceSettingsIntent.Choose(AppearanceSettingsAppearance.Dark))
        assertEquals(2, view.renders.size)
        assertEquals(listOf<String>("setAppearance(dark)", "setAppearance(dark)"), ports.calls)
    }

    @Test
    fun dispose_releases_everything() {
        val view = AppearanceSettingsFakeView()
        val ports = AppearanceSettingsFakePorts()
        val handle = mountAppearanceSettings(view, ports, AppearanceSettingsLogicDecisions())
        view.send(AppearanceSettingsIntent.Choose(AppearanceSettingsAppearance.Dark))
        handle.close()
        assertEquals(0, view.active.size)
        view.sendStale(AppearanceSettingsIntent.Choose(AppearanceSettingsAppearance.Light))
        assertEquals(2, view.renders.size)
        assertEquals(listOf<String>("setAppearance(dark)"), ports.calls)
    }

    @Test
    fun mount_rolls_back_when_the_first_render_throws() {
        val view = AppearanceSettingsFakeView().apply { onRender = { throw IllegalStateException("render failed") } }
        assertThrows<IllegalStateException> { mountAppearanceSettings(view, AppearanceSettingsFakePorts(), AppearanceSettingsLogicDecisions()) }
        assertEquals(0, view.active.size)
    }

    @Test
    fun dispose_is_idempotent_and_stale_handlers_of_every_action_are_inert() {
        val view = AppearanceSettingsFakeView()
        val ports = AppearanceSettingsFakePorts()
        val handle = mountAppearanceSettings(view, ports, AppearanceSettingsLogicDecisions())
        handle.close()
        handle.close()
        view.sendStale(AppearanceSettingsIntent.Choose(AppearanceSettingsAppearance.System))
        assertEquals(listOf(0, 1, 0), listOf(view.active.size, view.renders.size, ports.calls.size))
    }
}
