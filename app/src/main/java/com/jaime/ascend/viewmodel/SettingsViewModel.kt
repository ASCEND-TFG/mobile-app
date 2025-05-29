package com.jaime.ascend.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.jaime.ascend.ui.navigation.AppScreens
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SettingsViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : ViewModel() {

    fun deleteUserAccount(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser

                if (currentUser != null) {
                    // 1. Eliminar de Firestore
                    deleteUserFromFirestore(currentUser.uid)
                    // 2. Eliminar de Authentication
                    deleteUserFromAuth(currentUser)

                    onSuccess()
                } else {
                    onFailure(Exception("No hay usuario autenticado"))
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error al eliminar cuenta", e)
                onFailure(e)
            }
        }
    }

    private suspend fun deleteUserFromFirestore(userId: String) {
        try {
            // Eliminar documento del usuario
            firestore.collection("users").document(userId)
                .delete()
                .await()

            Log.d("SettingsViewModel", "Usuario eliminado de Firestore")
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Error eliminando de Firestore", e)
            throw e // Relanzamos para manejarlo en deleteUserAccount
        }
    }

    private suspend fun deleteUserFromAuth(user: FirebaseUser) {
        try {
            user.delete().await()
            Log.d("SettingsViewModel", "Usuario eliminado de Auth")
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Error eliminando de Auth", e)
            throw e // Relanzamos para manejarlo en deleteUserAccount
        }
    }
}