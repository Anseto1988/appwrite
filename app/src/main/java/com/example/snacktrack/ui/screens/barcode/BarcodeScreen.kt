package com.example.snacktrack.ui.screens.barcode

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snacktrack.ui.components.CommonTopAppBar
import com.example.snacktrack.ui.viewmodel.BarcodeViewModel
import com.example.snacktrack.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScreen(
    navController: NavController,
    dogId: String,
    viewModel: BarcodeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Barcode Scanner",
                onBackClick = { navController.navigateUp() },
                actions = {
                    IconButton(onClick = { viewModel.searchProductByText("") }) {
                        Icon(Icons.Default.Search, contentDescription = "Suche")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Scanner Status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isScanning) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = if (uiState.isScanning) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (uiState.isScanning) 
                            "Scanner aktiv..." 
                        else 
                            "Scanner bereit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (!uiState.isScanning) {
                        Button(
                            onClick = { viewModel.startScanning() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scanner starten")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.stopScanning() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scanner stoppen")
                        }
                    }
                }
            }
            
            // Gescanntes Produkt
            uiState.scannedProduct?.let { product ->
                ProductCard(
                    product = product,
                    onAddToInventory = { quantity, unit ->
                        viewModel.addToInventory(product, quantity, unit)
                    },
                    onAddToComparison = {
                        viewModel.addToComparison(product)
                    }
                )
            }
            
            // Allergen-Warnungen
            if (uiState.allergenAlerts.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Allergen-Warnung!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        uiState.allergenAlerts.forEach { alert ->
                            Text(
                                "• ${alert.detectedAllergens.joinToString { it.type.name }}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // Empfehlungen
            if (uiState.recommendations.isNotEmpty()) {
                Text(
                    "Alternative Produkte",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                
                uiState.recommendations.forEach { recommendation ->
                    RecommendationCard(recommendation = recommendation)
                }
            }
            
            // Loading Indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            // Error Message
            uiState.error?.let { error ->
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
            
            // Success Message
            uiState.successMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            message,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onAddToInventory: (Double, StockUnit) -> Unit,
    onAddToComparison: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                product.brand,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Nährwertinformationen
            product.nutritionalInfo?.let { nutrition ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NutritionChip("Protein", "${nutrition.protein}%")
                    NutritionChip("Fett", "${nutrition.fat}%")
                    NutritionChip("Kohlenhydrate", "${nutrition.carbohydrates}%")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onAddToInventory(1.0, StockUnit.PACKAGES) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inventar", style = MaterialTheme.typography.bodySmall)
                }
                
                OutlinedButton(
                    onClick = onAddToComparison,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Vergleichen", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun NutritionChip(
    label: String,
    value: String
) {
    AssistChip(
        onClick = { },
        label = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(label, style = MaterialTheme.typography.labelSmall)
                Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun RecommendationCard(
    recommendation: ProductRecommendation
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Recommend,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    recommendation.recommendedProduct.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    recommendation.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                "${(recommendation.score * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}