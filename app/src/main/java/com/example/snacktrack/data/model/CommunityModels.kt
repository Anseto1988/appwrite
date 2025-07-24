package com.example.snacktrack.data.model

import java.time.LocalDateTime

/**
 * Community forum post
 */
data class ForumPost(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatar: String? = null,
    val categoryId: String = "",
    val title: String = "",
    val content: String = "",
    val tags: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val viewCount: Int = 0,
    val likeCount: Int = 0,
    val replyCount: Int = 0,
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val isExpertVerified: Boolean = false,
    val breedSpecific: List<String> = emptyList() // List of breed IDs
)

data class ForumCategory(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val icon: String = "",
    val postCount: Int = 0,
    val isModerated: Boolean = false,
    val allowedUserTypes: List<UserType> = listOf(UserType.REGULAR)
)

data class ForumReply(
    val id: String = "",
    val postId: String = "",
    val parentReplyId: String? = null, // For nested replies
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatar: String? = null,
    val content: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val likeCount: Int = 0,
    val isExpertAnswer: Boolean = false,
    val isBestAnswer: Boolean = false
)

/**
 * Community events
 */
data class CommunityEvent(
    val id: String = "",
    val organizerId: String = "",
    val organizerName: String = "",
    val eventType: EventType = EventType.MEETUP,
    val title: String = "",
    val description: String = "",
    val location: EventLocation = EventLocation(),
    val startDateTime: LocalDateTime = LocalDateTime.now(),
    val endDateTime: LocalDateTime = LocalDateTime.now(),
    val maxParticipants: Int? = null,
    val currentParticipants: Int = 0,
    val registeredUserIds: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    val requirements: EventRequirements = EventRequirements(),
    val isFree: Boolean = true,
    val price: Double? = null,
    val status: EventStatus = EventStatus.UPCOMING,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class EventLocation(
    val name: String = "",
    val address: String = "",
    val city: String = "",
    val zipCode: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isOnline: Boolean = false,
    val onlineLink: String? = null
)

data class EventRequirements(
    val minimumAge: Int? = null,
    val maximumAge: Int? = null,
    val requiredVaccinations: List<String> = emptyList(),
    val allowedBreeds: List<String> = emptyList(), // Empty means all breeds
    val restrictedBreeds: List<String> = emptyList(),
    val requiresRegistration: Boolean = true,
    val requiresLeash: Boolean = true,
    val otherRequirements: String? = null
)

enum class EventType(val displayName: String, val icon: String) {
    MEETUP("Treffen", "🐕"),
    TRAINING("Training", "🎾"),
    COMPETITION("Wettbewerb", "🏆"),
    WORKSHOP("Workshop", "📚"),
    VET_CLINIC("Tierklinik", "🏥"),
    ADOPTION("Adoption", "❤️"),
    CHARITY("Wohltätigkeit", "🎗️"),
    EXHIBITION("Ausstellung", "🎪")
}

enum class EventStatus {
    UPCOMING,
    ONGOING,
    COMPLETED,
    CANCELLED,
    POSTPONED
}

/**
 * Expert profiles and Q&A
 */
data class ExpertProfile(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val title: String = "",
    val credentials: List<String> = emptyList(),
    val specializations: List<ExpertSpecialization> = emptyList(),
    val bio: String = "",
    val yearsOfExperience: Int = 0,
    val verifiedAt: LocalDateTime? = null,
    val rating: Float = 0f,
    val totalAnswers: Int = 0,
    val helpfulAnswers: Int = 0,
    val responseTime: String = "", // e.g., "Usually responds within 24 hours"
    val isAvailable: Boolean = true
)

enum class ExpertSpecialization(val displayName: String) {
    VETERINARY("Tiermedizin"),
    NUTRITION("Ernährung"),
    BEHAVIOR("Verhalten"),
    TRAINING("Training"),
    GROOMING("Pflege"),
    BREEDING("Zucht"),
    EMERGENCY_CARE("Notfallversorgung"),
    HOLISTIC("Ganzheitlich")
}

data class ExpertQuestion(
    val id: String = "",
    val askedByUserId: String = "",
    val askedByUserName: String = "",
    val categoryId: String = "",
    val title: String = "",
    val description: String = "",
    val dogBreed: String? = null,
    val dogAge: Int? = null,
    val urgency: QuestionUrgency = QuestionUrgency.NORMAL,
    val images: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val askedAt: LocalDateTime = LocalDateTime.now(),
    val status: QuestionStatus = QuestionStatus.OPEN,
    val assignedExpertId: String? = null,
    val answeredAt: LocalDateTime? = null,
    val viewCount: Int = 0,
    val isPublic: Boolean = true
)

enum class QuestionUrgency(val displayName: String, val color: String) {
    LOW("Niedrig", "#4CAF50"),
    NORMAL("Normal", "#2196F3"),
    HIGH("Hoch", "#FF9800"),
    URGENT("Dringend", "#F44336")
}

enum class QuestionStatus {
    OPEN,
    ASSIGNED,
    ANSWERED,
    RESOLVED,
    CLOSED
}

/**
 * User-generated content
 */
data class UserRecipe(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val title: String = "",
    val description: String = "",
    val category: RecipeCategory = RecipeCategory.MAIN_MEAL,
    val prepTime: Int = 0, // minutes
    val cookTime: Int = 0, // minutes
    val servings: Int = 1,
    val difficulty: RecipeDifficulty = RecipeDifficulty.EASY,
    val ingredients: List<RecipeIngredient> = emptyList(),
    val instructions: List<String> = emptyList(),
    val nutritionInfo: RecipeNutrition = RecipeNutrition(),
    val suitableFor: RecipeSuitability = RecipeSuitability(),
    val images: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val favoriteCount: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isApproved: Boolean = false,
    val approvedBy: String? = null
)

data class RecipeIngredient(
    val name: String = "",
    val amount: Double = 0.0,
    val unit: String = "",
    val notes: String? = null,
    val isOptional: Boolean = false
)

data class RecipeNutrition(
    val caloriesPerServing: Int? = null,
    val proteinGrams: Double? = null,
    val fatGrams: Double? = null,
    val carbsGrams: Double? = null,
    val fiberGrams: Double? = null,
    val calciumMg: Double? = null,
    val phosphorusMg: Double? = null
)

data class RecipeSuitability(
    val minAge: Int? = null, // months
    val maxAge: Int? = null,
    val suitableBreeds: List<String> = emptyList(), // Empty = all breeds
    val unsuitable: List<String> = emptyList(), // Conditions where not suitable
    val specialDiets: List<SpecialDiet> = emptyList()
)

enum class RecipeCategory(val displayName: String) {
    MAIN_MEAL("Hauptmahlzeit"),
    TREAT("Leckerli"),
    SUPPLEMENT("Ergänzung"),
    PUPPY("Welpen"),
    SENIOR("Senioren"),
    SPECIAL_DIET("Spezialdiät"),
    RAW("Rohfütterung")
}

enum class RecipeDifficulty(val displayName: String) {
    EASY("Einfach"),
    MEDIUM("Mittel"),
    HARD("Schwer")
}

enum class SpecialDiet(val displayName: String) {
    GRAIN_FREE("Getreidefrei"),
    HYPOALLERGENIC("Hypoallergen"),
    LOW_FAT("Fettarm"),
    HIGH_PROTEIN("Proteinreich"),
    DIABETIC("Diabetiker"),
    KIDNEY_FRIENDLY("Nierenfreundlich"),
    WEIGHT_LOSS("Gewichtsreduktion")
}

/**
 * Community tips and tricks
 */
data class CommunityTip(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val category: TipCategory = TipCategory.GENERAL,
    val title: String = "",
    val content: String = "",
    val breedSpecific: List<String> = emptyList(),
    val ageGroup: DogAgeGroup? = null,
    val images: List<String> = emptyList(),
    val videoUrl: String? = null,
    val tags: List<String> = emptyList(),
    val likeCount: Int = 0,
    val saveCount: Int = 0,
    val shareCount: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isVerified: Boolean = false,
    val verifiedBy: String? = null
)

enum class TipCategory(val displayName: String, val icon: String) {
    GENERAL("Allgemein", "💡"),
    TRAINING("Training", "🎾"),
    HEALTH("Gesundheit", "❤️"),
    NUTRITION("Ernährung", "🥩"),
    GROOMING("Pflege", "✂️"),
    BEHAVIOR("Verhalten", "🐕"),
    SAFETY("Sicherheit", "🛡️"),
    TRAVEL("Reisen", "✈️"),
    SEASONAL("Saisonal", "🌦️")
}

enum class DogAgeGroup(val displayName: String) {
    PUPPY("Welpe (0-12 Monate)"),
    YOUNG("Jung (1-3 Jahre)"),
    ADULT("Erwachsen (3-7 Jahre)"),
    SENIOR("Senior (7+ Jahre)")
}

/**
 * User types and permissions
 */
enum class UserType {
    REGULAR,
    EXPERT,
    MODERATOR,
    ADMIN
}

/**
 * Community statistics
 */
data class CommunityStats(
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val totalPosts: Int = 0,
    val totalEvents: Int = 0,
    val totalRecipes: Int = 0,
    val totalTips: Int = 0,
    val totalExperts: Int = 0,
    val questionsAnswered: Int = 0,
    val averageResponseTime: String = "",
    val topContributors: List<TopContributor> = emptyList(),
    val popularBreeds: List<BreedPopularity> = emptyList(),
    val trendingTopics: List<String> = emptyList()
)

data class TopContributor(
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String? = null,
    val contributionCount: Int = 0,
    val contributionType: String = "",
    val badge: ContributorBadge? = null
)

data class BreedPopularity(
    val breedId: String = "",
    val breedName: String = "",
    val userCount: Int = 0,
    val postCount: Int = 0
)

enum class ContributorBadge(val displayName: String, val icon: String) {
    BRONZE("Bronze", "🥉"),
    SILVER("Silber", "🥈"),
    GOLD("Gold", "🥇"),
    PLATINUM("Platin", "💎")
}

/**
 * Moderation and reporting
 */
data class ContentReport(
    val id: String = "",
    val reporterId: String = "",
    val contentType: ContentType = ContentType.POST,
    val contentId: String = "",
    val reason: ReportReason = ReportReason.INAPPROPRIATE,
    val description: String = "",
    val reportedAt: LocalDateTime = LocalDateTime.now(),
    val status: ReportStatus = ReportStatus.PENDING,
    val reviewedBy: String? = null,
    val reviewedAt: LocalDateTime? = null,
    val action: ModerationAction? = null
)

enum class ContentType {
    POST,
    REPLY,
    EVENT,
    RECIPE,
    TIP,
    QUESTION
}

enum class ReportReason(val displayName: String) {
    INAPPROPRIATE("Unangemessen"),
    SPAM("Spam"),
    HARASSMENT("Belästigung"),
    MISINFORMATION("Fehlinformation"),
    COPYRIGHT("Urheberrecht"),
    OTHER("Sonstiges")
}

enum class ReportStatus {
    PENDING,
    REVIEWING,
    RESOLVED,
    DISMISSED
}

enum class ModerationAction {
    WARNING,
    CONTENT_REMOVED,
    USER_SUSPENDED,
    USER_BANNED,
    NO_ACTION
}