package com.example.snacktrack.ui.screens.feeding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snacktrack.data.model.*
import com.example.snacktrack.data.repository.FeedingRepository
import com.example.snacktrack.data.repository.FoodRepository
import com.example.snacktrack.ui.components.CommonTopAppBar
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFeedingScreen(
    dogId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val feedingRepository = remember { FeedingRepository(context) }
    val foodRepository = remember { FoodRepository(context) }
    val scope = rememberCoroutineScope()
    
    var foods by remember { mutableStateOf<List<Food>>(emptyList()) }
    var selectedFood by remember { mutableStateOf<Food?>(null) }
    var amount by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(FeedingType.MAIN_MEAL) }
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    
    var showFoodDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Lade Futtermittel
    LaunchedEffect(Unit) {
        val result = foodRepository.getAllFoods()
        result.fold(
            onSuccess = { foodList ->
                foods = foodList
            },
            onFailure = { /* Fehler ignorieren */ }
        )
    }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Fütterung hinzufügen",
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
            // Futtermittel auswählen
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showFoodDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Futtermittel",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            selectedFood?.name ?: "Auswählen...",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selectedFood != null) FontWeight.Bold else FontWeight.Normal
                        )
                        selectedFood?.let { food ->
                            Text(
                                "${food.caloriesPerGram} kcal/g",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
            
            // Menge
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Menge (g) *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) },
                isError = amount.isEmpty() && isSaving,
                supportingText = {
                    if (selectedFood != null && amount.toDoubleOrNull() != null) {
                        val calories = (amount.toDouble() * selectedFood!!.caloriesPerGram).toInt()
                        Text("≈ $calories kcal")
                    }
                }
            )
            
            // Fütterungstyp
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Fütterungstyp",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FeedingType.entries.forEach { type ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(type.displayName)
                        }
                    }
                }
            }
            
            // Datum und Zeit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    onClick = { showDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Datum",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                
                Card(
                    modifier = Modifier.weight(1f),
                    onClick = { showTimePicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Zeit",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
            
            // Notizen
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notizen (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
            
            // Speichern Button
            Button(
                onClick = {
                    scope.launch {
                        isSaving = true
                        error = null
                        
                        // Validierung
                        if (selectedFood == null) {
                            error = "Bitte wählen Sie ein Futtermittel aus"
                            isSaving = false
                            return@launch
                        }
                        
                        val amountValue = amount.toDoubleOrNull()
                        if (amountValue == null || amountValue <= 0) {
                            error = "Bitte geben Sie eine gültige Menge ein"
                            isSaving = false
                            return@launch
                        }
                        
                        // Erstelle neue Fütterung
                        val feeding = Feeding(
                            id = "",
                            dogId = dogId,
                            foodId = selectedFood!!.id,
                            foodName = selectedFood!!.name,
                            amount = amountValue,
                            calories = (amountValue * selectedFood!!.caloriesPerGram).toInt(),
                            type = selectedType,
                            date = selectedDate,
                            time = selectedTime,
                            notes = notes.takeIf { it.isNotBlank() }
                        )
                        
                        val result = feedingRepository.addFeeding(feeding)
                        if (result.isSuccess) {
                            navController.navigateUp()
                        } else {
                            error = "Fehler beim Speichern: ${result.exceptionOrNull()?.message}"
                        }
                        
                        isSaving = false
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
                    Text("Fütterung speichern")
                }
            }
            
            // Fehleranzeige
            error?.let { err ->
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
                            err,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
    
    // Dialog zur Auswahl des Futtermittels
    if (showFoodDialog) {
        AlertDialog(
            onDismissRequest = { showFoodDialog = false },
            title = { Text("Futtermittel auswählen") },
            text = {
                Column {
                    foods.forEach { food ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedFood?.id == food.id,
                                onClick = {
                                    selectedFood = food
                                    showFoodDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(food.name)
                                Text(
                                    "${food.brand ?: ""} - ${food.caloriesPerGram} kcal/g",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFoodDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}