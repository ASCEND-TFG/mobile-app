package com.jaime.ascend.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

/**
 * Repository for friend requests.
 * @param firestore The Firebase Firestore instance.
 * @param auth The Firebase Authentication instance.
 * @param functions The Firebase Functions instance.
 * @param messaging The Firebase Messaging instance.
 * @author Jaime Martínez Fernández
 */
class FriendRequestRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val functions: FirebaseFunctions,
    private val messaging: FirebaseMessaging
) {

    /**
     * Sends a friend request to the target user.
     * @param targetUserId The ID of the target user.
     * @return A boolean indicating if the request was sent successfully.
     */
    suspend fun sendFriendRequest(targetUserId: String, username: String): Boolean {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return false
            firestore.collection("users").document(targetUserId)
                .update("pendingRequests", FieldValue.arrayUnion(currentUserId))
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Accepts a friend request from the sender user.
     * @param senderUserId The ID of the sender user.
     * @return A boolean indicating if the request was accepted successfully.
     */
    suspend fun acceptFriendRequest(senderUserId: String, currentUsername: String): Boolean {
        val currentUserId = auth.currentUser?.uid ?: return false

        return try {
            val batch = firestore.batch()
            val currentUserRef = firestore.collection("users").document(currentUserId)
            val senderUserRef = firestore.collection("users").document(senderUserId)

            batch.update(
                currentUserRef,
                "friends", FieldValue.arrayUnion(senderUserId),
                "pendingRequests", FieldValue.arrayRemove(senderUserId)
            )

            batch.update(
                senderUserRef,
                "friends", FieldValue.arrayUnion(currentUserId)
            )

            batch.commit().await()

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Searches for a user by their username.
     * @param username The username to search for.
     * @return A map containing the user's data if found, null otherwise.
     * @throws Exception if the search fails.
     */
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

    /**
     * Gets the current user's ID.
     * @return The current user's ID.
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Gets the current user's data.
     * @return A map containing the current user's data.
     */
     fun getUserData(userId: String) =
        firestore.collection("users").document(userId).get()

    /**
     * Rejects a friend request from the sender user.
     * @param currentUserId The ID of the current user.
     * @param senderUserId The ID of the sender user.
     * @throws Exception if the request could not be rejected.
     */
    suspend fun rejectFriendRequest(currentUserId: String, senderUserId: String) {
        firestore.collection("users").document(currentUserId)
            .update("pendingRequests", FieldValue.arrayRemove(senderUserId))
            .await()
    }
}