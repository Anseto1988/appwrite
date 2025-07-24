package com.example.snacktrack.ui.screens.community

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.snacktrack.data.model.Dog
import com.example.snacktrack.data.model.PostType
import com.example.snacktrack.data.repository.DogRepository
import com.example.snacktrack.ui.viewmodel.CommunityViewModel
import com.example.snacktrack.ui.viewmodel.CommunityViewModelFactory
import com.example.snacktrack.ui.viewmodel.PostCreationState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: CommunityViewModel = viewModel(factory = CommunityViewModelFactory(context))
    // Direct reference to the ViewModel's state property
    // No delegation needed as postCreationState is already a State object in the ViewModel
    val postCreationState = viewModel.postCreationState
    val dogRepository = remember { DogRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    
    // State für die Formulardaten
    var content by remember { mutableStateOf("") }
    var selectedPostType by remember { mutableStateOf(PostType.PHOTO) }
    var selectedDogId by remember { mutableStateOf<String?>(null) }
    var selectedDog by remember { mutableStateOf<Dog?>(null) }
    var hashtags by remember { mutableStateOf("") }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    
    // Laden der Hundeliste
    var dogs by remember { mutableStateOf<List<Dog>>(emptyList()) }
    LaunchedEffect(true) {
        coroutineScope.launch {
            dogRepository.getDogs()?.collect { dogsList ->
                dogs = dogsList
            }
        }
    }
    
    // Image picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            imageUris = imageUris + it 
        }
    }
    
    // Effekt für erfolgreiche Post-Erstellung
    LaunchedEffect(postCreationState) {
        if (postCreationState is PostCreationState.Success) {
            viewModel.resetPostCreationState()
            navController.popBackStack()
        }
    }
    
    val focusManager = LocalFocusManager.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Neuen Beitrag erstellen") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Beitragstyp-Auswahl
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Beitragstyp wählen",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    PostTypeSelector(
                        selectedPostType = selectedPostType,
                        onPostTypeSelected = { selectedPostType = it }
                    )
                }
            }
            
            // Beitragsinhalt
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Was möchtest du teilen?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 8,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            
            // Bilder hinzufügen
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Bilder hinzufügen",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Button zum Hinzufügen neuer Bilder
                        item {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { imagePicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Bild hinzufügen",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        // Ausgewählte Bilder
                        items(imageUris) { uri ->
                            Box(
                                modifier = Modifier.size(100.dp)
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Ausgewähltes Bild",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                
                                // Löschen-Button
                                IconButton(
                                    onClick = { imageUris = imageUris - uri },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Bild entfernen",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Hashtags
            OutlinedTextField(
                value = hashtags,
                onValueChange = { hashtags = it },
                label = { Text("Hashtags (durch Leerzeichen getrennt)") },
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("#") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            
            // Hund auswählen
            if (dogs.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { /* wird in der DropdownMenuItem behandelt */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedDog?.name ?: "Keinen Hund auswählen",
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = false)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .clickable {
                                // DropDown-Menü manuell anzeigen in separatem Dialog
                                // (wird hier vereinfacht)
                            },
                        label = { Text("Verknüpften Hund auswählen") }
                    )
                }
                
                // Vereinfachte Alternative zur Dropdown-Box
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Verknüpften Hund auswählen",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Option für keinen Hund
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedDogId = null
                                    selectedDog = null
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedDogId == null,
                                onClick = {
                                    selectedDogId = null
                                    selectedDog = null
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Keinen Hund auswählen")
                        }
                        
                        // Optionen für jeden Hund
                        dogs.forEach { dog ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedDogId = dog.id
                                        selectedDog = dog
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedDogId == dog.id,
                                    onClick = {
                                        selectedDogId = dog.id
                                        selectedDog = dog
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(dog.name)
                            }
                        }
                    }
                }
            }
            
            // Fehlermeldung anzeigen
            if (postCreationState is PostCreationState.Error) {
                Text(
                    text = (postCreationState as PostCreationState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Erstellen-Button
            Button(
                onClick = {
                    val hashtagsList = hashtags
                        .trim()
                        .split("\\s+".toRegex())
                        .filter { it.isNotEmpty() }
                    
                    viewModel.createPost(
                        content = content,
                        postType = selectedPostType,
                        dogId = selectedDogId,
                        imageUris = imageUris,
                        hashtags = hashtagsList
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = content.isNotEmpty() && postCreationState !is PostCreationState.Creating
            ) {
                if (postCreationState is PostCreationState.Creating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Beitrag erstellen")
            }
        }
    }
}

@Composable
fun PostTypeSelector(
    selectedPostType: PostType,
    onPostTypeSelected: (PostType) -> Unit
) {
    val postTypes = PostType.entries
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(postTypes) { postType ->
                PostTypeChip(
                    postType = postType,
                    selected = postType == selectedPostType,
                    onClick = { onPostTypeSelected(postType) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostTypeChip(
    postType: PostType,
    selected: Boolean,
    onClick: () -> Unit
) {
    val icon = when(postType) {
        PostType.PHOTO -> Icons.Default.PhotoCamera
        PostType.PROGRESS -> Icons.AutoMirrored.Filled.TrendingUp
        PostType.RECIPE -> Icons.Default.Restaurant
        PostType.TIP -> Icons.Default.Lightbulb
        PostType.STORY -> Icons.Default.Book
        PostType.QUESTION -> Icons.AutoMirrored.Filled.Help
    }
    
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(postType.displayName) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
