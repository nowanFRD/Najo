package com.najo

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.najo.data.PlannerStore
import com.najo.data.PlannerViewModel
import com.najo.ui.PlannerScreen
import com.najo.ui.components.LocalBrandPainter
import com.najo.ui.theme.NajoTheme
import java.awt.Dimension

fun main() = application {
    val viewModel = remember {
        val file = PlannerStore.defaultFile()
        val label = file.toString().replace(System.getProperty("user.home"), "~")
        PlannerViewModel(PlannerStore(file, label))
    }
    val icon = remember { loadWindowIcon() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Najo — Day Planner",
        icon = icon,
        state = rememberWindowState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(1200.dp, 820.dp),
        ),
        onPreviewKeyEvent = { event ->
            if (
                event.type == KeyEventType.KeyDown &&
                event.isCtrlPressed &&
                event.key == Key.N
            ) {
                if (!viewModel.editorOpen) viewModel.openNewTask()
                true
            } else {
                false
            }
        },
    ) {
        LaunchedEffect(Unit) {
            window.minimumSize = Dimension(1000, 700)
        }
        NajoTheme(darkTheme = viewModel.darkTheme) {
            CompositionLocalProvider(LocalBrandPainter provides icon) {
                PlannerScreen(viewModel)
            }
        }
    }
}

private fun loadWindowIcon(): Painter? {
    val stream = object {}.javaClass.getResourceAsStream("/icon.png") ?: return null
    return runCatching {
        BitmapPainter(stream.use { loadImageBitmap(it) })
    }.getOrNull()
}
