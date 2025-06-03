package com.jaime.ascend.data.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jaime.ascend.data.repository.CategoryRepository
import com.jaime.ascend.viewmodel.CategoriesViewModel

/**
 * Factory class for creating instances of [CategoriesViewModel].
 */
class CategoriesViewModelFactory(
    private val repository: CategoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoriesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoriesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}