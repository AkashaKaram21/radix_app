package com.radix.health.session

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "radix_session")

/**
 * Estado de sesión persistido en DataStore Preferences (siguiendo apuntes).
 *
 * Centraliza token, userId, role y preferencias del usuario para que el
 * resto de la app no acceda a SharedPreferences directamente.
 */
class SessionManager(private val context: Context) {

    private val keyToken = stringPreferencesKey("auth_token")
    private val keyUserId = longPreferencesKey("user_id")
    private val keyRole = stringPreferencesKey("user_role")
    private val keyMustChange = booleanPreferencesKey("must_change_password")
    private val keyRememberEmail = stringPreferencesKey("remember_email")
    private val keyOnboardingDone = booleanPreferencesKey("onboarding_done")
    private val keyApiBase = stringPreferencesKey("api_base_url")

    val token: Flow<String?> = context.dataStore.data.map { it[keyToken] }
    val userId: Flow<Long?> = context.dataStore.data.map { it[keyUserId] }
    val role: Flow<String?> = context.dataStore.data.map { it[keyRole] }
    val mustChangePassword: Flow<Boolean> =
        context.dataStore.data.map { it[keyMustChange] ?: false }
    val rememberEmail: Flow<String?> = context.dataStore.data.map { it[keyRememberEmail] }
    val onboardingDone: Flow<Boolean> =
        context.dataStore.data.map { it[keyOnboardingDone] ?: false }
    val apiBase: Flow<String?> = context.dataStore.data.map { it[keyApiBase] }

    suspend fun tokenOnce(): String? = context.dataStore.data.first()[keyToken]
    suspend fun userIdOnce(): Long? = context.dataStore.data.first()[keyUserId]
    suspend fun rememberEmailOnce(): String? =
        context.dataStore.data.first()[keyRememberEmail]
    suspend fun onboardingDoneOnce(): Boolean =
        context.dataStore.data.first()[keyOnboardingDone] ?: false
    suspend fun mustChangePasswordOnce(): Boolean =
        context.dataStore.data.first()[keyMustChange] ?: false

    suspend fun saveLogin(token: String, userId: Long, role: String?, mustChange: Boolean) {
        context.dataStore.edit {
            it[keyToken] = token
            it[keyUserId] = userId
            if (!role.isNullOrBlank()) it[keyRole] = role else it.remove(keyRole)
            it[keyMustChange] = mustChange
        }
    }

    suspend fun saveRememberEmail(email: String?) {
        context.dataStore.edit {
            if (email.isNullOrBlank()) it.remove(keyRememberEmail)
            else it[keyRememberEmail] = email
        }
    }

    suspend fun markOnboardingDone() {
        context.dataStore.edit { it[keyOnboardingDone] = true }
    }

    suspend fun clearMustChangePassword() {
        context.dataStore.edit { it[keyMustChange] = false }
    }

    suspend fun setApiBase(url: String) {
        context.dataStore.edit { it[keyApiBase] = url }
    }

    suspend fun clear() {
        context.dataStore.edit {
            it.remove(keyToken)
            it.remove(keyUserId)
            it.remove(keyRole)
            it.remove(keyMustChange)
        }
    }
}
