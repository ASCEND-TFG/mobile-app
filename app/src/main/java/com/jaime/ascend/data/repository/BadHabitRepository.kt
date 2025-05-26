package com.jaime.ascend.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.jaime.ascend.data.models.BadHabit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BadHabitRepository(private val firestore: FirebaseFirestore) {
    private val habitsCollection = firestore.collection("bhabits")

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

    suspend fun createBadHabit(habit: BadHabit): String {
        return try {
            val docRef = habitsCollection.add(habit.toMap()).await()
            docRef.id
        } catch (e: Exception) {
            logError("BADHABIT_CREATE", "Error creating bad habit", e)
            throw e
        }
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