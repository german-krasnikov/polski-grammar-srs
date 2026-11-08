package polski.presentation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Full 2x2x2 truth table for [RiveGate.skip] (`DSL-MIGRATION-PLAN.md` block B). */
class RiveGateTest {
    @Test fun allEnabled() = assertFalse(RiveGate.skip(reduceMotion = false, animationsEnabled = true, measurementDisabled = false))
    @Test fun reduceMotionSkips() = assertTrue(RiveGate.skip(reduceMotion = true, animationsEnabled = true, measurementDisabled = false))
    @Test fun animationsDisabledSkips() = assertTrue(RiveGate.skip(reduceMotion = false, animationsEnabled = false, measurementDisabled = false))
    @Test fun measurementSkips() = assertTrue(RiveGate.skip(reduceMotion = false, animationsEnabled = true, measurementDisabled = true))
    @Test fun reduceMotionAndAnimationsOff() = assertTrue(RiveGate.skip(reduceMotion = true, animationsEnabled = false, measurementDisabled = false))
    @Test fun reduceMotionAndMeasurement() = assertTrue(RiveGate.skip(reduceMotion = true, animationsEnabled = true, measurementDisabled = true))
    @Test fun animationsOffAndMeasurement() = assertTrue(RiveGate.skip(reduceMotion = false, animationsEnabled = false, measurementDisabled = true))
    @Test fun allDisabled() = assertTrue(RiveGate.skip(reduceMotion = true, animationsEnabled = false, measurementDisabled = true))
}
