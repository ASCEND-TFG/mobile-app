package com.jaime.ascend.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaime.ascend.R
import com.jaime.ascend.data.repository.FriendRequestRepository
import com.jaime.ascend.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FriendRequestViewModel(
    private val friendRequestRepository: FriendRequestRepository = FriendRequestRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val context: Context
) : ViewModel() {
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

    fun loadFriends() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val currentUserId = userRepository.getCurrentUserId() ?: return@launch
                val userDoc = userRepository.getUserDocument(currentUserId).await()
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
                // Manejar error
            } finally {
                _loading.value = false
            }
        }
    }

    private fun loadPendingRequests() {
        viewModelScope.launch {
            try {
                val currentUserId = friendRequestRepository.getCurrentUserId() ?: return@launch
                val userDoc = friendRequestRepository.getUserDocument(currentUserId).await()
                val pendingIds = userDoc?.get("pendingRequests") as? List<String> ?: emptyList()

                val requests = pendingIds.mapNotNull { userId ->
                    friendRequestRepository.getUserData(userId).await()?.data?.toMutableMap()?.apply {
                        put("documentId", userId)
                    }
                }

                _pendingRequests.value = requests
            } catch (e: Exception) {
                _uiState.value = FriendRequestUiState.Error("Error al cargar solicitudes")
            }
        }
    }

    fun acceptRequest(userId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = friendRequestRepository.getCurrentUserId() ?: return@launch
                friendRequestRepository.acceptFriendRequest(currentUserId, userId)
                loadPendingRequests() // Recargar la lista
                _uiState.value = FriendRequestUiState.RequestAccepted
            } catch (e: Exception) {
                _uiState.value = FriendRequestUiState.Error("Error al aceptar solicitud")
            }
        }
    }

    fun rejectRequest(userId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = friendRequestRepository.getCurrentUserId() ?: return@launch
                friendRequestRepository.rejectFriendRequest(currentUserId, userId)
                loadPendingRequests() // Recargar la lista
                _uiState.value = FriendRequestUiState.RequestRejected
            } catch (e: Exception) {
                _uiState.value = FriendRequestUiState.Error("Error al rechazar solicitud")
            }
        }
    }



fun sendFriendRequest(username: String) {
        viewModelScope.launch {
            _uiState.value = FriendRequestUiState.Loading

            try {
                val currentUserId = friendRequestRepository.getCurrentUserId()
                val targetUser = friendRequestRepository.searchUserByUsername(username) ?: throw Exception(context.getString(R.string.user_not_found))
                val targetUserId = targetUser["documentId"]?.toString() ?: ""

                // Verificar si es el mismo usuario
                if (targetUserId == currentUserId) {
                    throw Exception(context.getString(R.string.request_yourself))
                }

                // Verificar relación existente
                val friends = targetUser["friends"] as? List<String> ?: emptyList()
                val pendingRequests = targetUser["pendingRequests"] as? List<String> ?: emptyList()

                when {
                    friends.contains(currentUserId) -> {
                        _uiState.value = FriendRequestUiState.AlreadyFriends
                    }
                    pendingRequests.contains(currentUserId) -> {
                        _uiState.value = FriendRequestUiState.RequestAlreadySent
                    }
                    else -> {
                        val success = friendRequestRepository.sendFriendRequest(targetUserId)
                        _uiState.value = if (success) {
                            FriendRequestUiState.RequestSent
                        } else {
                            FriendRequestUiState.Error("Error al enviar solicitud")
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = FriendRequestUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun clearFoundUser() {
        _foundUser.value = null
    }
}

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