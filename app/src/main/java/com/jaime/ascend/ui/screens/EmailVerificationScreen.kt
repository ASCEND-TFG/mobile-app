package com.jaime.ascend.ui.screens

import AuthViewModel
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jaime.ascend.ui.navigation.AppScreens
import kotlinx.coroutines.delay

@Composable
fun EmailVerificationScreen(navController: NavController) {
    val viewModel = viewModel<AuthViewModel>()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val ctx = LocalContext.current

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000) // Verificar cada 5 segundos
            viewModel.checkEmailVerifiedWithReload { isVerified ->
                if (isVerified) {
                    navController.navigate(AppScreens.HomeScreen.route) {
                        popUpTo(AppScreens.EmailVerificationScreen.route) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Por favor verifica tu email", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                viewModel.sendEmailVerification { success ->
                    isLoading = false
                    if (success) {
                        Toast.makeText(ctx, "Email de verificación reenviado", Toast.LENGTH_SHORT).show()
                    } else {
                        errorMessage = "Error al reenviar el email"
                    }
                }
            },
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Reenviar email de verificación")
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}