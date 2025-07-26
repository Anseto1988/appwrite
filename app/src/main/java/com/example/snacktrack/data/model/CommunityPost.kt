package com.example.snacktrack.data.model

/**
 * Repräsentiert einen Community-Post
 */
data class CommunityPost(
    val id: String,
    val userId: String,
    val dogId: String? = null,
    val title: String = "",
    val content: String,
    val category: String = "general",
    val postType: PostType,
    val imageUrls: List<String> = emptyList(),
    val hashtags: List<String> = emptyList(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val createdAt: String? = null,
    
    // Optional: Erweiterte Informationen für UI
    val userProfile: CommunityProfile? = null,
    val dogInfo: Dog? = null,
    val isLikedByCurrentUser: Boolean = false
)

/**
 * Post-Typen für verschiedene Community-Inhalte
 */
enum class PostType(val displayName: String, val databaseValue: String) {
    PHOTO("Foto", "photo"),
    PROGRESS("Fortschritt", "progress"),
    RECIPE("Rezept", "recipe"),
    TIP("Tipp", "tip"),
    STORY("Geschichte", "story"),
    QUESTION("Frage", "question");
    
    companion object {
        fun fromDatabaseValue(value: String): PostType {
            return entries.find { it.databaseValue == value } 
                ?: throw IllegalArgumentException("Unknown PostType database value: $value")
        }
    }
}
