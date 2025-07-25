package com.example.snacktrack.data.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Export and Integration models
 */

// Data Export

data class ExportRequest(
    val id: String = "",
    val userId: String = "",
    val dogIds: List<String> = emptyList(), // Empty = all dogs
    val exportType: ExportType = ExportType.FULL_DATA,
    val format: ExportFormat = ExportFormat.JSON,
    val dateRange: DateRange? = null,
    val includePhotos: Boolean = false,
    val includeAnalytics: Boolean = true,
    val encryptionEnabled: Boolean = false,
    val requestedAt: LocalDateTime = LocalDateTime.now(),
    val status: ExportStatus = ExportStatus.PENDING,
    val completedAt: LocalDateTime? = null,
    val downloadUrl: String? = null,
    val expiresAt: LocalDateTime? = null,
    val fileSize: Long? = null
)

enum class ExportType {
    FULL_DATA,
    HEALTH_RECORDS,
    NUTRITION_DATA,
    WEIGHT_HISTORY,
    MEDICATION_HISTORY,
    EXPENSE_RECORDS,
    ACTIVITY_LOGS,
    CUSTOM_SELECTION
}

enum class ExportFormat {
    JSON,
    CSV,
    PDF,
    EXCEL,
    XML,
    VETERINARY_STANDARD // HL7/FHIR format
}

enum class ExportStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    EXPIRED
}

data class ExportOptions(
    val dataCategories: List<DataCategory> = DataCategory.values().toList(),
    val customFields: List<String> = emptyList(),
    val groupBy: GroupingOption? = null,
    val sortBy: SortingOption = SortingOption.DATE,
    val includeMetadata: Boolean = true,
    val locale: String = "de_DE",
    val timezone: String = "Europe/Berlin"
)

enum class DataCategory {
    BASIC_INFO,
    HEALTH,
    NUTRITION,
    WEIGHT,
    MEDICATIONS,
    VACCINATIONS,
    EXPENSES,
    ACTIVITIES,
    BEHAVIORS,
    DOCUMENTS,
    PHOTOS
}

enum class GroupingOption {
    BY_DOG,
    BY_DATE,
    BY_CATEGORY,
    BY_MONTH,
    BY_YEAR
}

enum class SortingOption {
    DATE,
    CATEGORY,
    DOG_NAME,
    ALPHABETICAL
}

// Veterinary Integration

data class VeterinaryIntegration(
    val id: String = "",
    val clinicName: String = "",
    val systemType: VetSystemType = VetSystemType.GENERIC,
    val apiEndpoint: String = "",
    val apiKey: String = "",
    val isActive: Boolean = true,
    val lastSync: LocalDateTime? = null,
    val syncFrequency: SyncFrequency = SyncFrequency.MANUAL,
    val dataMapping: DataMappingConfig = DataMappingConfig(),
    val permissions: VetIntegrationPermissions = VetIntegrationPermissions()
)

enum class VetSystemType {
    GENERIC,
    IDEXX,
    EZYVET,
    VETSPIRE,
    AVIMARK,
    IMPROMED,
    CORNERSTONE,
    CUSTOM_API
}

enum class SyncFrequency {
    MANUAL,
    DAILY,
    WEEKLY,
    AFTER_VISIT,
    REAL_TIME
}

data class DataMappingConfig(
    val dogIdMapping: String = "patient_id",
    val weightMapping: String = "weight_kg",
    val vaccineMapping: String = "vaccination_record",
    val medicationMapping: String = "prescription",
    val customMappings: Map<String, String> = emptyMap()
)

data class VetIntegrationPermissions(
    val canReadRecords: Boolean = true,
    val canWriteRecords: Boolean = false,
    val canSyncVaccinations: Boolean = true,
    val canSyncMedications: Boolean = true,
    val canSyncLabResults: Boolean = true,
    val canSyncAppointments: Boolean = true
)

data class VetRecord(
    val id: String = "",
    val dogId: String = "",
    val clinicId: String = "",
    val visitDate: LocalDate = LocalDate.now(),
    val veterinarian: String = "",
    val visitType: VetVisitType = VetVisitType.ROUTINE,
    val diagnosis: List<String> = emptyList(),
    val treatments: List<VetTreatment> = emptyList(),
    val labResults: List<LabResult> = emptyList(),
    val notes: String = "",
    val followUpDate: LocalDate? = null,
    val documents: List<VetDocument> = emptyList()
)

