package com.jaime.ascend.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.jaime.ascend.R
import com.jaime.ascend.ui.components.BlackButton
import com.jaime.ascend.ui.navigation.AppScreens
import com.jaime.ascend.ui.theme.AppTheme

@Composable
fun SignupScreen(navController: NavController) {
    AppTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SignupContent(navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupContent(navController: NavController) {
    var username by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var repeatPassword by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(25.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ascendlogo_removebg),
            contentDescription = "Ascend Logo",
            modifier = Modifier.size(140.dp),
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
                Box(modifier = Modifier.align(Alignment.Start)) {
                    Text(
                        text = stringResource(id = R.string.welcome_text),
                        style = TextStyle(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = { Text(stringResource(id = R.string.username_hint), color = MaterialTheme.colorScheme.tertiary) },
                    label = { Text(stringResource(id = R.string.username_label), color = MaterialTheme.colorScheme.primary) },
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.primary),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text(stringResource(id = R.string.email_hint), color = MaterialTheme.colorScheme.tertiary) },
                    label = { Text(stringResource(id = R.string.email_label), color = MaterialTheme.colorScheme.primary) },
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.primary),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text(stringResource(id = R.string.password_hint), color = MaterialTheme.colorScheme.tertiary) },
                    label = { Text(stringResource(id = R.string.password_label), color = MaterialTheme.colorScheme.primary) },
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.primary),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = repeatPassword,
                    onValueChange = { repeatPassword = it },
                    placeholder = { Text(stringResource(id = R.string.repeat_password_hint), color = MaterialTheme.colorScheme.tertiary) },
                    label = { Text(stringResource(id = R.string.repeat_password_label), color = MaterialTheme.colorScheme.primary) },
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.primary),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                BlackButton(
                    onClick = { /*Todo - Manejar signup*/ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(stringResource(id = R.string.signup_button), color = Color.White, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(onClick = { navController.navigate(route = AppScreens.LoginScreen.route) }) {
                    Text(stringResource(id = R.string.login_prompt), color = MaterialTheme.colorScheme.tertiary, fontSize = 14.sp)
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
