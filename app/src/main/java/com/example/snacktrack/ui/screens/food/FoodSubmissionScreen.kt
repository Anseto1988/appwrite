package com.example.snacktrack.ui.screens.food

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import com.example.snacktrack.ui.components.CommonTopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.snacktrack.data.model.FoodSubmission
import com.example.snacktrack.data.model.SubmissionStatus
import com.example.snacktrack.data.repository.FoodRepository
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSubmissionScreen(
    dogId: String,
    ean: String,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val foodRepository = remember { FoodRepository(context) }

    var brand by remember { mutableStateOf("") }
    var product by remember { mutableStateOf("") }
    var proteinInput by remember { mutableStateOf("") } // Geändert zu proteinInput etc.
    var fatInput by remember { mutableStateOf("") }
    var crudeFiberInput by remember { mutableStateOf("") }
    var rawAshInput by remember { mutableStateOf("") }
    var moistureInput by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    fun String.normalizeAndConvertToDouble(): Double? {
        return this.trim()
            .removeSuffix("%") // Entfernt ein eventuelles %-Zeichen am Ende
            .replace(',', '.') // Ersetzt Komma durch Punkt
            .toDoubleOrNull()
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Neues Futter vorschlagen",
                showBackButton = true,
                onBackClick = onBackClick,
                onAccountClick = { /* Account click handler */ },
                onLogoutClick = { /* Logout click handler */ }
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
            Text("EAN: $ean", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text("Marke") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = product,
                onValueChange = { product = it },
                label = { Text("Produktname") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Nährwerte (Angabe in % pro 100g):", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = proteinInput,
                onValueChange = { proteinInput = it },
                label = { Text("Protein (%)") }, // Label angepasst
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = fatInput,
                onValueChange = { fatInput = it },
                label = { Text("Fett (%)") }, // Label angepasst
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = crudeFiberInput,
                onValueChange = { crudeFiberInput = it },
                label = { Text("Rohfaser (%)") }, // Label angepasst
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = rawAshInput,
                onValueChange = { rawAshInput = it },
                label = { Text("Rohasche (%)") }, // Label angepasst
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = moistureInput,
                onValueChange = { moistureInput = it },
                label = { Text("Feuchtigkeit (%)") }, // Label angepasst
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (brand.isBlank() || product.isBlank() || proteinInput.isBlank() || fatInput.isBlank() || crudeFiberInput.isBlank() || rawAshInput.isBlank() || moistureInput.isBlank()) {
                        errorMessage = "Bitte alle Pflichtfelder ausfüllen."
                        return@Button
                    }
                    val proteinValue = proteinInput.normalizeAndConvertToDouble()
                    val fatValue = fatInput.normalizeAndConvertToDouble()
                    val crudeFiberValue = crudeFiberInput.normalizeAndConvertToDouble()
                    val rawAshValue = rawAshInput.normalizeAndConvertToDouble()
                    val moistureValue = moistureInput.normalizeAndConvertToDouble()

                    if (proteinValue == null || fatValue == null || crudeFiberValue == null || rawAshValue == null || moistureValue == null) {
                        errorMessage = "Ungültige Nährwertangaben. Bitte geben Sie gültige Zahlen ein (z.B. 10.5 oder 10,5)."
                        return@Button
                    }

                    // Optionale zusätzliche Validierung: Summe der Prozente
                    // val sumOfNutrients = proteinValue + fatValue + crudeFiberValue + rawAshValue + moistureValue
                    // if (sumOfNutrients > 100.5) { // Kleine Toleranz für Rundungsfehler
                    //     errorMessage = "Die Summe der Nährwertprozente übersteigt 100%."
                    //     return@Button
                    // }


                    isLoading = true
                    errorMessage = null

                    val foodSubmission = FoodSubmission(
                        ean = ean,
                        brand = brand,
                        product = product,
                        protein = proteinValue,
                        fat = fatValue,
                        crudeFiber = crudeFiberValue,
                        rawAsh = rawAshValue,
                        moisture = moistureValue,
                        status = SubmissionStatus.PENDING,
                        submittedAt = LocalDateTime.now()
                    )

                    scope.launch {
                        foodRepository.submitFoodEntry(foodSubmission)
                            .onSuccess {
                                isLoading = false
                                showSuccessDialog = true
                            }
                            .onFailure { e ->
                                isLoading = false
                                errorMessage = "Fehler beim Senden: ${e.message}"
                            }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Vorschlag senden")
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onSaveSuccess()
            },
            title = { Text("Vorschlag gesendet") },
            text = { Text("Vielen Dank! Dein Futtervorschlag wurde erfolgreich übermittelt und wird bald geprüft.") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    onSaveSuccess()
                }) {
                    Text("OK")
                }
            }
        )
    }
}