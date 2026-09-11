package com.payx.app.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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

private val smoothDecel = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

@Composable
fun PayxApp() {
    val nav = rememberNavController()
    NavHost(
        navController = nav,
        startDestination = Routes.LOGIN,
        enterTransition = {
            fadeIn(animationSpec = tween(300, easing = LinearEasing))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(260, easing = LinearEasing))
        }
    ) {
        composable(
            route = Routes.LOGIN,
            exitTransition = {
                fadeOut(animationSpec = tween(260, easing = LinearEasing)) +
                        scaleOut(targetScale = 0.96f, animationSpec = tween(300, easing = smoothDecel))
            }
        ) {
            LoginScreen(onSignedIn = {
                nav.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            })
        }

        composable(
            route = Routes.DASHBOARD,
            enterTransition = {
                fadeIn(animationSpec = tween(280, easing = LinearEasing)) +
                        scaleIn(initialScale = 0.96f, animationSpec = tween(320, easing = smoothDecel))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(260, easing = LinearEasing)) +
                        scaleOut(targetScale = 0.95f, animationSpec = tween(320, easing = smoothDecel))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(280, easing = LinearEasing)) +
                        scaleIn(initialScale = 0.96f, animationSpec = tween(320, easing = smoothDecel))
            }
        ) {
            DashboardScreen(
                onSend = { nav.navigate(Routes.SEND) },
                onTrack = { id -> nav.navigate(Routes.tracker(id)) },
                onSettings = { nav.navigate(Routes.SETTINGS) }
            )
        }

        composable(
            route = Routes.SEND,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { (it * 0.16f).toInt() },
                    animationSpec = tween(360, easing = smoothDecel)
                ) + fadeIn(animationSpec = tween(280, easing = LinearEasing)) +
                        scaleIn(initialScale = 0.95f, animationSpec = tween(360, easing = smoothDecel))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(240, easing = LinearEasing)) +
                        scaleOut(targetScale = 0.96f, animationSpec = tween(280, easing = smoothDecel))
            },
            popExitTransition = {
                slideOutVertically(
                    targetOffsetY = { (it * 0.16f).toInt() },
                    animationSpec = tween(320, easing = smoothDecel)
                ) + fadeOut(animationSpec = tween(240, easing = LinearEasing)) +
                        scaleOut(targetScale = 0.95f, animationSpec = tween(320, easing = smoothDecel))
            }
        ) {
            SendScreen(
                onBack = { nav.popBackStack() },
                onSubmitted = { id -> nav.navigate(Routes.tracker(id)) }
            )
        }

        composable(
            route = Routes.TRACKER,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { (it * 0.14f).toInt() },
                    animationSpec = tween(360, easing = smoothDecel)
                ) + fadeIn(animationSpec = tween(280, easing = LinearEasing))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(240, easing = LinearEasing))
            }
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("transferId") ?: "px-demo-transfer"
            TrackerScreen(
                transferId = id,
                onDone = {
                    nav.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = false }
                    }
                }
            )
        }

        composable(Routes.RECEIVER) { ReceiverScreen() }

        composable(
            route = Routes.SETTINGS,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { (it * 0.14f).toInt() },
                    animationSpec = tween(340, easing = smoothDecel)
                ) + fadeIn(animationSpec = tween(260, easing = LinearEasing))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(240, easing = LinearEasing))
            },
            popExitTransition = {
                slideOutVertically(
                    targetOffsetY = { (it * 0.14f).toInt() },
                    animationSpec = tween(300, easing = smoothDecel)
                ) + fadeOut(animationSpec = tween(240, easing = LinearEasing))
            }
        ) {
            SettingsScreen(
                onBack = { nav.popBackStack() },
                onSignedOut = {
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

