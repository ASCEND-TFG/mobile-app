package com.jaime.ascend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaime.ascend.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val _signUpState = MutableStateFlow<Boolean?>(null)
    val signUpState: StateFlow<Boolean?> get() = _signUpState

    fun signIn(email: String, password: String, callback: (Boolean) -> Unit) {
        if (email.isEmpty() || password.isEmpty()) {
            callback(false)
            return
        }
        authRepository.signIn(email, password, callback)
    }

    fun signUp(email: String, password: String,username: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            authRepository.signUp(email, password, username) { success ->
                if (success) {
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }
        }
    }

}