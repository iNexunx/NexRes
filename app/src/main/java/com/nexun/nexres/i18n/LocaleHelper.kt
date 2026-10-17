package com.nexun.nexres.i18n

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

/**
 * Applies an in-app language choice by wrapping the base [Context] with a
 * configuration that carries the selected locale. The choice is stored in
 * SharedPreferences so it is available synchronously during
 * `attachBaseContext`, before any resource is resolved.
 *
 * An empty tag means "follow the system".
 */
object LocaleHelper {

    const val SYSTEM = "system"

    private const val PREFS = "nexres_settings"
    private const val KEY_LANGUAGE = "language"

    /** The language tags shown in the picker, in display order. */
    val languages: List<Pair<String, String>> = listOf(
        SYSTEM to "System",
        "en" to "English",
        "es" to "Español",
        "fr" to "Français",
        "de" to "Deutsch",
        "pt" to "Português",
        "it" to "Italiano",
        "ru" to "Русский",
    )

    fun selected(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, SYSTEM)
            ?: SYSTEM

    fun setSelected(context: Context, tag: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, tag)
            .apply()
    }

    /** Returns a context whose resources use the selected locale. */
    fun wrap(context: Context): Context {
        val tag = selected(context)
        if (tag == SYSTEM || tag.isBlank()) return context

        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocales(LocaleList(locale))
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }
}
