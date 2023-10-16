import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import utils.HelpDialog
import view.BuildTray
import view.MenuBarWeather

@Composable
@Preview
fun App() {
    val appViewModel = AppViewModel()
    MaterialTheme {
        MainPage(appViewModel)
    }
}


fun main() = application {
    val isOpen = rememberSaveable { mutableStateOf(true) }
    val showTray = rememberSaveable { mutableStateOf(true) }
    val helpDialog = remember { mutableStateOf(false) }
    HelpDialog(helpDialog)
    if (isOpen.value) {
        isOpen.value = BuildTray(isOpen, showTray)
        Window(
            onCloseRequest = {
                isOpen.value = false
            },
            title = "导出Android图标(by aijiu)",
            state = rememberWindowState(width = 800.dp, height = 600.dp),
            icon = painterResource("image/launcher.png")
        ) {
            showTray.value = MenuBarWeather(isOpen, showTray, onClick = {
                if (it == "Last action: Help") {
                    helpDialog.value = true
                }
            })
            App()
        }
    }
}
