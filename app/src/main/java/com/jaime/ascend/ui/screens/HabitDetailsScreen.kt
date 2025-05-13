package com.jaime.ascend.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.jaime.ascend.viewmodel.HabitDetailViewModel
import kotlinx.coroutines.tasks.await
import java.util.Locale

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    navController: NavController,
    habitId: String,
    viewModel: HabitDetailViewModel = viewModel(
        factory = HabitDetailViewModelFactory(habitId)
    )
) {
    val habit by viewModel.habit.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var template by remember { mutableStateOf<HabitTemplate?>(null) }
    var category by remember { mutableStateOf<Category?>(null) }

    // Cargar template y category
    LaunchedEffect(habit) {
        habit?.let { currentHabit ->
            currentHabit.template?.get()?.await()?.toObject(HabitTemplate::class.java)?.let {
                template = it
            }
            currentHabit.category?.get()?.await()?.toObject(Category::class.java)?.let {
                category = it
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ActionBarWithBackButton(
                screenName = stringResource(id = R.string.habit_details),
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

                    habit != null -> {
                        val currentHabit = habit!!
                        HabitDetailContent(
                            habit = currentHabit,
                            template = template,
                            onDeleteClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }


    )

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                viewModel.deleteHabit(habitId)
                navController.popBackStack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun HabitDetailContent(
    habit: GoodHabit,
    template: HabitTemplate?,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Nombre del hábito
        Text(
            text = template?.getName(Locale.getDefault()) ?: habit.id,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )

        // Descripción
        Text(
            text = template?.getDescription(Locale.getDefault()) ?: "",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        val difficultyText = when (habit.difficulty.toString()) {
            "EASY" -> stringResource(R.string.easy)
            "MEDIUM" -> stringResource(R.string.medium)
            "HARD" -> stringResource(R.string.hard)
            else -> habit.difficulty.toString().lowercase().capitalize(Locale.getDefault())
        }

        // Dificultad
        DetailSection(
            title = stringResource(R.string.difficulty),
            content = difficultyText
        )

        // Recordatorio
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.reminder),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = habit.reminderTime ?: stringResource(R.string.no_reminder_set),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = if (habit.reminderTime != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Días seleccionados con estilo de círculos
        Text(
            text = stringResource(R.string.days_selected),
            style = MaterialTheme.typography.titleMedium
        )

        DayOfWeekDisplay(
            selectedDays = habit.days,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Recompensas
        Text(
            text = stringResource(R.string.rewards),
            style = MaterialTheme.typography.titleMedium
        )

        RewardSection(
            coinReward = habit.coinReward,
            xpReward = habit.xpReward
        )

        Spacer(modifier = Modifier.weight(1f))

        // Botones de acción
        ActionButtons(
            onDeleteClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun RewardSection(
    coinReward: Int,
    xpReward: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.coins),
                style = MaterialTheme.typography.bodyMedium
            )

            Row(modifier = Modifier) {
                Text(
                    text = "+$coinReward",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.width(3.dp))

                Icon(
                    imageVector = Icons.Filled.Paid,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }


        }
        Column {
            Text(
                text = stringResource(R.string.experience),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "+${xpReward} XP",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}


@Composable
private fun DayOfWeekDisplay(
    selectedDays: List<Int>,
    modifier: Modifier = Modifier
) {
    val days = listOf(
        stringResource(R.string.monday_initial),
        stringResource(R.string.tuesday_initial),
        stringResource(R.string.wednesday_initial),
        stringResource(R.string.thursday_initial),
        stringResource(R.string.friday_initial),
        stringResource(R.string.saturday_initial),
        stringResource(R.string.sunday_initial)
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEachIndexed { index, day ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(
                        if (selectedDays.contains(index)) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    color = if (selectedDays.contains(index
                    )) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
private fun ActionButtons(
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            modifier = Modifier
                .weight(0.75f)
                .height(50.dp),
            onClick = onDeleteClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
            content = {
                Icon(
                    modifier = Modifier
                        .fillMaxSize(),
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        )

        BlackButton(
            onClick = { /* TODO: Implement edit navigation */ },
            modifier = Modifier.weight(3f),
            content = {
                Text(
                    text = stringResource(R.string.edit),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.delete_habit_title),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(stringResource(R.string.delete_habit_confirmation))
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.onError
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}