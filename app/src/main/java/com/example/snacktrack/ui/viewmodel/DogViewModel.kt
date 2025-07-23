package com.example.snacktrack.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.example.snacktrack.data.model.Dog
import com.example.snacktrack.data.repository.DogRepository
import com.example.snacktrack.data.service.AppwriteService

class DogViewModel(context: Context) : ViewModel() {
    
    private val dogRepository = DogRepository(context)
    private val appwriteService = dogRepository.getAppwriteService()
    
    private val _dogs = MutableStateFlow<List<Dog>>(emptyList())
    val dogs: StateFlow<List<Dog>> = _dogs.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Speichert Bilddaten zwischen
    private val _imageData = mutableMapOf<String, MutableStateFlow<ByteArray?>>()
    
    // Import für Log hinzufügen
    private val TAG = "DogViewModel"
    
    init {
        loadDogs()
    }
    
    fun loadDogs() {
        viewModelScope.launch {
            _isLoading.value = true
            dogRepository.getDogs().collect { dogList ->
                _dogs.value = dogList
                _isLoading.value = false
            }
        }
    }
    
    fun saveDog(dog: Dog) {
        viewModelScope.launch {
            _isLoading.value = true
            dogRepository.saveDog(dog)
                .onSuccess {
                    loadDogs()
                }
                .onFailure { e ->
                    _errorMessage.value = "Fehler beim Speichern: ${e.message}"
                    _isLoading.value = false
                }
        }
    }
    
    fun deleteDog(dogId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            dogRepository.deleteDog(dogId)
                .onSuccess {
                    loadDogs()
                }
                .onFailure { e ->
                    _errorMessage.value = "Fehler beim Löschen: ${e.message}"
                    _isLoading.value = false
                }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    /**
     * Gibt die ID des aktuellen Benutzers zurück
     */
    suspend fun getCurrentUserId(): String {
        return try {
            val user = appwriteService.account.get()
            Log.d("DogViewModel", "Benutzer-ID erfolgreich abgerufen: ${user.id}")
            user.id
        } catch (e: Exception) {
            Log.e("DogViewModel", "Fehler beim Abrufen der Benutzer-ID: ${e.message}", e)
            ""
        }
    }
    
    /**
     * Liefert eine StateFlow für Bilddaten (ByteArray), die asynchron geladen werden
     * 
     * HINWEIS: Diese Methode wurde überarbeitet, um das Problem mit nicht-authentifizierten Bildabrufen zu beheben.
     * Anstatt direkt getFileView zu verwenden, wird nun ein authentifizierter Download durchgeführt.
     */
    fun getImageDataFlow(fileId: String): StateFlow<ByteArray?> {
        // Wenn wir bereits einen Flow für diese ID haben, geben wir ihn zurück
        if (_imageData.containsKey(fileId)) {
            return _imageData[fileId]!!.asStateFlow()
        }
        
        // Neuen Flow erstellen
        val flow = MutableStateFlow<ByteArray?>(null)
        _imageData[fileId] = flow
        
        // Bild-Daten im Hintergrund laden
        viewModelScope.launch {
            try {
                // Verwenden einer ordnungsgemäß authentifizierten Methode für den Datenabruf
                // Die getFileDownload-Methode stellt sicher, dass die Anfrage authentifiziert ist
                val imageData = appwriteService.storage.getFileDownload(
                    bucketId = AppwriteService.BUCKET_DOG_IMAGES,
                    fileId = fileId
                )
                
                // Daten im Flow speichern
                flow.value = imageData
                android.util.Log.d(TAG, "Bilddaten für ID $fileId erfolgreich geladen mit authentifizierter Methode")
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Fehler beim Laden der Bilddaten: ${e.message}", e)
            }
        }
        
        return flow.asStateFlow()
    }
    
    /**
     * Liefert eine authentifizierte URL für ein Bild
     * Dies löst das Problem mit nicht-authentifizierten URLs für die Bildanzeige
     */
    suspend fun getAuthenticatedImageUrl(fileId: String): String {
        return try {
            // Verwende getFilePreview anstelle von getFileView für eine authentifizierte URL
            // Das erzeugt eine vorzeichnete URL mit Authentifizierung
            val previewUrl = appwriteService.storage.getFilePreview(
                bucketId = AppwriteService.BUCKET_DOG_IMAGES,
                fileId = fileId
            )
            
            // Den Pfad zur authentifizierten URL zurückgeben
            previewUrl.toString()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Fehler beim Generieren der authentifizierten URL: ${e.message}")
            ""
        }
    }
} 