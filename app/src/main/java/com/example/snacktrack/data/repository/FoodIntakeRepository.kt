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
import com.example.snacktrack.data.model.FoodIntake
import com.example.snacktrack.data.service.AppwriteService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FoodIntakeRepository(private val context: Context) {
    
    private val appwriteService = AppwriteService.getInstance(context)
    private val databases = appwriteService.databases
    
    /**
     * Holt alle Futtereinträge für einen Hund an einem bestimmten Tag
     */
    fun getFoodIntakesForDog(dogId: String, date: LocalDate): Flow<List<FoodIntake>> = flow {
        try {
            val startOfDay = date.atStartOfDay()
            val endOfDay = date.plusDays(1).atStartOfDay().minusSeconds(1)
            
            val response = databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_INTAKE,
                queries = listOf(
                    Query.equal("dogId", dogId),
                    Query.greaterThanEqual("timestamp", startOfDay.format(DateTimeFormatter.ISO_DATE_TIME)),
                    Query.lessThanEqual("timestamp", endOfDay.format(DateTimeFormatter.ISO_DATE_TIME)),
                    Query.orderDesc("timestamp")
                )
            )
            
            val intakes = response.documents.map { doc ->
                FoodIntake(
                    id = doc.id,
                    dogId = doc.data["dogId"].toString(),
                    foodId = doc.data["foodId"]?.toString(),
                    // Try "name" first (new schema), fallback to "foodName" (backwards compatibility)
                    foodName = doc.data["name"]?.toString() ?: doc.data["foodName"]?.toString() ?: "",
                    // Try "amount" first (new schema), fallback to "amountGram" (backwards compatibility)
                    amountGram = (doc.data["amount"] as? Number)?.toDouble() 
                        ?: (doc.data["amountGram"] as? Number)?.toDouble() ?: 0.0,
                    calories = (doc.data["calories"] as? Number)?.toInt() ?: 0,
                    timestamp = (doc.data["timestamp"] as? String)?.let {
                        LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
                    } ?: LocalDateTime.now(),
                    // Try "notes" first (new schema), fallback to "note" (backwards compatibility)
                    note = doc.data["notes"]?.toString() ?: doc.data["note"]?.toString(),
                    protein = (doc.data["protein"] as? Number)?.toDouble(),
                    fat = (doc.data["fat"] as? Number)?.toDouble(),
                    carbs = (doc.data["carbs"] as? Number)?.toDouble()
                )
            }
            emit(intakes)
        } catch (e: Exception) {
            emit(emptyList<FoodIntake>())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Fügt einen neuen Futtereintrag hinzu
     */
    suspend fun addFoodIntake(intake: FoodIntake): Result<FoodIntake> = withContext(Dispatchers.IO) {
        try {
            // Erstelle eine Map und filtere null-Werte heraus, um 'invalid type data' Fehler zu vermeiden
            val dataMap = mutableMapOf<String, Any>(
                "dogId" to intake.dogId,
                "name" to intake.foodName,  // Changed from "foodName" to "name" to match schema
                "amount" to intake.amountGram,  // Using "amount" as expected by schema
                "calories" to intake.calories,
                "date" to intake.timestamp.format(DateTimeFormatter.ISO_DATE_TIME)  // Using "date" for timestamp
            )
            
            // Füge optionale Felder nur hinzu, wenn sie nicht null sind
            intake.foodId?.let { dataMap["foodId"] = it }
            intake.note?.let { dataMap["notes"] = it }  // Changed from "note" to "notes" to match schema
            
            // Diese Felder sind nicht im Schema definiert, aber können als zusätzliche Daten gespeichert werden
            intake.protein?.let { dataMap["protein"] = it }
            intake.fat?.let { dataMap["fat"] = it }
            intake.carbs?.let { dataMap["carbs"] = it }
            
            // Behalte diese für Rückwärtskompatibilität, falls alte Clients sie noch verwenden
            dataMap["foodName"] = intake.foodName
            dataMap["amountGram"] = intake.amountGram
            dataMap["timestamp"] = intake.timestamp.format(DateTimeFormatter.ISO_DATE_TIME)
            
            val response = databases.createDocument(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_INTAKE,
                documentId = ID.unique(),
                data = dataMap
            )
            
            Result.success(
                FoodIntake(
                    id = response.id,
                    dogId = response.data["dogId"].toString(),
                    foodId = response.data["foodId"]?.toString(),
                    // Try "name" first (new schema), fallback to "foodName" (backwards compatibility)
                    foodName = response.data["name"]?.toString() ?: response.data["foodName"]?.toString() ?: "",
                    // Try "amount" first (new schema), fallback to "amountGram" (backwards compatibility)
                    amountGram = (response.data["amount"] as? Number)?.toDouble()
                        ?: (response.data["amountGram"] as? Number)?.toDouble() ?: 0.0,
                    calories = (response.data["calories"] as? Number)?.toInt() ?: 0,
                    // Try "date" first (new schema), fallback to "timestamp" (backwards compatibility)
                    timestamp = (response.data["date"] as? String)?.let {
                        LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
                    } ?: (response.data["timestamp"] as? String)?.let {
                        LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
                    } ?: LocalDateTime.now(),
                    // Try "notes" first (new schema), fallback to "note" (backwards compatibility)
                    note = response.data["notes"]?.toString() ?: response.data["note"]?.toString(),
                    protein = (response.data["protein"] as? Number)?.toDouble(),
                    fat = (response.data["fat"] as? Number)?.toDouble(),
                    carbs = (response.data["carbs"] as? Number)?.toDouble()
                )
            )
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
    
    /**
     * Löscht einen Futtereintrag
     */
    suspend fun deleteFoodIntake(intakeId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            databases.deleteDocument(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_INTAKE,
                documentId = intakeId
            )
            Result.success(Unit)
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
    
    /**
     * Berechnet die Gesamtkalorien für einen Hund an einem bestimmten Tag
     */
    suspend fun getTotalCaloriesForDay(dogId: String, date: LocalDate): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val startOfDay = date.atStartOfDay()
            val endOfDay = date.plusDays(1).atStartOfDay().minusSeconds(1)
            
            val response = databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_INTAKE,
                queries = listOf(
                    Query.equal("dogId", dogId),
                    Query.greaterThanEqual("date", startOfDay.format(DateTimeFormatter.ISO_DATE_TIME)),
                    Query.lessThanEqual("date", endOfDay.format(DateTimeFormatter.ISO_DATE_TIME))
                )
            )
            
            val totalCalories = response.documents.sumOf { doc ->
                (doc.data["calories"] as? Number)?.toInt() ?: 0
            }
            
            Result.success(totalCalories)
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
} 