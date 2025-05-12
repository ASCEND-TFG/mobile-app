package com.jaime.ascend.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

object IconMapper {
    fun getCategoryIcon(iconName: String?): ImageVector {
        if (iconName == null)
            return Icons.Filled.Category
        else
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

    fun getHabitIcon(iconName: String?): ImageVector {
        if (iconName == null)
            return Icons.Filled.CheckCircle
        else
            return when (iconName.lowercase()) {
                //Career/Studies category
                "notesmaintenance" -> Icons.Filled.Notes
                "feedback" -> Icons.Filled.Feedback

                //Family category
                "callfamilymember" -> Icons.Filled.Call
                "familymealtime" -> Icons.Filled.DinnerDining
                "morninghug" -> Icons.Filled.EmojiEmotions
                "dailycheckin" -> Icons.Filled.Chat
                "familysupporttime" -> Icons.Filled.BackHand
                "teamcleaning" -> Icons.Filled.CleaningServices
                "familywalk" -> Icons.Filled.DirectionsWalk
                "familyvisits" -> Icons.Filled.Group
                "familyshopping" -> Icons.Filled.ShoppingCart
                "dogwalking" -> Icons.Filled.Pets
                "familymovienight" -> Icons.Filled.Movie
                else -> Icons.Filled.CheckCircle
            }
    }
}