package com.example.snacktrack.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
                    onButtonClick = { navController.navigate(Screen.DogManagement.route) }
                )
            } else {
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
    val tiles = remember(selectedDog) {
        listOf(
            // Hauptfunktionen (große Kacheln)
            DashboardTile(
                title = "Futter Tracking",
                subtitle = "Kalorien & Nährstoffe verfolgen",
                icon = Icons.Default.Restaurant,
                color = MaterialTheme.colorScheme.primary,
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
                icon = Icons.Default.TrendingUp,
                color = MaterialTheme.colorScheme.secondary,
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
                color = MaterialTheme.colorScheme.tertiary,
                onClick = { navController.navigate(Screen.DogManagement.route) }
            ),
            DashboardTile(
                title = "Barcode Scanner",
                subtitle = "Schnell erfassen",
                icon = Icons.Default.QrCodeScanner,
                color = MaterialTheme.colorScheme.primary,
                onClick = {
                    selectedDog?.let { dog ->
                        navController.navigate("barcode_scanner/${dog.id}")
                    }
                }
            ),
            DashboardTile(
                title = "Community",
                subtitle = "Austauschen",
                icon = Icons.Default.Forum,
                color = MaterialTheme.colorScheme.secondary,
                onClick = { navController.navigate("community_feed") }
            ),
            DashboardTile(
                title = "Einstellungen",
                subtitle = "App konfigurieren",
                icon = Icons.Default.Settings,
                color = MaterialTheme.colorScheme.outline,
                onClick = { navController.navigate(Screen.AccountManagement.route) }
            ),
            DashboardTile(
                title = "Statistiken",
                subtitle = "Auswertungen",
                icon = Icons.Default.Analytics,
                color = MaterialTheme.colorScheme.tertiary,
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
                color = MaterialTheme.colorScheme.primary,
                onClick = { navController.navigate(Screen.TeamManagement.route) }
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