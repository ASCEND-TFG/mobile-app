package com.jaime.ascend.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.jaime.ascend.data.models.GoodHabit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

    private fun loadHabit(habitId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val habitDoc = FirebaseFirestore.getInstance()
                    .collection("ghabits") // Ajusta según tu colección
                    .document(habitId)
                    .get()
                    .await()

                if (habitDoc.exists()) {
                    _habit.value = habitDoc.toObject(GoodHabit::class.java)
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
