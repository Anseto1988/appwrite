package com.example.snacktrack.data.service

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * OkHttp Interceptor for caching network responses
 */
class NetworkCacheInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val originalResponse = chain.proceed(request)
        
        // Only cache GET requests
        if (request.method != "GET") {
            return originalResponse
        }
        
        // Configure cache based on endpoint
        val cacheControl = when {
            // Cache food database for 24 hours
            request.url.toString().contains("foodDB") -> {
                CacheControl.Builder()
                    .maxAge(24, TimeUnit.HOURS)
                    .build()
            }
            // Cache dog breeds for 7 days
            request.url.toString().contains("hunderassen") -> {
                CacheControl.Builder()
                    .maxAge(7, TimeUnit.DAYS)
                    .build()
            }
            // Cache user profile data for 1 hour
            request.url.toString().contains("account") -> {
                CacheControl.Builder()
                    .maxAge(1, TimeUnit.HOURS)
                    .build()
            }
            // Cache images for 30 days
            request.url.toString().contains("files") && 
            (request.url.toString().contains("preview") || request.url.toString().contains("view")) -> {
                CacheControl.Builder()
                    .maxAge(30, TimeUnit.DAYS)
                    .build()
            }
            // Default cache for 5 minutes
            else -> {
                CacheControl.Builder()
                    .maxAge(5, TimeUnit.MINUTES)
                    .build()
            }
        }
        
        return originalResponse.newBuilder()
            .removeHeader("Pragma")
            .removeHeader("Cache-Control")
            .header("Cache-Control", cacheControl.toString())
            .build()
    }
}

/**
 * Offline cache interceptor to serve cached responses when offline
 */
class OfflineCacheInterceptor(private val networkManager: com.example.snacktrack.utils.NetworkManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        
        // Force cache usage when offline
        if (!networkManager.isNetworkAvailable()) {
            val cacheControl = CacheControl.Builder()
                .maxStale(7, TimeUnit.DAYS)
                .build()
            
            request = request.newBuilder()
                .cacheControl(cacheControl)
                .build()
        }
        
        return chain.proceed(request)
    }
}