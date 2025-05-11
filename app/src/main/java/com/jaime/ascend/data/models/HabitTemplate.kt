package com.jaime.ascend.data.models

import com.google.firebase.firestore.DocumentReference
import java.util.Locale


data class HabitTemplate(
    val id: String = "",
    val category: DocumentReference? = null,
    val icon: String = "",
    val name: Map<String, String> = emptyMap(),
    val description: Map<String, String> = emptyMap()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "category" to category!!,
            "icon" to icon,
            "name" to name,
            "description" to description
        )
    }

    fun getLocalizedName(locale: Locale): String {
        return name[locale.language] ?: name["en"] ?: ""
    }

    fun getLocalizedDescription(locale: Locale): String {
        return description[locale.language] ?: description["en"] ?: ""
    }
}