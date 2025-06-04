package com.jaime.ascend.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.jaime.ascend.data.repository.FriendRequestRepository
import com.jaime.ascend.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel for the friend request screen.
 * It allows the user to send friend requests.
 * @author Jaime Martínez Fernández
 * @param friendRequestRepository The repository for friend requests
 * @param userRepository The repository for users
 * @param context The application context
 */
class FriendRequestViewModel(
    private val friendRequestRepository: FriendRequestRepository,
    private val userRepository: UserRepository,
    private val context: Context
) : ViewModel() {

    private val repo = FriendRequestRepository(
        firestore = FirebaseFirestore.getInstance(),
        auth = FirebaseAuth.getInstance(),
        functions = FirebaseFunctions.getInstance(),
        messaging = FirebaseMessaging.getInstance()
    )

    private val _friendsList = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val friendsList: StateFlow<List<Map<String, Any>>> = _friendsList

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _uiState = MutableStateFlow<FriendRequestUiState>(FriendRequestUiState.Idle)
    val uiState: StateFlow<FriendRequestUiState> = _uiState

    private val _foundUser = MutableStateFlow<Map<String, Any>?>(null)
    val foundUser: StateFlow<Map<String, Any>?> = _foundUser

    private val _pendingRequests = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val pendingRequests: StateFlow<List<Map<String, Any>>> = _pendingRequests

    init {
        loadPendingRequests()
    }

    init {
        loadFriends()
    }

    /**
     * Loads the user's friends.
     * @throws Exception if there is an error loading the friends
     * @return The list of friends
     */
    fun loadFriends() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val currentUserId = userRepository.getCurrentUserId() ?: return@launch
                val userDoc = userRepository.getUserData(currentUserId).await()
                val friendsIds = userDoc?.get("friends") as? List<String> ?: emptyList()

                val friends = friendsIds.mapNotNull { friendId ->
                    userRepository.getUserData(friendId).await()?.data?.toMutableMap()?.apply {
                        put("documentId", friendId)
                        // Asegurar campos mínimos
                        putIfAbsent("avatarUrl", "")
                        putIfAbsent("username", "Usuario")
                        putIfAbsent("currentLife", 0)
                        putIfAbsent("maxLife", 100)
                        putIfAbsent("coins", 0)
                    }
                }

                _friendsList.value = friends
            } catch (e: Exception) {
                Log.e("TAG", "loadFriends: $e")
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Loads the pending friend requests.
     * @throws Exception if there is an error loading the requests
     * @return The list of pending requests
     */
    private fun loadPendingRequests() {
        viewModelScope.launch {
            try {
                val currentUserId = repo.getCurrentUserId() ?: return@launch
                val userDoc = repo.getUserData(currentUserId).await()
                val pendingIds = userDoc?.get("pendingRequests") as? List<String> ?: emptyList()

                val requests = pendingIds.mapNotNull { userId ->
                    repo.getUserData(userId).await()?.data?.toMutableMap()
                        ?.apply {
                            put("documentId", userId)
                        }
                }

                _pendingRequests.value = requests
            } catch (e: Exception) {
                _uiState.value = FriendRequestUiState.Error("Error al cargar solicitudes")
            }
        }
    }

    /**
     * Accepts a friend request.
     * @param userId The ID of the user to accept the request from
     * @throws Exception if there is an error accepting the request
     * @return The result of the operation
     */
    fun acceptRequest(userId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = repo.getCurrentUserId() ?: return@launch
                repo.acceptFriendRequest(userId, currentUserId)
                loadPendingRequests() // Recargar la lista
                _uiState.value = FriendRequestUiState.RequestAccepted
            } catch (e: Exception) {
                _uiState.value = FriendRequestUiState.Error("Error al aceptar solicitud")
            }
        }
    }

    /**
     * Rejects a friend request.
     * @param userId The ID of the user to reject the request from
     * @throws Exception if there is an error rejecting the request
     * @return The result of the operation
     */
    fun rejectRequest(userId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = repo.getCurrentUserId() ?: return@launch
                repo.rejectFriendRequest(currentUserId, userId)
                loadPendingRequests() // Recargar la lista
                _uiState.value = FriendRequestUiState.RequestRejected
            } catch (e: Exception) {
                _uiState.value = FriendRequestUiState.Error("Error al rechazar solicitud")
            }
        }
    }

    /**
     * Sends a friend request to a user.
     * @param username The username of the user to send the request to
     * @throws Exception if there is an error sending the request
     * @return The result of the operation
     */
    fun sendFriendRequest(username: String) {
        viewModelScope.launch {
            _uiState.value = FriendRequestUiState.Loading
            try {
                val targetUser = repo.searchUserByUsername(username) ?: throw Exception("Usuario no encontrado")
                val targetUserId = targetUser["documentId"].toString()
                val currentUserId = repo.getCurrentUserId() ?: throw Exception("No autenticado")

                if (targetUserId == currentUserId) throw Exception("No puedes enviarte solicitud a ti mismo")

                val friends = targetUser["friends"] as? List<String> ?: emptyList()
                val pending = targetUser["pendingRequests"] as? List<String> ?: emptyList()

                when {
                    friends.contains(currentUserId) -> _uiState.value = FriendRequestUiState.AlreadyFriends
                    pending.contains(currentUserId) -> _uiState.value = FriendRequestUiState.RequestAlreadySent
                    else -> {
                        val targetUsername = targetUser["username"].toString()
                        val success = repo.sendFriendRequest(targetUserId, targetUsername)
                        _uiState.value = if (success) {
                            FriendRequestUiState.RequestSent
                        } else {
                            FriendRequestUiState.Error("Error al enviar")
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = FriendRequestUiState.Error(e.message ?: "Error")
            }
        }
    }

    /**
     * Clears the found user.
     * @return The result of the operation
     */
    fun clearFoundUser() {
        _foundUser.value = null
    }
}

/**
 * Represents the state of the friend request screen.
 * @author Jaime Martínez Fernández
 * @param Idle The initial state
 * @param Loading The state while loading
 * @param UserFound The state when a user is found
 * @param UserNotFound The state when a user is not found
 * @param RequestSent The state when a request is sent
 * @param AlreadyFriends The state when a user is already a friend
 * @param RequestAlreadySent The state when a request is already sent
 * @param RequestAccepted The state when a request is accepted
 * @param RequestRejected The state when a request is rejected
 * @param Error The state when an error occurs
 * @return The state of the friend request screen
 */
sealed class FriendRequestUiState {
    object Idle : FriendRequestUiState()
    object Loading : FriendRequestUiState()
    object UserFound : FriendRequestUiState()
    object UserNotFound : FriendRequestUiState()
    object RequestSent : FriendRequestUiState()
    object AlreadyFriends : FriendRequestUiState()
    object RequestAlreadySent : FriendRequestUiState()
    object RequestAccepted : FriendRequestUiState()
    object RequestRejected : FriendRequestUiState()
    data class Error(val message: String) : FriendRequestUiState()
}