package com.example.snacktrack.ui.screens.barcode

import android.Manifest
import android.graphics.Bitmap
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.snacktrack.data.model.*
import com.example.snacktrack.ui.viewmodel.BarcodeViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun BarcodeScannerScreen(
    navController: NavController,
    dogId: String,
    viewModel: BarcodeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    var showProductDetails by remember { mutableStateOf(false) }
    var showComparison by remember { mutableStateOf(false) }
    var showInventory by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted && !cameraPermissionState.status.shouldShowRationale) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Barcode Scanner") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("barcode/history") }) {
                        Icon(Icons.Default.History, contentDescription = "Verlauf")
                    }
                    IconButton(onClick = { showInventory = true }) {
                        Badge(
                            modifier = Modifier.offset(x = 8.dp, y = (-8).dp)
                        ) {
                            Text("${uiState.inventory.size}")
                        }
                        Icon(Icons.Default.Inventory, contentDescription = "Inventar")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (cameraPermissionState.status.isGranted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Camera Preview
                CameraPreview(
                    onBarcodeScanned = { _ ->
                        // Barcode scanning will be handled by processBarcodeImage
                    }
                )
                
                // Scan Overlay
                ScanOverlay(
                    modifier = Modifier.fillMaxSize(),
                    isScanning = uiState.isScanning
                )
                
                // Bottom Sheet with Results
                AnimatedVisibility(
                    visible = uiState.scannedProduct != null,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it },
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    ProductResultCard(
                        product = uiState.scannedProduct!!,
                        allergenAlert = uiState.allergenAlerts.firstOrNull(),
                        onDetailsClick = { showProductDetails = true },
                        onAddToInventory = { product -> 
                            viewModel.addToInventory(product, 1.0, StockUnit.PACKAGES) 
                        },
                        onCompare = { showComparison = true },
                        onDismiss = { viewModel.clearScannedProduct() }
                    )
                }
                
                // Loading Indicator
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        } else {
            // Permission not granted
            PermissionRequestScreen(
                onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
            )
        }
    }
    
    // Product Details Dialog
    if (showProductDetails && uiState.scannedProduct != null) {
        ProductDetailsDialog(
            product = uiState.scannedProduct!!,
            onDismiss = { showProductDetails = false },
            onAddToShopping = { /* TODO: Implement shopping list */ }
        )
    }
    
    // Product Comparison Sheet
    if (showComparison) {
        ProductComparisonSheet(
            products = uiState.comparingProducts,
            comparison = uiState.comparisonResult,
            onAddProduct = { navController.navigate("barcode/search") },
            onCompare = { viewModel.compareProducts(it) },
            onDismiss = { showComparison = false }
        )
    }
    
    // Inventory Sheet
    if (showInventory) {
        InventorySheet(
            inventory = uiState.inventory,
            onUpdateStock = { productId, quantity, unit ->
                viewModel.updateInventoryQuantity(productId, quantity)
            },
            onDismiss = { showInventory = false }
        )
    }
}

@Composable
private fun CameraPreview(
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
    
    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    ) { view ->
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(view.surfaceProvider)
            }
            
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcode ->
                        onBarcodeScanned(barcode)
                    })
                }
            
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                // Handle camera error
            }
        }, ContextCompat.getMainExecutor(context))
    }
}

