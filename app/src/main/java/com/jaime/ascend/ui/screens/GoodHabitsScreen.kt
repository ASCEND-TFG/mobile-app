package com.jaime.ascend.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jaime.ascend.ui.theme.AppTheme

@Composable
fun GoodHabitsScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Good habits screen", style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview
@Composable
fun GoodHabitsPreview() {
    AppTheme {
        GoodHabitsScreen()
    }
}