package polski.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import polski.spike.SpikeRating
import polski.spike.SpikeSession

/** A disposable renderer experiment. [save] persists only the isolated preview state. */
@Composable
fun SpikeScreen(initial: SpikeSession, save: (SpikeSession) -> Unit) {
    var session by remember { mutableStateOf(initial) }

    fun update(next: SpikeSession) {
        session = next
        save(next)
    }

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
            SpikeHtmlInput(
                value = session.input,
                onValueChange = { update(session.type(it)) },
                onReveal = { update(session.reveal()) },
                modifier = Modifier.fillMaxWidth().height(160.dp),
            )
            SpikeHtmlActions(
                revealed = session.revealed,
                rating = session.rating,
                onReveal = { update(session.reveal()) },
                onRate = { update(session.rate(it)) },
                modifier = Modifier.fillMaxWidth().height(160.dp),
            )
            SpikeHtmlTable(modifier = Modifier.fillMaxWidth().height(340.dp))
    }
}

@Composable
internal expect fun SpikeHtmlInput(
    value: String,
    onValueChange: (String) -> Unit,
    onReveal: () -> Unit,
    modifier: Modifier,
)

@Composable
internal expect fun SpikeHtmlTable(modifier: Modifier)

@Composable
internal expect fun SpikeHtmlActions(
    revealed: Boolean,
    rating: SpikeRating?,
    onReveal: () -> Unit,
    onRate: (SpikeRating) -> Unit,
    modifier: Modifier,
)
