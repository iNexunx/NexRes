package com.nexun.nexres

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class NexResApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences("nexres_boot", MODE_PRIVATE)
        val mode = when (prefs.getString("theme_mode", "SYSTEM")) {
            "LIGHT" -> AppCompatDelegate.MODE_NIGHT_NO
            "DARK", "AMOLED" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
