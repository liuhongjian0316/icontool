package utils

object CommonUtils {
    fun humanReadableByteCount(bytes: Long, si: Boolean): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
        return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }
    fun unicodeEscapeToHtmlEntity(escape: String): String {
        if (!escape.startsWith("\\e")) {
            throw IllegalArgumentException("Invalid escape format!")
        }
        val hexValue = escape.drop(2)
        return "&#x$hexValue;"
    }
}