package com.example.snacktrack.data.service

import android.content.Context
import io.appwrite.Client
import io.appwrite.services.Account
import com.example.snacktrack.utils.SecureLogger

/**
 * Helper für die Erstellung von Appwrite Client Instanzen
 * Stellt sicher, dass keine API Keys im Client verwendet werden
 */
object AppwriteClientHelper {
    
    /**
     * Erstellt einen neuen Appwrite Client für User-Authentifizierung
     * WICHTIG: Verwendet KEINE API Keys, da diese User Sessions überschreiben
     */
    fun createUserClient(context: Context): Client {
        return Client(context).apply {
            setEndpoint(AppwriteConfig.ENDPOINT)
            setProject(AppwriteConfig.PROJECT_ID)
            setLocale("de-DE")
            setSelfSigned(false)
            
            // WICHTIG: Setze KEINEN API Key für Client Apps!
            // API Keys überschreiben User Sessions und verursachen "guest" Fehler
            
            SecureLogger.d("AppwriteClientHelper", "Client created without API key")
            SecureLogger.d("AppwriteClientHelper", "Endpoint: ${AppwriteConfig.ENDPOINT}")
            SecureLogger.d("AppwriteClientHelper", "Project: ${AppwriteConfig.PROJECT_ID}")
        }
    }
    
    /**
     * Verifiziert, dass der Client korrekt konfiguriert ist
     */
    suspend fun verifyClientConfiguration(client: Client): Boolean {
        return try {
            val account = Account(client)
            // Try to get the current session
            account.get()
            SecureLogger.d("AppwriteClientHelper", "Client configuration verified - session active")
            true
        } catch (e: Exception) {
            if (e.message?.contains("User (role: guests) missing scope (account)") == true) {
                SecureLogger.d("AppwriteClientHelper", "No active session - this is expected for new logins")
            } else {
                SecureLogger.e("AppwriteClientHelper", "Client configuration error", e)
            }
            false
        }
    }
}