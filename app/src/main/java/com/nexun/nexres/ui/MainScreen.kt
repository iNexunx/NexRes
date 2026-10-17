package com.nexun.nexres.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexun.nexres.Access
import com.nexun.nexres.Backend
import com.nexun.nexres.MainViewModel
import com.nexun.nexres.R
import com.nexun.nexres.UiState
import com.nexun.nexres.data.Preset
import com.nexun.nexres.display.DisplayState
import com.nexun.nexres.i18n.LocaleHelper
import kotlin.math.roundToInt

private val FieldShape = RoundedCornerShape(16.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    language: String,
    onSelectLanguage: (String) -> Unit,
    onRequestShizukuPermission: () -> Unit,
    onOpenShizuku: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        val base = context.getString(message.id, *message.args.toTypedArray())
        val text = if (message.detail.isNullOrBlank()) base else "$base: ${message.detail}"
        snackbarHostState.showSnackbar(text)
        viewModel.clearMessage()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.app_name),
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (state.model.isNotBlank()) {
                            Text(
                                state.model,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refreshAccess, enabled = !state.busy) {
                        Icon(
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = stringResource(R.string.action_refresh),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        MainContent(
            state = state,
            language = language,
            onSelectLanguage = onSelectLanguage,
            padding = padding,
            onRequestPermission = onRequestShizukuPermission,
            onOpenShizuku = onOpenShizuku,
            onSelectBackend = viewModel::selectBackend,
            onWidth = viewModel::setWidth,
            onHeight = viewModel::setHeight,
            onDensity = viewModel::setDensity,
            onApply = viewModel::apply,
            onReset = viewModel::reset,
            onPresetName = viewModel::setPresetName,
            onSave = viewModel::savePreset,
            onApplyPreset = viewModel::applyPreset,
            onDeletePreset = viewModel::deletePreset,
        )
    }
}

@Composable
private fun MainContent(
    state: UiState,
    language: String,
    onSelectLanguage: (String) -> Unit,
    padding: PaddingValues,
    onRequestPermission: () -> Unit,
    onOpenShizuku: () -> Unit,
    onSelectBackend: (Backend) -> Unit,
    onWidth: (String) -> Unit,
    onHeight: (String) -> Unit,
    onDensity: (String) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onPresetName: (String) -> Unit,
    onSave: () -> Unit,
    onApplyPreset: (Preset) -> Unit,
    onDeletePreset: (Preset) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .imePadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { HeroCard(state.current) }

        item {
            BackendCard(
                state = state,
                onRequestPermission = onRequestPermission,
                onOpenShizuku = onOpenShizuku,
                onSelectBackend = onSelectBackend,
            )
        }

        item {
            EditorCard(
                state = state,
                onWidth = onWidth,
                onHeight = onHeight,
                onDensity = onDensity,
                onApply = onApply,
                onReset = onReset,
            )
        }

        item {
            Text(
                stringResource(R.string.presets_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        item {
            PresetSaveRow(
                name = state.presetName,
                onName = onPresetName,
                onSave = onSave,
            )
        }

        if (state.presets.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.presets_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(state.presets, key = { "${it.name}-${it.width}-${it.height}-${it.density}" }) { preset ->
                PresetCard(preset, onApplyPreset, onDeletePreset)
            }
        }

        item { LanguageCard(selected = language, onSelect = onSelectLanguage) }
    }
}

@Composable
private fun HeroCard(current: DisplayState?) {
    val content = MaterialTheme.colorScheme.onPrimaryContainer
    val brush = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer,
        ),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(brush)
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.current_resolution).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = content.copy(alpha = 0.85f),
                )
                StatePill(active = current?.hasOverride == true, tint = content)
            }

            if (current == null) {
                Text(
                    stringResource(R.string.display_unavailable),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = content,
                )
            } else {
                Text(
                    stringResource(R.string.size_format, current.width, current.height),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = content,
                )
                Text(
                    stringResource(R.string.unit_dpi, current.density),
                    style = MaterialTheme.typography.titleMedium,
                    color = content.copy(alpha = 0.9f),
                )

                HorizontalDivider(color = content.copy(alpha = 0.2f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    MiniStat(
                        stringResource(R.string.physical),
                        stringResource(R.string.size_format, current.physicalWidth, current.physicalHeight),
                        content,
                    )
                    MiniStat(
                        stringResource(R.string.panel_dpi),
                        stringResource(R.string.unit_dpi, current.physicalDensity),
                        content,
                    )
                    MiniStat(
                        stringResource(R.string.mode),
                        stringResource(if (current.hasOverride) R.string.mode_custom else R.string.mode_default),
                        content,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatePill(active: Boolean, tint: Color) {
    Surface(
        color = tint.copy(alpha = 0.14f),
        contentColor = tint,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            stringResource(if (active) R.string.state_override else R.string.state_default).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: Color) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.75f),
        )
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = color,
        )
    }
}

