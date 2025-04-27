package com.jaime.ascend.data.models

import com.google.firebase.firestore.DocumentReference

data class GoodHabit(
    override val id: String = "",
    override val name: Map<String, String> = emptyMap(),
    override val description: Map<String, String> = emptyMap(),
    override val icon: String = "",
    val categoryRef: DocumentReference? = null,
    val xpReward: Int = 0,
    val coinReward: Int = 0
) : BaseHabit(id, name, description, "", icon)