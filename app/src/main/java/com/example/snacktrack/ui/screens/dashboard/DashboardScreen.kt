package com.example.snacktrack.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.snacktrack.data.model.Dog
import com.example.snacktrack.data.model.FoodIntake
import com.example.snacktrack.data.repository.AuthRepository
import com.example.snacktrack.data.repository.DogRepository
import com.example.snacktrack.data.repository.FoodIntakeRepository
import com.example.snacktrack.data.service.AppwriteService
import com.example.snacktrack.ui.components.CommonTopAppBar
import com.example.snacktrack.ui.navigation.Screen
import com.example.snacktrack.ui.theme.Green
import com.example.snacktrack.ui.theme.Red
import com.example.snacktrack.ui.theme.Yellow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    dogId: String,
    navController: androidx.navigation.NavController,
    onDogDetailClick: () -> Unit,
    onScannerClick: () -> Unit,
    onManualEntryClick: () -> Unit,
    onWeightHistoryClick: () -> Unit,
    onAddWeightClick: () -> Unit,
    onBackClick: () -> Unit,
    onAdminClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dogRepository = remember { DogRepository(context) }
    val foodIntakeRepository = remember { FoodIntakeRepository(context) }
    
    var dog by remember { mutableStateOf<Dog?>(null) }
    var foodIntakes by remember { mutableStateOf<List<FoodIntake>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var totalCalories by remember { mutableStateOf(0) }
    
    val today = LocalDate.now()
    
    LaunchedEffect(dogId) {
        try {
            // Lade den Hund
            dog = dogRepository.getDogs().firstOrNull()?.find { it.id == dogId }
            
            // Lade die Futteraufnahmen für heute
            foodIntakeRepository.getFoodIntakesForDog(dogId, today)
                .collect { intakes ->
                    foodIntakes = intakes
                    totalCalories = intakes.sumOf { it.calories }
                }
            
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Fehler beim Laden der Daten: ${e.message}"
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = dog?.name ?: "Dashboard",
                showBackButton = true,
                showDogDetailButton = true,
                onBackClick = onBackClick,
                onDogDetailClick = onDogDetailClick,
                onAdminClick = onAdminClick,
                onAccountClick = { navController.navigate(Screen.AccountManagement.route) },
                onLogoutClick = onLogoutClick
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
            } else if (dog == null) {
                Text(
                    text = "Hund nicht gefunden",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Datum
                    Text(
                        text = "${today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.GERMAN)}, ${today.format(DateTimeFormatter.ofPattern("dd. MMMM yyyy"))}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Kalorientacho
                    CalorieGauge(
                        consumedCalories = totalCalories,
                        dailyCalorieNeed = dog!!.calculateDailyCalorieNeed(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Quick Actions
                    Text(
                        text = "Schnellaktionen",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Zeige standardmäßig 4 Aktionen in Quick Actions (Admin-Button nur in TopAppBar)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionItem(
                            icon = Icons.Default.CameraAlt,
                            label = "Barcode scannen",
                            modifier = Modifier.weight(1f),
                            onClick = onScannerClick
                        )
                        
                        QuickActionItem(
                            icon = Icons.Default.Edit,
                            label = "Manueller Eintrag",
                            modifier = Modifier.weight(1f),
                            onClick = onManualEntryClick
                        )
                        
                        QuickActionItem(
                            icon = Icons.Default.Forum,
                            label = "Community",
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate("community_feed") }
                        )
                        
                        QuickActionItem(
                            icon = Icons.Default.BarChart,
                            label = "Gewichtsverlauf",
                            modifier = Modifier.weight(1f),
                            onClick = onWeightHistoryClick
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Letzte Einträge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Heutige Einträge",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Text(
                            text = "Gewicht hinzufügen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onAddWeightClick() }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (foodIntakes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Noch keine Einträge heute",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(foodIntakes) { intake ->
                                FoodIntakeItem(intake = intake)
                            }
                        }
                    }
                }
            }
            
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun CalorieGauge(
    consumedCalories: Int,
    dailyCalorieNeed: Int,
    modifier: Modifier = Modifier
) {
    val percentage = (consumedCalories.toFloat() / dailyCalorieNeed).coerceIn(0f, 1f)
    val color = when {
        percentage < 0.5f -> Green
        percentage < 0.9f -> Yellow
        else -> Red
    }
    
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage)
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$consumedCalories kcal",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "$dailyCalorieNeed kcal benötigt",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun QuickActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FoodIntakeItem(
    intake: FoodIntake,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = intake.foodName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${intake.amountGram}g",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Text(
                text = "${intake.calories} kcal",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}