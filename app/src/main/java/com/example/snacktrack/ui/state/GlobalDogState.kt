package com.example.snacktrack.ui.state

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.snacktrack.data.model.Dog

/**
 * Global state for managing the currently selected dog
 */
class GlobalDogState {
    var currentDog by mutableStateOf<Dog?>(null)
        private set
    
    var currentDogId by mutableStateOf<String?>(null)
        private set
    
    fun selectDog(dog: Dog?) {
        currentDog = dog
        currentDogId = dog?.id
    }
    
    fun selectDogById(dogId: String?) {
        currentDogId = dogId
        // Note: The actual dog object will be loaded by components that need it
        if (dogId == null) {
            currentDog = null
        }
    }
    
    fun clearSelection() {
        currentDog = null
        currentDogId = null
    }
}

/**
 * CompositionLocal for accessing GlobalDogState
 */
val LocalGlobalDogState = compositionLocalOf { GlobalDogState() }