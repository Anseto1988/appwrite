package com.example.snacktrack.utils

import android.content.Context
import com.example.snacktrack.data.service.AppwriteConfig
import com.example.snacktrack.data.service.AppwriteClientHelper
import io.appwrite.Client
import io.appwrite.services.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Test-Klasse für die Appwrite-Verbindung
 */
class AppwriteConnectionTest(private val context: Context) {
    
    /**
     * Führt einen umfassenden Verbindungstest durch
     */
    suspend fun runConnectionTest(): String = withContext(Dispatchers.IO) {
        val results = StringBuilder()
        
        results.appendLine("=== APPWRITE CONNECTION TEST ===")
        results.appendLine("Timestamp: ${System.currentTimeMillis()}")
        results.appendLine()
        
        // Test 1: Basic Configuration
        results.appendLine("1. CONFIGURATION TEST:")
        results.appendLine("   Endpoint: ${AppwriteConfig.ENDPOINT}")
        results.appendLine("   Project ID: ${AppwriteConfig.PROJECT_ID}")
        results.appendLine("   Database ID: ${AppwriteConfig.DATABASE_ID}")
        results.appendLine()
        
        // Test 2: Client Creation
        results.appendLine("2. CLIENT CREATION TEST:")
        try {
            val client = AppwriteClientHelper.createUserClient(context)
            results.appendLine("   ✓ Client created successfully")
            
            // Test 3: Direct connection test
            results.appendLine()
            results.appendLine("3. CONNECTION TEST:")
            results.appendLine("   Skipping health check (not available in Android SDK)")
            
            // Test 4: Account Service
            results.appendLine()
            results.appendLine("4. ACCOUNT SERVICE TEST:")
            val account = Account(client)
            
            // Test 4a: Check current session
            try {
                val user = account.get()
                results.appendLine("   ✓ Active session found!")
                results.appendLine("   User ID: ${user.id}")
                results.appendLine("   Email: ${user.email}")
                results.appendLine("   Status: ${user.status}")
            } catch (e: Exception) {
                if (e.message?.contains("User (role: guests) missing scope (account)") == true) {
                    results.appendLine("   ℹ No active session (expected for new login)")
                } else {
                    results.appendLine("   ✗ Account error: ${e.message}")
                }
            }
            
            // Test 4b: List sessions
            try {
                val sessions = account.listSessions()
                results.appendLine()
                results.appendLine("5. SESSION LIST TEST:")
                results.appendLine("   Total sessions: ${sessions.sessions.size}")
                sessions.sessions.forEachIndexed { index, session ->
                    results.appendLine("   Session #${index + 1}:")
                    results.appendLine("     - ID: ${session.id}")
                    results.appendLine("     - Provider: ${session.provider}")
                    results.appendLine("     - Current: ${session.current}")
                }
            } catch (e: Exception) {
                results.appendLine()
                results.appendLine("5. SESSION LIST TEST:")
                results.appendLine("   ✗ Cannot list sessions: ${e.message}")
            }
            
        } catch (e: Exception) {
            results.appendLine("   ✗ Client creation failed: ${e.message}")
            results.appendLine("   Stack trace: ${e.stackTraceToString()}")
        }
        
        results.appendLine()
        results.appendLine("=== TEST COMPLETED ===")
        
        results.toString()
    }
    
    /**
     * Testet die Login-Funktionalität
     */
    suspend fun testLogin(email: String, password: String): String = withContext(Dispatchers.IO) {
        val results = StringBuilder()
        
        results.appendLine("=== LOGIN TEST ===")
        results.appendLine("Email: $email")
        results.appendLine()
        
        try {
            val client = AppwriteClientHelper.createUserClient(context)
            val account = Account(client)
            
            // Delete existing sessions
            results.appendLine("1. Clearing existing sessions...")
            try {
                account.deleteSessions()
                results.appendLine("   ✓ Sessions cleared")
            } catch (e: Exception) {
                results.appendLine("   ℹ No sessions to clear")
            }
            
            // Create new session
            results.appendLine()
            results.appendLine("2. Creating new session...")
            val session = account.createEmailSession(email, password)
            results.appendLine("   ✓ Session created!")
            results.appendLine("   Session ID: ${session.id}")
            results.appendLine("   Provider: ${session.provider}")
            results.appendLine("   User ID: ${session.userId}")
            
            // Verify user
            results.appendLine()
            results.appendLine("3. Verifying user...")
            val user = account.get()
            results.appendLine("   ✓ User verified!")
            results.appendLine("   ID: ${user.id}")
            results.appendLine("   Email: ${user.email}")
            results.appendLine("   Status: ${user.status}")
            
        } catch (e: Exception) {
            results.appendLine("✗ Login test failed!")
            results.appendLine("Error: ${e.message}")
            results.appendLine("Type: ${e.javaClass.simpleName}")
        }
        
        results.appendLine()
        results.appendLine("=== TEST COMPLETED ===")
        
        results.toString()
    }
}