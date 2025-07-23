package com.example.snacktrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snacktrack.ui.components.CommonTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "SnackTrack Home",
                onAccountClick = { navController.navigate("account") },
                onLogoutClick = { /* Implement logout logic */ }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Dies ist der Home-Screen.\nDer BarcodeScanner wurde entfernt.")
        }
    }
}
