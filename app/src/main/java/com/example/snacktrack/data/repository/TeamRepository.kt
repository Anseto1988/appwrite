package com.example.snacktrack.data.repository

// Android Imports
import android.content.Context
import android.util.Log

// App Models
import com.example.snacktrack.data.model.Team
import com.example.snacktrack.data.model.TeamInvitation
import com.example.snacktrack.data.model.TeamMember
import com.example.snacktrack.data.model.BasicTeamRole
import com.example.snacktrack.data.service.AppwriteService

// Appwrite SDK Imports
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account
import io.appwrite.services.Databases

// Kotlin Coroutines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

// Java/Kotlin Standard Imports
import java.util.UUID

/**
 * TeamRepository - Neu implementiert mit Collections-basiertem Ansatz
 * 
 * Diese Implementierung nutzt ausschließlich Appwrite Collections anstelle der Teams-API.
 * Sie benötigt die folgenden Collections in Appwrite:
 * 
 * 1. teams: Speichert Team-Informationen
 *    - id: String (Document ID)
 *    - name: String (Teamname)
 *    - ownerId: String (Benutzer-ID des Team-Erstellers)
 *    - createdAt: DateTime
 * 
 * 2. team_members: Speichert Teammitgliedschaften
 *    - id: String (Document ID)
 *    - teamId: String (ID des Teams)
 *    - userId: String (ID des Benutzers)
 *    - role: String ("owner", "admin", "member")
 *    - joinedAt: DateTime
 * 
 * 3. team_invitations: Speichert ausstehende Einladungen
 *    - id: String (Document ID)
 *    - teamId: String (ID des Teams)
 *    - teamName: String (Name des Teams zur Anzeige)
 *    - invitedByUserId: String (ID des einladenden Benutzers)
 *    - invitedUserId: String (ID des eingeladenen Benutzers)
 *    - invitedEmail: String (E-Mail des eingeladenen Benutzers)
 *    - role: String ("admin", "member")
 *    - status: String ("pending", "accepted", "declined")
 *    - secret: String (Einmalig generieter Token für die Annahme)
 *    - createdAt: DateTime
 * 
 * 4. dog_sharing: Speichert die Freigaben von Hunden für Teams
 *    - id: String (Document ID)
 *    - dogId: String (ID des Hundes)
 *    - teamId: String (ID des Teams)
 *    - sharedByUserId: String (ID des Benutzers, der den Hund geteilt hat)
 *    - sharedAt: DateTime
 */
class TeamRepository(private val context: Context) {
    
    private val TAG = "TeamRepository"
    
    // Appwrite Services
    private val appwriteService = AppwriteService.getInstance(context)
    private val client = appwriteService.client
    private val databases = appwriteService.databases
    private val account = appwriteService.account
    private val storage = appwriteService.storage
    
    // Collection-Konstanten
    companion object {
        const val DATABASE_ID = AppwriteService.DATABASE_ID
        const val COLLECTION_TEAMS = AppwriteService.COLLECTION_TEAMS
        const val COLLECTION_TEAM_MEMBERS = AppwriteService.COLLECTION_TEAM_MEMBERS
        const val COLLECTION_TEAM_INVITATIONS = AppwriteService.COLLECTION_TEAM_INVITATIONS
        const val COLLECTION_DOG_SHARING = AppwriteService.COLLECTION_DOG_SHARING
        private const val TAG = "TeamRepository"
    }
    
    // Verfolgt den Status der Team-Liste für UI-Updates
    private val _teamsStateFlow = MutableStateFlow<List<com.example.snacktrack.data.model.Team>>(emptyList())
    val teamsStateFlow: StateFlow<List<com.example.snacktrack.data.model.Team>> = _teamsStateFlow.asStateFlow()
    
