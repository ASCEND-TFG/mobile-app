package com.jaime.ascend.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jaime.ascend.R
import com.jaime.ascend.data.factory.GoodHabitsViewModelFactory
import com.jaime.ascend.data.repository.CategoryRepository
import com.jaime.ascend.data.repository.GoodHabitRepository
import com.jaime.ascend.data.repository.TemplateRepository
import com.jaime.ascend.ui.components.ActionBarWithBackButton
import com.jaime.ascend.ui.components.BlackButton
import com.jaime.ascend.ui.components.DayOfWeekSelector
import com.jaime.ascend.ui.navigation.AppScreens
import com.jaime.ascend.utils.Difficulty
import com.jaime.ascend.viewmodel.GoodHabitsViewModel
import java.util.Locale

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddingGoodHabitScreen(
    navController: NavController,
    templateId: String,
    viewModel: GoodHabitsViewModel = viewModel(
        factory = GoodHabitsViewModelFactory(
            categoryRepository = CategoryRepository(FirebaseFirestore.getInstance()),
            habitRepository = GoodHabitRepository(
                FirebaseFirestore.getInstance(),
                FirebaseAuth.getInstance(),
                LocalContext.current
            ),
            templateRepository = TemplateRepository(FirebaseFirestore.getInstance()),
            context = LocalContext.current
        )
    ),
) {
    val template by viewModel.templateToAdd
    val isLoading by viewModel.isLoading
    var expanded by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf<Difficulty>(Difficulty.EASY) }
    var reminderTime by remember { mutableStateOf<String?>(null) }
    val timePickerState = rememberTimePickerState()
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDays by remember { mutableStateOf(emptyList<Int>()) }

    LaunchedEffect(Unit) {
        viewModel.loadTemplate(templateId)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ActionBarWithBackButton(
                screenName = stringResource(id = R.string.add_new_good_habit_title),
                navController = navController,
                modifier = Modifier
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (template == null) {
                Text("ERROR")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    template?.let { template ->
                        // Sección de información del hábito
                        Text(
                            text = template.getName(Locale.getDefault()),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )

                        Text(
                            text = template.getDescription(Locale.getDefault()),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.select_difficulty),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth()
                        )

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
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
                                onDismissRequest = { expanded = false }
                            ) {
                                Difficulty.entries.forEach { difficulty ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(difficulty.labelRes)) },
                                        onClick = {
                                            selectedDifficulty = difficulty
                                            expanded = false
                                        },
                                        colors = MenuDefaults.itemColors(
                                            textColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Sección de recordatorio (ahora arriba)
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
                                            reminderTime = null
                                        } else {
                                            showTimePicker = true
                                        }
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = if (reminderTime != null) Icons.Default.Close else Icons.Default.Schedule,
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
                                selectedDays = if (selectedDays.contains(day)) {
                                    selectedDays - day
                                } else {
                                    selectedDays + day
                                }
                            }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = stringResource(R.string.habit_motivational_text),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        BlackButton(
                            onClick = {
                                if (selectedDays.isNotEmpty()) {
                                    viewModel.createGoodHabit(
                                        templateId = templateId,
                                        days = selectedDays,
                                        difficulty = selectedDifficulty,
                                        reminderTime = reminderTime,
                                        onComplete = { result ->
                                            navController.navigate(AppScreens.HomeScreen.route)
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = selectedDays.isNotEmpty(),
                            content = {
                                Text(
                                    text = stringResource(R.string.create),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            if (showTimePicker) {
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
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
                                reminderTime = String.format(
                                    "%02d:%02d",
                                    timePickerState.hour,
                                    timePickerState.minute
                                )
                                showTimePicker = false
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(stringResource(R.string.confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showTimePicker = false }
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
        }
    }
}