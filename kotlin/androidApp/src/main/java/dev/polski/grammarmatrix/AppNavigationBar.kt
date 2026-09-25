package dev.polski.grammarmatrix

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import polski.presentation.AppTab

@Composable
internal fun AppNavigationBar(selected: AppTab, onSelect: (AppTab) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        listOf(
            Triple(AppTab.Training, "Карточки", R.drawable.nav_cards),
            Triple(AppTab.Matrix, "Матрица", R.drawable.nav_matrix),
            Triple(AppTab.Progress, "Прогресс", R.drawable.nav_progress),
        ).forEach { (tab, label, icon) ->
            NavigationBarItem(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                icon = { Icon(painterResource(icon), contentDescription = null) },
                label = { Text(label) },
            )
        }
        NavigationBarItem(
            selected = selected == AppTab.Vocabulary,
            onClick = { onSelect(AppTab.Vocabulary) },
            icon = { Text("Aa") },
            label = { Text("Слова") },
        )
    }
}

@Composable
internal fun AppNavigationRail(selected: AppTab, onSelect: (AppTab) -> Unit) {
    NavigationRail {
        listOf(
            Triple(AppTab.Training, "Карточки", R.drawable.nav_cards),
            Triple(AppTab.Matrix, "Матрица", R.drawable.nav_matrix),
            Triple(AppTab.Progress, "Прогресс", R.drawable.nav_progress),
        ).forEach { (tab, label, icon) ->
            NavigationRailItem(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                icon = { Icon(painterResource(icon), contentDescription = null) },
                label = { Text(label) },
            )
        }
        NavigationRailItem(
            selected = selected == AppTab.Vocabulary,
            onClick = { onSelect(AppTab.Vocabulary) },
            icon = { Text("Aa") },
            label = { Text("Слова") },
        )
    }
}
