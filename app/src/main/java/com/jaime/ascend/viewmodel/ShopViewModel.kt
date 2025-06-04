package com.jaime.ascend.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jaime.ascend.data.models.Moment
import com.jaime.ascend.data.repository.ShopRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/**
 * ViewModel for the Shop screen
 * @param shopRepo Repository for the shop
 * @param ctx Context of the application
 * @author Jaime Martínez Fernández
 */
class ShopViewModel(
    private val shopRepo: ShopRepository,
    private val ctx: Context
) : ViewModel() {
    private val _moments = mutableStateOf<List<Moment>>(emptyList())
    private val _userCoins = mutableIntStateOf(0)
    private val _currentLife = mutableIntStateOf(0)
    private val _maxLife = mutableIntStateOf(100)
    private val _ownedMoments = mutableStateOf<Set<String>>(emptySet())
    private val _isLoading = mutableStateOf(true)
    private val _daysUntilRefresh = mutableIntStateOf(0)
    private val _showResetMessage = mutableStateOf(false)
    private val _isUserDead = mutableStateOf(false)
    private val _revivalChallenge = mutableStateOf("")
    private val _isShopLocked = mutableStateOf(false)
    private val _showRevivalDialog = mutableStateOf(false)

    val moments: State<List<Moment>> = _moments
    val userCoins: State<Int> = _userCoins
    val currentLife: State<Int> = _currentLife
    val maxLife: State<Int> = _maxLife
    val isLoading: State<Boolean> = _isLoading
    val daysUntilRefresh: State<Int> = _daysUntilRefresh
    val showResetMessage: State<Boolean> = _showResetMessage
    val isUserDead: State<Boolean> = _isUserDead
    val revivalChallenge: State<String> = _revivalChallenge
    val isShopLocked: State<Boolean> = _isShopLocked
    val showRevivalDialog: State<Boolean> = _showRevivalDialog

    /**
     * Load initial data from Firestore
     */
    internal fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
                Toast.makeText(ctx, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return@launch
            }

            try {
                loadUserData(userId)

                val didReset = shopRepo.checkAndHandleWeeklyReset(userId)
                _showResetMessage.value = didReset

                val b = shopRepo.shouldGenerateNewMoments()
                println("should generate new moments $b")
                if (b) {
                    shopRepo.generateNewMoments()
                    _ownedMoments.value = emptySet()
                }

                val currentMoments = shopRepo.getCurrentMoments()

                _moments.value = currentMoments.map { moment ->
                    moment.copy(isOwned = moment.id in _ownedMoments.value)
                }

                _daysUntilRefresh.intValue = calculateDaysUntilNextMonday()
            } catch (e: Exception) {
                Log.e("ShopViewModel", "Error loading data", e)
                Toast.makeText(ctx, "Error cargando datos", Toast.LENGTH_SHORT).show()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load user data from Firestore
     * @param userId ID of the user
     */
    private suspend fun loadUserData(userId: String) {
        val userDoc = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .await()

        _userCoins.intValue = userDoc.getLong("coins")?.toInt() ?: 0
        _currentLife.intValue = userDoc.getLong("currentLife")?.toInt() ?: 0
        _maxLife.intValue = userDoc.getLong("maxLife")?.toInt() ?: 100
        _ownedMoments.value = (userDoc.get("ownedMoments") as? List<String>)?.toSet() ?: emptySet()
        _isShopLocked.value = userDoc.getBoolean("isShopLocked") ?: false
        _isUserDead.value = _currentLife.intValue <= 0
    }

    /**
     * Calculate the number of days until the next Monday
     * @return Number of days until the next Monday
     */
    private fun calculateDaysUntilNextMonday(): Int {
        val today = LocalDate.now()
        val nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
        return ChronoUnit.DAYS.between(today, nextMonday).toInt()
    }

    /**
     * Purcharse a moment
     * @param momentId ID of the moment to purchase
     * @throws Exception if there is an error purchasing the moment
     */
    fun purchaseMoment(momentId: String) {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                shopRepo.purchaseMoment(userId, momentId) { newCoins, newLife, ownedMoments ->
                    _userCoins.intValue = newCoins
                    _currentLife.intValue = newLife
                    _ownedMoments.value = ownedMoments.toSet()

                    // Guardar habito como owned
                    _moments.value = _moments.value.map {
                        if (it.id == momentId) it.copy(isOwned = true) else it
                    }
                }
            } catch (e: Exception) {
                Log.e("ShopViewModel", "Error purchasing moment", e)
                Toast.makeText(ctx, "Error al comprar el momento", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Dismiss the reset message
     */
    fun dismissResetMessage() {
        _showResetMessage.value = false
    }
}