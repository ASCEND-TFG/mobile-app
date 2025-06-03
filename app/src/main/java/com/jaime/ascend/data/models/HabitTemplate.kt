package com.jaime.ascend.data.models

import com.google.firebase.firestore.DocumentReference
import java.util.Locale


/**
 * HabitTemplate data class.
 * @author Jaime Martínez Fernández
 * @param id The unique identifier of the habit template.
 * @param category The category of the habit template.
 * @param icon The icon of the habit template.
 * @param name The name of the habit template.
 * @param description The description of the habit template.
 */
data class HabitTemplate(
    val id: String = "",
    val category: DocumentReference? = null,
    val icon: String = "",
    val name: Map<String, String> = emptyMap(),
    val description: Map<String, String> = emptyMap()
) {
    fun getName(locale: Locale): String {
        return name[locale.language] ?: name["en"] ?: ""
    }

    fun getDescription(locale: Locale): String {
        return description[locale.language] ?: description["en"] ?: ""
    }
}