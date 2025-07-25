package com.example.snacktrack.ui.screens.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snacktrack.ui.components.CommonTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityHubScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Community Hub",
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Welcome Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Willkommen in der SnackTrack Community!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Vernetzen Sie sich mit anderen Hundebesitzern, teilen Sie Tipps und lernen Sie von Experten",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Community Features Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    CommunityFeatureCard(
                        title = "Forum",
                        description = "Diskutieren Sie mit anderen",
                        icon = Icons.Default.Forum,
                        color = Color(0xFF2196F3),
                        onClick = { navController.navigate("community/forum") }
                    )
                }
                
                item {
                    CommunityFeatureCard(
                        title = "Events",
                        description = "Lokale Treffen & Aktivitäten",
                        icon = Icons.Default.Event,
                        color = Color(0xFF4CAF50),
                        onClick = { navController.navigate("community/events") }
                    )
                }
                
                item {
                    CommunityFeatureCard(
                        title = "Experten Q&A",
                        description = "Fragen Sie Profis",
                        icon = Icons.Default.Psychology,
                        color = Color(0xFFFF9800),
                        onClick = { navController.navigate("community/expert-qa") }
                    )
                }
                
                item {
                    CommunityFeatureCard(
                        title = "Rezepte",
                        description = "Teilen Sie Ihre Rezepte",
                        icon = Icons.Default.Restaurant,
                        color = Color(0xFF9C27B0),
                        onClick = { navController.navigate("community/recipes") }
                    )
                }
                
                item {
                    CommunityFeatureCard(
                        title = "Tipps & Tricks",
                        description = "Hilfreiche Ratschläge",
                        icon = Icons.Default.Lightbulb,
                        color = Color(0xFFF44336),
                        onClick = { navController.navigate("community/tips") }
                    )
                }
                
                item {
                    CommunityFeatureCard(
                        title = "Mein Profil",
                        description = "Ihre Community-Aktivität",
                        icon = Icons.Default.AccountCircle,
                        color = Color(0xFF00BCD4),
                        onClick = { navController.navigate("community/profile") }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommunityFeatureCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}