package com.jaime.ascend.data.models

import java.util.Locale

/**
 * Category data class.
 * @author Jaime Martínez Fernández
 * @param id The unique identifier of the category.
 * @param icon The icon of the category.
 * @param name The name of the category.
 * @param description The description of the category.
 * @param level The level of the category.
 * @param currentExp The current experience points of the category.
 * @param neededExp The needed experience points of the category.
*/
data class Category(
    val id: String = "",
    val icon: String = "",
    val name: Map<String, String> = emptyMap(),
    val description: Map<String, String> = emptyMap(),
    val level: Int = 0,
    val currentExp: Int = 0,
    val neededExp: Int = 0
) {
    /**
     * Returns the name of the category in the given locale.
     * @param locale The locale to use for localization.
     */
    fun getName(locale: Locale): String {
        return name[locale.language] ?: name["en"] ?: ""
    }

    /**
     * Returns the description of the category in the given locale.
     * @param locale The locale to use for localization.
     */
    fun getDescription(locale: Locale): String {
        return description[locale.language] ?: description["en"] ?: ""
    }
}