enum class VetVisitType {
    ROUTINE,
    EMERGENCY,
    SURGERY,
    VACCINATION,
    DENTAL,
    SPECIALIST,
    FOLLOW_UP
}

data class VetTreatment(
    val name: String = "",
    val type: TreatmentType = TreatmentType.MEDICATION,
    val dosage: String = "",
    val frequency: String = "",
    val duration: String = "",
    val instructions: String = ""
)

enum class TreatmentType {
    MEDICATION,
    PROCEDURE,
    THERAPY,
    DIETARY,
    BEHAVIORAL
}

data class LabResult(
    val testName: String = "",
    val value: String = "",
    val unit: String = "",
    val referenceRange: String = "",
    val isAbnormal: Boolean = false,
    val notes: String = ""
)

data class VetDocument(
    val id: String = "",
    val type: VetDocumentType = VetDocumentType.REPORT,
    val name: String = "",
    val mimeType: String = "",
    val url: String = "",
    val uploadedAt: LocalDateTime = LocalDateTime.now()
)

enum class VetDocumentType {
    REPORT,
    LAB_RESULT,
    XRAY,
    PRESCRIPTION,
    INVOICE,
    OTHER
}

// Calendar Integration

data class CalendarIntegration(
    val id: String = "",
    val userId: String = "",
    val provider: CalendarProvider = CalendarProvider.GOOGLE,
    val accountEmail: String = "",
    val calendarId: String = "",
    val isActive: Boolean = true,
    val syncSettings: CalendarSyncSettings = CalendarSyncSettings(),
    val lastSync: LocalDateTime? = null
)

enum class CalendarProvider {
    GOOGLE,
    APPLE,
    OUTLOOK,
    CALDAV,
    LOCAL
}

data class CalendarSyncSettings(
    val syncAppointments: Boolean = true,
    val syncMedications: Boolean = true,
    val syncFeedings: Boolean = false,
    val syncActivities: Boolean = false,
    val reminderMinutesBefore: Int = 30,
    val createRecurringEvents: Boolean = true,
    val eventColor: String? = null
)

data class CalendarEvent(
    val id: String = "",
    val dogId: String = "",
    val title: String = "",
    val description: String = "",
    val eventType: CalendarEventType = CalendarEventType.APPOINTMENT,
    val startTime: LocalDateTime = LocalDateTime.now(),
    val endTime: LocalDateTime? = null,
    val location: String? = null,
    val reminders: List<Int> = listOf(30), // minutes before
    val recurrenceRule: String? = null, // RFC 5545 RRULE
    val calendarId: String? = null,
    val externalId: String? = null // ID in external calendar
)

enum class CalendarEventType {
    APPOINTMENT,
    MEDICATION,
    FEEDING,
    ACTIVITY,
    GROOMING,
    TRAINING,
    OTHER
}

// Fitness Tracker Integration

data class FitnessTrackerIntegration(
    val id: String = "",
    val userId: String = "",
    val dogId: String = "",
    val deviceType: FitnessDeviceType = FitnessDeviceType.GENERIC,
    val deviceId: String = "",
    val isActive: Boolean = true,
    val syncSettings: FitnessTrackerSyncSettings = FitnessTrackerSyncSettings(),
    val lastSync: LocalDateTime? = null,
    val batteryLevel: Int? = null
)

enum class FitnessDeviceType {
    GENERIC,
    FITBARK,
    WHISTLE,
    LINK_AKC,
    PETPACE,
    GARMIN,
    APPLE_AIRTAG,
    CUSTOM
}

data class FitnessTrackerSyncSettings(
    val syncActivity: Boolean = true,
    val syncSleep: Boolean = true,
    val syncLocation: Boolean = false,
    val syncVitals: Boolean = false,
    val syncFrequency: SyncFrequency = SyncFrequency.DAILY,
    val activityGoals: ActivityGoals = ActivityGoals()
)

data class ActivityGoals(
    val dailySteps: Int? = null,
    val dailyActiveMinutes: Int? = null,
    val dailyCalories: Int? = null,
    val weeklyExerciseSessions: Int? = null
)

data class FitnessData(
    val id: String = "",
    val dogId: String = "",
    val deviceId: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val activityData: ActivityData? = null,
    val sleepData: SleepData? = null,
    val vitalData: VitalData? = null,
    val locationData: LocationData? = null
)

