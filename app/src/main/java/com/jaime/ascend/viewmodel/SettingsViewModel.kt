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

/**
 * ViewModel for the settings screen.
 * It allows the user to delete their account.
 * @author Jaime Martínez Fernández
 * @param auth Firebase authentication instance
 * @param firestore Firebase Firestore instance
 */
class SettingsViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : ViewModel() {

    /**
     * Deletes the user's account.
     * @param onSuccess Callback to be executed on successful deletion
     * @param onFailure Callback to be executed on failure
     */
    fun deleteUserAccount(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser

                if (currentUser != null) {
                    deleteUserFromFirestore(currentUser.uid)
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

    /**
     * Deletes the user's account from Firestore.
     * @param userId ID of the user
     * @throws Exception if there is an error deleting from Firestore
     */
    private suspend fun deleteUserFromFirestore(userId: String) {
        try {
            firestore.collection("users").document(userId)
                .delete()
                .await()

            Log.d("SettingsViewModel", "Usuario eliminado de Firestore")
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Error eliminando de Firestore", e)
            throw e
        }
    }

    /**
     * Deletes the user's account from Firebase Authentication.
     * @param user User to be deleted
     * @throws Exception if there is an error deleting from Auth
     */
    private suspend fun deleteUserFromAuth(user: FirebaseUser) {
        try {
            user.delete().await()
            Log.d("SettingsViewModel", "Usuario eliminado de Auth")
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Error eliminando de Auth", e)
            throw e
        }
    }
}