package com.example.snacktrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snacktrack.data.model.*
import com.example.snacktrack.data.repository.StatisticsRepository
import com.example.snacktrack.data.service.AppwriteService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class AdvancedStatisticsUiState(
    val isLoading: Boolean = false,
    val statistics: AdvancedStatistics? = null,
    val customReports: List<CustomReport> = emptyList(),
    val selectedReport: CustomReport? = null,
    val exportProgress: Float? = null,
    val error: String? = null
)

class AdvancedStatisticsViewModel(
    private val context: android.content.Context,
    private val appwriteService: AppwriteService
) : ViewModel() {
    private val statisticsRepository = StatisticsRepository(context, appwriteService)
    
    private val _uiState = MutableStateFlow(AdvancedStatisticsUiState())
    val uiState: StateFlow<AdvancedStatisticsUiState> = _uiState.asStateFlow()
    
    fun loadStatistics(dogId: String, period: AnalyticsPeriod = AnalyticsPeriod.MONTH) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = statisticsRepository.generateAdvancedStatistics(dogId, period)
                
                result.getOrNull()?.let { stats ->
                    _uiState.update {
                        it.copy(
                            statistics = stats,
                            isLoading = false,
                            error = null
                        )
                    }
                } ?: run {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun loadCustomReports(userId: String) {
        viewModelScope.launch {
            try {
                val result = statisticsRepository.getCustomReports(userId)
                
                result.getOrNull()?.let { reports ->
                    _uiState.update {
                        it.copy(customReports = reports)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun createCustomReport(report: CustomReport) {
        viewModelScope.launch {
            try {
                val result = statisticsRepository.createCustomReport(report)
                
                result.getOrNull()?.let { createdReport ->
                    _uiState.update {
                        it.copy(
                            customReports = it.customReports + createdReport,
                            selectedReport = createdReport
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun generateReport(reportId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(exportProgress = 0f) }
            
            try {
                val result = statisticsRepository.generateReport(reportId)
                
                result.getOrNull()?.let { reportData ->
                    // Save report to device
                    saveReportToDevice(reportData, reportId)
                    
                    _uiState.update {
                        it.copy(exportProgress = null)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        exportProgress = null,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun exportStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(exportProgress = 0f) }
            
            try {
                val statistics = _uiState.value.statistics ?: return@launch
                
                // Create comprehensive report
                val report = createComprehensiveReport(statistics)
                
                // Generate PDF
                val pdfData = generatePdfFromStatistics(statistics)
                
                // Save to device
                saveReportToDevice(pdfData, "statistics_${System.currentTimeMillis()}")
                
                _uiState.update {
                    it.copy(exportProgress = null)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        exportProgress = null,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun shareStatistics() {
        viewModelScope.launch {
            try {
                val statistics = _uiState.value.statistics ?: return@launch
                
                // Create shareable summary
                val summary = createStatisticsSummary(statistics)
                
                // Trigger share intent (platform-specific implementation needed)
                shareContent(summary)
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun compareToGoals(dogId: String) {
        viewModelScope.launch {
            try {
                // This would fetch goals and compare progress
                // For now, just update the comparative analysis
                val statistics = _uiState.value.statistics ?: return@launch
                
                // Update comparative analysis with goal comparison
                _uiState.update {
                    it.copy(
                        statistics = statistics.copy(
                            comparativeAnalysis = statistics.comparativeAnalysis.copy(
                                goalComparison = GoalComparison(
                                    goals = mapOf(
                                        "Gewichtsziel" to GoalProgress(
                                            goalValue = statistics.weightAnalytics.idealWeight,
                                            currentValue = statistics.weightAnalytics.currentWeight,
                                            progressPercent = calculateGoalProgress(
                                                statistics.weightAnalytics.currentWeight,
                                                statistics.weightAnalytics.idealWeight
                                            ),
                                            trend = statistics.weightAnalytics.weightTrend,
                                            onTrack = statistics.weightAnalytics.daysToIdealWeight?.let { it < 90 } ?: false
                                        )
                                    ),
                                    overallProgress = 75.0
                                )
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun updateReportSchedule(reportId: String, schedule: ReportSchedule) {
        viewModelScope.launch {
            try {
                // Update report schedule
                val reports = _uiState.value.customReports.map { report ->
                    if (report.id == reportId) {
                        report.copy(schedule = schedule)
                    } else {
                        report
                    }
                }
                
                _uiState.update {
                    it.copy(customReports = reports)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun deleteCustomReport(reportId: String) {
        viewModelScope.launch {
            try {
                // Delete from backend
                // For now, just remove from local state
                _uiState.update {
                    it.copy(
                        customReports = it.customReports.filter { report -> report.id != reportId },
                        selectedReport = if (it.selectedReport?.id == reportId) null else it.selectedReport
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    // Helper functions
    
    private fun createComprehensiveReport(statistics: AdvancedStatistics): String {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val now = LocalDateTime.now().format(formatter)
        
        return buildString {
            appendLine("SnackTrack Statistikbericht")
            appendLine("Erstellt am: $now")
            appendLine("Zeitraum: ${statistics.period}")
            appendLine()
            
            // Weight Section
            appendLine("GEWICHTSANALYSE")
            appendLine("================")
            appendLine("Aktuelles Gewicht: ${statistics.weightAnalytics.currentWeight} kg")
            appendLine("Idealgewicht: ${statistics.weightAnalytics.idealWeight} kg")
            appendLine("Trend: ${statistics.weightAnalytics.weightTrend}")
            appendLine("Änderung: ${statistics.weightAnalytics.weightChangePercent}%")
            appendLine()
            
            // Nutrition Section
            appendLine("ERNÄHRUNGSANALYSE")
            appendLine("==================")
            appendLine("Durchschnittliche Kalorien/Tag: ${statistics.nutritionAnalytics.averageDailyCalories}")
            appendLine("Empfohlene Kalorien/Tag: ${statistics.nutritionAnalytics.recommendedDailyCalories}")
            appendLine("Ernährungsvollständigkeit: ${statistics.nutritionAnalytics.nutritionalCompleteness}%")
            appendLine()
            
            // Health Section
            appendLine("GESUNDHEITSANALYSE")
            appendLine("==================")
            appendLine("Gesundheitsscore: ${statistics.healthAnalytics.healthScore}%")
            appendLine("Tierarztbesuche/Jahr: ${statistics.healthAnalytics.vetVisitFrequency}")
            appendLine("Medikamenten-Compliance: ${statistics.healthAnalytics.medicationAdherence}%")
            appendLine()
            
            // Activity Section
            appendLine("AKTIVITÄTSANALYSE")
            appendLine("=================")
            appendLine("Tägliche Aktivität: ${statistics.activityAnalytics.dailyActivityMinutes} Minuten")
            appendLine("Aktivitätslevel: ${statistics.activityAnalytics.activityLevel}")
            appendLine("Spaziergänge/Woche: ${statistics.activityAnalytics.walkFrequency}")
            appendLine()
            
            // Cost Section
            appendLine("KOSTENANALYSE")
            appendLine("=============")
            appendLine("Monatliche Kosten: €${statistics.costAnalytics.totalMonthlySpend}")
            appendLine("Kosten/Tag: €${statistics.costAnalytics.averageDailyCost}")
            appendLine("Jahresprognose: €${statistics.costAnalytics.projectedAnnualCost}")
            appendLine()
            
            // Predictions
            appendLine("VORHERSAGEN")
            appendLine("===========")
            appendLine("Gewicht in 30 Tagen: ${statistics.predictiveInsights.weightPrediction.predictedWeight30Days} kg")
            appendLine("Gewicht in 90 Tagen: ${statistics.predictiveInsights.weightPrediction.predictedWeight90Days} kg")
            statistics.predictiveInsights.recommendedInterventions.forEach { intervention ->
                appendLine("- ${intervention.title}: ${intervention.description}")
            }
        }
    }
    
    private fun createStatisticsSummary(statistics: AdvancedStatistics): String {
        return buildString {
            appendLine("📊 SnackTrack Statistik-Zusammenfassung")
            appendLine()
            appendLine("🏥 Gesundheit: ${statistics.healthAnalytics.healthScore.toInt()}%")
            appendLine("⚖️ Gewicht: ${statistics.weightAnalytics.currentWeight} kg (${getTrendEmoji(statistics.weightAnalytics.weightTrend)})")
            appendLine("🍖 Ernährung: ${statistics.nutritionAnalytics.nutritionalCompleteness.toInt()}% vollständig")
            appendLine("🏃 Aktivität: ${statistics.activityAnalytics.dailyActivityMinutes.toInt()} Min/Tag")
            appendLine("💰 Kosten: €${statistics.costAnalytics.totalMonthlySpend}/Monat")
            appendLine()
            appendLine("Top-Empfehlungen:")
            statistics.predictiveInsights.recommendedInterventions.take(3).forEach { intervention ->
                appendLine("• ${intervention.title}")
            }
        }
    }
    
    private fun getTrendEmoji(trend: StatisticsTrendDirection): String = when (trend) {
        StatisticsTrendDirection.INCREASING -> "📈"
        StatisticsTrendDirection.DECREASING -> "📉"
        StatisticsTrendDirection.STABLE -> "➡️"
        StatisticsTrendDirection.VOLATILE -> "📊"
    }
    
    private fun calculateGoalProgress(current: Double, goal: Double): Double {
        return if (goal > 0) {
            ((goal - current) / goal * 100).coerceIn(0.0, 100.0)
        } else {
            0.0
        }
    }
    
    private suspend fun generatePdfFromStatistics(statistics: AdvancedStatistics): ByteArray {
        // In a real implementation, this would use a PDF library
        // For now, return empty byte array
        return ByteArray(0)
    }
    
    private suspend fun saveReportToDevice(data: ByteArray, filename: String) {
        // Platform-specific implementation to save file
        // This would use Android's MediaStore or iOS's file system
    }
    
    private fun shareContent(content: String) {
        // Platform-specific implementation to share content
        // This would use Android's Intent.ACTION_SEND or iOS's share sheet
    }
}

class AdvancedStatisticsViewModelFactory(
    private val context: android.content.Context,
    private val appwriteService: AppwriteService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdvancedStatisticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdvancedStatisticsViewModel(context, appwriteService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}