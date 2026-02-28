package com.example.fati_market_frontend

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

private const val SESSION_DURATION_MS = 2L * 60 * 60 * 1000  // 2 hours

@Composable
fun AppNavigation(isDarkMode: Boolean, onThemeToggle: () -> Unit) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = "splash",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable("splash") {
            // Check whether a valid (<2 h) session already exists
            val destination = remember {
                val prefs     = context.getSharedPreferences("fatimarket_prefs", 0)
                val token     = prefs.getString("auth_token", null)
                val loginTime = prefs.getLong("login_timestamp", 0L)
                val elapsed   = System.currentTimeMillis() - loginTime
                if (token != null && elapsed < SESSION_DURATION_MS) "admin_home" else "login"
            }
            SplashScreen(navController, destination = destination)
        }
        composable("login") {
            LoginScreen(navController)
        }
        composable("signup") {
            SignUpScreen(navController)
        }
        composable("forgot_password") {
            ForgotPasswordScreen(navController)
        }
        composable("admin_home") {
            AdminDashboard(
                isDarkMode = isDarkMode,
                onThemeToggle = onThemeToggle,
                onLogout = {
                    context.getSharedPreferences("fatimarket_prefs", 0).edit().clear().apply()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
