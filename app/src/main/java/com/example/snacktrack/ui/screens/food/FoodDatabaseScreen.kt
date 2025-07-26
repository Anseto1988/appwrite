package com.example.snacktrack.ui.screens.food

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.snacktrack.data.model.Food
import com.example.snacktrack.data.repository.FoodRepository
import com.example.snacktrack.ui.components.CommonTopAppBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDatabaseScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val foodRepository = remember { FoodRepository(context) }
    val scope = rememberCoroutineScope()
    
    var foods by remember { mutableStateOf<List<Food>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Lade Futtermittel
    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        
        val result = foodRepository.getAllFoods()
        result.fold(
            onSuccess = { foodList ->
                foods = foodList
                isLoading = false
            },
            onFailure = { exception ->
                error = exception.message
                isLoading = false
            }
        )
    }
    
    // Gefilterte Liste basierend auf Suchbegriff
    val filteredFoods = foods.filter { food ->
        food.name.contains(searchQuery, ignoreCase = true) ||
        food.brand?.contains(searchQuery, ignoreCase = true) == true
    }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Futter-Datenbank",
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Suchleiste
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Suchen...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Löschen")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )
            
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                "Fehler beim Laden der Futtermittel",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                error ?: "Unbekannter Fehler",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                filteredFoods.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                if (searchQuery.isEmpty()) 
                                    "Noch keine Futtermittel vorhanden" 
                                else 
                                    "Keine Futtermittel gefunden",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredFoods) { food ->
                            FoodCard(food = food)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoodCard(food: Food) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        food.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    food.brand?.let { brand ->
                        Text(
                            brand,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Badge(
                    containerColor = when (food.type) {
                        "wet" -> MaterialTheme.colorScheme.primary
                        "dry" -> MaterialTheme.colorScheme.secondary
                        "treat" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        when (food.type) {
                            "wet" -> "Nassfutter"
                            "dry" -> "Trockenfutter"
                            "treat" -> "Leckerli"
                            else -> "Sonstiges"
                        }
                    )
                }
            }
            
            // Nährwerte
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NutrientInfo("Kalorien", "%.2f kcal/g".format(food.caloriesPerGram))
                NutrientInfo("Protein", "%.1f%%".format(food.nutritionalInfo["protein"] ?: 0.0))
                NutrientInfo("Fett", "%.1f%%".format(food.nutritionalInfo["fat"] ?: 0.0))
                NutrientInfo("Kohlenhydrate", "%.1f%%".format(food.nutritionalInfo["carbs"] ?: 0.0))
            }
            
            // Barcode
            if (food.barcode.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.QrCode,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        food.barcode,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun NutrientInfo(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}