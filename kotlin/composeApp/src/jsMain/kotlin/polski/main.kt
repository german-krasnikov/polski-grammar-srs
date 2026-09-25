package polski

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.jetbrains.skiko.wasm.onWasmReady
import polski.ui.TrainingWebApp

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        ComposeViewport(viewportContainerId = "root") {
            TrainingWebApp()
        }
    }
}
