package com.jaime.ascend.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.jaime.ascend.R

/**
 * Receiver for notification broadcasts.
 * @author Jaime Martínez Fernández
 */
class NotificationReceiver : BroadcastReceiver() {
    /**
     * Handles the broadcast intent.
     * @param context The context.
     * @param intent The intent.
     */
    override fun onReceive(context: Context, intent: Intent) {

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            notificationManager.isNotificationPolicyAccessGranted.not()) {
            Log.e("NotifDebug", "El modo No Molestar está activado")
        }

        val habitId = intent.getStringExtra("habitId") ?: run {
            Log.e("NotifDebug", "No se encontró habitId en el Intent")
            return
        }

        val habitName = intent.getStringExtra("habitName") ?: run {
            Log.e("NotifDebug", "No se encontró habitName en el Intent")
            return
        }

        Log.d("NotifDebug", "Preparando notificación para: $habitName ($habitId)")

        try {
            createNotificationChannel(context)
            sendNotification(context, habitId, habitName)
            Log.d("NotifDebug", "Notificación enviada con éxito")
        } catch (e: Exception) {
            Log.e("NotifDebug", "Error al enviar notificación", e)
        }
    }

    /**
     * Creates a notification channel if it doesn't exist.
     * @param context The context.
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

            val channelId = "habit_channel"
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    "Recordatorios de Hábitos",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificaciones para recordatorios de hábitos"
                    enableVibration(true)
                    setShowBadge(true)
                }
                notificationManager.createNotificationChannel(channel)
                Log.d("NotifDebug", "Canal de notificación CREADO")
            } else {
                Log.d("NotifDebug", "Canal de notificación YA EXISTE")
            }
        }
    }

    /**
     * Sends a notification.
     *
     * @param context The context.
     * @param habitId The ID of the habit.
     * @param habitName The name of the habit.
     */
    private fun sendNotification(context: Context, habitId: String, habitName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        val notificationId = habitId.hashCode()

        val notification = NotificationCompat.Builder(context, "habit_channel").apply {
            setSmallIcon(R.drawable.mountain)
            setContentTitle(habitName)
            setContentText(context.getString(R.string.it_is_time_to_complete_this_habit))
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setAutoCancel(true)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setCategory(NotificationCompat.CATEGORY_REMINDER)
        }.build()

        try {
            notificationManager.notify(notificationId, notification)
            Log.d("NotifDebug", "Notificación MOSTRADA con ID: $notificationId")
        } catch (e: Exception) {
            Log.e("NotifDebug", "Error al mostrar notificación", e)
        }
    }
}