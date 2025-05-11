package com.jaime.ascend.viewmodel

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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GoodHabitsViewModel(
    private val categoryRepository: CategoryRepository,
    private val habitRepository: HabitRepository,
    private val auth: FirebaseAuth,
) : ViewModel() {
    private val _habits = mutableStateOf<List<GoodHabit>>(emptyList())
    private val _templates = mutableStateOf<Map<String, HabitTemplate>>(emptyMap())
    private val _isLoading = mutableStateOf(false)
    private val _error = mutableStateOf<String?>(null)

    val habits:  State<List<GoodHabit>> = _habits
    val templates: State<Map<String, HabitTemplate>> = _templates
    val isLoading: State<Boolean> = _isLoading
    val error: State<String?> = _error

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

    // Call this when you get your habits list
    private suspend fun resolveTemplates(habits: List<GoodHabit>): List<GoodHabit> {
        return habits.map { habit ->
            habit.copy().apply {
                // Resolve template if not already resolved
                if (resolvedTemplate == null) {
                    resolvedTemplate = habit.template?.get()?.await()?.toObject(HabitTemplate::class.java)
                }
                // Resolve category if needed
                if (resolvedCategory == null) {
                    resolvedCategory = habit.category?.get()?.await()?.toObject(Category::class.java)
                }
            }
        }
    }
}