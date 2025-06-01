package com.jaime.ascend.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import androidx.compose.runtime.State

class DeathViewModel(
    private val context: Context
) : ViewModel() {
    private val _revivalChallenge = mutableStateOf("")
    val revivalChallenge: State<String> = _revivalChallenge

    private val _isChallengeCompleted = mutableStateOf(false)
    val isChallengeCompleted: State<Boolean> = _isChallengeCompleted

    init {
        loadUserChallenge()
    }

    private fun loadUserChallenge() {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

                // 1. Verificar si el usuario ya tiene un challenge pendiente
                val userDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()

                val pendingChallengeId = userDoc.getString("pendingChallenge")

                if (pendingChallengeId != null) {
                    loadSpecificChallenge(pendingChallengeId)
                } else {
                    assignNewChallenge(userId)
                }
            } catch (e: Exception) {
                Log.e("DeathViewModel", "Error loading user challenge", e)
            }
        }
    }

    private suspend fun loadSpecificChallenge(challengeId: String) {
        try {
            val challengeDoc = FirebaseFirestore.getInstance()
                .collection("challenges")
                .document(challengeId)
                .get()
                .await()

            val descriptionMap = challengeDoc.get("description") as? Map<String, String>
            _revivalChallenge.value = getLocalizedChallenge(descriptionMap)
        } catch (e: Exception) {
            Log.e("DeathViewModel", "Error loading specific challenge", e)
        }
    }

    private suspend fun assignNewChallenge(userId: String) {
        try {
            // 1. Obtener todos los challenges disponibles
            val challenges = FirebaseFirestore.getInstance()
                .collection("challenges")
                .get()
                .await()

            // 2. Seleccionar uno aleatorio
            val randomChallenge = challenges.documents.random()

            // 3. Guardar referencia en el usuario
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update("pendingChallenge", randomChallenge.id)
                .await()

            // 4. Cargar el challenge seleccionado
            val descriptionMap = randomChallenge.get("description") as? Map<String, String>
            _revivalChallenge.value = getLocalizedChallenge(descriptionMap)
        } catch (e: Exception) {
            Log.e("DeathViewModel", "Error assigning new challenge", e)
        }
    }

    private fun getLocalizedChallenge(descriptionMap: Map<String, String>?): String {
        val lang = when (Locale.getDefault().language) {
            "es" -> "es"
            else -> "en"
        }
        return descriptionMap?.get(lang) ?: descriptionMap?.get("en") ?: ""
    }

    fun completeChallenge() {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update(
                        mapOf(
                            "currentLife" to 50,
                            "isShopLocked" to false,
                            "pendingChallenge" to null
                        )
                    )
                    .await()

                _isChallengeCompleted.value = true
            } catch (e: Exception) {
                Log.e("DeathViewModel", "Error completing challenge", e)
            }
        }
    }
}