@Composable
private fun BackendCard(
    state: UiState,
    onRequestPermission: () -> Unit,
    onOpenShizuku: () -> Unit,
    onSelectBackend: (Backend) -> Unit,
) {
    val access = state.access
    val dotColor: Color
    val title: String
    val detail: String
    when (access) {
        Access.SHIZUKU -> {
            dotColor = MaterialTheme.colorScheme.primary
            title = stringResource(R.string.status_shizuku_connected_title)
            detail = stringResource(R.string.status_shizuku_connected_detail)
        }
        Access.SHIZUKU_DENIED -> {
            dotColor = MaterialTheme.colorScheme.error
            title = stringResource(R.string.status_shizuku_denied_title)
            detail = stringResource(R.string.status_shizuku_denied_detail)
        }
        Access.SHIZUKU_STOPPED -> {
            dotColor = MaterialTheme.colorScheme.tertiary
            title = stringResource(R.string.status_shizuku_stopped_title)
            detail = stringResource(R.string.status_shizuku_stopped_detail)
        }
        Access.ROOT -> {
            dotColor = MaterialTheme.colorScheme.primary
            title = stringResource(R.string.status_root_title)
            detail = stringResource(R.string.status_root_detail)
        }
        Access.NONE -> {
            dotColor = MaterialTheme.colorScheme.outline
            title = stringResource(R.string.status_none_title)
            detail = stringResource(R.string.status_none_detail)
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_shield),
                            contentDescription = null,
                            tint = dotColor,
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(8.dp))
                when (access) {
                    Access.SHIZUKU_DENIED -> Button(onClick = onRequestPermission, enabled = !state.busy) {
                        Text(stringResource(R.string.action_grant))
                    }
                    Access.SHIZUKU_STOPPED -> FilledTonalButton(onClick = onOpenShizuku) {
                        Text(stringResource(R.string.action_open))
                    }
                    Access.NONE -> FilledTonalButton(onClick = onOpenShizuku) {
                        Text(stringResource(R.string.action_get))
                    }
                    else -> Unit
                }
            }

            if (state.rootAvailable && state.shizukuGranted) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        stringResource(R.string.backend_label),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.weight(1f),
                    )
                    BackendButton(
                        stringResource(R.string.backend_shizuku),
                        state.activeBackend == Backend.SHIZUKU,
                        !state.busy,
                    ) { onSelectBackend(Backend.SHIZUKU) }
                    BackendButton(
                        stringResource(R.string.backend_root),
                        state.activeBackend == Backend.ROOT,
                        !state.busy,
                    ) { onSelectBackend(Backend.ROOT) }
                }
            }
        }
    }
}

@Composable
private fun BackendButton(
    label: String,
    active: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    if (active) {
        Button(onClick = onClick, enabled = enabled) { Text(label) }
    } else {
        FilledTonalButton(onClick = onClick, enabled = enabled) { Text(label) }
    }
}

@Composable
private fun EditorCard(
    state: UiState,
    onWidth: (String) -> Unit,
    onHeight: (String) -> Unit,
    onDensity: (String) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
) {
    val enabled = !state.busy && state.access.canRun
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.custom_resolution),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (state.busy) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                }
            }

            MetricSlider(
                label = stringResource(R.string.field_width),
                value = state.width,
                range = 320..3840,
                onValueChange = onWidth,
            )
            MetricSlider(
                label = stringResource(R.string.field_height),
                value = state.height,
                range = 320..3840,
                onValueChange = onHeight,
            )
            MetricSlider(
                label = stringResource(R.string.field_density),
                value = state.density,
                range = 120..960,
                onValueChange = onDensity,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NumberField(stringResource(R.string.field_width), state.width, Modifier.weight(1f), onWidth)
                NumberField(stringResource(R.string.field_height), state.height, Modifier.weight(1f), onHeight)
                NumberField(stringResource(R.string.field_density), state.density, Modifier.weight(1f), onDensity)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onApply, enabled = enabled, modifier = Modifier.weight(1f)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.action_apply))
                }
                FilledTonalButton(onClick = onReset, enabled = enabled, modifier = Modifier.weight(1f)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_reset),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.action_reset))
                }
            }
        }
    }
}

@Composable
private fun MetricSlider(
    label: String,
    value: String,
    range: IntRange,
    onValueChange: (String) -> Unit,
) {
    val current = value.toIntOrNull() ?: range.first
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(
                value.ifBlank { "—" },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = current.toFloat().coerceIn(range.first.toFloat(), range.last.toFloat()),
            onValueChange = { onValueChange(it.roundToInt().toString()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
        )
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { Text(label) },
        shape = FieldShape,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
    )
}

@Composable
private fun PresetSaveRow(
    name: String,
    onName: (String) -> Unit,
    onSave: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = name,
            onValueChange = onName,
            singleLine = true,
            shape = FieldShape,
            label = { Text(stringResource(R.string.preset_name_hint)) },
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(10.dp))
        Button(onClick = onSave) {
            Icon(
                painter = painterResource(R.drawable.ic_save),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.action_save))
        }
    }
}

@Composable
private fun PresetCard(
    preset: Preset,
    onApply: (Preset) -> Unit,
    onDelete: (Preset) -> Unit,
) {
    ElevatedCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(preset.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    "${stringResource(R.string.size_format, preset.width, preset.height)} · ${
                        stringResource(R.string.unit_dpi, preset.density)
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FilledTonalButton(onClick = { onApply(preset) }) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.action_apply))
            }
            IconButton(onClick = { onDelete(preset) }) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = stringResource(R.string.action_delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun LanguageCard(
    selected: String,
    onSelect: (String) -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_language),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.language_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LocaleHelper.languages.forEach { (tag, label) ->
                    FilterChip(
                        selected = selected == tag,
                        onClick = { if (selected != tag) onSelect(tag) },
                        label = { Text(label) },
                    )
                }
            }
        }
    }
}