data class ActivityData(
    val steps: Int = 0,
    val distance: Double = 0.0, // meters
    val activeMinutes: Int = 0,
    val calories: Double = 0.0,
    val activityLevel: Int = 0, // 0-10 scale
    val playTime: Int = 0, // minutes
    val restTime: Int = 0 // minutes
)

data class SleepData(
    val totalSleep: Int = 0, // minutes
    val deepSleep: Int = 0,
    val lightSleep: Int = 0,
    val awakeTime: Int = 0,
    val sleepQuality: Int = 0, // 0-100
    val interruptions: Int = 0
)

data class VitalData(
    val heartRate: Int? = null,
    val respiratoryRate: Int? = null,
    val temperature: Double? = null,
    val hydration: Int? = null // 0-100 scale
)

data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double? = null,
    val accuracy: Double = 0.0,
    val speed: Double? = null,
    val heading: Double? = null,
    val isIndoor: Boolean = false
)

// Cloud Backup

data class CloudBackup(
    val id: String = "",
    val userId: String = "",
    val provider: CloudProvider = CloudProvider.APPWRITE,
    val backupSettings: BackupSettings = BackupSettings(),
    val lastBackup: LocalDateTime? = null,
    val nextScheduledBackup: LocalDateTime? = null,
    val storageUsed: Long = 0, // bytes
    val storageLimit: Long = 5368709120, // 5GB default
    val backupHistory: List<BackupRecord> = emptyList()
)

enum class CloudProvider {
    APPWRITE,
    GOOGLE_DRIVE,
    DROPBOX,
    ICLOUD,
    ONEDRIVE,
    CUSTOM_S3
}

data class BackupSettings(
    val autoBackup: Boolean = true,
    val backupFrequency: BackupFrequency = BackupFrequency.DAILY,
    val backupTime: String = "02:00", // HH:mm
    val wifiOnly: Boolean = true,
    val includePhotos: Boolean = true,
    val includeDocuments: Boolean = true,
    val encryption: Boolean = true,
    val retentionDays: Int = 30,
    val incrementalBackup: Boolean = true
)

enum class BackupFrequency {
    MANUAL,
    DAILY,
    WEEKLY,
    MONTHLY
}

data class BackupRecord(
    val id: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val size: Long = 0,
    val duration: Long = 0, // seconds
    val status: BackupStatus = BackupStatus.COMPLETED,
    val itemsBackedUp: Int = 0,
    val error: String? = null
)

enum class BackupStatus {
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    PARTIAL
}

data class RestoreRequest(
    val id: String = "",
    val userId: String = "",
    val backupId: String = "",
    val restoreOptions: RestoreOptions = RestoreOptions(),
    val requestedAt: LocalDateTime = LocalDateTime.now(),
    val status: RestoreStatus = RestoreStatus.PENDING,
    val completedAt: LocalDateTime? = null,
    val itemsRestored: Int = 0,
    val error: String? = null
)

data class RestoreOptions(
    val overwriteExisting: Boolean = false,
    val restorePhotos: Boolean = true,
    val restoreDocuments: Boolean = true,
    val selectedDogs: List<String> = emptyList(), // Empty = all
    val selectedCategories: List<DataCategory> = emptyList() // Empty = all
)

enum class RestoreStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED
}

// API Integration

data class ApiIntegration(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val apiKey: String = "",
    val apiSecret: String? = null,
    val webhookUrl: String? = null,
    val permissions: ApiPermissions = ApiPermissions(),
    val rateLimits: RateLimits = RateLimits(),
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastUsed: LocalDateTime? = null,
    val usageStats: ApiUsageStats = ApiUsageStats()
)

data class ApiPermissions(
    val readDogs: Boolean = true,
    val writeDogs: Boolean = false,
    val readHealth: Boolean = true,
    val writeHealth: Boolean = false,
    val readNutrition: Boolean = true,
    val writeNutrition: Boolean = false,
    val readActivities: Boolean = true,
    val writeActivities: Boolean = false,
    val readAnalytics: Boolean = true,
    val webhookEvents: List<WebhookEvent> = emptyList()
)

