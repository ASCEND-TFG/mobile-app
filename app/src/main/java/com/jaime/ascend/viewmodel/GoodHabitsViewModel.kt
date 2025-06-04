package com.jaime.ascend.viewmodel

import android.content.Context
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

/**
 * ViewModel for the good habits screen.
 * It allows the user to create new good habits.
 * @author Jaime Martínez Fernández
 * @param categoryRepository The repository for categories
 * @param habitRepository The repository for good habits
 * @param templateRepository The repository for habit templates
 * @param context The application context
 */
class GoodHabitsViewModel(
    private val categoryRepository: CategoryRepository,
    private val habitRepository: GoodHabitRepository,
    private val templateRepository: TemplateRepository,
    context: Context,
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

    /**
     * Loads the good habit templates.
     * @throws Exception if there is an error loading the templates
     */
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

    /**
     * Creates a new good habit.
     * @param templateId The ID of the habit template to use
     * @param days The days of the week to complete the habit
     * @param difficulty The difficulty of the habit
     * @param reminderTime The time to remind the user to complete the habit
     * @param onComplete The callback to invoke when the habit is created
     * @throws Exception if there is an error creating the habit
     */
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

    /**
     * Loads the user's good habits.
     * @param userId The ID of the user to load the habits for
     */
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

    /**
     * Loads a habit template by its ID.
     * @param templateId The ID of the habit template to load
     */
    fun loadTemplate(templateId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val template = templateRepository.getGoodHabitTemplateById(templateId)
            _templateToAdd.value = template
            _isLoading.value = false
        }
    }

    /**
     * Loads the categories.
     * @throws Exception if there is an error loading the categories
     */
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

    /**
     * Selects a category.
     * @param category The category to select
     */
    fun selectCategory(category: Category?) {
        _selectedCategory.value = category
        _isLoading.value = true

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