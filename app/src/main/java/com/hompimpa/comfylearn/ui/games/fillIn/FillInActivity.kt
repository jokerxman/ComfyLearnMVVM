package com.hompimpa.comfylearn.ui.games.fillIn

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityFillInBinding
import com.hompimpa.comfylearn.helper.BaseActivity
import com.hompimpa.comfylearn.helper.GameContentProvider
import com.hompimpa.comfylearn.helper.LetterOptionsAdapter
import com.hompimpa.comfylearn.helper.setOnSoundClickListener
import kotlin.math.min

class FillInActivity : BaseActivity() {

    private lateinit var binding: ActivityFillInBinding
    private val viewModel: FillInViewModel by viewModels()

    private val letterSlots = mutableListOf<TextView>()
    private lateinit var category: String
    private lateinit var difficulty: String

    companion object {
        const val EXTRA_CATEGORY_PLAYED = "com.hompimpa.comfylearn.CATEGORY_PLAYED"
        const val EXTRA_QUESTIONS_COMPLETED = "com.hompimpa.comfylearn.QUESTIONS_COMPLETED"
    }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            setGameResultAndFinish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFillInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        category = intent.getStringExtra("CATEGORY") ?: "animal"
        difficulty = intent.getStringExtra("DIFFICULTY") ?: "MEDIUM"

        setupObservers()
        setupActionButtons()

        if (savedInstanceState == null) {
            viewModel.loadNewQuestion(category, difficulty)
        }
    }

    private fun setupObservers() {
        viewModel.uiState.observe(this) { state ->
            displayImage(state.imagePath)
            setupLetterSlots(state.word)
            setupLetterOptions(state.keyboardLetters)
        }
        viewModel.slotsState.observe(this) { slots ->
            updateSlotsUI(slots)
        }
        viewModel.hintsAvailable.observe(this) { hints ->
            updateHintButtonState(hints)
        }
        viewModel.toastEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.isGameFinished.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                setGameResultAndFinish()
            }
        }
    }

    private fun setupLetterSlots(word: String) {
        binding.letterSlots.removeAllViews()
        letterSlots.clear()

        repeat(word.length) {
            val placeholderSlot = TextView(this)
            letterSlots.add(placeholderSlot)
            binding.letterSlots.addView(placeholderSlot)
        }

        binding.letterSlots.doOnLayout { container ->
            if (container.width == 0) return@doOnLayout

            val slotMargin = resources.getDimensionPixelSize(R.dimen.slot_margin)
            val totalMargin = slotMargin * (word.length - 1).coerceAtLeast(0)
            val slotSize = (container.width - totalMargin) / word.length.coerceAtLeast(1)

            letterSlots.forEachIndexed { index, slotView ->
                slotView.apply {
                    val params = LinearLayout.LayoutParams(slotSize, slotSize)
                    if (index < letterSlots.lastIndex) {
                        params.rightMargin = slotMargin
                    }
                    layoutParams = params

                    gravity = Gravity.CENTER
                    background =
                        ContextCompat.getDrawable(this@FillInActivity, R.drawable.letter_slot_bg)
                    TextViewCompat.setAutoSizeTextTypeWithDefaults(
                        this,
                        TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
                    )
                    text = if (word[index].isWhitespace()) " " else "_"
                }
            }

            viewModel.slotsState.value?.let { updateSlotsUI(it) }
        }
    }

    private fun updateSlotsUI(slots: List<Char?>) {
        slots.forEachIndexed { index, char ->
            if (index < letterSlots.size) {
                letterSlots[index].text = when {
                    char == null -> "_"
                    char.isWhitespace() -> " "
                    else -> char.uppercase()
                }
            }
        }
    }

    private fun setupLetterOptions(keyboardLetters: List<String>) {
        val adapter = LetterOptionsAdapter(keyboardLetters) { letter ->
            viewModel.onLetterTyped(letter.first(), category, difficulty)
        }
        binding.letterOptions.adapter = adapter
        val columns = if (adapter.itemCount <= 5) adapter.itemCount.coerceAtLeast(1) else 5
        binding.letterOptions.layoutManager = GridLayoutManager(this, columns)

        binding.letterOptions.doOnLayout { view ->
            val rv = view as RecyclerView
            if (rv.width == 0 || rv.height == 0 || adapter.itemCount == 0) return@doOnLayout
            val rows = (adapter.itemCount + columns - 1) / columns
            val itemSize = min(rv.width / columns, rv.height / rows)
            (rv.adapter as? LetterOptionsAdapter)?.updateItemSize(itemSize)
        }
    }

    private fun setupActionButtons() {
        binding.customBackButton.setOnSoundClickListener { setGameResultAndFinish() }
        binding.hintButton.setOnSoundClickListener { viewModel.onHint(category, difficulty) }
        binding.deleteButton.setOnSoundClickListener { viewModel.onUndo() }
    }

    private fun updateHintButtonState(hintsAvailable: Int) {
        val hasEmptySlots = letterSlots.any { it.text == "_" }
        binding.hintButton.isEnabled = difficulty == "EASY" && hintsAvailable > 0 && hasEmptySlots
        binding.hintButton.alpha = if (binding.hintButton.isEnabled) 1.0f else 0.5f
        binding.hintButton.visibility = if (difficulty == "EASY") View.VISIBLE else View.GONE
    }

    private fun setGameResultAndFinish() {
        val resultIntent = Intent().apply {
            putExtra(EXTRA_CATEGORY_PLAYED, category)
            putExtra(EXTRA_QUESTIONS_COMPLETED, viewModel.getQuestionsAnsweredCount())
            putExtra("DIFFICULTY_PLAYED_BACK", difficulty)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun displayImage(path: String?) {
        val drawable = GameContentProvider.loadSvgFromAssets(this, path ?: "")
        binding.imagePrompt.setImageDrawable(
            drawable ?: ContextCompat.getDrawable(
                this,
                R.drawable.ic_placeholder_image
            )
        )
    }
}