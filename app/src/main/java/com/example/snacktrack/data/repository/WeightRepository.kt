package com.example.snacktrack.data.repository

import android.content.Context
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import com.example.snacktrack.data.model.WeightEntry
import com.example.snacktrack.data.service.AppwriteService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WeightRepository(private val context: Context) {
    
    private val appwriteService = AppwriteService.getInstance(context)
    private val databases = appwriteService.databases
    
    /**
     * Holt alle Gewichtseinträge für einen Hund
     */
    fun getWeightHistory(dogId: String): Flow<List<WeightEntry>> = flow {
        try {
            val response = databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_WEIGHT_ENTRIES,
                queries = listOf(
                    Query.equal("dogId", dogId),
                    Query.orderDesc("timestamp")
                )
            )
            
            val entries = response.documents.map { doc ->
                WeightEntry(
                    id = doc.id,
                    dogId = doc.data["dogId"].toString(),
                    weight = (doc.data["weight"] as? Number)?.toDouble() ?: 0.0,
                    timestamp = (doc.data["timestamp"] as? String)?.let {
                        LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
                    } ?: LocalDateTime.now(),
                    note = doc.data["notes"]?.toString() ?: doc.data["note"]?.toString()
                )
            }
            emit(entries)
        } catch (e: Exception) {
            emit(emptyList<WeightEntry>())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Fügt einen neuen Gewichtseintrag hinzu
     */
    suspend fun addWeightEntry(entry: WeightEntry): Result<WeightEntry> = withContext(Dispatchers.IO) {
        try {
            // Erstelle eine Map und filtere null-Werte heraus, um 'invalid type data' Fehler zu vermeiden
            val dataMap = mutableMapOf<String, Any>(
                "dogId" to entry.dogId,
                "weight" to entry.weight,
                "timestamp" to entry.timestamp.format(DateTimeFormatter.ISO_DATE_TIME)
            )
            
            // Füge optionale Felder nur hinzu, wenn sie nicht null sind
            entry.note?.let { dataMap["note"] = it }
            entry.note?.let { dataMap["notes"] = it }
            
            val response = databases.createDocument(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_WEIGHT_ENTRIES,
                documentId = ID.unique(),
                data = dataMap
            )
            
            Result.success(
                WeightEntry(
                    id = response.id,
                    dogId = response.data["dogId"].toString(),
                    weight = (response.data["weight"] as? Number)?.toDouble() ?: 0.0,
                    timestamp = (response.data["timestamp"] as? String)?.let {
                        LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
                    } ?: LocalDateTime.now(),
                    note = response.data["notes"]?.toString() ?: response.data["note"]?.toString()
                )
            )
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
    
    /**
     * Löscht einen Gewichtseintrag
     */
    suspend fun deleteWeightEntry(entryId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            databases.deleteDocument(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_WEIGHT_ENTRIES,
                documentId = entryId
            )
            Result.success(Unit)
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
}
