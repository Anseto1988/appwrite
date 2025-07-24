package com.example.snacktrack.utils

import java.time.LocalDate

/**
 * Utility object for input validation
 */
object ValidationUtils {
    
    /**
     * Validates email format
     */
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }
    
    /**
     * Validates password strength
     */
    fun isValidPassword(password: String): PasswordValidationResult {
        val errors = mutableListOf<String>()
        
        if (password.length < 8) {
            errors.add("Passwort muss mindestens 8 Zeichen lang sein")
        }
        if (!password.any { it.isUpperCase() }) {
            errors.add("Passwort muss mindestens einen Großbuchstaben enthalten")
        }
        if (!password.any { it.isLowerCase() }) {
            errors.add("Passwort muss mindestens einen Kleinbuchstaben enthalten")
        }
        if (!password.any { it.isDigit() }) {
            errors.add("Passwort muss mindestens eine Zahl enthalten")
        }
        
        return PasswordValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Validates dog name
     */
    fun isValidDogName(name: String): Boolean {
        val trimmedName = name.trim()
        return trimmedName.isNotEmpty() && 
               trimmedName.length <= 50 &&
               trimmedName.matches("^[a-zA-ZäöüÄÖÜß\\s-]+$".toRegex())
    }
    
    /**
     * Validates dog weight
     */
    fun isValidWeight(weight: Double): Boolean {
        return weight in 0.1..200.0 // 100g to 200kg
    }
    
    /**
     * Validates dog birth date
     */
    fun isValidBirthDate(date: LocalDate?): Boolean {
        if (date == null) return true // Birth date is optional
        
        val now = LocalDate.now()
        val maxAge = now.minusYears(30) // Dogs rarely live over 30 years
        
        return date.isAfter(maxAge) && !date.isAfter(now)
    }
    
    /**
     * Validates breed name
     */
    fun isValidBreed(breed: String): Boolean {
        val trimmedBreed = breed.trim()
        return trimmedBreed.isEmpty() || // Breed is optional
               (trimmedBreed.length <= 100 &&
                trimmedBreed.matches("^[a-zA-ZäöüÄÖÜß\\s-]+$".toRegex()))
    }
    
    /**
     * Validates food name
     */
    fun isValidFoodName(name: String): Boolean {
        val trimmedName = name.trim()
        return trimmedName.isNotEmpty() && 
               trimmedName.length <= 100
    }
    
    /**
     * Validates barcode format
     */
    fun isValidBarcode(barcode: String): Boolean {
        return barcode.matches("^[0-9]{8,13}$".toRegex())
    }
    
    /**
     * Validates calorie input
     */
    fun isValidCalories(calories: Int): Boolean {
        return calories in 1..10000 // 1 to 10,000 kcal per 100g
    }
    
    /**
     * Validates percentage input
     */
    fun isValidPercentage(percentage: Double): Boolean {
        return percentage in 0.0..100.0
    }
    
    /**
     * Sanitizes user input to prevent XSS and injection attacks
     */
    fun sanitizeInput(input: String): String {
        return input
            .trim()
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
            .replace("&", "&amp;")
            .replace("\n", " ")
            .replace("\r", " ")
            .replace("\t", " ")
            .take(1000) // Limit length to prevent excessive input
    }
    
    /**
     * Validates team name
     */
    fun isValidTeamName(name: String): Boolean {
        val trimmedName = name.trim()
        return trimmedName.isNotEmpty() && 
               trimmedName.length <= 50 &&
               trimmedName.matches("^[a-zA-Z0-9äöüÄÖÜß\\s-]+$".toRegex())
    }
    
    /**
     * Validates community post content
     */
    fun isValidPostContent(content: String): Boolean {
        val trimmedContent = content.trim()
        return trimmedContent.isNotEmpty() && 
               trimmedContent.length <= 5000
    }
    
    /**
     * Validates comment content
     */
    fun isValidCommentContent(content: String): Boolean {
        val trimmedContent = content.trim()
        return trimmedContent.isNotEmpty() && 
               trimmedContent.length <= 1000
    }
}

data class PasswordValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)