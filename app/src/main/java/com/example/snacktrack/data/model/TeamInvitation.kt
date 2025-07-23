package com.example.snacktrack.data.model

/**
 * Repräsentiert eine ausstehende Team-Einladung, die vom Benutzer bestätigt werden muss
 */
data class TeamInvitation(
    val teamId: String,
    val membershipId: String,
    val teamName: String,
    // Das Secret wird von der Appwrite API bereitgestellt und ist für die Bestätigung erforderlich
    val secret: String = ""
)