package com.example.snacktrack.ui.screens.dog

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.snacktrack.data.model.ActivityLevel
import com.example.snacktrack.data.model.Dog
import com.example.snacktrack.data.model.DogBreed
import com.example.snacktrack.data.model.Sex
import com.example.snacktrack.data.repository.DogRepository
import com.example.snacktrack.data.service.AppwriteService // Import AppwriteService
import com.example.snacktrack.ui.components.CommonTopAppBar
import com.example.snacktrack.ui.components.DogBreedAutocomplete
import com.example.snacktrack.ui.components.DatePickerField
import com.example.snacktrack.ui.viewmodel.DogBreedSearchViewModel
import com.example.snacktrack.utils.DateUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDogScreen(
    dogId: String?,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    // Debug-Log hinzufügen
    android.util.Log.d("AddEditDogScreen", "🎯 AddEditDogScreen wurde gestartet mit dogId: $dogId")
    android.util.Log.d(
        "AddEditDogScreen",
        "🎯 Modus: ${if (dogId == null) "HINZUFÜGEN" else "BEARBEITEN"}"
    )
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dogRepository = remember { DogRepository(context) }
    val breedSearchViewModel = remember { DogBreedSearchViewModel(context) }

    var dog by remember { mutableStateOf<Dog?>(null) }
    var isLoading by remember { mutableStateOf(dogId != null) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Form-Felder
    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var birthDateString by remember { mutableStateOf("") }
    var birthDateError by remember { mutableStateOf<String?>(null) }
    var weight by remember { mutableStateOf("") }
    var targetWeight by remember { mutableStateOf("") }
    var selectedSexIndex by remember { mutableStateOf(0) }
    var selectedActivityLevelIndex by remember { mutableStateOf(ActivityLevel.NORMAL.ordinal) } // Default zu Normal
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var currentImageId by remember { mutableStateOf<String?>(null) } // Renamed from imageId to avoid confusion

    // Breed search states
    val breedSuggestions by breedSearchViewModel.suggestions.collectAsState()
    val isBreedLoading by breedSearchViewModel.isLoading.collectAsState()
    val showBreedSuggestions by breedSearchViewModel.showSuggestions.collectAsState()

    // Dropdowns
    var sexExpanded by remember { mutableStateOf(false) }
    var activityLevelExpanded by remember { mutableStateOf(false) }

    val sexOptions = Sex.values().map { it.displayName }
    val activityLevelOptions = ActivityLevel.values().map { it.displayName }

    // Laden eines existierenden Hundes
    LaunchedEffect(dogId) {
        android.util.Log.d("AddEditDogScreen", "🔄 LaunchedEffect gestartet für dogId: $dogId")
        if (dogId != null) {
            try {
                android.util.Log.d("AddEditDogScreen", "📖 Lade existierenden Hund mit ID: $dogId")
                dogRepository.getDogs().collect { dogs ->
                    android.util.Log.d(
                        "AddEditDogScreen",
                        "📋 Erhalten ${dogs.size} Hunde von Repository"
                    )
                    val existingDog = dogs.find { it.id == dogId }
                    if (existingDog != null) {
                        android.util.Log.d(
                            "AddEditDogScreen",
                            "✅ Hund gefunden: ${existingDog.name} (ID: ${existingDog.id})"
                        )
                        dog = existingDog
                        name = existingDog.name
                        breed = existingDog.breed ?: ""
                        // Konvertiere ISO-Datum zu deutschem Format
                        birthDateString = existingDog.birthDate?.let { 
                            DateUtils.formatToGerman(it) 
                        } ?: ""
                        weight = existingDog.weight.toString()
                        targetWeight = existingDog.targetWeight?.toString() ?: ""
                        selectedSexIndex = existingDog.sex.ordinal
                        selectedActivityLevelIndex = existingDog.activityLevel.ordinal
                        currentImageId =
                            existingDog.imageId // Bild-ID des existierenden Hundes setzen
                        android.util.Log.d(
                            "AddEditDogScreen",
                            "✅ Daten geladen: Name=$name, Rasse=$breed, Gewicht=$weight"
                        )
                    } else {
                        android.util.Log.e(
                            "AddEditDogScreen",
                            "❌ Hund mit ID $dogId nicht gefunden!"
                        )
                    }
                    isLoading = false
                }
            } catch (e: Exception) {
                android.util.Log.e(
                    "AddEditDogScreen",
                    "❌ Fehler beim Laden des Hundes: ${e.message}",
                    e
                )
                errorMessage = "Fehler beim Laden des Hundes: ${e.message}"
                isLoading = false
            }
        } else {
            android.util.Log.d("AddEditDogScreen", "➕ Neuer Hund - kein Laden erforderlich")
            isLoading = false // Für neuen Hund nicht laden
        }
    }

    // Bild-Auswahl-Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imageUri = it
            currentImageId =
                null // Wenn ein neues Bild ausgewählt wird, alte ID entfernen, um neues Bild anzuzeigen
        }
    }

    Scaffold(
        topBar = {
            Log.d("AddEditDogScreen", "🎯 TopAppBar wird angezeigt für: ${if (dogId == null) "NEUEN Hund" else "EXISTIERENDEN Hund (ID: $dogId)"}")
            CommonTopAppBar(
                title = if (dogId == null) "Hund hinzufügen" else "Hund bearbeiten",
                showBackButton = true,
                onBackClick = onBackClick,
                onAdminClick = { /* Navigation zum Admin-Panel */ },
                onLogoutClick = { /* Zum Login-Screen navigieren */ }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                // Profilbild
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        val displayImageUri = imageUri // Vorrang für neu ausgewähltes Bild
                        val displayImageId = if (displayImageUri == null) currentImageId else null

                        if (displayImageUri != null) {
                            AsyncImage(
                                model = displayImageUri,
                                contentDescription = "Hundeprofilbild Vorschau",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (displayImageId != null) {
                            // Verwende AppwriteService.PROJECT_ID
                            val imageUrl =
                                "${AppwriteService.ENDPOINT}/storage/buckets/${AppwriteService.BUCKET_DOG_IMAGES}/files/${displayImageId}/view?project=${AppwriteService.PROJECT_ID}"
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Hundeprofilbild",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Bild hinzufügen",
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name*") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    isError = name.isBlank() && isSaving // Zeige Fehler, wenn Feld leer ist und gespeichert wird
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Rasse mit AutoComplete
                DogBreedAutocomplete(
                    value = breed,
                    onValueChange = { newValue ->
                        breed = newValue
                        breedSearchViewModel.searchBreeds(newValue)
                    },
                    suggestions = breedSuggestions,
                    isLoading = isBreedLoading,
                    showSuggestions = showBreedSuggestions,
                    onSuggestionSelected = { selectedBreed ->
                        breed = selectedBreed.name
                        breedSearchViewModel.hideSuggestions()
                    },
                    onFocusChanged = { hasFocus ->
                        if (hasFocus && breed.length >= 2) {
                            breedSearchViewModel.showSuggestions()
                        } else if (!hasFocus) {
                            // Kleine Verzögerung, damit der Klick auf Vorschlag funktioniert
                            scope.launch {
                                delay(150)
                                breedSearchViewModel.hideSuggestions()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "z.B. Labrador, Golden Retriever"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Geburtsdatum mit DatePicker
                DatePickerField(
                    value = birthDateString,
                    onValueChange = { newValue ->
                        birthDateString = newValue
                        // Bei DatePicker ist die Validierung nicht nötig, da nur gültige Daten ausgewählt werden können
                        birthDateError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = "Geburtsdatum",
                    placeholder = "Datum auswählen",
                    isError = birthDateError != null,
                    errorMessage = birthDateError
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Geschlecht
                ExposedDropdownMenuBox(
                    expanded = sexExpanded,
                    onExpandedChange = { sexExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = sexOptions[selectedSexIndex],
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexExpanded) },
                        label = { Text("Geschlecht*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = sexExpanded,
                        onDismissRequest = { sexExpanded = false }
                    ) {
                        sexOptions.forEachIndexed { index, option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedSexIndex = index
                                    sexExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Gewicht
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Aktuelles Gewicht (kg)*") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        isError = (weight.toDoubleOrNull() == null || weight.toDouble() <= 0) && isSaving
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    OutlinedTextField(
                        value = targetWeight,
                        onValueChange = { targetWeight = it },
                        label = { Text("Zielgewicht (kg)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Aktivitätslevel
                ExposedDropdownMenuBox(
                    expanded = activityLevelExpanded,
                    onExpandedChange = { activityLevelExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = activityLevelOptions[selectedActivityLevelIndex],
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = activityLevelExpanded) },
                        label = { Text("Aktivitätslevel*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = activityLevelExpanded,
                        onDismissRequest = { activityLevelExpanded = false }
                    ) {
                        activityLevelOptions.forEachIndexed { index, option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedActivityLevelIndex = index
                                    activityLevelExpanded = false
                                }
                            )
                        }
                    }
                }

                // Fehleranzeige
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Speichern-Button
                Button(
                    onClick = {
                        // Validierung
                        if (name.isBlank()) {
                            errorMessage = "Bitte gib einen Namen ein."
                            return@Button
                        }

                        val weightValue = weight.toDoubleOrNull()
                        if (weightValue == null || weightValue <= 0) {
                            errorMessage = "Bitte gib ein gültiges aktuelles Gewicht ein."
                            return@Button
                        }

                        val targetWeightValue = targetWeight.ifBlank { null }?.toDoubleOrNull()
                        if (targetWeight.isNotBlank() && (targetWeightValue == null || targetWeightValue <= 0)) {
                            errorMessage =
                                "Bitte gib ein gültiges Zielgewicht ein oder lasse das Feld leer."
                            return@Button
                        }

                        // Validierung des Geburtsdatums
                        val birthDate = if (birthDateString.isNotBlank()) {
                            val validationError = DateUtils.validateGermanDate(birthDateString)
                            if (validationError != null) {
                                errorMessage = validationError
                                return@Button
                            }
                            DateUtils.parseGermanDate(birthDateString)
                        } else {
                            null
                        }

                        isSaving = true // Um Fehlerhervorhebung bei Validierung zu triggern
                        errorMessage =
                            null // Reset error message before new validation/save attempt

                        scope.launch {
                            try {
                                var finalImageId =
                                    currentImageId // Behalte alte ID, falls kein neues Bild

                                if (imageUri != null) { // Neues Bild wurde ausgewählt
                                    val imageFile = File(
                                        context.cacheDir,
                                        "dog_image_${System.currentTimeMillis()}.jpg"
                                    )
                                    context.contentResolver.openInputStream(imageUri!!)
                                        ?.use { input ->
                                            imageFile.outputStream().use { output ->
                                                input.copyTo(output)
                                            }
                                        }

                                    val uploadResult = dogRepository.uploadDogImage(imageFile)
                                    if (uploadResult.isSuccess) {
                                        finalImageId = uploadResult.getOrNull()
                                    } else {
                                        errorMessage =
                                            "Fehler beim Hochladen des Bildes: ${uploadResult.exceptionOrNull()?.message}"
                                        isSaving = false
                                        return@launch
                                    }
                                }

                                // Hund speichern
                                val sexToSave = Sex.values()[selectedSexIndex]
                                val activityLevelToSave =
                                    ActivityLevel.values()[selectedActivityLevelIndex]

                                val dogToSave = Dog(
                                    id = dogId
                                        ?: "", // ID bleibt leer für neuen Hund, Appwrite generiert eine
                                    ownerId = "", // ownerId wird im Repository gesetzt
                                    name = name,
                                    breed = breed,
                                    birthDate = birthDate,
                                    sex = sexToSave,
                                    weight = weightValue,
                                    targetWeight = targetWeightValue,
                                    activityLevel = activityLevelToSave,
                                    imageId = finalImageId
                                )

                                val saveResult = dogRepository.saveDog(dogToSave)
                                if (saveResult.isSuccess) {
                                    onSaveSuccess()
                                } else {
                                    errorMessage =
                                        "Fehler beim Speichern: ${saveResult.exceptionOrNull()?.message}"
                                }
                            } catch (e: Exception) {
                                errorMessage =
                                    "Ein unerwarteter Fehler ist aufgetreten: ${e.message}"
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSaving && errorMessage == null) { // Zeige Ladeindikator nur wenn kein Fehler da ist
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
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