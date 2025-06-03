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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HeartBroken
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
import androidx.compose.ui.platform.LocalContext
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
import com.jaime.ascend.ui.components.RewardSection
import com.jaime.ascend.viewmodel.HabitDetailViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 *  Bad habit details screen.
 * It is used to show the details of a bad habit.
 * @param navController Navigation controller.
 */
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadHabitDetailsScreen(
    navController: NavController,
    habitId: String,
    viewModel: HabitDetailViewModel = viewModel(
        factory = HabitDetailViewModelFactory(habitId, false)
    )
) {
    val habit by viewModel.bhabit.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var template by remember { mutableStateOf<HabitTemplate?>(null) }
    var category by remember { mutableStateOf<Category?>(null) }
    val changesSaved by navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<Boolean>("changesSaved", false)
        ?.collectAsState() ?: remember { mutableStateOf(false) }
    val context = LocalContext.current
    var currentStreak by remember { mutableStateOf("0 min") }

    LaunchedEffect(habit?.createdAt, habit?.lastRelapse) {
        while (true) {
            habit?.let {
                currentStreak = it.calculateAndFormatCurrentStreak()
            }
            delay(60 * 1000)
        }
    }

    LaunchedEffect(changesSaved) {
        if (changesSaved) {
            viewModel.loadBadHabit(habitId)
            navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("changesSaved")
        }
    }

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
                        BadHabitDetailContent(
                            habit = habit!!,
                            template = template,
                            streakTime = currentStreak,
                            onDeleteClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxSize(),
                            navController = navController,
                        )
                    }
                }
            }
        }


    )

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                viewModel.deleteBadHabit(habitId)
                navController.popBackStack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

/**
 * Bad habit detail content.
 * It is used to show the details of a bad habit.
 * @param habit Habit.
 * @param template Habit template.
 * @param streakTime Streak time.
 * @param onDeleteClick On delete click.
 * @param modifier Modifier.
 * @param navController Navigation controller.
 */
@Composable
private fun BadHabitDetailContent(
    habit: BadHabit,
    template: HabitTemplate?,
    streakTime: String,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = template?.getName(Locale.getDefault()) ?: habit.id,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )

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

        DetailSection(
            title = stringResource(R.string.difficulty),
            content = difficultyText
        )

        Spacer(modifier = Modifier.height(8.dp))

        DetailSection(
            title = stringResource(R.string.streak),
            content = streakTime
        )

        Spacer(modifier = Modifier.height(8.dp))

        DetailSection(
            title = stringResource(R.string.last_relapse),
            content = habit.lastRelapse.formatToDayMonthYear()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.passive_rewards),
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RewardSection(
                coinReward = habit.coinReward,
                xpReward = habit.xpReward,
                modifier = Modifier.weight(1f)
            )
            LifeLossSection(
                lifeloss = habit.lifeLoss,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        // Botones
        ActionButtons(
            onDeleteClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth(),
            navController = navController,
            habitId = habit.id
        )
    }

}

/**
 * Life loss section.
 * It is used to show the life loss of a bad habit.
 * @param lifeloss Life loss.
 * @param modifier Modifier.
 */
@Composable
fun LifeLossSection(
    lifeloss: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.life_loss),
                style = MaterialTheme.typography.bodyMedium
            )

            Row(modifier = Modifier) {
                Text(
                    text = "-$lifeloss",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.width(3.dp))

                Icon(
                    imageVector = Icons.Filled.HeartBroken,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

        }
    }
}

/**
 * Detail section.
 * It is used to show the details of a bad habit.
 * @param title Title.
 * @param content Content.
 * @param modifier Modifier.
 */
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

/**
 * Action buttons.
 * It is used to show the action buttons of a bad habit.
 * @param onDeleteClick On delete click.
 * @param modifier Modifier.
 * @param habitId Habit id.
 * @param navController Navigation controller.
 */
@Composable
private fun ActionButtons(
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    habitId: String,
    navController: NavController
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
            onClick = {
                Log.d("HabitDetail", "Editing habit with ID: $habitId")
                navController.navigate("edit_bhabit/${habitId}")
            },
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

/**
 * Delete confirmation dialog.
 * It is used to show the delete confirmation dialog.
 * @param onConfirm On confirm.
 * @param onDismiss On dismiss.
 */
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

/**
 * Format to day month year.
 * It is used to format a date to day month year.
 * @return Formatted date.
 */
fun Date?.formatToDayMonthYear(): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(this)
}