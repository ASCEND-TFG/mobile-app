package com.jaime.ascend.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.tasks.await

class FriendRequestRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val functions: FirebaseFunctions,
    private val messaging: FirebaseMessaging
) {

    fun updateToken(token: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(currentUserId)
            .update("fcmToken", token)
    }

    suspend fun sendFriendRequest(targetUserId: String, username: String): Boolean {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return false
            firestore.collection("users").document(targetUserId)
                .update("pendingRequests", FieldValue.arrayUnion(currentUserId))
                .await()
            sendFriendRequestNotification(currentUserId, targetUserId, username)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun acceptFriendRequest(senderUserId: String, currentUsername: String): Boolean {
        val currentUserId = auth.currentUser?.uid ?: return false

        return try {
            // 1. Actualizar amistades en ambos usuarios
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

            // 2. Enviar notificación de aceptación
            sendRequestAcceptedNotification(currentUserId, senderUserId, currentUsername)

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun saveFCMToken(token: String? = null) {
        val userId = auth.currentUser?.uid ?: return
        val tokenToSave = token ?: messaging.token.await()

        try {
            firestore.collection("users").document(userId)
                .update("fcmToken", tokenToSave)
                .await()
        } catch (e: Exception) {
            // Si el documento no existe
            firestore.collection("users").document(userId)
                .set(mapOf("fcmToken" to tokenToSave))
                .await()
        }
    }

    private suspend fun sendFriendRequestNotification(
        senderId: String,
        receiverId: String,
        senderUsername: String
    ) {
        try {
            val receiverDoc = firestore.collection("users").document(receiverId).get().await()
            val fcmToken = receiverDoc.getString("fcmToken") ?: return

            val data = mapOf(
                "token" to fcmToken,
                "title" to "Nueva solicitud de amistad",
                "body" to "$senderUsername quiere ser tu amigo",
                "data" to mapOf(
                    "type" to "friends",
                    "senderId" to senderId
                )
            )

            functions.getHttpsCallable("sendNotification").call(data).await()
        } catch (e: Exception) {
            Log.e("FCM_ERROR", "Error sending notification", e)
        }
    }

    private suspend fun sendRequestAcceptedNotification(
        accepterId: String,
        senderId: String,
        accepterUsername: String
    ) {
        try {
            val senderDoc = firestore.collection("users").document(senderId).get().await()
            val fcmToken = senderDoc.getString("fcmToken") ?: return

            val data = mapOf(
                "to" to fcmToken,
                "notification" to mapOf(
                    "title" to "Solicitud aceptada",
                    "body" to "$accepterUsername ha aceptado tu solicitud"
                ),
                "data" to mapOf(
                    "type" to "friends",
                    "accepterId" to accepterId
                )
            )

            functions.getHttpsCallable("sendNotification").call(data).await()
        } catch (e: Exception) {
            // Log error
        }
    }


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

    /*suspend fun sendFriendRequest(targetUserId: String): Boolean {
        val currentUserId = getCurrentUserId() ?: return false
        return try {

            val senderUsername = {
                firestore.collection("users").document(targetUserId).get().await()
                    .getString("username") ?: ""
            }

            firestore.collection("users").document(targetUserId)
                .update("pendingRequests", FieldValue.arrayUnion(currentUserId))
                .await()
            sendFriendRequestNotification(currentUserId, targetUserId, senderUsername())
            true
        } catch (e: Exception) {
            false
        }
    }*/

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun getUserDocument(userId: String) =
        firestore.collection("users").document(userId).get()

    suspend fun getUserData(userId: String) =
        firestore.collection("users").document(userId).get()

    /*suspend fun acceptFriendRequest(currentUserId: String, senderUserId: String) {
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
    }*/

    suspend fun rejectFriendRequest(currentUserId: String, senderUserId: String) {
        firestore.collection("users").document(currentUserId)
            .update("pendingRequests", FieldValue.arrayRemove(senderUserId))
            .await()
    }

    private suspend fun getUsername(userId: String): String {
        return firestore.collection("users").document(userId).get().await()
            .getString("username") ?: ""
    }

}