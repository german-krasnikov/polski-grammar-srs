package polski.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import polski.desktop.DesktopPreferencesController
import polski.desktop.DesktopPreferencesStatus
import polski.desktop.MacSystemStatus
import polski.preferences.Appearance
import polski.preferences.Motion
import polski.preferences.PreferredAnswerMode
import polski.preferences.PreferredStyle
import polski.presentation.AnswerMode
import polski.presentation.StyleId
import polski.presentation.StyleRegistry
import polski.presentation.builtInStyleIds

/** Mac-only settings destination; actions are owned by the window and its repositories. */
@Composable
internal fun DesktopSettingsScreen(
    preferences: DesktopPreferencesController,
    macSystemStatus: MacSystemStatus,
    onClose: () -> Unit,
    onStyle: (StyleId) -> Unit,
    onAnswerMode: (AnswerMode) -> Unit,
    onAppearance: (Appearance) -> Unit,
    onMotion: (Motion) -> Unit,
    onProgressImport: () -> Unit,
    onProgressExport: () -> Unit,
    onVocabularyImport: () -> Unit,
    onVocabularyExport: () -> Unit,
    onPreferencesImport: () -> Unit,
    onPreferencesExport: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Настройки", style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground)
            OutlinedButton(onClick = onClose) { Text("Вернуться к разделу") }
        }
        val status = when (val current = preferences.status) {
            DesktopPreferencesStatus.Loaded -> "Настройки сохранены"
            is DesktopPreferencesStatus.RecoveryRequired -> "Настройки требуют восстановления: ${current.reason}"
            is DesktopPreferencesStatus.Unavailable -> "Настройки недоступны: ${current.reason}"
            is DesktopPreferencesStatus.WriteFailed -> "Настройки не сохранены: ${current.reason}. Текущий выбор можно экспортировать."
        }
        Text(status, color = if (preferences.status == DesktopPreferencesStatus.Loaded)
            MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error)
        SettingsCard("Курс и практика") {
            Text("Польский ↔ русский · активный курс")
            Text("Подача объяснений")
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                builtInStyleIds.forEach { id ->
                    TabButton(styleLabel(id), preferences.value.styleId == PreferredStyle.valueOf(id.value)) { onStyle(id) }
                }
            }
            Text("Способ ответа")
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TabButton("Вслух", preferences.value.answerMode == PreferredAnswerMode.Oral) { onAnswerMode(AnswerMode.Oral) }
                TabButton("Напечатать", preferences.value.answerMode == PreferredAnswerMode.Typed) { onAnswerMode(AnswerMode.Typed) }
            }
            Text("Если ответ уже показан, новый способ действует со следующего задания.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SettingsCard("Внешний вид") {
            Text("Тема")
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Appearance.entries.forEach { appearance ->
                    TabButton(when (appearance) { Appearance.System -> "Системная"; Appearance.Light -> "Светлая"; Appearance.Dark -> "Тёмная" },
                        preferences.value.appearance == appearance) { onAppearance(appearance) }
                }
            }
            Text("Движение")
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Motion.entries.forEach { motion ->
                    TabButton(if (motion == Motion.System) "Системное" else "Уменьшенное",
                        preferences.value.motion == motion) { onMotion(motion) }
                }
            }
            val systemMotion = when (macSystemStatus) {
                MacSystemStatus.Loading -> "Читаем настройки дисплея macOS…"
                is MacSystemStatus.Available -> if (macSystemStatus.appearance.reduceMotion)
                    "macOS: уменьшение движения включено." else "macOS: обычное движение."
                is MacSystemStatus.Unavailable -> "Системные настройки дисплея недоступны: ${macSystemStatus.reason}"
            }
            Text("$systemMotion В этой сборке нет анимированных переходов.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SettingsCard("Данные") {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onProgressImport) { Text("Импорт прогресса") }
                OutlinedButton(onClick = onProgressExport) { Text("Экспорт прогресса") }
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onVocabularyImport) { Text("Импорт словаря") }
                OutlinedButton(onClick = onVocabularyExport) { Text("Экспорт словаря") }
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onPreferencesImport) { Text("Импорт настроек") }
                OutlinedButton(onClick = onPreferencesExport) { Text("Экспорт настроек") }
            }
        }
        SettingsCard("Напоминания") { Text("Недоступно на Mac в этой сборке") }
        SettingsCard("О приложении") {
            Text("Polski Grammar Matrix · локальная практика без учётной записи")
            Text("Пробел показывает ответ, 1 и 2 оценивают карточку. ⌘, открывает настройки.")
        }
    }
}

/** U1 fix (EmphasisUXAudit): mirrors web's/Android's `styleLabel` — prefer the recipe's own "ru"
 *  content label, falling back to this Russian copy while a style's content isn't authored yet. */
private val fallbackStyleLabel: Map<StyleId, String> = mapOf(
    StyleId.RuleFirst to "Схемы и логика",
    StyleId.SituationFirst to "Живые ситуации",
    StyleId.NativeContrast to "Через сравнение с родным",
    StyleId.MinimalTheory to "Минимум теории",
)

private fun styleLabel(id: StyleId): String =
    StyleRegistry.recipes[id]?.label?.get("ru")?.takeIf { it.isNotBlank() } ?: fallbackStyleLabel.getValue(id)

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}
