package com.jaime.ascend.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.jaime.ascend.data.models.GoodHabit
import com.jaime.ascend.utils.Difficulty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class GoodHabitRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val templateRepository = TemplateRepository(firestore)
    private val habitsCollection = firestore.collection("ghabits")

    // Current active listeners to avoid duplicates
    private val activeListeners = mutableMapOf<String, ListenerRegistration>()

    suspend fun createGoodHabit(
        templateId: String,
        days: List<Int>,
        difficulty: Difficulty,
        reminderTime: String? = null,
    ): Boolean {
        var success = false

        val template = templateRepository.getGoodHabitTemplateById(templateId)
        val habitData = hashMapOf(
            "category" to template?.category,
            "coinReward" to difficulty.coinValue,
            "createdAt" to com.google.firebase.Timestamp.now(),
            "days" to days,
            "difficulty" to difficulty.name,
            "template" to FirebaseFirestore.getInstance()
                .document("ghabit_templates/$templateId"),
            "userId" to (auth.currentUser?.uid!!),
            "xpReward" to difficulty.xpValue,
            "completed" to false,
            "reminderTime" to reminderTime,
        )

        reminderTime?.let {
            habitData["reminderTime"] = it
        }

        FirebaseFirestore.getInstance()
            .collection("ghabits")
            .add(habitData)
            .await()

        success = true
        return success
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    fun getUserGoodHabitsRealTime(userId: String): Flow<List<GoodHabit>> = callbackFlow {
        val listenerKey = "habits_$userId"

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
                        doc.toObject(GoodHabit::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("HABIT_PARSE", "Error parsing habit ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                trySend(habits)
            }

        activeListeners[listenerKey] = listener
        awaitClose { activeListeners.remove(listenerKey)?.remove() }
    }
}
