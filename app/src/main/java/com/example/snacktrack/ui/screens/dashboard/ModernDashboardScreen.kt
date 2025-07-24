package com.example.snacktrack.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snacktrack.data.model.Dog
import com.example.snacktrack.data.repository.AuthRepository
import com.example.snacktrack.data.repository.DogRepository
import com.example.snacktrack.ui.components.CommonTopAppBar
import com.example.snacktrack.ui.navigation.Screen
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

data class DashboardTile(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit,
    val isLarge: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernDashboardScreen(
    navController: NavController,
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val dogRepository = remember { DogRepository(context) }
    
    var dogs by remember { mutableStateOf<List<Dog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedDog by remember { mutableStateOf<Dog?>(null) }
    
    val today = LocalDate.now()
    
    LaunchedEffect(Unit) {
        try {
            dogs = dogRepository.getDogs().firstOrNull() ?: emptyList()
            selectedDog = dogs.firstOrNull()
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "SnackTrack",
                showBackButton = false,
                showDogDetailButton = false,
                onBackClick = {},
                onDogDetailClick = {},
                onAdminClick = { navController.navigate("admin_moderation") },
                onAccountClick = { navController.navigate(Screen.AccountManagement.route) },
                onLogoutClick = onLogoutClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Begrüßung und Datum
            WelcomeSection(selectedDog = selectedDog, today = today)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (dogs.isEmpty()) {
                EmptyStateCard(
                    title = "Willkommen bei SnackTrack!",
                    subtitle = "Füge deinen ersten Hund hinzu, um zu starten",
                    icon = Icons.Default.Pets,
                    buttonText = "Hund hinzufügen",
                    onButtonClick = { navController.navigate(Screen.DogList.route) }
                )
            } else {
                // Hund-Auswahl falls mehrere Hunde vorhanden
                if (dogs.size > 1) {
                    DogSelectionCard(
                        dogs = dogs,
                        selectedDog = selectedDog,
                        onDogSelected = { selectedDog = it }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Dashboard Kacheln
                DashboardGrid(
                    selectedDog = selectedDog,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun WelcomeSection(
    selectedDog: Dog?,
    today: LocalDate
) {
    Column {
        Text(
            text = selectedDog?.let { "Hallo, ${it.name}!" } ?: "Willkommen!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "${today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.GERMAN)}, ${today.format(DateTimeFormatter.ofPattern("dd. MMMM yyyy"))}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DashboardGrid(
    selectedDog: Dog?,
    navController: NavController
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val outline = MaterialTheme.colorScheme.outline
    
    val tiles = remember(selectedDog, primary, secondary, tertiary, outline) {
        listOf(
            // Hauptfunktionen (große Kacheln)
            DashboardTile(
                title = "Futter Tracking",
                subtitle = "Kalorien & Nährstoffe verfolgen",
                icon = Icons.Default.Restaurant,
                color = primary,
                isLarge = true,
                onClick = {
                    selectedDog?.let { dog ->
                        navController.navigate("dashboard/${dog.id}")
                    }
                }
            ),
            DashboardTile(
                title = "Gewichtsverlauf",
                subtitle = "Gewicht dokumentieren",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                color = secondary,
                isLarge = true,
                onClick = {
                    selectedDog?.let { dog ->
                        navController.navigate("weight_history/${dog.id}")
                    }
                }
            ),
            
            // Sekundäre Funktionen (kleine Kacheln)
            DashboardTile(
                title = "Meine Hunde",
                subtitle = "Verwalten",
                icon = Icons.Default.Pets,
                color = tertiary,
                onClick = { navController.navigate(Screen.DogList.route) }
            ),
            DashboardTile(
                title = "Barcode Scanner",
                subtitle = "Schnell erfassen",
                icon = Icons.Default.QrCodeScanner,
                color = primary,
                onClick = {
                    selectedDog?.let { dog ->
                        navController.navigate(Screen.BarcodeScannerNav.createRoute(dog.id))
                    }
                }
            ),
            DashboardTile(
                title = "Futter Eingabe",
                subtitle = "Manuell hinzufügen",
                icon = Icons.Default.Add,
                color = secondary,
                onClick = {
                    selectedDog?.let { dog ->
                        navController.navigate(Screen.ManualFoodEntry.createRoute(dog.id))
                    }
                }
            ),
            DashboardTile(
                title = "Community",
                subtitle = "Austauschen",
                icon = Icons.Default.Forum,
                color = secondary,
                onClick = { navController.navigate(Screen.CommunityFeed.route) }
            ),
            DashboardTile(
                title = "Einstellungen",
                subtitle = "App konfigurieren",
                icon = Icons.Default.Settings,
                color = outline,
                onClick = { navController.navigate(Screen.AccountManagement.route) }
            ),
            DashboardTile(
                title = "Statistiken",
                subtitle = "Auswertungen",
                icon = Icons.Default.Analytics,
                color = tertiary,
                onClick = {
                    selectedDog?.let { dog ->
                        navController.navigate("statistics/${dog.id}")
                    }
                }
            ),
            DashboardTile(
                title = "Teams",
                subtitle = "Gemeinsam tracken",
                icon = Icons.Default.Group,
                color = primary,
                onClick = { navController.navigate("team_management") }
            )
        )
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(tiles) { tile ->
            DashboardTileCard(
                tile = tile,
                modifier = if (tile.isLarge) {
                    Modifier.height(140.dp)
                } else {
                    Modifier.height(120.dp)
                }
            )
        }
    }
}

@Composable
fun DashboardTileCard(
    tile: DashboardTile,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { tile.onClick() },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
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
                        text = tile.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = tile.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tile.color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tile.icon,
                        contentDescription = null,
                        tint = tile.color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onButtonClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonText)
            }
        }
    }
}

@Composable
fun DogSelectionCard(
    dogs: List<Dog>,
    selectedDog: Dog?,
    onDogSelected: (Dog) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Hund auswählen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dogs.forEach { dog ->
                    val isSelected = selectedDog?.id == dog.id
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onDogSelected(dog) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = if (isSelected) {
                            BorderStroke(
                                2.dp, 
                                MaterialTheme.colorScheme.primary
                            )
                        } else null
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pets,
                                    contentDescription = null,
                                    tint = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = dog.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}