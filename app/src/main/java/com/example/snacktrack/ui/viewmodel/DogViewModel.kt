package com.example.snacktrack.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.snacktrack.utils.SecureLogger
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
    
    fun saveDog(dog: Dog?) {
        if (dog == null) {
            _errorMessage.value = "Ungültige Hundedaten"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                dogRepository.saveDog(dog)
                    .onSuccess {
                        loadDogs()
                    }
                    .onFailure { e ->
                        _errorMessage.value = dogRepository.getErrorMessage(e)
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                SecureLogger.e(TAG, "Unexpected error saving dog", e)
                _errorMessage.value = "Ein unerwarteter Fehler ist aufgetreten"
                _isLoading.value = false
            }
        }
    }
    
    fun deleteDog(dogId: String?) {
        if (dogId.isNullOrBlank()) {
            _errorMessage.value = "Ungültige Hunde-ID"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                dogRepository.deleteDog(dogId)
                    .onSuccess {
                        loadDogs()
                    }
                    .onFailure { e ->
                        _errorMessage.value = dogRepository.getErrorMessage(e)
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                SecureLogger.e(TAG, "Unexpected error deleting dog", e)
                _errorMessage.value = "Ein unerwarteter Fehler ist aufgetreten"
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
            SecureLogger.d(TAG, "Benutzer-ID erfolgreich abgerufen")
            user.id
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Fehler beim Abrufen der Benutzer-ID", e)
            ""
        }
    }
    
    /**
     * Liefert eine StateFlow für Bilddaten (ByteArray), die asynchron geladen werden
     * 
     * HINWEIS: Diese Methode wurde überarbeitet, um das Problem mit nicht-authentifizierten Bildabrufen zu beheben.
     * Anstatt direkt getFileView zu verwenden, wird nun ein authentifizierter Download durchgeführt.
     */
    fun getImageDataFlow(fileId: String?): StateFlow<ByteArray?> {
        if (fileId.isNullOrBlank()) {
            return MutableStateFlow<ByteArray?>(null).asStateFlow()
        }
        
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
                SecureLogger.d(TAG, "Bilddaten erfolgreich geladen")
            } catch (e: Exception) {
                SecureLogger.e(TAG, "Fehler beim Laden der Bilddaten", e)
                flow.value = null
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
            SecureLogger.e(TAG, "Fehler beim Generieren der authentifizierten URL", e)
            ""
        }
    }
} 