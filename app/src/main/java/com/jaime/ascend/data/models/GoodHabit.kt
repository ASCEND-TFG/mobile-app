package com.jaime.ascend.data.models

import android.util.Log
import androidx.annotation.StringRes
import com.google.firebase.firestore.DocumentReference
import com.jaime.ascend.R
import kotlinx.coroutines.tasks.await
import java.util.*

data class GoodHabit(
    val id: String = "",
    val category: DocumentReference? = null,
    val template: DocumentReference? = null,
    val checked: Boolean = false,
    val coinReward: Int = 0,
    val createdAt: Date = Date(),
    val days: List<Int> = emptyList(),
    val difficulty: Difficulty = Difficulty.EASY,
    val xpReward: Int = 0,
    val userId: String = "",
) {
    @Transient var resolvedTemplate: HabitTemplate? = null
    @Transient var resolvedCategory: Category? = null

    suspend fun resolveTemplate(): HabitTemplate? {
        return if (resolvedTemplate != null) {
            resolvedTemplate  // Return cached version
        } else {
            try {
                val template = template?.get()?.await()?.toObject(HabitTemplate::class.java)
                resolvedTemplate = template
                template
            } catch (e: Exception) {
                Log.e("HABIT", "Error resolving template", e)
                null
            }
        }
    }

    suspend fun resolverCategory(): Category? {
        return if (resolvedCategory != null) {
            resolvedCategory  // Return cached version
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
            "checked" to checked,
            "coinReward" to coinReward,
            "createdAt" to createdAt,
            "days" to days,
            "difficulty" to difficulty,
            "xpReward" to xpReward,
            "userId" to userId
        )
    }
}

enum class Difficulty(
    val xpValue: Int,
    val coinValue: Int,
    val difficultyName: String,
    @StringRes val labelRes: Int,
) {
    EASY(xpValue = 30, coinValue = 5, difficultyName = "EASY", labelRes = R.string.easy),
    MEDIUM(xpValue = 70, coinValue = 15, difficultyName = "MEDIUM", labelRes = R.string.medium),
    HARD(xpValue = 150, coinValue = 30, difficultyName = "HARD", labelRes = R.string.hard)
}