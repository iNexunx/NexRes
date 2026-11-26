package com.nexun.nexres.ui

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexun.nexres.Backend
import com.nexun.nexres.R
import com.nexun.nexres.ShizukuState
import com.nexun.nexres.UiState
import com.nexun.nexres.data.Accent
import com.nexun.nexres.data.AppSettings
import com.nexun.nexres.data.FontChoice
import com.nexun.nexres.data.Preset
import com.nexun.nexres.data.ThemeMode
import com.nexun.nexres.data.ThemeStyle
import com.nexun.nexres.ui.theme.AccentPalettes
import com.nexun.nexres.ui.theme.nexResTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state: UiState, presets: List<Preset>, settings: AppSettings,
    onRefresh: () -> Unit, onGrant: () -> Unit, onOpenShizuku: () -> Unit, onGetShizuku: () -> Unit,
    onApply: () -> Unit, onReset: () -> Unit,
    onWidth: (String) -> Unit, onHeight: (String) -> Unit, onDensity: (String) -> Unit,
    onPresetName: (String) -> Unit, onSavePreset: () -> Unit, onApplyPreset: (Preset) -> Unit, onDeletePreset: (Preset) -> Unit,
    onStyle: (ThemeStyle) -> Unit, onThemeMode: (ThemeMode) -> Unit, onAccent: (Accent) -> Unit, onFont: (FontChoice) -> Unit, onLanguage: (String) -> Unit, onDynamicColor: (Boolean) -> Unit,
    onConsumeMessage: () -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val snack = remember { SnackbarHostState() }
    val ctx = LocalContext.current
    val msg = state.message
    LaunchedEffect(msg) {
        if (msg != null) {
            val text = msg.text ?: msg.res?.let { ctx.getString(it, *msg.args.toTypedArray()) } ?: return@LaunchedEffect
            snack.showSnackbar(text)
            onConsumeMessage()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background, scrolledContainerColor = MaterialTheme.colorScheme.background),
                title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.SemiBold, maxLines = 1) },
                actions = { IconButton(onClick = onRefresh) { Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.action_refresh)) } }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 0.dp) {
                val navColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = { Icon(Icons.Rounded.PhoneAndroid, null) }, label = { Text(stringResource(R.string.tab_display), maxLines = 1) }, colors = navColors)
                NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, icon = { Icon(Icons.Rounded.Settings, null) }, label = { Text(stringResource(R.string.tab_settings), maxLines = 1) }, colors = navColors)
            }
        },
        snackbarHost = { SnackbarHost(snack, modifier = Modifier.imePadding()) }
    ) { inner ->
        if (tab == 0) DisplayTab(inner, state, presets, settings, onGrant, onOpenShizuku, onGetShizuku, onApply, onReset, onWidth, onHeight, onDensity, onPresetName, onSavePreset, onApplyPreset, onDeletePreset)
        else SettingsTab(inner, settings, onStyle, onThemeMode, onAccent, onFont, onLanguage, onDynamicColor)
    }
}

@Composable
private fun DisplayTab(
    inner: PaddingValues, state: UiState, presets: List<Preset>, settings: AppSettings,
    onGrant: () -> Unit, onOpenShizuku: () -> Unit, onGetShizuku: () -> Unit,
    onApply: () -> Unit, onReset: () -> Unit,
    onWidth: (String) -> Unit, onHeight: (String) -> Unit, onDensity: (String) -> Unit,
    onPresetName: (String) -> Unit, onSavePreset: () -> Unit, onApplyPreset: (Preset) -> Unit, onDeletePreset: (Preset) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(inner).padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatusCard(state, settings, onGrant, onOpenShizuku, onGetShizuku)
        CurrentCard(state, settings)
        CustomCard(state, settings, onWidth, onHeight, onDensity, onApply, onReset)
        PresetCard(state, presets, settings, onPresetName, onSavePreset, onApplyPreset, onDeletePreset, Modifier.weight(1f, fill = true))
    }
}

