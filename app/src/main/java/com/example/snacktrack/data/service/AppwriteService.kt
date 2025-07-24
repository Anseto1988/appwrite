package com.example.snacktrack.data.service

import android.content.Context
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import io.appwrite.services.Realtime
import io.appwrite.services.Teams

/**
 * Service-Klasse für die Kommunikation mit Appwrite
 */
class AppwriteService private constructor(context: Context) {
    
    companion object {
        const val ENDPOINT = "https://parse.nordburglarp.de/v1"
        const val PROJECT_ID = "snackrack2"
        const val DATABASE_ID = "snacktrack-db"
        
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



    // Appwrite Client
    val client = Client(context)
        .setEndpoint(ENDPOINT)
        .setProject(PROJECT_ID)
        .setSelfSigned(true) // Nur für Entwicklung, in Produktion entfernen
        .setLocale("de-DE") // Set locale for error messages
    
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
            // Prüfen, ob aktuelle Session gültig ist
            account.get()
            true
        } catch (e: Exception) {
            // Session möglicherweise abgelaufen
            android.util.Log.e("AppwriteService", "Session-Validierung fehlgeschlagen: ${e.message}", e)
            
            try {
                // Versuche, die Sessions zu listen, um zu sehen ob überhaupt welche existieren
                val sessions = account.listSessions()
                android.util.Log.d("AppwriteService", "Gefundene Sessions: ${sessions.sessions.size}")
                
                // Für Diagnose: Log Session-Details
                sessions.sessions.forEachIndexed { index, session ->
                    android.util.Log.d("AppwriteService", "Session $index - ID: ${session.id}, IP: ${session.ip}, Expiry: ${session.expire}")
                }
                
                // Die aktuelle Session ist bereits ungültig, daher keine direkte Erneuerung möglich
                // Hier könnten wir in Zukunft eine automatische Neuanmeldung implementieren
                // Für jetzt müssen wir leider dem Benutzer mitteilen, dass eine Neuanmeldung nötig ist
                
                android.util.Log.e("AppwriteService", "Session ist ungültig und konnte nicht automatisch erneuert werden")
                return false
                
            } catch (e2: Exception) {
                android.util.Log.e("AppwriteService", "Fehler beim Abrufen der Sessions: ${e2.message}", e2)
                return false
            }
        }
    }
} 