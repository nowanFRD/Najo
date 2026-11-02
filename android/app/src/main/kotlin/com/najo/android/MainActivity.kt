package com.najo.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import com.najo.data.PlannerStore
import com.najo.data.PlannerViewModel
import com.najo.ui.PlannerScreen
import com.najo.ui.theme.NajoTheme
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val store = PlannerStore(
            file = File(filesDir, "planner.json").toPath(),
            displayName = "app storage",
        )

        setContent {
            val viewModel = remember { PlannerViewModel(store) }
            val dark = viewModel.darkTheme

            LaunchedEffect(dark) {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }

            BackHandler(enabled = viewModel.editorOpen) {
                viewModel.closeEditor()
            }

            NajoTheme(darkTheme = dark) {
                PlannerScreen(viewModel)
            }
        }
    }
}
