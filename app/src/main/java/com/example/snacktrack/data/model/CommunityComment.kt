package com.example.snacktrack.data.model

/**
 * Repräsentiert einen Kommentar zu einem Community-Post
 */
data class CommunityComment(
    val id: String,
    val postId: String,
    val userId: String,
    val content: String,
    val createdAt: String? = null,
    
    // Optional: Erweiterte Informationen für UI
    val userProfile: CommunityProfile? = null
)