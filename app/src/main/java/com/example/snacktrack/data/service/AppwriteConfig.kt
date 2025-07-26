package com.example.snacktrack.data.service

/**
 * Appwrite configuration constants
 */
object AppwriteConfig {
    const val ENDPOINT = "https://parse.nordburglarp.de/v1"
    const val PROJECT_ID = "snackrack2"
    const val API_KEY = "standard_6ecfcfdc68e8b72e8b7a6b10e6385848df6fb9b1a778918e8582a8f58319881aa90fe956d9feec7a534488b1d43f147fb170ca4c6197f646c0148b708400ee2a98e06b036f6dabc17128ee3388eebf088dd981f94e23f288658e19dd7f8d7b0c1a7ce1988f8cbc5e15b49ca4538166c217935c1b0164dd156388ce87012ea8c5"
    
    // Database IDs
    const val DATABASE_ID = "snacktrack"
    
    // Collection IDs
    const val COLLECTION_USERS = "users"
    const val COLLECTION_DOGS = "dogs"
    const val COLLECTION_FEEDINGS = "feedings"
    const val COLLECTION_WEIGHT_ENTRIES = "weight_entries"
    const val COLLECTION_FOODS = "foods"
    const val COLLECTION_TEAMS = "teams"
    const val COLLECTION_TEAM_MEMBERS = "team_members"
    const val COLLECTION_TEAM_DOGS = "team_dogs"
    const val COLLECTION_ACTIVITIES = "activities"
    const val COLLECTION_NOTIFICATIONS = "notifications"
    const val COLLECTION_HEALTH_RECORDS = "health_records"
    const val COLLECTION_VACCINATIONS = "vaccinations"
    const val COLLECTION_MEDICATIONS = "medications"
    
    // Storage Buckets
    const val BUCKET_DOG_IMAGES = "dog_images"
    const val BUCKET_FOOD_IMAGES = "food_images"
    const val BUCKET_AVATARS = "avatars"
    
    // Additional Collections
    const val COLLECTION_FOOD_DB = "food_db"
    const val COLLECTION_FOOD_SUBMISSIONS = "food_submissions"
    const val COLLECTION_FOOD_INTAKE = "food_intake"
    const val COLLECTION_DOG_SHARING = "dog_sharing"
    const val COLLECTION_COMMUNITY_POSTS = "community_posts"
    const val COLLECTION_COMMUNITY_COMMENTS = "community_comments"
}