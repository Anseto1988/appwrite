package com.example.snacktrack.data.service

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor für Session-Verwaltung
 * Stellt sicher, dass Session-Cookies korrekt behandelt werden
 */
class SessionInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Log request details
        android.util.Log.d("SessionInterceptor", "=== REQUEST ===")
        android.util.Log.d("SessionInterceptor", "URL: ${originalRequest.url}")
        android.util.Log.d("SessionInterceptor", "Method: ${originalRequest.method}")
        
        // Log cookies being sent
        val cookies = originalRequest.header("Cookie")
        if (cookies != null) {
            android.util.Log.d("SessionInterceptor", "Cookies: $cookies")
        } else {
            android.util.Log.d("SessionInterceptor", "No cookies in request")
        }
        
        // Add custom headers if needed
        val request = originalRequest.newBuilder()
            .addHeader("X-Appwrite-Response-Format", "1.0.0")
            .build()
        
        val response = chain.proceed(request)
        
        // Log response
        android.util.Log.d("SessionInterceptor", "=== RESPONSE ===")
        android.util.Log.d("SessionInterceptor", "Code: ${response.code}")
        
        // Log Set-Cookie headers
        val setCookies = response.headers("Set-Cookie")
        if (setCookies.isNotEmpty()) {
            android.util.Log.d("SessionInterceptor", "Set-Cookie headers received: ${setCookies.size}")
            setCookies.forEach { cookie ->
                android.util.Log.d("SessionInterceptor", "Set-Cookie: $cookie")
            }
        }
        
        return response
    }
}