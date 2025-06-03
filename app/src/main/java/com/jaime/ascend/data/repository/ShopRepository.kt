package com.jaime.ascend.data.repository

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.jaime.ascend.R
import com.jaime.ascend.data.models.Moment
import com.jaime.ascend.utils.ShopLocalCache
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.min

/**
 * Repository for the shop.
 * @param firestore The Firebase Firestore instance.
 * @param localCache The local cache for moments.
 * @param currentContext The current context.
 * @author Jaime Martínez Fernández
 */
class ShopRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val localCache: ShopLocalCache,
    private val currentContext: Context
) {

    /**
     * Constants for Firestore collections.
     * @property MOMENTS_COLLECTION The name of the moments collection.
     * @property USERS_COLLECTION The name of the users collection.
     */
    private companion object {
        const val MOMENTS_COLLECTION = "moments"
        const val USERS_COLLECTION = "users"
    }

    /**
     * Checks if it's time to reset the weekly moments.
     * @param userId The ID of the user.
     * @return True if it's time to reset, false otherwise.
     * @throws Exception if the reset fails.
     */
    suspend fun checkAndHandleWeeklyReset(userId: String): Boolean {
        val today = LocalDate.now()
        val lastResetDate = localCache.getLastReRollDate()?.let {
            LocalDate.parse(it)
        }

        // Verificar si es lunes y no se ha hecho reset esta semana
        val shouldReset = today.dayOfWeek == DayOfWeek.MONDAY &&
                (lastResetDate == null || lastResetDate.isBefore(today))

        if (shouldReset) {
            return try {
                // 1. Resetear momentos comprados del usuario
                firestore.collection(USERS_COLLECTION).document(userId)
                    .update("ownedMoments", emptyList<String>())
                    .await()

                // 2. Generar nuevos momentos aleatorios
                val allMoments = firestore.collection(MOMENTS_COLLECTION)
                    .get()
                    .await()
                    .toObjects(Moment::class.java)

                val randomMoments = allMoments.shuffled().take(4)
                localCache.cacheMoments(randomMoments)
                localCache.cacheLastReRollDate(today.toString())

                true
            } catch (e: Exception) {
                Log.e("ShopRepository", "Error en reset semanal", e)
                false
            }
        }
        return false
    }

    /**
     * Gets the current list of moments.
     * @return The current list of moments.
     */
    suspend fun getCurrentMoments(): List<Moment> {
        return localCache.getCachedMoments()
    }

    /**
     * Generates new random moments.
     * @throws Exception if the moments could not be generated.
     */
    suspend fun generateNewMoments() {
        val allMoments = firestore.collection(MOMENTS_COLLECTION)
            .get()
            .await()
            .toObjects(Moment::class.java)

        val randomMoments = allMoments.shuffled().take(4)
        localCache.cacheMoments(randomMoments)
        localCache.cacheLastReRollDate(LocalDate.now().toString())
        println("new random moments: $randomMoments")
    }

    /**
     * Gets the last re-roll date.
     * @return The last re-roll date, or null if not found.
     */
    suspend fun getLastReRollDate(): LocalDate? {
        val dateString = localCache.getLastReRollDate()

        return dateString?.let {
            LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
        }
    }

    /**
     * Checks if it's time to generate new moments.
     * @return True if it's time to generate new moments, false otherwise.
     */
    suspend fun shouldGenerateNewMoments(): Boolean {
        val lastReRoll = getLastReRollDate()

        if (lastReRoll == null)
            return true
        else {
            val today = LocalDate.now()
            val daysSinceLastReRoll = today.toEpochDay() - (lastReRoll.toEpochDay())

            return today.dayOfWeek == DayOfWeek.MONDAY && !lastReRoll.isEqual(today) || daysSinceLastReRoll >= 7
        }
    }

    /**
     * Purchases a moment.
     * @param currentUser The ID of the current user.
     * @param momentId The ID of the moment to purchase.
     * @param onSuccess The callback to execute on success.
     * @throws Exception if the purchase fails.
     */
    suspend fun purchaseMoment(
        currentUser: String,
        momentId: String,
        onSuccess: (Int, Int, List<String>) -> Unit
    ) {
        try {
            val userRef = firestore.collection(USERS_COLLECTION).document(currentUser)
            val momentDoc = firestore.collection(MOMENTS_COLLECTION)
                .document(momentId)
                .get()
                .await()

            val price = momentDoc.getLong("price")?.toInt() ?: 0
            val reward = momentDoc.getLong("reward")?.toInt() ?: 0

            firestore.runTransaction { transaction ->
                val userSnapshot = transaction.get(userRef)
                val currentCoins = userSnapshot.getLong("coins")?.toInt() ?: 0

                if (currentCoins < price) throw Exception(currentContext.getString(R.string.insufficent_coins))

                val newCoins = currentCoins - price
                val currentLife = userSnapshot.getLong("currentLife")?.toInt() ?: 0
                val maxLife = userSnapshot.getLong("maxLife")?.toInt() ?: 100
                val newLife = min(currentLife + reward, maxLife)
                val ownedMoments =
                    (userSnapshot.get("ownedMoments") as? List<String> ?: emptyList()) + momentId

                transaction.update(
                    userRef, mapOf(
                        "coins" to newCoins,
                        "currentLife" to newLife,
                        "ownedMoments" to ownedMoments.distinct()
                    )
                )

                Triple(newCoins, newLife, ownedMoments)
            }.await()?.let { (newCoins, newLife, ownedMoments) ->
                onSuccess(newCoins, newLife, ownedMoments)
                Toast.makeText(
                    currentContext,
                    currentContext.getString(R.string.moment_purchased), Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                currentContext,
                e.message, Toast.LENGTH_SHORT
            ).show()
        }
    }
}