package com.example.snacktrack.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snacktrack.data.model.CommunityPost
import com.example.snacktrack.data.model.CommunityProfile
import com.example.snacktrack.data.model.PostType
import com.example.snacktrack.data.repository.CommunityRepository
import com.example.snacktrack.data.repository.DogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel für Community-Funktionen
 */
class CommunityViewModel(private val context: Context) : ViewModel() {

    private val communityRepository = CommunityRepository(context)
    private val dogRepository = DogRepository(context)

    // UI-State für den Feed
    private val _feedState = MutableStateFlow<FeedState>(FeedState.Loading)
    val feedState: StateFlow<FeedState> = _feedState.asStateFlow()

    // Posts im Feed
    val posts: StateFlow<List<CommunityPost>> = communityRepository.feedPostsStateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Benutzerprofil
    val userProfile: StateFlow<CommunityProfile?> = communityRepository.userProfileStateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // State für das Erstellen von Posts
    var postCreationState by mutableStateOf<PostCreationState>(PostCreationState.Idle)
        private set

    // State für Profilbearbeitung
    var profileEditState by mutableStateOf<ProfileEditState>(ProfileEditState.Idle)
        private set

    init {
        loadFeed()
        loadUserProfile()
    }

    /**
     * Lädt den Feed mit Community-Posts
     */
    fun loadFeed() {
        _feedState.value = FeedState.Loading
        viewModelScope.launch {
            try {
                val result = communityRepository.refreshFeed()
                if (result.isSuccess) {
                    _feedState.value = FeedState.Success(result.getOrThrow())
                } else {
                    _feedState.value = FeedState.Error("Fehler beim Laden des Feeds")
                }
            } catch (e: Exception) {
                _feedState.value = FeedState.Error("Fehler beim Laden des Feeds: ${e.message}")
            }
        }
    }

    /**
     * Lädt das Profil des aktuellen Benutzers
     */
    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                communityRepository.getCurrentUserProfile()
            } catch (e: Exception) {
                // Fehler werden über den StateFlow gehandhabt
            }
        }
    }

    /**
     * Erstellt einen neuen Post
     */
    fun createPost(
        content: String,
        postType: PostType,
        dogId: String? = null,
        imageUris: List<Uri> = emptyList(),
        hashtags: List<String> = emptyList()
    ) {
        postCreationState = PostCreationState.Creating
        viewModelScope.launch {
            try {
                val result = communityRepository.createPost(
                    content = content,
                    postType = postType,
                    dogId = dogId,
                    imageUris = imageUris,
                    hashtags = hashtags
                )

                postCreationState = if (result.isSuccess) {
                    PostCreationState.Success(result.getOrThrow())
                } else {
                    PostCreationState.Error("Fehler beim Erstellen des Posts")
                }
            } catch (e: Exception) {
                postCreationState = PostCreationState.Error("Fehler beim Erstellen des Posts: ${e.message}")
            }
        }
    }

    /**
     * Erstellt oder aktualisiert das Benutzerprofil
     */
    fun createOrUpdateProfile(
        displayName: String,
        bio: String? = null,
        profileImageUri: Uri? = null
    ) {
        profileEditState = ProfileEditState.Saving
        viewModelScope.launch {
            try {
                val result = communityRepository.createOrUpdateProfile(
                    displayName = displayName,
                    bio = bio,
                    profileImageUri = profileImageUri
                )

                profileEditState = if (result.isSuccess) {
                    ProfileEditState.Success(result.getOrThrow())
                } else {
                    ProfileEditState.Error("Fehler beim Speichern des Profils")
                }
            } catch (e: Exception) {
                profileEditState = ProfileEditState.Error("Fehler beim Speichern des Profils: ${e.message}")
            }
        }
    }

    /**
     * Löscht einen Post
     */
    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                communityRepository.deletePost(postId)
                loadFeed() // Feed neu laden
            } catch (e: Exception) {
                // Fehler behandeln
            }
        }
    }

    /**
     * Liked oder Unlikes einen Post
     */
    fun toggleLike(postId: String) {
        viewModelScope.launch {
            try {
                communityRepository.toggleLike(postId)
                loadFeed() // Feed aktualisieren um den Like-Status zu zeigen
            } catch (e: Exception) {
                // Fehler behandeln
            }
        }
    }

    /**
     * Zustandsreset für Postersterllung
     */
    fun resetPostCreationState() {
        postCreationState = PostCreationState.Idle
    }

    /**
     * Zustandsreset für Profilbearbeitung
     */
    fun resetProfileEditState() {
        profileEditState = ProfileEditState.Idle
    }
}

/**
 * Zustände für den Feed
 */
sealed class FeedState {
    object Loading : FeedState()
    data class Success(val posts: List<CommunityPost>) : FeedState()
    data class Error(val message: String) : FeedState()
}

/**
 * Zustände für Postererstellung
 */
sealed class PostCreationState {
    object Idle : PostCreationState()
    object Creating : PostCreationState()
    data class Success(val post: CommunityPost) : PostCreationState()
    data class Error(val message: String) : PostCreationState()
}

/**
 * Zustände für Profilbearbeitung
 */
sealed class ProfileEditState {
    object Idle : ProfileEditState()
    object Saving : ProfileEditState()
    data class Success(val profile: CommunityProfile) : ProfileEditState()
    data class Error(val message: String) : ProfileEditState()
}

// CommunityViewModelFactory wurde in eine separate Datei verschoben
// Siehe com.example.snacktrack.ui.viewmodel.CommunityViewModelFactory
