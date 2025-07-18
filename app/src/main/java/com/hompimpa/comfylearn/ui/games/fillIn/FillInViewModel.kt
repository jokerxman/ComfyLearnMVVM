package com.hompimpa.comfylearn.ui.games.fillIn

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hompimpa.comfylearn.helper.Event
import com.hompimpa.comfylearn.helper.GameContentProvider
import com.hompimpa.comfylearn.helper.SoundManager
import com.hompimpa.comfylearn.helper.WordPair

data class FillInUiState(
    val word: String,
    val imagePath: String,
    val keyboardLetters: List<String>
)

class FillInViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableLiveData<FillInUiState>()
    val uiState: LiveData<FillInUiState> = _uiState

    private val _slotsState = MutableLiveData<List<Char?>>()
    val slotsState: LiveData<List<Char?>> = _slotsState

    private val _hintsAvailable = MutableLiveData<Int>()
    val hintsAvailable: LiveData<Int> = _hintsAvailable

    private val _toastEvent = MutableLiveData<Event<String>>()
    val toastEvent: LiveData<Event<String>> = _toastEvent

    private val _isGameFinished = MutableLiveData<Event<Unit>>()
    val isGameFinished: LiveData<Event<Unit>> = _isGameFinished

    private lateinit var currentWordPair: WordPair
    private var questionsAnsweredThisSession = 0

    fun loadNewQuestion(category: String, difficulty: String) {
        val wordPair = GameContentProvider.getNextWordPair(getApplication(), category, difficulty)
        if (wordPair == null) {
            _toastEvent.value = Event("Congratulations! You've completed all questions!")
            _isGameFinished.value = Event(Unit)
            return
        }

        currentWordPair = wordPair

        val imagePath = GameContentProvider.getImagePath(category, wordPair.base)
        _hintsAvailable.value = if (difficulty == "EASY") 1 else 0

        val revealedIndices = determineRevealedIndices(difficulty, wordPair.display)
        val initialSlots = wordPair.display.mapIndexed { index, char ->
            when {
                char.isWhitespace() -> ' '
                revealedIndices.contains(index) -> char.uppercaseChar()
                else -> null
            }
        }
        _slotsState.value = initialSlots

        val keyboardLetters = generateKeyboardLetters(wordPair.display)
        _uiState.value = FillInUiState(wordPair.display, imagePath, keyboardLetters)
    }

    fun onLetterTyped(letter: Char, category: String, difficulty: String) {
        val currentSlots = _slotsState.value?.toMutableList() ?: return
        val emptyIndex = currentSlots.indexOfFirst { it == null }
        if (emptyIndex != -1) {
            currentSlots[emptyIndex] = letter.lowercaseChar()
            _slotsState.value = currentSlots
            checkAnswer(currentSlots, category, difficulty)
        }
    }

    fun onUndo() {
        val currentSlots = _slotsState.value?.toMutableList() ?: return
        val lastFilledIndex =
            currentSlots.indexOfLast { it != null && it != ' ' && it.isLowerCase() }
        if (lastFilledIndex != -1) {
            currentSlots[lastFilledIndex] = null
            _slotsState.value = currentSlots
        }
    }

    fun onHint(category: String, difficulty: String) {
        if ((_hintsAvailable.value ?: 0) <= 0) return
        val currentSlots = _slotsState.value?.toMutableList() ?: return
        val emptyIndex = currentSlots.indexOfFirst { it == null }
        if (emptyIndex != -1) {
            currentSlots[emptyIndex] = currentWordPair.display[emptyIndex].uppercaseChar()
            _slotsState.value = currentSlots
            _hintsAvailable.value = (_hintsAvailable.value ?: 1) - 1
            checkAnswer(currentSlots, category, difficulty)
        }
    }

    fun getQuestionsAnsweredCount(): Int = questionsAnsweredThisSession

    private fun checkAnswer(slots: List<Char?>, category: String, difficulty: String) {
        if (slots.contains(null)) return

        val userAnswer = slots.joinToString("")
        val correctAnswer = currentWordPair.display

        if (userAnswer.equals(correctAnswer, ignoreCase = true)) {
            SoundManager.playSound(SoundManager.Sound.CORRECT_ANSWER)
            GameContentProvider.addUsedWord(getApplication(), category, currentWordPair.base)
            _toastEvent.value = Event("Correct!")
            questionsAnsweredThisSession++
            loadNewQuestion(category, difficulty)
        } else {
            SoundManager.playSound(SoundManager.Sound.INCORRECT_ANSWER)
            _toastEvent.value = Event("Not quite, try again!")
        }
    }

    private fun generateKeyboardLetters(word: String): List<String> {
        val wordLetters = word.filter { it.isLetter() }.map { it.uppercase() }.distinct()
        val pool = wordLetters.toMutableList()
        val alphabet = ('A'..'Z').map { it.toString() }
        val distractors = alphabet.filterNot { wordLetters.contains(it) }.shuffled()
        val needed = (15 - pool.size).coerceAtLeast(5)
        pool.addAll(distractors.take(needed))
        return pool.shuffled()
    }

    private fun determineRevealedIndices(difficulty: String, word: String): Set<Int> {
        if (difficulty == "HARD") return emptySet()
        val wordLength = word.count { it.isLetter() }
        val percentage = if (difficulty == "EASY") 0.5 else 0.25
        var toReveal = (wordLength * percentage).toInt()
        if (wordLength > 1 && toReveal >= wordLength) toReveal = wordLength - 1
        if (wordLength == 1) toReveal = 0
        return word.indices.filter { word[it].isLetter() }.shuffled().take(toReveal).toSet()
    }
}