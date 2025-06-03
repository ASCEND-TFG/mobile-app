package com.jaime.ascend.data.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jaime.ascend.data.repository.CategoryRepository
import com.jaime.ascend.data.repository.GoodHabitRepository
import com.jaime.ascend.data.repository.TemplateRepository
import com.jaime.ascend.viewmodel.GoodHabitsViewModel

/**
 * Factory class for creating instances of [GoodHabitsViewModel].
 * @author Jaime Martínez Fernández
 */
class GoodHabitsViewModelFactory(
    private val categoryRepository: CategoryRepository,
    private val habitRepository: GoodHabitRepository,
    private val templateRepository: TemplateRepository,
    private val context: Context
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
            context
        ) as T
    }
}