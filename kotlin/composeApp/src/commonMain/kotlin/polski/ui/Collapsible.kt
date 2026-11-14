package polski.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A panel that shows/hides itself with a height+opacity animation instead of popping in/out —
 * ported from Android's own `AndroidCollapsible` (D4, `Plans/Kotlin/Lane-android.md`) into
 * `commonMain` so Desktop and a future SB-9/SB-10 renderer share the same contract instead of
 * re-deriving it per host. [AnimatedVisibility] already removes [content] from the composition
 * (and so from the accessibility tree) while hidden, so no extra semantics are needed here.
 * `reduceMotion` (system setting or the app's own Settings toggle) shows/hides instantly, matching
 * every other reduced-motion path in this app.
 */
@Composable
fun Collapsible(visible: Boolean, reduceMotion: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = if (reduceMotion) EnterTransition.None
            else expandVertically(tween(220), expandFrom = Alignment.Top) + fadeIn(tween(220)),
        exit = if (reduceMotion) ExitTransition.None
            else shrinkVertically(tween(220), shrinkTowards = Alignment.Top) + fadeOut(tween(220)),
    ) { content() }
}

/** The repeated "scrollable row of chips/cells" shape (`Row` + `Modifier.horizontalScroll`) found
 *  across Desktop screens — a shared `commonMain` wrapper so new call sites stop hand-rolling it. */
@Composable
fun ScrollableChoiceRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), content = content)
}
