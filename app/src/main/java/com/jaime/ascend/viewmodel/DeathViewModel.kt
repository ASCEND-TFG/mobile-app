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

    /**
     * Loads the user's challenge.
     * @throws Exception if there is an error loading the challenge
     * @return The challenge
     */
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

    /**
     * Loads a specific challenge.
     * @param challengeId The ID of the challenge to load
     * @throws Exception if there is an error loading the challenge
     * @return The challenge
     */
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

    /**
     * Assigns a new challenge to the user.
     * @param userId The ID of the user to assign the challenge to
     * @throws Exception if there is an error assigning the challenge
     * @return The challenge
     */
    private suspend fun assignNewChallenge(userId: String) {
        try {
            val challenges = FirebaseFirestore.getInstance()
                .collection("challenges")
                .get()
                .await()

            val randomChallenge = challenges.documents.random()

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update("pendingChallenge", randomChallenge.id)
                .await()

            val descriptionMap = randomChallenge.get("description") as? Map<String, String>
            _revivalChallenge.value = getLocalizedChallenge(descriptionMap)
        } catch (e: Exception) {
            Log.e("DeathViewModel", "Error assigning new challenge", e)
        }
    }

    /**
     * Gets the localized challenge.
     * @param descriptionMap The map of descriptions
     * @return The localized challenge
     * @throws Exception if there is an error getting the localized challenge
     * @return The localized challenge
     */
    private fun getLocalizedChallenge(descriptionMap: Map<String, String>?): String {
        val lang = when (Locale.getDefault().language) {
            "es" -> "es"
            else -> "en"
        }
        return descriptionMap?.get(lang) ?: descriptionMap?.get("en") ?: ""
    }

    /**
     * Completes the challenge.
     * @throws Exception if there is an error completing the challenge
     * @return The result of the operation
     */
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