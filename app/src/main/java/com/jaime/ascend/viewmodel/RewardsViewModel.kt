package com.jaime.ascend.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.jaime.ascend.data.models.BadHabit
import com.jaime.ascend.data.models.GoodHabit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import kotlin.math.max
import kotlin.math.min

/**
 * ViewModel for the rewards screen.
 * It allows the user to check their rewards.
 * @author Jaime Martínez Fernández
 * @param auth Firebase authentication instance
 * @param firestore Firebase Firestore instance
 */
class RewardsViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : ViewModel() {

    var userCategories by mutableStateOf<Map<String, Map<String, Any>>>(emptyMap())
    var userCoins by mutableIntStateOf(0)
    var userHabits by mutableStateOf<List<String>>(emptyList())
    var maxLife by mutableIntStateOf(100)
    var currentLife by mutableIntStateOf(10)
    var lastRelapse by mutableStateOf<Date?>(null)
    var lastDailyReset by mutableStateOf<Date?>(null)
    var lastWeeklyReset by mutableStateOf<Date?>(null)

    private var userListener: ListenerRegistration? = null

    init {
        loadUserData()
        checkPendingResets()
        scheduleDailyReset()
    }

    /**
     * Cleans up the listener when the ViewModel is destroyed
     */
    override fun onCleared() {
        userListener?.remove()
        super.onCleared()
    }

    /**
     * Loads the user's data from Firestore
     * @throws Exception if there is an error loading
     */
    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return

        userListener?.remove()

