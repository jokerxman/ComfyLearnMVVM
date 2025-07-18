package com.hompimpa.comfylearn.ui.study.arithmetic

import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityArithmeticBinding
import com.hompimpa.comfylearn.helper.BaseActivity
import com.hompimpa.comfylearn.helper.GameContentProvider
import com.hompimpa.comfylearn.helper.setOnSoundClickListener

class ArithmeticActivity : BaseActivity() {

    private lateinit var binding: ActivityArithmeticBinding
    private val viewModel: ArithmeticViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArithmeticBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        binding.nextProblemButton.setOnSoundClickListener {
            viewModel.onNextProblemClicked()
        }
    }

    private fun setupObservers() {
        viewModel.progressState.observe(this) { state ->
            binding.levelNameText.text = state.levelName
            binding.levelProgressBar.max = state.maxProgress
            binding.levelProgressBar.progress = state.progress
        }

        viewModel.problemState.observe(this) { state ->
            binding.questionText.text = state.questionText
            binding.operatorTextView.text = state.operator
            populateObjectsGrid(binding.firstOperandObjectsGrid, state.op1Count, state.imagePath)
            populateObjectsGrid(binding.secondOperandObjectsGrid, state.op2Count, state.imagePath)
            populateObjectsGrid(binding.answerObjectsGrid, state.answerCount, state.imagePath)
        }

        viewModel.isGameComplete.observe(this) { isComplete ->
            if (isComplete) {
                Toast.makeText(this, getString(R.string.all_levels_completed), Toast.LENGTH_LONG)
                    .show()
                binding.nextProblemButton.isEnabled = false
            } else {
                binding.nextProblemButton.isEnabled = true
            }
        }

        viewModel.showToast.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateObjectsGrid(gridLayout: GridLayout, count: Int, imagePath: String) {
        gridLayout.removeAllViews()
        if (count <= 0) return

        gridLayout.columnCount = 2
        gridLayout.rowCount = when {
            count > 9 -> 4
            count > 4 -> 3
            else -> count.coerceAtLeast(2)
        }
        gridLayout.post {
            val gridWidth = gridLayout.width
            if (gridWidth == 0) return@post

            val itemMargin = resources.getDimensionPixelSize(R.dimen.arithmetic_item_margin)
            val numColumns = gridLayout.columnCount

            val itemSize = (gridWidth / numColumns) - (itemMargin * 2)

            repeat(count) {
                val imageView = ImageView(this).apply {
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                    setImageDrawable(
                        GameContentProvider.loadSvgFromAssets(
                            this@ArithmeticActivity,
                            imagePath
                        )
                    )
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = itemSize
                        height = itemSize
                        setMargins(itemMargin, itemMargin, itemMargin, itemMargin)
                    }
                }
                gridLayout.addView(imageView)
            }
        }
    }
}