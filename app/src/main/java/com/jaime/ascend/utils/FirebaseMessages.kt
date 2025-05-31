package com.jaime.ascend.utils

import com.jaime.ascend.data.repository.FriendRequestRepository
import com.jaime.ascend.ui.screens.MainActivity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.jaime.ascend.R

class FirebaseMessages : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FirebaseMessages", "From: ${remoteMessage.from}")
        Log.d("FirebaseMessages", "Data: ${remoteMessage.data}")

        remoteMessage.notification?.let { notification ->
            Log.d("FirebaseMessages", "Notification Title: ${notification.title}")
            Log.d("FirebaseMessages", "Notification Body: ${notification.body}")
            showNotification(
                notification.title ?: "",
                notification.body ?: "",
                remoteMessage.data
            )
        } ?: run {
            Log.d("FirebaseMessages", "No notification payload, data only message")
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String>,
        type: String = "friends"
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            data.forEach { (key, value) -> putExtra(key, value) }
            putExtra("notification_type", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = when(type) {
            "friends" -> getString(R.string.friends_channel_id)
            else -> getString(R.string.friends_channel_id)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ascendlogo_removebg)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }


    private val friendRepository by lazy {
        FriendRequestRepository(
            firestore = FirebaseFirestore.getInstance(),
            auth = FirebaseAuth.getInstance(),
            functions = FirebaseFunctions.getInstance(),
            messaging = FirebaseMessaging.getInstance()
        )
    }
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onNewToken(token: String) {
        Log.d("FirebaseMessages", "Refreshed token: $token")

        sendRegistrationToServer(token)
    }

    fun sendRegistrationToServer(token: String) {
        friendRepository.updateToken(token)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal para notificaciones de amistades
            val friendsChannel = NotificationChannel(
                getString(R.string.friends_channel_id),
                getString(R.string.friends_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.friends_channel_description)
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 100, 200)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(friendsChannel)
        }
    }

    enum class NotificationType(val channelId: String) {
        FRIEND_REQUEST("friends_channel"),
        MESSAGE("messages_channel"),
        SYSTEM("system_channel")
    }
}