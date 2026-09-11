package com.payx.app.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.payx.app.auth.AuthViewModel
import com.payx.app.payments.DashboardViewModel
import com.payx.app.payments.SendViewModel
import com.payx.app.payments.TrackerViewModel
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
fun PayxApp(authViewModel: AuthViewModel = viewModel()) {
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val startDestination = remember {
        if (authViewModel.state.value.user != null) Routes.DASHBOARD else Routes.LOGIN
    }
    val nav = rememberNavController()
    val activity = LocalContext.current as Activity

    LaunchedEffect(authState.user) {
        val route = nav.currentDestination?.route
        if (authState.user != null && route == Routes.LOGIN) {
            nav.navigate(Routes.DASHBOARD) {
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
        } else if (authState.user == null && route != null && route != Routes.LOGIN) {
            nav.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = nav, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            LoginScreen(
                isLoading = authState.isLoading,
                error = authState.error,
                onContinueWithGoogle = { country ->
                    authViewModel.signIn(activity, country)
                }
            )
        }
        composable(Routes.DASHBOARD) {
            val dashboardViewModel: DashboardViewModel = viewModel()
            val dashboardState by dashboardViewModel.state.collectAsStateWithLifecycle()
            val lifecycleOwner = LocalLifecycleOwner.current
            LaunchedEffect(lifecycleOwner) {
                lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    dashboardViewModel.refresh()
                }
            }
            DashboardScreen(
                user = authState.user,
                state = dashboardState,
                onSend = { nav.navigate(Routes.SEND) },
                onTrack = { id -> nav.navigate(Routes.tracker(id)) },
                onSettings = { nav.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.SEND) {
            val sendViewModel: SendViewModel = viewModel()
            val sendState by sendViewModel.state.collectAsStateWithLifecycle()
            LaunchedEffect(sendState.createdPaymentId) {
                val paymentId = sendState.createdPaymentId ?: return@LaunchedEffect
                sendViewModel.consumeCreatedPayment()
                nav.navigate(Routes.tracker(paymentId))
            }
            SendScreen(
                state = sendState,
                onSearchQueryChange = sendViewModel::onSearchQueryChange,
                onAmountChange = sendViewModel::onAmountChange,
                onRefreshRecipients = sendViewModel::refreshRecipients,
                onSubmit = { recipient ->
                    sendViewModel.submit(recipient, authState.user?.walletAddress)
                },
                onBack = { nav.popBackStack() }
            )
        }
        composable(Routes.TRACKER) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("transferId").orEmpty()
            val trackerViewModel: TrackerViewModel = viewModel()
            val trackerState by trackerViewModel.state.collectAsStateWithLifecycle()
            LaunchedEffect(id) { trackerViewModel.start(id) }
            TrackerScreen(
                state = trackerState,
                onDone = {
                    nav.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = false }
                    }
                }
            )
        }
        composable(Routes.RECEIVER) { ReceiverScreen() }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                user = authState.user,
                onBack = { nav.popBackStack() },
                onSignedOut = { authViewModel.signOut() }
            )
        }
    }
}
