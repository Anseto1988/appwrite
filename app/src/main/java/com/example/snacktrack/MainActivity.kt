package com.example.snacktrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.snacktrack.ui.navigation.SnackTrackNavGraph
import com.example.snacktrack.ui.navigation.Screen
import com.example.snacktrack.ui.components.BottomNavigationBar
import com.example.snacktrack.ui.state.GlobalDogState
import com.example.snacktrack.ui.state.LocalGlobalDogState
import com.example.snacktrack.ui.theme.SnacktrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()
            val navBackStackEntry = navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry.value?.destination?.route
            val globalDogState = remember { GlobalDogState() }
            
            // Zeige Bottom Navigation nur auf Hauptseiten
            val showBottomBar = when (currentRoute) {
                "login", "register", "auth" -> false
                else -> currentRoute != null && (
                    currentRoute == Screen.Home.route ||
                    currentRoute == Screen.DogList.route ||
                    currentRoute == Screen.Community.route ||
                    currentRoute == Screen.Settings.route ||
                    currentRoute.startsWith("main/")
                )
            }

            SnacktrackTheme {
                CompositionLocalProvider(LocalGlobalDogState provides globalDogState) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            if (showBottomBar) {
                                BottomNavigationBar(navController = navController)
                            }
                        }
                    ) { innerPadding ->
                        SnackTrackNavGraph(
                            navController = navController,
                            startDestination = "auth",
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SnacktrackTheme {
        androidx.compose.material3.Text("Hello Android!")
    }
}
