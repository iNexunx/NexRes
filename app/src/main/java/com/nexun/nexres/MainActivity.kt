package com.nexun.nexres

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexun.nexres.data.ThemeMode
import com.nexun.nexres.ui.MainScreen
import com.nexun.nexres.ui.theme.NexResTheme
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {
    private val vm: MainViewModel by viewModels()

    private val permListener = Shizuku.OnRequestPermissionResultListener { _, result ->
        vm.onPermissionResult(result == PackageManager.PERMISSION_GRANTED)
    }
    private val binderReceived = Shizuku.OnBinderReceivedListener { vm.refresh() }
    private val binderDead = Shizuku.OnBinderDeadListener { vm.refresh() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Shizuku.addRequestPermissionResultListener(permListener)
        Shizuku.addBinderReceivedListenerSticky(binderReceived)
        Shizuku.addBinderDeadListener(binderDead)
        enableEdgeToEdge()
        setContent {
            val state by vm.state.collectAsStateWithLifecycle()
            val presets by vm.presets.collectAsStateWithLifecycle()
            val settings by vm.settings.collectAsStateWithLifecycle()
            val dark = when (settings.themeMode) {
                ThemeMode.DARK, ThemeMode.AMOLED -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            LaunchedEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = if (dark) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT) else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                    navigationBarStyle = if (dark) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT) else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                )
            }
            NexResTheme(settings = settings) {
                MainScreen(
                    state = state, presets = presets, settings = settings,
                    onRefresh = vm::refresh,
                    onGrant = { requestShizukuPermission() },
                    onOpenShizuku = { openShizukuApp() },
                    onGetShizuku = { openUrl("https://shizuku.rikka.app/") },
                    onApply = vm::applyCustom, onReset = vm::reset,
                    onWidth = vm::setWidth, onHeight = vm::setHeight, onDensity = vm::setDensity,
                    onPresetName = vm::setPresetName, onSavePreset = vm::savePreset,
                    onApplyPreset = vm::applyPreset, onDeletePreset = vm::deletePreset,
                    onStyle = vm::setThemeStyle, onThemeMode = vm::setThemeMode, onAccent = vm::setAccent, onFont = vm::setFont, onLanguage = vm::setLanguage, onDynamicColor = vm::setUseDynamicColor,
                    onConsumeMessage = vm::consumeMessage,
                )
            }
        }
    }

    override fun onResume() { super.onResume(); vm.refresh() }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(permListener)
        Shizuku.removeBinderReceivedListener(binderReceived)
        Shizuku.removeBinderDeadListener(binderDead)
    }

    private fun requestShizukuPermission() {
        try {
            if (!Shizuku.pingBinder()) { vm.refresh(); return }
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) { vm.refresh(); return }
            if (Shizuku.shouldShowRequestPermissionRationale()) { vm.onPermissionResult(false); return }
            Shizuku.requestPermission(1001)
        } catch (_: Throwable) { vm.refresh() }
    }

    private fun openShizukuApp() {
        val pm = packageManager
        val pkgs = listOf("moe.shizuku.privileged.api", "moe.shizuku.manager")
        for (pkg in pkgs) {
            val intent = pm.getLaunchIntentForPackage(pkg)
            if (intent != null) { runCatching { startActivity(intent) }; return }
        }
        openUrl("https://shizuku.rikka.app/")
    }

    private fun openUrl(url: String) { runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } }
}
