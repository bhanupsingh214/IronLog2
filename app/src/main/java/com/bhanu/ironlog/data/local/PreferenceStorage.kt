package com.bhanu.ironlog.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ironlog_prefs"
)

@Singleton
class PreferenceStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    val lastCloudBackupTimestamp: Flow<Long> = dataStore.data.map { prefs ->
        prefs[KEY_LAST_CLOUD_BACKUP] ?: 0L
    }

    suspend fun setLastCloudBackupTimestamp(timestamp: Long) {
        dataStore.edit { prefs ->
            prefs[KEY_LAST_CLOUD_BACKUP] = timestamp
        }
    }

    val userEmail: Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_USER_EMAIL]
    }

    suspend fun setUserEmail(email: String?) {
        dataStore.edit { prefs ->
            if (email == null) {
                prefs.remove(KEY_USER_EMAIL)
            } else {
                prefs[KEY_USER_EMAIL] = email
            }
        }
    }

    val userDisplayName: Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_USER_DISPLAY_NAME]
    }

    suspend fun setUserDisplayName(name: String?) {
        dataStore.edit { prefs ->
            if (name == null) {
                prefs.remove(KEY_USER_DISPLAY_NAME)
            } else {
                prefs[KEY_USER_DISPLAY_NAME] = name
            }
        }
    }

    // Helper for non-flow access where needed (e.g. initial state in AccountRepository)
    suspend fun getUserEmailOnce(): String? = userEmail.first()
    suspend fun getUserDisplayNameOnce(): String? = userDisplayName.first()
    suspend fun getLastCloudBackupTimestampOnce(): Long = lastCloudBackupTimestamp.first()

    companion object {
        private val KEY_LAST_CLOUD_BACKUP = longPreferencesKey("last_cloud_backup_ts")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_DISPLAY_NAME = stringPreferencesKey("user_display_name")
    }
}
