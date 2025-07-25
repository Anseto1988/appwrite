package com.example.snacktrack.data.repository

import com.example.snacktrack.data.model.*
import com.example.snacktrack.data.service.AppwriteService
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.Document
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class HealthRepository(
    private val context: android.content.Context,
    private val appwriteService: AppwriteService
) : BaseRepository() {
    
    companion object {
        const val ALLERGIES_COLLECTION_ID = "allergies"
        const val MEDICATIONS_COLLECTION_ID = "medications"
        const val HEALTH_ENTRIES_COLLECTION_ID = "health_entries"
    }
    
    // Allergy Management
    suspend fun addAllergy(allergy: DogAllergy): Result<DogAllergy> = safeApiCall {
        val data = mapOf(
            "dogId" to allergy.dogId,
            "allergen" to allergy.allergen,
            "allergyType" to allergy.allergyType.name,
            "severity" to allergy.severity.name,
            "symptoms" to allergy.symptoms,
            "diagnosedDate" to allergy.diagnosedDate?.toString(),
            "diagnosedBy" to allergy.diagnosedBy,
            "notes" to allergy.notes,
            "isActive" to allergy.isActive
        )
        
        val document = appwriteService.databases.createDocument(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = ALLERGIES_COLLECTION_ID,
            documentId = ID.unique(),
            data = data
        )
        
        documentToAllergy(document)
    }
    
    suspend fun getAllergiesForDog(dogId: String): Result<List<DogAllergy>> = safeApiCall {
        val response = appwriteService.databases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = ALLERGIES_COLLECTION_ID,
            queries = listOf(
                Query.equal("dogId", dogId),
                Query.equal("isActive", true)
            )
        )
        
        response.documents.map { documentToAllergy(it) }
    }
    
    suspend fun checkFoodForAllergens(dogId: String, ingredients: List<String>): Result<List<DogAllergy>> = safeApiCall {
        val allergies = getAllergiesForDog(dogId).getOrNull() ?: emptyList()
        
        allergies.filter { allergy ->
            ingredients.any { ingredient ->
                ingredient.contains(allergy.allergen, ignoreCase = true) ||
                allergy.allergen.contains(ingredient, ignoreCase = true)
            }
        }
    }
    
    // Medication Management
    suspend fun addMedication(medication: DogMedication): Result<DogMedication> = safeApiCall {
        val data = mapOf(
            "dogId" to medication.dogId,
            "medicationName" to medication.medicationName,
            "medicationType" to medication.medicationType.name,
            "dosage" to medication.dosage,
            "frequency" to medication.frequency.name,
            "startDate" to medication.startDate.toString(),
            "endDate" to medication.endDate?.toString(),
            "reminderTimes" to medication.reminderTimes.map { it.toString() },
            "foodInteraction" to medication.foodInteraction.name,
            "purpose" to medication.purpose,
            "veterinarianName" to medication.veterinarianName,
            "notes" to medication.notes,
            "isActive" to medication.isActive
        )
        
        val document = appwriteService.databases.createDocument(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = MEDICATIONS_COLLECTION_ID,
            documentId = ID.unique(),
            data = data
        )
        
        documentToMedication(document)
    }
    
    suspend fun getActiveMedications(dogId: String): Result<List<DogMedication>> = safeApiCall {
        val response = appwriteService.databases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = MEDICATIONS_COLLECTION_ID,
            queries = listOf(
                Query.equal("dogId", dogId),
                Query.equal("isActive", true)
            )
        )
        
        response.documents.map { documentToMedication(it) }
    }
    
    suspend fun getMedicationReminders(dogId: String, date: LocalDate): Result<List<MedicationReminder>> = safeApiCall {
        val medications = getActiveMedications(dogId).getOrNull() ?: emptyList()
        val reminders = mutableListOf<MedicationReminder>()
        
        medications.forEach { medication ->
            if (date >= medication.startDate && (medication.endDate == null || date <= medication.endDate)) {
                medication.reminderTimes.forEach { time ->
                    reminders.add(
                        MedicationReminder(
                            medication = medication,
                            scheduledTime = LocalDateTime.of(date, time),
                            foodRequirement = medication.foodInteraction
                        )
                    )
                }
            }
        }
        
        reminders.sortedBy { it.scheduledTime }
    }
    
    // Health Diary
    suspend fun addHealthEntry(entry: DogHealthEntry): Result<DogHealthEntry> = safeApiCall {
        val data = mapOf(
            "dogId" to entry.dogId,
            "entryDate" to entry.entryDate.toString(),
            "entryType" to entry.entryType.name,
            "symptoms" to entry.symptoms.map { it.name },
            "behaviorChanges" to entry.behaviorChanges.map { it.name },
            "appetite" to entry.appetite.name,
            "energyLevel" to entry.energyLevel.name,
            "stoolQuality" to entry.stoolQuality?.name,
            "vomiting" to entry.vomiting,
            "temperature" to entry.temperature,
            "weight" to entry.weight,
            "possibleTriggers" to entry.possibleTriggers,
            "veterinaryVisit" to entry.veterinaryVisit,
            "treatment" to entry.treatment,
            "notes" to entry.notes,
            "attachedImageIds" to entry.attachedImageIds
        )
        
        val document = appwriteService.databases.createDocument(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = HEALTH_ENTRIES_COLLECTION_ID,
            documentId = ID.unique(),
            data = data
        )
        
        documentToHealthEntry(document)
    }
    
    suspend fun getHealthEntries(
        dogId: String,
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null
    ): Result<List<DogHealthEntry>> = safeApiCall {
        val queries = mutableListOf(Query.equal("dogId", dogId))
        
        startDate?.let {
            queries.add(Query.greaterThanEqual("entryDate", it.toString()))
        }
        
        endDate?.let {
            queries.add(Query.lessThanEqual("entryDate", it.toString()))
        }
        
        val response = appwriteService.databases.listDocuments(
            databaseId = AppwriteService.DATABASE_ID,
            collectionId = HEALTH_ENTRIES_COLLECTION_ID,
            queries = queries
        )
        
        response.documents.map { documentToHealthEntry(it) }
    }
    
    suspend fun correlateSymptomWithFood(
        dogId: String,
        symptom: HealthSymptom,
        daysToCheck: Int = 3
    ): Result<SymptomFoodCorrelation> = safeApiCall {
        val endDate = LocalDateTime.now()
        val startDate = endDate.minusDays(daysToCheck.toLong())
        
        val healthEntries = getHealthEntries(dogId, startDate, endDate).getOrNull() ?: emptyList()
        val symptomEntries = healthEntries.filter { it.symptoms.contains(symptom) }
        
        // Get food intake data for the same period
        val foodIntakeRepository = FoodIntakeRepository(context)
        val foodIntakes = mutableListOf<FoodIntake>()
        
        // Get food intakes for each day in the range
        var currentDate = startDate.toLocalDate()
        while (!currentDate.isAfter(endDate.toLocalDate())) {
            val dailyIntakes = foodIntakeRepository.getFoodIntakesForDog(dogId, currentDate).first()
            foodIntakes.addAll(dailyIntakes)
            currentDate = currentDate.plusDays(1)
        }
        
        // Analyze correlation
        val foodFrequency = mutableMapOf<String, Int>()
        symptomEntries.forEach { entry ->
            // Look for food consumed within 24 hours before symptom
            val relevantFoods = foodIntakes.filter { food ->
                food.timestamp.isAfter(entry.entryDate.minusHours(24)) &&
                food.timestamp.isBefore(entry.entryDate)
            }
            
            relevantFoods.forEach { food ->
                foodFrequency[food.foodName] = foodFrequency.getOrDefault(food.foodName, 0) + 1
            }
        }
        
        SymptomFoodCorrelation(
            symptom = symptom,
            occurences = symptomEntries.size,
            suspectedFoods = foodFrequency.entries
                .sortedByDescending { it.value }
                .take(5)
                .map { SuspectedFood(it.key, it.value, (it.value.toFloat() / symptomEntries.size) * 100) }
        )
    }
    
    // Helper functions
    private fun documentToAllergy(document: Document<Map<String, Any>>): DogAllergy {
        return DogAllergy(
            id = document.id,
            dogId = document.data["dogId"] as String,
            allergen = document.data["allergen"] as String,
            allergyType = AllergyType.valueOf(document.data["allergyType"] as String),
            severity = DogAllergySeverity.valueOf(document.data["severity"] as String),
            symptoms = (document.data["symptoms"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            diagnosedDate = (document.data["diagnosedDate"] as? String)?.let { LocalDate.parse(it) },
            diagnosedBy = document.data["diagnosedBy"] as? String,
            notes = document.data["notes"] as? String,
            isActive = document.data["isActive"] as Boolean
        )
    }
    
    private fun documentToMedication(document: Document<Map<String, Any>>): DogMedication {
        return DogMedication(
            id = document.id,
            dogId = document.data["dogId"] as String,
            medicationName = document.data["medicationName"] as String,
            medicationType = MedicationType.valueOf(document.data["medicationType"] as String),
            dosage = document.data["dosage"] as String,
            frequency = MedicationFrequency.valueOf(document.data["frequency"] as String),
            startDate = LocalDate.parse(document.data["startDate"] as String),
            endDate = (document.data["endDate"] as? String)?.let { LocalDate.parse(it) },
            reminderTimes = (document.data["reminderTimes"] as? List<*>)
                ?.filterIsInstance<String>()
                ?.map { LocalTime.parse(it) } ?: emptyList(),
            foodInteraction = FoodInteraction.valueOf(document.data["foodInteraction"] as String),
            purpose = document.data["purpose"] as String,
            veterinarianName = document.data["veterinarianName"] as? String,
            notes = document.data["notes"] as? String,
            isActive = document.data["isActive"] as Boolean
        )
    }
    
    private fun documentToHealthEntry(document: Document<Map<String, Any>>): DogHealthEntry {
        return DogHealthEntry(
            id = document.id,
            dogId = document.data["dogId"] as String,
            entryDate = LocalDateTime.parse(document.data["entryDate"] as String),
            entryType = HealthEntryType.valueOf(document.data["entryType"] as String),
            symptoms = (document.data["symptoms"] as? List<*>)
                ?.filterIsInstance<String>()
                ?.mapNotNull { name -> HealthSymptom.values().find { it.name == name } } ?: emptyList(),
            behaviorChanges = (document.data["behaviorChanges"] as? List<*>)
                ?.filterIsInstance<String>()
                ?.mapNotNull { name -> BehaviorChange.values().find { it.name == name } } ?: emptyList(),
            appetite = AppetiteLevel.valueOf(document.data["appetite"] as String),
            energyLevel = EnergyLevel.valueOf(document.data["energyLevel"] as String),
            stoolQuality = (document.data["stoolQuality"] as? String)?.let { StoolQuality.valueOf(it) },
            vomiting = document.data["vomiting"] as Boolean,
            temperature = document.data["temperature"] as? Double,
            weight = document.data["weight"] as? Double,
            possibleTriggers = (document.data["possibleTriggers"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            veterinaryVisit = document.data["veterinaryVisit"] as Boolean,
            treatment = document.data["treatment"] as? String,
            notes = document.data["notes"] as String,
            attachedImageIds = (document.data["attachedImageIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        )
    }
}

// Helper data classes
data class MedicationReminder(
    val medication: DogMedication,
    val scheduledTime: LocalDateTime,
    val foodRequirement: FoodInteraction
)

data class SymptomFoodCorrelation(
    val symptom: HealthSymptom,
    val occurences: Int,
    val suspectedFoods: List<SuspectedFood>
)

data class SuspectedFood(
    val foodName: String,
    val occurences: Int,
    val correlationPercentage: Float
)