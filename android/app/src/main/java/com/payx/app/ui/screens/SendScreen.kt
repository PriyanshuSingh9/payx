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
fun SendScreen(onSubmitted: (String) -> Unit) {
    // Phase 3: corridor picker -> GET /rates quote card (fee, ETA, exact
    // receive amount) -> POST /recipients/validate -> POST /transfers ->
    // on-ramp widget -> onSubmitted(transferId).
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Send money", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = { onSubmitted("placeholder-id") }, modifier = Modifier.padding(top = 16.dp)) {
            Text("Confirm and pay")
        }
    }
}
