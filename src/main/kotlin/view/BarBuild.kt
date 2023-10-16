@file:OptIn(ExperimentalComposeUiApi::class)

package view

import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.*

@Composable
fun FrameWindowScope.MenuBarWeather(
    isOpen: MutableState<Boolean>,
    showTray: MutableState<Boolean>,
    onClick: (action: String) -> Unit = {},
): Boolean {
    var isSubmenuShowing by remember { mutableStateOf(true) }
    var action by remember { mutableStateOf("Last action: None") }

    MenuBar {
        Menu("文件", mnemonic = 'F') {
            Item(
                "复制（假的）",
                onClick = { action = "Last action: Copy" },
                shortcut = KeyShortcut(Key.C, ctrl = true)
            )
            Item(
                "粘贴（假的）",
                onClick = { action = "Last action: Paste" },
                shortcut = KeyShortcut(Key.V, ctrl = true)
            )
        }
        Menu("显示", mnemonic = 'A') {
            CheckboxItem(
                "显示托盘",
                checked = showTray.value,
                onCheckedChange = {
                    showTray.value = !showTray.value
                }
            )
            CheckboxItem(
                "高级设置",
                checked = isSubmenuShowing,
                onCheckedChange = {
                    isSubmenuShowing = !isSubmenuShowing
                }
            )
            if (isSubmenuShowing) {
                Menu("设置") {
                    Item("设置一", onClick = { action = "Last action: Setting 1" })
                    Item("设置二", onClick = { action = "Last action: Setting 2" })
                }
            }
            Separator()
            Item(
                "关于",
                icon = painterResource("image/launcher.png"),
                onClick = { action = "Last action: About" })
            Item(
                "退出",
                onClick = { isOpen.value = false },
                shortcut = KeyShortcut(Key.Escape),
                mnemonic = 'E'
            )
        }
        Menu("帮助", mnemonic = 'H') {
            Item("导入帮助", onClick = {
                action = "Last action: Help"
                onClick(action)
            })
        }
    }
    println("action:$action")
    return showTray.value
}

@Composable
fun ApplicationScope.BuildTray(
    isOpen: MutableState<Boolean>,
    showTray: MutableState<Boolean>
): Boolean {
    if (!showTray.value) {
        return isOpen.value
    }
    val trayState = rememberTrayState()
    val infoNotification = rememberNotification("更多功能1", "更多功能1", Notification.Type.Info)
    val warnNotification =
        rememberNotification("更多功能2", "更多功能2", type = Notification.Type.Warning)

    Tray(
        state = trayState,
        icon = painterResource("image/launcher.png"),
        menu = {
            Item(
                "通知1",
                onClick = {
                    trayState.sendNotification(infoNotification)
                }
            )
            Item(
                "通知2",
                onClick = {
                    trayState.sendNotification(warnNotification)
                }
            )
            Separator()
            Item(
                "退出",
                onClick = {
                    isOpen.value = false
                }
            )
        }
    )
    return isOpen.value
}
