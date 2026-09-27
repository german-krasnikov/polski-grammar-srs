package polski.presentation

/**
 * UC-10 renamed the 2-value `explanationMethod`/`ExplanationMethod` to the 4-value [StyleId], but
 * the iOS/macOS Swift bridges (and their NSUserDefaults keys) still speak the old wire vocabulary
 * until those hosts get their own 4-way picker (`Plans/Kotlin/StylesBlueprint.md` §5/§6) — no Swift
 * source changes in this task. NativeContrast/MinimalTheory have no wire value there yet.
 */
fun legacyStyleWireValue(wire: String): StyleId? = when (wire) {
    "Logic" -> StyleId.RuleFirst
    "Situations" -> StyleId.SituationFirst
    else -> null
}

/** The inverse: collapses all 4 styles down to the 2 wire values the old bridge understands. */
fun StyleId.toLegacyWireValue(): String = if (this == StyleId.SituationFirst) "Situations" else "Logic"
