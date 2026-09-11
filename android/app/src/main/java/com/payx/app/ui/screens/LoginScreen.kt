package com.payx.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(onSignedIn: () -> Unit) {
    // Phase 3: Google sign-in (play-services-auth) -> POST /auth/google with
    // the Keystore-derived wallet address -> persist session JWT in DataStore.
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("PayX", style = MaterialTheme.typography.displayMedium)
        Text("Pay anyone, anywhere, in any currency.")
        Button(onClick = onSignedIn, modifier = Modifier.padding(top = 24.dp)) {
            Text("Sign in with Google")
        }
    }
}
