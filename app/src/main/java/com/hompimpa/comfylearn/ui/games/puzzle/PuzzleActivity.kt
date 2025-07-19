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

    private var currentDrag: DragState? = null

    private data class DragState(
        val view: View,
        val initialX: Float,
        val initialY: Float,
        val originalX: Float,
        val originalY: Float,
        val dX: Float,
        val dY: Float,
        var isDragging: Boolean = false
    )

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
            val tileView = LayoutInflater.from(this)
                .inflate(
                    R.layout.item_character_option,
                    binding.layoutCharacterOptions,
                    false
                ) as PuzzleTileView
            tileView.text = char.toString()
            tileView.setOnTouchListener(OptionTileTouchListener())
            binding.layoutCharacterOptions.addView(tileView)
        }
        binding.layoutCharacterOptions.post { binding.layoutCharacterOptions.rescatterChildren() }
    }

    private inner class OptionTileTouchListener : View.OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            if (view.isInvisible) return false
            return when (event.action) {
                MotionEvent.ACTION_DOWN -> handleDragStart(view, event)
                MotionEvent.ACTION_MOVE -> handleDragMove(event)
                MotionEvent.ACTION_UP -> handleDragEnd(view, event)
                else -> false
            }
        }
    }

    private fun handleDragStart(view: View, event: MotionEvent): Boolean {
        currentDrag = DragState(
            view = view,
            initialX = event.rawX,
            initialY = event.rawY,
            originalX = view.x,
            originalY = view.y,
            dX = view.x - event.rawX,
            dY = view.y - event.rawY
        )
        binding.layoutCharacterOptions.bringChildToFront(view)
        return true
    }

    private fun handleDragMove(event: MotionEvent): Boolean {
        val drag = currentDrag ?: return false
        if (!drag.isDragging && (abs(event.rawX - drag.initialX) > touchSlop || abs(event.rawY - drag.initialY) > touchSlop)) {
            drag.isDragging = true
        }
        if (drag.isDragging) {
            drag.view.x = event.rawX + drag.dX
            drag.view.y = event.rawY + drag.dY
        }
        return true
    }

    private fun handleDragEnd(view: View, event: MotionEvent): Boolean {
        val drag = currentDrag ?: return false
        if (drag.isDragging) {
            val targetSlot = targetSlots.find { isViewOver(it, event.rawX, event.rawY) }
            if (targetSlot != null) {
                placeLetterInSlot(view as TextView, targetSlot.tag as Int)
            } else if (isViewOver(binding.layoutCharacterOptions, event.rawX, event.rawY)) {
                letTileStayAtNewPosition(view)
            } else {
                returnTileToPile(view, drag.originalX, drag.originalY)
            }
        }
        view.performClick()
        currentDrag = null
        return true
    }

    private fun letTileStayAtNewPosition(tile: View) {
        binding.layoutCharacterOptions.getChildState(tile)?.let {
            it.x = tile.x
            it.y = tile.y
            it.initialized = true
        }
    }

    private fun removeLetterFromSlot(slotIndex: Int) {
        slotFilledBy.remove(slotIndex)?.let { tileInSlot ->
            tileInSlot.isVisible = true
            binding.layoutCharacterOptions.getChildState(tileInSlot)?.initialized = false
            binding.layoutCharacterOptions.requestLayout()
        }
        targetSlots[slotIndex].text = ""
    }

    private fun placeLetterInSlot(tileView: TextView, slotIndex: Int) {
        removeLetterFromSlot(slotIndex)
        targetSlots[slotIndex].text = tileView.text
        tileView.isInvisible = true
        slotFilledBy[slotIndex] = tileView

        if (slotFilledBy.size == viewModel.uiState.value?.wordToGuess?.length) {
            val formedWord = targetSlots.joinToString("") { it.text }
            viewModel.checkWord(formedWord, currentCategory, currentDifficulty)
        }
    }

    private fun returnTileToPile(tile: View, originalX: Float, originalY: Float) {
        tile.x = originalX
        tile.y = originalY
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
        if (feedback.isGameEnd || feedback.isSuccess) {
            showPersistentFeedback(feedback.message, feedback.isSuccess, feedback.isGameEnd)
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

    private fun showPersistentFeedback(message: String, isSuccess: Boolean, isGameEnd: Boolean) {
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