package com.nexun.nexres.i18n

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleHelper {
    const val SYSTEM = "system"
    fun apply(tag: String) {
        val locales = if (tag == SYSTEM) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag)
        val current = AppCompatDelegate.getApplicationLocales().toLanguageTags().ifBlank { SYSTEM }
        val wanted = if (tag == SYSTEM) SYSTEM else tag
        if (current == wanted) return
        AppCompatDelegate.setApplicationLocales(locales)
    }
    fun currentTag(): String = AppCompatDelegate.getApplicationLocales().toLanguageTags().ifBlank { SYSTEM }
}
