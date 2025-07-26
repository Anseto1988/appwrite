package com.example.snacktrack.data.service

import android.content.Context
import android.content.SharedPreferences
import com.example.snacktrack.utils.SecureLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manager für die Session-Verwaltung
 * Hilft bei der Persistenz von Sessions zwischen App-Neustarts
 */
class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_SESSION_ID = "session_id"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_SESSION_EXPIRE = "session_expire"
        
        @Volatile
        private var INSTANCE: SessionManager? = null
        
        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context).also { INSTANCE = it }
            }
        }
    }
    
    // Session state
    private val _isSessionActive = MutableStateFlow(hasValidSession())
    val isSessionActive: Flow<Boolean> = _isSessionActive.asStateFlow()
    
    /**
     * Speichert Session-Informationen
     */
    fun saveSession(sessionId: String, userId: String, userEmail: String, expireTime: Long) {
        prefs.edit().apply {
            putString(KEY_SESSION_ID, sessionId)
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, userEmail)
            putLong(KEY_SESSION_EXPIRE, expireTime)
            apply()
        }
        _isSessionActive.value = true
        SecureLogger.d("SessionManager", "Session saved for user: $userEmail")
    }
    
    /**
     * Löscht die gespeicherte Session
     */
    fun clearSession() {
        prefs.edit().apply {
            remove(KEY_SESSION_ID)
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            remove(KEY_SESSION_EXPIRE)
            apply()
        }
        _isSessionActive.value = false
        SecureLogger.d("SessionManager", "Session cleared")
    }
    
    /**
     * Prüft ob eine gültige Session existiert
     */
    fun hasValidSession(): Boolean {
        val sessionId = prefs.getString(KEY_SESSION_ID, null)
        val expireTime = prefs.getLong(KEY_SESSION_EXPIRE, 0)
        
        return sessionId != null && System.currentTimeMillis() < expireTime
    }
    
    /**
     * Gibt die gespeicherte User ID zurück
     */
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    
    /**
     * Gibt die gespeicherte User Email zurück
     */
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    
    /**
     * Gibt die Session ID zurück
     */
    fun getSessionId(): String? = prefs.getString(KEY_SESSION_ID, null)
}