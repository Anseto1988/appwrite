package com.example.snacktrack.ui.screens.food

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.snacktrack.data.model.Food
import com.example.snacktrack.data.model.FoodIntake
import com.example.snacktrack.data.repository.FoodIntakeRepository
import com.example.snacktrack.data.repository.FoodRepository
import com.example.snacktrack.ui.components.CommonTopAppBar
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailScreen(
    foodId: String,
    dogId: String,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val foodRepository = remember { FoodRepository(context) }
    val foodIntakeRepository = remember { FoodIntakeRepository(context) }
    
    var food by remember { mutableStateOf<Food?>(null) }
    var amountGram by remember { mutableStateOf("100") }
    var note by remember { mutableStateOf("") }
    var calculatedCalories by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Lade die Futterdetails direkt mit der ID
    LaunchedEffect(foodId) {
        isLoading = true
        
        try {
            Log.d("FoodDetailScreen", "Lade Produkt mit ID: $foodId")
            foodRepository.getFoodById(foodId)
                .onSuccess { foundFood ->
                    Log.d("FoodDetailScreen", "Produkt geladen: ${foundFood.product}")
                    food = foundFood
                    
                    // Standardwerte setzen
                    calculatedCalories = foundFood.kcalPer100g.roundToInt()
                    
                    isLoading = false
                }
                .onFailure { e ->
                    Log.e("FoodDetailScreen", "Fehler beim Laden des Produkts: ${e.message}", e)
                    errorMessage = "Fehler beim Laden der Daten: ${e.message}"
                    isLoading = false
                }
        } catch (e: Exception) {
            Log.e("FoodDetailScreen", "Exception beim Laden des Produkts: ${e.message}", e)
            errorMessage = "Fehler beim Laden der Daten: ${e.message}"
            isLoading = false
        }
    }
    
    // Berechne Kalorien basierend auf der Menge
    LaunchedEffect(amountGram, food) {
        if (food != null) {
            val amount = amountGram.toDoubleOrNull() ?: 0.0
            calculatedCalories = (food!!.kcalPer100g * amount / 100).roundToInt()
        }
    }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Futter hinzufügen",
                showBackButton = true,
                onBackClick = onBackClick,
                onAccountClick = { /* Account click handler */ },
                onLogoutClick = { /* Logout click handler */ }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (food == null) {
                Text(
                    text = "Futter nicht gefunden",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Produktinformationen
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (food!!.imageUrl != null) {
                                    AsyncImage(
                                        model = food!!.imageUrl,
                                        contentDescription = "Bild von ${food!!.product}",
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                }
                                
                                Column {
                                    Text(
                                        text = food!!.brand,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    
                                    Text(
                                        text = food!!.product,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Nährwerte pro 100g",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            NutritionRow("Kalorien", "${food!!.kcalPer100g.roundToInt()} kcal")
                            NutritionRow("Protein", "${food!!.protein}g")
                            NutritionRow("Fett", "${food!!.fat}g")
                            NutritionRow("Kohlenhydrate", "${food!!.carbs.roundToInt()}g")
                            NutritionRow("Rohfaser", "${food!!.crudeFiber}g")
                            NutritionRow("Rohasche", "${food!!.rawAsh}g")
                            NutritionRow("Feuchtigkeit", "${food!!.moisture}g")
                            
                            if (food!!.additives.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "Zusatzstoffe",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                food!!.additives.forEach { (key, value) ->
                                    NutritionRow(key, value)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Mengeneingabe
                    Text(
                        text = "Menge eingeben",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = amountGram,
                        onValueChange = { amountGram = it },
                        label = { Text("Menge (g)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Kalorienanzeige
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$calculatedCalories kcal",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Text(
                                text = "für ${amountGram.toDoubleOrNull() ?: 0.0}g",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Notizfeld
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
                    
                    // Speichern-Button
                    Button(
                        onClick = {
                            val amount = amountGram.toDoubleOrNull()
                            if (amount == null || amount <= 0) {
                                errorMessage = "Bitte gib eine gültige Menge ein"
                                return@Button
                            }
                            
                            isSaving = true
                            errorMessage = null
                            
                            val foodIntake = FoodIntake(
                                dogId = dogId,
                                foodId = foodId,
                                foodName = "${food!!.brand} ${food!!.product}",
                                amountGram = amount,
                                calories = calculatedCalories,
                                timestamp = LocalDateTime.now(),
                                note = note.ifBlank { null },
                                protein = food!!.protein * amount / 100,
                                fat = food!!.fat * amount / 100,
                                carbs = food!!.carbs * amount / 100
                            )
                            
                            scope.launch {
                                foodIntakeRepository.addFoodIntake(foodIntake)
                                    .onSuccess {
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
}

@Composable
fun NutritionRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
    
    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
}