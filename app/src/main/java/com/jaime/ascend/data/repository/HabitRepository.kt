package com.jaime.ascend.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.jaime.ascend.data.models.Difficulty
import com.jaime.ascend.data.models.GoodHabit
import com.jaime.ascend.data.models.HabitTemplate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class HabitRepository(private val firestore: FirebaseFirestore) {
    private val habitsCollection = firestore.collection("ghabits")
    private val usersCollection = firestore.collection("users")
    private val templatesCollection = firestore.collection("ghabit_templates")
    private val categoriesCollection = firestore.collection("categories")

    // Current active listeners to avoid duplicates
    private val activeListeners = mutableMapOf<String, ListenerRegistration>()

    suspend fun getHabitById(habitId: String): GoodHabit? {
        return habitsCollection.document(habitId).get().await().toObject(GoodHabit::class.java)
    }

    /* ------------------------- Template Operations ------------------------- */
    suspend fun getTemplateInfo(templateId: String): HabitTemplate? {
        return try {
            val document = templatesCollection.document(templateId).get().await()
            document.toObject(HabitTemplate::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            logError("TEMPLATE", "Error fetching template $templateId", e)
            null
        }
    }

    /* ------------------------- Realtime Habit Flow ------------------------- */
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
                        logError("HABIT_PARSE", "Error parsing habit ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                trySend(habits)
            }

        activeListeners[listenerKey] = listener
        awaitClose { activeListeners.remove(listenerKey)?.remove() }
    }

    /* ------------------------- CRUD Operations ------------------------- */

    suspend fun createGoodHabit(
        userId: String,
        templateId: String,
        categoryId: String,
        days: List<Int>,
        difficulty: Difficulty,
    ): Result<String> {
        return try {
            // Get references
            val templateRef = templatesCollection.document(templateId)
            val categoryRef = categoriesCollection.document(categoryId)

            // Get template data (with null check)
            val template = getTemplateInfo(templateId) ?: return Result.failure(
                IllegalArgumentException("Template not found")
            )

            // Create the new habit with enum values
            val newHabit = GoodHabit(
                userId = userId,
                template = templateRef,
                category = categoryRef,
                days = days,
                difficulty = difficulty,
                xpReward = difficulty.xpValue,
                coinReward = difficulty.coinValue,
                createdAt = Date()
            )

            val docRef = habitsCollection.add(newHabit.toMap()).await()
            Result.success(docRef.id)

        } catch (e: Exception) {
            logError("CREATE_HABIT", "Error creating habit", e)
            Result.failure(e)
        }
    }

    suspend fun getHabitWithTemplate(habitId: String): Pair<GoodHabit, HabitTemplate?> {
        val habit = habitsCollection.document(habitId).get().await()
            .toObject(GoodHabit::class.java) ?: throw Exception("Habit not found")

        val template = habit.resolveTemplate()
        return habit to template
    }

    suspend fun List<GoodHabit>.resolveAllTemplates(): List<Pair<GoodHabit, HabitTemplate?>> {
        // Get all unique template references first
        val templateRefs = this.map { it.template }.distinct()

        // Batch fetch all templates
        val templates = templateRefs.associateWith { ref ->
            try {
                ref?.get()?.await()?.toObject(HabitTemplate::class.java)
            } catch (e: Exception) {
                Log.e("BATCH_TEMPLATE", "Error loading template ${ref?.path}", e)
                null
            }
        }

        // Map back to original habits
        return this.map { habit ->
            habit to templates[habit.template]
        }
    }

    /* ------------------------- Query Operations ------------------------- */
    suspend fun getGoodHabitsByCategory(categoryId: String): List<GoodHabit> {
        return try {
            val categoryRef = categoriesCollection.document(categoryId)
            habitsCollection
                .whereEqualTo("category", categoryRef)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(GoodHabit::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            logError("CATEGORY_HABITS", "Error fetching habits for category $categoryId", e)
            emptyList()
        }
    }

    suspend fun searchGoodHabits(query: String): List<GoodHabit> {
        return try {
            // Firestore doesn't support full-text search, so we search in name/description
            habitsCollection
                .orderBy("name")
                .startAt(query)
                .endAt("$query\uf8ff")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(GoodHabit::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            logError("SEARCH", "Error searching habits", e)
            emptyList()
        }
    }

    /* ------------------------- Helper Functions ------------------------- */
    private fun logError(tag: String, message: String, e: Exception) {
        Log.e(tag, "$message: ${e.message}")
    }

    fun cleanup() {
        activeListeners.values.forEach { it.remove() }
        activeListeners.clear()
    }
}

// Extension function for DocumentReference
suspend fun DocumentReference.awaitWithId(): Pair<String, Map<String, Any>?> =
    suspendCoroutine { continuation ->
        get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    continuation.resume(document.id to document.data)
                } else {
                    continuation.resumeWithException(NoSuchElementException("Document does not exist"))
                }
            } else {
                continuation.resumeWithException(task.exception ?: Exception("Unknown error"))
            }
        }
    }