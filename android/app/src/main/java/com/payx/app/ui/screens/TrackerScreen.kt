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
fun TrackerScreen(onDone: () -> Unit) {
    // Phase 3: poll GET /transfers/:id -> 4 stages (on-ramp, escrow locked,
    // off-ramp, completed) with Solscan link and shareable receipt.
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Payment tracker", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onDone, modifier = Modifier.padding(top = 16.dp)) { Text("Done") }
    }
}
