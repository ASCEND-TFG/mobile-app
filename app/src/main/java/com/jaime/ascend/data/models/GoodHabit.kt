package com.jaime.ascend.data.models

import com.google.firebase.firestore.DocumentReference
import com.jaime.ascend.utils.Difficulty
import java.util.Date

/**
 * GoodHabit data class.
 * @author Jaime Martínez Fernández
 * @param id The unique identifier of the good habit.
 * @param category The category of the good habit.
 * @param template The template of the good habit.
 * @param completed Whether the good habit is completed or not.
 * @param coinReward The coin reward of the good habit.
 * @param createdAt The date when the good habit was created.
 * @param days The days of the week when the good habit need to be completed.
 * @param difficulty The difficulty of the good habit.
 * @param xpReward The XP reward of the good habit.
 * @param userId The ID of the user who owns the good habit.
 * @param reminderTime The time when the good habit reminder should be triggered.
 */
data class GoodHabit(
    val id: String = "",
    val category: DocumentReference? = null,
    val template: DocumentReference? = null,
    var completed: Boolean = false,
    val coinReward: Int = 0,
    val createdAt: Date = Date(),
    val days: List<Int> = emptyList(),
    val difficulty: Difficulty = Difficulty.EASY,
    val xpReward: Int = 0,
    val userId: String = "",
    val reminderTime: String? = null,
)