package com.jaime.ascend.ui.screens

import AuthViewModel
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jaime.ascend.R
import com.jaime.ascend.ui.components.BlackButton
import com.jaime.ascend.ui.navigation.AppScreens
import com.jaime.ascend.ui.theme.AppTheme
import com.jaime.ascend.ui.theme.AppTypography
import com.jaime.ascend.ui.theme.displayFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, navController: NavController) {
    val viewModel = viewModel<AuthViewModel>()
    val emailPattern = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]{2,}")
    val loginErrorMessage = stringResource(id = R.string.login_error_message_credentials)
    val emptyFieldsMessage = stringResource(id = R.string.signup_error_message_empty_fields)
    val emailErrorMessage = stringResource(id = R.string.signup_error_message_email_format)

    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }
    val ctx: Context = LocalContext.current

    fun validateEmail() {
        emailError = when {
            email.text.isEmpty() -> null
            !emailPattern.matches(email.text) -> emailErrorMessage
            else -> null
        }
    }

    fun isFormValid(): Boolean {
        return email.text.isNotEmpty() &&
                password.text.isNotEmpty() &&
                emailError == null
    }

    AppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(25.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ascendlogo_removebg),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier
                            .width(150.dp)
                            .padding(vertical = 20.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(24.dp)
                    ) {
                        Column {
                            Text(
                                text = stringResource(id = R.string.welcome_text),
                                style = AppTypography.headlineMedium.copy(
                                    fontFamily = displayFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                ),
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    validateEmail()
                                },
                                isError = emailError != null,
                                supportingText = {
                                    if (emailError != null) {
                                        Text(
                                            text = emailError!!,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                placeholder = {
                                    Text(
                                        stringResource(id = R.string.email_hint),
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                },
                                label = {
                                    Text(
                                        stringResource(id = R.string.email_label),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    errorBorderColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = {
                                    Text(
                                        stringResource(id = R.string.password_hint),
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                },
                                label = {
                                    Text(
                                        stringResource(id = R.string.password_label),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        formError = null
                                        validateEmail()

                                        if (emailError == null) {
                                            viewModel.signIn(email.text, password.text) { success ->
                                                if (success) {
                                                    navController.navigate(route = AppScreens.HomeScreen.route) {
                                                        popUpTo(AppScreens.LoginScreen.route) {
                                                            inclusive = true
                                                        }
                                                    }
                                                } else if (password.text.isEmpty() || email.text.isEmpty()) {
                                                    formError = emptyFieldsMessage
                                                } else {
                                                    formError = loginErrorMessage
                                                    Log.e("AuthRepository", "Failed login")
                                                }
                                            }
                                        }
                                    }
                                ),
                                textStyle = TextStyle(color = MaterialTheme.colorScheme.primary),
                                visualTransformation = PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(5.dp))

                            TextButton(onClick = {
                                if (email.text.isBlank()) {
                                    formError = ctx.getString(R.string.recover_email)
                                } else if (emailError != null) {
                                    formError = ctx.getString(R.string.correct_email_format)
                                } else {
                                    viewModel.sendPasswordResetEmail(email.text) { success ->
                                        if (success) {
                                            formError = ctx.getString(R.string.recovery_email_send)
                                        } else {
                                            formError = ctx.getString(R.string.recovery_email_error)
                                        }
                                    }
                                }
                            }) {
                                Text(
                                    stringResource(id = R.string.forgot_password),
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            BlackButton(
                                onClick = {
                                    formError = null
                                    validateEmail()

                                    if (emailError == null) {
                                        viewModel.signIn(email.text, password.text) { success ->
                                            if (success) {
                                                navController.navigate(route = AppScreens.HomeScreen.route) {
                                                    popUpTo(AppScreens.LoginScreen.route) {
                                                        inclusive = true
                                                    }
                                                }
                                            } else if (password.text.isEmpty() || email.text.isEmpty()) {
                                                formError = emptyFieldsMessage
                                            } else {
                                                formError = loginErrorMessage
                                                Log.e("AuthRepository", "Failed login")
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                enabled = isFormValid(),
                                content = {
                                    Text(
                                        stringResource(id = R.string.login_button),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 16.sp
                                    )
                                }
                            )

                            if (formError != null) {
                                Text(
                                    text = formError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .padding(top = 10.dp)
                                        .fillMaxWidth()
                                        .wrapContentWidth(Alignment.CenterHorizontally)
                                        .align(Alignment.CenterHorizontally)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            TextButton(onClick = { navController.navigate(route = AppScreens.SignupScreen.route) }) {
                                Text(
                                    stringResource(id = R.string.signup_prompt),
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}