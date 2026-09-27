package polski.presentation

/**
 * Typed, presentation-only fragment a card shows for one [StylePhase]. No HTML/Compose types
 * live here; a host renders each variant however fits its UI. Generic across target/native
 * language packs — nothing below names a specific language pair.
 */
sealed interface Block {
    /** [parts] highlights [text] from the skill's explicit `focus.before`/`focus.after` pair only
     *  (EmphasisUXAudit E7/S4) — see [styleTextHighlightParts]; joins back to [text] exactly. */
    data class Formula(val text: String, val parts: List<EndingPart>) : Block
    /** [detail] is the per-exercise `Exercise.explanation` — the theory box has always shown it
     *  alongside [text] (the skill's rule/theory) for rule-first; kept as its own field rather than
     *  folded into [text] so a host can still tell "the rule" from "this exercise's own note" apart.
     *  [parts] highlights [text] only, the same way as [Formula.parts]. */
    data class Rule(val text: String, val detail: String, val parts: List<EndingPart>) : Block
    data class Table(val caption: String, val rows: List<TableRow>) : Block
    /** [parts] highlights [text] the same way as [Formula.parts]. */
    data class Scene(val text: String, val parts: List<EndingPart>) : Block
    /** Native (L1) side of each pair is never highlighted (EmphasisUXAudit E7); only
     *  [NativeParallelPair.targetParts] carries the explicit-pair highlight. */
    data class NativeParallel(val pairs: List<NativeParallelPair>) : Block
    /** [itemParts] highlights each [items] entry the same way as [Formula.parts] — parallel to
     *  [items] by index (`itemParts[i]` joins back to `items[i]`); [items] itself keeps its
     *  original `List<String>` shape so an unmigrated host still renders it unchanged. */
    data class Examples(val items: List<String>, val itemParts: List<List<EndingPart>>) : Block
    /** [collapsedLabel] defaults to "" — CORE names no language; a host/skill supplies real UI text.
     *  [parts] highlights [text] the same way as [Formula.parts]. */
    data class WhyOnDemand(val text: String, val parts: List<EndingPart>, val collapsedLabel: String = "") : Block
    data class Changes(val items: List<ChangeItem>) : Block
    data class Contrast(val before: List<EndingPart>, val after: List<EndingPart>) : Block
}

data class TableRow(val label: String, val before: List<EndingPart>, val after: List<EndingPart>)
data class ChangeItem(val before: List<EndingPart>, val after: List<EndingPart>, val reason: String)
/** [targetParts] highlights [target] from the skill's explicit pair only; [native] is prose in the
 *  learner's own language and is never highlighted (EmphasisUXAudit E7). */
data class NativeParallelPair(val native: String, val target: String, val note: String, val matches: Boolean, val targetParts: List<EndingPart>)
