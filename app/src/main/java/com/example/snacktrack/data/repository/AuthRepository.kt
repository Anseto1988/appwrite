package com.example.snacktrack.data.repository

import android.app.Activity
import android.content.Context
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

class AuthRepository(private val context: Context) {
    
    private val appwriteService = AppwriteService.getInstance(context)
    private val account = appwriteService.account
    
    /**
     * Registriert einen neuen Benutzer
     */
    suspend fun register(email: String, password: String, name: String): Result<User<Map<String, Any>>> = withContext(Dispatchers.IO) {
        try {
            val user = account.create(
                userId = ID.unique(),
                email = email,
                password = password,
                name = name
            )
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
            account.createEmailSession(email, password)
            Result.success(Unit)
        } catch (e: AppwriteException) {
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
    suspend fun loginWithGoogle(activity: Activity): Result<Unit> = withContext(Dispatchers.IO) {
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
} 