package com.jaime.ascend.viewmodel

import androidx.lifecycle.ViewModel
import com.jaime.ascend.auth.AuthRepository

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    fun signIn(email: String, password: String, callback: (Boolean) -> Unit) {
        if (email.isEmpty() || password.isEmpty()) {
            callback(false)
            return
        }
        authRepository.signIn(email, password, callback)
    }

    fun signUp(email: String, password: String, callback: (Boolean) -> Unit) {
        if (email.isEmpty() || password.isEmpty()) {
            callback(false)
            return
        }
        authRepository.signUp(email, password, callback)
    }

}