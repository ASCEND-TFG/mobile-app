package com.jaime.ascend.data.models

import androidx.annotation.StringRes
import com.google.firebase.firestore.DocumentReference
import com.jaime.ascend.R
import java.util.*

data class GoodHabit(
    override val id: String = "",
    override val name: Map<String, String> = emptyMap(),
    override val description: Map<String, String> = emptyMap(),
    override val icon: String = "",
    override val categoryRef: DocumentReference? = null,
    val xpReward: Int = 0,
    val coinReward: Int = 0,
    val difficulty: Difficulty = Difficulty.EASY,
    val reminderTime: String? = null,
    val days: List<Int> = emptyList(),
    val checked: Boolean = false,
    val createdAt: Date = Date(),
    val userId: String = "",
    val categoryName: String = ""
) : BaseHabit(id, name, description, null, icon) {
    fun getLocalizedName(languageCode: String = "en"): String {
        return name[languageCode] ?: name["en"] ?: ""
    }

    fun getLocalizedDescription(languageCode: String = "en"): String {
        return description[languageCode] ?: description["en"] ?: ""
    }
}

enum class Difficulty(
    val xpValue: Int,
    val coinValue: Int,
    @StringRes val labelRes: Int
) {
    EASY(xpValue = 30, coinValue = 5, labelRes = R.string.easy),
    MEDIUM(xpValue = 70, coinValue = 15, labelRes = R.string.medium),
    HARD(xpValue = 150, coinValue = 30, labelRes = R.string.hard)
}

fun GoodHabit.getExtraData(): Map<String, Any?> {
    return mapOf(
        "xpReward" to this.xpReward,
        "coinReward" to this.coinReward,
        "difficulty" to this.difficulty,
        "reminderTime" to this.reminderTime,
        "days" to this.days,
        "categoryRef" to this.categoryRef
    )
}