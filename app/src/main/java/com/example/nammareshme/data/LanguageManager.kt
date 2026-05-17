package com.example.nammareshme.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class LanguageManager(private val context: Context) {
    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val UNREAD_NOTIFICATIONS_KEY = intPreferencesKey("unread_notifications")
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val LAST_SEEN_ALERTS_TIME_KEY = longPreferencesKey("last_seen_alerts_time")
        private val PROFILE_IMAGE_URI_KEY = stringPreferencesKey("profile_image_uri")
    }

    val getLanguage: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_KEY] ?: "English"
        }

    val getUnreadCount: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[UNREAD_NOTIFICATIONS_KEY] ?: 0
        }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }

    val getLastSeenAlertsTime: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_SEEN_ALERTS_TIME_KEY] ?: 0L
        }

    val getProfileImageUri: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PROFILE_IMAGE_URI_KEY]
        }

    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    suspend fun setUnreadCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[UNREAD_NOTIFICATIONS_KEY] = count
        }
    }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDark
        }
    }

    suspend fun setLastSeenAlertsTime(time: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SEEN_ALERTS_TIME_KEY] = time
        }
    }

    suspend fun saveProfileImageUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[PROFILE_IMAGE_URI_KEY] = uri
        }
    }
}
