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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
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
import com.jaime.ascend.data.models.Category
import com.jaime.ascend.data.models.GoodHabit
import com.jaime.ascend.data.models.HabitTemplate
import com.jaime.ascend.ui.components.ActionBarWithBackButton
import com.jaime.ascend.ui.components.BlackButton
import com.jaime.ascend.ui.components.DayOfWeekSelector
import com.jaime.ascend.utils.Difficulty
import com.jaime.ascend.viewmodel.HabitDetailViewModel
import kotlinx.coroutines.tasks.await
import java.util.Locale

/**
 * Edit good habit screen.
 * It is used to edit a good habit.
 * @param navController Navigation controller.
 * @param habitId Id of the habit to edit.
 * @param viewModel Habit detail view model.
 * @author Jaime Martínez Fernández
 */
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGoodHabitScreen(
    navController: NavController,
    habitId: String,
    viewModel: HabitDetailViewModel = viewModel(
        factory = HabitDetailViewModelFactory(habitId, true)
    )
) {
    val habit by viewModel.ghabit.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var template by remember { mutableStateOf<HabitTemplate?>(null) }
    var category by remember { mutableStateOf<Category?>(null) }

    var expanded by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf<Difficulty>(Difficulty.EASY) }
    var reminderTime by remember { mutableStateOf<String?>(null) }
    val timePickerState = rememberTimePickerState()
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDays by remember { mutableStateOf(emptyList<Int>()) }

    LaunchedEffect(habit) {
        habit?.let { currentHabit ->
            try {
                currentHabit.template?.let { templateRef ->
                    templateRef.get().await().toObject(HabitTemplate::class.java)?.let {
                        template = it
                    }
                }

                currentHabit.category?.let { categoryRef ->
                    categoryRef.get().await().toObject(Category::class.java)?.let {
                        category = it
                    }
                }

                selectedDifficulty = currentHabit.difficulty
                reminderTime = currentHabit.reminderTime
                selectedDays = currentHabit.days ?: emptyList()
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
                        EditGoodHabitContent(
                            habit = habit!!,
                            template = template!!,
                            selectedDifficulty = selectedDifficulty,
                            onDifficultyChange = { selectedDifficulty = it },
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            reminderTime = reminderTime,
                            onReminderTimeChange = { reminderTime = it },
                            showTimePicker = showTimePicker,
                            onShowTimePickerChange = { showTimePicker = it },
                            timePickerState = timePickerState,
                            selectedDays = selectedDays,
                            onSelectedDaysChange = { selectedDays = it },
                            onSaveClick = {
                                viewModel.updateGoodHabit(
                                    habitId = habitId,
                                    days = selectedDays,
                                    difficulty = selectedDifficulty,
                                    reminderTime = reminderTime
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

/**
 * Edit good habit content.
 * It is used to edit a good habit.
 * @param habit Habit to edit.
 * @param template Habit template.
 * @param selectedDifficulty Selected difficulty.
 * @param onDifficultyChange Callback when difficulty is changed.
 * @param expanded Dropdown expanded state.
 * @param onExpandedChange Callback when dropdown is expanded/collapsed.
 * @param reminderTime Reminder time.
 * @param onReminderTimeChange Callback when reminder time is changed.
 * @param showTimePicker Time picker dialog state.
 * @param onShowTimePickerChange Callback when time picker dialog state is changed.
 * @param timePickerState Time picker state.
 * @param selectedDays Selected days.
 * @param onSelectedDaysChange Callback when days are changed.
 * @param onSaveClick Callback when save button is clicked.
 * @param modifier Modifier.
 */
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditGoodHabitContent(
    habit: GoodHabit,
    template: HabitTemplate,
    selectedDifficulty: Difficulty,
    onDifficultyChange: (Difficulty) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    reminderTime: String?,
    onReminderTimeChange: (String?) -> Unit,
    showTimePicker: Boolean,
    onShowTimePickerChange: (Boolean) -> Unit,
    timePickerState: TimePickerState,
    selectedDays: List<Int>,
    onSelectedDaysChange: (List<Int>) -> Unit,
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
        Text(
            text = template.getName(Locale.getDefault()),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Text(
            text = template.getDescription(Locale.getDefault()),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.set_reminder),
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (reminderTime != null) {
                    Text(
                        text = reminderTime ?: "",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                IconButton(
                    onClick = {
                        if (reminderTime != null) {
                            onReminderTimeChange(null)
                        } else {
                            onShowTimePickerChange(true)
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (reminderTime != null) Icons.Filled.Close else Icons.Filled.Schedule,
                        contentDescription = stringResource(R.string.set_reminder),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.how_many_times_week),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )

        DayOfWeekSelector(
            selectedDays = selectedDays,
            onDaySelected = { day ->
                onSelectedDaysChange(
                    if (selectedDays.contains(day)) {
                        selectedDays - day
                    } else {
                        selectedDays + day
                    }
                )
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        BlackButton(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedDays.isNotEmpty(),
            content = {
                Text(
                    text = stringResource(R.string.save_changes),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        )
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { onShowTimePickerChange(false) },
            title = {
                Text(
                    text = stringResource(R.string.select_reminder_time),
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReminderTimeChange(
                            String.format(
                                "%02d:%02d",
                                timePickerState.hour,
                                timePickerState.minute
                            )
                        )
                        onShowTimePickerChange(false)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onShowTimePickerChange(false) }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}