package com.jaime.ascend.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.jaime.ascend.data.models.GoodHabit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.math.max

class RewardsViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val habitsViewModel: GoodHabitsViewModel
) : ViewModel() {

    var userCategories by mutableStateOf<Map<String, Map<String, Any>>>(emptyMap())
    var userCoins by mutableIntStateOf(0)
    var userHabits by mutableStateOf<List<String>>(emptyList())
    var maxLife by mutableIntStateOf(100)
    var currentLife by mutableIntStateOf(10)

    private var userListener: ListenerRegistration? = null

    init {
        loadUserData()
        viewModelScope.launch {
            checkPendingResets()

        }
        scheduleDailyReset()
    }

    override fun onCleared() {
        userListener?.remove()
        super.onCleared()
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return

        userListener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("RewardsViewModel", "Error listening to user data", error)
                    return@addSnapshotListener
                }

                snapshot?.let { doc ->
                    userCoins = (doc["coins"] as? Number)?.toInt() ?: 0
                    userCategories = (doc["categories"] as? Map<String, Map<String, Any>>) ?: emptyMap()
                    userHabits = (doc["ghabits"] as? List<String>) ?: emptyList()
                    maxLife = (doc["maxLife"] as? Number)?.toInt() ?: 100
                    currentLife = (doc["currentLife"] as? Number)?.toInt() ?: 10
                }
            }
    }

    fun toggleHabitCompleted(habit: GoodHabit, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                firestore.collection("ghabits").document(habit.id)
                    .update("completed", isCompleted)
                    .await()

                if (isCompleted) {
                    processHabitCompletion(habit)
                } else {
                    processHabitUncompletion(habit)
                }
            } catch (e: Exception) {
                Log.e("RewardsViewModel", "Error updating habit completion", e)
            }
        }
    }

    private suspend fun processHabitCompletion(habit: GoodHabit) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(userId)

        firestore.runTransaction { transaction ->
            val userDoc = transaction.get(userRef)
            val categories = (userDoc["categories"] as? Map<String, Map<String, Any>>)?.toMutableMap()
                ?: mutableMapOf()

            val categoryPath = habit.category?.id ?: return@runTransaction
            val categoryId = categoryPath.split("/").lastOrNull() ?: return@runTransaction

            val categoryData = categories[categoryId]?.toMutableMap() ?: createDefaultCategory().toMutableMap()

            val currentExp = (categoryData["currentExp"] as? Number)?.toInt() ?: 0
            val currentLevel = (categoryData["level"] as? Number)?.toInt() ?: 1
            var remainingExp = currentExp + habit.xpReward
            var newLevel = currentLevel
            var neededExp = (categoryData["neededExp"] as? Number)?.toInt() ?: calculateNextLevelExp(currentLevel)

            while (remainingExp >= neededExp && newLevel < 10) {
                remainingExp -= neededExp
                newLevel++
                neededExp = calculateNextLevelExp(newLevel)
            }

            categoryData.apply {
                put("currentExp", remainingExp)
                put("level", newLevel)
                put("neededExp", neededExp)
                put("nextLevelExp", calculateNextLevelExp(newLevel))
            }
            categories[categoryId] = categoryData

            val currentCoins = (userDoc["coins"] as? Number)?.toInt() ?: 0
            val newCoins = currentCoins + habit.coinReward

            val completedHabits = (userDoc["ghabits"] as? List<String>)?.toMutableList() ?: mutableListOf()
            if (!completedHabits.contains(habit.id)) {
                completedHabits.add(habit.id)
            }

            transaction.update(
                userRef,
                mapOf(
                    "categories" to categories,
                    "coins" to newCoins,
                    "ghabits" to completedHabits
                )
            )
        }.await()
    }

    private suspend fun processHabitUncompletion(habit: GoodHabit) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(userId)

        firestore.runTransaction { transaction ->
            val userDoc = transaction.get(userRef)
            val categories = (userDoc["categories"] as? Map<String, Map<String, Any>>)?.toMutableMap()
                ?: return@runTransaction

            val categoryPath = habit.category?.id ?: return@runTransaction
            val categoryId = categoryPath.split("/").lastOrNull() ?: return@runTransaction
            val categoryData = categories[categoryId]?.toMutableMap() ?: return@runTransaction

            val currentExp = (categoryData["currentExp"] as? Number)?.toInt() ?: 0
            var currentLevel = (categoryData["level"] as? Number)?.toInt() ?: 1
            var remainingExp = max(0, currentExp - habit.xpReward)
            var neededExp = (categoryData["neededExp"] as? Number)?.toInt() ?: calculateNextLevelExp(currentLevel)

            while (currentLevel > 1 && remainingExp <= 0) {
                currentLevel--
                neededExp = calculateNextLevelExp(currentLevel)
                remainingExp += neededExp
            }

            if (currentLevel < 1) {
                currentLevel = 1
                remainingExp = 0
                neededExp = calculateNextLevelExp(1)
            }

            categoryData.apply {
                put("currentExp", remainingExp)
                put("level", currentLevel)
                put("neededExp", neededExp)
            }
            categories[categoryId] = categoryData

            val currentCoins = (userDoc["coins"] as? Number)?.toInt() ?: 0
            val newCoins = max(0, currentCoins - habit.coinReward)

            val completedHabits = (userDoc["ghabits"] as? List<String>)?.toMutableList() ?: mutableListOf()
            completedHabits.remove(habit.id)

            transaction.update(
                userRef,
                mapOf(
                    "categories" to categories,
                    "coins" to newCoins,
                    "ghabits" to completedHabits
                )
            )
        }.await()
    }

    fun calculateNextLevelExp(currentLevel: Int): Int {
        return when (currentLevel) {
            1 -> 150
            2 -> 300
            3 -> 500
            4 -> 750
            5 -> 1050
            6 -> 1400
            7 -> 1800
            8 -> 2250
            9 -> 2750
            else -> 0
        }
    }

    private fun scheduleDailyReset() {
        viewModelScope.launch {
            while (true) {
                val now = Calendar.getInstance()
                val nextReset = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }

                val delayMillis = nextReset.timeInMillis - now.timeInMillis
                delay(if (delayMillis > 0) delayMillis else 0)

                executeDailyReset()

                if (now.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                    executeWeeklyReset()
                }
            }
        }
    }

    private suspend fun checkPendingResets() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(userId)

        try {
            val userDoc = userRef.get().await()
            val lastDailyReset = userDoc.getTimestamp("lastDailyReset")?.toDate()
            val lastWeeklyReset = userDoc.getTimestamp("lastWeeklyReset")?.toDate()
            val now = Calendar.getInstance()

            if (lastDailyReset == null || !isSameDay(lastDailyReset, now.timeInMillis)) {
                executeDailyReset()
                userRef.update("lastDailyReset", FieldValue.serverTimestamp()).await()
            }

            if (now.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY &&
                (lastWeeklyReset == null || !isSameWeek(lastWeeklyReset, now.timeInMillis))) {
                executeWeeklyReset()
            }
        } catch (e: Exception) {
            Log.e("Reset", "Error checking pending resets", e)
        }
    }

    private suspend fun executeDailyReset() {
        val userId = auth.currentUser?.uid ?: return
        try {
            val habitsSnapshot = firestore.collection("ghabits")
                .whereEqualTo("userId", userId)
                .whereEqualTo("completed", true)
                .get()
                .await()

            val batch = firestore.batch()
            habitsSnapshot.documents.forEach { doc ->
                batch.update(doc.reference, "completed", false)
            }
            batch.commit().await()
            Log.d("Reset", "Daily reset executed")
        } catch (e: Exception) {
            Log.e("Reset", "Error in daily reset", e)
        }
    }

    private suspend fun executeWeeklyReset() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(userId)

        try {
            firestore.runTransaction { transaction ->
                transaction.update(
                    userRef,
                    mapOf(
                        "categories" to createDefaultCategories(),
                        "lastWeeklyReset" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Log.d("Reset", "Weekly reset executed")
        } catch (e: Exception) {
            Log.e("Reset", "Error in weekly reset", e)
        }
    }

    private fun createDefaultCategories(): Map<String, Map<String, Any>> {
        return mapOf(
            "career_studies" to createDefaultCategory(),
            "couple" to createDefaultCategory(),
            "family" to createDefaultCategory(),
            "finances" to createDefaultCategory(),
            "mental_health" to createDefaultCategory(),
            "physic_health" to createDefaultCategory(),
            "self_care" to createDefaultCategory(),
            "social" to createDefaultCategory()
        )
    }

    private fun createDefaultCategory(): Map<String, Any> {
        return mapOf(
            "level" to 1,
            "currentExp" to 0,
            "neededExp" to 150
        )
    }

    private fun isSameDay(date1: Date, timeMillis: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = timeMillis }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameWeek(date1: Date, timeMillis: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = timeMillis }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
    }
}