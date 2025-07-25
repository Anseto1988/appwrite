# SnackTrack API Documentation

This document provides comprehensive API documentation for all repository classes in the SnackTrack application. Each repository handles data operations for specific features and communicates with the Appwrite backend.

## 📋 Table of Contents

1. [Base Repository](#base-repository)
2. [Authentication Repository](#authentication-repository)
3. [Dog Repository](#dog-repository)
4. [Food Repository](#food-repository)
5. [Food Intake Repository](#food-intake-repository)
6. [Weight Repository](#weight-repository)
7. [Health Repository](#health-repository)
8. [Team Repository](#team-repository)
9. [Community Repository](#community-repository)
10. [AI Repository](#ai-repository)
11. [Barcode Repository](#barcode-repository)
12. [Statistics Repository](#statistics-repository)
13. [Export Repository](#export-repository)
14. [Offline Repository](#offline-repository)

## Base Repository

All repositories extend from `BaseRepository` which provides common functionality.

### BaseRepository

```kotlin
abstract class BaseRepository {
    protected suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): Result<T>
}
```

**Common Features:**
- Exception handling
- Network error management
- Logging capabilities
- Result wrapping

## Authentication Repository

Handles user authentication and session management.

### Methods

#### login
```kotlin
suspend fun login(email: String, password: String): Result<User>
```
**Parameters:**
- `email`: User's email address
- `password`: User's password

**Returns:** `Result<User>` - User object or error

#### register
```kotlin
suspend fun register(email: String, password: String, name: String): Result<User>
```
**Parameters:**
- `email`: User's email address
- `password`: User's password
- `name`: User's display name

**Returns:** `Result<User>` - Newly created user or error

#### loginWithGoogle
```kotlin
suspend fun loginWithGoogle(activity: Activity): Result<User>
```
**Parameters:**
- `activity`: Android activity for OAuth flow

**Returns:** `Result<User>` - Authenticated user or error

#### logout
```kotlin
suspend fun logout(): Result<Unit>
```
**Returns:** `Result<Unit>` - Success or error

#### getCurrentUser
```kotlin
suspend fun getCurrentUser(): Result<User>
```
**Returns:** `Result<User>` - Current authenticated user or error

## Dog Repository

Manages dog profiles and related operations.

### Methods

#### createDog
```kotlin
suspend fun createDog(dog: Dog): Result<Dog>
```
**Parameters:**
- `dog`: Dog object containing:
  - `name`: String
  - `breed`: String
  - `birthDate`: String (ISO 8601)
  - `weight`: Double
  - `gender`: String ("male" or "female")
  - `neutered`: Boolean
  - `profileImageUrl`: String? (optional)

**Returns:** `Result<Dog>` - Created dog with generated ID

#### getDogs
```kotlin
suspend fun getDogs(userId: String): Result<List<Dog>>
```
**Parameters:**
- `userId`: Owner's user ID

**Returns:** `Result<List<Dog>>` - List of user's dogs

#### getDog
```kotlin
suspend fun getDog(dogId: String): Result<Dog>
```
**Parameters:**
- `dogId`: Dog's unique identifier

**Returns:** `Result<Dog>` - Dog details

#### updateDog
```kotlin
suspend fun updateDog(dogId: String, updates: Map<String, Any>): Result<Dog>
```
**Parameters:**
- `dogId`: Dog's unique identifier
- `updates`: Map of field names to new values

**Returns:** `Result<Dog>` - Updated dog

#### deleteDog
```kotlin
suspend fun deleteDog(dogId: String): Result<Unit>
```
**Parameters:**
- `dogId`: Dog's unique identifier

**Returns:** `Result<Unit>` - Success or error

## Food Repository

Manages food products database.

### Methods

#### searchFoods
```kotlin
suspend fun searchFoods(query: String, limit: Int = 20): Result<List<Food>>
```
**Parameters:**
- `query`: Search term
- `limit`: Maximum results (default: 20)

**Returns:** `Result<List<Food>>` - Matching food products

#### getFoodByBarcode
```kotlin
suspend fun getFoodByBarcode(barcode: String): Result<Food?>
```
**Parameters:**
- `barcode`: EAN/UPC code

**Returns:** `Result<Food?>` - Food product or null if not found

#### getFood
```kotlin
suspend fun getFood(foodId: String): Result<Food>
```
**Parameters:**
- `foodId`: Food's unique identifier

**Returns:** `Result<Food>` - Food details

#### createFood
```kotlin
suspend fun createFood(food: Food): Result<Food>
```
**Parameters:**
- `food`: Food object containing:
  - `brand`: String
  - `product`: String
  - `ean`: String? (optional)
  - `protein`: Double
  - `fat`: Double
  - `crudeFiber`: Double
  - `rawAsh`: Double
  - `moisture`: Double
  - `calories`: Double? (optional)
  - `imageUrl`: String? (optional)

**Returns:** `Result<Food>` - Created food product

## Food Intake Repository

Tracks daily food consumption.

### Methods

#### addFoodIntake
```kotlin
suspend fun addFoodIntake(intake: FoodIntake): Result<FoodIntake>
```
**Parameters:**
- `intake`: FoodIntake object containing:
  - `dogId`: String
  - `foodId`: String
  - `amount`: Double (in grams)
  - `date`: String (ISO 8601)
  - `mealType`: String ("breakfast", "lunch", "dinner", "snack")

**Returns:** `Result<FoodIntake>` - Created intake record

#### getFoodIntakes
```kotlin
suspend fun getFoodIntakes(
    dogId: String, 
    startDate: String, 
    endDate: String
): Result<List<FoodIntake>>
```
**Parameters:**
- `dogId`: Dog's unique identifier
- `startDate`: Start date (ISO 8601)
- `endDate`: End date (ISO 8601)

**Returns:** `Result<List<FoodIntake>>` - Intake records within date range

#### updateFoodIntake
```kotlin
suspend fun updateFoodIntake(
    intakeId: String, 
    amount: Double
): Result<FoodIntake>
```
**Parameters:**
- `intakeId`: Intake record ID
- `amount`: New amount in grams

**Returns:** `Result<FoodIntake>` - Updated intake record

#### deleteFoodIntake
```kotlin
suspend fun deleteFoodIntake(intakeId: String): Result<Unit>
```
**Parameters:**
- `intakeId`: Intake record ID

**Returns:** `Result<Unit>` - Success or error

## Weight Repository

Manages weight tracking history.

### Methods

#### addWeight
```kotlin
suspend fun addWeight(
    dogId: String, 
    weight: Double, 
    date: String
): Result<Weight>
```
**Parameters:**
- `dogId`: Dog's unique identifier
- `weight`: Weight in kg
- `date`: Measurement date (ISO 8601)

**Returns:** `Result<Weight>` - Created weight record

#### getWeightHistory
```kotlin
suspend fun getWeightHistory(
    dogId: String, 
    limit: Int = 30
): Result<List<Weight>>
```
**Parameters:**
- `dogId`: Dog's unique identifier
- `limit`: Maximum records (default: 30)

**Returns:** `Result<List<Weight>>` - Weight history, newest first

#### getLatestWeight
```kotlin
suspend fun getLatestWeight(dogId: String): Result<Weight?>
```
**Parameters:**
- `dogId`: Dog's unique identifier

**Returns:** `Result<Weight?>` - Most recent weight or null

## Health Repository

Manages health records, medications, and allergies.

### Methods

#### addMedication
```kotlin
suspend fun addMedication(medication: DogMedication): Result<DogMedication>
```
**Parameters:**
- `medication`: DogMedication object containing:
  - `dogId`: String
  - `name`: String
  - `dosage`: String
  - `frequency`: String
  - `startDate`: String (ISO 8601)
  - `endDate`: String? (optional)
  - `notes`: String? (optional)

**Returns:** `Result<DogMedication>` - Created medication record

#### getMedications
```kotlin
suspend fun getMedications(
    dogId: String, 
    activeOnly: Boolean = true
): Result<List<DogMedication>>
```
**Parameters:**
- `dogId`: Dog's unique identifier
- `activeOnly`: Filter for active medications only

**Returns:** `Result<List<DogMedication>>` - Medication list

#### addAllergy
```kotlin
suspend fun addAllergy(allergy: DogAllergy): Result<DogAllergy>
```
**Parameters:**
- `allergy`: DogAllergy object containing:
  - `dogId`: String
  - `allergen`: String
  - `severity`: String ("mild", "moderate", "severe")
  - `symptoms`: String? (optional)
  - `diagnosedDate`: String (ISO 8601)

**Returns:** `Result<DogAllergy>` - Created allergy record

#### getAllergies
```kotlin
suspend fun getAllergies(dogId: String): Result<List<DogAllergy>>
```
**Parameters:**
- `dogId`: Dog's unique identifier

**Returns:** `Result<List<DogAllergy>>` - Allergy list

## Team Repository

Handles multi-user collaboration features.

### Methods

#### createTeam
```kotlin
suspend fun createTeam(name: String, description: String?): Result<Team>
```
**Parameters:**
- `name`: Team name
- `description`: Optional team description

**Returns:** `Result<Team>` - Created team

#### inviteToTeam
```kotlin
suspend fun inviteToTeam(
    teamId: String, 
    email: String, 
    role: String = "member"
): Result<TeamInvitation>
```
**Parameters:**
- `teamId`: Team's unique identifier
- `email`: Invitee's email
- `role`: Role in team ("member" or "admin")

**Returns:** `Result<TeamInvitation>` - Created invitation

#### acceptInvitation
```kotlin
suspend fun acceptInvitation(invitationId: String): Result<Unit>
```
**Parameters:**
- `invitationId`: Invitation's unique identifier

**Returns:** `Result<Unit>` - Success or error

#### getTeams
```kotlin
suspend fun getTeams(userId: String): Result<List<Team>>
```
**Parameters:**
- `userId`: User's unique identifier

**Returns:** `Result<List<Team>>` - User's teams

## Community Repository

Manages community features like posts and comments.

### Methods

#### createPost
```kotlin
suspend fun createPost(post: CommunityPost): Result<CommunityPost>
```
**Parameters:**
- `post`: CommunityPost object containing:
  - `title`: String
  - `content`: String
  - `category`: String
  - `tags`: List<String>
  - `imageUrls`: List<String>? (optional)

**Returns:** `Result<CommunityPost>` - Created post

#### getPosts
```kotlin
suspend fun getPosts(
    category: String? = null, 
    limit: Int = 20, 
    offset: Int = 0
): Result<List<CommunityPost>>
```
**Parameters:**
- `category`: Optional category filter
- `limit`: Maximum results
- `offset`: Pagination offset

**Returns:** `Result<List<CommunityPost>>` - Post list

#### addComment
```kotlin
suspend fun addComment(
    postId: String, 
    content: String
): Result<CommunityComment>
```
**Parameters:**
- `postId`: Post's unique identifier
- `content`: Comment text

**Returns:** `Result<CommunityComment>` - Created comment

## AI Repository

Handles AI-powered features and recommendations.

### Methods

#### getRecommendations
```kotlin
suspend fun getRecommendations(
    dogId: String, 
    type: String = "feeding"
): Result<List<AIRecommendation>>
```
**Parameters:**
- `dogId`: Dog's unique identifier
- `type`: Recommendation type ("feeding", "health", "activity")

**Returns:** `Result<List<AIRecommendation>>` - AI recommendations

#### analyzeNutrition
```kotlin
suspend fun analyzeNutrition(
    dogId: String, 
    dateRange: DateRange
): Result<NutritionAnalysis>
```
**Parameters:**
- `dogId`: Dog's unique identifier
- `dateRange`: Date range for analysis

**Returns:** `Result<NutritionAnalysis>` - Detailed nutrition analysis

## Barcode Repository

Manages barcode scanning and product lookup.

### Methods

#### scanBarcode
```kotlin
suspend fun scanBarcode(barcode: String): Result<BarcodeResult>
```
**Parameters:**
- `barcode`: Scanned barcode value

**Returns:** `Result<BarcodeResult>` - Product information or suggestions

#### submitBarcodeProduct
```kotlin
suspend fun submitBarcodeProduct(
    barcode: String, 
    productInfo: Food
): Result<Unit>
```
**Parameters:**
- `barcode`: Product barcode
- `productInfo`: Food product details

**Returns:** `Result<Unit>` - Success or error

## Statistics Repository

Provides analytics and reporting functionality.

### Methods

#### getDogStatistics
```kotlin
suspend fun getDogStatistics(
    dogId: String, 
    period: String = "month"
): Result<DogStatistics>
```
**Parameters:**
- `dogId`: Dog's unique identifier
- `period`: Time period ("week", "month", "year")

**Returns:** `Result<DogStatistics>` - Comprehensive statistics

#### getNutritionTrends
```kotlin
suspend fun getNutritionTrends(
    dogId: String, 
    nutrient: String, 
    days: Int = 30
): Result<List<NutrientTrend>>
```
**Parameters:**
- `dogId`: Dog's unique identifier
- `nutrient`: Nutrient name ("protein", "fat", etc.)
- `days`: Number of days to analyze

**Returns:** `Result<List<NutrientTrend>>` - Trend data points

## Export Repository

Handles data export functionality.

### Methods

#### exportDogData
```kotlin
suspend fun exportDogData(
    dogId: String, 
    format: String = "pdf"
): Result<ExportResult>
```
**Parameters:**
- `dogId`: Dog's unique identifier
- `format`: Export format ("pdf", "csv", "json")

**Returns:** `Result<ExportResult>` - Export file URL or data

#### shareWithVet
```kotlin
suspend fun shareWithVet(
    dogId: String, 
    vetEmail: String, 
    dateRange: DateRange
): Result<Unit>
```
**Parameters:**
- `dogId`: Dog's unique identifier
- `vetEmail`: Veterinarian's email
- `dateRange`: Data range to share

**Returns:** `Result<Unit>` - Success or error

## Offline Repository

Manages offline data synchronization.

### Methods

#### enableOfflineMode
```kotlin
suspend fun enableOfflineMode(dogIds: List<String>): Result<Unit>
```
**Parameters:**
- `dogIds`: Dogs to enable offline mode for

**Returns:** `Result<Unit>` - Success or error

#### syncOfflineData
```kotlin
suspend fun syncOfflineData(): Result<SyncResult>
```
**Returns:** `Result<SyncResult>` - Sync statistics

#### getPendingChanges
```kotlin
suspend fun getPendingChanges(): Result<List<PendingChange>>
```
**Returns:** `Result<List<PendingChange>>` - Unsynced changes

## Error Handling

All repository methods return a `Result<T>` type which can be:
- `Result.Success(data)` - Operation succeeded
- `Result.Error(exception)` - Operation failed

### Common Exceptions

```kotlin
sealed class SnackTrackException : Exception() {
    object NetworkError : SnackTrackException()
    object AuthenticationError : SnackTrackException()
    object NotFoundError : SnackTrackException()
    data class ServerError(val code: Int) : SnackTrackException()
    data class ValidationError(val field: String) : SnackTrackException()
}
```

## Best Practices

1. **Always handle Result types**:
   ```kotlin
   when (val result = repository.getDogs(userId)) {
       is Result.Success -> handleDogs(result.data)
       is Result.Error -> showError(result.exception)
   }
   ```

2. **Use coroutines for async operations**:
   ```kotlin
   viewModelScope.launch {
       val dogs = dogRepository.getDogs(userId)
       // Update UI
   }
   ```

3. **Implement proper error handling**:
   ```kotlin
   try {
       val result = repository.someOperation()
       // Handle success
   } catch (e: SnackTrackException) {
       // Handle specific errors
   }
   ```

---

**Version**: 1.0.0  
**Last Updated**: January 2025

For implementation details, refer to the source code in `app/src/main/java/com/example/snacktrack/data/repository/`