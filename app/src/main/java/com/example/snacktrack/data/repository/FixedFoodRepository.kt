package com.example.snacktrack.data.repository

import android.content.Context
import android.util.Log
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.snacktrack.data.model.Food
import com.example.snacktrack.data.service.AppwriteService
import org.json.JSONObject

/**
 * Diese Hilfsmethode enthält den korrigierten Code für die Futter-Suche
 * Diese muss in FoodRepository.kt eingefügt werden
 */
suspend fun searchFoodFixed(context: Context, query: String): Result<List<Food>> = withContext(Dispatchers.IO) {
    try {
        // Debug-Logging
        Log.d("FoodRepository", "Suche nach Futter mit Suchbegriff: $query")
        
        val appwriteService = AppwriteService.getInstance(context)
        val databases = appwriteService.databases
        
        // Erstellen der Abfrage
        val queries = listOf(
            Query.search("product", query), 
            Query.limit(25)
        )
        
        Log.d("FoodRepository", "Verwendete Abfragen: $queries")
        
        val response = databases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = AppwriteService.COLLECTION_FOOD_DB,
            queries = queries
        )
        
        Log.d("FoodRepository", "Anzahl gefundener Ergebnisse: ${response.documents.size}")
        
        val foods = response.documents.map { doc ->
            Food(
                id = doc.id,
                ean = doc.data["ean"]?.toString() ?: "",
                brand = doc.data["brand"]?.toString() ?: "",
                product = doc.data["product"]?.toString() ?: "",
                protein = (doc.data["protein"] as? Number)?.toDouble() ?: 0.0,
                fat = (doc.data["fat"] as? Number)?.toDouble() ?: 0.0,
                crudeFiber = (doc.data["crudeFiber"] as? Number)?.toDouble() ?: 0.0,
                rawAsh = (doc.data["rawAsh"] as? Number)?.toDouble() ?: 0.0,
                moisture = (doc.data["moisture"] as? Number)?.toDouble() ?: 0.0,
                additives = (doc.data["additives"] as? String)?.let { jsonString ->
                    try {
                        val json = JSONObject(jsonString)
                        val map = mutableMapOf<String, String>()
                        json.keys().forEach { key ->
                            map[key] = json.getString(key)
                        }
                        map.toMap()
                    } catch (e: Exception) {
                        Log.w("FoodRepository", "Fehler beim Parsen der Additives JSON für Futter ${doc.id}: $jsonString", e)
                        emptyMap<String, String>()
                    }
                } ?: emptyMap<String, String>(),
                imageUrl = doc.data["imageUrl"]?.toString()
            )
        }
        
        if (foods.isNotEmpty()) {
            Log.d("FoodRepository", "Erstes Ergebnis: ${foods[0].product}")
        }
        
        Result.success(foods)
    } catch (e: AppwriteException) {
        Log.e("FoodRepository", "AppwriteException in searchFood: ${e.message}, Code: ${e.code}, Response: ${e.response}", e)
        Result.failure(e)
    } catch (e: Exception) {
        Log.e("FoodRepository", "Allgemeiner Fehler in searchFood: ${e.message}", e)
        Result.failure(e)
    }
}
