package com.payx.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReceiverScreen() {
    // Phase 3: GET /me/receiver-dashboard -> inbound totals + received list.
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Received", style = MaterialTheme.typography.headlineMedium)
    }
}
