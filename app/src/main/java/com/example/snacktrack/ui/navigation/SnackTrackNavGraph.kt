package com.example.snacktrack.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.snacktrack.ui.screens.auth.LoginScreen
import com.example.snacktrack.ui.screens.auth.RegisterScreen
import com.example.snacktrack.ui.screens.main.TileDashboardScreen
import com.example.snacktrack.ui.screens.dogs.AddDogScreen
import com.example.snacktrack.ui.screens.dogs.DogDetailScreen
import com.example.snacktrack.ui.screens.dogs.DogListScreen
import com.example.snacktrack.ui.screens.feeding.FeedingListScreen
import com.example.snacktrack.ui.screens.weight.WeightHistoryScreen
import com.example.snacktrack.ui.screens.health.HealthOverviewScreen
import com.example.snacktrack.ui.screens.food.FoodDatabaseScreen
import com.example.snacktrack.ui.screens.settings.SettingsScreen
import com.example.snacktrack.ui.screens.statistics.StatisticsScreen
import com.example.snacktrack.ui.screens.dogs.EditDogScreen
import com.example.snacktrack.ui.screens.feeding.AddFeedingScreen
import com.example.snacktrack.ui.screens.admin.FoodSubmissionAdminScreen
import com.example.snacktrack.ui.screens.barcode.BarcodeScreen
import com.example.snacktrack.ui.screens.settings.ExportImportScreen
import com.example.snacktrack.ui.screens.health.PreventionPlanScreen
import com.example.snacktrack.ui.screens.team.TeamManagementScreen
import com.example.snacktrack.ui.screens.account.AccountManagementScreen
import com.example.snacktrack.ui.screens.community.CommunityScreen
import com.example.snacktrack.data.service.AppwriteService
import com.example.snacktrack.ui.viewmodel.*
import androidx.compose.ui.platform.LocalContext

sealed class Screen(val route: String) {
    // Auth Screens
    object Login : Screen("login")
    object Register : Screen("register")
    
    // Main Screens
    object Home : Screen("home")
    object DogList : Screen("dogs")
    object AddDog : Screen("add_dog")
    object DogDetail : Screen("dog/{dogId}") {
        fun createRoute(dogId: String) = "dog/$dogId"
    }
    
    // Feature Screens
    object Feeding : Screen("feeding/{dogId}") {
        fun createRoute(dogId: String) = "feeding/$dogId"
    }
    object WeightHistory : Screen("weight/{dogId}") {
        fun createRoute(dogId: String) = "weight/$dogId"
    }
    object Health : Screen("health/{dogId}") {
        fun createRoute(dogId: String) = "health/$dogId"
    }
    object Statistics : Screen("statistics/{dogId}") {
        fun createRoute(dogId: String) = "statistics/$dogId"
    }
    object AdvancedStatistics : Screen("advanced_statistics/{dogId}") {
        fun createRoute(dogId: String) = "advanced_statistics/$dogId"
    }
    
    // Advanced Features
    object Barcode : Screen("barcode/{dogId}") {
        fun createRoute(dogId: String) = "barcode/$dogId"
    }
    object ExportImport : Screen("export_import")
    object PreventionPlan : Screen("prevention_plan")
    object TeamManagement : Screen("team_management")
    object AccountManagement : Screen("account_management")
    object EditDog : Screen("edit_dog/{dogId}") {
        fun createRoute(dogId: String) = "edit_dog/$dogId"
    }
    object AddFeeding : Screen("add_feeding/{dogId}") {
        fun createRoute(dogId: String) = "add_feeding/$dogId"
    }
    object FoodSubmissionAdmin : Screen("food_submission_admin")
    
    // Other Screens
    object FoodDatabase : Screen("food_database")
    object Settings : Screen("settings")
    object Community : Screen("community")
}

