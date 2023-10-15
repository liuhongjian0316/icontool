import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import utils.getDataStore
import java.io.File


class AppViewModel {


    private val dataStore: DataStore<Preferences> = getDataStore()

    // 选择的文件
    private val _fileList = MutableStateFlow(mutableStateListOf<File>(File("asd"), File("asd2")))
    val fileList: Flow<List<File>>
        get() = _fileList

    val _currentFile = MutableStateFlow<File?>(null)
    val currentFile: Flow<File?>
        get() = _currentFile

    val _currentFileIndex = MutableStateFlow<Int?>(null)
    val currentFileIndex: Flow<Int?>
        get() = _currentFileIndex


    suspend fun changeCurrentFile(file: File) {
        try {
            _currentFile.value = file
        } catch (e: Exception) {
        }
    }

    suspend fun changeCurrentFileIndex(index: Int?) {
        try {
            _currentFileIndex.value = index
        } catch (e: Exception) {
        }
    }

    suspend fun selectFile(file: File) {
        try {
            val updatedList = mutableListOf(file).also {
                it.addAll(_fileList.value)
            }
            _fileList.value = updatedList.toMutableStateList()
        } catch (e: Exception) {
            println("Error adding file: ${e.message}")
        }
    }
}