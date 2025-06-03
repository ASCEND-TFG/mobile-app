package com.jaime.ascend.utils

import androidx.annotation.StringRes
import com.jaime.ascend.R

/**
 * Represents a difficulty level.
 * @param xpValue The experience points value.
 * @param coinValue The coin value.
 * @param difficultyName The name of the difficulty.
 * @param lifeLoss The life loss value.
 * @param labelRes The resource ID of the difficulty label.
 * @author Jaime Martínez Fernández
 */
enum class Difficulty(
    val xpValue: Int,
    val coinValue: Int,
    val difficultyName: String,
    val lifeLoss: Int,
    @StringRes val labelRes: Int,
) {
    EASY(
        xpValue = 30,
        coinValue = 5,
        difficultyName = "EASY",
        lifeLoss = 10,
        labelRes = R.string.easy
    ),
    MEDIUM(
        xpValue = 70,
        coinValue = 15,
        difficultyName = "MEDIUM",
        lifeLoss = 25,
        labelRes = R.string.medium
    ),
    HARD(
        xpValue = 150,
        coinValue = 30,
        difficultyName = "HARD",
        lifeLoss = 50,
        labelRes = R.string.hard
    )
}