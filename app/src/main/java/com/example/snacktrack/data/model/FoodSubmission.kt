package com.example.snacktrack.data.model

import java.time.LocalDateTime

/**
 * Repräsentiert einen Vorschlag eines Nutzers für einen neuen Futtermitteleintrag
 */
data class FoodSubmission(
    val id: String = "",
    val userId: String = "",
    val ean: String = "",
    val brand: String = "",
    val product: String = "",
    val protein: Double = 0.0,
    val fat: Double = 0.0,
    val crudeFiber: Double = 0.0,
    val rawAsh: Double = 0.0,
    val moisture: Double = 0.0,
    val additives: Map<String, String> = emptyMap(),
    val imageUrl: String? = null,
    val status: SubmissionStatus = SubmissionStatus.PENDING,
    val submittedAt: LocalDateTime = LocalDateTime.now(),
    val reviewedAt: LocalDateTime? = null
)

enum class SubmissionStatus {
    PENDING,
    APPROVED,
    REJECTED
} 