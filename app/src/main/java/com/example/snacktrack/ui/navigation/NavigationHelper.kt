package com.example.snacktrack.ui.navigation

import androidx.navigation.NavController
import com.example.snacktrack.ui.state.GlobalDogState

/**
 * Helper object for simplified navigation with global dog state
 */
object NavigationHelper {
    
    /**
     * Navigate to Statistics screen
     * Uses current dog from global state or prompts to select one
     */
    fun navigateToStatistics(
        navController: NavController,
        globalDogState: GlobalDogState,
        specificDogId: String? = null
    ) {
        val dogId = specificDogId ?: globalDogState.currentDogId
        
        if (dogId != null) {
            navController.navigate(Screen.Statistics.createRoute(dogId))
        } else {
            // Navigate to dog list to select a dog first
            navController.navigate(Screen.DogList.route) {
                launchSingleTop = true
            }
        }
    }
    
    /**
     * Navigate to Feeding screen
     * Uses current dog from global state or prompts to select one
     */
    fun navigateToFeeding(
        navController: NavController,
        globalDogState: GlobalDogState,
        specificDogId: String? = null
    ) {
        val dogId = specificDogId ?: globalDogState.currentDogId
        
        if (dogId != null) {
            navController.navigate(Screen.Feeding.createRoute(dogId))
        } else {
            // Navigate to dog list to select a dog first
            navController.navigate(Screen.DogList.route) {
                launchSingleTop = true
            }
        }
    }
    
    /**
     * Navigate to Add Feeding screen
     * Uses current dog from global state or prompts to select one
     */
    fun navigateToAddFeeding(
        navController: NavController,
        globalDogState: GlobalDogState,
        specificDogId: String? = null
    ) {
        val dogId = specificDogId ?: globalDogState.currentDogId
        
        if (dogId != null) {
            navController.navigate(Screen.AddFeeding.createRoute(dogId))
        } else {
            // Navigate to dog list to select a dog first
            navController.navigate(Screen.DogList.route) {
                launchSingleTop = true
            }
        }
    }
    
    /**
     * Navigate to Weight History screen
     * Uses current dog from global state or prompts to select one
     */
    fun navigateToWeightHistory(
        navController: NavController,
        globalDogState: GlobalDogState,
        specificDogId: String? = null
    ) {
        val dogId = specificDogId ?: globalDogState.currentDogId
        
        if (dogId != null) {
            navController.navigate(Screen.WeightHistory.createRoute(dogId))
        } else {
            // Navigate to dog list to select a dog first
            navController.navigate(Screen.DogList.route) {
                launchSingleTop = true
            }
        }
    }
    
    /**
     * Navigate to Health screen
     * Uses current dog from global state or prompts to select one
     */
    fun navigateToHealth(
        navController: NavController,
        globalDogState: GlobalDogState,
        specificDogId: String? = null
    ) {
        val dogId = specificDogId ?: globalDogState.currentDogId
        
        if (dogId != null) {
            navController.navigate(Screen.Health.createRoute(dogId))
        } else {
            // Navigate to dog list to select a dog first
            navController.navigate(Screen.DogList.route) {
                launchSingleTop = true
            }
        }
    }
    
    /**
     * Navigate to Barcode scanner
     * Uses current dog from global state or prompts to select one
     */
    fun navigateToBarcode(
        navController: NavController,
        globalDogState: GlobalDogState,
        specificDogId: String? = null
    ) {
        val dogId = specificDogId ?: globalDogState.currentDogId
        
        if (dogId != null) {
            navController.navigate(Screen.Barcode.createRoute(dogId))
        } else {
            // Navigate to dog list to select a dog first
            navController.navigate(Screen.DogList.route) {
                launchSingleTop = true
            }
        }
    }
}