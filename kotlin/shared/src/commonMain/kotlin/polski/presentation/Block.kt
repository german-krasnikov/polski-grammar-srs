package polski.presentation

/**
 * Typed, presentation-only fragment a card shows for one [StylePhase]. No HTML/Compose types
 * live here; a host renders each variant however fits its UI. Generic across target/native
 * language packs — nothing below names a specific language pair.
 */
sealed interface Block {
    data class Formula(val text: String) : Block
    /** [detail] is the per-exercise `Exercise.explanation` — the theory box has always shown it
     *  alongside [text] (the skill's rule/theory) for rule-first; kept as its own field rather than
     *  folded into [text] so a host can still tell "the rule" from "this exercise's own note" apart. */
    data class Rule(val text: String, val detail: String) : Block
    data class Table(val caption: String, val rows: List<TableRow>) : Block
    data class Scene(val text: String) : Block
    data class NativeParallel(val pairs: List<NativeParallelPair>) : Block
    data class Examples(val items: List<String>) : Block
    /** [collapsedLabel] defaults to "" — CORE names no language; a host/skill supplies real UI text. */
    data class WhyOnDemand(val text: String, val collapsedLabel: String = "") : Block
    data class Changes(val items: List<ChangeItem>) : Block
    data class Contrast(val before: List<EndingPart>, val after: List<EndingPart>) : Block
}

data class TableRow(val label: String, val before: List<EndingPart>, val after: List<EndingPart>)
data class ChangeItem(val before: List<EndingPart>, val after: List<EndingPart>, val reason: String)
data class NativeParallelPair(val native: String, val target: String, val note: String, val matches: Boolean)
