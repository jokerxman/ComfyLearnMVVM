package com.hompimpa.comfylearn.ui.games.drawing

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.helper.Event
import com.hompimpa.comfylearn.views.Board
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class DrawingUiState(
    val penColor: Int = Color.BLACK,
    val brushSize: Float = 20f,
    val drawingMode: Board.Mode = Board.Mode.DRAW,
    val isEraser: Boolean = false
)

class DrawingViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableLiveData(DrawingUiState())
    val uiState: LiveData<DrawingUiState> = _uiState

    private val _showColoringPages = MutableLiveData<Event<Map<String, Int>>>()
    val showColoringPages: LiveData<Event<Map<String, Int>>> = _showColoringPages

    private val _showColorPicker = MutableLiveData<Event<Int>>()
    val showColorPicker: LiveData<Event<Int>> = _showColorPicker

    private val _saveResultToast = MutableLiveData<Event<String>>()
    val saveResultToast: LiveData<Event<String>> = _saveResultToast

    private val _newBackgroundImage = MutableLiveData<Bitmap>()
    val newBackgroundImage: LiveData<Bitmap> = _newBackgroundImage

    private val coloringPages = mapOf(
        "Animal" to R.drawable.animal_outline,
        "Car" to R.drawable.car_outline,
        "House" to R.drawable.house_outline
    )

    fun onSetPenColor(color: Int) {
        _uiState.value =
            _uiState.value?.copy(penColor = color, isEraser = false, drawingMode = Board.Mode.DRAW)
    }

    fun onSetBrushSize(size: Float) {
        _uiState.value =
            _uiState.value?.copy(brushSize = size, isEraser = false, drawingMode = Board.Mode.DRAW)
    }

    fun onSetEraserMode() {
        _uiState.value = _uiState.value?.copy(isEraser = true, drawingMode = Board.Mode.DRAW)
    }

    fun onSetShapeMode() {
        _uiState.value = _uiState.value?.copy(drawingMode = Board.Mode.SHAPE_RECTANGLE)
    }

    fun onPickColorClicked() {
        _showColorPicker.value = Event(_uiState.value?.penColor ?: Color.BLACK)
    }

    fun onColoringPageClicked() {
        _showColoringPages.value = Event(coloringPages)
    }

    fun onColoringPageSelected(resId: Int) {
        try {
            val bitmap =
                BitmapFactory.decodeResource(getApplication<Application>().resources, resId)
            _newBackgroundImage.value = bitmap
        } catch (_: Exception) {
            _saveResultToast.value = Event("Coloring page image not found.")
        }
    }

    @SuppressLint("HardcodedDispatcher")
    fun saveDrawing(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    "drawing_${System.currentTimeMillis()}.jpg"
                )
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/ComfyLearn"
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                try {
                    resolver.openOutputStream(it)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val updateValues = ContentValues().apply {
                            put(MediaStore.Images.Media.IS_PENDING, 0)
                        }
                        resolver.update(it, updateValues, null, null)
                    }
                    _saveResultToast.postValue(Event("Drawing saved to Gallery!"))
                } catch (_: Exception) {
                    _saveResultToast.postValue(Event("Failed to save drawing."))
                }
            } ?: _saveResultToast.postValue(Event("Failed to create image file."))
        }
    }
}