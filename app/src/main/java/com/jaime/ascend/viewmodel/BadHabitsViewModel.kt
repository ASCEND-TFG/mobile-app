package com.jaime.ascend.viewmodel

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
import kotlinx.coroutines.launch

/**
 * View model for bad habits.
 * @author Jaime Martínez Fernández
 * @param categoryRepository The category repository.
 * @param habitRepository The habit repository.
 * @param templateRepository The template repository.
 * @param auth The authentication.
 * @author Jaime Martínez Fernández
 */
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

    /**
     * Load bad habit templates.
     */
    fun loadBadHabitTemplates() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _templates.value = templateRepository.getAllBadHabitTemplates()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Create a bad habit.
     * @param templateId The id of the template.
     * @param difficulty The difficulty.
     * @param onComplete The callback.
     * @throws Exception If an error occurs.
     */
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

    /**
     * Load bad habits.
     * @param userId The id of the user.
     */
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

    /**
     * Load template.
     * @param templateId The id of the template.
     */
    fun loadTemplate(templateId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val template = templateRepository.getBadHabitTemplateById(templateId)
            _templateToAdd.value = template
            _isLoading.value = false
        }
    }

    /**
     * Load categories.
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
     * Select category.
     * @param category The category.
     */
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
}