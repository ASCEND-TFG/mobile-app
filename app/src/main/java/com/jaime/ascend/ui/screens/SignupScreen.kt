package com.jaime.ascend.ui.screens

import AuthViewModel
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.jaime.ascend.R
import com.jaime.ascend.ui.components.BlackButton
import com.jaime.ascend.ui.navigation.AppScreens
import com.jaime.ascend.ui.theme.AppTheme
import com.jaime.ascend.ui.theme.AppTypography
import com.jaime.ascend.ui.theme.displayFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(navController: NavController) {
    val viewModel = viewModel<AuthViewModel>()
    val firestore = FirebaseFirestore.getInstance()
    val emailPattern = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]{2,}")

    val emailErrorMessage = stringResource(id = R.string.signup_error_message_email_format)
    val passwordMismatchMessage = stringResource(id = R.string.signup_error_message_password_mismatch)
    val weakPasswordMessage = stringResource(id = R.string.signup_error_message_weak_password)
    val longUsernameMessage = stringResource(id = R.string.signup_error_message_long_username)
    val shortUsernameMessage = stringResource(id = R.string.signup_error_message_short_username)
    val emailCollisionMessage = stringResource(id = R.string.signup_error_message_email_in_use)
    val userCollisionMessage = stringResource(id = R.string.signup_error_message_username_in_use)

    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var username by remember { mutableStateOf(TextFieldValue("")) }
    var confirmPassword by remember { mutableStateOf(TextFieldValue("")) }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }

    var showVerificationMessage by remember { mutableStateOf(false) }

    val ctx = LocalContext.current

    fun validateUsername() {
        usernameError = when {
            username.text.isEmpty() -> null
            username.text.length < 3 -> shortUsernameMessage
            username.text.length > 15 -> longUsernameMessage
            else -> null
        }
    }

    fun validateEmail() {
        emailError = when {
            email.text.isEmpty() -> null
            !emailPattern.matches(email.text) -> emailErrorMessage
            else -> null
        }
    }

    fun validateConfirmPassword() {
        confirmPasswordError = when {
            confirmPassword.text.isEmpty() -> null
            password.text != confirmPassword.text -> passwordMismatchMessage
            else -> null
        }
    }

    fun validatePassword() {
        passwordError = when {
            password.text.isEmpty() -> null
            password.text.length < 6 -> weakPasswordMessage
            else -> null
        }
        validateConfirmPassword()
    }

    fun isFormValid(): Boolean {
        return username.text.isNotEmpty() &&
                email.text.isNotEmpty() &&
                password.text.isNotEmpty() &&
                confirmPassword.text.isNotEmpty() &&
                usernameError == null &&
                emailError == null &&
                passwordError == null &&
                confirmPasswordError == null
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
                                value = username,
                                onValueChange = { newValue ->
                                    username = newValue
                                    validateUsername()
                                },
                                isError = usernameError != null,
                                supportingText = {
                                    if (usernameError != null) {
                                        Text(
                                            text = usernameError!!,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                placeholder = {
                                    Text(
                                        stringResource(id = R.string.username_hint),
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                },
                                label = {
                                    Text(
                                        stringResource(id = R.string.username_label),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    errorBorderColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = email,
                                onValueChange = { newValue ->
                                    email = newValue
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
                                onValueChange = { newValue ->
                                    password = newValue
                                    validatePassword()
                                },
                                isError = passwordError != null,
                                supportingText = {
                                    if (passwordError != null) {
                                        Text(
                                            text = passwordError!!,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
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
                                textStyle = TextStyle(color = MaterialTheme.colorScheme.primary),
                                visualTransformation = PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    errorBorderColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { newValue ->
                                    confirmPassword = newValue
                                    validateConfirmPassword()
                                },
                                isError = confirmPasswordError != null,
                                supportingText = {
                                    if (confirmPasswordError != null) {
                                        Text(
                                            text = confirmPasswordError!!,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                placeholder = {
                                    Text(
                                        stringResource(id = R.string.repeat_password_hint),
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                },
                                label = {
                                    Text(
                                        stringResource(id = R.string.repeat_password_hint),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                textStyle = TextStyle(color = MaterialTheme.colorScheme.primary),
                                visualTransformation = PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    errorBorderColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            BlackButton(
                                onClick = {
                                    formError = null

                                    firestore.collection("users")
                                        .whereEqualTo("username", username.text)
                                        .get()
                                        .addOnSuccessListener { querySnapshot ->
                                            if (!querySnapshot.isEmpty) {
                                                formError = userCollisionMessage
                                            } else {
                                                viewModel.signUp(email.text, password.text, username.text) { success ->
                                                    if (success) {
                                                        viewModel.sendEmailVerification { emailSent ->
                                                            if (emailSent) {
                                                                navController.navigate(AppScreens.EmailVerificationScreen.route) {
                                                                    popUpTo(AppScreens.SignupScreen.route) {
                                                                        inclusive = true
                                                                    }
                                                                }
                                                            } else {
                                                                formError = "Error al enviar el email de verificación"
                                                            }
                                                        }
                                                    } else {
                                                        formError = emailCollisionMessage
                                                    }
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
                                        text = stringResource(id = R.string.signup_button),
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

                            TextButton(onClick = { navController.navigate(route = AppScreens.LoginScreen.route) }) {
                                Text(
                                    stringResource(id = R.string.login_prompt),
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        /*if (showVerificationMessage) {
            AlertDialog(
                onDismissRequest = { showVerificationMessage = false },
                title = { Text("Verifica tu email") },
                text = { Text("Hemos enviado un enlace de verificación a ${email.text}. Por favor revisa tu bandeja de entrada.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.sendEmailVerification()
                            // Opcional: mostrar toast de confirmación
                            Toast.makeText(ctx, "Email reenviado", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Reenviar verificación")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showVerificationMessage = false
                            navController.navigate(AppScreens.LoginScreen.route)
                        }
                    ) {
                        Text("Ir a login")
                    }
                }
            )
        }*/
    }
}