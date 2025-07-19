package com.hompimpa.comfylearn.ui.games.puzzle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.helper.GameContentProvider
import com.hompimpa.comfylearn.helper.SoundManager
import com.hompimpa.comfylearn.helper.WordPair
import com.hompimpa.comfylearn.ui.games.DifficultySelectionActivity

data class PuzzleUiState(
    val imagePath: String,
    val wordToGuess: String,
    val optionLetters: List<Char>
)

data class Feedback(val message: String, val isSuccess: Boolean, val isGameEnd: Boolean = false)

class PuzzleViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableLiveData<PuzzleUiState>()
    val uiState: LiveData<PuzzleUiState> = _uiState

    private val _feedback = MutableLiveData<Feedback>()
    val feedback: LiveData<Feedback> = _feedback

    private lateinit var currentWordPair: WordPair

    fun loadNextWord(category: String, difficulty: String) {
        val wordPair = GameContentProvider.getNextWordPair(getApplication(), category, difficulty)
        if (wordPair == null) {
            handleNoMoreWords(category, difficulty)
            return
        }
        currentWordPair = wordPair

        val imagePath = GameContentProvider.getImagePath(category, wordPair.base)
        val optionLetters = generateOptionPool(wordPair.display, difficulty)

        _uiState.value = PuzzleUiState(imagePath, wordPair.display.uppercase(), optionLetters)
    }

    fun checkWord(formedWord: String, category: String, difficulty: String) {
        val context = getApplication<Application>()
        if (formedWord.length < currentWordPair.display.length) {
            _feedback.value =
                Feedback(context.getString(R.string.feedback_incomplete), isSuccess = false)
            return
        }

        if (formedWord.lowercase() == currentWordPair.display.lowercase()) {
            SoundManager.playSound(SoundManager.Sound.CORRECT_ANSWER)

            GameContentProvider.addUsedWord(context, category, currentWordPair.base)
            val allUsed = GameContentProvider.allWordsUsed(context, category, difficulty)
            _feedback.value = Feedback(
                context.getString(R.string.feedback_correct),
                isSuccess = true,
                isGameEnd = allUsed
            )
        } else {
            SoundManager.playSound(SoundManager.Sound.INCORRECT_ANSWER)
            _feedback.value = Feedback(
                context.getString(R.string.feedback_incorrect),
                isSuccess = false
            )
        }
    }

    private fun handleNoMoreWords(category: String, difficulty: String) {
        SoundManager.playSound(SoundManager.Sound.CORRECT_ANSWER)
        val context = getApplication<Application>()
        val allUsed = GameContentProvider.allWordsUsed(context, category, difficulty)
        val message = if (allUsed) context.getString(R.string.congratulations_all_words, category)
        else context.getString(R.string.no_more_words)
        _feedback.value = Feedback(message, isSuccess = allUsed, isGameEnd = true)
    }

    private fun generateOptionPool(word: String, difficulty: String): List<Char> {
        val distractors = when (difficulty) {
            DifficultySelectionActivity.DIFFICULTY_EASY -> 1
            DifficultySelectionActivity.DIFFICULTY_MEDIUM -> 2
            else -> 3
        }
        val alphabet = GameContentProvider.getAlphabet(getApplication())
        val pool = word.uppercase().toMutableList()
        val potentialDistractors = alphabet.filterNot { word.uppercase().contains(it) }.shuffled()
        pool.addAll(potentialDistractors.take(distractors))
        return pool.shuffled()
    }
}