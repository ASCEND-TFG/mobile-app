package com.jaime.ascend.data.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.jaime.ascend.data.repository.CategoryRepository
import com.jaime.ascend.data.repository.GoodHabitRepository
import com.jaime.ascend.data.repository.TemplateRepository
import com.jaime.ascend.viewmodel.GoodHabitsViewModel

class GoodHabitsViewModelFactory(
    private val categoryRepository: CategoryRepository,
    private val habitRepository: GoodHabitRepository,
    private val templateRepository: TemplateRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(GoodHabitsViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return GoodHabitsViewModel(
            categoryRepository,
            habitRepository,
            templateRepository,
        ) as T
    }
}