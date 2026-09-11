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
fun DashboardScreen(onSend: () -> Unit, onTrack: (String) -> Unit, onSettings: () -> Unit) {
    // Phase 3: GET /me/dashboard -> balance hero, live rate ticker, corridor
    // picker, recent transfers (tap -> onTrack).
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onSend, modifier = Modifier.padding(top = 16.dp)) { Text("Send money") }
        Button(onClick = onSettings, modifier = Modifier.padding(top = 8.dp)) { Text("Settings") }
    }
}
