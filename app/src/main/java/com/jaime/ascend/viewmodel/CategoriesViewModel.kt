package com.jaime.ascend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.jaime.ascend.data.models.Category
import com.jaime.ascend.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * View model for the categories screen.
 * @param repository Repository for the categories.
 * @author Jaime Martínez Fernández
 */
class CategoriesViewModel(
    private val repository: CategoryRepository
) : ViewModel() {

    /**
     * State for the categories.
     */
    sealed interface CategoriesState {
        object Loading : CategoriesState
        data class Success(val categories: List<Category>) : CategoriesState
        data class Error(val message: String) : CategoriesState
    }

    private val _state = MutableStateFlow<CategoriesState>(CategoriesState.Loading)
    val state: StateFlow<CategoriesState> = _state.asStateFlow()

    init {
        loadCategories()
    }

    /**
     * Load the categories.
     */
    fun loadCategories() {
        viewModelScope.launch {
            _state.value = CategoriesState.Loading
            try {
                val categories = repository.getCategories()
                _state.value = CategoriesState.Success(categories)
            } catch (e: Exception) {
                _state.value = CategoriesState.Error("Error: ${e.localizedMessage}")
            }
        }
    }
}
