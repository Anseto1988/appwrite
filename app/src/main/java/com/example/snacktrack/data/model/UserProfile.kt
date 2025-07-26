package com.example.snacktrack.data.model

import java.time.LocalDateTime

/**
 * Repräsentiert ein Benutzerprofil für die Community-Funktionen
 */
data class UserProfile(
    val userId: String,
    val displayName: String,
    val bio: String? = null,
    val profileImageUrl: String? = null,
    val joinedAt: LocalDateTime,
    val postCount: Int = 0,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val badges: List<String> = emptyList(),
    val isFollowing: Boolean = false
)