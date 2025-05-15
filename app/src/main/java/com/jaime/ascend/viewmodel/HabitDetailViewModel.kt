package com.jaime.ascend.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.jaime.ascend.data.models.Difficulty
import com.jaime.ascend.data.models.GoodHabit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.Timestamp

class HabitDetailViewModel(habitId: String) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val habitsCollection = firestore.collection("ghabits")

    private val _habit = MutableStateFlow<GoodHabit?>(null)
    val habit: StateFlow<GoodHabit?> = _habit.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadHabit(habitId)
    }

    fun updateHabit(
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

                habitsCollection.document(habitId)
                    .update(updates)
                    .await()

                // Actualizar el estado local
                _habit.value = _habit.value?.copy(
                    days = days,
                    difficulty = difficulty,
                    coinReward = difficulty.coinValue,
                    xpReward = difficulty.xpValue,
                    reminderTime = reminderTime
                )
            } catch (e: Exception) {
                _error.value = "Error updating habit: ${e.localizedMessage}"
                Log.e("HabitDetailViewModel", "Error updating habit", e)
            } finally {
                _isLoading.value = false
            }
        }
    }


    internal fun loadHabit(habitId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val habitDoc = FirebaseFirestore.getInstance()
                    .collection("ghabits") // Ajusta según tu colección
                    .document(habitId)
                    .get()
                    .await()

                if (habitDoc.exists()) {
                    _habit.value = habitDoc.toObject(GoodHabit::class.java)?.copy(id = habitDoc.id)
                    Log.i("TAG", "loadHabit: ${_habit.value}")
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

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            try {
                habitsCollection.document(habitId).delete().await()
                Log.d("HabitDetailViewModel", "Habit deleted successfully")
            } catch (e: Exception) {
                _error.value = "Failed to delete habit: ${e.message}"
                Log.e("HabitDetailViewModel", "Error deleting habit", e)
            }
        }
    }
}