@Composable
fun SnackTrackNavGraph(
    navController: NavHostController,
    startDestination: String = "auth"
) {
    val context = LocalContext.current
    val appwriteService = remember { AppwriteService.getInstance(context) }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Navigation
        navigation(
            startDestination = Screen.Login.route,
            route = "auth"
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("main") {
                            popUpTo("auth") { inclusive = true }
                        }
                    },
                    onRegisterClick = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }
            
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate("main") {
                            popUpTo("auth") { inclusive = true }
                        }
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        // Main Navigation
        navigation(
            startDestination = Screen.Home.route,
            route = "main"
        ) {
            composable(Screen.Home.route) {
                TileDashboardScreen(
                    navController = navController
                )
            }
            
            composable(Screen.DogList.route) {
                DogListScreen(
                    navController = navController,
                    onDogClick = { dogId -> navController.navigate(Screen.DogDetail.createRoute(dogId)) },
                    onAddDogClick = { navController.navigate(Screen.AddDog.route) }
                )
            }
            
            composable(Screen.AddDog.route) {
                val viewModel = remember { AddDogViewModel(context, appwriteService) }
                AddDogScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }
            
            composable(Screen.DogDetail.route) { backStackEntry ->
                val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
                val dogViewModel = remember { DogViewModel(context) }
                DogDetailScreen(
                    dogId = dogId,
                    navController = navController,
                    dogViewModel = dogViewModel
                )
            }
            
            composable(Screen.Feeding.route) { backStackEntry ->
                val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
                FeedingListScreen(
                    dogId = dogId,
                    navController = navController
                )
            }
            
            composable(Screen.WeightHistory.route) { backStackEntry ->
                val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
                WeightHistoryScreen(
                    dogId = dogId,
                    navController = navController
                )
            }
            
            composable(Screen.Health.route) { backStackEntry ->
                val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
                HealthOverviewScreen(
                    dogId = dogId,
                    navController = navController
                )
            }
            
            composable(Screen.Statistics.route) { backStackEntry ->
                val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
                StatisticsScreen(
                    dogId = dogId,
                    navController = navController
                )
            }
            
            // Advanced Statistics placeholder
            composable(Screen.AdvancedStatistics.route) { backStackEntry ->
                val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
                // TODO: Implement AdvancedStatisticsScreen
                StatisticsScreen(
                    dogId = dogId,
                    navController = navController
                )
            }
            
            composable(Screen.TeamManagement.route) {
                TeamManagementScreen(
                    navController = navController
                )
            }
            
            composable(Screen.Barcode.route) { backStackEntry ->
                val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
                val barcodeViewModel = remember { BarcodeViewModel(context, appwriteService, dogId) }
                BarcodeScreen(
                    navController = navController,
                    dogId = dogId,
                    viewModel = barcodeViewModel
                )
            }
            
            composable(Screen.ExportImport.route) {
                ExportImportScreen(
                    navController = navController
                )
            }
            
            composable(Screen.PreventionPlan.route) {
                PreventionPlanScreen(
                    navController = navController
                )
            }
            
            // Placeholders for future screens
            
            composable(Screen.FoodDatabase.route) {
                FoodDatabaseScreen(
                    navController = navController
                )
            }
            
            composable(Screen.Settings.route) {
                SettingsScreen(
                    navController = navController
                )
            }
            
            composable(Screen.AccountManagement.route) {
                AccountManagementScreen(
                    navController = navController
                )
            }
            
            composable(Screen.EditDog.route) { backStackEntry ->
                val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
                EditDogScreen(
                    dogId = dogId,
                    navController = navController
                )
            }
            
            composable(Screen.AddFeeding.route) { backStackEntry ->
                val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
                AddFeedingScreen(
                    dogId = dogId,
                    navController = navController
                )
            }
            
            composable(Screen.FoodSubmissionAdmin.route) {
                FoodSubmissionAdminScreen(
                    navController = navController
                )
            }
            
            composable(Screen.Community.route) {
                CommunityScreen(
                    navController = navController
                )
            }
        }
    }
}