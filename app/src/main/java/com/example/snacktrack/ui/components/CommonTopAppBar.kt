package com.example.snacktrack.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Groups // Icon für Community
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.snacktrack.data.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * Gemeinsame TopAppBar-Komponente für alle Screens
 * @param title Titel der TopAppBar
 * @param showBackButton Soll der Zurück-Button angezeigt werden?
 * @param showDogDetailButton Soll der Hunde-Details-Button angezeigt werden?
 * @param currentDogOwnerId Die ID des Besitzers des aktuellen Hundes, oder null wenn kein Hund angezeigt wird
 * @param onBackClick Callback für den Zurück-Button
 * @param onDogDetailClick Callback für den Hunde-Details-Button
 * @param onAdminClick Callback für den Admin-Button
 * @param onAccountClick Callback für den Kontoverwaltungs-Button
 * @param onLogoutClick Callback für den Logout-Button
 * @param actions Optionaler zusätzlicher Content für die Actions-Sektion (rechts)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopAppBar(
    title: String,
    showBackButton: Boolean = true,
    showDogDetailButton: Boolean = false,
    currentDogOwnerId: String? = null,
    onBackClick: () -> Unit = {},
    onDogDetailClick: () -> Unit = {},
    onAdminClick: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onCommunityClick: () -> Unit = {}, // Neuer Parameter für Community-Navigation
    actions: @Composable RowScope.() -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository(context) }
    
    // Prüfe, ob der Benutzer Admin-Rechte hat
    var isAdmin by remember { mutableStateOf(false) }
    var currentUserId by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        // Admin-Status abrufen
        authRepository.isAdmin().collect { adminStatus ->
            isAdmin = adminStatus
        }
    }
    
    LaunchedEffect(Unit) {
        // Aktuelle Benutzer-ID abrufen
        authRepository.getCurrentUser().collect { user ->
            user?.let {
                currentUserId = it.id
            }
        }
    }
    
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zurück"
                    )
                }
            }
        },
        actions = {
            // Hunde-Details-Button (Edit-Button)
            if (showDogDetailButton) {
                android.util.Log.d("CommonTopAppBar", "🎯 Edit-Button wird angezeigt")
                IconButton(
                    onClick = { 
                        android.util.Log.d("CommonTopAppBar", "🔥 Edit-Button WURDE GEKLICKT!")
                        try {
                            onDogDetailClick()
                            android.util.Log.d("CommonTopAppBar", "✅ onDogDetailClick Callback aufgerufen")
                        } catch (e: Exception) {
                            android.util.Log.e("CommonTopAppBar", "❌ Fehler im onDogDetailClick: ${e.message}", e)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Hund bearbeiten"
                    )
                }
            } else {
                android.util.Log.d("CommonTopAppBar", "❌ Edit-Button wird NICHT angezeigt (showDogDetailButton ist false)")
            }
            
            // Community-Button für Zugriff auf soziale Funktionen
            IconButton(onClick = onCommunityClick) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = "Community"
                )
            }
            
            // Benutzerdefinierte Actions einfügen
            actions()
            
            // Kontoverwaltungs-Button
            IconButton(onClick = onAccountClick) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Kontoverwaltung"
                )
            }
            
            // Admin-Button anzeigen wenn Benutzer Admin-Rechte hat
            if (isAdmin) {
                IconButton(onClick = onAdminClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Admin-Panel"
                    )
                }
            }
            
            // Logout-Button
            IconButton(
                onClick = {
                    scope.launch {
                        authRepository.logout()
                            .onSuccess {
                                onLogoutClick()
                            }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Abmelden"
                )
            }
        }
    )
}