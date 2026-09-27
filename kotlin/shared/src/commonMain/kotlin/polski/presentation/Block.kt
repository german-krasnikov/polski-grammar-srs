package polski.presentation

/**
 * Typed, presentation-only fragment a card shows for one [StylePhase]. No HTML/Compose types
 * live here; a host renders each variant however fits its UI. Generic across target/native
 * language packs — nothing below names a specific language pair.
 */
sealed interface Block {
    data class Formula(val text: String) : Block
    data class Rule(val text: String) : Block
    data class Table(val caption: String, val rows: List<TableRow>) : Block
    data class Scene(val text: String) : Block
    data class NativeParallel(val pairs: List<NativeParallelPair>) : Block
    data class Examples(val items: List<String>) : Block
    data class WhyOnDemand(val text: String, val collapsedLabel: String = "Почему так?") : Block
    data class Changes(val items: List<ChangeItem>) : Block
    data class Contrast(val before: List<EndingPart>, val after: List<EndingPart>) : Block
}

data class TableRow(val label: String, val before: List<EndingPart>, val after: List<EndingPart>)
data class ChangeItem(val before: List<EndingPart>, val after: List<EndingPart>, val reason: String)
data class NativeParallelPair(val native: String, val target: String, val note: String, val matches: Boolean)
