package com.nexun.nexres

import android.app.Application
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nexun.nexres.data.Accent
import com.nexun.nexres.data.AppSettings
import com.nexun.nexres.data.FontChoice
import com.nexun.nexres.data.Preset
import com.nexun.nexres.data.PresetRepository
import com.nexun.nexres.data.SettingsStore
import com.nexun.nexres.data.ThemeMode
import com.nexun.nexres.data.ThemeStyle
import com.nexun.nexres.display.DisplayController
import com.nexun.nexres.display.DisplayInfo
import com.nexun.nexres.i18n.LocaleHelper
import com.nexun.nexres.shell.CmdResult
import com.nexun.nexres.shell.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Backend { NONE, SHIZUKU, ROOT }
enum class ShizukuState { NOT_INSTALLED, STOPPED, DENIED, READY }
data class UiMessage(val text: String? = null, val res: Int? = null, val args: List<Any> = emptyList())

data class UiState(
    val backend: Backend = Backend.NONE,
    val shizuku: ShizukuState = ShizukuState.STOPPED,
    val display: DisplayInfo? = null,
    val busy: Boolean = false,
    val width: String = "",
    val height: String = "",
    val density: String = "",
    val presetName: String = "",
    val message: UiMessage? = null,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PresetRepository(application)
    private val settingsStore = SettingsStore(application)
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    val presets: StateFlow<List<Preset>> = repo.presets.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val settings: StateFlow<AppSettings> = settingsStore.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val status = withContext(Dispatchers.IO) { detectStatus() }
            val info = if (status.first != Backend.NONE) withContext(Dispatchers.IO) { DisplayController.read(status.first) } else null
            _state.update { s ->
                val w = s.width.ifBlank { info?.currentWidth?.takeIf { it > 0 }?.toString().orEmpty() }
                val h = s.height.ifBlank { info?.currentHeight?.takeIf { it > 0 }?.toString().orEmpty() }
                val d = s.density.ifBlank { info?.currentDensity?.takeIf { it > 0 }?.toString().orEmpty() }
                s.copy(backend = status.first, shizuku = status.second, display = info ?: s.display, busy = false, width = w, height = h, density = d)
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _state.update { it.copy(message = UiMessage(res = if (granted) R.string.msg_perm_granted else R.string.msg_perm_denied)) }
        refresh()
    }

    fun setWidth(v: String) = _state.update { it.copy(width = v.filter(Char::isDigit).take(5)) }
    fun setHeight(v: String) = _state.update { it.copy(height = v.filter(Char::isDigit).take(5)) }
    fun setDensity(v: String) = _state.update { it.copy(density = v.filter(Char::isDigit).take(4)) }
    fun setPresetName(v: String) = _state.update { it.copy(presetName = v.take(24)) }
    fun consumeMessage() = _state.update { it.copy(message = null) }

    fun applyCustom() {
        val w = _state.value.width.toIntOrNull(); val h = _state.value.height.toIntOrNull(); val d = _state.value.density.toIntOrNull()
        if (w == null || h == null || d == null || w < 320 || h < 320 || d < 72) {
            _state.update { it.copy(message = UiMessage(res = R.string.msg_invalid_values)) }; return
        }
        runCommand(resOk = R.string.msg_applied, okArgs = listOf(w, h, d)) { b -> DisplayController.apply(b, w, h, d) }
    }

    fun reset() = runCommand(resOk = R.string.msg_reset_done) { b -> DisplayController.reset(b) }

    fun applyPreset(p: Preset) {
        _state.update { it.copy(width = p.width.toString(), height = p.height.toString(), density = p.density.toString()) }
        runCommand(resOk = R.string.msg_applied, okArgs = listOf(p.width, p.height, p.density)) { b -> DisplayController.apply(b, p.width, p.height, p.density) }
    }

    fun savePreset() {
        val w = _state.value.width.toIntOrNull(); val h = _state.value.height.toIntOrNull(); val d = _state.value.density.toIntOrNull()
        if (w == null || h == null || d == null || w < 320 || h < 320 || d < 72) {
            _state.update { it.copy(message = UiMessage(res = R.string.msg_enter_valid)) }; return
        }
        viewModelScope.launch {
            val current = presets.value.toMutableList()
            val name = _state.value.presetName.trim().ifBlank { "${w}x${h} · ${d}dpi" }
            current.removeAll { it.width == w && it.height == h && it.density == d }
            current.add(0, Preset(name, w, h, d))
            repo.save(current.take(12))
            _state.update { it.copy(presetName = "", message = UiMessage(res = R.string.msg_preset_saved)) }
        }
    }

    fun deletePreset(p: Preset) { viewModelScope.launch { repo.save(presets.value.filterNot { it == p }) } }

    fun setThemeStyle(v: ThemeStyle) = viewModelScope.launch { settingsStore.setThemeStyle(v) }
    fun setThemeMode(v: ThemeMode) {
        viewModelScope.launch { settingsStore.setThemeMode(v) }
        getApplication<Application>().getSharedPreferences("nexres_boot", 0).edit().putString("theme_mode", v.name).apply()
        val mode = when (v) {
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK, ThemeMode.AMOLED -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
    fun setAccent(v: Accent) = viewModelScope.launch { settingsStore.setAccent(v) }
    fun setFont(v: FontChoice) = viewModelScope.launch { settingsStore.setFont(v) }
    fun setUseDynamicColor(v: Boolean) = viewModelScope.launch { settingsStore.setUseDynamicColor(v) }
    fun setLanguage(tag: String) { viewModelScope.launch { settingsStore.setLanguage(tag) }; LocaleHelper.apply(tag) }

    private fun runCommand(resOk: Int, okArgs: List<Any> = emptyList(), block: suspend (Backend) -> CmdResult) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, message = null) }
            val status = withContext(Dispatchers.IO) { detectStatus() }
            val backend = status.first
            val shizuku = status.second
            if (backend == Backend.NONE) {
                _state.update { it.copy(busy = false, backend = backend, shizuku = shizuku, message = UiMessage(res = R.string.msg_no_access)) }
                return@launch
            }
            val result = withContext(Dispatchers.IO) { block(backend) }
            val final = if (!result.ok && shouldRetryWithRoot(backend, result)) withContext(Dispatchers.IO) { block(Backend.ROOT) } else result
            val finalBackend = if (final !== result) Backend.ROOT else backend
            val info = withContext(Dispatchers.IO) { DisplayController.read(finalBackend) }
            val finalStatus = withContext(Dispatchers.IO) { detectStatus().let { if (it.first == Backend.NONE && finalBackend != Backend.NONE) finalBackend to it.second else it } }
            _state.update {
                it.copy(
                    busy = false, backend = finalStatus.first, shizuku = finalStatus.second, display = info ?: it.display,
                    message = if (final.ok) UiMessage(res = resOk, args = okArgs) else friendlyError(final, finalBackend)
                )
            }
        }
    }

    private fun shouldRetryWithRoot(backend: Backend, r: CmdResult): Boolean {
        if (backend != Backend.SHIZUKU) return false
        val raw = "${r.error}\n${r.output}".lowercase()
        return Shell.isRootAvailable() && (raw.contains("securityexception") || raw.contains("permission denial") || raw.contains("write_secure_settings") || raw.contains("not allowed") || raw.contains("requires"))
    }

    private fun friendlyError(result: CmdResult, backend: Backend): UiMessage {
        val raw = "${result.error}\n${result.output}".lowercase()
        return when {
            raw.contains("securityexception") || raw.contains("permission denial") || raw.contains("write_secure_settings") || raw.contains("not allowed") || raw.contains("requires android.permission") ->
                UiMessage(res = if (backend == Backend.SHIZUKU) R.string.msg_perm_missing_shizuku else R.string.msg_perm_missing_root)
            raw.contains("unknown command") || raw.contains("not found") || raw.contains("no such") -> UiMessage(res = R.string.msg_cmd_unsupported)
            raw.contains("shizuku not ready") || raw.contains("permission denial: shizuku") -> UiMessage(res = R.string.msg_shizuku_not_ready)
            raw.contains("no backend") -> UiMessage(res = R.string.msg_no_access)
            else -> UiMessage(res = R.string.msg_apply_failed_detail)
        }
    }

    private fun detectStatus(): Pair<Backend, ShizukuState> {
        val shizuku = detectShizuku(Backend.NONE)
        val backend = when {
            shizuku == ShizukuState.READY -> Backend.SHIZUKU
            Shell.isRootAvailable() -> Backend.ROOT
            else -> Backend.NONE
        }
        return backend to if (backend == Backend.SHIZUKU) ShizukuState.READY else shizuku
    }

    private fun detectShizuku(backend: Backend): ShizukuState {
        if (backend == Backend.SHIZUKU) return ShizukuState.READY
        val pm = getApplication<Application>().packageManager
        val installed = listOf("moe.shizuku.privileged.api", "moe.shizuku.manager").any { pkg ->
            try { pm.getPackageInfo(pkg, 0); true } catch (_: Throwable) { false }
        }
        return try {
            val binder = rikka.shizuku.Shizuku.pingBinder()
            val granted = binder && rikka.shizuku.Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            when {
                granted -> ShizukuState.READY
                binder -> ShizukuState.DENIED
                installed -> ShizukuState.STOPPED
                else -> ShizukuState.NOT_INSTALLED
            }
        } catch (_: Throwable) {
            if (installed) ShizukuState.STOPPED else ShizukuState.NOT_INSTALLED
        }
    }
}
