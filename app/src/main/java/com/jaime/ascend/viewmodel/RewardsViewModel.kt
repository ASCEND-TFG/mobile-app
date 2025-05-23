package com.jaime.ascend.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.jaime.ascend.data.models.GoodHabit
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
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
        checkWeeklyReset()
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
                // Update habit completion status
                firestore.collection("ghabits").document(habit.id)
                    .update("completed", isCompleted)
                    .await()


                // Process rewards or penalties
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

            // Get category ID from habit reference (format: "/categories/couple")
            val categoryPath = habit.category?.id ?: return@runTransaction
            val categoryId = categoryPath.split("/").lastOrNull() ?: return@runTransaction

            // Get or initialize category data
            val categoryData = categories[categoryId]?.toMutableMap() ?: createDefaultCategory().toMutableMap()

            // Current stats
            val currentExp = (categoryData["currentExp"] as? Number)?.toInt() ?: 0
            val currentLevel = (categoryData["level"] as? Number)?.toInt() ?: 1
            var remainingExp = currentExp + habit.xpReward
            var newLevel = currentLevel
            var neededExp = (categoryData["neededExp"] as? Number)?.toInt() ?: calculateNextLevelExp(currentLevel)

            // Level up logic
            while (remainingExp >= neededExp && newLevel < 10) {
                remainingExp -= neededExp
                newLevel++
                neededExp = calculateNextLevelExp(newLevel)
            }

            // Update category data
            categoryData.apply {
                put("currentExp", remainingExp)
                put("level", newLevel)
                put("neededExp", neededExp)
                put("nextLevelExp", calculateNextLevelExp(newLevel))
            }
            categories[categoryId] = categoryData

            // Update coins
            val currentCoins = (userDoc["coins"] as? Number)?.toInt() ?: 0
            val newCoins = currentCoins + habit.coinReward

            // Update completed habits list
            val completedHabits = (userDoc["ghabits"] as? List<String>)?.toMutableList() ?: mutableListOf()
            if (!completedHabits.contains(habit.id)) {
                completedHabits.add(habit.id)
            }

            // Commit all changes
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

            // Get category ID from habit reference
            val categoryPath = habit.category?.id ?: return@runTransaction
            val categoryId = categoryPath.split("/").lastOrNull() ?: return@runTransaction
            val categoryData = categories[categoryId]?.toMutableMap() ?: return@runTransaction

            // Current stats
            val currentExp = (categoryData["currentExp"] as? Number)?.toInt() ?: 0
            var currentLevel = (categoryData["level"] as? Number)?.toInt() ?: 1
            var remainingExp = max(0, currentExp - habit.xpReward)
            var neededExp = (categoryData["neededExp"] as? Number)?.toInt() ?: calculateNextLevelExp(currentLevel)

            // Level down logic if exp goes negative
            while (currentLevel > 1 && remainingExp <= 0) {
                currentLevel--
                neededExp = calculateNextLevelExp(currentLevel)
                remainingExp += neededExp
            }

            // Ensure we don't go below level 1
            if (currentLevel < 1) {
                currentLevel = 1
                remainingExp = 0
                neededExp = calculateNextLevelExp(1)
            }

            // Update category data
            categoryData.apply {
                put("currentExp", remainingExp)
                put("level", currentLevel)
                put("neededExp", neededExp)
            }
            categories[categoryId] = categoryData

            // Update coins (ensure doesn't go negative)
            val currentCoins = (userDoc["coins"] as? Number)?.toInt() ?: 0
            val newCoins = max(0, currentCoins - habit.coinReward)

            // Update completed habits list
            val completedHabits = (userDoc["ghabits"] as? List<String>)?.toMutableList() ?: mutableListOf()
            completedHabits.remove(habit.id)

            // Commit all changes
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
            1 -> 150   // Level 1 → 2: 150 XP needed
            2 -> 300    // Level 2 → 3: 300 XP needed
            3 -> 500    // Level 3 → 4: 500 XP needed
            4 -> 750    // Level 4 → 5: 750 XP needed
            5 -> 1050   // Level 5 → 6: 1050 XP needed
            6 -> 1400   // Level 6 → 7: 1400 XP needed
            7 -> 1800   // Level 7 → 8: 1800 XP needed
            8 -> 2250   // Level 8 → 9: 2250 XP needed
            9 -> 2750   // Level 9 → 10: 2750 XP needed
            else -> 0   // Max level (10) - no more XP needed
        }
    }

    private fun checkWeeklyReset() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            val userRef = firestore.collection("users").document(userId)

            try {
                firestore.runTransaction { transaction ->
                    val userDoc = transaction.get(userRef)
                    val lastReset = userDoc.getTimestamp("lastReset")?.toDate()
                    val now = Calendar.getInstance()

                    if (now.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && (lastReset == null || !isSameWeek(lastReset, now.timeInMillis)))
                        {
                            val resetCategories = mapOf(
                                "career_studies" to createDefaultCategory(),
                                "couple" to createDefaultCategory(),
                                "family" to createDefaultCategory(),
                                "finances" to createDefaultCategory(),
                                "mental_health" to createDefaultCategory(),
                                "physic_health" to createDefaultCategory(),
                                "self_care" to createDefaultCategory(),
                                "social" to createDefaultCategory()
                            )

                            transaction.update(
                                userRef,
                                mapOf(
                                    "categories" to resetCategories,
                                    "lastReset" to now.time,
                                    "currentLife" to maxLife // Restore full life
                                )
                            )
                        }
                }.await()
            } catch (e: Exception) {
                Log.e("RewardsViewModel", "Error during weekly reset check", e)
            }
        }
    }

    private fun createDefaultCategory(): Map<String, Any> {
        return mapOf(
            "level" to 1,
            "currentExp" to 0,
            "neededExp" to 150,
        )
    }

    private fun isSameWeek(date1: java.util.Date, timeMillis: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = timeMillis }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
    }
}