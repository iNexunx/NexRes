package com.nexun.nexres.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nexun.nexres.data.Accent
import com.nexun.nexres.data.AppSettings
import com.nexun.nexres.data.ThemeMode
import com.nexun.nexres.data.ThemeStyle

@Immutable
data class NexResStyleTokens(
    val cardShape: RoundedCornerShape,
    val pillShape: RoundedCornerShape,
    val fieldShape: RoundedCornerShape,
    val containerPadding: androidx.compose.ui.unit.Dp,
    val itemSpacing: androidx.compose.ui.unit.Dp,
    val cardBorder: Boolean,
)

fun materialTokens() = NexResStyleTokens(RoundedCornerShape(24.dp), RoundedCornerShape(999.dp), RoundedCornerShape(16.dp), 14.dp, 10.dp, true)
fun miuixTokens() = NexResStyleTokens(RoundedCornerShape(30.dp), RoundedCornerShape(18.dp), RoundedCornerShape(20.dp), 14.dp, 8.dp, false)

private fun materialShapes() = Shapes(extraSmall = RoundedCornerShape(10.dp), small = RoundedCornerShape(14.dp), medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(24.dp), extraLarge = RoundedCornerShape(30.dp))
private fun miuixShapes() = Shapes(extraSmall = RoundedCornerShape(14.dp), small = RoundedCornerShape(18.dp), medium = RoundedCornerShape(24.dp), large = RoundedCornerShape(30.dp), extraLarge = RoundedCornerShape(36.dp))

@Composable
fun NexResTheme(settings: AppSettings = AppSettings(), content: @Composable () -> Unit) {
    val dark = when (settings.themeMode) {
        ThemeMode.DARK, ThemeMode.AMOLED -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val amoled = settings.themeMode == ThemeMode.AMOLED
    val accent = AccentPalettes[settings.accent.name] ?: AccentPalettes.getValue("BLUE")
    val dynamic = settings.useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val ctx = LocalContext.current

    val base = when {
        dynamic && dark -> dynamicDarkColorScheme(ctx)
        dynamic && !dark -> dynamicLightColorScheme(ctx)
        dark -> darkColorScheme(
            primary = accent.dark, onPrimary = Color(0xFF0F1722),
            primaryContainer = accent.containerDark, onPrimaryContainer = Color(0xFFEAF2FF),
            secondary = accent.dark, secondaryContainer = accent.containerDark.copy(alpha = 0.85f),
            background = Color(0xFF101319), onBackground = Color(0xFFE6EAF2),
            surface = Color(0xFF161A22), onSurface = Color(0xFFE6EAF2),
            surfaceVariant = Color(0xFF2A303C), onSurfaceVariant = Color(0xFFC0C8D6),
            surfaceContainerLowest = Color(0xFF0D1016), surfaceContainerLow = Color(0xFF191D26), surfaceContainer = Color(0xFF1E2330), surfaceContainerHigh = Color(0xFF272D3A), surfaceContainerHighest = Color(0xFF303848),
            outline = Color(0xFF4A5261), outlineVariant = Color(0xFF394150),
        )
        else -> lightColorScheme(
            primary = accent.light, onPrimary = Color.White,
            primaryContainer = accent.containerLight, onPrimaryContainer = Color(0xFF102033),
            secondary = accent.light, secondaryContainer = accent.containerLight.copy(alpha = 0.8f),
            background = Color(0xFFF6F7FB), onBackground = Color(0xFF171B22),
            surface = Color(0xFFF7F8FC), onSurface = Color(0xFF171B22),
            surfaceVariant = Color(0xFFE2E7F0), onSurfaceVariant = Color(0xFF434B59),
            surfaceContainerLowest = Color(0xFFFFFFFF), surfaceContainerLow = Color(0xFFF2F4F9), surfaceContainer = Color(0xFFFFFFFF), surfaceContainerHigh = Color(0xFFEAEFF7), surfaceContainerHighest = Color(0xFFDFE5EF),
            outline = Color(0xFF747D8C), outlineVariant = Color(0xFFD5DCE8),
        )
    }

    val scheme = base.copy(
        background = when {
            amoled -> Color(0xFF000000)
            settings.themeStyle == ThemeStyle.MIUIX && dark -> Color(0xFF0E1014)
            settings.themeStyle == ThemeStyle.MIUIX && !dark -> Color(0xFFF1F2F4)
            else -> base.background
        },
        surface = when {
            amoled -> Color(0xFF000000)
            settings.themeStyle == ThemeStyle.MIUIX && dark -> Color(0xFF14171C)
            settings.themeStyle == ThemeStyle.MIUIX && !dark -> Color(0xFFF1F2F4)
            else -> base.surface
        },
        surfaceContainerLowest = if (amoled) Color(0xFF000000) else if (settings.themeStyle == ThemeStyle.MIUIX && !dark) Color(0xFFFAFAFC) else base.surfaceContainerLowest,
        surfaceContainerLow = if (amoled) Color(0xFF050608) else if (settings.themeStyle == ThemeStyle.MIUIX && dark) Color(0xFF181B21) else if (settings.themeStyle == ThemeStyle.MIUIX) Color(0xFFF7F8FA) else base.surfaceContainerLow,
        surfaceContainer = if (amoled) Color(0xFF0B0D10) else if (settings.themeStyle == ThemeStyle.MIUIX && dark) Color(0xFF1E222A) else if (settings.themeStyle == ThemeStyle.MIUIX) Color(0xFFFFFFFF) else base.surfaceContainer,
        surfaceContainerHigh = if (amoled) Color(0xFF12151A) else if (settings.themeStyle == ThemeStyle.MIUIX && dark) Color(0xFF262B34) else if (settings.themeStyle == ThemeStyle.MIUIX) Color(0xFFE9EBEF) else base.surfaceContainerHigh,
        surfaceContainerHighest = if (amoled) Color(0xFF191D24) else base.surfaceContainerHighest,
        outline = if (amoled) Color(0xFF3A404A) else if (settings.themeStyle == ThemeStyle.MIUIX && dark) Color(0xFF454B56) else if (settings.themeStyle == ThemeStyle.MIUIX) Color(0xFFCDD3DD) else base.outline,
        outlineVariant = if (amoled) Color(0xFF2A303A) else if (settings.themeStyle == ThemeStyle.MIUIX && dark) Color(0xFF353B46) else if (settings.themeStyle == ThemeStyle.MIUIX) Color(0xFFDEE2EA) else base.outlineVariant,
    )

    MaterialTheme(
        colorScheme = scheme,
        shapes = if (settings.themeStyle == ThemeStyle.MIUIX) miuixShapes() else materialShapes(),
        typography = appTypography(settings.font),
        content = content,
    )
}

@Composable
fun nexResTokens(style: ThemeStyle): NexResStyleTokens = if (style == ThemeStyle.MIUIX) miuixTokens() else materialTokens()
