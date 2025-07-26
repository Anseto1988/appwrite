package com.example.snacktrack.data.repository

import android.content.Context
import android.util.Log
import com.example.snacktrack.utils.SecureLogger
// Composables may throw cancellation exceptions during composition changes
// We'll handle them generically rather than using the private class directly
import com.example.snacktrack.data.model.Dog
import com.example.snacktrack.data.model.Sex
import com.example.snacktrack.data.model.ActivityLevel
import com.example.snacktrack.data.service.AppwriteService
import com.example.snacktrack.utils.DateUtils
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.Document
import io.appwrite.models.FileList
import io.appwrite.models.InputFile
import io.appwrite.services.Databases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DogRepository(private val context: Context) : BaseRepository() {
    
    private val appwriteService = AppwriteService.getInstance(context)
    private val databases = appwriteService.databases
    
    /**
     * Gibt den AppwriteService zurück, damit er im ViewModel verwendet werden kann
     */
    fun getAppwriteService(): AppwriteService {
        return appwriteService
    }
    
    /**
     * Holt alle Hunde des aktuell eingeloggten Nutzers und die mit ihm geteilten Hunde
     * Die Methode ist robust gegenüber Compose-Lifecycle-Änderungen und fängt
     * LeftCompositionCancellationException ab
     */
    fun getDogs(): Flow<List<Dog>> = flow {
        try {
            val startTime = System.currentTimeMillis()
            SecureLogger.d("DogRepository", "=== STARTE ABRUF ALLER HUNDE ===")
            
            // Prüfe, ob eine gültige Session vorhanden ist
            if (!appwriteService.ensureValidSession()) {
                Log.e("DogRepository", "Keine gültige Session. Benutzer muss sich erneut anmelden.")
                
                // DEBUG: Detaillierte Session-Analyse
                try {
                    Log.d("DogRepository", "=== SESSION DEBUG START ===")
                    Log.d("DogRepository", "Endpoint: ${AppwriteService.ENDPOINT}")
                    Log.d("DogRepository", "Project ID: ${AppwriteService.PROJECT_ID}")
                    
                    // Check if we can list sessions
                    try {
                        val sessions = appwriteService.account.listSessions()
                        Log.d("DogRepository", "Sessions found: ${sessions.sessions.size}")
                        sessions.sessions.forEachIndexed { index, session ->
                            Log.d("DogRepository", "Session $index:")
                            Log.d("DogRepository", "  - ID: ${session.id}")
                            Log.d("DogRepository", "  - Provider: ${session.provider}")
                            Log.d("DogRepository", "  - UserID: ${session.userId}")
                            Log.d("DogRepository", "  - Current: ${session.current}")
                            Log.d("DogRepository", "  - Expire: ${session.expire}")
                        }
                    } catch (e: Exception) {
                        Log.e("DogRepository", "Cannot list sessions: ${e.message}")
                    }
                    
                    // Try to get account directly
                    try {
                        val account = appwriteService.account.get()
                        Log.d("DogRepository", "UNEXPECTED: Account retrieved despite session check failure")
                        Log.d("DogRepository", "  - ID: ${account.id}")
                        Log.d("DogRepository", "  - Email: ${account.email}")
                        Log.d("DogRepository", "  - Status: ${account.status}")
                    } catch (e: Exception) {
                        Log.d("DogRepository", "Expected: Cannot get account - ${e.message}")
                    }
                    
                    Log.d("DogRepository", "=== SESSION DEBUG END ===")
                } catch (e: Exception) {
                    Log.e("DogRepository", "DEBUG ERROR: ${e.message}")
                }
                
                emit(emptyList<Dog>())
                return@flow
            }
            
            val user = appwriteService.account.get()
            Log.d("DogRepository", "Aktueller Benutzer: ${user.id} (${user.email})")
            
            // Hunde des Benutzers abrufen
            val ownDogsResponse = databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_DOGS,
                queries = listOf(Query.equal("ownerId", user.id))
            )
            
            // Eigene Hunde
            val ownDogs = convertDocumentsToDogs(ownDogsResponse.documents)
            Log.d("DogRepository", "Eigene Hunde gefunden: ${ownDogs.size} (IDs: ${ownDogs.map { it.id }})")
            
            // Geteilte Hunde aus der dog_sharing Collection abrufen
            Log.d("DogRepository", "Beginne Suche nach geteilten Hunden für Benutzer ${user.id}")
            
            // DEBUG: Alle vorhandenen Sharing-Einträge abrufen (zur Überprüfung)
            try {
                val allSharings = databases.listDocuments(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.COLLECTION_DOG_SHARING,
                    queries = listOf() // Keine Filter, alle Dokumente abrufen
                )
                Log.d("DogRepository", "DEBUG: Alle vorhandenen Sharing-Einträge in der Datenbank: ${allSharings.documents.size}")
                for (share in allSharings.documents) {
                    val dogId = share.data["dogId"] as? String ?: "?"
                    val teamId = share.data["teamId"] as? String ?: "?"
                    val sharedBy = share.data["sharedByUserId"] as? String ?: "?"
                    Log.d("DogRepository", "DEBUG: Sharing-Eintrag: dogId=$dogId, teamId=$teamId, sharedBy=$sharedBy")
                }
            } catch (e: Exception) {
                Log.e("DogRepository", "Fehler beim DEBUG-Abruf aller Sharing-Einträge: ${e.message}", e)
            }
            
            // 1. Zuerst finden wir alle Teams, in denen der Benutzer Mitglied ist
            val teamMemberships = databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_TEAM_MEMBERS,
                queries = listOf(Query.equal("userId", user.id))
            )
            
            Log.d("DogRepository", "Gefundene Team-Mitgliedschaften: ${teamMemberships.documents.size}")
            if (teamMemberships.documents.isEmpty()) {
                Log.w("DogRepository", "ACHTUNG: Keine Teammitgliedschaften gefunden für User ${user.id}")
                
                // DEBUG: Alle vorhandenen Team-Mitgliedschaften abrufen (zur Überprüfung)
                try {
                    val allMemberships = databases.listDocuments(
                        databaseId = AppwriteService.DATABASE_ID,
                        collectionId = AppwriteService.COLLECTION_TEAM_MEMBERS,
                        queries = listOf() // Keine Filter, alle Dokumente abrufen
                    )
                    Log.d("DogRepository", "DEBUG: Alle Team-Mitgliedschaften in der Datenbank: ${allMemberships.documents.size}")
                    for (membership in allMemberships.documents) {
                        val userId = membership.data["userId"] as? String ?: "?"
                        val teamId = membership.data["teamId"] as? String ?: "?"
                        Log.d("DogRepository", "DEBUG: Mitgliedschaft: userId=$userId, teamId=$teamId")
                    }
                } catch (e: Exception) {
                    Log.e("DogRepository", "Fehler beim DEBUG-Abruf aller Mitgliedschaften: ${e.message}", e)
                }
            }
            
            // 2. Für jedes Team die geteilten Hunde abrufen
            val sharedDogsList = mutableListOf<Dog>()
            for (membership in teamMemberships.documents) {
                val teamId = membership.data["teamId"] as? String ?: continue
                Log.d("DogRepository", "Suche nach Hundefreigaben für Team $teamId")
                
                // 3. Hundefreigaben für dieses Team finden
                val dogSharing = databases.listDocuments(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.COLLECTION_DOG_SHARING,
                    queries = listOf(Query.equal("teamId", teamId))
                )
                
                Log.d("DogRepository", "Gefundene Hundefreigaben für Team $teamId: ${dogSharing.documents.size}")
                if (dogSharing.documents.isEmpty()) {
                    Log.w("DogRepository", "ACHTUNG: Keine Hundefreigaben für Team $teamId gefunden")
                }
                
                // 4. Für jede Freigabe den Hund abrufen
                for (sharing in dogSharing.documents) {
                    val dogId = sharing.data["dogId"] as? String ?: continue
                    Log.d("DogRepository", "Versuche Hund mit ID $dogId abzurufen")
                    
                    try {
                        val dogDoc = databases.getDocument(
                            databaseId = AppwriteService.DATABASE_ID,
                            collectionId = AppwriteService.COLLECTION_DOGS,
                            documentId = dogId
                        )
                        val dog = convertDocumentsToDogs(listOf(dogDoc)).first()
                        Log.d("DogRepository", "Geteilter Hund gefunden: ${dog.name} (ID: ${dog.id})")
                        sharedDogsList.add(dog)
                    } catch (e: Exception) {
                        Log.e("DogRepository", "Fehler beim Abrufen des Hundes $dogId: ${e.message}", e)
                        // Hund konnte nicht abgerufen werden, überspringen
                    }
                }
            }
            
            // Eigene Hunde und geteilte Hunde kombinieren, Duplikate entfernen
            val allDogs = (ownDogs + sharedDogsList).distinctBy { it.id }
            
            val duration = System.currentTimeMillis() - startTime
            Log.d("DogRepository", "=== ABRUF ALLER HUNDE ABGESCHLOSSEN: ${allDogs.size} Hunde (${ownDogs.size} eigene, ${sharedDogsList.size} geteilt) in $duration ms ===")
            emit(allDogs)
        } catch (e: Exception) {
            // Prüfen, ob es sich um eine Composition-bezogene Cancellation-Exception handelt
            if (e.javaClass.name.contains("CompositionCancellationException")) {
                Log.d("DogRepository", "UI-Komponente wurde geschlossen während des Datenabrufs - das ist normal.")
            } else {
                Log.e("DogRepository", "Kritischer Fehler beim Abrufen aller Hunde: ${e.message}", e)
            }
            emit(emptyList<Dog>())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Konvertiert ein einzelnes Appwrite-Dokument in ein Dog-Objekt
     */
    private fun convertDocumentToDog(doc: Document<Map<String, Any>>): Dog {
        return Dog(
            id = doc.id,
            ownerId = doc.data["ownerId"].toString(),
            name = doc.data["name"].toString(),
            birthDate = (doc.data["birthDate"] as? String)?.let {
                DateUtils.parseISODate(it)
            },
            breed = doc.data["breed"]?.toString() ?: "",
            sex = doc.data["sex"]?.toString()?.let { enumValueOf<com.example.snacktrack.data.model.Sex>(it) }
                ?: com.example.snacktrack.data.model.Sex.UNKNOWN,
            weight = (doc.data["weight"] as? Number)?.toDouble() ?: 0.0,
            targetWeight = (doc.data["targetWeight"] as? Number)?.toDouble(),
            activityLevel = doc.data["activityLevel"]?.toString()?.let {
                enumValueOf<com.example.snacktrack.data.model.ActivityLevel>(it)
            } ?: com.example.snacktrack.data.model.ActivityLevel.NORMAL,
            imageId = doc.data["imageId"]?.toString(),
            teamId = doc.data["teamId"]?.toString()
        )
    }
    
    /**
     * Konvertiert Appwrite-Dokumente in Dog-Objekte
     */
    private fun convertDocumentsToDogs(documents: List<Document<Map<String, Any>>>): List<Dog> {
        return documents.map { doc -> convertDocumentToDog(doc) }
    }
    
    /**
     * Holt alle Hunde des aktuell eingeloggten Nutzers ohne Team-Hunde
     */
    fun getOwnDogs(): Flow<List<Dog>> = flow {
        try {
            val user = appwriteService.account.get()
            val response = databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_DOGS,
                queries = listOf(Query.equal("ownerId", user.id))
            )
            
            val dogs = response.documents.map { doc ->
                Dog(
                    id = doc.id,
                    ownerId = doc.data["ownerId"].toString(),
                    name = doc.data["name"].toString(),
                    birthDate = (doc.data["birthDate"] as? String)?.let {
                        DateUtils.parseISODate(it)
                    },
                    breed = doc.data["breed"]?.toString() ?: "",
                    sex = doc.data["sex"]?.toString()?.let { enumValueOf<com.example.snacktrack.data.model.Sex>(it) }
                        ?: com.example.snacktrack.data.model.Sex.UNKNOWN,
                    weight = (doc.data["weight"] as? Number)?.toDouble() ?: 0.0,
                    targetWeight = (doc.data["targetWeight"] as? Number)?.toDouble(),
                    activityLevel = doc.data["activityLevel"]?.toString()?.let {
                        enumValueOf<com.example.snacktrack.data.model.ActivityLevel>(it)
                    } ?: com.example.snacktrack.data.model.ActivityLevel.NORMAL,
                    imageId = doc.data["imageId"]?.toString()
                )
            }
            emit(dogs)
        } catch (e: Exception) {
            emit(emptyList<Dog>())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Erstellt oder aktualisiert einen Hund
     */
    suspend fun saveDog(dog: Dog): Result<Dog> = withContext(Dispatchers.IO) {
        safeApiCall {
            val user = appwriteService.account.get()
            
            // Validate input data
            require(dog.name.isNotBlank()) { "Hundename darf nicht leer sein" }
            require(dog.weight > 0) { "Gewicht muss größer als 0 sein" }
            
            val data = mapOf(
                "ownerId" to user.id,
                "name" to dog.name.trim(),
                "birthDate" to dog.birthDate?.let { DateUtils.formatToISO(it) },
                "breed" to dog.breed.trim(),
                "sex" to dog.sex.name,
                "weight" to dog.weight,
                "targetWeight" to dog.targetWeight,
                "activityLevel" to dog.activityLevel.name,
                "imageId" to dog.imageId,
                "teamId" to dog.teamId,
                "dailyCalorieNeed" to dog.calculateDailyCalorieNeed()
            )
            
            val response = if (dog.id.isNotEmpty()) {
                // Update
                SecureLogger.d("DogRepository", "Updating dog with ID: ${dog.id}")
                databases.updateDocument(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.COLLECTION_DOGS,
                    documentId = dog.id,
                    data = data
                )
            } else {
                // Create
                SecureLogger.d("DogRepository", "Creating new dog")
                databases.createDocument(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.COLLECTION_DOGS,
                    documentId = ID.unique(),
                    data = data
                )
            }
            
            Dog(
                id = response.id,
                ownerId = response.data["ownerId"].toString(),
                name = response.data["name"].toString(),
                birthDate = (response.data["birthDate"] as? String)?.let {
                    DateUtils.parseISODate(it)
                },
                breed = response.data["breed"]?.toString() ?: "",
                sex = response.data["sex"]?.toString()?.let { enumValueOf<com.example.snacktrack.data.model.Sex>(it) }
                    ?: com.example.snacktrack.data.model.Sex.UNKNOWN,
                weight = (response.data["weight"] as? Number)?.toDouble() ?: 0.0,
                targetWeight = (response.data["targetWeight"] as? Number)?.toDouble(),
                activityLevel = response.data["activityLevel"]?.toString()?.let {
                    enumValueOf<com.example.snacktrack.data.model.ActivityLevel>(it)
                } ?: com.example.snacktrack.data.model.ActivityLevel.NORMAL,
                imageId = response.data["imageId"]?.toString(),
                teamId = response.data["teamId"]?.toString()
            )
        }
    }
    
    /**
     * Aktualisiert einen bestehenden Hund
     */
    suspend fun updateDog(dog: Dog): Result<Dog> = saveDog(dog)
    
    /**
     * Lädt ein Bild für einen Hund hoch
     */
    suspend fun uploadDogImage(file: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = appwriteService.storage.createFile(
                bucketId = AppwriteService.BUCKET_DOG_IMAGES,
                fileId = ID.unique(),
                file = InputFile.fromFile(file)
            )
            Result.success(response.id)
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
    
    /**
     * Holt einen spezifischen Hund anhand seiner ID
     */
    suspend fun getDogById(dogId: String): Result<Dog> = withContext(Dispatchers.IO) {
        try {
            val document = databases.getDocument(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_DOGS,
                documentId = dogId
            )
            val dog = convertDocumentToDog(document)
            Result.success(dog)
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
    
    /**
     * Löscht einen Hund
     */
    suspend fun deleteDog(dogId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            databases.deleteDocument(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_DOGS,
                documentId = dogId
            )
            Result.success(Unit)
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
} 