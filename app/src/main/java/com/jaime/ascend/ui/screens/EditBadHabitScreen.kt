package com.jaime.ascend.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jaime.ascend.R
import com.jaime.ascend.data.factory.HabitDetailViewModelFactory
import com.jaime.ascend.data.models.BadHabit
import com.jaime.ascend.data.models.Category
import com.jaime.ascend.data.models.HabitTemplate
import com.jaime.ascend.ui.components.ActionBarWithBackButton
import com.jaime.ascend.ui.components.BlackButton
import com.jaime.ascend.utils.Difficulty
import com.jaime.ascend.viewmodel.HabitDetailViewModel
import kotlinx.coroutines.tasks.await
import java.util.Locale

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBadHabitScreen(
    navController: NavController,
    habitId: String,
    viewModel: HabitDetailViewModel = viewModel(
        factory = HabitDetailViewModelFactory(habitId, false)
    )
) {
    val habit by viewModel.bhabit.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var template by remember { mutableStateOf<HabitTemplate?>(null) }
    var category by remember { mutableStateOf<Category?>(null) }

    // Estados para la edición
    var expanded by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf<Difficulty>(Difficulty.EASY) }

    LaunchedEffect(habit) {
        habit?.let { currentHabit ->
            try {
                // Carga el template solo si la referencia existe
                currentHabit.template?.let { templateRef ->
                    templateRef.get().await().toObject(HabitTemplate::class.java)?.let {
                        template = it
                    }
                }

                // Carga la categoría si existe
                currentHabit.category?.let { categoryRef ->
                    categoryRef.get().await().toObject(Category::class.java)?.let {
                        category = it
                    }
                }

                // Inicializa los estados editables
                selectedDifficulty = currentHabit.difficulty

            } catch (e: Exception) {
                Log.e("EditHabit", "Error loading template/category", e)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ActionBarWithBackButton(
                screenName = stringResource(id = R.string.edit_habit_title),
                navController = navController
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    error != null -> {
                        Text(
                            text = "Error: $error",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    habit != null && template != null -> {
                        EditBadHabitContent(
                            habit = habit!!,
                            template = template!!,
                            selectedDifficulty = selectedDifficulty,
                            onDifficultyChange = { selectedDifficulty = it },
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            onSaveClick = {
                                viewModel.updateBadHabit(
                                    habitId = habitId,
                                    difficulty = selectedDifficulty
                                )
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    "changesSaved",
                                    true
                                )
                                navController.popBackStack()
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    )
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditBadHabitContent(
    habit: BadHabit,
    template: HabitTemplate,
    selectedDifficulty: Difficulty,
    onDifficultyChange: (Difficulty) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    )
    {
        // Nombre del hábito (no editable)
        Text(
            text = template.getName(Locale.getDefault()),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )

        // Descripción (no editable)
        Text(
            text = template.getDescription(Locale.getDefault()),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Dificultad (editable con Dropdown)
        Text(
            text = stringResource(R.string.select_difficulty),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = onExpandedChange
        ) {
            TextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                readOnly = true,
                value = stringResource(selectedDifficulty.labelRes),
                onValueChange = {},
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                Difficulty.entries.forEach { difficulty ->
                    DropdownMenuItem(
                        text = { Text(stringResource(difficulty.labelRes)) },
                        onClick = {
                            onDifficultyChange(difficulty)
                            onExpandedChange(false)
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Spacer(modifier = Modifier.weight(1f))

        // Botón de guardar
        BlackButton(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth(),
            content = {
                Text(
                    text = stringResource(R.string.save_changes),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        )
    }
}
