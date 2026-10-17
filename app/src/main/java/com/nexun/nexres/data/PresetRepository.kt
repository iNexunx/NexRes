package com.nexun.nexres.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.presetDataStore: DataStore<Preferences> by preferencesDataStore(name = "presets")

private val KEY = stringPreferencesKey("presets")
private const val SEP = "\u0001"
private const val ROW = "\n"

class PresetRepository(context: Context) {

    private val store = context.presetDataStore

    val presets: Flow<List<Preset>> = store.data.map { prefs -> decode(prefs[KEY].orEmpty()) }

    suspend fun save(list: List<Preset>) {
        store.edit { it[KEY] = encode(list) }
    }

    private fun encode(list: List<Preset>): String =
        list.joinToString(ROW) { "${it.name}$SEP${it.width}$SEP${it.height}$SEP${it.density}" }

    private fun decode(raw: String): List<Preset> =
        raw.lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.split(SEP)
                if (parts.size != 4) return@mapNotNull null
                val width = parts[1].toIntOrNull() ?: return@mapNotNull null
                val height = parts[2].toIntOrNull() ?: return@mapNotNull null
                val density = parts[3].toIntOrNull() ?: return@mapNotNull null
                Preset(parts[0], width, height, density)
            }
            .toList()
}
