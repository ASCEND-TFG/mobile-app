package com.jaime.ascend.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.jaime.ascend.data.models.Category
import com.jaime.ascend.data.models.GoodHabit
import com.jaime.ascend.data.models.HabitTemplate
import com.jaime.ascend.data.repository.CategoryRepository
import com.jaime.ascend.data.repository.HabitRepository
import com.jaime.ascend.data.repository.TemplateRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GoodHabitsViewModel(
    private val categoryRepository: CategoryRepository,
    private val habitRepository: HabitRepository,
    private val templateRepository: TemplateRepository,
    private val auth: FirebaseAuth,
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

    // Load habits WITHOUT resolving templates upfront
    fun loadHabits(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            habitRepository.getUserGoodHabitsRealTime(userId)
                .collect { habits ->
                    _habits.value = habits // Just store the raw habits
                    _isLoading.value = false
                }
        }
    }

    fun loadTemplate(templateId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val template = templateRepository.getTemplateById(templateId)
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


    // Call this when you get your habits list
    private suspend fun resolveTemplates(habits: List<GoodHabit>): List<GoodHabit> {
        return habits.map { habit ->
            habit.copy().apply {
                // Resolve template if not already resolved
                if (resolvedTemplate == null) {
                    resolvedTemplate =
                        habit.template?.get()?.await()?.toObject(HabitTemplate::class.java)
                }
                // Resolve category if needed
                if (resolvedCategory == null) {
                    resolvedCategory =
                        habit.category?.get()?.await()?.toObject(Category::class.java)
                }
            }
        }
    }

    fun selectCategory(category: Category?) {
        _selectedCategory.value = category
        _isLoading.value = true

        // Load templates for the selected category
        viewModelScope.launch {
            try {
                _templates.value = templateRepository.getTemplatesByCategory(category?.id!!)
            } catch (e: Exception) {
                Log.e("TAG", "selectCategory: $e")
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false

            }
        }
    }


}