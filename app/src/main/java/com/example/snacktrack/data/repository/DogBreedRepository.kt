package com.example.snacktrack.data.repository

import android.content.Context
import android.util.Log
import com.example.snacktrack.data.model.DogBreed
import com.example.snacktrack.data.service.AppwriteService
import io.appwrite.Query
import io.appwrite.models.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository für Hunderassen-Daten
 */
class DogBreedRepository(private val context: Context) {
    
    private val appwriteService = AppwriteService.getInstance(context)
    private val databases = appwriteService.databases
    
    /**
     * Sucht nach Hunderassen basierend auf einem Suchbegriff
     * Verwendet verschiedene Suchstrategien für optimale Ergebnisse
     */
    suspend fun searchBreeds(query: String, limit: Int = 10): Result<List<DogBreed>> = withContext(Dispatchers.IO) {
        try {
            Log.d("DogBreedRepository", "Suche nach Hunderassen mit Query: '$query'")
            
            if (query.isBlank()) {
                return@withContext Result.success(emptyList())
            }
            
            // Versuche verschiedene Suchstrategien
            var breeds = tryFulltextSearch(query, limit)
            
            // Fallback auf startsWith wenn Fulltext keine Ergebnisse liefert
            if (breeds.isEmpty() && query.length >= 1) {
                breeds = tryStartsWithSearch(query, limit)
            }
            
            // Fallback auf Contains-Suche wenn immer noch keine Ergebnisse
            if (breeds.isEmpty() && query.length >= 2) {
                breeds = tryContainsSearch(query, limit)
            }
            
            Log.d("DogBreedRepository", "Gefunden: ${breeds.size} Hunderassen für Query '$query'")
            Result.success(breeds)
            
        } catch (e: Exception) {
            Log.e("DogBreedRepository", "Fehler beim Suchen nach Hunderassen: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Versucht Fulltext-Suche
     */
    private suspend fun tryFulltextSearch(query: String, limit: Int): List<DogBreed> {
        return try {
            val response = databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_HUNDERASSEN,
                queries = listOf(
                    "search(\"name\", \"$query\")",
                    "limit($limit)",
                    "orderAsc(\"name\")"
                )
            )
            response.documents.map { convertDocumentToDogBreed(it) }
        } catch (e: Exception) {
            Log.d("DogBreedRepository", "Fulltext-Suche fehlgeschlagen: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Versucht StartsWith-Suche
     */
    private suspend fun tryStartsWithSearch(query: String, limit: Int): List<DogBreed> {
        return try {
            val response = databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_HUNDERASSEN,
                queries = listOf(
                    "startsWith(\"name\", \"$query\")",
                    "limit($limit)",
                    "orderAsc(\"name\")"
                )
            )
            response.documents.map { convertDocumentToDogBreed(it) }
        } catch (e: Exception) {
            Log.d("DogBreedRepository", "StartsWith-Suche fehlgeschlagen: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Versucht Contains-Suche (für Teilstrings)
     */
    private suspend fun tryContainsSearch(query: String, limit: Int): List<DogBreed> {
        return try {
            // Contains ist nicht direkt verfügbar, daher verwenden wir eine allgemeine Suche
            // und filtern lokal
            val response = databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_HUNDERASSEN,
                queries = listOf(
                    "limit(50)", // Mehr laden für lokales Filtern
                    "orderAsc(\"name\")"
                )
            )
            
            val allBreeds = response.documents.map { convertDocumentToDogBreed(it) }
            
            // Lokales Filtern nach Contains (case-insensitive)
            allBreeds.filter { breed ->
                breed.name.contains(query, ignoreCase = true)
            }.take(limit)
            
        } catch (e: Exception) {
            Log.d("DogBreedRepository", "Contains-Suche fehlgeschlagen: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Holt alle Hunderassen (für Fallback)
     */
    suspend fun getAllBreeds(limit: Int = 50): Result<List<DogBreed>> = withContext(Dispatchers.IO) {
        try {
            val response = databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_HUNDERASSEN,
                queries = listOf(
                    "limit($limit)",
                    "orderAsc(\"name\")"
                )
            )
            
            val breeds = response.documents.map { doc ->
                convertDocumentToDogBreed(doc)
            }
            
            Result.success(breeds)
            
        } catch (e: Exception) {
            Log.e("DogBreedRepository", "Fehler beim Abrufen aller Hunderassen: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Konvertiert ein Appwrite-Dokument in ein DogBreed-Objekt
     */
    private fun convertDocumentToDogBreed(doc: Document<Map<String, Any>>): DogBreed {
        return DogBreed(
            id = doc.id,
            name = doc.data["name"]?.toString() ?: "",
            groesse = doc.data["groesse"]?.toString() ?: "",
            gewichtMin = (doc.data["gewicht_min"] as? Number)?.toInt(),
            gewichtMax = (doc.data["gewicht_max"] as? Number)?.toInt(),
            aktivitaetslevel = doc.data["aktivitaetslevel"]?.toString()
        )
    }
}
