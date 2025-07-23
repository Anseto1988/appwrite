package com.example.snacktrack.ui.screens.food

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.snacktrack.data.model.Food
import com.example.snacktrack.data.model.FoodIntake
import com.example.snacktrack.data.repository.FoodIntakeRepository
import com.example.snacktrack.ui.components.CommonTopAppBar
import com.example.snacktrack.ui.viewmodel.ManualFoodEntryViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualFoodEntryScreen(
    dogId: String,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val foodIntakeRepository = remember { FoodIntakeRepository(context) }
    
    // Initialize the ViewModel
    val viewModel: ManualFoodEntryViewModel = viewModel(
        factory = ManualFoodEntryViewModel.Factory(context)
    )
    
    // Collect food suggestions from ViewModel
    val foodSuggestions by viewModel.foodSuggestions.collectAsState()
    val vmIsLoading by viewModel.isLoading.collectAsState()
    val vmErrorMessage by viewModel.errorMessage.collectAsState()
    
    // UI state
    var foodName by remember { mutableStateOf("") }
    var amountGram by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuggestions by remember { mutableStateOf(false) }
    var selectedFood by remember { mutableStateOf<Food?>(null) }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Manuelle Futtereingabe",
                showBackButton = true,
                onBackClick = onBackClick,
                onAccountClick = {}
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Food name field with live suggestions
            OutlinedTextField(
                value = foodName,
                onValueChange = { 
                    foodName = it
                    if (it.length >= 2) {
                        viewModel.searchFoodByName(it)
                        showSuggestions = true
                    } else {
                        viewModel.clearSuggestions()
                        showSuggestions = false
                    }
                },
                label = { Text("Futtername") },
                placeholder = { Text("Futtername eingeben") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        showSuggestions = focusState.isFocused && foodName.length >= 2 && foodSuggestions.isNotEmpty()
                    },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                trailingIcon = {
                    if (vmIsLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(4.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            )
            
            // Hinweistext zum Suchen
            if (foodName.length == 1) {
                Text(
                    text = "Mindestens 2 Zeichen eingeben, um nach Futternamen zu suchen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Separates Markenfeld für die Markensuche
            var brandName by remember { mutableStateOf("") }
            
            OutlinedTextField(
                value = brandName,
                onValueChange = { 
                    brandName = it
                    if (it.length >= 2) {
                        // Markensuche durchführen
                        viewModel.searchFoodByBrand(it)
                        showSuggestions = true
                    } else if (it.isEmpty() && foodName.length >= 2) {
                        // Wenn Markenfeld geleert wird, zurück zur Produktsuche
                        viewModel.searchFoodByName(foodName)
                        showSuggestions = true
                    } else if (it.isEmpty()) {
                        viewModel.clearSuggestions()
                        showSuggestions = false
                    }
                },
                label = { Text("Marke (optional)") },
                placeholder = { Text("Markenname eingeben") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused && brandName.length >= 2) {
                            showSuggestions = foodSuggestions.isNotEmpty()
                        }
                    },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )
            
            // Hinweistext zur Markensuche
            if (brandName.length == 1) {
                Text(
                    text = "Mindestens 2 Zeichen eingeben, um nach Marken zu suchen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
            
            // Suggestions dropdown
            if (showSuggestions && foodSuggestions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(foodSuggestions) { food ->
                            FoodSuggestionItem(
                                food = food,
                                onSelect = {
                                    foodName = food.product
                                    
                                    // If amount is provided, calculate calories
                                    val amount = amountGram.toDoubleOrNull()
                                    if (amount != null && amount > 0) {
                                        calories = viewModel.calculateCaloriesFromGrams(food, amount).toString()
                                    }
                                    
                                    // Store the selected food
                                    selectedFood = food
                                    
                                    // Hide suggestions and clear focus
                                    showSuggestions = false
                                    viewModel.clearSuggestions()
                                    focusManager.clearFocus()
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = amountGram,
                onValueChange = { grams ->
                    amountGram = grams
                    
                    // Auto-calculate calories if a food is selected
                    val amount = grams.toDoubleOrNull()
                    selectedFood?.let { food ->
                        if (amount != null && amount > 0) {
                            calories = viewModel.calculateCaloriesFromGrams(food, amount).toString()
                        }
                    }
                },
                label = { Text("Menge (g)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = calories,
                onValueChange = { newValue ->
                    calories = newValue
                },
                label = { Text("Kalorien (kcal)") },
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
                    if (foodName.isBlank()) {
                        errorMessage = "Bitte gib einen Futternamen ein"
                        return@Button
                    }
                    
                    val amount = amountGram.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorMessage = "Bitte gib eine gültige Menge ein"
                        return@Button
                    }
                    
                    val caloriesInt = calories.toIntOrNull()
                    if (caloriesInt == null || caloriesInt <= 0) {
                        errorMessage = "Bitte gib gültige Kalorien ein"
                        return@Button
                    }
                    
                    isLoading = true
                    errorMessage = null
                    
                    val foodIntake = FoodIntake(
                        dogId = dogId,
                        foodName = foodName,
                        amountGram = amount,
                        calories = caloriesInt,
                        timestamp = LocalDateTime.now(),
                        note = note.ifBlank { null }
                    )
                    
                    scope.launch {
                        foodIntakeRepository.addFoodIntake(foodIntake)
                            .onSuccess {
                                isLoading = false
                                onSaveSuccess()
                            }
                            .onFailure { e ->
                                isLoading = false
                                errorMessage = "Fehler beim Speichern: ${e.message}"
                            }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
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

@Composable
private fun FoodSuggestionItem(
    food: Food,
    onSelect: (Food) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(food) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = food.product,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        if (food.brand.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = food.brand,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = String.format("%.1f kcal/100g", food.kcalPer100g),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1
        )
    }
}