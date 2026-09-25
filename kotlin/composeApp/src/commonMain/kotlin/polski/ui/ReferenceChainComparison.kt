package polski.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import polski.data.courseAspectNoPresent
import polski.data.ReferenceChainRow
import polski.data.ReferenceTenseRow
import polski.data.ReferenceAspectRow
import polski.data.MaleAccRow
import polski.presentation.ContrastPair

/** Shows both sides of an authored matrix transition with text labels as well as colour. */
@Composable
fun ReferenceChainComparison(row: ReferenceChainRow, modifier: Modifier = Modifier) {
    ReferenceTransitionComparison(row.label, row.from, row.to, row.change, modifier)
}

@Composable
fun ReferenceTenseComparison(row: ReferenceTenseRow, modifier: Modifier = Modifier) {
    ReferenceTransitionComparison(row.label, row.from, row.to, null, modifier)
}

@Composable
fun ReferenceAspectComparison(row: ReferenceAspectRow, modifier: Modifier = Modifier) {
    val present = row.present
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(row.label, style = MaterialTheme.typography.titleMedium)
        if (present == null) Text(courseAspectNoPresent.compact)
        else ReferenceTransitionComparison("Настоящее", row.from, present, null, Modifier)
        ReferenceTransitionComparison("Прошедшее", row.from, row.past, null, Modifier)
        ReferenceTransitionComparison("Будущее", row.from, row.future, null, Modifier)
    }
}

@Composable
fun MaleAccComparison(row: MaleAccRow, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("${row.label} · ${row.title}", style = MaterialTheme.typography.titleMedium)
        row.examples.forEach { example ->
            ReferenceTransitionComparison("Форма в Bierniku", example.from, example.to, example.sentence, Modifier)
        }
        Text(row.rule, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ReferenceTransitionComparison(label: String, from: String, to: String, note: String?, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        ContrastPairText(ContrastPair.generated(from, to))
        if (note != null) Text(note, style = MaterialTheme.typography.bodyMedium)
    }
}