    // Verfolgt den Status der Einladungen für UI-Updates
    private val _invitationsStateFlow = MutableStateFlow<List<TeamInvitation>>(emptyList())
    val invitationsStateFlow: StateFlow<List<TeamInvitation>> = _invitationsStateFlow.asStateFlow()    /**
     * Erstellt ein neues Team
     */
    suspend fun createTeam(name: String): Result<com.example.snacktrack.data.model.Team> = withContext(Dispatchers.IO) {
        try {
            // Aktuellen Benutzer abrufen
            val currentUser = account.get()
            val userId = currentUser.id
            
            Log.d(TAG, "Erstelle Team: $name mit Owner: $userId")
            
            // Team-Daten erstellen
            val teamData = HashMap<String, Any>()
            // Ensure teamId is included - this is the required field in the collection
            teamData["teamId"] = java.util.UUID.randomUUID().toString()
            teamData["name"] = name
            teamData["ownerId"] = userId
            // Use ISO 8601 format for datetime that Appwrite expects
            teamData["createdAt"] = java.time.OffsetDateTime.now().toString()
            
            // Erstelle das Team-Dokument
            val teamDoc = databases.createDocument(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_TEAMS,
                documentId = "unique()",
                data = teamData
            )
            
            // Erstelle die Mitgliedschaft für den Ersteller (als Owner)
            val memberData = HashMap<String, Any>()
            memberData["teamId"] = teamDoc.id
            memberData["userId"] = userId
            memberData["role"] = BasicTeamRole.OWNER.name
            // Use ISO 8601 format for datetime that Appwrite expects
            memberData["joinedAt"] = java.time.OffsetDateTime.now().toString()
            
            databases.createDocument(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_TEAM_MEMBERS,
                documentId = "unique()",
                data = memberData
            )
            
            // Team-Objekt erstellen
            val ownerMember = TeamMember(
                userId = userId,
                email = currentUser.email,
                name = currentUser.name,
                role = BasicTeamRole.OWNER
            )
            
            val newTeam = com.example.snacktrack.data.model.Team(
                id = teamDoc.id,
                name = name,
                ownerId = userId,
                members = listOf(ownerMember)
            )
            
            // Teams neu laden, um UI zu aktualisieren
            loadTeams()
            
            Result.success(newTeam)
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Erstellen des Teams: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Lädt alle Teams des aktuellen Benutzers
     */
    suspend fun loadTeams(): Result<List<com.example.snacktrack.data.model.Team>> = withContext(Dispatchers.IO) {
        try {
            // Aktuellen Benutzer abrufen
            val currentUser = account.get()
            val userId = currentUser.id
            
            Log.d(TAG, "Lade Teams für Benutzer: $userId")
            
            // Liste für alle gefundenen Teams
            val userTeams = mutableListOf<com.example.snacktrack.data.model.Team>()
            
            try {
                // Finde alle Teammitgliedschaften des Benutzers
                val memberships = databases.listDocuments(
                    databaseId = DATABASE_ID,
                    collectionId = COLLECTION_TEAM_MEMBERS,
                    queries = listOf(Query.equal("userId", userId))
                )
                
                Log.d(TAG, "${memberships.documents.size} Team-Mitgliedschaften für Benutzer $userId gefunden")
                
                // Laden der Team-Details für jede Mitgliedschaft
                for (membership in memberships.documents) {
                    try {
                        val teamId = membership.data["teamId"] as? String ?: continue
                        val teamResult = getTeam(teamId)
                        
                        if (teamResult.isSuccess) {
                            userTeams.add(teamResult.getOrThrow())
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Fehler beim Laden des Teams für Mitgliedschaft: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Abfragen der Teammitgliedschaften: ${e.message}", e)
            }
            
            // StateFlow mit den gefundenen Teams aktualisieren
            _teamsStateFlow.value = userTeams
            
            Log.d(TAG, "Insgesamt ${userTeams.size} Teams für Benutzer $userId geladen")
            
            Result.success(userTeams)
        } catch (e: Exception) {
            Log.e(TAG, "Genereller Fehler beim Laden der Teams: ${e.message}", e)
            Result.failure(e)
        }
    }    /**
     * Holt ein bestimmtes Team anhand seiner ID
     */
    suspend fun getTeam(teamId: String): Result<com.example.snacktrack.data.model.Team> = withContext(Dispatchers.IO) {
        try {
            // Team-Details laden
            val teamDoc = databases.getDocument(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_TEAMS,
                documentId = teamId
            )
            
            // Teammitglieder laden
            val teamMembers = databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_TEAM_MEMBERS,
                queries = listOf(Query.equal("teamId", teamId))
            )
            
            // Benutzerdetails für alle Mitglieder laden
            val members = mutableListOf<TeamMember>()
            
            for (memberDoc in teamMembers.documents) {
                val memberId = memberDoc.data["userId"] as? String ?: continue
                val role = memberDoc.data["role"] as? String ?: BasicTeamRole.VIEWER.name
                
                try {
                    // Direktes Laden des Benutzers geht in Appwrite nur für den aktuellen Benutzer
                    // In einer realen App würden wir hier eine Collection für Benutzerprofile abfragen
                    val currentUser = account.get()
                    val email = if (currentUser.id == memberId) currentUser.email else "$memberId@example.com"
                    val name = if (currentUser.id == memberId) currentUser.name else "Benutzer $memberId"
                    
                    members.add(
                        TeamMember(
                            userId = memberId,
                            email = email,
                            name = name,
                            role = BasicTeamRole.valueOf(role)
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Fehler beim Laden des Benutzers $memberId: ${e.message}")
                }
            }
            
            // Team-Objekt erstellen
            val team = com.example.snacktrack.data.model.Team(
                id = teamId,
                name = teamDoc.data["name"] as? String ?: "Unbenanntes Team",
                ownerId = teamDoc.data["ownerId"] as? String ?: "",
                members = members
            )
            
            Result.success(team)
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Laden des Teams $teamId: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sucht nach Benutzern basierend auf einer E-Mail-Adresse
     */
    suspend fun searchUsers(email: String): Result<List<com.example.snacktrack.data.model.User>> = withContext(Dispatchers.IO) {
        try {
            // In einem produktiven System würden wir hier eine spezielle Suchanfrage machen
            // Da die Appwrite Account-API keine direkte Suche bietet, müssten wir
            // eine eigene Collection für Benutzerprofile haben und diese durchsuchen
            
            // Hinweis: Dies ist ein vereinfachter Ansatz und würde in einer realen App
            // durch eine tatsächliche Benutzersuche über eine Collection ersetzt werden
            val dummyUser = com.example.snacktrack.data.model.User(
                id = "user_id_placeholder",
                email = email,
                name = email.substringBefore('@')
            )
            
            Result.success(listOf(dummyUser))
        } catch (e: Exception) {
            Log.e(TAG, "Fehler bei der Benutzersuche: ${e.message}", e)
            Result.failure(e)
        }
    }    /**
     * Lädt alle ausstehenden Team-Einladungen für den aktuellen Benutzer
     */
    suspend fun loadPendingInvitations(): Result<List<TeamInvitation>> = withContext(Dispatchers.IO) {
        try {
            // Aktuellen Benutzer abrufen
            val currentUser = account.get()
            val userId = currentUser.id
            
            Log.d(TAG, "Suche nach Team-Einladungen für Benutzer: $userId")
            
            // Liste für alle gefundenen Einladungen
            val pendingInvitations = mutableListOf<TeamInvitation>()
            
            try {
                // Abfrage nach Einladungen für den aktuellen Benutzer, die noch nicht bestätigt wurden
                // Wir suchen nach zwei Kriterien: entweder die invitedUserId stimmt überein ODER die invitedEmail
                val invitationDocuments = databases.listDocuments(
                    databaseId = DATABASE_ID,
                    collectionId = COLLECTION_TEAM_INVITATIONS,
                    queries = listOf(
                        Query.equal("status", "pending"),
                        Query.equal("invitedEmail", currentUser.email)
                    )
                )
                
                Log.d(TAG, "${invitationDocuments.documents.size} Einladungsdokumente für Benutzer $userId gefunden")
                
                // Konvertiere jedes Dokument in ein TeamInvitation-Objekt
                for (document in invitationDocuments.documents) {
                    try {
                        val teamId = document.data["teamId"] as? String ?: continue
                        val membershipId = document.id // Die Dokument-ID als MembershipId verwenden
                        val teamName = document.data["teamName"] as? String ?: "Unbekanntes Team"
                        val secret = document.data["secret"] as? String ?: ""
                        
                        pendingInvitations.add(
                            TeamInvitation(
                                teamId = teamId,
                                membershipId = membershipId,
                                teamName = teamName,
                                secret = secret
                            )
                        )
                        
                        Log.d(TAG, "Einladung gefunden: Team=$teamName, ID=$membershipId")
                    } catch (e: Exception) {
                        Log.e(TAG, "Fehler beim Verarbeiten der Einladung ${document.id}: ${e.message}")
                    }
                }
                
                Log.d(TAG, "Insgesamt ${pendingInvitations.size} ausstehende Einladungen für Benutzer $userId gefunden")
                if (pendingInvitations.isEmpty()) {
                    Log.d(TAG, "Keine Einladungen gefunden.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Abfragen der Einladungen: ${e.message}")
            }
            
            // StateFlow mit den gefundenen Einladungen aktualisieren
            _invitationsStateFlow.value = pendingInvitations
            
            Result.success(pendingInvitations)
        } catch (e: Exception) {
            Log.e(TAG, "Genereller Fehler beim Laden der Einladungen: ${e.message}")
            Result.failure(e)
        }
    }    /**
     * Fügt einen Benutzer zu einem Team hinzu (Sendet eine Einladung)
     */
    suspend fun addTeamMember(teamId: String, email: String, role: BasicTeamRole): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Aktuellen Benutzer abrufen (Einladender)
            val currentUser = account.get()
            val inviterId = currentUser.id
            
            // Team-Details abrufen
            val teamDoc = databases.getDocument(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_TEAMS,
                documentId = teamId
            )
            
            val teamName = teamDoc.data["name"] as? String ?: "Unbenanntes Team"
            
            // In einer realen App würden wir hier eine Benutzersuche durchführen,
            // um die ID des Benutzers mit der angegebenen E-Mail zu finden
            // Da Appwrite keine direkte Benutzersuche per E-Mail bietet, müssten wir eine
            // eigene Collection für Benutzerprofile haben und diese abfragen
            
            // Für diese Demo: Entweder verwenden wir den aktuellen Benutzer, wenn die E-Mail übereinstimmt,
            // oder wir verwenden einen Platzhalter
            val invitedUserId = if (currentUser.email == email) {
                currentUser.id
            } else {
                // In einer realen App würden wir hier nach der Benutzer-ID in einer Collection suchen
                "user_id_placeholder"
            }
            
            // Unique Secret für die Einladung generieren
            val secret = UUID.randomUUID().toString()
            
            // Einladung erstellen
            val invitationData = hashMapOf(
                "teamId" to teamId,
                "teamName" to teamName,
                "invitedByUserId" to inviterId,
                "invitedUserId" to invitedUserId,
                "invitedEmail" to email,
                "role" to role.name,
                "status" to "pending",
                "secret" to secret,
                "createdAt" to java.time.OffsetDateTime.now().toString()
            )
            
            // Dokument in der Einladungs-Collection erstellen
            databases.createDocument(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_TEAM_INVITATIONS,
                documentId = "unique()",
                data = invitationData
            )
            
            Log.d(TAG, "Einladung für $email zu Team $teamName erstellt")
            
            // In einer realen App würde hier eine E-Mail mit dem Einladungslink gesendet werden
            // Der Link würde den Secret-Token enthalten, der für die Annahme der Einladung benötigt wird
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Hinzufügen des Teammitglieds: ${e.message}", e)
            Result.failure(e)
        }
    }    /**
     * Akzeptiert eine Team-Einladung
     */
    suspend fun acceptInvitation(invitation: TeamInvitation): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Aktuellen Benutzer abrufen
            val currentUser = account.get()
            val userId = currentUser.id
            
            Log.d(TAG, "Akzeptiere Einladung: TeamID=${invitation.teamId}, MembershipID=${invitation.membershipId}")
            
            if (invitation.secret.isBlank()) {
                Log.e(TAG, "Fehler: Secret darf nicht leer sein. Es muss aus der Einladungs-E-Mail extrahiert werden.")
                return@withContext Result.failure(Exception("Secret darf nicht leer sein"))
            }
            
            try {
                // Einladungsdokument abrufen
                val invitationDoc = databases.getDocument(
                    databaseId = DATABASE_ID,
                    collectionId = COLLECTION_TEAM_INVITATIONS,
                    documentId = invitation.membershipId
                )
                
                // Prüfen, ob das Secret übereinstimmt
                val storedSecret = invitationDoc.data["secret"] as? String ?: ""
                if (storedSecret != invitation.secret) {
                    Log.e(TAG, "Fehler: Das Secret stimmt nicht überein")
                    return@withContext Result.failure(Exception("Ungültiges Secret"))
                }
                
                // Rolle aus der Einladung extrahieren
                val role = invitationDoc.data["role"] as? String ?: BasicTeamRole.VIEWER.name
                
                // Neues Teammitglied erstellen
                val memberData = hashMapOf(
                    "teamId" to invitation.teamId,
                    "userId" to userId,
                    "role" to role,
                    // Use ISO 8601 format for datetime that Appwrite expects
                    "joinedAt" to java.time.OffsetDateTime.now().toString()
                )
                
                // Mitglied zur team_members Collection hinzufügen
                databases.createDocument(
                    databaseId = DATABASE_ID,
                    collectionId = AppwriteService.COLLECTION_TEAM_MEMBERS,
                    documentId = "unique()",
                    data = memberData
                )
                
                // Einladungsstatus auf "accepted" setzen
                val updatedData = hashMapOf(
                    "status" to "accepted"
                )
                
                databases.updateDocument(
                    databaseId = DATABASE_ID,
                    collectionId = COLLECTION_TEAM_INVITATIONS,
                    documentId = invitation.membershipId,
                    data = updatedData
                )
                
                Log.d(TAG, "Einladung erfolgreich akzeptiert")
                
                // Note: We should reload invitations and teams after this operation
                // This might cause a suspension function warning, but it's safer to let the caller handle reloading
                // instead of trying to force it here with improper coroutine context
                
                // Rather than doing complex coroutine handling here, we'll log success
                // and let the caller (ViewModel) handle refreshing data
                
                // Log that we've successfully joined the team
                Log.d(TAG, "Erfolgreich dem Team ${invitation.teamId} beigetreten")
                
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Akzeptieren der Einladung: ${e.message}")
                Result.failure(e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Allgemeiner Fehler beim Akzeptieren der Einladung: ${e.message}")
            Result.failure(e)
        }
    }    /**
     * Lehnt eine Team-Einladung ab
     */
    suspend fun declineInvitation(invitation: TeamInvitation): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Lehne Einladung ab: TeamID=${invitation.teamId}, MembershipID=${invitation.membershipId}")
            
            try {
                // Status der Einladung auf "declined" setzen
                val updatedData = hashMapOf(
                    "status" to "declined"
                )
                
                databases.updateDocument(
                    databaseId = DATABASE_ID,
                    collectionId = COLLECTION_TEAM_INVITATIONS,
                    documentId = invitation.membershipId,
                    data = updatedData
                )
                
                Log.d(TAG, "Einladung erfolgreich abgelehnt")
                
                // Einladungen neu laden
                loadPendingInvitations()
                
                Result.success(true)
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Ablehnen der Einladung: ${e.message}")
                Result.failure(e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Allgemeiner Fehler beim Ablehnen der Einladung: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Aktualisiert die Rolle eines Teammitglieds
     */
    suspend fun updateTeamMemberRole(
        teamId: String,
        membershipId: String,
        role: BasicTeamRole
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Mitgliedschaftsdokument aktualisieren
            val updatedData = hashMapOf(
                "role" to role.name
            )
            
            databases.updateDocument(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_TEAM_MEMBERS,
                documentId = membershipId,
                data = updatedData
            )
            
            // Teams neu laden, um UI zu aktualisieren
            loadTeams()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }    /**
     * Entfernt ein Mitglied aus einem Team
     */
    suspend fun removeTeamMember(teamId: String, membershipId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Mitgliedschaftsdokument löschen
            databases.deleteDocument(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_TEAM_MEMBERS,
                documentId = membershipId
            )
            
            // Teams neu laden, um UI zu aktualisieren
            loadTeams()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Löscht ein Team
     */
    suspend fun deleteTeam(teamId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Team aus der teams Collection löschen
            databases.deleteDocument(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_TEAMS,
                documentId = teamId
            )
            
            // Alle Mitgliedschaften dieses Teams löschen
            val memberships = databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_TEAM_MEMBERS,
                queries = listOf(Query.equal("teamId", teamId))
            )
            
            for (memberDoc in memberships.documents) {
                databases.deleteDocument(
                    databaseId = DATABASE_ID,
                    collectionId = COLLECTION_TEAM_MEMBERS,
                    documentId = memberDoc.id
                )
            }
            
            // Alle Einladungen für dieses Team löschen
            val invitations = databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_TEAM_INVITATIONS,
                queries = listOf(Query.equal("teamId", teamId))
            )
            
            for (invitationDoc in invitations.documents) {
                databases.deleteDocument(
                    databaseId = DATABASE_ID,
                    collectionId = COLLECTION_TEAM_INVITATIONS,
                    documentId = invitationDoc.id
                )
            }
            
            // Alle Dog-Sharing-Einträge für dieses Team löschen
            val dogSharings = databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_DOG_SHARING,
                queries = listOf(Query.equal("teamId", teamId))
            )
            
            for (sharingDoc in dogSharings.documents) {
                databases.deleteDocument(
                    databaseId = DATABASE_ID,
                    collectionId = COLLECTION_DOG_SHARING,
                    documentId = sharingDoc.id
                )
            }
            
            // Teams neu laden, um UI zu aktualisieren
            loadTeams()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }    /**
     * Teilt einen Hund mit einem Team
     */
    suspend fun shareDogWithTeam(dogId: String, teamId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Aktuellen Benutzer abrufen
            Log.d(TAG, "Starte shareDogWithTeam für dogId=$dogId, teamId=$teamId")
            val currentUser = account.get()
            val userId = currentUser.id
            Log.d(TAG, "Aktueller Benutzer: $userId")
            
            // Prüfen, ob der Hund bereits mit diesem Team geteilt ist
            Log.d(TAG, "Prüfe auf existierende Sharing-Einträge in ${AppwriteService.COLLECTION_DOG_SHARING}")
            val existingShares = databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_DOG_SHARING,
                queries = listOf(
                    Query.equal("dogId", dogId),
                    Query.equal("teamId", teamId)
                )
            )
            
            Log.d(TAG, "Gefundene Sharing-Einträge: ${existingShares.documents.size}")
            
            if (existingShares.documents.isNotEmpty()) {
                Log.d(TAG, "Hund ist bereits mit diesem Team geteilt, ID: ${existingShares.documents.first().id}")
                return@withContext Result.success(Unit)
            }
            
            // Neues Sharing-Dokument erstellen
            Log.d(TAG, "Erstelle neues Sharing-Dokument")
            val sharingData = hashMapOf(
                "dogId" to dogId,
                "teamId" to teamId,
                "sharedByUserId" to userId,
                "sharedAt" to java.time.OffsetDateTime.now().toString()
            )
            
            Log.d(TAG, "Erstelle Dokument in Collection: ${AppwriteService.COLLECTION_DOG_SHARING}")
            Log.d(TAG, "Verwende databaseId=${DATABASE_ID}")
            // Erstelle eine konkrete ID statt "unique()"
            val docId = ID.unique()
            Log.d(TAG, "Generierte Dokument-ID: $docId")
            val createdDocument = databases.createDocument(
                databaseId = DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_DOG_SHARING,
                documentId = docId,
                data = sharingData
            )
            
            Log.d(TAG, "Hund $dogId erfolgreich mit Team $teamId geteilt. Dokument-ID: ${createdDocument.id}")
            return@withContext Result.success(Unit)
        } catch (e: AppwriteException) {
            // Spezifischer Appwrite-Fehler mit mehr Details
            Log.e(TAG, "Appwrite-Fehler beim Teilen des Hundes: Code=${e.code}, Nachricht=${e.message}, Typ=${e.type}", e)
            return@withContext Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Allgemeiner Fehler beim Teilen des Hundes: ${e.message}", e)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Beendet die Freigabe eines Hundes für ein Team
     */
    suspend fun unshareDogsFromTeam(dogId: String, teamId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Sharing-Dokumente für diesen Hund und dieses Team finden
            val sharings = databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_DOG_SHARING,
                queries = listOf(
                    Query.equal("dogId", dogId),
                    Query.equal("teamId", teamId)
                )
            )
            
            // Alle gefundenen Sharing-Dokumente löschen
            for (sharingDoc in sharings.documents) {
                databases.deleteDocument(
                    databaseId = DATABASE_ID,
                    collectionId = COLLECTION_DOG_SHARING,
                    documentId = sharingDoc.id
                )
            }
            
            Log.d(TAG, "Freigabe von Hund $dogId für Team $teamId entfernt")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Entfernen der Hundefreigabe: ${e.message}", e)
            Result.failure(e)
        }
    }    /**
     * Ruft alle Teams ab, mit denen ein Hund geteilt wurde
     */
    suspend fun getTeamsForDog(dogId: String): Result<List<com.example.snacktrack.data.model.Team>> = withContext(Dispatchers.IO) {
        try {
            // Alle Sharing-Dokumente für diesen Hund finden
            val sharings = databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_DOG_SHARING,
                queries = listOf(Query.equal("dogId", dogId))
            )
            
            // Team-IDs extrahieren
            val teamIds = sharings.documents.mapNotNull { doc ->
                doc.data["teamId"] as? String
            }
            
            // Teams laden
            val teams = mutableListOf<com.example.snacktrack.data.model.Team>()
            
            for (teamId in teamIds) {
                try {
                    // Team-Details laden
                    val teamResult = getTeam(teamId)
                    if (teamResult.isSuccess) {
                        val team = teamResult.getOrThrow()
                        teams.add(team)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Fehler beim Laden des Teams $teamId: ${e.message}")
                }
            }
            
            Result.success(teams)
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Abrufen der Teams für Hund $dogId: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Ruft alle Hunde ab, die mit einem Team geteilt wurden
     */
    suspend fun getSharedDogIdsForTeam(teamId: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            // Alle Sharing-Dokumente für dieses Team finden
            val sharings = databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_DOG_SHARING,
                queries = listOf(Query.equal("teamId", teamId))
            )
            
            // Hunde-IDs extrahieren
            val dogIds = sharings.documents.mapNotNull { doc ->
                doc.data["dogId"] as? String
            }
            
            Result.success(dogIds)
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Abrufen der geteilten Hunde für Team $teamId: ${e.message}", e)
            Result.failure(e)
        }
    }
}