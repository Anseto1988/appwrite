package com.example.snacktrack.ui.screens.dogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snacktrack.data.model.*
import com.example.snacktrack.data.repository.DogRepository
import com.example.snacktrack.ui.components.CommonTopAppBar
import com.example.snacktrack.ui.viewmodel.AddDogViewModel
import com.example.snacktrack.utils.DateUtils
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDogScreen(
    navController: NavController,
    viewModel: AddDogViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dogRepository = remember { DogRepository(context) }
    
    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var birthDateString by remember { mutableStateOf("") }
    var birthDateError by remember { mutableStateOf<String?>(null) }
    var weight by remember { mutableStateOf("") }
    var targetWeight by remember { mutableStateOf("") }
    var selectedSex by remember { mutableStateOf(Sex.MALE) }
    var selectedActivityLevel by remember { mutableStateOf(ActivityLevel.NORMAL) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Hund hinzufügen",
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Pets, contentDescription = null) },
                isError = name.isEmpty() && isSaving,
                supportingText = if (name.isEmpty() && isSaving) {
                    { Text("Name ist erforderlich") }
                } else null
            )
            
            // Rasse
            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text("Rasse") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
            )
            
            // Geburtsdatum
            OutlinedTextField(
                value = birthDateString,
                onValueChange = { 
                    birthDateString = it
                    birthDateError = null
                },
                label = { Text("Geburtsdatum (TT.MM.JJJJ) *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = birthDateError != null,
                supportingText = birthDateError?.let { { Text(it) } }
            )
            
            // Gewicht
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Gewicht (kg) *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = weight.isEmpty() && isSaving,
                supportingText = if (weight.isEmpty() && isSaving) {
                    { Text("Gewicht ist erforderlich") }
                } else null
            )
            
            // Zielgewicht
            OutlinedTextField(
                value = targetWeight,
                onValueChange = { targetWeight = it },
                label = { Text("Zielgewicht (kg)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.TrendingUp, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            
            // Geschlecht
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Geschlecht",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Sex.entries.forEach { sex ->
                            FilterChip(
                                selected = selectedSex == sex,
                                onClick = { selectedSex = sex },
                                label = { Text(sex.displayName) },
                                leadingIcon = if (selectedSex == sex) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }
            
            // Aktivitätslevel
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Aktivitätslevel",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ActivityLevel.entries.forEach { level ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedActivityLevel == level,
                                onClick = { selectedActivityLevel = level }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(level.displayName)
                                Text(
                                    level.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Bild hinzufügen
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { imagePickerLauncher.launch("image/*") }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            if (imageUri != null) "Bild ausgewählt" else "Bild hinzufügen",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (imageUri != null) {
                            Text(
                                "Tippen zum Ändern",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Fehleranzeige
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            error,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            // Speichern Button
            Button(
                onClick = {
                    scope.launch {
                        isSaving = true
                        errorMessage = null
                        birthDateError = null
                        
                        // Validierung
                        if (name.isBlank()) {
                            errorMessage = "Bitte geben Sie einen Namen ein"
                            isSaving = false
                            return@launch
                        }
                        
                        if (birthDateString.isBlank()) {
                            birthDateError = "Bitte geben Sie das Geburtsdatum ein"
                            isSaving = false
                            return@launch
                        }
                        
                        // Parse Geburtsdatum
                        val birthDate = DateUtils.parseGermanDate(birthDateString)
                        if (birthDate == null) {
                            birthDateError = "Ungültiges Datumsformat. Bitte TT.MM.JJJJ verwenden"
                            isSaving = false
                            return@launch
                        }
                        
                        if (birthDate.isAfter(LocalDate.now())) {
                            birthDateError = "Das Geburtsdatum kann nicht in der Zukunft liegen"
                            isSaving = false
                            return@launch
                        }
                        
                        val weightValue = weight.toDoubleOrNull()
                        if (weightValue == null || weightValue <= 0) {
                            errorMessage = "Bitte geben Sie ein gültiges Gewicht ein"
                            isSaving = false
                            return@launch
                        }
                        
                        val targetWeightValue = if (targetWeight.isNotBlank()) {
                            targetWeight.toDoubleOrNull()
                        } else null
                        
                        try {
                            // Speichere den Hund
                            val newDog = Dog(
                                id = "", // Wird von Appwrite generiert
                                name = name.trim(),
                                breed = breed.trim().takeIf { it.isNotBlank() } ?: "",
                                birthDate = birthDate,
                                weight = weightValue,
                                targetWeight = targetWeightValue,
                                sex = selectedSex,
                                activityLevel = selectedActivityLevel,
                                imageId = null // Wird später hinzugefügt wenn Bild hochgeladen wird
                            )
                            
                            val result = dogRepository.saveDog(newDog)
                            if (result.isSuccess) {
                                // TODO: Bild hochladen wenn vorhanden
                                navController.navigateUp()
                            } else {
                                errorMessage = "Fehler beim Speichern: ${result.exceptionOrNull()?.message}"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Fehler: ${e.message}"
                        } finally {
                            isSaving = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hund speichern")
                }
            }
        }
    }
}