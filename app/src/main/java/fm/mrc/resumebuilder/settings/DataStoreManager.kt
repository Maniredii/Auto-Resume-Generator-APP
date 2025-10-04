package fm.mrc.resumebuilder.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore manager for app preferences
 */
class DataStoreManager(private val context: Context) {
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")
        
        // Preference keys
        private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
        private val DEFAULT_TEMPLATE_KEY = stringPreferencesKey("default_template")
        private val PREMIUM_FLAG_KEY = booleanPreferencesKey("premium_flag")
        private val SYNC_OPT_IN_KEY = booleanPreferencesKey("sync_opt_in")
        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        private val AUTO_SAVE_ENABLED_KEY = booleanPreferencesKey("auto_save_enabled")
    }
    
    /**
     * App preferences data class
     */
    data class AppPreferences(
        val isDarkTheme: Boolean = false,
        val defaultTemplate: String = "modern",
        val isPremiumUser: Boolean = false,
        val isSyncOptIn: Boolean = false,
        val isNotificationsEnabled: Boolean = true,
        val isAutoSaveEnabled: Boolean = true
    )
    
    /**
     * Get app preferences as Flow
     */
    val preferences: Flow<AppPreferences> = context.dataStore.data.map { preferences ->
        AppPreferences(
            isDarkTheme = preferences[DARK_THEME_KEY] ?: false,
            defaultTemplate = preferences[DEFAULT_TEMPLATE_KEY] ?: "modern",
            isPremiumUser = preferences[PREMIUM_FLAG_KEY] ?: false,
            isSyncOptIn = preferences[SYNC_OPT_IN_KEY] ?: false,
            isNotificationsEnabled = preferences[NOTIFICATIONS_ENABLED_KEY] ?: true,
            isAutoSaveEnabled = preferences[AUTO_SAVE_ENABLED_KEY] ?: true
        )
    }
    
    /**
     * Update dark theme preference
     */
    suspend fun updateDarkTheme(isDarkTheme: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_THEME_KEY] = isDarkTheme
        }
    }
    
    /**
     * Update default template preference
     */
    suspend fun updateDefaultTemplate(template: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_TEMPLATE_KEY] = template
        }
    }
    
    /**
     * Update premium flag
     */
    suspend fun updatePremiumFlag(isPremium: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PREMIUM_FLAG_KEY] = isPremium
        }
    }
    
    /**
     * Update sync opt-in preference
     */
    suspend fun updateSyncOptIn(isOptIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SYNC_OPT_IN_KEY] = isOptIn
        }
    }
    
    /**
     * Update notifications enabled preference
     */
    suspend fun updateNotificationsEnabled(isEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = isEnabled
        }
    }
    
    /**
     * Update auto save enabled preference
     */
    suspend fun updateAutoSaveEnabled(isEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_SAVE_ENABLED_KEY] = isEnabled
        }
    }
    
    /**
     * Clear all preferences
     */
    suspend fun clearAllPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
