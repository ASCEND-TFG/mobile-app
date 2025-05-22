package com.jaime.ascend.data.models

import java.util.Locale

data class Category(
    val id: String = "",
    val icon: String = "",
    val name: Map<String, String> = emptyMap(),
    val description: Map<String, String> = emptyMap(),
    val level: Int = 0,
    val currentExp: Int = 0,
    val neededExp: Int = 0
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "icon" to icon,
            "name" to name,
            "description" to description
        )
    }

    fun getName(locale: Locale): String {
        return name[locale.language] ?: name["en"] ?: ""
    }

    fun getDescription(locale: Locale): String {
        return description[locale.language] ?: description["en"] ?: ""
    }
}