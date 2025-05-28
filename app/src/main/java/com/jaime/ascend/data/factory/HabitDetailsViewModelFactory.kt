package com.jaime.ascend.data.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jaime.ascend.viewmodel.HabitDetailViewModel

class HabitDetailViewModelFactory(private val habitId: String, private val isGoodHabit: Boolean) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitDetailViewModel(habitId, isGoodHabit) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}