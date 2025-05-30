package com.jaime.ascend.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import kotlinx.coroutines.tasks.await

class FriendRequestRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun searchUserByUsername(username: String): Map<String, Any>? {
        return try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("username", username.trim())
                .get()
                .await()

            querySnapshot.documents.firstOrNull()?.data?.also {
                it["documentId"] = querySnapshot.documents.firstOrNull()?.id ?: ""
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun sendFriendRequest(targetUserId: String): Boolean {
        val currentUserId = getCurrentUserId() ?: return false
        return try {
            firestore.collection("users").document(targetUserId)
                .update("pendingRequests", FieldValue.arrayUnion(currentUserId))
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

}