package com.nexun.nexres

import android.app.Application
import android.os.Build
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nexun.nexres.data.Preset
import com.nexun.nexres.data.PresetRepository
import com.nexun.nexres.display.DisplayController
import com.nexun.nexres.display.DisplayState
import com.nexun.nexres.shell.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Backend { SHIZUKU, ROOT }

enum class Access {
    SHIZUKU,
    SHIZUKU_DENIED,
    SHIZUKU_STOPPED,
    ROOT,
    NONE;

    val canRun: Boolean get() = this == SHIZUKU || this == ROOT
}

/** A localized message resolved late, so it always uses the current language. */
data class UiMessage(
    @StringRes val id: Int,
    val args: List<Any> = emptyList(),
    val detail: String? = null,
)

data class UiState(
    val access: Access = Access.NONE,
    val model: String = "",
    val current: DisplayState? = null,
    val width: String = "",
    val height: String = "",
    val density: String = "",
    val presetName: String = "",
    val presets: List<Preset> = emptyList(),
    val busy: Boolean = false,
    val message: UiMessage? = null,
    val rootAvailable: Boolean = false,
    val shizukuGranted: Boolean = false,
    val activeBackend: Backend? = null,
    val preferred: Backend? = null,
)

class MainViewModel(private val app: Application) : AndroidViewModel(app) {

    private val repository = PresetRepository(app)
    private val _state = MutableStateFlow(UiState(model = deviceName()))
    val state: StateFlow<UiState> = _state.asStateFlow()

    @Volatile
    private var runner: ((String) -> String)? = null

    init {
        viewModelScope.launch {
            repository.presets.collect { list -> _state.update { it.copy(presets = list) } }
        }
        refreshAccess()
    }

    fun refreshAccess() {
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }

            val shizukuGranted = withContext(Dispatchers.IO) { Shell.hasShizukuPermission() }
            val shizukuRunning = withContext(Dispatchers.IO) { Shell.isShizukuRunning() }
            val shizukuInstalled = withContext(Dispatchers.IO) { Shell.isShizukuInstalled(app) }
            val preferred = _state.value.preferred

            // Fast path: Shizuku is usable and not explicitly overridden. The
            // display is read immediately, before the (possibly slow or
            // prompting) root probe below.
            val useShizuku = shizukuGranted && preferred != Backend.ROOT
            if (useShizuku) {
                activate(Backend.SHIZUKU)
            } else {
                val access = when {
                    shizukuRunning -> Access.SHIZUKU_DENIED
                    shizukuInstalled -> Access.SHIZUKU_STOPPED
                    else -> Access.NONE
                }
                runner = null
                _state.update {
                    it.copy(
                        access = access,
                        shizukuGranted = shizukuGranted,
                        activeBackend = null,
                        busy = false,
                    )
                }
            }

            // Root detection can block on a superuser prompt; keep it out of the
            // Shizuku read path so values never wait on it.
            val rootAvailable = withContext(Dispatchers.IO) { Shell.hasRoot() }
            _state.update { it.copy(rootAvailable = rootAvailable, shizukuGranted = shizukuGranted) }

