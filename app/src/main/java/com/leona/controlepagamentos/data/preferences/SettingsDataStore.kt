package com.leona.controlepagamentos.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsStore by preferencesDataStore(name = "payment_settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val captureEnabled: Boolean = true,
    val firstFinancialDay: Int = 1,
    val defaultCategoryId: String = "outros",
    val currencyCode: String = "BRL",
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

class SettingsDataStore(private val context: Context) {
    val settings: Flow<AppSettings> = context.settingsStore.data.map { preferences ->
        AppSettings(
            captureEnabled = preferences[Keys.CAPTURE_ENABLED] ?: true,
            firstFinancialDay = preferences[Keys.FIRST_FINANCIAL_DAY] ?: 1,
            defaultCategoryId = preferences[Keys.DEFAULT_CATEGORY_ID] ?: "outros",
            currencyCode = preferences[Keys.CURRENCY_CODE] ?: "BRL",
            themeMode = ThemeMode.entries.firstOrNull { it.name == preferences[Keys.THEME_MODE] }
                ?: ThemeMode.SYSTEM
        )
    }

    suspend fun setCaptureEnabled(enabled: Boolean) {
        context.settingsStore.edit { it[Keys.CAPTURE_ENABLED] = enabled }
    }

    suspend fun setFirstFinancialDay(day: Int) {
        context.settingsStore.edit { it[Keys.FIRST_FINANCIAL_DAY] = day.coerceIn(1, 28) }
    }

    suspend fun setDefaultCategory(categoryId: String) {
        context.settingsStore.edit { it[Keys.DEFAULT_CATEGORY_ID] = categoryId }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    private object Keys {
        val CAPTURE_ENABLED = booleanPreferencesKey("capture_enabled")
        val FIRST_FINANCIAL_DAY = intPreferencesKey("first_financial_day")
        val DEFAULT_CATEGORY_ID = stringPreferencesKey("default_category_id")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
