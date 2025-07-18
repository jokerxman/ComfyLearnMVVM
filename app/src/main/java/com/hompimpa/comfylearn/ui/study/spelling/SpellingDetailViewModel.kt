package com.hompimpa.comfylearn.ui.study.spelling

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.edit
import androidx.core.graphics.createBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hompimpa.comfylearn.helper.AppConstants
import com.hompimpa.comfylearn.helper.GameContentProvider

class SpellingDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _displayImage = MutableLiveData<Bitmap?>()
    val displayImage: LiveData<Bitmap?> = _displayImage

    private val _displayItems = MutableLiveData<List<String>>()
    val displayItems: LiveData<List<String>> = _displayItems

    private val _isNextButtonVisible = MutableLiveData<Boolean>()
    val isNextButtonVisible: LiveData<Boolean> = _isNextButtonVisible

    private val _errorState = MutableLiveData<String?>()
    val errorState: LiveData<String?> = _errorState

    fun loadCategory(category: String, isConsonant: Boolean) {
        if (isConsonant) {
            loadConsonantCategory(category)
        } else {
            loadWordCategory(category)
        }
    }

    fun onSyllableTapped(syllable: String) {
        _displayImage.value = GameContentProvider.createSyllableBitmap(getApplication(), syllable)
    }

    private fun loadWordCategory(category: String) {
        _isNextButtonVisible.value = true
        val context = getApplication<Application>()
        val words = GameContentProvider.getWordsForCategory(context, category)

        if (words.isNotEmpty()) {
            val randomWord = words.random()
            val englishWord =
                GameContentProvider.getEnglishEquivalent(context, randomWord, category)
            if (englishWord != null) {
                val imagePath = GameContentProvider.getImagePath(category, englishWord)
                val drawable = GameContentProvider.loadSvgFromAssets(context, imagePath)
                _displayImage.value = drawableToBitmap(drawable)
                _displayItems.value = listOf(randomWord)
            } else {
                _errorState.value = "Could not find image for '$randomWord'."
            }
        } else {
            _errorState.value = "No items found for '$category'."
        }
    }

    private fun loadConsonantCategory(category: String) {
        _isNextButtonVisible.value = false
        val context = getApplication<Application>()
        val syllables = GameContentProvider.getItemsForConsonantSyllables(context, category)
        if (syllables.isNotEmpty()) {
            val imagePath = GameContentProvider.getConsonantImagePath(category)
            val drawable = GameContentProvider.loadSvgFromAssets(context, imagePath)
            _displayImage.value = drawableToBitmap(drawable)
            _displayItems.value = syllables
        } else {
            _errorState.value = "No syllables found for '$category'."
        }
    }

    fun saveSpellingProgress(category: String) {
        getApplication<Application>().getSharedPreferences(
            AppConstants.PREFS_PROGRESSION,
            Context.MODE_PRIVATE
        ).edit {
            putBoolean(AppConstants.getSpellingCategoryProgressKey(category), true)
        }
    }

    private fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null || drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) return null
        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}