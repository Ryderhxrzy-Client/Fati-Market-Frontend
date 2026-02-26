package com.example.fati_market_frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.fati_market_frontend.ui.theme.FatiMarketFrontendTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences("fatimarket_prefs", MODE_PRIVATE)

        setContent {
            var isDarkMode by remember {
                mutableStateOf(prefs.getBoolean("dark_mode", false))
            }

            FatiMarketFrontendTheme(darkTheme = isDarkMode) {
                AppNavigation(
                    isDarkMode = isDarkMode,
                    onThemeToggle = {
                        isDarkMode = !isDarkMode
                        prefs.edit().putBoolean("dark_mode", isDarkMode).apply()
                    }
                )
            }
        }
    }
}
