package com.example.snacktrack.data.repository

import android.content.Context
import android.util.Log
import com.example.snacktrack.data.model.Feeding
import com.example.snacktrack.data.service.AppwriteService
import com.example.snacktrack.data.service.AppwriteConfig
import com.example.snacktrack.utils.DateUtils
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.models.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Repository für Fütterungs-Daten
 */
class FeedingRepository(context: Context) : BaseRepository() {
    private val appwriteService = AppwriteService.getInstance(context)
    private val databases = appwriteService.databases
    
    /**
     * Fügt eine neue Fütterung hinzu
     */
    suspend fun addFeeding(feeding: Feeding): Result<Feeding> = withContext(Dispatchers.IO) {
        safeApiCall {
            val data = mapOf(
                "dogId" to feeding.dogId,
                "foodId" to feeding.foodId,
                "foodName" to feeding.foodName,
                "amount" to feeding.amount,
                "calories" to feeding.calories,
                "timestamp" to feeding.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "notes" to feeding.notes
            )
            
            val response = databases.createDocument(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_FEEDINGS,
                documentId = ID.unique(),
                data = data
            )
            
            convertDocumentToFeeding(response)
        }
    }
    
    /**
     * Lädt alle Fütterungen für einen Hund
     */
    fun getFeedingsForDog(dogId: String): Flow<List<Feeding>> = flow {
        try {
            Log.d("FeedingRepository", "Lade Fütterungen für Hund: $dogId")
            
            val response = databases.listDocuments(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_FEEDINGS,
                queries = listOf(
                    Query.equal("dogId", dogId),
                    Query.orderDesc("timestamp")
                )
            )
            
            val feedings = response.documents.map { doc ->
                convertDocumentToFeeding(doc)
            }
            
            Log.d("FeedingRepository", "Fütterungen geladen: ${feedings.size}")
            emit(feedings)
            
        } catch (e: Exception) {
            Log.e("FeedingRepository", "Fehler beim Laden der Fütterungen: ${e.message}", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Lädt Fütterungen nach Datum
     */
    fun getFeedingsByDate(dogId: String, date: LocalDate): Flow<List<Feeding>> = flow {
        try {
            val startOfDay = date.atStartOfDay()
            val endOfDay = date.plusDays(1).atStartOfDay()
            
            val response = databases.listDocuments(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_FEEDINGS,
                queries = listOf(
                    Query.equal("dogId", dogId),
                    Query.greaterThanEqual("timestamp", startOfDay.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)),
                    Query.lessThan("timestamp", endOfDay.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)),
                    Query.orderDesc("timestamp")
                )
            )
            
            val feedings = response.documents.map { doc ->
                convertDocumentToFeeding(doc)
            }
            
            emit(feedings)
            
        } catch (e: Exception) {
            Log.e("FeedingRepository", "Fehler beim Laden der Fütterungen nach Datum: ${e.message}", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Löscht eine Fütterung
     */
    suspend fun deleteFeeding(feedingId: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeApiCall {
            databases.deleteDocument(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_FEEDINGS,
                documentId = feedingId
            )
            Unit
        }
    }
    
    /**
     * Aktualisiert eine Fütterung
     */
    suspend fun updateFeeding(feeding: Feeding): Result<Feeding> = withContext(Dispatchers.IO) {
        safeApiCall {
            val data = mapOf(
                "dogId" to feeding.dogId,
                "foodId" to feeding.foodId,
                "foodName" to feeding.foodName,
                "amount" to feeding.amount,
                "calories" to feeding.calories,
                "timestamp" to feeding.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "notes" to feeding.notes
            )
            
            val response = databases.updateDocument(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_FEEDINGS,
                documentId = feeding.id,
                data = data
            )
            
            convertDocumentToFeeding(response)
        }
    }
    
    /**
     * Berechnet die täglichen Gesamtkalorien für einen Hund
     */
    fun getDailyCalories(dogId: String, date: LocalDate): Flow<Int> = flow {
        try {
            val feedings = getFeedingsByDate(dogId, date)
            feedings.collect { feedingList ->
                val totalCalories = feedingList.sumOf { it.calories }
                emit(totalCalories)
            }
        } catch (e: Exception) {
            Log.e("FeedingRepository", "Fehler beim Berechnen der Tageskalorien: ${e.message}", e)
            emit(0)
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Holt die letzte Fütterung für einen Hund
     */
    suspend fun getLastFeeding(dogId: String): Result<Feeding?> = withContext(Dispatchers.IO) {
        try {
            val response = databases.listDocuments(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_FEEDINGS,
                queries = listOf(
                    Query.equal("dogId", dogId),
                    Query.orderDesc("timestamp"),
                    Query.limit(1)
                )
            )
            
            val feeding = response.documents.firstOrNull()?.let { doc ->
                convertDocumentToFeeding(doc)
            }
            
            Result.success(feeding)
        } catch (e: Exception) {
            Log.e("FeedingRepository", "Fehler beim Abrufen der letzten Fütterung: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Hilfsfunktionen
    
    private fun convertDocumentToFeeding(doc: Document<Map<String, Any>>): Feeding {
        val timestamp = LocalDateTime.parse(
            doc.data["timestamp"].toString(),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        )
        return Feeding(
            id = doc.id,
            dogId = doc.data["dogId"].toString(),
            foodId = doc.data["foodId"]?.toString(),
            foodName = doc.data["foodName"].toString(),
            amount = (doc.data["amount"] as? Number)?.toDouble() ?: 0.0,
            calories = (doc.data["calories"] as? Number)?.toInt() ?: 0,
            timestamp = timestamp,
            date = timestamp.toLocalDate(),
            time = timestamp.toLocalTime(),
            notes = doc.data["notes"]?.toString()
        )
    }
}