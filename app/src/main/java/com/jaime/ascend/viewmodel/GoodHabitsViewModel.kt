package com.jaime.ascend.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaime.ascend.data.models.Category
import com.jaime.ascend.data.models.GoodHabit
import com.jaime.ascend.data.models.HabitTemplate
import com.jaime.ascend.data.repository.CategoryRepository
import com.jaime.ascend.data.repository.GoodHabitRepository
import com.jaime.ascend.data.repository.TemplateRepository
import com.jaime.ascend.utils.Difficulty
import kotlinx.coroutines.launch

class GoodHabitsViewModel(
    private val categoryRepository: CategoryRepository,
    private val habitRepository: GoodHabitRepository,
    private val templateRepository: TemplateRepository,
) : ViewModel() {
    private val _habits = mutableStateOf<List<GoodHabit>>(emptyList())
    private val _templates = mutableStateOf<List<HabitTemplate>>(emptyList())
    private val _isLoading = mutableStateOf(false)
    private val _error = mutableStateOf<String?>(null)
    private val _categories = mutableStateOf<List<Category>>(emptyList())
    private val _selectedCategory = mutableStateOf<Category?>(null)
    private val _templateToAdd = mutableStateOf<HabitTemplate?>(null)

    val habits: State<List<GoodHabit>> = _habits
    val templates: State<List<HabitTemplate>> = _templates
    val isLoading: State<Boolean> = _isLoading
    val error: State<String?> = _error
    val categories: State<List<Category>> = _categories
    val selectedCategory: State<Category?> = _selectedCategory
    val templateToAdd: State<HabitTemplate?> = _templateToAdd

    fun loadGoodHabitTemplates() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _templates.value = templateRepository.getAllGoodHabitTemplates()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createGoodHabit(
        templateId: String,
        days: List<Int>,
        difficulty: Difficulty,
        reminderTime: String? = null,
        onComplete: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                if (habitRepository.createGoodHabit(templateId, days, difficulty, reminderTime)) {
                    onComplete(Result.success(Unit))
                }
            } catch (e: Exception) {
                Log.e("GoodHabitsVM", "Error creating habit", e)
                onComplete(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Load habits WITHOUT resolving templates upfront
    fun loadHabits(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            habitRepository.getUserGoodHabitsRealTime(userId)
                .collect { habits ->
                    _habits.value = habits
                    _isLoading.value = false
                }
        }
    }

    fun loadTemplate(templateId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val template = templateRepository.getGoodHabitTemplateById(templateId)
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

        // Load templates for the selected category
        viewModelScope.launch {
            try {
                _templates.value = templateRepository.getGoodHabitTemplatesByCategory(category?.id!!)
            } catch (e: Exception) {
                Log.e("TAG", "selectCategory: $e")
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false

            }
        }
    }
}