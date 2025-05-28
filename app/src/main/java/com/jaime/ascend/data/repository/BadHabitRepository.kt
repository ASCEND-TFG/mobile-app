package com.jaime.ascend.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.jaime.ascend.data.models.BadHabit
import com.jaime.ascend.utils.Difficulty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BadHabitRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val habitsCollection = firestore.collection("bhabits")
    private val templateRepository = TemplateRepository(firestore)

    // Current active listeners to avoid duplicates
    private val activeListeners = mutableMapOf<String, ListenerRegistration>()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getUserBadHabitsRealTime(userId: String): Flow<List<BadHabit>> = callbackFlow {
        val listenerKey = "badhabits_$userId"

        // Cancel any existing listener for this user
        activeListeners[listenerKey]?.remove()

        val listener = habitsCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val habits = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(BadHabit::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        logError("BADHABIT_PARSE", "Error parsing bad habit ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                trySend(habits)
            }

        activeListeners[listenerKey] = listener
        awaitClose { activeListeners.remove(listenerKey)?.remove() }
    }

    suspend fun createBadHabit(
        templateId: String,
        difficulty: Difficulty,
    ): Boolean {
        var success = false

        val template = templateRepository.getBadHabitTemplateById(templateId)
        val habitData = hashMapOf(
            "category" to template?.category,
            "coinReward" to difficulty.coinValue,
            "createdAt" to com.google.firebase.Timestamp.now(),
            "difficulty" to difficulty.name,
            "template" to FirebaseFirestore.getInstance()
                .document("bhabit_templates/$templateId"),
            "userId" to (auth.currentUser?.uid!!),
            "xpReward" to difficulty.xpValue,
            "completed" to false,
            "lifeLoss" to difficulty.lifeLoss,
            "lastRelapse" to com.google.firebase.Timestamp.now(),
            "streak" to null
        )

        FirebaseFirestore.getInstance()
            .collection("bhabits")
            .add(habitData)
            .await()

        success = true
        return success
    }

    suspend fun updateBadHabit(habit: BadHabit) {
        try {
            habitsCollection.document(habit.id).set(habit.toMap()).await()
        } catch (e: Exception) {
            logError("BADHABIT_UPDATE", "Error updating bad habit ${habit.id}", e)
            throw e
        }
    }

    suspend fun deleteBadHabit(habitId: String) {
        try {
            habitsCollection.document(habitId).delete().await()
        } catch (e: Exception) {
            logError("BADHABIT_DELETE", "Error deleting bad habit $habitId", e)
            throw e
        }
    }

    /* ------------------------- Helper Functions ------------------------- */
    private fun logError(tag: String, message: String, e: Exception) {
        Log.e(tag, "$message: ${e.message}")
    }
}