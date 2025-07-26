package com.example.snacktrack.data.repository

import android.content.Context
import android.util.Log // Import für Logging
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import com.example.snacktrack.data.model.Food
import com.example.snacktrack.data.model.FoodSubmission
import com.example.snacktrack.data.model.SubmissionStatus
import com.example.snacktrack.data.service.AppwriteService
import org.json.JSONObject // Import für JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FoodRepository(private val context: Context) {

    private val appwriteService = AppwriteService.getInstance(context)
    private val databases = appwriteService.databases

    /**
     * Lädt alle Futtermittel aus der Datenbank
     */
    suspend fun getAllFoods(): Result<List<Food>> = withContext(Dispatchers.IO) {
        try {
            val response = databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_DB,
                queries = listOf(Query.limit(100))
            )
            
            val foods = response.documents.map { documentToFood(it) }
            Result.success(foods)
        } catch (e: Exception) {
            Log.e("FoodRepository", "Error getting all foods: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sucht nach Lebensmitteln basierend auf einem Suchbegriff (veraltet, siehe searchFood unten)
     */
    @Deprecated("Use searchFood which returns Result<List<Food>>", ReplaceWith("searchFood(query)"))
    fun searchFoods(query: String): Flow<List<Food>> = flow {
        try {
            val response = databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_DB,
                queries = listOf(
                    Query.search("product", query), // Sucht im Produktnamen
                    // Optional: Query.search("brand", query) // Könnte auch in der Marke suchen
                    Query.limit(50)
                )
            )

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
                            emptyMap<String, String>()
                        }
                    } ?: emptyMap<String, String>(),
                    imageUrl = doc.data["imageUrl"]?.toString()
                )
            }
            emit(foods)
        } catch (e: Exception) {
            Log.e("FoodRepository", "Exception in searchFoods (Flow): ${e.message}", e)
            emit(emptyList<Food>())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Holt ein Lebensmittel anhand der ID
     */
    suspend fun getFoodById(foodId: String): Result<Food> = withContext(Dispatchers.IO) {
        try {
            val response = databases.getDocument(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_DB,
                documentId = foodId
            )

            val food = Food(
                id = response.id,
                ean = response.data["ean"]?.toString() ?: "",
                brand = response.data["brand"]?.toString() ?: "",
                product = response.data["product"]?.toString() ?: "",
                protein = (response.data["protein"] as? Number)?.toDouble() ?: 0.0,
                fat = (response.data["fat"] as? Number)?.toDouble() ?: 0.0,
                crudeFiber = (response.data["crudeFiber"] as? Number)?.toDouble() ?: 0.0,
                rawAsh = (response.data["rawAsh"] as? Number)?.toDouble() ?: 0.0,
                moisture = (response.data["moisture"] as? Number)?.toDouble() ?: 0.0,
                additives = (response.data["additives"] as? String)?.let { jsonString ->
                    try {
                        val json = JSONObject(jsonString)
                        val map = mutableMapOf<String, String>()
                        json.keys().forEach { key ->
                            map[key] = json.getString(key)
                        }
                        map.toMap()
                    } catch (e: Exception) {
                        emptyMap<String, String>()
                    }
                } ?: emptyMap<String, String>(),
                imageUrl = response.data["imageUrl"]?.toString()
            )

            Result.success(food)
        } catch (e: AppwriteException) {
            Log.e("FoodRepository", "AppwriteException in getFoodById: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Sucht nach einem Lebensmittel anhand des EAN/Barcodes
     * 
     * Diese verbesserte Version normalisiert den Barcode und schreibt detaillierte Logs
     */
    suspend fun getFoodByEAN(ean: String): Result<Food?> = withContext(Dispatchers.IO) {
        try {
            // Normalisiere den Barcode (entferne Leerzeichen und führende Nullen)
            val normalizedEan = ean.trim()
            
            Log.d("FoodRepository", "Suche nach Produkt mit EAN: $normalizedEan")
            
            // Versuche zuerst eine exakte Suche
            val response = databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_DB,
                queries = listOf(Query.equal("ean", normalizedEan))
            )
            
            Log.d("FoodRepository", "Anzahl gefundener Dokumente: ${response.documents.size}")
            
            if (response.documents.isNotEmpty()) {
                val doc = response.documents.first()
                Log.d("FoodRepository", "Produkt gefunden: ID=${doc.id}, EAN=${doc.data["ean"]}, Name=${doc.data["product"]}")
                
                val food = Food(
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
                            emptyMap<String, String>()
                        }
                    } ?: emptyMap<String, String>(),
                    imageUrl = doc.data["imageUrl"]?.toString()
                )
                Result.success(food)
            } else {
                // Wenn keine exakte Übereinstimmung gefunden wurde, versuchen wir eine flexiblere Suche
                // Diese zweite Suche ist optional, abhängig von den Anforderungen
                Log.d("FoodRepository", "Kein Produkt mit exakter EAN-Übereinstimmung gefunden")
                Result.success(null)
            }
        } catch (e: AppwriteException) {
            Log.e("FoodRepository", "AppwriteException in getFoodByEAN: ${e.message}")
            Result.failure(e)
        }
    }


    
    /**
     * Reicht ein neues Lebensmittel zur Aufnahme in die Datenbank ein (veraltet, nutze submitFoodEntry)
     */
    @Deprecated("Use submitFoodEntry instead", ReplaceWith("submitFoodEntry(food.toFoodSubmission())"))
    suspend fun submitFoodSubmission(food: Food): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Diese Methode ist weniger präzise als submitFoodEntry, da sie Food statt FoodSubmission verwendet.
            // Es wird empfohlen, submitFoodEntry zu verwenden.
            val user = appwriteService.account.get() // Annahme: Der einreichende Benutzer ist der aktuell eingeloggte.
            val additivesJsonString = JSONObject(food.additives as Map<*, *>).toString()

            databases.createDocument(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_SUBMISSIONS,
                documentId = ID.unique(),
                data = mapOf(
                    "userId" to user.id,
                    "ean" to food.ean,
                    "brand" to food.brand,
                    "product" to food.product,
                    "protein" to food.protein,
                    "fat" to food.fat,
                    "crudeFiber" to food.crudeFiber,
                    "rawAsh" to food.rawAsh,
                    "moisture" to food.moisture,
                    "additives" to additivesJsonString,
                    "imageUrl" to food.imageUrl,
                    "status" to SubmissionStatus.PENDING.name, // Standardstatus
                    "submittedAt" to LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                )
            )
            Result.success(Unit)
        } catch (e: AppwriteException) {
            Log.e("FoodRepository", "AppwriteException in submitFoodSubmission (deprecated): ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Erstellt einen neuen Vorschlag für die Futterdatenbank
     */
    suspend fun submitFoodEntry(submission: FoodSubmission): Result<FoodSubmission> = withContext(Dispatchers.IO) {
        try {
            val user = appwriteService.account.get()

            // Konvertiere die additives Map in einen JSON String
            val additivesJsonString = JSONObject(submission.additives as Map<*, *>).toString()

            val data = mapOf(
                "userId" to user.id,
                "ean" to submission.ean,
                "brand" to submission.brand,
                "product" to submission.product,
                "protein" to submission.protein,
                "fat" to submission.fat,
                "crudeFiber" to submission.crudeFiber,
                "rawAsh" to submission.rawAsh,
                "moisture" to submission.moisture,
                "additives" to additivesJsonString, // Hier den JSON-String verwenden
                "imageUrl" to submission.imageUrl,
                "status" to submission.status.name,
                "submittedAt" to submission.submittedAt.format(DateTimeFormatter.ISO_DATE_TIME)
            )

            val response = databases.createDocument(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_SUBMISSIONS,
                documentId = ID.unique(),
                data = data
            )

            Result.success(
                FoodSubmission(
                    id = response.id,
                    userId = response.data["userId"].toString(),
                    ean = response.data["ean"].toString(),
                    brand = response.data["brand"].toString(),
                    product = response.data["product"].toString(),
                    protein = (response.data["protein"] as? Number)?.toDouble() ?: 0.0,
                    fat = (response.data["fat"] as? Number)?.toDouble() ?: 0.0,
                    crudeFiber = (response.data["crudeFiber"] as? Number)?.toDouble() ?: 0.0,
                    rawAsh = (response.data["rawAsh"] as? Number)?.toDouble() ?: 0.0,
                    moisture = (response.data["moisture"] as? Number)?.toDouble() ?: 0.0,
                    additives = (response.data["additives"] as? String)?.let { jsonString ->
                        try {
                            val json = JSONObject(jsonString)
                            val map = mutableMapOf<String, String>()
                            json.keys().forEach { key ->
                                map[key] = json.getString(key)
                            }
                            map.toMap()
                        } catch (e: Exception) {
                            submission.additives // Fallback auf die ursprünglich gesendeten Additives
                        }
                    } ?: submission.additives, // Fallback
                    imageUrl = response.data["imageUrl"]?.toString(),
                    status = response.data["status"]?.toString()?.let {
                        try { enumValueOf<SubmissionStatus>(it) } catch (e: IllegalArgumentException) { SubmissionStatus.PENDING }
                    } ?: SubmissionStatus.PENDING,
                    submittedAt = (response.data["submittedAt"] as? String)?.let {
                        LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
                    } ?: LocalDateTime.now(),
                    reviewedAt = (response.data["reviewedAt"] as? String)?.let {
                        LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
                    }
                )
            )
        } catch (e: AppwriteException) {
            Log.e("FoodRepository", "AppwriteException in submitFoodEntry: ${e.message}, Code: ${e.code}, Response: ${e.response}", e)
            Result.failure(e)
        } catch (e: Exception) { // Fange auch andere Exceptions ab (z.B. von JSONObject)
            Log.e("FoodRepository", "General Exception in submitFoodEntry: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Gibt alle Futtermittel-Submissions zurück, die auf Überprüfung warten
     */
    fun getAllFoodSubmissions(): Flow<List<FoodSubmission>> = flow {
        try {
            val response = databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_SUBMISSIONS,
                queries = listOf(
                    Query.limit(100) // Limitiere auf die ersten 100 Submissions
                )
            )
            
            val submissions = response.documents.map { doc ->
                FoodSubmission(
                    id = doc.id,
                    userId = doc.data["userId"]?.toString() ?: "",
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
                            mapOf() // Fallback auf leere Map
                        }
                    } ?: mapOf(),
                    imageUrl = doc.data["imageUrl"]?.toString(),
                    status = doc.data["status"]?.toString()?.let {
                        try { enumValueOf<SubmissionStatus>(it) } catch (e: IllegalArgumentException) { SubmissionStatus.PENDING }
                    } ?: SubmissionStatus.PENDING,
                    submittedAt = (doc.data["submittedAt"] as? String)?.let {
                        try {
                            LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
                        } catch (e: Exception) {
                            LocalDateTime.now()
                        }
                    } ?: LocalDateTime.now(),
                    reviewedAt = (doc.data["reviewedAt"] as? String)?.let {
                        try {
                            LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
                        } catch (e: Exception) {
                            null
                        }
                    }
                )
            }
            
            emit(submissions)
        } catch (e: Exception) {
            Log.e("FoodRepository", "Error getting food submissions: ${e.message}", e)
            emit(emptyList<FoodSubmission>())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Überprüft eine Food-Submission und fügt sie der Food-DB hinzu, wenn sie genehmigt wird
     * @param submission Die zu überprüfende Submission
     * @param approve Ob die Submission genehmigt werden soll
     * @return Ein Result mit der aktualisierten Submission oder einem Fehler
     */
    suspend fun reviewFoodSubmission(submission: FoodSubmission, approve: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Aktualisiere den Status der Submission
            val newStatus = if (approve) SubmissionStatus.APPROVED else SubmissionStatus.REJECTED
            val reviewedAt = LocalDateTime.now()
            
            // 1. Aktualisiere die Submission mit dem neuen Status
            val updatedData = mapOf(
                "status" to newStatus.name,
                "reviewedAt" to reviewedAt.format(DateTimeFormatter.ISO_DATE_TIME)
            )
            
            databases.updateDocument(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_SUBMISSIONS,
                documentId = submission.id,
                data = updatedData
            )
            
            // 2. Wenn genehmigt, füge sie der Food-DB hinzu
            if (approve) {
                // Konvertiere Additives zu JSON String
                val additivesJsonString = JSONObject(submission.additives as Map<*, *>).toString()
                
                val foodData = mapOf(
                    "ean" to submission.ean,
                    "brand" to submission.brand,
                    "product" to submission.product,
                    "protein" to submission.protein,
                    "fat" to submission.fat,
                    "crudeFiber" to submission.crudeFiber,
                    "rawAsh" to submission.rawAsh,
                    "moisture" to submission.moisture,
                    "additives" to additivesJsonString,
                    "imageUrl" to submission.imageUrl
                )
                
                databases.createDocument(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.COLLECTION_FOOD_DB,
                    documentId = ID.unique(),
                    data = foodData
                )
                
                // 3. Lösche die Submission aus der Submissions-Collection
                databases.deleteDocument(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.COLLECTION_FOOD_SUBMISSIONS,
                    documentId = submission.id
                )
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FoodRepository", "Error reviewing food submission: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Löscht eine Food-Submission
     */
    suspend fun deleteFoodSubmission(submissionId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            databases.deleteDocument(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_SUBMISSIONS,
                documentId = submissionId
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FoodRepository", "Error deleting food submission: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Sucht Futtermittel anhand eines Suchbegriffs mit erweiterter Suche in Produktnamen und Marken.
     * Gibt ein Result-Objekt zurück, das entweder eine Liste von Food-Objekten oder eine Exception enthält.
     * Entfernt Duplikate basierend auf der ID.
     */
    suspend fun searchFood(query: String): Result<List<Food>> = withContext(Dispatchers.IO) {
        try {
            // Debug-Logging
            Log.d("FoodRepository", "Suche nach Futter mit Suchbegriff: $query")
            
            if (query.length < 2) {
                return@withContext Result.success(emptyList())
            }
            
            // Ergebnisse für Produkte suchen
            val productResults = try {
                databases.listDocuments(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.COLLECTION_FOOD_DB,
                    queries = listOf(
                        Query.search("product", query),
                        Query.limit(25)
                    )
                )
            } catch (e: Exception) {
                Log.e("FoodRepository", "Fehler bei der Produktsuche: ${e.message}", e)
                null
            }
            
            // Ergebnisse für Marken suchen
            val brandResults = try {
                databases.listDocuments(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.COLLECTION_FOOD_DB,
                    queries = listOf(
                        Query.search("brand", query),
                        Query.limit(25)
                    )
                )
            } catch (e: Exception) {
                Log.e("FoodRepository", "Fehler bei der Markensuche: ${e.message}", e)
                null
            }
            
            // Ergebnisse zusammenführen und Duplikate entfernen
            val combinedResults = mutableMapOf<String, Food>()
            
            // Produkt-Ergebnisse hinzufügen
            productResults?.documents?.forEach { document ->
                val food = documentToFood(document)
                combinedResults[food.id] = food
                Log.d("FoodRepository", "Produkt gefunden: ${food.product} von ${food.brand} (ID: ${food.id})")
            }
            
            // Marken-Ergebnisse hinzufügen (überschreibt bei ID-Konflikt nicht, da wir die Map verwenden)
            brandResults?.documents?.forEach { document ->
                val food = documentToFood(document)
                if (!combinedResults.containsKey(food.id)) {
                    combinedResults[food.id] = food
                    Log.d("FoodRepository", "Marke gefunden: ${food.product} von ${food.brand} (ID: ${food.id})")
                }
            }
            
            val foods = combinedResults.values.toList()
            
            Log.d("FoodRepository", "Insgesamt gefunden: ${foods.size} Ergebnisse")
            
            if (foods.isNotEmpty()) {
                Log.d("FoodRepository", "Erstes Ergebnis: ${foods[0].product} von ${foods[0].brand}")
            } else {
                Log.d("FoodRepository", "Keine Ergebnisse gefunden für: $query")
            }
            
            Result.success(foods)
        } catch (e: AppwriteException) {
            Log.e("FoodRepository", "AppwriteException in searchFood: ${e.message}, Code: ${e.code}, Response: ${e.response}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("FoodRepository", "General Exception in searchFood: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sucht speziell nach Marken mit verbesserter Suche
     */
    suspend fun searchFoodByBrand(brand: String): Result<List<Food>> = withContext(Dispatchers.IO) {
        try {
            Log.d("FoodRepository", "Suche nach Marke: $brand")
            
            if (brand.length < 2) {
                return@withContext Result.success(emptyList())
            }
            
            val results = databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_DB,
                queries = listOf(
                    Query.search("brand", brand),
                    Query.limit(25)
                )
            )
            
            Log.d("FoodRepository", "Markensuche gefunden: ${results.documents.size} Ergebnisse")
            
            val foods = results.documents.map { document ->
                val food = documentToFood(document)
                Log.d("FoodRepository", "Marke gefunden: ${food.product} von ${food.brand} (ID: ${food.id})")
                food
            }
            
            Result.success(foods)
        } catch (e: AppwriteException) {
            Log.e("FoodRepository", "AppwriteException in searchFoodByBrand: ${e.message}, Code: ${e.code}, Response: ${e.response}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("FoodRepository", "Exception in searchFoodByBrand: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Hilfsfunktion zum Konvertieren eines Appwrite-Dokuments in ein Food-Objekt
     */
    fun documentToFood(document: io.appwrite.models.Document<Map<String, Any>>): Food {
        val documentData = document.data
        return Food(
            id = document.id,
            ean = documentData["ean"]?.toString() ?: "",
            brand = documentData["brand"]?.toString() ?: "",
            product = documentData["product"]?.toString() ?: "",
            protein = (documentData["protein"] as? Number)?.toDouble() ?: 0.0,
            fat = (documentData["fat"] as? Number)?.toDouble() ?: 0.0,
            crudeFiber = (documentData["crudeFiber"] as? Number)?.toDouble() ?: 0.0,
            rawAsh = (documentData["rawAsh"] as? Number)?.toDouble() ?: 0.0,
            moisture = (documentData["moisture"] as? Number)?.toDouble() ?: 0.0,
            additives = (documentData["additives"] as? String)?.let { jsonString ->
                try {
                    val json = JSONObject(jsonString)
                    val map = mutableMapOf<String, String>()
                    json.keys().forEach { key ->
                        map[key] = json.getString(key)
                    }
                    map.toMap()
                } catch (e: Exception) {
                    Log.w("FoodRepository", "Failed to parse additives JSON for food ${document.id}: $jsonString", e)
                    emptyMap<String, String>()
                }
            } ?: emptyMap<String, String>(),
            imageUrl = documentData["imageUrl"]?.toString()
        )
    }
}