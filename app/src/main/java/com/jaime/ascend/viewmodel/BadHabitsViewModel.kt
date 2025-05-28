package com.jaime.ascend.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.jaime.ascend.data.models.BadHabit
import com.jaime.ascend.data.models.Category
import com.jaime.ascend.data.models.HabitTemplate
import com.jaime.ascend.data.repository.BadHabitRepository
import com.jaime.ascend.data.repository.CategoryRepository
import com.jaime.ascend.data.repository.TemplateRepository
import com.jaime.ascend.utils.Difficulty
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BadHabitsViewModel(
    private val categoryRepository: CategoryRepository,
    private val habitRepository: BadHabitRepository,
    private val templateRepository: TemplateRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _habits = mutableStateOf<List<BadHabit>>(emptyList())
    private val _templates = mutableStateOf<List<HabitTemplate>>(emptyList())
    private val _isLoading = mutableStateOf(false)
    private val _error = mutableStateOf<String?>(null)
    private val _categories = mutableStateOf<List<Category>>(emptyList())
    private val _selectedCategory = mutableStateOf<Category?>(null)
    private val _templateToAdd = mutableStateOf<HabitTemplate?>(null)

    val habits: State<List<BadHabit>> = _habits
    val templates: State<List<HabitTemplate>> = _templates
    val isLoading: State<Boolean> = _isLoading
    val error: State<String?> = _error
    val categories: State<List<Category>> = _categories
    val selectedCategory: State<Category?> = _selectedCategory
    val templateToAdd: State<HabitTemplate?> = _templateToAdd

    fun createBadHabit(
        templateId: String,
        difficulty: Difficulty,
        onComplete: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (habitRepository.createBadHabit(templateId, difficulty)) {
                    onComplete(Result.success(Unit))
                }
            } catch (e: Exception) {
                Log.e("BadHabitsVM", "Error creating habit", e)
                onComplete(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadHabits(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            habitRepository
                .getUserBadHabitsRealTime(userId)
                .collect { habits ->
                    _habits.value = habits
                    _isLoading.value = false
                }
        }
    }

    fun loadTemplate(templateId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val template = templateRepository.getBadHabitTemplateById(templateId)
            _templateToAdd.value = template
            _isLoading.value = false
        }
    }

    suspend fun loadCategories() {
        _isLoading.value = true
        try {
            _categories.value = categoryRepository.getCategories()
        } catch (e: Exception) {
            _error.value = e.localizedMessage
        } finally {
            _isLoading.value = false
        }
    }

    fun selectCategory(category: Category?) {
        _selectedCategory.value = category
        _isLoading.value = true

        viewModelScope.launch {
            try {
                _templates.value = templateRepository.getBadHabitTemplatesByCategory(category?.id!!)
            } catch (e: Exception) {
                Log.e("BadHabitsVM", "Error loading templates", e)
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleHabitCompleted(habit: BadHabit, isCompleted: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedHabit = habit.copy(completed = isCompleted)
                habitRepository.updateBadHabit(updatedHabit)
            } catch (e: Exception) {
                Log.e("BadHabitsVM", "Error updating habit", e)
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }
}