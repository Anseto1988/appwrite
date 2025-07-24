package com.example.snacktrack.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.snacktrack.ui.screens.auth.LoginScreen
import com.example.snacktrack.ui.screens.auth.RegisterScreen
import com.example.snacktrack.ui.screens.dashboard.DashboardScreen
import com.example.snacktrack.ui.screens.dashboard.ModernDashboardScreen
import com.example.snacktrack.ui.screens.dog.AddEditDogScreen
import com.example.snacktrack.ui.screens.dog.DogDetailScreen
import com.example.snacktrack.ui.screens.dog.DogListScreen
import com.example.snacktrack.ui.screens.food.FoodDetailScreen
import com.example.snacktrack.ui.screens.food.ManualFoodEntryScreen
import com.example.snacktrack.ui.screens.food.FoodSubmissionScreen
import com.example.snacktrack.ui.screens.food.FoodSubmissionAdminScreen // Import für den Admin-Screen
import com.example.snacktrack.ui.screens.weight.AddWeightScreen
import com.example.snacktrack.ui.screens.weight.WeightHistoryScreen
import com.example.snacktrack.ui.screens.food.BarcodeScanner // Import für den BarcodeScanner Screen
import com.example.snacktrack.ui.screens.account.AccountManagementScreen // Import für den Kontoverwaltungs-Screen
import com.example.snacktrack.ui.viewmodel.DogViewModel
// Community Screen Imports
import com.example.snacktrack.ui.screens.community.CommunityFeedScreen
import com.example.snacktrack.ui.screens.community.CreatePostScreen
import com.example.snacktrack.ui.screens.community.PostDetailScreen
import com.example.snacktrack.ui.screens.community.ProfileScreen

// ViewModel Factory für DogViewModel (sollte in einer eigenen Datei oder DogViewModel.kt sein)
class DogViewModelFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DogViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    
    object Home : Screen("home")
    object DogList : Screen("dog_list")
    
    object AccountManagement : Screen("account_management")

    object Dashboard : Screen("dashboard/{dogId}") {
        fun createRoute(dogId: String) = "dashboard/$dogId"
    }
    
    object FoodSubmissionAdmin : Screen("food_submissions_admin")
    
    // Community Screens
    object CommunityFeed : Screen("community_feed")
    
    object CreatePost : Screen("community_create_post")
    
    object PostDetail : Screen("community_post_detail/{postId}") {
        fun createRoute(postId: String) = "community_post_detail/$postId"
    }
    
    object Profile : Screen("community_profile?userId={userId}") {
        fun createRoute(userId: String? = null) = userId?.let { "community_profile?userId=$it" } ?: "community_profile"
    }

    object AddDog : Screen("add_dog") // Eigene Route für das Hinzufügen
    object EditDog : Screen("edit_dog/{dogId}") { // Eigene Route für das Bearbeiten
        fun createRoute(dogId: String) = "edit_dog/$dogId"
    }

    object DogDetail : Screen("dog_detail/{dogId}") {
        fun createRoute(dogId: String) = "dog_detail/$dogId"
    }

    // Eigene Route für den BarcodeScanner-Bildschirm
    object BarcodeScannerNav : Screen("barcode_scanner_nav/{dogId}") {
        fun createRoute(dogId: String) = "barcode_scanner_nav/$dogId"
    }

    object FoodDetail : Screen("food_detail/{foodId}/{dogId}") {
        fun createRoute(foodId: String, dogId: String) = "food_detail/$foodId/$dogId"
    }

    object ManualFoodEntry : Screen("manual_food_entry/{dogId}") {
        fun createRoute(dogId: String) = "manual_food_entry/$dogId"
    }

    // Route für den FoodSubmissionScreen
    object FoodSubmission : Screen("food_submission/{dogId}?ean={ean}") {
        fun createRoute(dogId: String, ean: String) = "food_submission/$dogId?ean=$ean"
    }

    object AddWeight : Screen("add_weight/{dogId}") {
        fun createRoute(dogId: String) = "add_weight/$dogId"
    }

    object WeightHistory : Screen("weight_history/{dogId}") {
        fun createRoute(dogId: String) = "weight_history/$dogId"
    }
}

