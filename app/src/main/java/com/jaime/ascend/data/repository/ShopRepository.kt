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

class ShopRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val localCache: ShopLocalCache,
    private val currentContext: Context
) {

    private companion object {
        const val MOMENTS_COLLECTION = "moments"
        const val USERS_COLLECTION = "users"
    }

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
     * Obtiene los momentos actuales de la caché local
     * @return Lista de momentos, si no hay, devuelve una lista vacia
     */
    suspend fun getCurrentMoments(): List<Moment> {
        return localCache.getCachedMoments().ifEmpty {
            // Si no hay en caché, generar nuevos momentos
            val allMoments = firestore.collection(MOMENTS_COLLECTION)
                .get()
                .await()
                .toObjects(Moment::class.java)

            val randomMoments = allMoments.shuffled().take(4)
            localCache.cacheMoments(randomMoments)
            randomMoments
        }
    }

    /**
     * Obtiene la fecha del último reroll de la caché local
     * @return Fecha del último reroll, si se conoce, devuelve null
     */
    suspend fun getLastReRollDate(): LocalDate? {
        val dateString = localCache.getLastReRollDate()

        val date = dateString?.let {
            LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
        }

        return date
    }


    /**
     * Verifica si es necesario generar nuevos momentos
     * @return True si es necesario, false en caso contrario
     */
    suspend fun shouldGenerateNewMoments(): Boolean {
        val lastReRoll = getLastReRollDate()
        val today = LocalDate.now()
        val daysSinceLastReRoll = today.toEpochDay() - (lastReRoll?.toEpochDay() ?: 0)

        return today.dayOfWeek == DayOfWeek.MONDAY && daysSinceLastReRoll >= 7
    }


    /**
     * Realiza la compra de un momento
     * @param currentUser ID del usuario actual
     * @param momentId ID del momento a comprar
     * @param onSuccess Callback que se ejecuta en caso de éxito
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