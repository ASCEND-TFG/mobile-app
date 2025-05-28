package com.jaime.ascend.data.models

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ServerTimestamp
import com.jaime.ascend.utils.Difficulty
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class BadHabit(
    val id: String = "",
    val category: DocumentReference? = null,
    val template: DocumentReference? = null,
    var completed: Boolean = false,
    val coinReward: Int = 0,
    val createdAt: Date = Date(),
    val difficulty: Difficulty = Difficulty.EASY,
    val lifeLoss: Int = 0,
    @ServerTimestamp
    val lastRelapse: Date? = Date(),
    val xpReward: Int = 0,
    val userId: String = "",
    @Transient var currentStreak: String = "0 min"
) {
    @Transient var resolvedTemplate: HabitTemplate? = null
    @Transient var resolvedCategory: Category? = null

    fun calculateAndFormatCurrentStreak(): String {
        val referenceDate = lastRelapse ?: createdAt
        val diff = System.currentTimeMillis() - referenceDate.time
        val minutes = (diff / (1000 * 60)).toInt()

        return when {
            minutes < 60 -> "$minutes min"
            minutes < 24 * 60 -> "${minutes / 60} h"
            else -> "${minutes / (24 * 60)} d"
        }
    }

    suspend fun resolverCategory(): Category? {
        return if (resolvedCategory != null) {
            resolvedCategory
        } else {
            try {
                val category = category?.get()?.await()?.toObject(Category::class.java)
                resolvedCategory = category
                category
            } catch (e: Exception) {
                Log.e("HABIT", "Error resolving category", e)
                null
            }
        }
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "category" to category!!,
            "template" to template!!,
            "checked" to completed,
            "coinReward" to coinReward,
            "createdAt" to createdAt,
            "lifeLoss" to lifeLoss,
            "difficulty" to difficulty,
            "xpReward" to xpReward,
            "userId" to userId,
        )
    }
}

