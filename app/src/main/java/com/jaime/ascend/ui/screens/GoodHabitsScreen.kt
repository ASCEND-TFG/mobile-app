package com.jaime.ascend.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jaime.ascend.R
import com.jaime.ascend.data.factory.GoodHabitsViewModelFactory
import com.jaime.ascend.data.factory.RewardsViewModelFactory
import com.jaime.ascend.data.repository.CategoryRepository
import com.jaime.ascend.data.repository.GoodHabitRepository
import com.jaime.ascend.data.repository.TemplateRepository
import com.jaime.ascend.ui.components.BlackButton
import com.jaime.ascend.ui.components.GoodHabitCard
import com.jaime.ascend.ui.navigation.AppScreens
import com.jaime.ascend.ui.theme.AppTheme
import com.jaime.ascend.viewmodel.GoodHabitsViewModel
import com.jaime.ascend.viewmodel.RewardsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Good habits screen.
 * It contains the list of good habits.
 * @author Jaime Martínez Fernández
 * @param navController Navigation controller.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoodHabitsScreen(
    navController: NavController,
) {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val viewModel: GoodHabitsViewModel = viewModel(
        factory = GoodHabitsViewModelFactory(
            categoryRepository = CategoryRepository(firestore),
            habitRepository = GoodHabitRepository(firestore, auth, LocalContext.current),
            templateRepository = TemplateRepository(firestore),
            context = LocalContext.current
        )
    )
    val rewardsViewModel: RewardsViewModel = viewModel(
        factory = RewardsViewModelFactory(
            auth = auth,
            firestore = firestore,
        )
    )

    val configuration = LocalConfiguration.current
    val currentLocale by rememberUpdatedState(configuration.locales[0])
    val context = LocalContext.current

    val habits by viewModel.habits
    val isLoading by viewModel.isLoading
    val error by viewModel.error

    LaunchedEffect(auth.currentUser) {
        auth.currentUser?.uid?.let { uid ->
            viewModel.loadHabits(uid)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 0.dp)
        ) {
            Text(
                text = getFormattedDate(context, currentLocale),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else if (error != null) {
                Text("Error: $error")
            } else if (habits.isEmpty()) {
                LazyColumn {
                    item {
                        Text(
                            text = stringResource(R.string.no_habits),
                            textAlign = TextAlign.Center
                        )
                    }

                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            BlackButton(
                                onClick = { navController.navigate(AppScreens.AddNewGoodHabitScreen.route) },
                                modifier = Modifier
                                    .width(250.dp)
                                    .padding(bottom = 4.dp, top = 24.dp),
                                enabled = true,
                                content = {
                                    Text(
                                        text = stringResource(R.string.add_new_good_habit_title),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            )
                        }
                    }
                }
            } else {
                LazyColumn {
                    items(habits) { habit ->
                        GoodHabitCard(
                            habit = habit,
                            onHabitClick = { clickedHabit ->
                                navController.navigate("ghabit_details/${clickedHabit.id}")
                            },
                            viewModel = rewardsViewModel
                        )
                    }

                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            BlackButton(
                                onClick = { navController.navigate(AppScreens.AddNewGoodHabitScreen.route) },
                                modifier = Modifier
                                    .width(250.dp)
                                    .padding(bottom = 4.dp, top = 24.dp),
                                enabled = true,
                                content = {
                                    Text(
                                        text = stringResource(R.string.add_new_good_habit_title),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Get the formatted date.
 * @param context Context.
 * @param locale Locale.
 * @return The formatted date.
 */
private fun getFormattedDate(context: android.content.Context, locale: Locale): String {
    val pattern = if (locale.language == "es") "dd MMMM, yyyy" else "dd MMMM, yyyy"
    return SimpleDateFormat(pattern, locale).format(Date())
}
