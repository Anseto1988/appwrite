package com.example.snacktrack.data.model

/**
 * Repräsentiert ein Team im SnackTrack-System für das Teilen von Hunden
 */
data class Team(
    val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val members: List<TeamMember> = emptyList()
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
