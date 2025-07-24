package com.example.snacktrack.utils

import android.util.Log
import com.example.snacktrack.BuildConfig

/**
 * Secure logging utility that sanitizes sensitive information
 * and only logs in debug builds
 */
object SecureLogger {
    
    private const val TAG_PREFIX = "SnackTrack_"
    
    /**
     * Sanitizes error messages to remove sensitive information
     */
    private fun sanitizeMessage(message: String?): String {
        if (message == null) return "Unknown error"
        
        return message
            // Remove email addresses
            .replace(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"), "[EMAIL]")
            // Remove potential passwords
            .replace(Regex("password[\"']?\\s*[:=]\\s*[\"']?[^\\s\"']+"), "password=[REDACTED]")
            // Remove API keys
            .replace(Regex("(api[_-]?key|apikey)[\"']?\\s*[:=]\\s*[\"']?[^\\s\"']+", RegexOption.IGNORE_CASE), "api_key=[REDACTED]")
            // Remove tokens
            .replace(Regex("(token|jwt|bearer)[\"']?\\s*[:=]\\s*[\"']?[^\\s\"']+", RegexOption.IGNORE_CASE), "token=[REDACTED]")
            // Remove session IDs
            .replace(Regex("session[_-]?id[\"']?\\s*[:=]\\s*[\"']?[^\\s\"']+", RegexOption.IGNORE_CASE), "session_id=[REDACTED]")
            // Remove user IDs that look like UUIDs
            .replace(Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"), "[UUID]")
    }
    
    /**
     * Logs debug messages only in debug builds
     */
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG_PREFIX + tag, sanitizeMessage(message))
        }
    }
    
    /**
     * Logs info messages
     */
    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG_PREFIX + tag, sanitizeMessage(message))
        }
    }
    
    /**
     * Logs warning messages
     */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.w(TAG_PREFIX + tag, sanitizeMessage(message), sanitizeThrowable(throwable))
            } else {
                Log.w(TAG_PREFIX + tag, sanitizeMessage(message))
            }
        }
    }
    
    /**
     * Logs error messages
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val sanitizedMessage = sanitizeMessage(message)
        
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.e(TAG_PREFIX + tag, sanitizedMessage, sanitizeThrowable(throwable))
            } else {
                Log.e(TAG_PREFIX + tag, sanitizedMessage)
            }
        } else {
            // In release, only log generic error without details
            Log.e(TAG_PREFIX + tag, "An error occurred")
        }
    }
    
    /**
     * Sanitizes throwable to remove sensitive information from stack traces
     */
    private fun sanitizeThrowable(throwable: Throwable): Throwable {
        return Exception(sanitizeMessage(throwable.message))
    }
    
    /**
     * Gets a user-friendly error message for display
     */
    fun getUserFriendlyError(throwable: Throwable): String {
        return when {
            throwable.message?.contains("401") == true -> "Bitte melden Sie sich erneut an"
            throwable.message?.contains("403") == true -> "Sie haben keine Berechtigung für diese Aktion"
            throwable.message?.contains("404") == true -> "Die angeforderten Daten wurden nicht gefunden"
            throwable.message?.contains("500") == true -> "Serverfehler. Bitte versuchen Sie es später erneut"
            throwable.message?.contains("network", ignoreCase = true) == true -> "Netzwerkfehler. Bitte überprüfen Sie Ihre Internetverbindung"
            throwable.message?.contains("timeout", ignoreCase = true) == true -> "Die Anfrage hat zu lange gedauert. Bitte versuchen Sie es erneut"
            else -> "Ein unerwarteter Fehler ist aufgetreten"
        }
    }
}