import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import utils.CommonUtils
import utils.CommonUtils.humanReadableByteCount
import utils.ShowDialog
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
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
            val result = fileChooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.selectedFile
            }
        }
        selectedFile
    }
}


@Composable
fun MainPage(appViewModel: AppViewModel) {
    val fileList by appViewModel.fileList.collectAsState(mutableListOf())
    val currentFile by appViewModel.currentFile.collectAsState(null)
    val currentFileIndex by appViewModel.currentFileIndex.collectAsState(null)


//    LaunchedEffect(currentCityId) {
//        appViewModel.getWeather(currentCityId)
//    }

    Row(
        modifier = Modifier.fillMaxSize().padding(10.dp),
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


fun extractContentValues(input: String): List<String> {
    val regex = """\.icon-.*:before \{ content: "(.*?)" \}""".toRegex()
    return regex.findAll(input)
        .map { it.groupValues[1] }
        .toList()
}

@Composable
fun LeftPanel(appViewModel: AppViewModel, fileList: List<File>, currentFileIndex: Int?) {
    val scope = rememberCoroutineScope()
    Box(
        Modifier.fillMaxHeight().width(300.dp).padding(end = 10.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Button(onClick = {
                scope.launch {
                    val result = pickFile()
                    if (result != null) {
                        appViewModel.selectFile(result)
                        appViewModel.changeCurrentFileIndex(0)
                        appViewModel.changeCurrentFile(result)
                    }
                    println(result)
                }
            }) {
                Text("选择文件")
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (fileList.isNotEmpty()) {
                    items(fileList!!.size) {
                        LeftFileRow(file = fileList[it], index = it, selected = currentFileIndex == it) {
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
fun RightPanel(modifier: Modifier, appViewModel: AppViewModel, fileList: List<File>, currentFile: File?) {
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
                    Text("请选择CSS文件")
                }
            }

            if (fileList.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("请选择CSS文件")
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
    val iconPre = remember {
        mutableStateOf("icon-")
    }

    val writeIconPre = remember {
        mutableStateOf("icon-")
    }
    val fontSize = remember {
        mutableStateOf("16")
    }
    val innerFunction = remember {
        mutableStateOf("TigerIotFontIcon")
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

//        ShowDialog(showDialog, "建议", "多学一个知识点，就少说一句求人的话！", "努力", "共勉") {}

        Button(onClick = {
            handlerData(file = file, prefix = iconPre.value, writeIconPre = writeIconPre.value)
        }) {
            Text("获取Android图标资源")
        }
    }
}


private fun handlerData(
    file: File,
    prefix: String = "icon-",
    writeIconPre: String = "icon-",
    onFail: (message: String) -> Unit = {}
) {
    try {
        if (file.length().toInt() != 0) {
            var result = ""
            var cssContent = ""
            file.bufferedReader().use { reader ->
                reader.forEachLine { line ->
                    cssContent += line + "\n"
                }
            }
            val pattern = """\.${prefix}(.*?):before \{\s*content: "(.*?)";\s*\}""".toRegex()
            val matches = pattern.findAll(cssContent)


            for (match in matches) {
                val iconName = match.groupValues[1].replace("-", "_")
                val iconValue = CommonUtils.unicodeEscapeToHtmlEntity(match.groupValues[2])
                println("$iconName: $iconValue")

            }



            /**
            生产文件


            <string name="icon_reset" translatable="false">&#xe61d;</string>


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
        }
    } catch (e: Exception) {
        onFail(e.message.toString())
    }
}