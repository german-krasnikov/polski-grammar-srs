package polski.presentation

/**
 * Single source of truth for whether a host should skip playing a Rive effect
 * (`Plans/Architecture/DSL-MIGRATION-PLAN.md` block B). Replaces three previously independent,
 * host-local gating checks — Android's `cardEffectToPlay`, web's `riveGated`, and iOS/macOS's
 * inline `guard !reduceMotion` — with one pure function each host maps its own signals onto.
 */
object RiveGate {
    /**
     * `true` when no Rive call (bridge load, trigger, prewarm) should happen: reduced motion is
     * active, the app's own Animations setting is off, or a measurement variant disables Rive for
     * this session (e.g. the web `?riveDisabled=1` escape hatch).
     */
    fun skip(reduceMotion: Boolean, animationsEnabled: Boolean, measurementDisabled: Boolean): Boolean =
        reduceMotion || !animationsEnabled || measurementDisabled
}
