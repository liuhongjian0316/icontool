import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import utils.CommonUtils
import utils.CommonUtils.humanReadableByteCount
import utils.ShowDialog
import utils.openBrowse
import java.awt.Graphics
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetAdapter
import java.awt.dnd.DropTargetDropEvent
import java.io.File
import java.time.Instant
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

suspend fun pickFile(): File? {
    return withContext(Dispatchers.Default) {
        var selectedFile: File? = null
        SwingUtilities.invokeAndWait {
            val fileChooser = JFileChooser()
            val filter = FileNameExtensionFilter("css文件", "css")
            fileChooser.fileFilter = filter
            fileChooser.isAcceptAllFileFilterUsed = false
            fileChooser.isMultiSelectionEnabled = false

            val userHome = System.getProperty("user.home")
            val downloadsDirectory = File(userHome, "Downloads")
            if (downloadsDirectory.exists()) {
                fileChooser.currentDirectory = downloadsDirectory
            }
            val result = fileChooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.selectedFile
            }
        }
        selectedFile
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainPage(appViewModel: AppViewModel) {
    val fileList by appViewModel.fileList.collectAsState(mutableListOf())
    val currentFile by appViewModel.currentFile.collectAsState(null)
    val currentFileIndex by appViewModel.currentFileIndex.collectAsState(null)
    Row(
        modifier = Modifier.fillMaxSize().padding(10.dp).zIndex(99999999999f)
    ) {
        // 选择项目
        LeftPanel(appViewModel, fileList, currentFileIndex)
        Divider(modifier = Modifier.fillMaxHeight().width(1.dp), thickness = 1.dp)
        // 右侧图标预览
        RightPanel(
            modifier = Modifier.weight(1f).width(500.dp),
            appViewModel = appViewModel,
            fileList = fileList,
            currentFile = currentFile
        )
    }
}

@Composable
private fun LeftPanel(appViewModel: AppViewModel, fileList: List<File>, currentFileIndex: Int?) {
    val scope = rememberCoroutineScope()
    Box(
        Modifier.fillMaxHeight().width(300.dp).padding(end = 10.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TextButton(onClick = {
                try {
                    "https://icomoon.io/".openBrowse()
                } catch (e: Exception) {

                }
            }) {
                Text("图标网站")
            }
            Button(onClick = {
                scope.launch {
                    val result = pickFile()
                    if (result != null) {
                        appViewModel.selectFile(result)
                        appViewModel.changeCurrentFileIndex(0)
                        appViewModel.changeCurrentFile(result)
                    }
//                    println(result)
                }
            }) {
                Text("选择文件")
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (fileList.isNotEmpty()) {
                    items(fileList!!.size) {
                        LeftFileRow(
                            file = fileList[it],
                            index = it,
                            selected = currentFileIndex == it
                        ) {
                            scope.launch {
                                appViewModel.changeCurrentFileIndex(it)
                                appViewModel.changeCurrentFile(fileList[it])
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeftFileRow(
    modifier: Modifier = Modifier,
    file: File,
    index: Int = 0,
    selected: Boolean,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.padding(bottom = 10.dp),
        color = if (selected) MaterialTheme.colors.primary else Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().clickable {
                onClick()
            },
        ) {
            Text(
                modifier = Modifier.padding(start = 6.dp, top = 10.dp),
                text = "#${index}.${file.name}", style = TextStyle(
                    fontSize = 14.sp
                )
            )
            Text(
                modifier = Modifier.padding(start = 6.dp, bottom = 10.dp),
                text = file.absolutePath, style = TextStyle(
                    fontSize = 12.sp
                )
            )
        }
    }

}

@Composable
private fun RightPanel(
    modifier: Modifier,
    appViewModel: AppViewModel,
    fileList: List<File>,
    currentFile: File?
) {
    val scope = rememberCoroutineScope()
    Card(
        modifier = Modifier
            .fillMaxSize(),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            if (currentFile == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("请选择字体图标的CSS文件")
                }
            }

            if (fileList.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("请选择字体图标的CSS文件")
                }
            }

            if (currentFile != null) {
                FileDetailBox(file = currentFile)
            }
        }
    }
}

@Composable
private fun FileDetailBox(file: File) {
    val scope = rememberCoroutineScope()

    val iconPre = remember {
        mutableStateOf("icon-")
    }
    val writeIconPre = remember {
        mutableStateOf("icon_")
    }
    val fontSize = remember {
        mutableStateOf("16")
    }
    val innerFunction = remember {
        mutableStateOf("TigerIotFontIcon")
    }
    val innerFunctionIconPre = remember {
        mutableStateOf("TigerIotFontIcon")
    }
    val showDialog = remember {
        mutableStateOf(false)
    }
    val dialogString = remember {
        mutableStateOf("")
    }
    val dialogFilePathString = remember {
        mutableStateOf("")
    }

    ShowDialog(showDialog, "提示", "${dialogString.value}", "确定复制文件路径", "取消", "确定") {
        scope.launch {
            try {
                // 复制文件绝对路径
                if (dialogFilePathString.value.isNotEmpty()) {
                    CommonUtils.copyToClipboardDesktop(dialogFilePathString.value)
                }
            } catch (e: Exception) {
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "文件名：${file.name}")
        val readableSize = humanReadableByteCount(file.length(), false)
        Text(
            text = "文件大小：$readableSize"
        )
        OutlinedTextField(modifier = Modifier.padding(top = 10.dp), label = {
            Text("读取图标前缀")
        }, value = iconPre.value, onValueChange = {
            iconPre.value = it
        })


        OutlinedTextField(modifier = Modifier.padding(top = 10.dp), label = {
            Text("写入图标前缀")
        }, value = writeIconPre.value, onValueChange = {
            writeIconPre.value = it
        })

        OutlinedTextField(modifier = Modifier.padding(top = 10.dp), label = {
            Text("写入函数名称")
        }, value = innerFunction.value, onValueChange = {
            innerFunction.value = it
        })

        OutlinedTextField(
            label = {
                Text("默认字体大小(sp)")
            },
            value = fontSize.value,
            onValueChange = {
                fontSize.value = it
            },
        )

        OutlinedTextField(modifier = Modifier.padding(top = 10.dp), label = {
            Text("生成图标函数名称")
        }, value = innerFunctionIconPre.value, onValueChange = {
            innerFunctionIconPre.value = it
        })


        Button(onClick = {
            handlerData(
                file = file,
                prefix = iconPre.value,
                writeIconPre = writeIconPre.value,
                innerFunction = innerFunction.value,
                innerFunctionIconPre = innerFunctionIconPre.value,
                fontSize = fontSize.value,
                onSuccess = { tip, filePath ->
                    dialogString.value = tip
                    dialogFilePathString.value = filePath
                    showDialog.value = true
                },
                onFail = {
                    dialogString.value = it
                    dialogFilePathString.value = ""
                    showDialog.value = true
                }
            )
        }) {
            Text("获取Android图标资源")
        }
        Text(
            "默认生成的位置在用户的下载目录(Downloads)下",
            style = TextStyle(fontSize = 10.sp, color = Color.Red)
        )
    }
}

@Composable
fun DropTargetPanel(content: @Composable () -> Unit, onFilesDropped: (List<File>) -> Unit) {
    val updatedOnFilesDropped = rememberUpdatedState(onFilesDropped)
    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        SwingPanel(
            modifier = Modifier.fillMaxSize(),
            background = Color.Transparent,
            factory = {
                JPanel().apply {
                    isOpaque = false
                    DropTarget(
                        this,
                        DnDConstants.ACTION_COPY_OR_MOVE,
                        object : DropTargetAdapter() {
                            override fun drop(evt: DropTargetDropEvent) {
                                evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE)
                                val transferable = evt.transferable
                                val dataFlavor =
                                    transferable.transferDataFlavors.firstOrNull { it.isFlavorJavaFileListType }
                                val files = transferable.getTransferData(dataFlavor) as? List<*>
                                updatedOnFilesDropped.value(files?.filterIsInstance<File>() ?: emptyList())
                                evt.dropComplete(true)
                            }
                        },
                        true
                    )
                }
            }
        )
    }
    content()
}

private fun handlerData(
    file: File,
    prefix: String = "icon-",
    writeIconPre: String = "icon_",
    innerFunction: String = "TigerIotFontIcon",
    innerFunctionIconPre: String = "TigerIotFontIcon",
    fontSize: String = "16",
    onFail: (message: String) -> Unit = {},
    onSuccess: (message: String, filePath: String) -> Unit = { _, _ -> },
) {
    /**
    生成文件
    <string name="icon_reset" translatable="false">&#xe61d;</string>
    @Composable
    fun TestIcon(
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle(),
    fontSize: TextUnit = 16.sp,
    color: Color = getCurrentColors().onBackground,
    ) {
    TigerIotFontIcon(
    R.string.icon_reset,
    textStyle = textStyle,
    modifier = modifier,
    fontSize = fontSize,
    color = color
    )
    }
     */
    try {
        if (file.length().toInt() != 0) {
            var strResourceResult = "" // string 资源文件
            var functionResult = "" // 图标函数组件

            var cssContent = ""
            file.bufferedReader().use { reader ->
                reader.forEachLine { line ->
                    cssContent += line + "\n"
                }
            }
            val pattern = """\.${prefix}(.*?):before \{\s*content: "(.*?)";\s*\}""".toRegex()
            val matches = pattern.findAll(cssContent)

            for (match in matches) {
                var iconName = match.groupValues[1].replace("-", "_")
                val iconValue = CommonUtils.unicodeEscapeToHtmlEntity(match.groupValues[2])
//                println("$iconName: $iconValue")
                // 写入android string 资源
                strResourceResult += "<string name=\"${writeIconPre}${iconName}\" translatable=\"false\">${iconValue}</string>\n"
                // 写入android icon组件函数
                functionResult += "@Composable\n" +
                        "fun ${innerFunctionIconPre}_${iconName}(\n" +
                        "    modifier: Modifier = Modifier,\n" +
                        "    textStyle: TextStyle = TextStyle(),\n" +
                        "    fontSize: TextUnit = $fontSize.sp,\n" +
                        "    color: Color = getCurrentColors().onBackground,\n" +
                        ") {\n" +
                        "    ${innerFunction}(\n" +
                        "        R.string.${writeIconPre}${iconName},\n" +
                        "        textStyle = textStyle,\n" +
                        "        modifier = modifier,\n" +
                        "        fontSize = fontSize,\n" +
                        "        color = color\n" +
                        "    )\n" +
                        "}\n"
            }

//            println(strResourceResult)
//            println(functionResult)

            // 生成新文件 并下载
            val userHome = System.getProperty("user.home")
            val downloadsDirectory = File(userHome, "Downloads")
            if (downloadsDirectory.exists()) {
                val newFile =
                    File(downloadsDirectory, "${file.name}_${Instant.now().toEpochMilli()}.txt")
                val resultFileString =
                    "$strResourceResult\n//-----------------------分割线-------------------------------\n\n$functionResult"
                newFile.writeText(resultFileString)
                onSuccess("生成成功，文件位置：${newFile.absolutePath}", newFile.absolutePath)
            } else {
                onFail("生成失败！！！！")
            }
        } else {
            onFail("生成失败！！！！")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        onFail(e.message.toString())
    }
}