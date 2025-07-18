package com.hompimpa.comfylearn.ui.games.puzzle

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityPuzzleBinding
import com.hompimpa.comfylearn.helper.BaseActivity
import com.hompimpa.comfylearn.helper.GameContentProvider
import com.hompimpa.comfylearn.helper.SoundManager
import com.hompimpa.comfylearn.helper.setOnSoundClickListener
import com.hompimpa.comfylearn.ui.games.DifficultySelectionActivity
import com.hompimpa.comfylearn.views.PuzzleTileView
import java.util.Locale
import kotlin.math.abs

class PuzzleActivity : BaseActivity() {

    private lateinit var binding: ActivityPuzzleBinding
    private val viewModel: PuzzleViewModel by viewModels()

    private lateinit var currentDifficulty: String
    private lateinit var currentCategory: String

    private val targetSlots = mutableListOf<TextView>()
    private val slotFilledBy = mutableMapOf<Int, View>()
    private val touchSlop by lazy { ViewConfiguration.get(this).scaledTouchSlop }

    private var draggedView: View? = null
    private var dXTouch = 0f
    private var dYTouch = 0f
    private var isDragging = false
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var originalTileX = 0f
    private var originalTileY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPuzzleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentDifficulty =
            intent.getStringExtra("DIFFICULTY") ?: DifficultySelectionActivity.DIFFICULTY_MEDIUM
        currentCategory = intent.getStringExtra("CATEGORY") ?: "animal"
        title =
            "Puzzle: ${currentCategory.replaceFirstChar { it.titlecase(Locale.getDefault()) }} ($currentDifficulty)"

        setupObservers()
        setupClickListeners()
        updateInstructions()

