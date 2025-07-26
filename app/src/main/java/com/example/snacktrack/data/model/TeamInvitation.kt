package com.example.snacktrack.data.model

import java.time.LocalDateTime

/**
 * Repräsentiert eine ausstehende Team-Einladung, die vom Benutzer bestätigt werden muss
 */
data class TeamInvitation(
    val id: String = "",
    val teamId: String,
    val membershipId: String = "",
    val teamName: String,
    val invitedBy: String = "",
    val invitedByName: String = "",
    val invitedUser: String = "",
    val role: TeamRole = TeamRole.VIEWER,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val status: InvitationStatus = InvitationStatus.PENDING,
    // Das Secret wird von der Appwrite API bereitgestellt und ist für die Bestätigung erforderlich
    val secret: String = ""
)

enum class InvitationStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    EXPIRED
}