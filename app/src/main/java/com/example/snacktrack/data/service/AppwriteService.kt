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
        val API_KEY = AppwriteConfig.API_KEY
        
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
    
    // Configure OkHttp with caching and cookie management
    private val cacheSize = 50L * 1024L * 1024L // 50 MB cache
    private val cache = Cache(File(context.cacheDir, "http_cache"), cacheSize)
    
    // Cookie manager for session persistence
    private val cookieJarManager = CookieJarManager(context)
    
    private val okHttpClient = OkHttpClient.Builder()
        .cache(cache)
        .cookieJar(cookieJarManager) // Add cookie management
        .addInterceptor(SessionInterceptor()) // Add session interceptor for debugging
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
        
        // IMPORTANT: Do NOT set API Key for client-side apps!
        // API Keys override user sessions and cause "guest" user issues
        // For Android apps, always use user authentication (email/password or OAuth)
        
        // Configure with custom OkHttpClient for cookie persistence
        // Try different methods to set the HTTP client
        var httpClientSet = false
        
        // Method 1: Try setHttpClient
        try {
            val setHttpClientMethod = this::class.java.getDeclaredMethod("setHttpClient", OkHttpClient::class.java)
            setHttpClientMethod.isAccessible = true
            setHttpClientMethod.invoke(this, okHttpClient)
            httpClientSet = true
            SecureLogger.d("AppwriteService", "Successfully set custom OkHttpClient using setHttpClient")
        } catch (e: Exception) {
            SecureLogger.d("AppwriteService", "setHttpClient not available: ${e.message}")
        }
        
        // Method 2: Try setHttp if setHttpClient didn't work
        if (!httpClientSet) {
            try {
                val setHttpMethod = this::class.java.getDeclaredMethod("setHttp", OkHttpClient::class.java)
                setHttpMethod.isAccessible = true
                setHttpMethod.invoke(this, okHttpClient)
                httpClientSet = true
                SecureLogger.d("AppwriteService", "Successfully set custom OkHttpClient using setHttp")
            } catch (e: Exception) {
                SecureLogger.d("AppwriteService", "setHttp not available: ${e.message}")
            }
        }
        
        // Method 3: Try direct field access
        if (!httpClientSet) {
            try {
                val httpField = this::class.java.getDeclaredField("http")
                httpField.isAccessible = true
                httpField.set(this, okHttpClient)
                httpClientSet = true
                SecureLogger.d("AppwriteService", "Successfully set custom OkHttpClient using field access")
            } catch (e: Exception) {
                SecureLogger.d("AppwriteService", "Direct field access failed: ${e.message}")
            }
        }
        
        if (!httpClientSet) {
            SecureLogger.e("AppwriteService", "WARNING: Could not set custom OkHttpClient - cookies may not persist!")
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
            val user = account.get()
            SecureLogger.d("AppwriteService", "Valid session found for user: ${user.email}")
            true
        } catch (e: Exception) {
            // Check if it's specifically the guest scope error
            if (e.message?.contains("User (role: guests) missing scope (account)") == true) {
                SecureLogger.d("AppwriteService", "User is not authenticated (guest user)")
            } else {
                SecureLogger.e("AppwriteService", "Session check failed", e)
            }
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