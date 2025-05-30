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

    suspend fun getUserDocument(userId: String) =
        firestore.collection("users").document(userId).get()

    suspend fun getUserData(userId: String) =
        firestore.collection("users").document(userId).get()

    suspend fun acceptFriendRequest(currentUserId: String, senderUserId: String) {
        val batch = firestore.batch()

        // Añade a amigos de ambos usuarios
        val currentUserRef = firestore.collection("users").document(currentUserId)
        val senderUserRef = firestore.collection("users").document(senderUserId)

        batch.update(currentUserRef,
            "friends", FieldValue.arrayUnion(senderUserId),
            "pendingRequests", FieldValue.arrayRemove(senderUserId)
        )

        batch.update(senderUserRef,
            "friends", FieldValue.arrayUnion(currentUserId)
        )

        batch.commit().await()
    }

    suspend fun rejectFriendRequest(currentUserId: String, senderUserId: String) {
        firestore.collection("users").document(currentUserId)
            .update("pendingRequests", FieldValue.arrayRemove(senderUserId))
            .await()
    }

}