package com.jaime.ascend.data.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jaime.ascend.data.repository.CategoryRepository
import com.jaime.ascend.data.repository.HabitRepository
import com.jaime.ascend.viewmodel.GoodHabitsViewModel

class GoodHabitsViewModelFactory(
    private val categoryRepository: CategoryRepository,
    private val habitRepository: HabitRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GoodHabitsViewModel::class.java)) {
            return GoodHabitsViewModel(categoryRepository, habitRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}