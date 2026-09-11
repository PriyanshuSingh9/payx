package com.payx.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.payx.app.ui.PayxApp
import com.payx.app.ui.theme.PayxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PayxTheme {
                PayxApp()
            }
        }
    }
}
