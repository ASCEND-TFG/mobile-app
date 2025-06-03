package com.jaime.ascend.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Repository for the user.
 * @param firestore The Firebase Firestore instance.
 * @param auth The Firebase Authentication instance.
 */
class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    /**
     * Gets the user document.
     * @param userId The ID of the user.
     * @return The user document.
     */
     fun getUserData(userId: String): Task<DocumentSnapshot> {
        return firestore.collection("users").document(userId).get()
    }

    /**
     * Gets the current user ID.
     * @return The current user ID.
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

}