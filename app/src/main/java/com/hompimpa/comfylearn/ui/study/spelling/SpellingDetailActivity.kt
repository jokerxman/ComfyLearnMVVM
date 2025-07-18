package com.hompimpa.comfylearn.ui.study.spelling

import android.graphics.Bitmap
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivitySpellingDetailBinding
import com.hompimpa.comfylearn.helper.BaseActivity
import com.hompimpa.comfylearn.helper.SoundManager
import com.hompimpa.comfylearn.helper.setOnSoundClickListener

class SpellingDetailActivity : BaseActivity() {

    private lateinit var binding: ActivitySpellingDetailBinding
    private val viewModel: SpellingDetailViewModel by viewModels()

    private var categoryName: String? = null
    private var isConsonant: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpellingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryName = intent.getStringExtra("CATEGORY_NAME")
        isConsonant = intent.getBooleanExtra("IS_CONSONANT", false)

        setupActionBar()
        setupObservers()
        setupClickListeners()

        categoryName?.let {
            viewModel.loadCategory(it, isConsonant)
            viewModel.saveSpellingProgress(it)
        } ?: displayError("No category selected.")
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = categoryName?.replaceFirstChar { it.uppercase() }
    }

    private fun setupObservers() {
        viewModel.displayImage.observe(this) { bitmap -> displayImage(bitmap) }
        viewModel.displayItems.observe(this) { items -> setupItemTextViews(items) }
        viewModel.isNextButtonVisible.observe(this) { isVisible ->
            binding.nextWordButton.isVisible = isVisible
        }
        viewModel.errorState.observe(this) { message -> message?.let { displayError(it) } }
    }

    private fun setupClickListeners() {
        binding.nextWordButton.setOnSoundClickListener {
            categoryName?.let { viewModel.loadCategory(it, isConsonant) }
        }
    }

    private fun setupItemTextViews(items: List<String>) {
        binding.syllableContainer.removeAllViews()
        val isSyllable = items.size > 1

        items.forEach { itemText ->
            val textView = TextView(this).apply {
                text = itemText
                textSize = if (isSyllable) 24f else 30f
                gravity = Gravity.CENTER
                setBackgroundResource(R.drawable.syllable_background)
                setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
                setOnClickListener {
                    SoundManager.playSoundByName(this@SpellingDetailActivity, itemText)
                    if (isSyllable) {
                        viewModel.onSyllableTapped(itemText)
                    }
                }
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                val margin = if (isSyllable) dpToPx(4) else 0
                setMargins(margin, dpToPx(8), margin, 0)
            }
            binding.syllableContainer.addView(textView, params)
        }
        binding.syllableContainer.isVisible = true
    }

    private fun displayImage(bitmap: Bitmap?) {
        binding.itemImageView.isVisible = bitmap != null
        binding.itemImageView.setImageBitmap(bitmap)
    }

    private fun displayError(message: String) {
        binding.errorTextView.text = message
        binding.errorTextView.isVisible = true
        binding.itemImageView.isVisible = false
        binding.syllableContainer.removeAllViews()
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
        ).toInt()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}