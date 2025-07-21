package com.hompimpa.comfylearn.ui.games

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityDifficultySelectionBinding
import com.hompimpa.comfylearn.helper.setOnSoundClickListener

class DifficultySelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDifficultySelectionBinding
    private val viewModel: DifficultySelectionViewModel by viewModels()

    companion object {
        const val EXTRA_GAME_CATEGORY = "com.hompimpa.comfylearn.GAME_CATEGORY"
        const val EXTRA_GAME_TYPE = "com.hompimpa.comfylearn.GAME_TYPE"
        const val EXTRA_SELECTED_DIFFICULTY = "com.hompimpa.comfylearn.SELECTED_DIFFICULTY"

        const val DIFFICULTY_EASY = "EASY"
        const val DIFFICULTY_MEDIUM = "MEDIUM"
        const val DIFFICULTY_HARD = "HARD"

        fun newIntent(context: Context, category: String, gameType: String): Intent {
            return Intent(context, DifficultySelectionActivity::class.java).apply {
                putExtra(EXTRA_GAME_CATEGORY, category)
                putExtra(EXTRA_GAME_TYPE, gameType)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDifficultySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.select_difficulty)

        val category = intent.getStringExtra(EXTRA_GAME_CATEGORY)
        val gameType = intent.getStringExtra(EXTRA_GAME_TYPE)

        if (category == null || gameType == null) {
            Toast.makeText(this, "Error: Game data missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.init(category, gameType)
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.initialDifficulty.observe(this) { difficulty ->
            setSelectedDifficultyRadio(difficulty)
        }

        viewModel.finishWithResult.observe(this) { event ->
            event.getContentIfNotHandled()?.let { result ->
                val resultIntent = Intent().apply {
                    putExtra(EXTRA_GAME_TYPE, result.gameType)
                    putExtra(EXTRA_GAME_CATEGORY, result.category)
                    putExtra(EXTRA_SELECTED_DIFFICULTY, result.selectedDifficulty)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    private fun setupListeners() {
        binding.buttonConfirmDifficulty.setOnSoundClickListener {
            val selectedDifficulty = getSelectedDifficultyFromRadio()
            viewModel.onConfirmButtonClicked(selectedDifficulty)
        }
    }

    private fun setSelectedDifficultyRadio(difficulty: String?) {
        val buttonId = when (difficulty) {
            DIFFICULTY_EASY -> R.id.easyRadioButtonActivity
            DIFFICULTY_HARD -> R.id.hardRadioButtonActivity
            else -> R.id.mediumRadioButtonActivity
        }
        binding.difficultySelectorRadioGroupActivity.check(buttonId)
    }

    private fun getSelectedDifficultyFromRadio(): String {
        return when (binding.difficultySelectorRadioGroupActivity.checkedRadioButtonId) {
            R.id.easyRadioButtonActivity -> DIFFICULTY_EASY
            R.id.hardRadioButtonActivity -> DIFFICULTY_HARD
            else -> DIFFICULTY_MEDIUM
        }
    }
}