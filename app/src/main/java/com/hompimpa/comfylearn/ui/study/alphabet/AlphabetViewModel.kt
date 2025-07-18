package com.hompimpa.comfylearn.ui.study.alphabet

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hompimpa.comfylearn.helper.AppConstants

class AlphabetViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentLetter = MutableLiveData<Char>()
    val currentLetter: LiveData<Char> = _currentLetter

    fun setInitialLetter(letter: Char) {
        if (_currentLetter.value == null) {
            _currentLetter.value = letter
        }
    }

    fun nextLetter() {
        val next = (_currentLetter.value ?: 'A') + 1
        if (next <= 'Z') {
            _currentLetter.value = next
        }
    }

    fun previousLetter() {
        val prev = (_currentLetter.value ?: 'A') - 1
        if (prev >= 'A') {
            _currentLetter.value = prev
        }
    }

    fun markAsVisited() {
        getApplication<Application>().getSharedPreferences(
            AppConstants.PREFS_PROGRESSION, Context.MODE_PRIVATE
        ).edit {
            putBoolean(AppConstants.getAlphabetVisitedKey(), true)
        }
    }
}