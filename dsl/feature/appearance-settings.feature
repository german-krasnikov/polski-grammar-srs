# First COMPOSE_HOST pilot slice (Plans/DSL-CONSTRUCT-PLAN.md §2): one self-contained control of the settings
# panel — the appearance picker (System/Light/Dark) that DesktopSettingsScreen.kt and SettingsWeb.kt already
# show, modeled as its own FEATURE automaton instead of being carried inside the larger hand-written
# preferences reducer. The real app's `onAppearance: (Appearance) -> Unit` callback is this FEATURE's
# `setAppearance` PORT; the hand-written host binds it to `DesktopPreferencesController`/web prefs, outside
# this file (ARCHITECTURE.md §6.1: DSL does not generate layout/styles or own persistence).
FEATURE AppearanceSettings USES mvp
  STATE appearance : enum system|light|dark = system
  PORT setAppearance(appearance)
  ACTION choose(value: appearance) -> appearance = value; CALL setAppearance
  SHOW appearance
  EXAMPLES appearance-settings.examples
END
