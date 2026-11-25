package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.firebase.ThemePreferences
import com.example.ui.screens.AppNavigation
import com.example.ui.theme.AulaVirtualTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefs = remember { ThemePreferences(applicationContext) }
            val darkOverride by prefs.darkModeFlow.collectAsStateWithLifecycle(initialValue = null)
            val systemDark = isSystemInDarkTheme()
            val useDark = darkOverride ?: systemDark
            val scope = rememberCoroutineScope()

            AulaVirtualTheme(darkTheme = useDark) {
                AppNavigation(
                    isDarkTheme = useDark,
                    onToggleDarkTheme = { enabled ->
                        scope.launch { prefs.setDarkMode(enabled) }
                    }
                )
            }
        }
    }
}
