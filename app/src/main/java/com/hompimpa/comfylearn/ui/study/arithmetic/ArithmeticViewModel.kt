package com.hompimpa.comfylearn.ui.study.arithmetic

import android.app.Application
import android.content.Context
import androidx.annotation.StringRes
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.helper.GameContentProvider
import kotlin.random.Random

data class ProblemState(
    val questionText: String,
    val operator: String,
    val op1Count: Int,
    val op2Count: Int,
    val answerCount: Int,
    val imagePath: String
)

data class ProgressState(val levelName: String, val progress: Int, val maxProgress: Int)

class ArithmeticViewModel(application: Application) : AndroidViewModel(application) {

    private val _problemState = MutableLiveData<ProblemState>()
    val problemState: LiveData<ProblemState> = _problemState

    private val _progressState = MutableLiveData<ProgressState>()
    val progressState: LiveData<ProgressState> = _progressState

    private val _isGameComplete = MutableLiveData<Boolean>()
    val isGameComplete: LiveData<Boolean> = _isGameComplete

    private val _showToast = MutableLiveData<String>()
    val showToast: LiveData<String> = _showToast

    private var currentLevel = 1
    private var problemsCompletedInLevel = 0
    private val problemsPerLevel = 10

    private val levels = listOf(
        Level(R.string.level_type_addition, isAddition = true, maxNumber = 5),
        Level(R.string.level_type_addition_hard, isAddition = true, maxNumber = 10),
        Level(R.string.level_type_subtraction, isAddition = false, maxNumber = 10),
        Level(R.string.level_type_subtraction_hard, isAddition = false, maxNumber = 20),
        Level(R.string.level_type_mixed, isAddition = null, maxNumber = 15)
    )

    init {
        loadProgress()
        setupNewQuestion()
    }

    fun onNextProblemClicked() {
        problemsCompletedInLevel++
        if (problemsCompletedInLevel >= problemsPerLevel) {
            levelUp()
        }
        setupNewQuestion()
    }

    private fun setupNewQuestion() {
        if (currentLevel > levels.size) {
            _isGameComplete.value = true
        } else {
            _isGameComplete.value = false
            updateProgressUI()
            generateQuestion()
        }
    }

    private fun generateQuestion() {
        val context = getApplication<Application>()
        val visualCategory = GameContentProvider.getGameCategories(context).random()

        val wordPair =
            GameContentProvider.getNextWordPair(context, visualCategory, "easy") ?: return
        val imagePath = GameContentProvider.getImagePath(visualCategory, wordPair.base)

        val level = levels[currentLevel - 1]
        val isAddition = level.isAddition ?: Random.nextBoolean()

        val num1: Int
        val num2: Int
        if (isAddition) {
            num1 = Random.nextInt(1, level.maxNumber + 1)
            num2 = Random.nextInt(1, level.maxNumber + 1)
        } else {
            num1 = Random.nextInt(2, level.maxNumber + 1)
            num2 = Random.nextInt(1, num1)
        }

        val answer = if (isAddition) num1 + num2 else num1 - num2
        val operator = if (isAddition) "+" else "-"
        val questionText = "$num1 $operator $num2 = $answer"

        _problemState.value = ProblemState(questionText, operator, num1, num2, answer, imagePath)
    }

    private fun levelUp() {
        if (currentLevel < levels.size) {
            currentLevel++
            problemsCompletedInLevel = 0
            _showToast.value =
                getApplication<Application>().getString(R.string.level_up_message, currentLevel)
            saveProgress()
        } else {
            _isGameComplete.value = true
        }
    }

    private fun updateProgressUI() {
        val context = getApplication<Application>()
        val level = levels[currentLevel - 1]
        val levelName = context.getString(level.nameResId)
        val progressText = context.getString(R.string.level_name_format, currentLevel, levelName)
        _progressState.value =
            ProgressState(progressText, problemsCompletedInLevel, problemsPerLevel)
    }

    private fun saveProgress() {
        val context = getApplication<Application>()
        context.getSharedPreferences("ArithmeticProgress", Context.MODE_PRIVATE).edit {
            putInt("currentLevel", currentLevel)
            putInt("problemsCompletedInLevel", problemsCompletedInLevel)
        }
    }

    private fun loadProgress() {
        val context = getApplication<Application>()
        val prefs = context.getSharedPreferences("ArithmeticProgress", Context.MODE_PRIVATE)
        currentLevel = prefs.getInt("currentLevel", 1)
        problemsCompletedInLevel = prefs.getInt("problemsCompletedInLevel", 0)
    }

    data class Level(
        @param:StringRes val nameResId: Int,
        val isAddition: Boolean?,
        val maxNumber: Int
    )
}