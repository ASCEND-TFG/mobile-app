package com.jaime.ascend.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.jaime.ascend.data.models.BadHabit
import com.jaime.ascend.data.models.GoodHabit
import com.jaime.ascend.utils.Difficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HabitDetailViewModel(habitId: String, isGoodHabit: Boolean) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val ghabitsCollection = firestore.collection("ghabits")
    private val bhabitsCollection = firestore.collection("bhabits")

    private val _ghabit = MutableStateFlow<GoodHabit?>(null)
    val ghabit: StateFlow<GoodHabit?> = _ghabit.asStateFlow()

    private val _bhabit = MutableStateFlow<BadHabit?>(null)
    val bhabit: StateFlow<BadHabit?> = _bhabit.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        if (isGoodHabit) {
            loadGoodHabit(habitId)
        } else {
            loadBadHabit(habitId)
        }
    }

    fun updateBadHabit(
        habitId: String,
        difficulty: Difficulty,
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updates = hashMapOf<String, Any>(
                    "difficulty" to difficulty.name,
                    "coinReward" to difficulty.coinValue,
                    "xpReward" to difficulty.xpValue,
                    "lifeLoss" to difficulty.lifeLoss
                )

                bhabitsCollection.document(habitId)
                    .update(updates)
                    .await()

                // Actualizar el estado local
                _bhabit.value = _bhabit.value?.copy(
                    difficulty = difficulty,
                    coinReward = difficulty.coinValue,
                    xpReward = difficulty.xpValue,
                    lifeLoss = difficulty.lifeLoss
                    )
            } catch (e: Exception) {
                _error.value = "Error updating habit: ${e.localizedMessage}"
                Log.e("HabitDetailViewModel", "Error updating habit", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateGoodHabit(
        habitId: String,
        days: List<Int>,
        difficulty: Difficulty,
        reminderTime: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updates = hashMapOf<String, Any>(
                    "days" to days,
                    "difficulty" to difficulty.name,
                    "coinReward" to difficulty.coinValue,
                    "xpReward" to difficulty.xpValue,
                )

                if (reminderTime != null) {
                    updates["reminderTime"] = reminderTime
                } else {
                    updates["reminderTime"] = FieldValue.delete()
                }

                ghabitsCollection.document(habitId)
                    .update(updates)
                    .await()

                // Actualizar el estado local
                _ghabit.value = _ghabit.value?.copy(
                    days = days,
                    difficulty = difficulty,
                    coinReward = difficulty.coinValue,
                    xpReward = difficulty.xpValue,
                    reminderTime = reminderTime,

                )
            } catch (e: Exception) {
                _error.value = "Error updating habit: ${e.localizedMessage}"
                Log.e("HabitDetailViewModel", "Error updating habit", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    internal fun loadBadHabit(habitId: String) {
        Log.d("HabitDetail", "Loading habit with ID: $habitId")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val habitDoc = FirebaseFirestore.getInstance()
                    .collection("bhabits")
                    .document(habitId)
                    .get()
                    .await()

                if (habitDoc.exists()) {
                    _bhabit.value = habitDoc.toObject(BadHabit::class.java)?.copy(id = habitDoc.id)
                    Log.i("TAG", "loadHabit: ${_bhabit.value}")
                } else {
                    _error.value = "Habit not found"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    internal fun loadGoodHabit(habitId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val habitDoc = FirebaseFirestore.getInstance()
                    .collection("ghabits")
                    .document(habitId)
                    .get()
                    .await()

                if (habitDoc.exists()) {
                    _ghabit.value = habitDoc.toObject(GoodHabit::class.java)?.copy(id = habitDoc.id)
                    Log.i("TAG", "loadHabit: ${_ghabit.value}")
                } else {
                    _error.value = "Habit not found"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteBadHabit(habitId: String) {
        viewModelScope.launch {
            try {
                bhabitsCollection.document(habitId).delete().await()
                Log.d("HabitDetailViewModel", "Habit deleted successfully")
            } catch (e: Exception) {
                _error.value = "Failed to delete habit: ${e.message}"
                Log.e("HabitDetailViewModel", "Error deleting habit", e)
            }
        }
    }

    fun deleteGoodHabit(habitId: String) {
        viewModelScope.launch {
            try {
                ghabitsCollection.document(habitId).delete().await()
                Log.d("HabitDetailViewModel", "Habit deleted successfully")
            } catch (e: Exception) {
                _error.value = "Failed to delete habit: ${e.message}"
                Log.e("HabitDetailViewModel", "Error deleting habit", e)
            }
        }
    }
}
