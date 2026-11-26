package com.nexun.nexres.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeStyle { MATERIAL, MIUIX }
enum class ThemeMode { SYSTEM, LIGHT, DARK, AMOLED }
enum class Accent { DEFAULT, BLUE, GREEN, ORANGE, PURPLE, PINK, RED, TEAL, CYAN, INDIGO, YELLOW, LIME, BROWN }
enum class FontChoice { SYSTEM, SANS, SERIF, MONO }

data class AppSettings(
    val themeStyle: ThemeStyle = ThemeStyle.MATERIAL,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accent: Accent = Accent.DEFAULT,
    val font: FontChoice = FontChoice.SYSTEM,
    val language: String = "system",
    val useDynamicColor: Boolean = true,
)

class SettingsStore(context: Context) {
    private val store = context.settingsDataStore
    private val styleKey = stringPreferencesKey("theme_style")
    private val modeKey = stringPreferencesKey("theme_mode")
    private val accentKey = stringPreferencesKey("accent")
    private val fontKey = stringPreferencesKey("font")
    private val langKey = stringPreferencesKey("language")
    private val dynamicKey = booleanPreferencesKey("use_dynamic_color")

    val settings: Flow<AppSettings> = store.data.map { p ->
        AppSettings(
            themeStyle = runCatching { ThemeStyle.valueOf(p[styleKey] ?: "") }.getOrDefault(ThemeStyle.MATERIAL),
            themeMode = runCatching { ThemeMode.valueOf(p[modeKey] ?: "") }.getOrDefault(ThemeMode.SYSTEM),
            accent = runCatching { Accent.valueOf(p[accentKey] ?: "") }.getOrDefault(Accent.DEFAULT),
            font = runCatching { FontChoice.valueOf(p[fontKey] ?: "") }.getOrDefault(FontChoice.SYSTEM),
            language = p[langKey] ?: "system",
            useDynamicColor = p[dynamicKey] ?: true,
        )
    }

    suspend fun setThemeStyle(v: ThemeStyle) { store.edit { it[styleKey] = v.name } }
    suspend fun setThemeMode(v: ThemeMode) { store.edit { it[modeKey] = v.name } }
    suspend fun setAccent(v: Accent) { store.edit { it[accentKey] = v.name } }
    suspend fun setFont(v: FontChoice) { store.edit { it[fontKey] = v.name } }
    suspend fun setLanguage(v: String) { store.edit { it[langKey] = v } }
    suspend fun setUseDynamicColor(v: Boolean) { store.edit { it[dynamicKey] = v } }
}
