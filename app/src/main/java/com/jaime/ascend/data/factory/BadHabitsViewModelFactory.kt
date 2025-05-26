package com.jaime.ascend.data.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.jaime.ascend.data.repository.BadHabitRepository
import com.jaime.ascend.data.repository.CategoryRepository
import com.jaime.ascend.data.repository.TemplateRepository
import com.jaime.ascend.viewmodel.BadHabitsViewModel

class BadHabitsViewModelFactory(
    private val categoryRepository: CategoryRepository,
    private val habitRepository: BadHabitRepository,
    private val templateRepository: TemplateRepository,
    private val auth: FirebaseAuth
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(BadHabitsViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return BadHabitsViewModel(
            categoryRepository = categoryRepository,
            habitRepository = habitRepository,
            templateRepository = templateRepository,
            auth = auth
        ) as T
    }
}