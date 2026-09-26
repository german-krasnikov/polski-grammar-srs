package polski.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Debug-only escape hatch for the A/B/C measurement variants
 * (`Plans/Kotlin/FlipCardRivePlan.md` §5/FC2-15): variant B disables Rive, keeping the native flip.
 *
 * v1 read `System.getProperty("polski.debug.riveDisabled")`, which no test running outside this
 * process (an instrumented test, `adb shell`, a UI automator script) can ever set — a real,
 * documented gap (`v1-measurements.json`'s android section). This plain Compose-state singleton is
 * reachable the same way `AndroidSettingsScreen`'s hidden long-press toggle flips it: through the
 * real UI, so `adb shell input tap`/`uiautomator` can drive it exactly like a person would.
 */
object RiveMeasurementVariant {
    var riveDisabled by mutableStateOf(false)
}
