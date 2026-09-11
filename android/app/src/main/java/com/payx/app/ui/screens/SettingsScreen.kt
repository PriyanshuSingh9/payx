package com.payx.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(onSignedOut: () -> Unit) {
    // Phase 3: profile, wallet address display, sign out (clear DataStore +
    // Google sign-out; keys stay in the Keystore).
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onSignedOut, modifier = Modifier.padding(top = 16.dp)) { Text("Sign out") }
    }
}
