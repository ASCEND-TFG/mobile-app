package com.jaime.ascend.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.auth.User
import com.jaime.ascend.R
import com.jaime.ascend.data.repository.FriendRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FriendRequestViewModel(
    private val repository: FriendRequestRepository = FriendRequestRepository(),
    private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow<FriendRequestUiState>(FriendRequestUiState.Idle)
    val uiState: StateFlow<FriendRequestUiState> = _uiState

    private val _foundUser = MutableStateFlow<Map<String, Any>?>(null)
    val foundUser: StateFlow<Map<String, Any>?> = _foundUser

    fun sendFriendRequest(username: String) {
        viewModelScope.launch {
            _uiState.value = FriendRequestUiState.Loading

            try {
                val currentUserId = repository.getCurrentUserId() ?: throw Exception("Usuario no autenticado")
                val targetUser = repository.searchUserByUsername(username) ?: throw Exception("Usuario no encontrado")
                val targetUserId = targetUser["documentId"]?.toString() ?: throw Exception("ID de usuario inválido")

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
                        val success = repository.sendFriendRequest(targetUserId)
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
    data class Error(val message: String) : FriendRequestUiState()
}