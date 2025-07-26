package com.example.snacktrack.ui.screens.dogs

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.util.Log
import com.example.snacktrack.data.model.Dog
import com.example.snacktrack.data.repository.AuthRepository
import com.example.snacktrack.data.repository.DogRepository
import com.example.snacktrack.data.service.AppwriteService
import com.example.snacktrack.ui.components.CommonTopAppBar
import com.example.snacktrack.ui.navigation.Screen
import com.example.snacktrack.ui.theme.Green
import com.example.snacktrack.ui.viewmodel.DogViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DogListScreen(
    onDogClick: (String) -> Unit,
    onAddDogClick: () -> Unit,
    onAdminClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dogRepository = remember { DogRepository(context) }
    val dogViewModel = remember { DogViewModel(context) }

    val dogs by dogRepository.getDogs().collectAsState(initial = emptyList())
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Dialog-Zustände
    var showDeleteDialog by remember { mutableStateOf(false) }
    var dogToDelete by remember { mutableStateOf<Dog?>(null) }

    LaunchedEffect(Unit) {
        isLoading = false
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Meine Hunde",
                showBackButton = false,
                showDogDetailButton = false,
                onAdminClick = onAdminClick,
                onAccountClick = { navController.navigate(Screen.AccountManagement.route) },
                onLogoutClick = onLogoutClick,
                onCommunityClick = { /* TODO: Community Feature */ } // Navigation zur Community
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddDogClick,
                containerColor = Green // Beachte: Material3 verwendet containerColor
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Hund hinzufügen"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (dogs.isEmpty()) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                    Text(
                        text = "Noch keine Hunde hinzugefügt",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tippe auf das Plus-Symbol, um deinen ersten Hund hinzuzufügen",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = dogs,
                        key = { dog -> dog.id } // Stable key for better recomposition
                    ) { dog ->
                        DogItem(
                            dog = dog,
                            dogViewModel = dogViewModel,
                            onClick = remember(dog.id) { { onDogClick(dog.id) } },
                            onEditClick = remember(dog.id) {
                                {
                                    // Navigiere zum Bearbeiten des Hundes
                                    navController.navigate(Screen.EditDog.createRoute(dog.id))
                                }
                            },
                            onDeleteClick = remember(dog) {
                                {
                                    dogToDelete = dog
                                    showDeleteDialog = true
                                }
                            }
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Lösch-Dialog
            if (showDeleteDialog && dogToDelete != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Hund löschen") },
                    text = { Text("Möchtest du ${dogToDelete?.name} wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                dogToDelete?.id?.let { dogId ->
                                    scope.launch {
                                        dogViewModel.deleteDog(dogId)
                                    }
                                }
                                showDeleteDialog = false
                            }
                        ) {
                            Text("Löschen")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteDialog = false }
                        ) {
                            Text("Abbrechen")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DogItem(
    dog: Dog,
    dogViewModel: DogViewModel,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (dog.imageId != null) {
                    // Die Bilddaten als Flow vom ViewModel abonnieren
                    val imageDataFlow = dogViewModel.getImageDataFlow(dog.imageId)
                    val imageData by imageDataFlow.collectAsState()
                    
                    if (imageData == null) {
                        // Während Bilddaten geladen werden
                        CircularProgressIndicator(
                            modifier = Modifier.size(30.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        // Bilddaten wurden geladen, jetzt das Bild anzeigen
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageData)
                                .crossfade(true)
                                .listener(
                                    onError = { _, result ->
                                        android.util.Log.e("DogImage", "Fehler beim Laden des Bildes für ${dog.name}: ${result.throwable.message}")
                                    },
                                    onSuccess = { _, _ ->
                                        android.util.Log.d("DogImage", "Bild für ${dog.name} erfolgreich geladen")
                                    }
                                )
                                .build(),
                            contentDescription = "Bild von ${dog.name}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dog.name.firstOrNull()?.toString() ?: "?",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick)
            ) {
                Text(
                    text = dog.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dog.breed,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${dog.weight} kg • ${dog.calculateDailyCalorieNeed()} kcal täglich",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Menü für Bearbeiten und Löschen
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Aktionen"
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Bearbeiten") },
                        leadingIcon = { 
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Bearbeiten"
                            ) 
                        },
                        onClick = {
                            showMenu = false
                            onEditClick()
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("Löschen") },
                        leadingIcon = { 
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Löschen"
                            ) 
                        },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}