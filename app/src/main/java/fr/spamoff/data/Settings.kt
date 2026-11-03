package fr.spamoff.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.settings by preferencesDataStore("settings")
data class Settings(val enabled: Boolean = true, val automated: Boolean = false, val autoUpdate: Boolean = true, val theme: String = "Système")
class SettingsRepository(private val context: Context) {
 private val enabled = booleanPreferencesKey("enabled")
 private val automated = booleanPreferencesKey("automated")
 private val auto = booleanPreferencesKey("auto")
 private val theme = stringPreferencesKey("theme")
 val state = context.settings.data.map { Settings(it[enabled] ?: true, it[automated] ?: false, it[auto] ?: true, it[theme] ?: "Système") }
 suspend fun setEnabled(value: Boolean) { context.settings.edit { it[enabled] = value } }
 suspend fun setAutomated(value: Boolean) { context.settings.edit { it[automated] = value } }
 suspend fun setAuto(value: Boolean) { context.settings.edit { it[auto] = value } }
 suspend fun setTheme(value: String) { require(value in listOf("Système", "Clair", "Sombre")); context.settings.edit { it[theme] = value } }
}
