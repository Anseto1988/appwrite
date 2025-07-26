package com.example.snacktrack.data.model

import java.time.LocalDateTime

/**
 * Repräsentiert ein Team im SnackTrack-System für das Teilen von Hunden
 */
data class Team(
    val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val members: List<TeamMember> = emptyList(),
    val sharedDogs: List<String>? = null,
    val description: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Repräsentiert ein Teammitglied
 */
data class TeamMember(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val role: BasicTeamRole = BasicTeamRole.VIEWER
)

/**
 * Definiert die verfügbaren Rollen für Teammitglieder
 */
enum class BasicTeamRole(val displayName: String) {
    OWNER("Besitzer"),
    EDITOR("Bearbeiter"),
    VIEWER("Betrachter")
}

// Alias for TeamRole to maintain compatibility
typealias TeamRole = BasicTeamRole

// TeamInvitation is defined in TeamInvitation.kt
