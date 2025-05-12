package com.jaime.ascend.ui.screens

import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.jaime.ascend.data.models.Difficulty
import com.jaime.ascend.data.models.HabitTemplate
import com.jaime.ascend.data.repository.CategoryRepository
import com.jaime.ascend.data.repository.HabitRepository
import com.jaime.ascend.data.repository.TemplateRepository
import com.jaime.ascend.ui.components.ActionBarWithBackButton
import com.jaime.ascend.ui.components.BlackButton
import com.jaime.ascend.ui.components.DayOfWeekSelector
import com.jaime.ascend.ui.navigation.AppScreens
import com.jaime.ascend.viewmodel.GoodHabitsViewModel
import java.util.Locale

@Composable
fun AddingGoodHabitScreen(
    navController: NavController,
    templateId: String,
    viewModel: GoodHabitsViewModel = viewModel(
        factory = GoodHabitsViewModelFactory(
            categoryRepository = CategoryRepository(FirebaseFirestore.getInstance()),
            habitRepository = HabitRepository(FirebaseFirestore.getInstance()),
            auth = FirebaseAuth.getInstance(),
            templateRepository = TemplateRepository(FirebaseFirestore.getInstance())
        )
    ),
) {
    val template by viewModel.templateToAdd

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
            Column {
                Text("ADDING GOOD HABIT")

                template?.let { template ->
                    Text(template.getName(Locale.getDefault()))
                    Text(template.getDescription(Locale.getDefault()))
                }
            }
        }
    }
}

/*
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddingGoodHabitScreen2(
    navController: NavController,
    categoryId: String,
    templateId: String,
    viewModel: GoodHabitsViewModel = viewModel(
        factory = GoodHabitsViewModelFactory(
            categoryRepository = CategoryRepository(FirebaseFirestore.getInstance()),
            habitRepository = HabitRepository(FirebaseFirestore.getInstance()),
            auth = FirebaseAuth.getInstance()
        )
    ),
) {
    var selectedDays by remember { mutableStateOf<List<Int>>(emptyList()) }
    var selectedDifficulty by remember { mutableStateOf<Difficulty?>(Difficulty.EASY) }
    var expanded by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(initialHour = 8, initialMinute = 0)
    var reminderTime by remember { mutableStateOf<String?>(null) }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        Log.i("TAG", "AddingGoodHabitScreen: TEMPLATE: $templateId")
        viewModel.getTemplateInfo(templateId)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ActionBarWithBackButton(
                screenName = stringResource(R.string.add_new_good_habit_title),
                navController = navController
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val currentState = state) {
                is State.Loading -> {
                    Log.i("TAG", "AddingGoodHabitScreen: LOADING")
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is State.Error -> {
                    Log.i("TAG", "AddingGoodHabitScreen: ERROR")
                    Text(
                        text = currentState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is State.Success -> {
                    val template = currentState.template
                    Log.i("TAG", "AddingGoodHabitScreen: TEMPLATE: $template")

                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = template?.getLocalizedName(Locale.getDefault().language)
                                    ?: "Loading...",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = template?.getLocalizedDescription(Locale.getDefault().language)
                                    ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Text(
                                text = stringResource(R.string.select_difficulty),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                TextField(
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    readOnly = true,
                                    value = selectedDifficulty?.let { stringResource(it.labelRes) }
                                        ?: stringResource(R.string.select_difficulty),
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

                            Spacer(modifier = Modifier.height(24.dp))

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

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = stringResource(R.string.how_many_times_week),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

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
                        }

                        Column {
                            Spacer(modifier = Modifier.height(32.dp))

                            Text(
                                text = stringResource(R.string.habit_motivational_text),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            BlackButton(
                                onClick = {
                                    if (selectedDays.isNotEmpty() && selectedDifficulty != null) {
                                        val nameMap =
                                            mapOf(
                                                "en" to template?.name["en"]!!,
                                                "es" to template.name["es"]!!
                                            )
                                        val descriptionMap =
                                            mapOf(
                                                "en" to template.description["en"]!!,
                                                "es" to template.description["es"]!!
                                            )

                                        viewModel.createGoodHabit(
                                            name = nameMap,
                                            description = descriptionMap,
                                            icon = template.icon,
                                            categoryRef = FirebaseFirestore.getInstance()
                                                .collection("categories")
                                                .document(template.category),
                                            difficulty = selectedDifficulty!!,
                                            reminderTime = reminderTime,
                                            days = selectedDays,
                                            onComplete = { result ->
                                                when {
                                                    result.isSuccess -> {
                                                        navController.navigate(route = AppScreens.HomeScreen.route) {
                                                            popUpTo(AppScreens.AddingGoodHabitScreen.route) {
                                                                inclusive = true
                                                            }
                                                        }
                                                    }

                                                    result.isFailure -> { /* Show error */
                                                    }
                                                }
                                            }
                                        )

                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = selectedDays.isNotEmpty() && selectedDifficulty != null,
                                content = {
                                    Text(
                                        text = stringResource(R.string.create),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }

                            )
                        }
                    }
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
}*/