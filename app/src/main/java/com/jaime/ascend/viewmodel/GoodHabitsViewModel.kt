package com.jaime.ascend.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaime.ascend.data.models.Category
import com.jaime.ascend.data.models.GoodHabit
import com.jaime.ascend.data.repository.CategoryRepository
import com.jaime.ascend.data.repository.HabitRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GoodHabitsViewModel(
    private val categoryRepository: CategoryRepository,
    private val habitRepository: HabitRepository,
    private val savedStateHandle: SavedStateHandle? = null
) : ViewModel() {

    sealed interface State {
        object Loading : State
        data class Success(
            val categories: List<Category> = emptyList(),
            val goodHabits: List<GoodHabit> = emptyList(),
            val currentCategory: String? = null,
            val searchedHabits: List<GoodHabit> = emptyList(),
        ) : State

        data class Error(val message: String) : State
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _state.value = State.Loading
            try {
                val categories = categoryRepository.getCategories()
                _state.value = State.Success(
                    categories = categories,
                    searchedHabits = emptyList(),
                )
            } catch (e: Exception) {
                _state.value = State.Error("Error loading categories: ${e.message}")
            }
        }
    }

    fun loadGoodHabitsByCategory(categoryId: String) {
        viewModelScope.launch {
            _state.value = State.Loading
            try {
                val habits = habitRepository.getGoodHabitsByCategory(categoryId)
                _state.value = (_state.value as? State.Success)?.copy(
                    goodHabits = habits,
                    currentCategory = categoryId
                ) ?: State.Success(
                    goodHabits = habits,
                    currentCategory = categoryId
                )
            } catch (e: Exception) {
                _state.value = State.Error(e.message.toString())
            }
        }
    }

    fun clearCurrentCategory() {
        _state.value = (_state.value as? State.Success)?.copy(
            currentCategory = null,
            goodHabits = emptyList()
        ) ?: State.Loading
        loadCategories()
    }

    private var searchJob: Job? = null

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.isNotEmpty()) {
                val habits = habitRepository.searchGoodHabits(query)
                _state.update { currentState ->
                    when (currentState) {
                        is State.Success -> currentState.copy(
                            searchedHabits = habits
                        )

                        else -> currentState
                    }
                }
            } else {
                _state.update { currentState ->
                    when (currentState) {
                        is State.Success -> currentState.copy(
                            searchedHabits = emptyList()
                        )

                        else -> currentState
                    }
                }
            }
        }
    }
}
