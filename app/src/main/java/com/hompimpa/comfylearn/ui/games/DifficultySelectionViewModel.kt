package com.hompimpa.comfylearn.ui.games

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hompimpa.comfylearn.helper.Event

class DifficultySelectionViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences =
        application.getSharedPreferences("GameSettingsPrefs", Context.MODE_PRIVATE)

    private val _initialDifficulty = MutableLiveData<String>()
    val initialDifficulty: LiveData<String> = _initialDifficulty

    private val _finishWithResult = MutableLiveData<Event<DifficultySelectionResult>>()
    val finishWithResult: LiveData<Event<DifficultySelectionResult>> = _finishWithResult

    private var gameCategory: String? = null
    private var gameType: String? = null

    fun init(category: String?, type: String?) {
        gameCategory = category
        gameType = type

        if (gameCategory == null || gameType == null) {
            return
        }
        _initialDifficulty.value = loadLastSelectedDifficulty()
    }

    private fun loadLastSelectedDifficulty(): String {
        return sharedPreferences.getString("last_selected_universal_difficulty", "MEDIUM")
            ?: "MEDIUM"
    }

    fun onConfirmButtonClicked(selectedDifficulty: String) {
        saveSelectedDifficulty(selectedDifficulty)

        val type = gameType ?: return
        val category = gameCategory ?: return

        _finishWithResult.value = Event(
            DifficultySelectionResult(
                gameType = type,
                category = category,
                selectedDifficulty = selectedDifficulty
            )
        )
    }

    private fun saveSelectedDifficulty(difficulty: String) {
        sharedPreferences.edit {
            putString("last_selected_universal_difficulty", difficulty)
        }
    }
}