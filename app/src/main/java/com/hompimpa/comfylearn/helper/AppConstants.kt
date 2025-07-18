package com.hompimpa.comfylearn.helper

import java.util.Locale

object AppConstants {
    const val PREFS_PROGRESSION = "user_progression_prefs"

    private fun String.normalize() = this.replace(" ", "_").lowercase(Locale.ROOT)

    fun getSpellingCategoryProgressKey(categoryName: String): String {
        return "progress_spelling_${categoryName.normalize()}"
    }

    fun getPuzzleProgressKey(categoryName: String, difficulty: String): String {
        return "progress_puzzle_${categoryName.normalize()}_${difficulty.normalize()}"
    }

    fun getMathGameProgressKey(): String = "progress_mathgame_solved"
    fun getDrawingVisitedKey(): String = "visited_drawing"
    fun getAlphabetVisitedKey(): String = "visited_alphabet"
    fun getNumberVisitedKey(): String = "visited_number"
}
