package com.example.snacktrack.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snacktrack.data.model.CommunityPost
import com.example.snacktrack.data.model.CommunityComment
import com.example.snacktrack.data.model.UserProfile
import com.example.snacktrack.data.repository.CommunityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileEditState {
    object Idle : ProfileEditState()
    object Loading : ProfileEditState()
    object Saving : ProfileEditState()
    object Success : ProfileEditState()
    data class Error(val message: String) : ProfileEditState()
}

sealed class CommentState {
    object Loading : CommentState()
    data class Success(val comments: List<CommunityComment>) : CommentState()
    data class Error(val message: String) : CommentState()
}

sealed class FeedState {
    object Loading : FeedState()
    data class Success(val posts: List<CommunityPost>) : FeedState()
    data class Error(val message: String) : FeedState()
}

sealed class PostCreationState {
    object Idle : PostCreationState()
    object Loading : PostCreationState()
    object Success : PostCreationState()
    data class Error(val message: String) : PostCreationState()
}

class CommunityViewModel(
    private val context: Context
) : ViewModel() {
    
    private val communityRepository = CommunityRepository(context)
    
    private val _posts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val posts: StateFlow<List<CommunityPost>> = _posts.asStateFlow()
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    private val _profileEditState = MutableStateFlow<ProfileEditState>(ProfileEditState.Idle)
    val profileEditState: StateFlow<ProfileEditState> = _profileEditState.asStateFlow()
    
    private val _feedState = MutableStateFlow<FeedState>(FeedState.Loading)
    val feedState: StateFlow<FeedState> = _feedState.asStateFlow()
    
    private val _postCreationState = MutableStateFlow<PostCreationState>(PostCreationState.Idle)
    val postCreationState: StateFlow<PostCreationState> = _postCreationState.asStateFlow()
    
    private val _comments = MutableStateFlow<List<CommunityComment>>(emptyList())
    val comments: StateFlow<List<CommunityComment>> = _comments.asStateFlow()
    
    private val _commentState = MutableStateFlow<CommentState>(CommentState.Loading)
    val commentState: StateFlow<CommentState> = _commentState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    val userId: String = "current_user_id" // TODO: Get from auth
    
    init {
        loadPosts()
    }
    
    fun loadPosts(category: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            communityRepository.getPosts(category).collect { postList ->
                _posts.value = postList
                _isLoading.value = false
            }
        }
    }
    
    fun createPost(
        title: String,
        content: String,
        category: String,
        postType: String = "general",
        images: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _postCreationState.value = PostCreationState.Loading
            communityRepository.createPost(title, content, category, postType, images)
                .onSuccess {
                    _postCreationState.value = PostCreationState.Success
                    loadPosts()
                }
                .onFailure { e ->
                    _postCreationState.value = PostCreationState.Error(e.message ?: "Fehler beim Erstellen des Beitrags")
                }
        }
    }
    
    fun toggleLike(postId: String) {
        viewModelScope.launch {
            communityRepository.toggleLikePost(postId)
                .onSuccess {
                    loadPosts()
                }
                .onFailure { e ->
                    _error.value = e.message
                }
        }
    }
    
    fun createComment(postId: String, content: String) {
        viewModelScope.launch {
            communityRepository.createComment(postId, content)
                .onSuccess {
                    // Reload posts to update comment count
                    loadPosts()
                }
                .onFailure { e ->
                    _error.value = e.message
                }
        }
    }
    
    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            // TODO: Implement delete comment
            _error.value = "Löschen von Kommentaren noch nicht implementiert"
        }
    }
    
    fun loadUserProfile(userId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            // TODO: Load user profile from repository
            // Use provided userId or default to current user
            val targetUserId = userId ?: this@CommunityViewModel.userId
            _userProfile.value = null
            _isLoading.value = false
        }
    }
    
    fun createOrUpdateProfile(displayName: String, bio: String) {
        viewModelScope.launch {
            _profileEditState.value = ProfileEditState.Saving
            // TODO: Create or update profile via repository
            _profileEditState.value = ProfileEditState.Success
        }
    }
    
    fun updateProfile(displayName: String, bio: String) {
        createOrUpdateProfile(displayName, bio)
    }
    
    fun loadFeed(category: String? = null) {
        viewModelScope.launch {
            _feedState.value = FeedState.Loading
            communityRepository.getPosts(category).collect { postList ->
                _feedState.value = if (postList.isEmpty()) {
                    FeedState.Error("Keine Beiträge gefunden")
                } else {
                    FeedState.Success(postList)
                }
            }
        }
    }
    
    fun deletePost(postId: String) {
        viewModelScope.launch {
            // TODO: Implement delete post
            _error.value = "Löschen von Beiträgen noch nicht implementiert"
        }
    }
    
    fun resetPostCreationState() {
        _postCreationState.value = PostCreationState.Idle
    }
    
    fun loadCommentsForPost(postId: String) {
        viewModelScope.launch {
            _commentState.value = CommentState.Loading
            communityRepository.getComments(postId).collect { commentList ->
                _comments.value = commentList
                _commentState.value = CommentState.Success(comments = commentList)
            }
        }
    }
}

class CommunityViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommunityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CommunityViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}