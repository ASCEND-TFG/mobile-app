package com.jaime.ascend.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.jaime.ascend.data.models.Category
import com.jaime.ascend.data.models.Difficulty
import com.jaime.ascend.data.models.GoodHabit
import com.jaime.ascend.data.repository.CategoryRepository
import com.jaime.ascend.data.repository.HabitRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

class GoodHabitsViewModel(
    private val categoryRepository: CategoryRepository,
    private val habitRepository: HabitRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val currentUser = auth.currentUser

    sealed interface State {
        object Loading : State
        data class Success(
            val categories: List<Category> = emptyList(),
            val goodHabits: List<GoodHabit> = emptyList(),
            val categoryMap: Map<String, String> = emptyMap(),
            val currentCategory: String? = null,
            val searchedHabits: List<GoodHabit> = emptyList(),
        ) : State

        data class Error(val message: String) : State
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _userHabits = MutableStateFlow<List<GoodHabit>>(emptyList())
    val userHabits = _userHabits.asStateFlow()

    init {
        loadCategories()
    }

    fun loadUserHabits() {
        viewModelScope.launch {
            _state.value = State.Loading
            try {
                habitRepository.getUserGoodHabitsRealTime(currentUser?.uid ?: "")
                    .collect { habits ->
                        val updatedHabits = habits.map { habit ->
                            val categoryName = habit.categoryRef?.let { ref ->
                                try {
                                    val category = ref.get().await().toObject(Category::class.java)
                                    category?.name?.getLocalizedName(Locale.getDefault()) ?: "Unknown"
                                } catch (e: Exception) {
                                    "Unknown"
                                }
                            } ?: "Unknown"

                            habit.copy().apply {
                                this.categoryName = categoryName
                            }
                        }

                        _userHabits.value = updatedHabits
                        _state.value = State.Success(
                            goodHabits = updatedHabits
                        )
                    }
            } catch (e: Exception) {
                _state.value = State.Error("Error loading habits: ${e.message}")
            }
        }
    }

    private fun Map<String, String>.getLocalizedName(locale: Locale): String {
        return this[locale.language] ?: this["en"] ?: "Unknown"
    }

    private fun Map<String, String>.localize(locale: Locale): Map<String, String> {
        return mapOf(
            "en" to (this["en"] ?: ""),
            "es" to (this["es"] ?: ""),
            "current" to (this[locale.language] ?: this["en"] ?: "")
        )
    }


    fun createGoodHabit(
        name: Map<String, String>,
        description: Map<String, String>,
        icon: String,
        categoryRef: DocumentReference,
        difficulty: Difficulty,
        reminderTime: String?,
        days: List<Int>,
        onComplete: (Result<String>) -> Unit
    ) {
        if (currentUser == null) {
            onComplete(Result.failure(Exception("User not authenticated")))
            return
        }

        viewModelScope.launch {
            try {
                val result = habitRepository.createGoodHabit(
                    name = name,
                    description = description,
                    icon = icon,
                    categoryId = categoryRef.id,
                    difficulty = difficulty,
                    reminderTime = reminderTime,
                    days = days,
                    userId = currentUser.uid
                )

                result.fold(
                    onSuccess = { habitId ->
                        onComplete(Result.success(habitId))
                    },
                    onFailure = { e ->
                        onComplete(Result.failure(e))
                    }
                )
            } catch (e: Exception) {
                onComplete(Result.failure(e))
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            _state.value = State.Loading
            try {
                val categories = categoryRepository.getCategories()
                _state.value = State.Success(
                    categories = categories,
                    goodHabits = emptyList(),
                    searchedHabits = emptyList()
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