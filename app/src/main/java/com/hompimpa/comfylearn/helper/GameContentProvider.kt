package com.hompimpa.comfylearn.helper

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.PictureDrawable
import androidx.core.content.edit
import androidx.core.graphics.createBitmap
import com.caverock.androidsvg.SVG
import com.hompimpa.comfylearn.R
import java.io.IOException
import java.util.Locale

data class WordPair(val display: String, val base: String)

object GameContentProvider {

    private const val PREFS_USED_WORDS = "GameContentProvider_UsedWords"

    private fun getArrayResourceIdForCategory(category: String): Int {
        return when (category.lowercase(Locale.ROOT)) {
            "animal" -> R.array.animal
            "objek" -> R.array.objek
            else -> 0
        }
    }

    private fun getEnglishWordsForCategory(context: Context, category: String): List<String> {
        val resId = getArrayResourceIdForCategory(category)
        if (resId == 0) return emptyList()
        val englishConfig =
            Configuration(context.resources.configuration).apply { setLocale(Locale.ENGLISH) }
        val englishContext = context.createConfigurationContext(englishConfig)
        return englishContext.resources.getStringArray(resId).toList()
    }

    private fun getUsedWords(context: Context, category: String): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_USED_WORDS, Context.MODE_PRIVATE)
        return prefs.getStringSet(category.lowercase(Locale.ROOT), emptySet()) ?: emptySet()
    }

    fun addUsedWord(context: Context, category: String, baseWord: String) {
        val prefs = context.getSharedPreferences(PREFS_USED_WORDS, Context.MODE_PRIVATE)
        val categoryKey = category.lowercase(Locale.ROOT)
        val currentUsedWords = getUsedWords(context, category).toMutableSet()
        currentUsedWords.add(baseWord.uppercase(Locale.ROOT))
        prefs.edit { putStringSet(categoryKey, currentUsedWords) }
    }

    fun getWordsForCategory(context: Context, category: String): List<String> {
        val arrayResId = getArrayResourceIdForCategory(category)
        return if (arrayResId != 0) {
            try {
                context.resources.getStringArray(arrayResId).toList()
            } catch (_: Resources.NotFoundException) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun getEnglishEquivalent(
        context: Context,
        localizedWord: String,
        categoryName: String
    ): String? {
        val resId = getArrayResourceIdForCategory(categoryName)
        if (resId == 0) return null
        val localizedArray = context.resources.getStringArray(resId)
        val wordIndex = localizedArray.indexOfFirst { it.equals(localizedWord, ignoreCase = true) }
        if (wordIndex == -1) return null

        val englishConfig =
            Configuration(context.resources.configuration).apply { setLocale(Locale.ENGLISH) }
        val englishContext = context.createConfigurationContext(englishConfig)
        val englishArray = englishContext.resources.getStringArray(resId)
        return if (wordIndex < englishArray.size) englishArray[wordIndex] else null
    }

    fun getNextWordPair(context: Context, category: String, difficulty: String): WordPair? {
        val localizedWords = getWordsForCategory(context, category)
        val englishBaseWords = getEnglishWordsForCategory(context, category)
        if (localizedWords.isEmpty() || localizedWords.size != englishBaseWords.size) return null

        val usedWords = getUsedWords(context, category)
        val (minLength, maxLength) = getDifficultyBounds(difficulty)

        val availableIndices = englishBaseWords.indices.filter { index ->
            val baseWord = englishBaseWords[index]
            baseWord.length in minLength..maxLength && !usedWords.contains(baseWord.uppercase(Locale.ROOT))
        }
        return availableIndices.randomOrNull()
            ?.let { WordPair(display = localizedWords[it], base = englishBaseWords[it]) }
    }

    fun allWordsUsed(context: Context, category: String, difficulty: String): Boolean {
        val allBaseWords = getEnglishWordsForCategory(context, category)
        if (allBaseWords.isEmpty()) return true
        val (minLength, maxLength) = getDifficultyBounds(difficulty)
        val relevantWords = allBaseWords
            .filter { it.length in minLength..maxLength }
            .map { it.uppercase(Locale.ROOT) }
            .toSet()
        val usedWords = getUsedWords(context, category)
        return relevantWords.isNotEmpty() && usedWords.containsAll(relevantWords)
    }

    fun resetUsedWordsForCategory(context: Context, category: String) {
        val prefs = context.getSharedPreferences(PREFS_USED_WORDS, Context.MODE_PRIVATE)
        prefs.edit { remove(category.lowercase(Locale.ROOT)) }
    }

    fun getTotalWordsForPuzzleCategory(
        context: Context,
        category: String,
        difficulty: String
    ): Int {
        val allWords = getEnglishWordsForCategory(context, category)
        val (minLength, maxLength) = getDifficultyBounds(difficulty)
        return allWords.count { it.length in minLength..maxLength }
    }

    private fun getDifficultyBounds(difficulty: String): Pair<Int, Int> {
        return when (difficulty.lowercase(Locale.ROOT)) {
            "easy", "mudah" -> 1 to 4
            "medium", "sedang" -> 4 to 7
            "hard", "sulit" -> 7 to Int.MAX_VALUE
            else -> 1 to Int.MAX_VALUE
        }
    }

    fun getAlphabet(context: Context): List<Char> {
        return try {
            context.resources.getStringArray(R.array.alphabets).mapNotNull { it.firstOrNull() }
        } catch (_: Exception) {
            ('A'..'Z').toList()
        }
    }

    fun getGameCategories(context: Context): List<String> {
        return context.resources.getStringArray(R.array.game_category_keys).toList()
    }

    fun getPuzzleDifficulties(context: Context): List<String> {
        return context.resources.getStringArray(R.array.puzzle_difficulties).toList()
    }

    fun getItemsForConsonantSyllables(context: Context, consonant: String): List<String> {
        val consonantLower = consonant.lowercase(Locale.ROOT)
        val consonantOrder =
            context.resources.getStringArray(R.array.consonants).map { it.lowercase(Locale.ROOT) }
        val index = consonantOrder.indexOf(consonantLower)
        if (index == -1) return emptyList()
        return runCatching {
            val spellArray = context.resources.getStringArray(R.array.spell)
            if (index < spellArray.size) {
                spellArray[index].split(",").map { it.trim() }.filter { it.isNotEmpty() }
            } else {
                emptyList()
            }
        }.getOrElse { emptyList() }
    }

    fun getImagePath(category: String, itemName: String): String {
        val normalizedCategory = category.lowercase(Locale.ROOT)
        val normalizedItemName = itemName.lowercase(Locale.ROOT).replace(" ", "_")
        return "images/${normalizedCategory}_${normalizedItemName}.svg"
    }

    fun getConsonantImagePath(consonant: String): String {
        val normalizedConsonant = consonant.lowercase(Locale.ROOT)
        return "${normalizedConsonant}.svg"
    }

    fun loadSvgFromAssets(context: Context, path: String): PictureDrawable? {
        return try {
            context.assets.open(path).use { stream ->
                SVG.getFromInputStream(stream)?.renderToPicture()?.let { PictureDrawable(it) }
            }
        } catch (_: IOException) {
            null
        }
    }

    fun createSyllableBitmap(context: Context, syllable: String): Bitmap? {
        if (syllable.isBlank()) return null
        val letterDrawables = syllable.mapNotNull { char ->
            val path = getAlphabetImagePath(char)
            loadSvgFromAssets(context, path)
        }
        if (letterDrawables.isEmpty()) return null
        val standardHeight = 150
        var totalWidth = 0
        val sizedBitmaps = letterDrawables.mapNotNull { drawable ->
            if (drawable.intrinsicHeight <= 0) return@mapNotNull null
            val aspectRatio = drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
            val calculatedWidth = (standardHeight * aspectRatio).toInt()
            totalWidth += calculatedWidth
            val bmp = createBitmap(calculatedWidth, standardHeight)
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, calculatedWidth, standardHeight)
            drawable.draw(canvas)
            bmp
        }
        if (totalWidth == 0) return null
        val resultBitmap = createBitmap(totalWidth, standardHeight)
        val resultCanvas = Canvas(resultBitmap)
        var currentX = 0f
        sizedBitmaps.forEach { bmp ->
            resultCanvas.drawBitmap(bmp, currentX, 0f, null)
            currentX += bmp.width
            bmp.recycle()
        }
        return resultBitmap
    }

    private fun getAlphabetImagePath(character: Char): String {
        val normalizedChar = character.toString().lowercase(Locale.ROOT)
        return "${normalizedChar}.svg"
    }
}