package com.example.snacktrack.data.service

import android.content.Context
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import io.appwrite.services.Realtime
import io.appwrite.services.Teams
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import com.example.snacktrack.utils.SecureLogger
import com.example.snacktrack.utils.NetworkManager

/**
 * Service-Klasse für die Kommunikation mit Appwrite
 */
class AppwriteService private constructor(context: Context) {
    
    companion object {
        const val ENDPOINT = AppwriteConfig.ENDPOINT
        const val PROJECT_ID = AppwriteConfig.PROJECT_ID
        const val DATABASE_ID = AppwriteConfig.DATABASE_ID
        const val API_KEY = AppwriteConfig.API_KEY
        
        // Collection IDs
        const val COLLECTION_DOGS = "dogs"
        const val COLLECTION_WEIGHT_ENTRIES = "weightEntries"
        const val COLLECTION_FOOD_INTAKE = "foodIntake"
        const val COLLECTION_FOOD_DB = "foodDB"
        const val COLLECTION_FOOD_SUBMISSIONS = "foodSubmissions"
        const val COLLECTION_TEAMS = "teams"
        const val COLLECTION_TEAM_MEMBERS = "team_members"
        const val COLLECTION_TEAM_INVITATIONS = "team_invitations"
        const val COLLECTION_DOG_SHARING = "dog_sharing"
        const val COLLECTION_HUNDERASSEN = "hunderassen"
        
        // Bucket IDs
        const val BUCKET_DOG_IMAGES = "dog_images"
        
        @Volatile
        private var instance: AppwriteService? = null
        
        fun getInstance(context: Context): AppwriteService {
            return instance ?: synchronized(this) {
                instance ?: AppwriteService(context).also { instance = it }
            }
        }
    }



    // Network connectivity manager
    val networkManager = NetworkManager(context)
    
    // Configure OkHttp with caching
    private val cacheSize = 50L * 1024L * 1024L // 50 MB cache
    private val cache = Cache(File(context.cacheDir, "http_cache"), cacheSize)
    
    private val okHttpClient = OkHttpClient.Builder()
        .cache(cache)
        .addInterceptor(NetworkCacheInterceptor())
        .addNetworkInterceptor(OfflineCacheInterceptor(networkManager))
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    // Appwrite Client with custom OkHttp configuration
    val client = Client(context).apply {
        setEndpoint(ENDPOINT)
        setProject(PROJECT_ID)
        setLocale("de-DE") // Set locale for error messages
        setSelfSigned(false) // Explicitly set to false for production
        // Note: API Key is typically used server-side. 
        // For Android apps, user authentication is the preferred method.
        // Configure with custom OkHttpClient if available
        // Note: This depends on the Appwrite SDK version - may need setHttpClient() or similar method
        try {
            val setHttpMethod = this::class.java.getDeclaredMethod("setHttpClient", OkHttpClient::class.java)
            setHttpMethod.invoke(this, okHttpClient)
        } catch (e: NoSuchMethodException) {
            // If method doesn't exist, try setHttp or other variants
            try {
                val setHttpMethod = this::class.java.getDeclaredMethod("setHttp", OkHttpClient::class.java)
                setHttpMethod.invoke(this, okHttpClient)
            } catch (e2: Exception) {
                SecureLogger.w("AppwriteService", "Could not set custom OkHttpClient: ${e2.message}")
            }
        }
    }
    
    // Appwrite Services
    val account = Account(client)
    val databases = Databases(client)
    val storage = Storage(client)
    val realtime = Realtime(client)
    val teams = Teams(client)
    
    /**
     * Prüft, ob ein Nutzer eingeloggt ist
     */
    suspend fun isLoggedIn(): Boolean {
        return try {
            account.get()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Stellt sicher, dass eine gültige Session vorhanden ist
     * Gibt true zurück, wenn eine gültige Session besteht oder erfolgreich aktualisiert wurde
     */
    suspend fun ensureValidSession(): Boolean {
        return try {
            // Try to get current account - this will fail if session is invalid
            account.get()
            true
        } catch (e: Exception) {
            SecureLogger.e("AppwriteService", "No valid session available", e)
            false
        }
    }
    
    /**
     * Gets the current user ID
     * @return The user ID or null if not logged in
     */
    suspend fun getCurrentUserId(): String? {
        return try {
            val user = account.get()
            user.id
        } catch (e: Exception) {
            SecureLogger.e("AppwriteService", "Failed to get current user ID", e)
            null
        }
    }
    
    /**
     * Gets the current user
     * @return The current user or null if not logged in
     */
    suspend fun getCurrentUser(): io.appwrite.models.User<Map<String, Any>>? {
        return try {
            account.get()
        } catch (e: Exception) {
            SecureLogger.e("AppwriteService", "Failed to get current user", e)
            null
        }
    }
} 