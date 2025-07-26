package com.example.snacktrack.data.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Repräsentiert eine Fütterung
 */
data class Feeding(
    val id: String = "",
    val dogId: String = "",
    val foodId: String? = null,
    val foodName: String = "",
    val amount: Double = 0.0,
    val calories: Int = 0,
    val type: FeedingType = FeedingType.MAIN_MEAL,
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val notes: String? = null
)

/**
 * Art der Fütterung
 */
enum class FeedingType(val displayName: String) {
    MAIN_MEAL("Hauptmahlzeit"),
    SNACK("Snack"),
    TREAT("Leckerli"),
    TRAINING("Training")
}