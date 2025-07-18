package com.hompimpa.comfylearn.ui.games

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hompimpa.comfylearn.helper.Event
import com.hompimpa.comfylearn.ui.games.drawing.DrawingActivity
import com.hompimpa.comfylearn.ui.games.fillIn.FillInActivity
import com.hompimpa.comfylearn.ui.games.mathgame.MathGameActivity
import com.hompimpa.comfylearn.ui.games.puzzle.PuzzleActivity

data class NavigationAction(
    val targetClass: Class<out Activity>,
    val extras: Bundle = Bundle(),
    val forResult: Boolean = false
)

class GamesViewModel : ViewModel() {

    private val _navigationEvent = MutableLiveData<Event<NavigationAction>>()
    val navigationEvent: LiveData<Event<NavigationAction>> = _navigationEvent

    fun onGameReady(gameType: String, category: String, difficulty: String) {
        val targetClass = when (gameType) {
            "FILL_IN" -> FillInActivity::class.java
            "PUZZLE" -> PuzzleActivity::class.java
            "MATH" -> MathGameActivity::class.java
            else -> null
        } ?: return

        val extras = Bundle().apply {
            putString("CATEGORY", category)
            putString("DIFFICULTY", difficulty)
            putString(MathGameActivity.EXTRA_SELECTED_DIFFICULTY, difficulty)
        }
        _navigationEvent.value = Event(NavigationAction(targetClass, extras))
    }

    fun onGameSelected(gameType: String) {
        if (gameType == "DRAWING") {
            _navigationEvent.value = Event(NavigationAction(DrawingActivity::class.java))
        }
    }

    fun onDifficultySelectionNeeded(category: String, gameType: String) {
        val extras = Bundle().apply {
            putString(DifficultySelectionActivity.EXTRA_GAME_CATEGORY, category)
            putString(DifficultySelectionActivity.EXTRA_GAME_TYPE, gameType)
        }
        _navigationEvent.value = Event(NavigationAction(DifficultySelectionActivity::class.java, extras, forResult = true))
    }
}