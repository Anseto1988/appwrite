package com.example.snacktrack.data.repository

import android.content.Context
import androidx.activity.ComponentActivity
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.User
import io.appwrite.ID
import io.appwrite.models.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import com.example.snacktrack.data.service.AppwriteService

class AuthRepository(private val context: Context) : BaseRepository() {
    
    private val appwriteService = AppwriteService.getInstance(context)
    private val account = appwriteService.account
    
    /**
     * Registriert einen neuen Benutzer und erstellt automatisch eine Session
     */
    suspend fun register(email: String, password: String, name: String): Result<User<Map<String, Any>>> = withContext(Dispatchers.IO) {
        try {
            // Create the user
            val user = account.create(
                userId = ID.unique(),
                email = email,
                password = password,
                name = name
            )
            
            // Automatically create a session for the new user
            try {
                account.createEmailSession(email, password)
            } catch (sessionError: AppwriteException) {
                // Log the error but don't fail registration
                android.util.Log.w("AuthRepository", "Could not create session after registration: ${sessionError.message}")
            }
            
            Result.success(user)
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
    
    /**
     * Meldet einen Benutzer an
     */
    suspend fun login(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // First, delete any existing sessions to ensure clean state
            try {
                account.deleteSessions()
            } catch (e: Exception) {
                // Ignore errors when deleting sessions
                android.util.Log.d("AuthRepository", "No existing sessions to delete: ${e.message}")
            }
            
            // Create new email session
            val session = account.createEmailSession(email, password)
            android.util.Log.d("AuthRepository", "Login successful. Session ID: ${session.id}, Provider: ${session.provider}")
            
            // Verify we can get the user
            val user = account.get()
            android.util.Log.d("AuthRepository", "User verified: ${user.email}, Status: ${user.status}")
            
            Result.success(Unit)
        } catch (e: AppwriteException) {
            android.util.Log.e("AuthRepository", "Login failed: ${e.message}, Code: ${e.code}, Type: ${e.type}")
            Result.failure(e)
        }
    }
    
    /**
     * Meldet den aktuellen Benutzer ab
     */
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            account.deleteSession("current")
            Result.success(Unit)
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
    
    /**
     * Holt den aktuell eingeloggten Benutzer
     */
    fun getCurrentUser(): Flow<User<Map<String, Any>>?> = flow {
        try {
            val user = account.get()
            emit(user)
        } catch (e: Exception) {
            emit(null)
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Prüft, ob der aktuelle Benutzer Admin-Rechte hat
     */
    fun isAdmin(): Flow<Boolean> = flow {
        try {
            val user = account.get()
            // Prüfen, ob der Benutzer das Label "admin" hat
            val labels = user.labels
            emit(labels.contains("admin"))
        } catch (e: Exception) {
            emit(false)
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Prüft, ob ein Benutzer eingeloggt ist
     */
    suspend fun isLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        try {
            account.get()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Startet Google OAuth2 Login
     */
    suspend fun loginWithGoogle(activity: ComponentActivity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            account.createOAuth2Session(
                activity = activity,
                provider = "google",
                success = "appwrite-callback-${appwriteService.client.config["project"]}://auth",
                failure = "appwrite-callback-${appwriteService.client.config["project"]}://auth"
            )
            Result.success(Unit)
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
    
    /**
     * Setzt das Passwort zurück
     */
    suspend fun resetPassword(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            account.createRecovery(email, "https://snacktrack.app/reset-password")
            Result.success(Unit)
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
    
    /**
     * Debug-Methode um Session-Status zu prüfen
     */
    suspend fun debugSessionStatus(): String = withContext(Dispatchers.IO) {
        try {
            // Try to get current user
            val user = account.get()
            val sessions = try {
                account.listSessions()
            } catch (e: Exception) {
                null
            }
            
            buildString {
                appendLine("=== SESSION DEBUG INFO ===")
                appendLine("User ID: ${user.id}")
                appendLine("User Email: ${user.email}")
                appendLine("User Name: ${user.name}")
                appendLine("Registration: ${user.registration}")
                appendLine("Status: ${user.status}")
                appendLine("Email Verification: ${user.emailVerification}")
                appendLine("Phone Verification: ${user.phoneVerification}")
                appendLine("Labels: ${user.labels.joinToString(", ")}")
                appendLine("Prefs: ${user.prefs}")
                
                if (sessions != null) {
                    appendLine("\n=== ACTIVE SESSIONS ===")
                    appendLine("Total Sessions: ${sessions.total}")
                    sessions.sessions.forEach { session ->
                        appendLine("\nSession ID: ${session.id}")
                        appendLine("Provider: ${session.provider}")
                        appendLine("Created: ${session.createdAt}")
                        appendLine("Expires: ${session.expire}")
                        appendLine("Current: ${session.current}")
                        appendLine("Device: ${session.deviceName}")
                        appendLine("OS: ${session.osName} ${session.osVersion}")
                    }
                } else {
                    appendLine("\n=== NO SESSIONS AVAILABLE ===")
                }
            }
        } catch (e: Exception) {
            "Session Debug Error: ${e.message}\n${e.stackTraceToString()}"
        }
    }
} 