package polski.presentation

import polski.core.model.TileSegment
import polski.core.model.TileSelection

/**
 * SB6-AC: gates "Проверить" — true exactly when every [TileSegment.Choice] in [segments] has a
 * matching [TileSelection] in [placed]. Extracted from the web-only inline check
 * (`TileBuilderWeb.renderTileBuilderSection`) so a later Android/iOS renderer (SB-9/SB-10) calls
 * the same rule instead of re-deriving it.
 */
fun tileBuilderCanCheck(segments: List<TileSegment>, placed: List<TileSelection>): Boolean =
    segments.filterIsInstance<TileSegment.Choice>().all { choice -> placed.any { it.position == choice.position } }

/**
 * SB6-AC3's "only one picker open at a time": toggle-closes [tapped] when it is already [current],
 * otherwise opens [tapped] and implicitly closes whatever else was open. Generic over the panel id
 * type so both the tile picker (`Int` position) and the error panel reuse it.
 */
fun <Id> togglePanel(current: Id?, tapped: Id): Id? = if (current == tapped) null else tapped
