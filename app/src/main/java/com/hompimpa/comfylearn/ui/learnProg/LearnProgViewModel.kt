package com.hompimpa.comfylearn.ui.learnProg

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.helper.AppConstants
import com.hompimpa.comfylearn.helper.GameContentProvider
import java.util.Locale

class LearnProgViewModel(application: Application) : AndroidViewModel(application) {

    private val _progressionItems = MutableLiveData<List<ProgressionItem>>()
    val progressionItems: LiveData<List<ProgressionItem>> = _progressionItems

    fun loadProgression() {
        val context = getApplication<Application>()
        val prefs =
            context.getSharedPreferences(AppConstants.PREFS_PROGRESSION, Context.MODE_PRIVATE)
        val gameCategories = GameContentProvider.getGameCategories(context)
        val puzzleDifficulties = GameContentProvider.getPuzzleDifficulties(context)
        val items = mutableListOf<ProgressionItem>()

        val statusNotStarted = context.getString(R.string.progress_status_not_started)
        val statusVisited = context.getString(R.string.progress_status_visited)
        val statusCompleted = context.getString(R.string.progress_status_completed)

        val arithmeticPrefs =
            context.getSharedPreferences("ArithmeticProgress", Context.MODE_PRIVATE)
        val arithCurrentLevel = arithmeticPrefs.getInt("currentLevel", 1)
        val isArithVisited = arithCurrentLevel > 1
        items.add(
            createSimpleProgressItem(
                "ALPHABET",
                "Alphabet",
                prefs.getBoolean(AppConstants.getAlphabetVisitedKey(), false),
                statusVisited,
                statusNotStarted
            )
        )
        items.add(
            createSimpleProgressItem(
                "NUMBER",
                "Number",
                prefs.getBoolean(AppConstants.getNumberVisitedKey(), false),
                statusVisited,
                statusNotStarted
            )
        )
        items.add(
            createSimpleProgressItem(
                "ARITHMETIC",
                "Arithmetic",
                isArithVisited,
                statusVisited,
                statusNotStarted
            )
        )
        items.add(
            createSimpleProgressItem(
                "DRAWING",
                "Drawing",
                prefs.getBoolean(AppConstants.getDrawingVisitedKey(), false),
                statusVisited,
                statusNotStarted
            )
        )

        val mathProblemsSolved = prefs.getInt(AppConstants.getMathGameProgressKey(), 0)
        val mathStatus = if (mathProblemsSolved > 0) context.getString(
            R.string.progress_status_problems_solved,
            mathProblemsSolved
        ) else statusNotStarted
        items.add(ProgressionItem("MATH", "Math Game", null, "Math Game", mathStatus))

        gameCategories.forEach { category ->
            val spellingKey = AppConstants.getSpellingCategoryProgressKey(category)
            items.add(
                ProgressionItem(
                    gameType = "SPELLING", category = category, difficulty = null,
                    activityName = "Spelling: ${category.replaceFirstChar { it.titlecase(Locale.getDefault()) }}",
                    status = if (prefs.getBoolean(
                            spellingKey,
                            false
                        )
                    ) statusVisited else statusNotStarted
                )
            )
        }

        gameCategories.forEach { category ->
            puzzleDifficulties.forEach { difficulty ->
                val progressKeyBase = AppConstants.getPuzzleProgressKey(category, difficulty)
                val isCompleted = prefs.getBoolean(progressKeyBase + "_completed", false)
                val wordsSolved = prefs.getInt(progressKeyBase + "_words_solved", 0)
                val totalWords = GameContentProvider.getTotalWordsForPuzzleCategory(
                    context,
                    category,
                    difficulty
                )
                val status = when {
                    isCompleted -> statusCompleted
                    wordsSolved > 0 -> context.getString(
                        R.string.progress_status_words_solved,
                        wordsSolved,
                        totalWords
                    )

                    else -> statusNotStarted
                }
                items.add(
                    ProgressionItem(
                        gameType = "PUZZLE", category = category, difficulty = difficulty,
                        activityName = "Puzzle: ${category.replaceFirstChar { it.titlecase(Locale.getDefault()) }} - $difficulty",
                        status = status
                    )
                )
            }
        }
        _progressionItems.value = items
    }

    private fun createSimpleProgressItem(
        gameType: String,
        name: String,
        isVisited: Boolean,
        visitedText: String,
        notStartedText: String
    ): ProgressionItem {
        return ProgressionItem(
            gameType,
            name,
            null,
            name,
            if (isVisited) visitedText else notStartedText
        )
    }
}