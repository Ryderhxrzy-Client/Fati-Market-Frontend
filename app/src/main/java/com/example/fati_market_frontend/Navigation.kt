package com.example.fati_market_frontend

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation(isDarkMode: Boolean, onThemeToggle: () -> Unit) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable("splash") {
            // TODO: change destination to "login" when done debugging
            SplashScreen(navController, destination = "admin_home")
        }
//        composable("login") {
//            LoginScreen(navController)
//        }
        composable("signup") {
            SignUpScreen(navController)
        }
        composable("forgot_password") {
            ForgotPasswordScreen(navController)
        }
        composable("admin_home") {
            AdminDashboard(
                isDarkMode = isDarkMode,
                onThemeToggle = onThemeToggle
            )
        }
    }
}
