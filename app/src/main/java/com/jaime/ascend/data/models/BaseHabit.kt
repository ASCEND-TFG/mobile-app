package com.jaime.ascend.data.models

sealed class BaseHabit(
    open val id: String = "",
    open val name: Map<String, String> = emptyMap(),
    open val description: Map<String, String> = emptyMap(),
    open val categoryId: String = "",
    open val icon: String = ""
) {
    fun getName(language: String): String {
        return name[language] ?: name["es"] ?: ""
    }

    fun getDescription(language: String): String {
        return description[language] ?: description["es"] ?: ""
    }
}