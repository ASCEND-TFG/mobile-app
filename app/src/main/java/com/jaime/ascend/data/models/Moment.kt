package com.jaime.ascend.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Moment(
    val id: String = "",
    val name: Map<String, String> = emptyMap(),
    val description: Map<String, String> = emptyMap(),
    val icon: String = "",
    val unlockInDays: Int? = null,
    val reward: Int = 0,
    val bought: Boolean = false,
    val price: Int = 0,
    val isOwned: Boolean = false
) {
    fun getName(language: String): String {
        return name[language] ?: name["en"] ?: ""
    }

    fun getDescription(language: String): String {
        return description[language] ?: description["en"] ?: ""
    }
}