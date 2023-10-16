package utils

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

object CommonUtils {
    // 转换文件大小
    fun humanReadableByteCount(bytes: Long, si: Boolean): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
        return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }

    // 转unicode
    fun unicodeEscapeToHtmlEntity(escape: String): String {
        try {
            val hexValue = escape.drop(1)
            return "&#x$hexValue;"
        }catch (e:Exception) {
            return ""
        }
    }

    // 复制文件到剪切板
    fun copyToClipboardDesktop(data: String) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val selection = StringSelection(data)
        clipboard.setContents(selection, selection)
    }
}