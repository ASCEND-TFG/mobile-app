package com.jaime.ascend.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

object IconMapper {
    fun getCategoryIcon(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            "familyrestroom" -> Icons.Filled.FamilyRestroom
            "handshake" -> Icons.Filled.Handshake
            "school" -> Icons.Filled.School
            "favorite" -> Icons.Filled.Favorite
            "accountbalancewallet" -> Icons.Filled.AccountBalanceWallet
            "psychology" -> Icons.Filled.Psychology
            "fitnesscenter" -> Icons.Filled.FitnessCenter
            "selfimprovement" -> Icons.Filled.SelfImprovement
            else -> Icons.Filled.Category
        }
    }

    fun getHabitIcon(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            "call" -> Icons.Filled.Call
            else -> Icons.Filled.CheckCircle
        }
    }
}