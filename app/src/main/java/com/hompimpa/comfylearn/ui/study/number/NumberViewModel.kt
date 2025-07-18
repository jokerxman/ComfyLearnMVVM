package com.hompimpa.comfylearn.ui.study.number

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hompimpa.comfylearn.helper.AppConstants

class NumberViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentNumber = MutableLiveData<Int>()
    val currentNumber: LiveData<Int> = _currentNumber

    fun setInitialNumber(number: Int) {
        if (_currentNumber.value == null) {
            _currentNumber.value = number
        }
    }

    fun nextNumber() {
        val next = (_currentNumber.value ?: 0) + 1
        if (next <= 9) {
            _currentNumber.value = next
        }
    }

    fun previousNumber() {
        val prev = (_currentNumber.value ?: 0) - 1
        if (prev >= 0) {
            _currentNumber.value = prev
        }
    }

    fun markAsVisited() {
        getApplication<Application>().getSharedPreferences(
            AppConstants.PREFS_PROGRESSION, Context.MODE_PRIVATE
        ).edit {
            putBoolean(AppConstants.getNumberVisitedKey(), true)
        }
    }
}