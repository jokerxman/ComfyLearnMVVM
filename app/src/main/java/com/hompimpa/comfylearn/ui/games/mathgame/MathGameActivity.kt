package com.hompimpa.comfylearn.ui.games.mathgame

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityMathGameBinding
import com.hompimpa.comfylearn.helper.BaseActivity
import com.hompimpa.comfylearn.helper.GameContentProvider
import com.hompimpa.comfylearn.helper.SoundManager
import com.hompimpa.comfylearn.helper.setOnSoundClickListener
import kotlinx.coroutines.launch

class MathGameActivity : BaseActivity() {

    private lateinit var binding: ActivityMathGameBinding
    private val viewModel: MathGameViewModel by viewModels()

    private lateinit var currentDifficulty: String
    private lateinit var answerButtons: List<Button>

    companion object {
        const val EXTRA_SELECTED_DIFFICULTY = "com.hompimpa.comfylearn.SELECTED_DIFFICULTY"
        const val DIFFICULTY_EASY = "EASY"
        const val DIFFICULTY_MEDIUM = "MEDIUM"
        const val DIFFICULTY_HARD = "HARD"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMathGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentDifficulty = intent.getStringExtra(EXTRA_SELECTED_DIFFICULTY) ?: DIFFICULTY_MEDIUM
        answerButtons = listOf(
            binding.answerButton1,
            binding.answerButton2,
            binding.answerButton3,
            binding.answerButton4
        )

        setupUI()
        setupObservers()

        if (savedInstanceState == null) {
            viewModel.generateNewProblem(currentDifficulty)
        }
    }

    private fun setupUI() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        answerButtons.forEach { button ->
            button.setOnSoundClickListener {
                val answer = button.tag as? Int
                if (answer != null) {
                    viewModel.onAnswerSelected(answer)
                }
            }
        }
        binding.nextProblemButton.setOnSoundClickListener {
            viewModel.generateNewProblem(currentDifficulty)
        }
    }

    private fun setupObservers() {
        viewModel.problem.observe(this) { problem ->
            binding.problemTextView.text = problem.questionText
            binding.operatorTextView.text = problem.operator
            populateObjectsGrid(
                binding.firstOperandObjectsGrid,
                problem.op1Count,
                problem.imagePath
            )
            populateObjectsGrid(
                binding.secondOperandObjectsGrid,
                problem.op2Count,
                problem.imagePath
            )
        }

        viewModel.answerOptions.observe(this) { options ->
            applyDifficultySettings()
            resetButtonStates()
            answerButtons.filter { it.isVisible }.forEachIndexed { index, button ->
                button.text = options.choices[index].toString()
                button.tag = options.choices[index]
            }
        }

        viewModel.answerFeedback.observe(this) { feedback ->
            handleAnswerFeedback(feedback.wasCorrect, feedback.correctAnswer)
        }

        lifecycleScope.launch {
            viewModel.problemsSolvedFlow.collect { solvedCount ->
                supportActionBar?.subtitle = getString(R.string.problems_solved_format, solvedCount)
            }
        }
    }

    private fun handleAnswerFeedback(wasCorrect: Boolean, correctAnswer: Int) {
        answerButtons.forEach { it.isEnabled = false }
        val correctButton = answerButtons.find { (it.tag as? Int) == correctAnswer }

        if (wasCorrect) {
            SoundManager.playSound(SoundManager.Sound.CORRECT_ANSWER)
            correctButton?.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.feedback_positive_bg
                )
            )
            correctButton?.let { animateCorrectAnswer(it) }
            binding.nextProblemButton.isVisible = true
        } else {
            SoundManager.playSound(SoundManager.Sound.INCORRECT_ANSWER)
            val selectedButton = answerButtons.find { !it.isEnabled && it.tag != correctAnswer }
            selectedButton?.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.feedback_negative_bg
                )
            )
            selectedButton?.let { animateIncorrectAnswer(it) }

            Handler(Looper.getMainLooper()).postDelayed({
                correctButton?.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.feedback_positive_bg
                    )
                )
                binding.nextProblemButton.isVisible = true
            }, 1000)
        }
    }

    private fun populateObjectsGrid(gridLayout: GridLayout, count: Int, imagePath: String) {
        gridLayout.removeAllViews()
        if (count <= 0) return

        gridLayout.columnCount = when {
            count > 6 -> 3
            count > 1 -> 2
            else -> 1
        }
        val itemSize = resources.getDimensionPixelSize(R.dimen.arithmetic_item_size)
        val margin = resources.getDimensionPixelSize(R.dimen.arithmetic_item_margin)

        repeat(count) {
            val imageView = ImageView(this).apply {
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                setImageDrawable(
                    GameContentProvider.loadSvgFromAssets(
                        this@MathGameActivity,
                        imagePath
                    )
                )
                layoutParams = GridLayout.LayoutParams().apply {
                    width = itemSize
                    height = itemSize
                    setMargins(margin, margin, margin, margin)
                }
            }
            gridLayout.addView(imageView)
        }
    }

    private fun applyDifficultySettings() {
        binding.problemTextView.isVisible = currentDifficulty != DIFFICULTY_HARD
        val numChoices = when (currentDifficulty) {
            DIFFICULTY_EASY -> 2
            DIFFICULTY_MEDIUM -> 3
            else -> 4
        }
        answerButtons.forEachIndexed { index, button ->
            button.isVisible = index < numChoices
        }
    }

    private fun resetButtonStates() {
        answerButtons.forEach {
            it.isEnabled = true
            it.alpha = 1f
            it.setBackgroundColor(ContextCompat.getColor(this, R.color.button_default_bg))
        }
        binding.nextProblemButton.isVisible = false
    }

    private fun animateCorrectAnswer(button: Button) {
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(button, View.SCALE_X, 1f, 1.2f, 1f),
                ObjectAnimator.ofFloat(button, View.SCALE_Y, 1f, 1.2f, 1f)
            )
            interpolator = OvershootInterpolator()
            duration = 500
            start()
        }
    }

    private fun animateIncorrectAnswer(button: Button) {
        ObjectAnimator.ofFloat(button, View.TRANSLATION_X, 0f, 25f, -25f, 25f, -25f, 0f).apply {
            duration = 500
            start()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}