@Composable
fun SnackTrackNavGraph(navController: NavHostController) {
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) { // Zum neuen Home Dashboard
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.DogList.route) { // Zu DogList nach Registrierung
                        popUpTo(Screen.Register.route) { inclusive = true }
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // Neues Home Dashboard
        composable(Screen.Home.route) {
            ModernDashboardScreen(
                navController = navController,
                onLogoutClick = { 
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.DogList.route) {
            DogListScreen(
                onDogClick = { dogId ->
                    navController.navigate(Screen.Dashboard.createRoute(dogId)) // Zu Dashboard mit dogId
                },
                onAddDogClick = { navController.navigate(Screen.AddDog.route) }, // Zu AddDog
                onAdminClick = { navController.navigate(Screen.FoodSubmissionAdmin.route) }, // Zum Admin-Panel
                onLogoutClick = { 
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true } // Gesamten Navigations-Stack leeren
                    }
                },
                navController = navController
            )
        }

        composable(
            route = Screen.Dashboard.route,
            arguments = listOf(navArgument("dogId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
            // KORREKTUR HIER: Die Lambdas erwarten keine Parameter, da der dogId bereits
            // im DashboardScreen durch die Navigation bekannt ist und von dort verwendet wird.
            DashboardScreen(
                dogId = dogId,
                navController = navController,
                onDogDetailClick = { navController.navigate(Screen.DogDetail.createRoute(dogId)) },
                onManualEntryClick = { navController.navigate(Screen.ManualFoodEntry.createRoute(dogId)) },
                onWeightHistoryClick = { navController.navigate(Screen.WeightHistory.createRoute(dogId)) },
                onAddWeightClick = { navController.navigate(Screen.AddWeight.createRoute(dogId)) },
                onScannerClick = { navController.navigate(Screen.BarcodeScannerNav.createRoute(dogId)) },
                onBackClick = { navController.popBackStack() },
                onAdminClick = { navController.navigate(Screen.FoodSubmissionAdmin.route) },
                onLogoutClick = { 
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true } // Gesamten Navigations-Stack leeren
                    }
                }
            )
        }

        composable(Screen.AddDog.route) { // Für neuen Hund
            AddEditDogScreen(
                dogId = null,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditDog.route, // Für existierenden Hund
            arguments = listOf(navArgument("dogId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dogId = backStackEntry.arguments?.getString("dogId")
            AddEditDogScreen(
                dogId = dogId,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.DogDetail.route,
            arguments = listOf(navArgument("dogId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
            val dogViewModel: DogViewModel = viewModel(factory = DogViewModelFactory(context)) // ViewModel bereitstellen
            DogDetailScreen(
                dogId = dogId,
                navController = navController,
                dogViewModel = dogViewModel
            )
        }

        composable(
            route = Screen.BarcodeScannerNav.route,
            arguments = listOf(navArgument("dogId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
            BarcodeScanner(
                dogId = dogId,
                onFoodFound = { foodId ->
                    navController.popBackStack()
                    navController.navigate(Screen.FoodDetail.createRoute(foodId, dogId))
                },
                onFoodNotFound = { ean ->
                    navController.popBackStack()
                    navController.navigate(Screen.FoodSubmission.createRoute(dogId, ean))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.FoodDetail.route,
            arguments = listOf(
                navArgument("foodId") { type = NavType.StringType },
                navArgument("dogId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId") ?: ""
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
            FoodDetailScreen(
                foodId = foodId,
                dogId = dogId,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ManualFoodEntry.route,
            arguments = listOf(navArgument("dogId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
            ManualFoodEntryScreen(
                dogId = dogId,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.FoodSubmission.route,
            arguments = listOf(
                navArgument("dogId") { type = NavType.StringType },
                navArgument("ean") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
            val ean = backStackEntry.arguments?.getString("ean") ?: ""
            FoodSubmissionScreen(
                dogId = dogId,
                ean = ean,
                onSaveSuccess = {
                    navController.popBackStack()
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddWeight.route,
            arguments = listOf(navArgument("dogId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
            AddWeightScreen(
                dogId = dogId,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.WeightHistory.route,
            arguments = listOf(navArgument("dogId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dogId = backStackEntry.arguments?.getString("dogId") ?: ""
            WeightHistoryScreen(dogId = dogId, navController = navController)
        }
        
        // Admin-Screen für die Verwaltung von Futterbeiträgen
        composable(route = Screen.FoodSubmissionAdmin.route) {
            FoodSubmissionAdminScreen(navController = navController)
        }
        
        // Kontoverwaltungs-Screen für Team-Verwaltung
        composable(route = Screen.AccountManagement.route) {
            AccountManagementScreen(navController = navController)
        }
        
        // Community Feed Screen
        composable(route = Screen.CommunityFeed.route) {
            CommunityFeedScreen(
                navController = navController,
                onCreatePost = { navController.navigate(Screen.CreatePost.route) },
                onPostClick = { postId -> navController.navigate(Screen.PostDetail.createRoute(postId)) },
                onProfileClick = { userId -> navController.navigate(Screen.Profile.createRoute(userId)) }
            )
        }
        
        // Post Detail Screen
        composable(
            route = Screen.PostDetail.route,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            PostDetailScreen(
                postId = postId,
                navController = navController
            )
        }
        
        // Create Post Screen
        composable(route = Screen.CreatePost.route) {
            CreatePostScreen(navController = navController)
        }
        
        // Profile Screen
        composable(
            route = Screen.Profile.route,
            arguments = listOf(navArgument("userId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            ProfileScreen(navController = navController, userId = userId)
        }
    }
}