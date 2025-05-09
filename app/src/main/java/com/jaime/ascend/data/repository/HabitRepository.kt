package com.jaime.ascend.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jaime.ascend.data.models.BadHabit
import com.jaime.ascend.data.models.Difficulty
import com.jaime.ascend.data.models.GoodHabit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Locale

class HabitRepository(
    private val firestore: FirebaseFirestore,
) {
    private val habitsCollection = firestore.collection("ghabits")
    private val usersCollection = firestore.collection("users")

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getUserGoodHabitsRealTime(userId: String): Flow<List<GoodHabit>> = callbackFlow {
        // Query
        val query = habitsCollection
            .whereEqualTo("userId", userId)
            .orderBy("name", Query.Direction.ASCENDING)
            .limitToLast(50)

        // Send query and get results
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val habits = snapshot?.documents?.mapNotNull { doc ->
                Log.i("TAG", "getUserGoodHabitsRealTime: $doc")

                doc.toObject(GoodHabit::class.java)?.copy(
                    id = doc.id,
                )

            } ?: emptyList()

            trySend(habits)
        }

        awaitClose { listener.remove() }
    }


    suspend fun createGoodHabit(
        name: Map<String, String>,
        description: Map<String, String>,
        icon: String,
        categoryId: String,
        difficulty: Difficulty,
        reminderTime: String?,
        days: List<Int>,
        userId: String,
    ): Result<String> = try {
        val categoryRef = firestore.collection("categories").document(categoryId)
        Log.i("TAG", "createGoodHabit: $categoryRef")

        val habit = GoodHabit(
            name = name,
            description = description,
            icon = icon,
            categoryRef = categoryRef,
            xpReward = difficulty.xpValue,
            coinReward = difficulty.coinValue,
            difficulty = difficulty,
            reminderTime = reminderTime,
            days = days,
            userId = userId
        )

        val docRef = habitsCollection.add(habit).await()

        // Update user's habits array
        usersCollection.document(userId)
            .update("ghabits", FieldValue.arrayUnion(docRef.id)).await()

        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateUserXPAndCoins(
        userId: String,
        xpToAdd: Int,
        coinsToAdd: Int,
    ): Result<Unit> = try {
        usersCollection.document(userId).update(
            mapOf(
                "currentExp" to FieldValue.increment(xpToAdd.toDouble()),
                "coins" to FieldValue.increment(coinsToAdd.toDouble())
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getGoodHabitsByCategory(categoryId: String, languageCode: String = Locale.getDefault().language): List<GoodHabit> {
        return try {
            val categoryRef = firestore.collection("categories").document(categoryId)
            // Get the category document and extract the name map
            val categorySnapshot = categoryRef.get().await()
            val categoryName = categorySnapshot.get("id") as String

            // Get all habits for this category
            val result = firestore.collection("ghabit_templates")
                .whereEqualTo("category", categoryRef)
                .get()
                .await()

            // Convert to GoodHabit objects with the localized category name
            result.map { document ->
                document.toObject(GoodHabit::class.java).copy(
                    id = document.id,
                    categoryName = categoryName
                )
            }
        } catch (e: Exception) {
            Log.e("TAG", "getGoodHabitsByCategory: ${e.message}")
            emptyList()
        }
    }

    suspend fun getBadHabitsByCategory(categoryId: String): List<BadHabit> {
        return firestore.collection("bhabit_templates").whereEqualTo("categoryId", categoryId).get()
            .await().toObjects(BadHabit::class.java)
    }

    suspend fun searchGoodHabits(query: String): List<GoodHabit> {
        return firestore.collection("ghabit_templates").get().await()
            .toObjects(GoodHabit::class.java)
            .filter { habit ->
                habit.name.any { (_, value) ->
                    value.contains(query, ignoreCase = true)
                } ||
                        habit.description.any { (_, value) ->
                            value.contains(query, ignoreCase = true)
                        }
            }
    }

    suspend fun searchBadHabits(query: String): List<BadHabit> {
        return firestore.collection("bhabit_templates").get().await()
            .toObjects(BadHabit::class.java)
            .filter { it.name.values.any { name -> name.contains(query, ignoreCase = true) } }
    }
}