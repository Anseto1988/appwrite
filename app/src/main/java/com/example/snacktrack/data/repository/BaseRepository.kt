package com.example.snacktrack.data.repository

import com.example.snacktrack.utils.SecureLogger
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import java.net.UnknownHostException
import java.net.SocketTimeoutException
import java.io.IOException

/**
 * Base repository class with common error handling and retry logic
 */
abstract class BaseRepository {
    
    companion object {
        private const val TAG = "BaseRepository"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 1000L
    }
    
    /**
     * Executes a suspend function with retry logic and error handling
     */
    protected suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): Result<T> {
        var lastException: Exception? = null
        
        repeat(MAX_RETRY_ATTEMPTS) { attempt ->
            try {
                val result = apiCall()
                return Result.success(result)
            } catch (e: CancellationException) {
                // Don't retry on cancellation
                throw e
            } catch (e: Exception) {
                lastException = e
                SecureLogger.e(TAG, "API call failed (attempt ${attempt + 1}/$MAX_RETRY_ATTEMPTS)", e)
                
                // Don't retry on certain errors
                when (e) {
                    is AppwriteException -> {
                        when (e.code) {
                            401 -> return Result.failure(UnauthorizedException("Unauthorized"))
                            403 -> return Result.failure(ForbiddenException("Forbidden"))
                            404 -> return Result.failure(NotFoundException("Not found"))
                            409 -> return Result.failure(ConflictException("Conflict"))
                            else -> {
                                if (e.code != null && e.code!! >= 400 && e.code!! < 500) {
                                    // Client errors - don't retry
                                    return Result.failure(e)
                                }
                            }
                        }
                    }
                }
                
                // Retry with exponential backoff
                if (attempt < MAX_RETRY_ATTEMPTS - 1) {
                    delay(RETRY_DELAY_MS * (attempt + 1))
                }
            }
        }
        
        // All retries failed
        return Result.failure(lastException ?: Exception("Unknown error"))
    }
    
    /**
     * Maps network and Appwrite exceptions to user-friendly messages
     */
    fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is UnauthorizedException -> "Bitte melden Sie sich erneut an"
            is ForbiddenException -> "Sie haben keine Berechtigung für diese Aktion"
            is NotFoundException -> "Die angeforderten Daten wurden nicht gefunden"
            is ConflictException -> "Es besteht ein Konflikt mit bestehenden Daten"
            is UnknownHostException -> "Keine Internetverbindung"
            is SocketTimeoutException -> "Die Anfrage hat zu lange gedauert"
            is IOException -> "Netzwerkfehler aufgetreten"
            is AppwriteException -> {
                when (throwable.code) {
                    500 -> "Serverfehler. Bitte versuchen Sie es später erneut"
                    else -> "Ein Fehler ist aufgetreten: ${throwable.message}"
                }
            }
            else -> "Ein unerwarteter Fehler ist aufgetreten"
        }
    }
    
    /**
     * Checks if an error is recoverable (worth retrying)
     */
    protected fun isRecoverableError(throwable: Throwable): Boolean {
        return when (throwable) {
            is UnknownHostException,
            is SocketTimeoutException,
            is IOException -> true
            is AppwriteException -> {
                // Server errors are potentially recoverable
                throwable.code != null && throwable.code!! >= 500
            }
            else -> false
        }
    }
}

// Custom exceptions for better error handling
class UnauthorizedException(message: String) : Exception(message)
class ForbiddenException(message: String) : Exception(message)
class NotFoundException(message: String) : Exception(message)
class ConflictException(message: String) : Exception(message)