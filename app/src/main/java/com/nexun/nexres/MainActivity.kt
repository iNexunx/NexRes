package com.nexun.nexres

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.nexun.nexres.i18n.LocaleHelper
import com.nexun.nexres.shell.Shell
import com.nexun.nexres.ui.MainScreen
import com.nexun.nexres.ui.theme.NexResTheme
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val permissionListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == REQUEST_CODE) {
                viewModel.onShizukuPermissionResult(grantResult == PackageManager.PERMISSION_GRANTED)
            }
        }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Shizuku.addRequestPermissionResultListener(permissionListener)

        val language = LocaleHelper.selected(this)

        setContent {
            NexResTheme {
                MainScreen(
                    viewModel = viewModel,
                    language = language,
                    onSelectLanguage = { tag ->
                        LocaleHelper.setSelected(this, tag)
                        recreate()
                    },
                    onRequestShizukuPermission = {
                        runCatching { Shizuku.requestPermission(REQUEST_CODE) }
                            .onFailure { viewModel.onShizukuPermissionResult(false) }
                    },
                    onOpenShizuku = ::openShizuku,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshAccess()
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        super.onDestroy()
    }

    private fun openShizuku() {
        val intent = packageManager.getLaunchIntentForPackage(Shell.SHIZUKU_PACKAGE)
            ?: Intent(Intent.ACTION_VIEW).setData(android.net.Uri.parse("market://details?id=${Shell.SHIZUKU_PACKAGE}"))
        runCatching { startActivity(intent) }
    }

    companion object {
        const val REQUEST_CODE = 1001
    }
}