        userListener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("RewardsViewModel", "Error listening to user data", error)
                    return@addSnapshotListener
                }

                snapshot?.let { doc ->
                    val newCompletedHabits = (doc["bhabits"] as? List<String>) ?: emptyList()

                    if (newCompletedHabits != userHabits) {
                        userHabits = newCompletedHabits
                    }

                    userCoins = (doc["coins"] as? Number)?.toInt() ?: 0
                    userCategories =
                        (doc["categories"] as? Map<String, Map<String, Any>>) ?: emptyMap()
                    maxLife = (doc["maxLife"] as? Number)?.toInt() ?: 100
                    currentLife = (doc["currentLife"] as? Number)?.toInt() ?: 10
                    lastRelapse = doc["lastRelapse"] as? Date
                }
            }
    }

    /**
     * Toggles the completion status of a habit
     * @param habit The habit to toggle
     * @param isCompleted The new completion status
     * @throws Exception if there is an error updating the habit
     */
    fun toggleBadHabitCompleted(habit: BadHabit, isCompleted: Boolean) {
        val currentDate = Calendar.getInstance().time

        viewModelScope.launch {
            try {
                firestore.collection("bhabits").document(habit.id)
                    .update("completed", isCompleted, "lastRelapse", currentDate)
                    .await()

                if (isCompleted) {
                    processBadHabitCompletion(habit)
                } else {
                    processBadHabitUncompletion(habit)
                }
            } catch (e: Exception) {
                Log.e("RewardsViewModel", "Error updating habit completion", e)
            }
        }
    }

    /**
     * Toggles the completion status of a habit
     * @param habit The habit to toggle
     * @param isCompleted The new completion status
     * @throws Exception if there is an error updating the habit
     */
    fun toggleGoodHabitCompleted(habit: GoodHabit, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                firestore.collection("ghabits").document(habit.id)
                    .update("completed", isCompleted)
                    .await()

                if (isCompleted) {
                    processGoodHabitCompletion(habit)
                } else {
                    processGoodHabitUncompletion(habit)
                }
            } catch (e: Exception) {
                Log.e("RewardsViewModel", "Error updating habit completion", e)
            }
        }
    }

    /**
     * Processes the completion of a good habit
     * @param habit The habit to process
     */
    private suspend fun processGoodHabitCompletion(habit: GoodHabit) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(userId)

        firestore.runTransaction { transaction ->
            val userDoc = transaction.get(userRef)
            val categories =
                (userDoc["categories"] as? Map<String, Map<String, Any>>)?.toMutableMap()
                    ?: mutableMapOf()

            val categoryPath = habit.category?.id ?: return@runTransaction
            val categoryId = categoryPath.split("/").lastOrNull() ?: return@runTransaction

            val categoryData =
                categories[categoryId]?.toMutableMap() ?: createDefaultCategory().toMutableMap()

            val currentExp = (categoryData["currentExp"] as? Number)?.toInt() ?: 0
            val currentLevel = (categoryData["level"] as? Number)?.toInt() ?: 1
            var remainingExp = currentExp + habit.xpReward
            var newLevel = currentLevel
            var neededExp =
                (categoryData["neededExp"] as? Number)?.toInt() ?: calculateNextLevelExp(
                    currentLevel
                )

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

            val completedHabits =
                (userDoc["ghabits"] as? List<String>)?.toMutableList() ?: mutableListOf()
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

    /**
     * Processes the uncompletion of a good habit
     * @param habit The habit to process
     */
    private suspend fun processGoodHabitUncompletion(habit: GoodHabit) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(userId)

        firestore.runTransaction { transaction ->
            val userDoc = transaction.get(userRef)
            val categories =
                (userDoc["categories"] as? Map<String, Map<String, Any>>)?.toMutableMap()
                    ?: return@runTransaction

            val categoryPath = habit.category?.id ?: return@runTransaction
            val categoryId = categoryPath.split("/").lastOrNull() ?: return@runTransaction
            val categoryData = categories[categoryId]?.toMutableMap() ?: return@runTransaction

            val currentExp = (categoryData["currentExp"] as? Number)?.toInt() ?: 0
            var currentLevel = (categoryData["level"] as? Number)?.toInt() ?: 1
            var remainingExp = max(0, currentExp - habit.xpReward)
            var neededExp =
                (categoryData["neededExp"] as? Number)?.toInt() ?: calculateNextLevelExp(
                    currentLevel
                )

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

            val completedHabits =
                (userDoc["ghabits"] as? List<String>)?.toMutableList() ?: mutableListOf()
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

    /**
     * Processes the completion of a bad habit
     * @param habit The habit to process
     * @throws Exception if there is an error processing the habit
     */
    private suspend fun processBadHabitCompletion(habit: BadHabit) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(userId)

        firestore.runTransaction { transaction ->
            val userDoc = transaction.get(userRef)

            val currentLife = (userDoc["currentLife"] as? Number)?.toInt() ?: 0
            val newLife = max(0, currentLife - habit.lifeLoss)
            val lastRelapse = FieldValue.serverTimestamp()

            val completedHabits =
                (userDoc["bhabits"] as? List<String>)?.toMutableList() ?: mutableListOf()
            if (!completedHabits.contains(habit.id)) {
                completedHabits.add(habit.id)
            }

            transaction.update(
                userRef,
                mapOf(
                    "bhabits" to completedHabits,
                    "currentLife" to newLife,
                    "lastRelapse" to lastRelapse
                )
            )
            val habitRef = firestore.collection("bhabits").document(habit.id)
            transaction.update(habitRef, "completed", true)
        }.await()
    }

    /**
     * Processes the uncompletion of a bad habit
     * @param habit The habit to process
     */
    private suspend fun processBadHabitUncompletion(habit: BadHabit) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(userId)

        firestore.runTransaction { transaction ->
            val userDoc = transaction.get(userRef)

            val currentLife = (userDoc["currentLife"] as? Number)?.toInt() ?: 0
            val maxLife = (userDoc["maxLife"] as? Number)?.toInt() ?: 100
            val newLife = min(maxLife, currentLife + habit.lifeLoss)

            val completedHabits =
                (userDoc["bhabits"] as? List<String>)?.toMutableList() ?: mutableListOf()
            completedHabits.remove(habit.id)

            transaction.update(
                userRef,
                mapOf(
                    "bhabits" to completedHabits,
                    "currentLife" to newLife
                )
            )
        }.await()
    }

    /**
     * Calculates the next level experience for a given level
     * @param currentLevel The current level
     * @return The experience needed to reach the next level
     */
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

    /**
     * Schedules a daily reset for the user
     */
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

                withContext(Dispatchers.IO) {
                    executeDailyReset()
                    if (now.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                        executeWeeklyReset()
                    }
                }
            }
        }
    }

    /**
     * Checks if there are pending resets and executes them if needed
     */
    private fun checkPendingResets() {
        viewModelScope.launch {
            println("Checking pending resets")
            val userId = auth.currentUser?.uid ?: return@launch
            val userRef = firestore.collection("users").document(userId)

            try {
                val userDoc = userRef.get().await()
                lastDailyReset = userDoc.getTimestamp("lastDailyReset")?.toDate()
                lastWeeklyReset = userDoc.getTimestamp("lastWeeklyReset")?.toDate()
                println("Last daily reset: $lastDailyReset")
                println("Last weekly reset: $lastWeeklyReset")

                val now = Date()

                if (lastDailyReset == null || !isSameDay(lastDailyReset!!, now)) {
                    executeDailyReset()
                    userRef.update("lastDailyReset", Timestamp.now()).await()
                }

                val dayOfWeek = Calendar.getInstance().apply { time = now }.get(Calendar.DAY_OF_WEEK)

                if (lastWeeklyReset == null ||
                    !isSameWeek(lastWeeklyReset!!, now)
                    && dayOfWeek == Calendar.MONDAY
                ) {
                    println("Executing weekly reset")
                    executeWeeklyReset()
                }
            } catch (e: Exception) {
                Log.e("Reset", "Error checking pending resets", e)
            }
        }
    }

    /**
     * Executes a daily passive rewards for the user
     * @throws Exception if there is an error processing the rewards
     */
    private suspend fun executeDailyPassiveRewards() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(userId)

        try {
            val bhabitsSnapshot = firestore.collection("bhabits")
                .whereEqualTo("userId", userId)
                .whereEqualTo("completed", false)
                .get()
                .await()

            if (bhabitsSnapshot.isEmpty) {
                Log.d("PassiveRewards", "No habits eligible for passive rewards")
                return
            }

            val categoryRewards = mutableMapOf<String, Pair<Int, Int>>()

            bhabitsSnapshot.documents.forEach { doc ->
                try {
                    val habit = doc.toObject(BadHabit::class.java) ?: return@forEach
                    val categoryId = habit.category?.id?.split("/")?.lastOrNull() ?: return@forEach

                    val passiveCoins = habit.coinReward
                    val passiveXp = habit.xpReward

                    val current = categoryRewards[categoryId] ?: (0 to 0)
                    categoryRewards[categoryId] = current.copy(
                        first = current.first + passiveCoins,
                        second = current.second + passiveXp
                    )
                } catch (e: Exception) {
                    Log.e("PassiveRewards", "Error processing habit ${doc.id}", e)
                }
            }

            if (categoryRewards.isNotEmpty()) {
                firestore.runTransaction { transaction ->
                    val updatedUserDoc = transaction.get(userRef)
                    val categories =
                        (updatedUserDoc["categories"] as? Map<String, Map<String, Any>>)?.toMutableMap()
                            ?: mutableMapOf()

                    categoryRewards.forEach { (categoryId, rewards) ->
                        val categoryData = categories[categoryId]?.toMutableMap()
                            ?: createDefaultCategory().toMutableMap()

                        var currentExp = (categoryData["currentExp"] as? Number)?.toInt() ?: 0
                        var currentLevel = (categoryData["level"] as? Number)?.toInt() ?: 1

                        currentExp += rewards.second

                        var neededExp = (categoryData["neededExp"] as? Number)?.toInt()
                            ?: calculateNextLevelExp(currentLevel)

                        while (currentExp >= neededExp && currentLevel < 10) {
                            currentExp -= neededExp
                            currentLevel++
                            neededExp = calculateNextLevelExp(currentLevel)
                        }

                        categoryData.apply {
                            put("currentExp", currentExp)
                            put("level", currentLevel)
                            put("neededExp", neededExp)
                        }
                        categories[categoryId] = categoryData
                    }

                    val totalCoins = categoryRewards.values.sumOf { it.first }
                    val currentUserCoins = (updatedUserDoc["coins"] as? Number)?.toInt() ?: 0
                    val newCoins = currentUserCoins + totalCoins

                    transaction.update(
                        userRef,
                        mapOf(
                            "categories" to categories,
                            "coins" to newCoins,
                            "lastPassiveReward" to FieldValue.serverTimestamp()
                        )
                    )
                }.await()

                Log.d(
                    "PassiveRewards",
                    "Applied passive rewards to ${categoryRewards.size} categories"
                )
            }
        } catch (e: Exception) {
            Log.e("PassiveRewards", "Error processing daily passive rewards", e)
        }
    }

    /**
     * Executes a daily reset for the user
     * @throws Exception if there is an error executing the reset
     */
    private suspend fun executeDailyReset() {
        val userId = auth.currentUser?.uid ?: return
        try {
            val ghabitsSnapshot = firestore.collection("ghabits")
                .whereEqualTo("userId", userId)
                .whereEqualTo("completed", true)
                .get()
                .await()

            val bhabitsSnapshot = firestore.collection("bhabits")
                .whereEqualTo("userId", userId)
                .whereEqualTo("completed", true)
                .get()
                .await()

            val gbatch = firestore.batch()
            ghabitsSnapshot.documents.forEach { doc ->
                gbatch.update(doc.reference, "completed", false)
            }

            val bbatch = firestore.batch()
            bhabitsSnapshot.documents.forEach { doc ->
                bbatch.update(doc.reference, "completed", false)
            }

            gbatch.commit().await()
            bbatch.commit().await()
            executeDailyPassiveRewards()
            Log.d("Reset", "Daily reset executed")
        } catch (e: Exception) {
            Log.e("Reset", "Error in daily reset", e)
        }
    }

    /**
     * Executes a weekly reset for the user
     * @throws Exception if there is an error executing the reset
     */
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

    /**
     * Creates or resets the default categories
     */
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

    /**
     * Creates a default category for a new user
     * @return The default data for a category
     */
    private fun createDefaultCategory(): Map<String, Any> {
        return mapOf(
            "level" to 1,
            "currentExp" to 0,
            "neededExp" to 150
        )
    }

    /**
     * Checks if two dates are in the same day
     * @param date1 The first date
     * @param date2 The second date
     * @return True if the dates are in the same day, false otherwise
     */
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Checks if two dates are in the same week
     * @param date1 The first date
     * @param date2 The second date
     * @return True if the dates are in the same week, false otherwise
     */
    private fun isSameWeek(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
    }
}