package com.example.snacktrack.ui.screens.weight

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.example.snacktrack.ui.components.CommonTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.snacktrack.data.model.Dog
import com.example.snacktrack.data.model.WeightEntry
import com.example.snacktrack.data.repository.DogRepository
import com.example.snacktrack.data.repository.WeightRepository
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Composable
fun AddWeightScreen(
    dogId: String,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dogRepository = remember { DogRepository(context) }
    val weightRepository = remember { WeightRepository(context) }
    
    var dog by remember { mutableStateOf<Dog?>(null) }
    var weight by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Lade Hundedaten
    LaunchedEffect(dogId) {
        try {
            dogRepository.getDogs().collect { dogs ->
                dog = dogs.find { it.id == dogId }
                if (dog != null) {
                    weight = dog!!.weight.toString()
                }
                isLoading = false
            }
        } catch (e: Exception) {
            errorMessage = "Fehler beim Laden der Daten: ${e.message}"
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Gewicht hinzufügen",
                showBackButton = true,
                onBackClick = onBackClick,
                onAccountClick = {}, // Empty placeholder
                onLogoutClick = {} // Empty placeholder
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (dog == null) {
                Text(
                    text = "Hund nicht gefunden",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Text(
                    text = "Gewicht für ${dog!!.name} eintragen",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Gewicht (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Notiz (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        val weightValue = weight.toDoubleOrNull()
                        if (weightValue == null || weightValue <= 0) {
                            errorMessage = "Bitte gib ein gültiges Gewicht ein"
                            return@Button
                        }
                        
                        isSaving = true
                        errorMessage = null
                        
                        val weightEntry = WeightEntry(
                            dogId = dogId,
                            weight = weightValue,
                            timestamp = LocalDateTime.now(),
                            note = note.ifBlank { null }
                        )
                        
                        scope.launch {
                            weightRepository.addWeightEntry(weightEntry)
                                .onSuccess {
                                    // Aktualisiere auch das aktuelle Gewicht des Hundes
                                    dog?.let { currentDog ->
                                        val updatedDog = currentDog.copy(weight = weightValue)
                                        dogRepository.saveDog(updatedDog)
                                    }
                                    
                                    isSaving = false
                                    onSaveSuccess()
                                }
                                .onFailure { e ->
                                    isSaving = false
                                    errorMessage = "Fehler beim Speichern: ${e.message}"
                                }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Speichern")
                    }
                }
            }
        }
    }
} 