package com.example.snacktrack.data.service

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * Cookie Manager für Appwrite Session Cookies
 * Speichert Cookies persistent für Session-Verwaltung
 */
class CookieJarManager(private val context: Context) : CookieJar {
    
    private val cookieStore = mutableMapOf<String, List<Cookie>>()
    private val prefs = context.getSharedPreferences("appwrite_cookies", Context.MODE_PRIVATE)
    
    init {
        // Load saved cookies
        loadCookies()
    }
    
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        cookieStore[host] = cookies
        
        // Save cookies persistently
        val editor = prefs.edit()
        cookies.forEach { cookie ->
            val key = "${host}_${cookie.name}"
            val value = "${cookie.value}|${cookie.expiresAt}|${cookie.domain}|${cookie.path}"
            editor.putString(key, value)
            
            android.util.Log.d("CookieJarManager", "Saved cookie: ${cookie.name} = ${cookie.value} for ${host}")
        }
        editor.apply()
    }
    
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        val cookies = cookieStore[host] ?: emptyList()
        
        // Also check for cookies from the base domain
        val baseDomain = getBaseDomain(host)
        val domainCookies = cookieStore[baseDomain] ?: emptyList()
        
        val allCookies = (cookies + domainCookies).distinctBy { "${it.domain}|${it.name}" }
        
        android.util.Log.d("CookieJarManager", "Loading ${allCookies.size} cookies for ${host}")
        allCookies.forEach { cookie ->
            android.util.Log.d("CookieJarManager", "  - ${cookie.name} = ${cookie.value}")
        }
        
        return allCookies
    }
    
    private fun loadCookies() {
        prefs.all.forEach { (key, value) ->
            if (value is String) {
                try {
                    val parts = value.split("|")
                    if (parts.size >= 4) {
                        val host = key.substringBefore("_")
                        val name = key.substringAfter("_")
                        val cookieValue = parts[0]
                        val expiresAt = parts[1].toLong()
                        val domain = parts[2]
                        val path = parts[3]
                        
                        // Only load non-expired cookies
                        if (expiresAt > System.currentTimeMillis()) {
                            val cookie = Cookie.Builder()
                                .name(name)
                                .value(cookieValue)
                                .domain(domain)
                                .path(path)
                                .expiresAt(expiresAt)
                                .build()
                            
                            val existingCookies = cookieStore[host] ?: emptyList()
                            cookieStore[host] = existingCookies + cookie
                            
                            android.util.Log.d("CookieJarManager", "Loaded saved cookie: $name for $host")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CookieJarManager", "Error loading cookie: $key", e)
                }
            }
        }
    }
    
    private fun getBaseDomain(host: String): String {
        val parts = host.split(".")
        return if (parts.size > 2) {
            parts.takeLast(2).joinToString(".")
        } else {
            host
        }
    }
    
    fun clearAllCookies() {
        cookieStore.clear()
        prefs.edit().clear().apply()
        android.util.Log.d("CookieJarManager", "All cookies cleared")
    }
}