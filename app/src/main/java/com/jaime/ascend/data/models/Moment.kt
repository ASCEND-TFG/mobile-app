package com.jaime.ascend.data.models

import kotlinx.serialization.Serializable

/**
 * Moment data class.
 * @author Jaime Martínez Fernández
 * @param id The unique identifier of the moment.
 * @param category The category of the moment.
 * @param icon The icon of the moment.
 * @param name The name of the moment.
 * @param description The description of the moment.
 * @param reward The reward of the moment.
 * @param isOwned Whether the moment is owned or not.
 * @param bought Whether the moment has been bought or not.
 * @param price The price of the moment.
 * @param unlockInDays The number of days required to unlock the moment.
 */
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