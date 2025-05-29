import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jaime.ascend.data.models.Moment
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import kotlin.math.min
import kotlin.random.Random
import androidx.compose.runtime.State
import kotlinx.coroutines.delay
import java.util.Date

class ShopViewModel : ViewModel() {
    private val db = Firebase.firestore
    private var appContext: Context? = null
    private val _moments = mutableStateOf<List<Moment>>(emptyList())
    private val _userCoins = mutableStateOf(0)
    private val _currentLife = mutableStateOf(0)
    private val _maxLife = mutableStateOf(0)
    private val _daysUntilRefresh = mutableStateOf(0)
    private val _ownedMoments = mutableStateOf<Set<String>>(emptySet())
    private val _lastResetDate = mutableStateOf<Date?>(null)
    private val _shouldReloadMoments = mutableStateOf(true)

    val moments: State<List<Moment>> = _moments
    val userCoins: State<Int> = _userCoins
    val currentLife: State<Int> = _currentLife
    val maxLife: State<Int> = _maxLife
    val daysUntilRefresh: State<Int> = _daysUntilRefresh


    init {
        loadInitialData()
        scheduleWeeklyResetCheck()
    }

    fun setContext(context: Context) {
        appContext = context.applicationContext
    }

    private fun loadInitialData() = viewModelScope.launch {
        loadUserData()
        checkWeeklyReset()
        if (_shouldReloadMoments.value) {
            loadRandomMoments()
            _shouldReloadMoments.value = false
        }
        calculateDaysUntilRefresh()
    }

    private suspend fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            _userCoins.value = userDoc.getLong("coins")?.toInt() ?: 0
            _currentLife.value = userDoc.getLong("currentLife")?.toInt() ?: 0
            _maxLife.value = userDoc.getLong("maxLife")?.toInt() ?: 100
            _ownedMoments.value =
                (userDoc.get("ownedMoments") as? List<String>)?.toSet() ?: emptySet()
            _lastResetDate.value = userDoc.getDate("lastResetDate")
        } catch (e: Exception) {
            showError("Error loading user data", e)
        }
    }

    private fun checkWeeklyReset() {
        val today = Calendar.getInstance()
        val lastMonday = getLastMonday()

        if (_lastResetDate.value == null ||
            _lastResetDate.value!!.before(lastMonday.time)
        ) {

            if (today.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                resetWeeklyData()
                saveResetDate(today.time)
                _shouldReloadMoments.value = true
            }
        }
    }

    /**
     * Guarda la fecha de reinicio semanal en la base de datos
     * @param date Fecha de reinicio semanal
     */
    private fun saveResetDate(date: Date) = viewModelScope.launch {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
        try {
            db.collection("users").document(userId)
                .update("lastResetDate", date)
                .await()
            _lastResetDate.value = date
        } catch (e: Exception) {
            showError("Error saving reset date", e)
        }
    }

    private fun resetWeeklyData() {
        _ownedMoments.value = emptySet()
        _shouldReloadMoments.value = true
        showToast("¡Nueva semana! Momentos reseteados")
    }

    private fun getLastMonday(): Calendar {
        val cal = Calendar.getInstance()
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

    private fun scheduleWeeklyResetCheck() {
        viewModelScope.launch {
            while (true) {
                val now = Calendar.getInstance()
                val nextCheck = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                val delay = nextCheck.timeInMillis - now.timeInMillis
                if (delay > 0) delay(delay)

                checkWeeklyReset()
                calculateDaysUntilRefresh()
            }
        }
    }

    private fun calculateDaysUntilRefresh() {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
        _daysUntilRefresh.value = when {
            currentDay < Calendar.MONDAY -> Calendar.MONDAY - currentDay
            currentDay > Calendar.MONDAY -> 7 - (currentDay - Calendar.MONDAY)
            else -> 7
        }
    }

    fun loadRandomMoments(force: Boolean = false) = viewModelScope.launch {
        if (force || _shouldReloadMoments.value) {
            try {
                val allMoments = db.collection("moments").get().await()
                    .toObjects(Moment::class.java)

                _moments.value = allMoments.shuffled().take(4).map { moment ->
                    moment.copy(isOwned = moment.id in _ownedMoments.value)
                }
                _shouldReloadMoments.value = false
            } catch (e: Exception) {
                showError("Error loading moments", e)
                _moments.value = emptyList()
            }
        }
    }

    fun purchaseMoment(momentId: String) = viewModelScope.launch {
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            showToast("Usuario no autenticado")
            return@launch
        }

        try {
            val momentDoc = db.collection("moments").document(momentId).get().await()
            val price = momentDoc.getLong("price")?.toInt() ?: 0
            val reward = momentDoc.getLong("reward")?.toInt() ?: 0

            val userRef = db.collection("users").document(currentUser)

            db.runTransaction { transaction ->
                val userSnapshot = transaction.get(userRef)
                val currentCoins = userSnapshot.getLong("coins")?.toInt() ?: 0

                if (currentCoins < price) throw Exception("Insufficient coins")

                // Actualizar datos
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
                // Actualizar estado local
                _userCoins.value = newCoins
                _currentLife.value = newLife
                _ownedMoments.value = ownedMoments.toSet()
                _moments.value = _moments.value.map {
                    if (it.id == momentId) it.copy(isOwned = true) else it
                }
                showToast("¡Compra exitosa!")
            }
        } catch (e: Exception) {
            showToast(if (e.message == "Insufficient coins") "No tienes suficientes monedas" else "Error al procesar la compra")
            Log.e("ShopViewModel", "Purchase error", e)
        }
    }

    private fun showToast(message: String) {
        appContext?.let { Toast.makeText(it, message, Toast.LENGTH_SHORT).show() }
    }

    private fun showError(message: String, e: Exception) {
        Log.e("ShopViewModel", message, e)
        showToast("$message. Ver logs para detalles.")
    }
}