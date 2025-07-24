package com.example.snacktrack.utils

import android.content.Context
import com.example.snacktrack.data.service.AppwriteService
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

/**
 * Manages user session lifecycle and automatic refresh
 */
class SessionManager(private val context: Context) {
    
    private val appwriteService = AppwriteService.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Unknown)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()
    
    private var sessionCheckJob: Job? = null
    private var lastSessionCheckTime = 0L
    
    companion object {
        private const val TAG = "SessionManager"
        private const val SESSION_CHECK_INTERVAL = 5 * 60 * 1000L // 5 minutes
        private const val SESSION_REFRESH_THRESHOLD = 30 * 60 * 1000L // 30 minutes
        
        @Volatile
        private var instance: SessionManager? = null
        
        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context).also { instance = it }
            }
        }
    }
    
    init {
        startSessionMonitoring()
    }
    
    /**
     * Starts monitoring session status
     */
    private fun startSessionMonitoring() {
        sessionCheckJob?.cancel()
        
        sessionCheckJob = scope.launch {
            while (isActive) {
                checkSession()
                delay(SESSION_CHECK_INTERVAL)
            }
        }
    }
    
    /**
     * Checks current session status
     */
    suspend fun checkSession() {
        try {
            val currentTime = System.currentTimeMillis()
            
            // Avoid checking too frequently
            if (currentTime - lastSessionCheckTime < 10000) { // 10 seconds
                return
            }
            
            lastSessionCheckTime = currentTime
            
            // Try to get current user
            val user = appwriteService.account.get()
            
            // Check sessions
            val sessions = appwriteService.account.listSessions()
            val currentSession = sessions.sessions.firstOrNull { it.current }
            
            if (currentSession != null) {
                val expiryTime = parseExpiryTime(currentSession.expire)
                val timeUntilExpiry = expiryTime - currentTime
                
                when {
                    timeUntilExpiry <= 0 -> {
                        _sessionState.value = SessionState.Expired
                        SecureLogger.w(TAG, "Session has expired")
                    }
                    timeUntilExpiry <= SESSION_REFRESH_THRESHOLD -> {
                        _sessionState.value = SessionState.NeedsRefresh
                        SecureLogger.d(TAG, "Session needs refresh")
                        // Attempt to refresh session
                        refreshSession()
                    }
                    else -> {
                        _sessionState.value = SessionState.Active
                        SecureLogger.d(TAG, "Session is active")
                    }
                }
            } else {
                _sessionState.value = SessionState.NoSession
                SecureLogger.w(TAG, "No active session found")
            }
        } catch (e: AppwriteException) {
            when (e.code) {
                401 -> {
                    _sessionState.value = SessionState.Expired
                    SecureLogger.e(TAG, "Session check failed - unauthorized", e)
                }
                else -> {
                    SecureLogger.e(TAG, "Session check failed", e)
                }
            }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Unexpected error during session check", e)
        }
    }
    
    /**
     * Attempts to refresh the session
     */
    private suspend fun refreshSession() {
        try {
            // Get current session
            val sessions = appwriteService.account.listSessions()
            val currentSession = sessions.sessions.firstOrNull { it.current }
            
            if (currentSession != null) {
                // Update session by calling get() which refreshes the session
                appwriteService.account.get()
                _sessionState.value = SessionState.Active
                SecureLogger.d(TAG, "Session refreshed successfully")
            } else {
                _sessionState.value = SessionState.NoSession
            }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Failed to refresh session", e)
            _sessionState.value = SessionState.Expired
        }
    }
    
    /**
     * Forces a session refresh
     */
    suspend fun forceRefresh(): Boolean {
        return try {
            checkSession()
            when (_sessionState.value) {
                is SessionState.Active -> true
                is SessionState.NeedsRefresh -> {
                    refreshSession()
                    _sessionState.value is SessionState.Active
                }
                else -> false
            }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Force refresh failed", e)
            false
        }
    }
    
    /**
     * Parses expiry time from ISO string
     */
    private fun parseExpiryTime(expiry: String): Long {
        return try {
            // Parse ISO 8601 date string
            val instant = java.time.Instant.parse(expiry)
            instant.toEpochMilli()
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Failed to parse expiry time", e)
            System.currentTimeMillis() // Return current time as fallback
        }
    }
    
    /**
     * Cleans up resources
     */
    fun cleanup() {
        sessionCheckJob?.cancel()
        scope.cancel()
    }
}

/**
 * Represents different session states
 */
sealed class SessionState {
    object Unknown : SessionState()
    object NoSession : SessionState()
    object Active : SessionState()
    object NeedsRefresh : SessionState()
    object Expired : SessionState()
}