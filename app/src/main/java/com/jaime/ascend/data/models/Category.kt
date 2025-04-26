package com.jaime.ascend.data.models

data class Category(
    val id: String = "",
    val name: Map<String, String> = emptyMap(),
    val description: Map<String, String> = emptyMap(),
    val icon: String = ""
) {
    fun getName(language: String): String {
        return name[language] ?: name["es"] ?: ""
    }

    fun getDescription(language: String): String {
        return description[language] ?: description["es"] ?: ""
    }

    fun getCategoryIcon(): String {
        return icon
    }
}