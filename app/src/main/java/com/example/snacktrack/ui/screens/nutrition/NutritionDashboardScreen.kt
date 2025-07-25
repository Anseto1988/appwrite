package com.example.snacktrack.ui.screens.nutrition

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snacktrack.data.model.*
import com.example.snacktrack.ui.components.CommonTopAppBar
import com.example.snacktrack.ui.viewmodel.NutritionViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionDashboardScreen(
    navController: NavController,
    dogId: String,
    viewModel: NutritionViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(dogId) {
        viewModel.loadNutritionData(dogId, LocalDate.now())
    }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Ernährungsanalyse",
                onBackClick = { navController.navigateUp() },
                actions = {
                    IconButton(onClick = { navController.navigate("nutrition/history/$dogId") }) {
                        Icon(Icons.Default.History, contentDescription = "Verlauf")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Date selector
                item {
                    DateSelector(
                        selectedDate = uiState.selectedDate,
                        onDateChange = { date ->
                            viewModel.loadNutritionData(dogId, date)
                        }
                    )
                }
                
                // Nutrition Score
                item {
                    NutritionScoreCard(
                        score = uiState.nutritionAnalysis?.getNutritionScore() ?: 0,
                        date = uiState.selectedDate
                    )
                }
                
                // Nutrient Overview
                item {
                    NutrientOverviewCard(
                        analysis = uiState.nutritionAnalysis,
                        onDetailsClick = { navController.navigate("nutrition/details/$dogId") }
                    )
                }
                
                // Treat Budget
                item {
                    TreatBudgetCard(
                        treatBudget = uiState.treatBudget,
                        onAddTreat = { navController.navigate("nutrition/add-treat/$dogId") }
                    )
                }
                
                // BARF Calculator
                item {
                    BARFCalculatorCard(
                        calculation = uiState.barfCalculation,
                        onCalculateClick = { navController.navigate("nutrition/barf-calculator/$dogId") }
                    )
                }
                
                // Recommendations
                uiState.nutritionAnalysis?.let { analysis ->
                    val recommendations = analysis.getRecommendations()
                    if (recommendations.isNotEmpty()) {
                        item {
                            RecommendationsCard(recommendations = recommendations)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateSelector(
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onDateChange(selectedDate.minusDays(1)) }
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Vorheriger Tag")
            }
            
            Text(
                text = when (selectedDate) {
                    LocalDate.now() -> "Heute"
                    LocalDate.now().minusDays(1) -> "Gestern"
                    else -> selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(
                onClick = { onDateChange(selectedDate.plusDays(1)) },
                enabled = selectedDate < LocalDate.now()
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Nächster Tag")
            }
        }
    }
}

@Composable
private fun NutritionScoreCard(
    score: Int,
    date: LocalDate
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                score >= 80 -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                score >= 60 -> Color(0xFFFFC107).copy(alpha = 0.1f)
                else -> Color(0xFFFF5722).copy(alpha = 0.1f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Ernährungs-Score",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                CircularProgressIndicator(
                    progress = score / 100f,
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    color = when {
                        score >= 80 -> Color(0xFF4CAF50)
                        score >= 60 -> Color(0xFFFFC107)
                        else -> Color(0xFFFF5722)
                    }
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "$score",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "von 100",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                when {
                    score >= 80 -> "Ausgezeichnet!"
                    score >= 60 -> "Gut"
                    score >= 40 -> "Verbesserungsfähig"
                    else -> "Achtung erforderlich"
                },
                style = MaterialTheme.typography.titleMedium,
                color = when {
                    score >= 80 -> Color(0xFF4CAF50)
                    score >= 60 -> Color(0xFFFFC107)
                    else -> Color(0xFFFF5722)
                }
            )
        }
    }
}

@Composable
private fun NutrientOverviewCard(
    analysis: NutritionAnalysis?,
    onDetailsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Nährstoffübersicht",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onDetailsClick) {
                    Text("Details")
                }
            }
            
            if (analysis != null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Calories
                NutrientProgressBar(
                    label = "Kalorien",
                    current = analysis.totalCalories,
                    target = analysis.recommendedCalories,
                    unit = "kcal",
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Protein
                NutrientProgressBar(
                    label = "Protein",
                    current = analysis.totalProtein.toInt(),
                    target = analysis.recommendedProtein.toInt(),
                    unit = "g",
                    color = Color(0xFF9C27B0)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Fat
                NutrientProgressBar(
                    label = "Fett",
                    current = analysis.totalFat.toInt(),
                    target = analysis.recommendedFat.toInt(),
                    unit = "g",
                    color = Color(0xFFFF9800)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Carbs
                NutrientProgressBar(
                    label = "Kohlenhydrate",
                    current = analysis.totalCarbs.toInt(),
                    target = analysis.recommendedCarbs.toInt(),
                    unit = "g",
                    color = Color(0xFF4CAF50)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Keine Daten für diesen Tag",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun NutrientProgressBar(
    label: String,
    current: Int,
    target: Int,
    unit: String,
    color: Color
) {
    val progress = if (target > 0) (current.toFloat() / target).coerceIn(0f, 1.5f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress)
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "$current / $target $unit",
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    progress < 0.8f -> Color(0xFFFF5722)
                    progress > 1.2f -> Color(0xFFFF5722)
                    else -> Color(0xFF4CAF50)
                }
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun TreatBudgetCard(
    treatBudget: TreatBudget?,
    onAddTreat: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Cookie,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Leckerli-Budget",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(onClick = onAddTreat) {
                    Icon(Icons.Default.Add, contentDescription = "Leckerli hinzufügen")
                }
            }
            
            if (treatBudget != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                // Progress bar
                val progress = (treatBudget.budgetPercentageUsed / 100.0).toFloat()
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        progress <= 0.8f -> Color(0xFF4CAF50)
                        progress <= 1f -> Color(0xFFFFC107)
                        else -> Color(0xFFFF5722)
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${treatBudget.totalTreatCalories} kcal verwendet",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "${treatBudget.remainingBudget} kcal übrig",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = when {
                            treatBudget.remainingBudget > 50 -> Color(0xFF4CAF50)
                            treatBudget.remainingBudget > 0 -> Color(0xFFFFC107)
                            else -> Color(0xFFFF5722)
                        }
                    )
                }
                
                // Recent treats
                if (treatBudget.treats.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "Heute gegeben:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    treatBudget.treats.takeLast(3).forEach { treat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${treat.timeGiven} - ${treat.name}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "${treat.calories} kcal",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BARFCalculatorCard(
    calculation: BARFCalculation?,
    onCalculateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onCalculateClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Calculate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "BARF-Rechner",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (calculation != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    "Tagesration: ${calculation.dailyFoodAmount.toInt()}g",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BARFComponent("Fleisch", calculation.meatAmount.toInt(), Color(0xFFD32F2F))
                    BARFComponent("Knochen", calculation.boneAmount.toInt(), Color(0xFF7B1FA2))
                    BARFComponent("Organe", calculation.organAmount.toInt(), Color(0xFF1976D2))
                    BARFComponent("Gemüse", calculation.vegetableAmount.toInt(), Color(0xFF388E3C))
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Tippen für BARF-Berechnung",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BARFComponent(
    label: String,
    amount: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            "${amount}g",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RecommendationsCard(
    recommendations: List<NutritionRecommendation>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Empfehlungen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            recommendations.forEach { recommendation ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (recommendation.severity) {
                            RecommendationSeverity.HIGH -> Color(0xFFFFEBEE)
                            RecommendationSeverity.MEDIUM -> Color(0xFFFFF3E0)
                            RecommendationSeverity.LOW -> Color(0xFFE8F5E9)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            when (recommendation.severity) {
                                RecommendationSeverity.HIGH -> Icons.Default.Error
                                RecommendationSeverity.MEDIUM -> Icons.Default.Warning
                                RecommendationSeverity.LOW -> Icons.Default.Info
                            },
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = when (recommendation.severity) {
                                RecommendationSeverity.HIGH -> Color(0xFFD32F2F)
                                RecommendationSeverity.MEDIUM -> Color(0xFFF57C00)
                                RecommendationSeverity.LOW -> Color(0xFF388E3C)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            recommendation.message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}