enum class WebhookEvent {
    DOG_CREATED,
    DOG_UPDATED,
    WEIGHT_RECORDED,
    FEEDING_RECORDED,
    HEALTH_ISSUE_DETECTED,
    MEDICATION_DUE,
    VACCINATION_DUE,
    ANALYTICS_READY
}

data class RateLimits(
    val requestsPerMinute: Int = 60,
    val requestsPerHour: Int = 1000,
    val requestsPerDay: Int = 10000,
    val burstLimit: Int = 10
)

data class ApiUsageStats(
    val totalRequests: Long = 0,
    val successfulRequests: Long = 0,
    val failedRequests: Long = 0,
    val lastRequestAt: LocalDateTime? = null,
    val averageResponseTime: Long = 0, // milliseconds
    val endpointUsage: Map<String, Long> = emptyMap()
)

// Import functionality

data class ImportRequest(
    val id: String = "",
    val userId: String = "",
    val source: ImportSource = ImportSource.CSV,
    val fileUrl: String = "",
    val mappingConfig: ImportMappingConfig = ImportMappingConfig(),
    val validationRules: ValidationRules = ValidationRules(),
    val requestedAt: LocalDateTime = LocalDateTime.now(),
    val status: ImportStatus = ImportStatus.PENDING,
    val progress: ImportProgress = ImportProgress(),
    val results: ImportResults? = null
)

enum class ImportSource {
    CSV,
    EXCEL,
    JSON,
    XML,
    OTHER_APP,
    VETERINARY_SYSTEM
}

data class ImportMappingConfig(
    val columnMappings: Map<String, String> = emptyMap(),
    val dateFormat: String = "yyyy-MM-dd",
    val decimalSeparator: String = ".",
    val encoding: String = "UTF-8",
    val skipRows: Int = 0,
    val hasHeader: Boolean = true
)

data class ValidationRules(
    val requireDogName: Boolean = true,
    val requireBirthDate: Boolean = false,
    val validateWeightRange: Boolean = true,
    val minWeight: Double = 0.1,
    val maxWeight: Double = 200.0,
    val allowFutureDates: Boolean = false,
    val allowDuplicates: Boolean = false
)

enum class ImportStatus {
    PENDING,
    VALIDATING,
    PROCESSING,
    COMPLETED,
    FAILED,
    PARTIAL_SUCCESS
}

data class ImportProgress(
    val totalRows: Int = 0,
    val processedRows: Int = 0,
    val successfulRows: Int = 0,
    val failedRows: Int = 0,
    val currentStep: String = "",
    val percentComplete: Int = 0
)

data class ImportResults(
    val dogsImported: Int = 0,
    val recordsImported: Int = 0,
    val duplicatesSkipped: Int = 0,
    val errors: List<ImportError> = emptyList(),
    val warnings: List<ImportWarning> = emptyList(),
    val summary: Map<String, Int> = emptyMap()
)

data class ImportError(
    val row: Int = 0,
    val column: String = "",
    val value: String = "",
    val error: String = ""
)

data class ImportWarning(
    val row: Int = 0,
    val column: String = "",
    val warning: String = ""
)

// Data Synchronization

data class SyncConfiguration(
    val id: String = "",
    val userId: String = "",
    val syncEnabled: Boolean = true,
    val syncInterval: ExportSyncInterval = ExportSyncInterval.ON_CHANGE,
    val syncOnWifiOnly: Boolean = true,
    val conflictResolution: ConflictResolution = ConflictResolution.LATEST_WINS,
    val lastSync: LocalDateTime? = null,
    val syncHistory: List<SyncRecord> = emptyList()
)

enum class ExportSyncInterval {
    ON_CHANGE,
    EVERY_HOUR,
    EVERY_DAY,
    MANUAL
}

enum class ConflictResolution {
    LATEST_WINS,
    SERVER_WINS,
    CLIENT_WINS,
    MANUAL
}

data class SyncRecord(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val direction: SyncDirection = SyncDirection.BIDIRECTIONAL,
    val itemsSynced: Int = 0,
    val conflicts: Int = 0,
    val duration: Long = 0, // milliseconds
    val status: ExportSyncStatus = ExportSyncStatus.SUCCESS
)

enum class SyncDirection {
    UPLOAD,
    DOWNLOAD,
    BIDIRECTIONAL
}

enum class ExportSyncStatus {
    SUCCESS,
    PARTIAL,
    FAILED,
    CANCELLED
}