package com.jaime.ascend.data.models

import com.google.firebase.firestore.DocumentReference

data class BadHabit(
    override val id: String = "",
    override val name: Map<String, String> = emptyMap(),
    override val description: Map<String, String> = emptyMap(),
    override val categoryRef: DocumentReference? = null,
    override val icon: String = "",
    val streak: Int = 0,
    val healthPenalty: Int = 0
) : BaseHabit(id, name, description, categoryRef, icon)