@Composable
private fun ScanOverlay(
    modifier: Modifier = Modifier,
    isScanning: Boolean
) {
    Box(modifier = modifier) {
        // Scanning frame
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.Center)
        ) {
            // Corner markers
            val cornerSize = 50.dp
            val cornerWidth = 4.dp
            
            // Top left
            Box(
                modifier = Modifier
                    .size(cornerSize)
                    .align(Alignment.TopStart)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cornerWidth)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Box(
                    modifier = Modifier
                        .width(cornerWidth)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            
            // Top right
            Box(
                modifier = Modifier
                    .size(cornerSize)
                    .align(Alignment.TopEnd)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cornerWidth)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Box(
                    modifier = Modifier
                        .width(cornerWidth)
                        .fillMaxHeight()
                        .align(Alignment.TopEnd)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            
            // Bottom left
            Box(
                modifier = Modifier
                    .size(cornerSize)
                    .align(Alignment.BottomStart)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cornerWidth)
                        .align(Alignment.BottomStart)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Box(
                    modifier = Modifier
                        .width(cornerWidth)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            
            // Bottom right
            Box(
                modifier = Modifier
                    .size(cornerSize)
                    .align(Alignment.BottomEnd)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cornerWidth)
                        .align(Alignment.BottomEnd)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Box(
                    modifier = Modifier
                        .width(cornerWidth)
                        .fillMaxHeight()
                        .align(Alignment.TopEnd)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            
            // Scanning line animation
            if (isScanning) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.primary)
                        .align(Alignment.Center)
                )
            }
        }
        
        // Instructions
        Text(
            "Barcode im Rahmen positionieren",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
                .background(
                    Color.Black.copy(alpha = 0.7f),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ProductResultCard(
    product: Product,
    allergenAlert: AllergenAlert?,
    onDetailsClick: () -> Unit,
    onAddToInventory: (Product) -> Unit,
    onCompare: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Allergen Alert
            allergenAlert?.let { alert ->
                AlertCard(
                    alert = alert,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Product Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Product Image
                AsyncImage(
                    model = product.images.firstOrNull()?.url,
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Product Details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        product.brand,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Price
                    product.variants.firstOrNull()?.price?.let { price ->
                        Text(
                            "€${String.format("%.2f", price.amount)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Close button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.Top)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Schließen")
                }
            }
            
            // Quick Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                product.nutritionalInfo?.let { nutrition ->
                    NutritionChip(
                        label = "Protein",
                        value = "${nutrition.protein.toInt()}%"
                    )
                    NutritionChip(
                        label = "Fett",
                        value = "${nutrition.fat.toInt()}%"
                    )
                    NutritionChip(
                        label = "Kalorien",
                        value = "${nutrition.calories.toInt()} kcal"
                    )
                }
            }
            
            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDetailsClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Details")
                }
                
                OutlinedButton(
                    onClick = onCompare,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Compare,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Vergleichen")
                }
                
                Button(
                    onClick = { onAddToInventory(product) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hinzufügen")
                }
            }
        }
    }
}

