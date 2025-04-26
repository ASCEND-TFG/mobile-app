package com.jaime.ascend.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.jaime.ascend.R
import com.jaime.ascend.ui.components.BlackButton
import com.jaime.ascend.ui.navigation.AppScreens
import com.jaime.ascend.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GoodHabitsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 0.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = getFormattedDate(LocalContext.current),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Habits will be listed here",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        BlackButton(
            onClick = { navController.navigate(AppScreens.AddNewGoodHabitScreen.route) },
            modifier = Modifier.width(250.dp).align(Alignment.CenterHorizontally),
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

private fun getFormattedDate(context: android.content.Context): String {
    val locale = if (context.resources.configuration.locales[0].language == "es") {
        Locale("es", "ES")
    } else {
        Locale.ENGLISH
    }

    val pattern = if (locale.language == "es") {
        "dd MMMM, yyyy"
    } else {
        "dd MMMM, yyyy"
    }

    val dateFormat = SimpleDateFormat(pattern, locale)
    return dateFormat.format(Date())
}

@Preview
@Composable
fun GoodHabitsPreview() {
    AppTheme {
        GoodHabitsScreen(navController = rememberNavController())
    }
}