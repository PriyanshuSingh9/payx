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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
    const val TRACKER = "tracker/{transferId}?recipient={recipient}&inr={inr}&usd={usd}&timeTaken={timeTaken}"
    const val RECEIVER = "receiver"
    const val SETTINGS = "settings"

    fun tracker(
        transferId: String,
        recipient: String? = null,
        inr: String? = null,
        usd: String? = null,
        timeTaken: String? = null
    ): String {
        val base = "tracker/$transferId"
        val params = mutableListOf<String>()
        if (!recipient.isNullOrBlank()) params.add("recipient=${java.net.URLEncoder.encode(recipient, "UTF-8")}")
        if (!inr.isNullOrBlank()) params.add("inr=${java.net.URLEncoder.encode(inr, "UTF-8")}")
        if (!usd.isNullOrBlank()) params.add("usd=${java.net.URLEncoder.encode(usd, "UTF-8")}")
        if (!timeTaken.isNullOrBlank()) params.add("timeTaken=${java.net.URLEncoder.encode(timeTaken, "UTF-8")}")
        return if (params.isNotEmpty()) "$base?${params.joinToString("&")}" else base
    }
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
                onSubmitted = { id, recipient, inr, usd ->
                    nav.navigate(Routes.tracker(id, recipient, inr, usd))
                }
            )
        }

        composable(
            route = Routes.TRACKER,
            arguments = listOf(
                navArgument("transferId") { type = NavType.StringType },
                navArgument("recipient") {
                    type = NavType.StringType
                    defaultValue = "Priya Sharma"
                    nullable = true
                },
                navArgument("inr") {
                    type = NavType.StringType
                    defaultValue = "₹1,79,761.50"
                    nullable = true
                },
                navArgument("usd") {
                    type = NavType.StringType
                    defaultValue = "$2,000.00"
                    nullable = true
                },
                navArgument("timeTaken") {
                    type = NavType.StringType
                    defaultValue = "4.2s"
                    nullable = true
                }
            ),
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
            val recipient = backStackEntry.arguments?.getString("recipient") ?: "Priya Sharma"
            val inr = backStackEntry.arguments?.getString("inr") ?: "₹1,79,761.50"
            val usd = backStackEntry.arguments?.getString("usd") ?: "$2,000.00"
            val time = backStackEntry.arguments?.getString("timeTaken") ?: "4.2s"
            TrackerScreen(
                transferId = id,
                recipientName = recipient,
                inrAmount = inr,
                usdAmount = usd,
                timeTaken = time,
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