@Composable
private fun StatusCard(state: UiState, settings: AppSettings, onGrant: () -> Unit, onOpen: () -> Unit, onGet: () -> Unit) {
    val ready = state.backend == Backend.SHIZUKU || state.backend == Backend.ROOT
    val (title, detail, action, fn) = when {
        state.backend == Backend.SHIZUKU -> Quad(R.string.status_shizuku_connected_title, R.string.status_shizuku_connected_detail, null, null)
        state.backend == Backend.ROOT -> Quad(R.string.status_root_title, R.string.status_root_detail, null, null)
        state.shizuku == ShizukuState.DENIED -> Quad(R.string.status_shizuku_denied_title, R.string.status_shizuku_denied_detail, R.string.action_grant, onGrant)
        state.shizuku == ShizukuState.STOPPED -> Quad(R.string.status_shizuku_stopped_title, R.string.status_shizuku_stopped_detail, R.string.action_open, onOpen)
        state.shizuku == ShizukuState.NOT_INSTALLED -> Quad(R.string.status_none_title, R.string.status_none_detail, R.string.action_get, onGet)
        else -> Quad(R.string.status_none_title, R.string.status_none_detail, R.string.action_open, onOpen)
    }
    SettingsCard(settings, pad = 12.dp, gap = 6.dp) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(if (ready) Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber, null, tint = if (ready) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            Column(Modifier.weight(1f)) {
                Text(stringResource(title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(stringResource(detail), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (action != null && fn != null) FilledTonalButton(onClick = fn, shape = nexResTokens(settings.themeStyle).pillShape, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) { Text(stringResource(action), maxLines = 1) }
        }
    }
}
private data class Quad(val a: Int, val b: Int, val c: Int?, val d: (() -> Unit)?)

@Composable
private fun CurrentCard(state: UiState, settings: AppSettings) {
    val info = state.display
    SettingsCard(settings, pad = 12.dp, gap = 8.dp) {
        Text(stringResource(R.string.section_current_resolution), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatPill(Modifier.weight(1f), stringResource(R.string.current_resolution), if (info != null) stringResource(R.string.size_format, info.currentWidth, info.currentHeight) else "-")
            StatPill(Modifier.weight(1f), stringResource(R.string.field_density), info?.currentDensity?.let { stringResource(R.string.unit_dpi, it) } ?: "-")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatPill(Modifier.weight(1f), stringResource(R.string.physical), if (info != null) stringResource(R.string.size_format, info.physicalWidth, info.physicalHeight) else "-")
            StatPill(Modifier.weight(1f), stringResource(R.string.mode), stringResource(if (info?.isOverride == true) R.string.mode_custom else R.string.mode_default))
        }
    }
}

@Composable
private fun StatPill(mod: Modifier, label: String, value: String) {
    Surface(mod, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerHigh, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun CustomCard(state: UiState, settings: AppSettings, onWidth: (String) -> Unit, onHeight: (String) -> Unit, onDensity: (String) -> Unit, onApply: () -> Unit, onReset: () -> Unit) {
    SettingsCard(settings, pad = 12.dp, gap = 8.dp) {
        Text(stringResource(R.string.section_custom_resolution), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallField(state.width, onWidth, R.string.field_width, Modifier.weight(1f), settings)
            SmallField(state.height, onHeight, R.string.field_height, Modifier.weight(1f), settings)
            SmallField(state.density, onDensity, R.string.field_density, Modifier.weight(1f), settings)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val t = nexResTokens(settings.themeStyle)
            Button(onClick = onApply, enabled = !state.busy, modifier = Modifier.weight(1f), shape = t.pillShape, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary), contentPadding = PaddingValues(vertical = 10.dp)) { Text(stringResource(R.string.action_apply), maxLines = 1) }
            OutlinedButton(onClick = onReset, enabled = !state.busy, modifier = Modifier.weight(1f), shape = t.pillShape, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), contentPadding = PaddingValues(vertical = 10.dp)) { Text(stringResource(R.string.action_reset), maxLines = 1) }
        }
    }
}

@Composable private fun SmallField(value: String, onValue: (String) -> Unit, label: Int, mod: Modifier, settings: AppSettings) {
    OutlinedTextField(value, onValue, modifier = mod, label = { Text(stringResource(label), maxLines = 1, overflow = TextOverflow.Ellipsis) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = nexResTokens(settings.themeStyle).fieldShape)
}

@Composable
private fun PresetCard(state: UiState, presets: List<Preset>, settings: AppSettings, onPresetName: (String) -> Unit, onSavePreset: () -> Unit, onApply: (Preset) -> Unit, onDelete: (Preset) -> Unit, mod: Modifier) {
    SettingsCard(settings, pad = 12.dp, gap = 8.dp, modifier = mod) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.presets_title), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text("${presets.size}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(state.presetName, onPresetName, modifier = Modifier.weight(1f), label = { Text(stringResource(R.string.preset_name_hint), maxLines = 1, overflow = TextOverflow.Ellipsis) }, singleLine = true, shape = nexResTokens(settings.themeStyle).fieldShape)
            FilledTonalButton(onClick = onSavePreset, enabled = !state.busy, shape = nexResTokens(settings.themeStyle).pillShape, contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) { Text(stringResource(R.string.action_save), maxLines = 1) }
        }
        if (presets.isEmpty()) {
            Text(stringResource(R.string.presets_empty), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        } else {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                presets.take(4).forEach { p ->
                    val t = nexResTokens(settings.themeStyle)
                    Surface(
                        onClick = { onApply(p) }, shape = t.pillShape, color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (t.cardBorder) 0.55f else 0.35f))
                    ) {
                        Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(p.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(stringResource(R.string.preset_summary, p.width, p.height, p.density), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                            IconButton(onClick = { onDelete(p) }, modifier = Modifier.size(34.dp)) { Icon(Icons.Rounded.DeleteOutline, contentDescription = stringResource(R.string.action_delete), modifier = Modifier.size(18.dp)) }
                        }
                    }
                }
                if (presets.size > 4) Text(stringResource(R.string.more_presets, presets.size - 4), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SettingsTab(inner: PaddingValues, settings: AppSettings, onStyle: (ThemeStyle) -> Unit, onThemeMode: (ThemeMode) -> Unit, onAccent: (Accent) -> Unit, onFont: (FontChoice) -> Unit, onLanguage: (String) -> Unit, onDynamicColor: (Boolean) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(inner).padding(horizontal = 12.dp, vertical = 4.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SettingsCard(settings, pad = 12.dp, gap = 8.dp) {
            Text(stringResource(R.string.section_appearance), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Label(R.string.theme_style)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoicePill(stringResource(R.string.style_material), settings.themeStyle == ThemeStyle.MATERIAL, settings, Modifier.weight(1f)) { onStyle(ThemeStyle.MATERIAL) }
                ChoicePill(stringResource(R.string.style_miuix), settings.themeStyle == ThemeStyle.MIUIX, settings, Modifier.weight(1f)) { onStyle(ThemeStyle.MIUIX) }
            }
            Label(R.string.theme_mode)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChoicePill(stringResource(R.string.mode_system), settings.themeMode == ThemeMode.SYSTEM, settings, Modifier.weight(1f)) { onThemeMode(ThemeMode.SYSTEM) }
                    ChoicePill(stringResource(R.string.mode_light), settings.themeMode == ThemeMode.LIGHT, settings, Modifier.weight(1f)) { onThemeMode(ThemeMode.LIGHT) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChoicePill(stringResource(R.string.mode_dark), settings.themeMode == ThemeMode.DARK, settings, Modifier.weight(1f)) { onThemeMode(ThemeMode.DARK) }
                    ChoicePill(stringResource(R.string.mode_amoled), settings.themeMode == ThemeMode.AMOLED, settings, Modifier.weight(1f)) { onThemeMode(ThemeMode.AMOLED) }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.dynamic_color), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Switch(checked = settings.useDynamicColor, onCheckedChange = onDynamicColor)
                }
            }
            Label(R.string.accent_color)
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(end = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Accent.entries.forEach { a ->
                    val c = accentColor(a)
                    val selected = settings.accent == a
                    Box(
                        Modifier.size(32.dp).clip(CircleShape).background(c)
                            .border(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            .clickable { onAccent(a) }
                    )
                }
            }
        }
        SettingsCard(settings, pad = 12.dp, gap = 8.dp) {
            Label(R.string.section_language)
            val langs = listOf("system" to R.string.lang_system, "es" to R.string.lang_es, "en" to R.string.lang_en, "pt" to R.string.lang_pt, "fr" to R.string.lang_fr, "de" to R.string.lang_de, "it" to R.string.lang_it, "ru" to R.string.lang_ru, "ar" to R.string.lang_ar, "hi" to R.string.lang_hi, "ja" to R.string.lang_ja, "ko" to R.string.lang_ko, "zh-CN" to R.string.lang_zh)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                langs.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (tag, label) -> ChoicePill(stringResource(label), settings.language == tag, settings, Modifier.weight(1f)) { onLanguage(tag) } }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
        SettingsCard(settings, pad = 12.dp, gap = 4.dp) {
            Text(stringResource(R.string.about_title), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text(stringResource(R.string.app_name) + " v1.0.0 · Nexun", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            }
            Text("github.com/iNexunx/NexRes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable private fun Label(res: Int) { Text(stringResource(res), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1) }

@Composable private fun SettingsCard(settings: AppSettings, pad: androidx.compose.ui.unit.Dp = 12.dp, gap: androidx.compose.ui.unit.Dp = 8.dp, container: Color = MaterialTheme.colorScheme.surfaceContainer, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val t = nexResTokens(settings.themeStyle)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = t.cardShape,
        colors = CardDefaults.cardColors(containerColor = container),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (t.cardBorder) 0.7f else 0.35f))
    ) { Column(Modifier.padding(pad), verticalArrangement = Arrangement.spacedBy(gap), content = content) }
}

@Composable private fun ChoicePill(label: String, selected: Boolean, settings: AppSettings, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val t = nexResTokens(settings.themeStyle)
    Surface(
        onClick = onClick, modifier = modifier, shape = t.pillShape,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun accentColor(a: Accent): Color {
    return if (a == Accent.DEFAULT) MaterialTheme.colorScheme.primary else (AccentPalettes[a.name] ?: AccentPalettes.getValue("BLUE")).seed
}