@Composable
private fun AlertCard(
    alert: AllergenAlert,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (alert.severity) {
        AlertSeverity.INFO -> Color(0xFF2196F3).copy(alpha = 0.1f)
        AlertSeverity.WARNING -> Color(0xFFFF9800).copy(alpha = 0.1f)
        AlertSeverity.DANGER -> Color(0xFFFF5722).copy(alpha = 0.1f)
        AlertSeverity.CRITICAL -> Color(0xFFF44336).copy(alpha = 0.1f)
    }
    
    val contentColor = when (alert.severity) {
        AlertSeverity.INFO -> Color(0xFF1976D2)
        AlertSeverity.WARNING -> Color(0xFFF57C00)
        AlertSeverity.DANGER -> Color(0xFFE64A19)
        AlertSeverity.CRITICAL -> Color(0xFFD32F2F)
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                when (alert.severity) {
                    AlertSeverity.INFO -> Icons.Default.Info
                    AlertSeverity.WARNING -> Icons.Default.Warning
                    AlertSeverity.DANGER -> Icons.Default.Warning
                    AlertSeverity.CRITICAL -> Icons.Default.Error
                },
                contentDescription = null,
                tint = contentColor
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Allergen-Warnung",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    alert.recommendation,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
private fun NutritionChip(
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDetailsDialog(
    product: Product,
    onDismiss: () -> Unit,
    onAddToShopping: (Product) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                product.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                product.brand,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Schließen")
                        }
                    }
                }
                
                // Images
                if (product.images.isNotEmpty()) {
                    item {
                        ProductImageGallery(images = product.images)
                    }
                }
                
                // Category & Certifications
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = { },
                            label = { Text(getCategoryText(product.category)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Category,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                        
                        product.certifications.forEach { cert ->
                            AssistChip(
                                onClick = { },
                                label = { Text(cert.type.name) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFF4CAF50)
                                    )
                                }
                            )
                        }
                    }
                }
                
                // Description
                if (product.description.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                product.description,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Nutritional Information
                product.nutritionalInfo?.let { nutrition ->
                    item {
                        NutritionalInfoSection(nutrition)
                    }
                }
                
                // Ingredients
                if (product.ingredients.isNotEmpty()) {
                    item {
                        IngredientsSection(product.ingredients)
                    }
                }
                
                // Allergens
                if (product.allergens.isNotEmpty()) {
                    item {
                        AllergensSection(product.allergens)
                    }
                }
                
                // Variants
                if (product.variants.isNotEmpty()) {
                    item {
                        VariantsSection(product.variants)
                    }
                }
                
                // Actions
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onAddToShopping(product) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Zur Einkaufsliste")
                        }
                        
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Fertig")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductImageGallery(images: List<ProductImage>) {
    var selectedIndex by remember { mutableStateOf(0) }
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Main image
        AsyncImage(
            model = images[selectedIndex].url,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        
        // Thumbnails
        if (images.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                images.forEachIndexed { index, image ->
                    AsyncImage(
                        model = image.url,
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { selectedIndex = index }
                            .then(
                                if (index == selectedIndex) {
                                    Modifier.border(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(4.dp)
                                    )
                                } else Modifier
                            ),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
private fun NutritionalInfoSection(nutrition: NutritionalInfo) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Nährwertangaben",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "pro ${nutrition.servingSize}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Divider()
            
            NutritionRow("Energie", "${nutrition.calories.toInt()} kcal")
            NutritionRow("Protein", "${nutrition.protein}%")
            NutritionRow("Fett", "${nutrition.fat}%")
            NutritionRow("Kohlenhydrate", "${nutrition.carbohydrates}%")
            NutritionRow("Ballaststoffe", "${nutrition.fiber}%")
            NutritionRow("Feuchtigkeit", "${nutrition.moisture}%")
            
            nutrition.guaranteedAnalysis?.let { analysis ->
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    "Garantierte Analyse",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                NutritionRow("Rohprotein (min)", "${analysis.crudeProteinMin}%")
                NutritionRow("Rohfett (min)", "${analysis.crudeFatMin}%")
                NutritionRow("Rohfaser (max)", "${analysis.crudeFiberMax}%")
                NutritionRow("Feuchtigkeit (max)", "${analysis.moistureMax}%")
            }
        }
    }
}

@Composable
private fun NutritionRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun IngredientsSection(ingredients: List<Ingredient>) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Zutaten",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            ingredients.forEach { ingredient ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        when (ingredient.source) {
                            IngredientSource.ANIMAL -> Icons.Default.Pets
                            IngredientSource.PLANT -> Icons.Default.Grass
                            IngredientSource.MINERAL -> Icons.Default.Science
                            IngredientSource.SYNTHETIC -> Icons.Default.Biotech
                            null -> Icons.Default.Circle
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        ingredient.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    ingredient.percentage?.let { percentage ->
                        Text(
                            "${percentage}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AllergensSection(allergens: List<Allergen>) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    "Enthält Allergene",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            allergens.forEach { allergen ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        getAllergenText(allergen.type),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Badge(
                        containerColor = when (allergen.severity) {
                            AllergenSeverity.MILD -> Color(0xFF4CAF50)
                            AllergenSeverity.MODERATE -> Color(0xFFFF9800)
                            AllergenSeverity.SEVERE -> Color(0xFFFF5722)
                            AllergenSeverity.LIFE_THREATENING -> Color(0xFFF44336)
                        }
                    ) {
                        Text(
                            when (allergen.severity) {
                                AllergenSeverity.MILD -> "Mild"
                                AllergenSeverity.MODERATE -> "Moderat"
                                AllergenSeverity.SEVERE -> "Schwer"
                                AllergenSeverity.LIFE_THREATENING -> "Lebensbedrohlich"
                            }
                        )
                    }
                }
                
                allergen.notes?.let { notes ->
                    Text(
                        notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun VariantsSection(variants: List<ProductVariant>) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Verfügbare Varianten",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            variants.forEach { variant ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            variant.size,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        variant.flavor?.let { flavor ->
                            Text(
                                flavor,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    variant.price?.let { price ->
                        Text(
                            "€${String.format("%.2f", price.amount)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductComparisonSheet(
    products: List<Product>,
    comparison: ProductComparison?,
    onAddProduct: () -> Unit,
    onCompare: (List<ComparisonCriterion>) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Produkte vergleichen",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            // Selected products
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products) { product ->
                    ComparisonProductCard(product)
                }
                
                item {
                    Card(
                        modifier = Modifier
                            .size(100.dp)
                            .clickable { onAddProduct() },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Produkt hinzufügen",
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    "Hinzufügen",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
            
            if (products.size >= 2) {
                // Comparison criteria
                Text(
                    "Vergleichskriterien",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                val criteria = remember {
                    mutableStateListOf(
                        ComparisonCriterion(CriterionType.PRICE, 1.0),
                        ComparisonCriterion(CriterionType.PROTEIN_CONTENT, 1.0),
                        ComparisonCriterion(CriterionType.ALLERGEN_FREE, 1.0)
                    )
                }
                
                criteria.forEach { criterion ->
                    CriterionSelector(
                        criterion = criterion,
                        onWeightChange = { weight ->
                            val index = criteria.indexOf(criterion)
                            criteria[index] = criterion.copy(weight = weight)
                        }
                    )
                }
                
                Button(
                    onClick = { onCompare(criteria) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Vergleichen")
                }
                
                // Comparison results
                comparison?.let { comp ->
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    ComparisonResultsSection(comp)
                }
            }
        }
    }
}

@Composable
private fun ComparisonProductCard(product: Product) {
    Card(
        modifier = Modifier.size(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = product.images.firstOrNull()?.url,
                contentDescription = product.name,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            Text(
                product.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CriterionSelector(
    criterion: ComparisonCriterion,
    onWeightChange: (Double) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            getCriterionText(criterion.type),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        Slider(
            value = criterion.weight.toFloat(),
            onValueChange = { onWeightChange(it.toDouble()) },
            valueRange = 0f..2f,
            steps = 3,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            when {
                criterion.weight < 0.5 -> "Unwichtig"
                criterion.weight < 1.5 -> "Normal"
                else -> "Wichtig"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ComparisonResultsSection(comparison: ProductComparison) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Ergebnisse",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Winner
        comparison.recommendation?.let { rec ->
            val winner = comparison.products.find { it.id == rec.recommendedProductId }
            winner?.let { product ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Empfehlung: ${product.name}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                rec.reason,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
        
        // Rankings
        comparison.results.rankings.forEach { ranking ->
            val product = comparison.products.find { it.id == ranking.productId }
            product?.let {
                RankingCard(
                    rank = ranking.rank,
                    product = it,
                    score = ranking.score,
                    pros = ranking.pros,
                    cons = ranking.cons
                )
            }
        }
    }
}

@Composable
private fun RankingCard(
    rank: Int,
    product: Product,
    score: Double,
    pros: List<String>,
    cons: List<String>
) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when (rank) {
                            1 -> Color(0xFFFFD700)
                            2 -> Color(0xFFC0C0C0)
                            3 -> Color(0xFFCD7F32)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$rank",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Score: ${score.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (pros.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            Icons.Default.ThumbUp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF4CAF50)
                        )
                        Text(
                            pros.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
                
                if (cons.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            Icons.Default.ThumbDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFF44336)
                        )
                        Text(
                            cons.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF44336)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventorySheet(
    inventory: List<ProductInventory>,
    onUpdateStock: (String, Double, StockUnit) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Inventar",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(inventory) { item ->
                InventoryItemCard(
                    inventory = item,
                    onUpdateStock = { quantity, unit ->
                        onUpdateStock(item.product.id, quantity, unit)
                    }
                )
            }
        }
    }
}

@Composable
private fun InventoryItemCard(
    inventory: ProductInventory,
    onUpdateStock: (Double, StockUnit) -> Unit
) {
    var showUpdateDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = inventory.product.images.firstOrNull()?.url,
                contentDescription = inventory.product.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    inventory.product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "${inventory.currentStock.quantity} ${getStockUnitText(inventory.currentStock.unit)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    inventory.consumptionRate?.daysUntilEmpty?.let { days ->
                        Badge(
                            containerColor = when {
                                days <= 3 -> MaterialTheme.colorScheme.error
                                days <= 7 -> Color(0xFFFF9800)
                                else -> MaterialTheme.colorScheme.primary
                            }
                        ) {
                            Text("$days Tage")
                        }
                    }
                }
                
                inventory.expirationDate?.let { expDate ->
                    Text(
                        "Haltbar bis: ${expDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (expDate.isBefore(LocalDateTime.now().plusWeeks(1))) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            IconButton(onClick = { showUpdateDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Bearbeiten")
            }
        }
    }
    
    if (showUpdateDialog) {
        StockUpdateDialog(
            currentStock = inventory.currentStock,
            onUpdate = onUpdateStock,
            onDismiss = { showUpdateDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StockUpdateDialog(
    currentStock: StockLevel,
    onUpdate: (Double, StockUnit) -> Unit,
    onDismiss: () -> Unit
) {
    var quantity by remember { mutableStateOf(currentStock.quantity.toString()) }
    var unit by remember { mutableStateOf(currentStock.unit) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bestand aktualisieren") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Menge") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { }
                ) {
                    OutlinedTextField(
                        value = getStockUnitText(unit),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Einheit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    quantity.toDoubleOrNull()?.let { qty ->
                        onUpdate(qty, unit)
                    }
                    onDismiss()
                }
            ) {
                Text("Aktualisieren")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

@Composable
private fun PermissionRequestScreen(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                "Kamera-Berechtigung erforderlich",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            
            Text(
                "Um Barcodes zu scannen, benötigt die App Zugriff auf Ihre Kamera.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            
            Button(onClick = onRequestPermission) {
                Text("Berechtigung erteilen")
            }
        }
    }
}

// Helper functions

private fun getCategoryText(category: ProductCategory): String = when (category) {
    ProductCategory.DRY_FOOD -> "Trockenfutter"
    ProductCategory.WET_FOOD -> "Nassfutter"
    ProductCategory.TREATS -> "Leckerli"
    ProductCategory.SUPPLEMENTS -> "Ergänzungsmittel"
    ProductCategory.RAW_FOOD -> "Rohfutter"
    ProductCategory.MEDICATION -> "Medikamente"
    ProductCategory.GROOMING -> "Pflege"
    ProductCategory.TOYS -> "Spielzeug"
    ProductCategory.ACCESSORIES -> "Zubehör"
    ProductCategory.OTHER -> "Sonstiges"
}

private fun getAllergenText(type: AllergenType): String = when (type) {
    AllergenType.CHICKEN -> "Huhn"
    AllergenType.BEEF -> "Rind"
    AllergenType.LAMB -> "Lamm"
    AllergenType.FISH -> "Fisch"
    AllergenType.EGGS -> "Eier"
    AllergenType.DAIRY -> "Milchprodukte"
    AllergenType.WHEAT -> "Weizen"
    AllergenType.CORN -> "Mais"
    AllergenType.SOY -> "Soja"
    AllergenType.PEANUTS -> "Erdnüsse"
    AllergenType.TREE_NUTS -> "Baumnüsse"
    AllergenType.SHELLFISH -> "Schalentiere"
    AllergenType.OTHER -> "Sonstige"
}

private fun getCriterionText(type: CriterionType): String = when (type) {
    CriterionType.PRICE -> "Preis"
    CriterionType.PROTEIN_CONTENT -> "Proteingehalt"
    CriterionType.FAT_CONTENT -> "Fettgehalt"
    CriterionType.CALORIES -> "Kalorien"
    CriterionType.INGREDIENT_QUALITY -> "Zutatenqualität"
    CriterionType.ALLERGEN_FREE -> "Allergenfrei"
    CriterionType.CERTIFICATIONS -> "Zertifizierungen"
    CriterionType.USER_RATINGS -> "Nutzerbewertungen"
    CriterionType.AVAILABILITY -> "Verfügbarkeit"
    CriterionType.SUSTAINABILITY -> "Nachhaltigkeit"
}

private fun getStockUnitText(unit: StockUnit): String = when (unit) {
    StockUnit.PACKAGES -> "Packungen"
    StockUnit.KILOGRAMS -> "kg"
    StockUnit.POUNDS -> "lbs"
    StockUnit.CANS -> "Dosen"
    StockUnit.BAGS -> "Beutel"
    StockUnit.PIECES -> "Stück"
}

// Barcode Analyzer for CameraX
private class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {
    
    private val scanner = BarcodeScanning.getClient()
    private var isProcessing = false
    
    override fun analyze(imageProxy: ImageProxy) {
        if (isProcessing) {
            imageProxy.close()
            return
        }
        
        isProcessing = true
        
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    barcodes.firstOrNull()?.rawValue?.let { barcode ->
                        onBarcodeDetected(barcode)
                    }
                    isProcessing = false
                }
                .addOnFailureListener {
                    isProcessing = false
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
            isProcessing = false
        }
    }
}