package com.example.snacktrack.data.repository

import android.content.Context
import android.util.Log
import com.example.snacktrack.data.service.AppwriteService
import com.example.snacktrack.data.service.AppwriteConfig
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.models.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Team(
    val id: String,
    val name: String,
    val description: String? = null,
    val ownerId: String,
    val createdAt: LocalDateTime,
    val memberCount: Int = 1,
    val members: List<TeamMember> = emptyList(),
    val sharedDogs: List<String>? = null
)

data class TeamMember(
    val id: String,
    val teamId: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val role: TeamRole,
    val joinedAt: LocalDateTime
) {
    val name: String get() = userName
    val email: String get() = userEmail
}

enum class TeamRole {
    OWNER,
    ADMIN,
    MEMBER
}

class TeamRepository(private val context: Context) : BaseRepository() {
    
    private val appwriteService = AppwriteService.getInstance(context)
    private val databases = appwriteService.databases
    
    /**
     * Holt alle Teams eines Benutzers
     */
    fun getTeams(): Flow<List<Team>> = flow {
        try {
            Log.d("TeamRepository", "Lade Teams")
            
            // Prüfe Session
            if (!appwriteService.ensureValidSession()) {
                Log.e("TeamRepository", "Keine gültige Session")
                emit(emptyList())
                return@flow
            }
            
            val user = appwriteService.account.get()
            
            // Hole alle Teams, in denen der Benutzer Mitglied ist
            val membershipResponse = databases.listDocuments(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_TEAM_MEMBERS,
                queries = listOf(
                    Query.equal("userId", user.id)
                )
            )
            
            val teamIds = membershipResponse.documents.map { doc ->
                doc.data["teamId"].toString()
            }
            
            if (teamIds.isEmpty()) {
                emit(emptyList())
                return@flow
            }
            
            // Hole Team-Details mit Mitgliedern
            val teams = mutableListOf<Team>()
            for (teamId in teamIds) {
                try {
                    val teamDoc = databases.getDocument(
                        databaseId = AppwriteConfig.DATABASE_ID,
                        collectionId = AppwriteConfig.COLLECTION_TEAMS,
                        documentId = teamId
                    )
                    
                    // Hole Mitglieder für dieses Team
                    val membersResponse = databases.listDocuments(
                        databaseId = AppwriteConfig.DATABASE_ID,
                        collectionId = AppwriteConfig.COLLECTION_TEAM_MEMBERS,
                        queries = listOf(Query.equal("teamId", teamId))
                    )
                    
                    val members = membersResponse.documents.map { doc ->
                        convertDocumentToTeamMember(doc)
                    }
                    
                    teams.add(convertDocumentToTeam(teamDoc).copy(members = members))
                } catch (e: Exception) {
                    Log.e("TeamRepository", "Fehler beim Abrufen von Team $teamId: ${e.message}")
                }
            }
            
            Log.d("TeamRepository", "Teams geladen: ${teams.size}")
            emit(teams)
            
        } catch (e: Exception) {
            Log.e("TeamRepository", "Fehler beim Laden der Teams: ${e.message}", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Erstellt ein neues Team
     */
    suspend fun createTeam(name: String, description: String? = null): Result<Team> = withContext(Dispatchers.IO) {
        safeApiCall {
            val user = appwriteService.account.get()
            val now = LocalDateTime.now()
            
            // Erstelle Team
            val teamData = mapOf(
                "name" to name,
                "description" to description,
                "ownerId" to user.id,
                "createdAt" to now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
            
            val teamResponse = databases.createDocument(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_TEAMS,
                documentId = ID.unique(),
                data = teamData
            )
            
            // Füge Owner als Team-Mitglied hinzu
            val memberData = mapOf(
                "teamId" to teamResponse.id,
                "userId" to user.id,
                "userName" to user.name,
                "userEmail" to user.email,
                "role" to TeamRole.OWNER.name,
                "joinedAt" to now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
            
            databases.createDocument(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_TEAM_MEMBERS,
                documentId = ID.unique(),
                data = memberData
            )
            
            convertDocumentToTeam(teamResponse)
        }
    }
    
    /**
     * Holt alle Mitglieder eines Teams
     */
    fun getTeamMembers(teamId: String): Flow<List<TeamMember>> = flow {
        try {
            val response = databases.listDocuments(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_TEAM_MEMBERS,
                queries = listOf(
                    Query.equal("teamId", teamId)
                )
            )
            
            val members = response.documents.map { doc ->
                convertDocumentToTeamMember(doc)
            }
            
            emit(members)
            
        } catch (e: Exception) {
            Log.e("TeamRepository", "Fehler beim Laden der Team-Mitglieder: ${e.message}", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Fügt ein Mitglied zum Team hinzu
     */
    suspend fun addTeamMember(teamId: String, userEmail: String, role: TeamRole = TeamRole.MEMBER): Result<TeamMember> = withContext(Dispatchers.IO) {
        safeApiCall {
            // TODO: Benutzer anhand der E-Mail suchen und hinzufügen
            // Dies erfordert eine Benutzer-Suche-Funktion im Backend
            throw NotImplementedError("Benutzersuche noch nicht implementiert")
        }
    }
    
    /**
     * Entfernt ein Mitglied aus dem Team
     */
    suspend fun removeTeamMember(teamId: String, userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeApiCall {
            // Finde das Mitgliedschafts-Dokument
            val response = databases.listDocuments(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_TEAM_MEMBERS,
                queries = listOf(
                    Query.equal("teamId", teamId),
                    Query.equal("userId", userId)
                )
            )
            
            if (response.documents.isNotEmpty()) {
                databases.deleteDocument(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.COLLECTION_TEAM_MEMBERS,
                    documentId = response.documents.first().id
                )
            }
            
            Unit
        }
    }
    
    /**
     * Löscht ein Team
     */
    suspend fun deleteTeam(teamId: String): Result<Unit> = withContext(Dispatchers.IO) {
        safeApiCall {
            // Lösche alle Team-Mitgliedschaften
            val membersResponse = databases.listDocuments(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_TEAM_MEMBERS,
                queries = listOf(Query.equal("teamId", teamId))
            )
            
            for (doc in membersResponse.documents) {
                databases.deleteDocument(
                    databaseId = AppwriteConfig.DATABASE_ID,
                    collectionId = AppwriteConfig.COLLECTION_TEAM_MEMBERS,
                    documentId = doc.id
                )
            }
            
            // Lösche das Team
            databases.deleteDocument(
                databaseId = AppwriteConfig.DATABASE_ID,
                collectionId = AppwriteConfig.COLLECTION_TEAMS,
                documentId = teamId
            )
            
            Unit
        }
    }
    
    // Hilfsfunktionen
    
    private fun convertDocumentToTeam(doc: Document<Map<String, Any>>): Team {
        return Team(
            id = doc.id,
            name = doc.data["name"].toString(),
            description = doc.data["description"]?.toString(),
            ownerId = doc.data["ownerId"].toString(),
            createdAt = LocalDateTime.parse(
                doc.data["createdAt"].toString(),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
            ),
            memberCount = (doc.data["memberCount"] as? Number)?.toInt() ?: 1
        )
    }
    
    private fun convertDocumentToTeamMember(doc: Document<Map<String, Any>>): TeamMember {
        return TeamMember(
            id = doc.id,
            teamId = doc.data["teamId"].toString(),
            userId = doc.data["userId"].toString(),
            userName = doc.data["userName"].toString(),
            userEmail = doc.data["userEmail"].toString(),
            role = TeamRole.valueOf(doc.data["role"].toString()),
            joinedAt = LocalDateTime.parse(
                doc.data["joinedAt"].toString(),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
            )
        )
    }
}