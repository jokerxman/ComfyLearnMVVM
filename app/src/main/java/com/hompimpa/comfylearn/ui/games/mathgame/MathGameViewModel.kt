package com.hompimpa.comfylearn.ui.games.mathgame

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hompimpa.comfylearn.helper.AppConstants
import com.hompimpa.comfylearn.helper.GameContentProvider
import com.hompimpa.comfylearn.helper.GameManager
import com.hompimpa.comfylearn.helper.SoundManager
import kotlinx.coroutines.launch
import kotlin.random.Random

data class MathProblem(
    val questionText: String,
    val operator: String,
    val imagePath: String,
    val op1Count: Int,
    val op2Count: Int
)

data class AnswerOptions(val choices: List<Int>)
data class AnswerFeedback(val wasCorrect: Boolean, val correctAnswer: Int)

class MathGameViewModel(application: Application) : AndroidViewModel(application) {

    private val gameManager = GameManager(application)

    private val _problem = MutableLiveData<MathProblem>()
    val problem: LiveData<MathProblem> = _problem

    private val _answerOptions = MutableLiveData<AnswerOptions>()
    val answerOptions: LiveData<AnswerOptions> = _answerOptions

    private val _answerFeedback = MutableLiveData<AnswerFeedback>()
    val answerFeedback: LiveData<AnswerFeedback> = _answerFeedback

    val problemsSolvedFlow = gameManager.problemsSolvedFlow

    private var expectedAnswer: Int = 0

    fun generateNewProblem(difficulty: String) {
        val context = getApplication<Application>()

        val visualCategory = GameContentProvider.getGameCategories(context).random()
        val wordPair =
            GameContentProvider.getNextWordPair(context, visualCategory, difficulty) ?: return
        val imagePath = GameContentProvider.getImagePath(visualCategory, wordPair.base)

        val isAddition = Random.nextBoolean()
        val maxOperand = when (difficulty) {
            "EASY" -> 5
            "MEDIUM" -> 7
            else -> 9
        }

        val op1: Int
        val op2: Int
        if (isAddition) {
            op1 = Random.nextInt(1, maxOperand + 1)
            op2 = Random.nextInt(1, maxOperand + 1)
        } else {
            op1 = Random.nextInt(2, maxOperand + 2)
            op2 = Random.nextInt(1, op1 + 1)
        }
        expectedAnswer = if (isAddition) op1 + op2 else op1 - op2

        val operator = if (isAddition) "+" else "-"
        val questionText = "$op1 $operator $op2 = ?"
        _problem.value = MathProblem(questionText, operator, imagePath, op1, op2)

        generateAnswerOptions(difficulty)
    }

    fun onAnswerSelected(selectedAnswer: Int) {
        val wasCorrect = selectedAnswer == expectedAnswer
        if (wasCorrect) {
            SoundManager.playSound(SoundManager.Sound.CORRECT_ANSWER)
            viewModelScope.launch {
                gameManager.incrementProblemsSolved()
                saveProgressToSharedPrefs()
            }
        } else {
            SoundManager.playSound(SoundManager.Sound.INCORRECT_ANSWER)
        }
        _answerFeedback.value = AnswerFeedback(wasCorrect, expectedAnswer)
    }

    private fun generateAnswerOptions(difficulty: String) {
        val numChoices = when (difficulty) {
            "EASY" -> 2
            "MEDIUM" -> 3
            else -> 4
        }
        val choices = mutableSetOf(expectedAnswer)
        val maxOffset = if (difficulty == "EASY") 2 else 4
        val offsets = ((-maxOffset..-1) + (1..maxOffset)).shuffled()

        for (offset in offsets) {
            if (choices.size >= numChoices) break
            choices.add((expectedAnswer + offset).coerceAtLeast(0))
        }
        while (choices.size < numChoices) {
            choices.add(Random.nextInt(0, expectedAnswer + maxOffset * 2))
        }
        _answerOptions.value = AnswerOptions(choices.toList().shuffled())
    }

    private fun saveProgressToSharedPrefs() {
        val context = getApplication<Application>()
        val prefs =
            context.getSharedPreferences(AppConstants.PREFS_PROGRESSION, Context.MODE_PRIVATE)
        val currentSolved = prefs.getInt(AppConstants.getMathGameProgressKey(), 0)
        prefs.edit {
            putInt(AppConstants.getMathGameProgressKey(), currentSolved + 1)
        }
    }
}