            if (useShizuku) return@launch
            when {
                rootAvailable -> activate(Backend.ROOT)
                shizukuGranted -> activate(Backend.SHIZUKU)
            }
        }
    }

    private fun activate(backend: Backend) {
        runner = when (backend) {
            Backend.SHIZUKU -> Shell::runShizuku
            Backend.ROOT -> Shell::runRoot
        }
        _state.update {
            it.copy(
                access = if (backend == Backend.SHIZUKU) Access.SHIZUKU else Access.ROOT,
                activeBackend = backend,
            )
        }
        loadCurrent()
    }

    fun selectBackend(backend: Backend) {
        _state.update { it.copy(preferred = backend) }
        refreshAccess()
    }

    fun onShizukuPermissionResult(granted: Boolean) {
        _state.update {
            it.copy(message = UiMessage(if (granted) R.string.msg_perm_granted else R.string.msg_perm_denied))
        }
        refreshAccess()
    }

    private fun loadCurrent() {
        val run = runner ?: return
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            runCatching { withContext(Dispatchers.IO) { DisplayController.read(run) } }
                .onSuccess { ds ->
                    _state.update {
                        it.copy(
                            busy = false,
                            current = ds,
                            width = ds.width.toString(),
                            height = ds.height.toString(),
                            density = ds.density.toString(),
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            busy = false,
                            message = UiMessage(R.string.msg_read_failed, detail = e.message),
                        )
                    }
                }
        }
    }

    fun apply() {
        val run = runner
        if (run == null) {
            _state.update { it.copy(message = UiMessage(R.string.msg_no_access)) }
            return
        }
        val width = _state.value.width.toIntOrNull()
        val height = _state.value.height.toIntOrNull()
        val density = _state.value.density.toIntOrNull()
        if (width == null || height == null || density == null) {
            _state.update { it.copy(message = UiMessage(R.string.msg_invalid_values)) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            runCatching {
                withContext(Dispatchers.IO) {
                    DisplayController.setSize(run, width, height)
                    DisplayController.setDensity(run, density)
                }
            }
                .onSuccess {
                    _state.update {
                        it.copy(
                            busy = false,
                            message = UiMessage(R.string.msg_applied, listOf(width, height, density)),
                        )
                    }
                    loadCurrent()
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            busy = false,
                            message = UiMessage(R.string.msg_apply_failed, detail = e.message),
                        )
                    }
                }
        }
    }

    fun reset() {
        val run = runner
        if (run == null) {
            _state.update { it.copy(message = UiMessage(R.string.msg_no_access)) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            runCatching {
                withContext(Dispatchers.IO) {
                    DisplayController.resetSize(run)
                    DisplayController.resetDensity(run)
                }
            }
                .onSuccess {
                    _state.update {
                        it.copy(busy = false, message = UiMessage(R.string.msg_reset_done))
                    }
                    loadCurrent()
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            busy = false,
                            message = UiMessage(R.string.msg_reset_failed, detail = e.message),
                        )
                    }
                }
        }
    }

    fun savePreset() {
        val width = _state.value.width.toIntOrNull()
        val height = _state.value.height.toIntOrNull()
        val density = _state.value.density.toIntOrNull()
        if (width == null || height == null || density == null) {
            _state.update { it.copy(message = UiMessage(R.string.msg_enter_valid)) }
            return
        }
        val name = _state.value.presetName.ifBlank { "${width}x$height @ $density" }
        viewModelScope.launch {
            repository.save(_state.value.presets + Preset(name, width, height, density))
            _state.update {
                it.copy(presetName = "", message = UiMessage(R.string.msg_preset_saved, listOf(name)))
            }
        }
    }

    fun applyPreset(preset: Preset) {
        _state.update {
            it.copy(
                width = preset.width.toString(),
                height = preset.height.toString(),
                density = preset.density.toString(),
            )
        }
        apply()
    }

    fun deletePreset(preset: Preset) {
        viewModelScope.launch {
            repository.save(_state.value.presets - preset)
        }
    }

    fun setWidth(value: String) = _state.update { it.copy(width = value.onlyDigits()) }
    fun setHeight(value: String) = _state.update { it.copy(height = value.onlyDigits()) }
    fun setDensity(value: String) = _state.update { it.copy(density = value.onlyDigits()) }
    fun setPresetName(value: String) = _state.update { it.copy(presetName = value) }
    fun clearMessage() = _state.update { it.copy(message = null) }

    private fun String.onlyDigits(): String = filter(Char::isDigit).take(5)

    private fun deviceName(): String {
        val name = listOf(Build.MANUFACTURER, Build.MODEL)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .trim()
        return name.ifBlank { "Android" }
    }
}