        viewModel.loadNextWord(currentCategory, currentDifficulty)
    }

    private fun setupObservers() {
        viewModel.uiState.observe(this) { state ->
            displayImage(state.imagePath)
            slotFilledBy.clear()
            setupTargetSlots(state.wordToGuess)
            setupCharacterOptions(state.optionLetters)
        }
        viewModel.feedback.observe(this) { feedback ->
            handleFeedback(feedback)
        }
    }

    private fun setupClickListeners() {
        binding.buttonCheckWord.setOnSoundClickListener {
            val formedWord = targetSlots.joinToString("") { it.text }
            viewModel.checkWord(formedWord, currentCategory, currentDifficulty)
        }
        binding.buttonPlayAgain.setOnSoundClickListener {
            binding.layoutFeedback.isVisible = false
            GameContentProvider.resetUsedWordsForCategory(this, currentCategory)
            viewModel.loadNextWord(currentCategory, currentDifficulty)
        }
        binding.buttonNextWord.setOnSoundClickListener {
            binding.layoutFeedback.isVisible = false
            viewModel.loadNextWord(currentCategory, currentDifficulty)
        }
    }

    private fun updateInstructions() {
        binding.textViewPuzzleInstructions.text = when (currentDifficulty) {
            DifficultySelectionActivity.DIFFICULTY_HARD -> getString(R.string.puzzle_instructions_hard)
            else -> getString(R.string.puzzle_instructions)
        }
    }

    private fun setupTargetSlots(word: String) {
        binding.layoutTargetSlots.removeAllViews()
        targetSlots.clear()
        word.indices.forEach { index ->
            val slotView = LayoutInflater.from(this)
                .inflate(R.layout.item_target_slot, binding.layoutTargetSlots, false) as TextView
            slotView.tag = index
            slotView.setOnSoundClickListener { removeLetterFromSlot(index) }
            binding.layoutTargetSlots.addView(slotView)
            targetSlots.add(slotView)
        }
    }

    private fun setupCharacterOptions(letters: List<Char>) {
        binding.layoutCharacterOptions.removeAllViews()
        letters.forEach { char ->
            val tileView = LayoutInflater.from(this).inflate(
                R.layout.item_character_option,
                binding.layoutCharacterOptions,
                false
            ) as PuzzleTileView
            tileView.text = char.toString()
            tileView.setOnTouchListener(OptionTileTouchListener(tileView))
            binding.layoutCharacterOptions.addView(tileView)
        }
        binding.layoutCharacterOptions.post { binding.layoutCharacterOptions.rescatterChildren() }
    }

    private inner class OptionTileTouchListener(private val tileView: View) : View.OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            if (view.isInvisible) return false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDragging = false
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    originalTileX = tileView.x
                    originalTileY = tileView.y
                    draggedView = tileView
                    binding.layoutCharacterOptions.bringChildToFront(tileView)
                    dXTouch = tileView.x - event.rawX
                    dYTouch = tileView.y - event.rawY
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (draggedView != tileView) return false
                    if (!isDragging && (abs(event.rawX - initialTouchX) > touchSlop || abs(event.rawY - initialTouchY) > touchSlop)) {
                        isDragging = true
                    }
                    if (isDragging) {
                        tileView.x = event.rawX + dXTouch
                        tileView.y = event.rawY + dYTouch
                    }
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (draggedView != tileView) return false
                    if (isDragging) {
                        val droppedOnSlot =
                            targetSlots.find { isViewOver(it, event.rawX, event.rawY) }
                        droppedOnSlot?.let {
                            placeLetterInSlot(
                                tileView as TextView,
                                it.tag as Int
                            )
                        } ?: returnTileToPile(tileView)
                    }
                    view.performClick()
                    isDragging = false
                    draggedView = null
                    return true
                }
            }
            return false
        }
    }

    private fun removeLetterFromSlot(slotIndex: Int) {
        slotFilledBy.remove(slotIndex)?.let { tileInSlot ->
            tileInSlot.isVisible = true
            returnTileToPile(tileInSlot)
        }
        targetSlots[slotIndex].text = ""
    }

    private fun placeLetterInSlot(tileView: TextView, slotIndex: Int) {
        removeLetterFromSlot(slotIndex)
        targetSlots[slotIndex].text = tileView.text
        tileView.isInvisible = true
        slotFilledBy[slotIndex] = tileView

        val formedWord = targetSlots.joinToString("") { it.text }
        if (formedWord.length == viewModel.uiState.value?.wordToGuess?.length) {
            viewModel.checkWord(formedWord, currentCategory, currentDifficulty)
        }
    }

    private fun returnTileToPile(tile: View) {
        tile.x = originalTileX
        tile.y = originalTileY
        binding.layoutCharacterOptions.getChildState(tile)?.let {
            it.x = tile.x
            it.y = tile.y
            it.initialized = true
        }
    }

    private fun isViewOver(view: View, x: Float, y: Float): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return Rect(location[0], location[1], location[0] + view.width, location[1] + view.height)
            .contains(x.toInt(), y.toInt())
    }

    private fun handleFeedback(feedback: Feedback) {
        val sound =
            if (feedback.isSuccess) SoundManager.Sound.CORRECT_ANSWER else SoundManager.Sound.INCORRECT_ANSWER
        SoundManager.playSound(sound)

        if (feedback.isGameEnd || feedback.isSuccess) {
            showEndGameFeedback(feedback.message, feedback.isSuccess, feedback.isGameEnd)
        } else {
            showTemporaryFeedback(feedback.message)
        }
    }

    private fun showTemporaryFeedback(message: String) {
        binding.textViewFeedback.text = message
        binding.textViewFeedback.setBackgroundColor(
            ContextCompat.getColor(
                this,
                R.color.feedback_negative_bg
            )
        )
        binding.textViewFeedback.isVisible = true
        Handler(Looper.getMainLooper()).postDelayed(
            { binding.textViewFeedback.isInvisible = true },
            2000
        )
    }

    private fun showEndGameFeedback(message: String, isSuccess: Boolean, isGameEnd: Boolean) {
        binding.textViewFeedbackPopup.text = message
        val colorRes = when {
            isGameEnd -> R.color.feedback_neutral_bg
            isSuccess -> R.color.feedback_positive_bg
            else -> R.color.feedback_negative_bg
        }
        binding.textViewFeedbackPopup.setBackgroundColor(ContextCompat.getColor(this, colorRes))
        binding.layoutFeedback.isVisible = true
        binding.buttonNextWord.isVisible = isSuccess && !isGameEnd
        binding.buttonPlayAgain.isVisible = isGameEnd
    }

    private fun displayImage(imagePath: String) {
        val drawable = GameContentProvider.loadSvgFromAssets(this, imagePath)
        binding.itemImageView.setImageDrawable(drawable)
    }
}