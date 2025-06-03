package com.jaime.ascend.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.jaime.ascend.data.models.GoodHabit
import com.jaime.ascend.utils.Difficulty
import com.jaime.ascend.utils.NotificationReceiver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Locale

/**
 * Repository for good habits.
 * @param firestore The Firebase Firestore instance.
 * @param auth The Firebase Authentication instance.
 * @author Jaime Martínez Fernández
 */
class GoodHabitRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val ctx: Context
) {
    private val templateRepository = TemplateRepository(firestore)
    private val habitsCollection = firestore.collection("ghabits")
    private val activeListeners = mutableMapOf<String, ListenerRegistration>()
    private val alarmManager =
        ctx.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

    /**
     * Creates a new good habit.
     * @param templateId The ID of the good habit template to use.
     * @param days The days of the week to complete the habit.
     * @param difficulty The difficulty of the habit.
     * @param reminderTime The time to send a reminder notification.
     * @return A boolean indicating if the habit was created successfully.
     * @throws Exception if the habit could not be created.
     */
    suspend fun createGoodHabit(
        templateId: String,
        days: List<Int>,
        difficulty: Difficulty,
        reminderTime: String? = null,
    ): Boolean {
        var success = false
        val template = templateRepository.getGoodHabitTemplateById(templateId)
        val habitData = hashMapOf(
            "category" to template?.category,
            "coinReward" to difficulty.coinValue,
            "createdAt" to Timestamp.now(),
            "days" to days,
            "difficulty" to difficulty.name,
            "template" to FirebaseFirestore.getInstance()
                .document("ghabit_templates/$templateId"),
            "userId" to (auth.currentUser?.uid!!),
            "xpReward" to difficulty.xpValue,
            "completed" to false,
            "reminderTime" to reminderTime,
        )

        val documentReference = habitsCollection.add(habitData).await()
        val habitId = documentReference.id

        reminderTime?.let { time ->
            if (days.isNotEmpty()) {
                scheduleReminderAlarm(
                    habitId,
                    template?.getName(Locale.getDefault()).toString(), time, days
                )
            }
        }

        success = true
        return success
    }

    /**
     * Schedules a reminder alarm for a good habit.
     * @param habitId The ID of the habit.
     * @param habitName The name of the habit.
     * @param time The time to send a reminder notification.
     * @param days The days of the week to complete the habit.
     * @throws Exception if the alarm could not be scheduled.
     */
    private fun scheduleReminderAlarm(
        habitId: String,
        habitName: String,
        time: String,
        days: List<Int>
    ) {
        Log.d("AlarmDebug", "Hábito: $habitName (ID: $habitId)")
        Log.d("AlarmDebug", "Hora recordatorio: $time")
        Log.d("AlarmDebug", "Días programados: ${days.joinToString()}")

        try {
            val (hour, minute) = time.split(":").map { it.toInt() }

            days.forEach { yourDayCode ->
                val calendarDay = when (yourDayCode) {
                    0 -> Calendar.MONDAY
                    1 -> Calendar.TUESDAY
                    2 -> Calendar.WEDNESDAY
                    3 -> Calendar.THURSDAY
                    4 -> Calendar.FRIDAY
                    5 -> Calendar.SATURDAY
                    6 -> Calendar.SUNDAY
                    else -> {
                        Log.e("AlarmDebug", "Día no válido: $yourDayCode")
                        return@forEach
                    }
                }

                val calendar = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, calendarDay)
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)

                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.DAY_OF_MONTH, 7)
                    }
                }

                val intent = Intent(ctx, NotificationReceiver::class.java).apply {
                    putExtra("habitId", habitId)
                    putExtra("habitName", habitName)
                    putExtra("notificationId", habitId.hashCode() + yourDayCode)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    ctx,
                    habitId.hashCode() + yourDayCode,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )

                Log.d("AlarmDebug", "Alarma REPETITIVA programada para: ${calendar.time}")
                Log.d("AlarmDebug", "Se repetirá cada 7 días (semanalmente)")
            }
        } catch (e: Exception) {
            Log.e("AlarmDebug", "Error al programar alarma repetitiva", e)
        }
    }

    /**
     * Gets a flow of real-time updates for a user's good habits.
     * @param userId The ID of the user.
     * @return A flow of lists of good habits.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getUserGoodHabitsRealTime(userId: String): Flow<List<GoodHabit>> = callbackFlow {
        val listenerKey = "habits_$userId"

        activeListeners[listenerKey]?.remove()

        val listener = habitsCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val habits = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(GoodHabit::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("HABIT_PARSE", "Error parsing habit ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                trySend(habits)
            }

        activeListeners[listenerKey] = listener
        awaitClose { activeListeners.remove(listenerKey)?.remove() }
    }
}
