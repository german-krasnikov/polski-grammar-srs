package polski.desktop

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import polski.preferences.Appearance

private val dark = darkColorScheme(
    primary = Color(0xFFFFC48B), onPrimary = Color(0xFF39230F),
    secondary = Color(0xFF9CD5C5), secondaryContainer = Color(0xFF34575B),
    background = Color(0xFF101B21), surface = Color(0xFF192A31),
    surfaceContainer = Color(0xFF20343D), onSurface = Color(0xFFF4F4EF),
    outline = Color(0xFF73929B), error = Color(0xFFFFA89E),
)
private val light = lightColorScheme(
    primary = Color(0xFF704514), onPrimary = Color.White,
    secondary = Color(0xFF245A53), secondaryContainer = Color(0xFFDCEFEA),
    background = Color(0xFFF7F7F2), surface = Color.White,
    surfaceContainer = Color(0xFFE8F0ED), onSurface = Color(0xFF18262B),
    outline = Color(0xFF567078), error = Color(0xFFAA302A),
)

@Composable
internal fun DesktopTheme(appearance: Appearance, system: MacSystemStatus, content: @Composable () -> Unit) {
    val observed = (system as? MacSystemStatus.Available)?.appearance
    val darkMode = when (appearance) {
        Appearance.System -> observed?.isDark ?: isSystemInDarkTheme()
        Appearance.Light -> false
        Appearance.Dark -> true
    }
    val base = if (darkMode) dark else light
    val colors = if (observed?.increaseContrast == true) base.copy(
        outline = if (darkMode) Color.White else Color.Black,
    ) else base
    MaterialTheme(colorScheme = colors, content = content)
}
