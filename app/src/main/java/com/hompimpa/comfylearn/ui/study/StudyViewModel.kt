package com.hompimpa.comfylearn.ui.study

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hompimpa.comfylearn.helper.Event
import com.hompimpa.comfylearn.ui.study.alphabet.AlphabetActivity
import com.hompimpa.comfylearn.ui.study.arithmetic.ArithmeticActivity
import com.hompimpa.comfylearn.ui.study.spelling.SpellingDetailActivity

class StudyViewModel : ViewModel() {

    private val _navigationEvent = MutableLiveData<Event<Pair<Class<out Activity>, Bundle?>>>()
    val navigationEvent: LiveData<Event<Pair<Class<out Activity>, Bundle?>>> = _navigationEvent

    fun onAlphabetSelected(initialLetter: Char) {
        val bundle = Bundle().apply { putChar("letter", initialLetter) }
        _navigationEvent.value = Event(Pair(AlphabetActivity::class.java, bundle))
    }

    fun onSpellingSelected() {
        _navigationEvent.value = Event(Pair(SpellingDetailActivity::class.java, null))
    }

    fun onArithmeticSelected() {
        _navigationEvent.value = Event(Pair(ArithmeticActivity::class.java, null))
    }
}