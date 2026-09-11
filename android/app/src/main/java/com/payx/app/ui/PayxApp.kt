package com.payx.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.payx.app.ui.screens.DashboardScreen
import com.payx.app.ui.screens.LoginScreen
import com.payx.app.ui.screens.ReceiverScreen
import com.payx.app.ui.screens.SendScreen
import com.payx.app.ui.screens.SettingsScreen
import com.payx.app.ui.screens.TrackerScreen

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val SEND = "send"
    const val TRACKER = "tracker/{transferId}"
    const val RECEIVER = "receiver"
    const val SETTINGS = "settings"

    fun tracker(transferId: String) = "tracker/$transferId"
}

@Composable
fun PayxApp() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) { LoginScreen(onSignedIn = { nav.navigate(Routes.DASHBOARD) }) }
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onSend = { nav.navigate(Routes.SEND) },
                onTrack = { id -> nav.navigate(Routes.tracker(id)) },
                onSettings = { nav.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.SEND) {
            SendScreen(onSubmitted = { id -> nav.navigate(Routes.tracker(id)) })
        }
        composable(Routes.TRACKER) { TrackerScreen(onDone = { nav.popBackStack() }) }
        composable(Routes.RECEIVER) { ReceiverScreen() }
        composable(Routes.SETTINGS) { SettingsScreen(onSignedOut = { nav.navigate(Routes.LOGIN) }) }
    }
}
