package com.example.fati_market_frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.fati_market_frontend.ui.theme.FatiMarketFrontendTheme
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()  // Keep this

        // Block per-frame IME animation updates from reaching Compose so the keyboard
        // appears/disappears instantly instead of triggering a recompose on every frame.
        ViewCompat.setWindowInsetsAnimationCallback(
            window.decorView,
            object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {
                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>
                ) = insets
            }
        )

        // Configure Coil with memory + disk cache to avoid re-downloading images
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .memoryCache {
                    MemoryCache.Builder(this)
                        .maxSizePercent(0.25) // 25% of available memory
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(cacheDir.resolve("image_cache"))
                        .maxSizeBytes(50L * 1024 * 1024) // 50 MB
                        .build()
                }
                .crossfade(true)
                .build()
        )

        val prefs = getSharedPreferences("fatimarket_prefs", MODE_PRIVATE)

        setContent {
            var isDarkMode by remember {
                mutableStateOf(prefs.getBoolean("dark_mode", false))
            }

            FatiMarketFrontendTheme(darkTheme = isDarkMode) {
                // Wrap the entire app with ProvideWindowInsets
                androidx.compose.foundation.layout.WindowInsets.apply {
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
}