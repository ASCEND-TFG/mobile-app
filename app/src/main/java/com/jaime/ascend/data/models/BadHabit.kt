package com.jaime.ascend.data.models

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ServerTimestamp
import com.jaime.ascend.utils.Difficulty
import java.util.Date

/**
 * BadHabit data class.
 * @author Jaime Martínez Fernández
 * @param id The unique identifier of the bad habit.
 * @param category The category of the bad habit.
 * @param template The template of the bad habit.
 * @param completed Whether the bad habit is completed or not.
 * @param coinReward The coin reward of the bad habit.
 * @param createdAt The date when the bad habit was created.
 * @param difficulty The difficulty of the bad habit.
 * @param lifeLoss The life loss of the bad habit.
 * @param lastRelapse The last time when the bad habit was completed.
 * @param xpReward The XP reward of the bad habit.
 * @param userId The ID of the user who owns the bad habit.
 * @param currentStreak The current streak of the bad habit.
 */
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
    /**
     * Calculates and formats the current streak of the bad habit.
     * @return The formatted current streak.
     */
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

    /**
     * Converts the bad habit to a map.
     * @return The map representation of the bad habit.
     */
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

