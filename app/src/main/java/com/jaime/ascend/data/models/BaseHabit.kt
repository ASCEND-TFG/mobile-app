package com.jaime.ascend.data.models

import com.google.firebase.firestore.DocumentReference

sealed class BaseHabit(
    open val id: String = "",
    //open val name: Map<String, String> = emptyMap(),
    //open val description: Map<String, String> = emptyMap(),
    open val categoryRef: DocumentReference? = null,
    open val templateRef: DocumentReference? = null,
    //open val icon: String = ""
)