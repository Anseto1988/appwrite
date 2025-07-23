package com.example.snacktrack.data.model

/**
 * Repräsentiert ein Community-Profil eines Benutzers
 */
data class CommunityProfile(
    val id: String,
    val userId: String,
    val displayName: String,
    val bio: String? = null,
    val profileImageUrl: String? = null,
    val isPremium: Boolean = false,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0,
    val createdAt: String? = null
)
