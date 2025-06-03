package com.jaime.ascend.ui.screens

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.jaime.ascend.R
import com.jaime.ascend.ui.theme.AppTheme
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Screen that presents a splash screen during the app's startup process.
 * It displays a loading indicator and a random quote from Firestore.
 * @author Jaime Martínez Fernández
 */
class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SplashScreenContent {
                finish()
            }
        }
    }
}

/**
 * Composable function that displays the splash screen content.
 * @param onTimeout Callback function to be executed when the splash screen timeout is reached.
 */
@Composable
fun SplashScreenContent(onTimeout: () -> Unit = {}) {
    val defaultText = stringResource(id = R.string.loading)
    val splashText = remember { mutableStateOf(defaultText) }
    val language = Locale.getDefault().language
    val db = FirebaseFirestore.getInstance()
    AppTheme {
        LaunchedEffect(Unit) {
            db.collection("motivationQuotes")
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val randomQuote = documents.documents.random()
                        val quote = randomQuote[language] as? String ?: defaultText
                        splashText.value = quote
                    } else {
                        Log.e("SplashScreen", "No quotes found in Firestore")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("SplashScreen", "Failed to load quotes from Firestore", exception)
                }

            delay(3000)
            onTimeout()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(25.dp))

                Text(
                    text = splashText.value,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 50.dp)
                )
            }
        }
    }
}