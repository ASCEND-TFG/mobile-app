package com.jaime.ascend.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun getUserDocument(userId: String): Task<DocumentSnapshot> {
        return firestore.collection("users").document(userId).get()
    }

    suspend fun getUserData(userId: String): Task<DocumentSnapshot> {
        return firestore.collection("users").document(userId).get()
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

}