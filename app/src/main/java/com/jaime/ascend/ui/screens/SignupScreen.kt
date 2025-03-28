package com.jaime.ascend.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.jaime.ascend.R
import com.jaime.ascend.ui.components.BlackButton
import com.jaime.ascend.ui.navigation.AppScreens
import com.jaime.ascend.ui.theme.AppTheme
import com.jaime.ascend.ui.theme.AppTypography
import com.jaime.ascend.ui.theme.displayFontFamily
import com.jaime.ascend.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(navController: NavController) {
    val viewModel = viewModel<AuthViewModel>()
    val emailPattern = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]{2,}")
    val emailErrorMessage = stringResource(id = R.string.signup_error_message_email_format)
    val passwordMismatchMessage =
        stringResource(id = R.string.signup_error_message_password_mismatch)
    val weakPasswordMessage = stringResource(id = R.string.signup_error_message_weak_password)
    val userCollisionMessage = stringResource(id = R.string.signup_error_message_email_in_use)
    val emptyFieldsMessage = stringResource(id = R.string.signup_error_message_empty_fields)
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var confirmPassword by remember { mutableStateOf(TextFieldValue("")) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(25.dp)
            .fillMaxSize()
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ascendlogo_removebg),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier
                .width(140.dp)
                .padding(top = 88.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(id = R.string.welcome_text),
                    style = AppTypography.headlineMedium.copy(
                        fontFamily = displayFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { newValue ->
                        email = newValue
                        errorMessage =
                            if (!emailPattern.matches(newValue.text)) emailErrorMessage else null
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
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { newValue ->
                        password = newValue
                        errorMessage = if (newValue.text.length < 6) weakPasswordMessage else null
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
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
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
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(modifier = Modifier.align(Alignment.Start)) {
                    TextButton(onClick = { /*Todo - Recover password*/ }) {
                        Text(
                            stringResource(id = R.string.forgot_password),
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                BlackButton(
                    onClick = {
                        if (password.text != confirmPassword.text) {
                            errorMessage = passwordMismatchMessage
                        } else if (password.text.isEmpty() || confirmPassword.text.isEmpty() || email.text.isEmpty()) {
                            errorMessage = emptyFieldsMessage
                        } else {
                            viewModel.signUp(email.text, password.text) { success ->
                                if (success) {
                                    navController.navigate(route = AppScreens.HomeScreen.route)
                                } else {
                                    errorMessage = userCollisionMessage
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        stringResource(id = R.string.signup_button),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 16.sp
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 10.dp)
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


@Preview(showBackground = true)
@Composable
fun SignupScreenPreview() {
    AppTheme {
        SignupScreen(navController = rememberNavController())
    }
}
