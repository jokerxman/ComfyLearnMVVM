package com.hompimpa.comfylearn.ui.study.alphabet

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.commit
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityAlphabetBinding
import com.hompimpa.comfylearn.helper.BaseActivity
import com.hompimpa.comfylearn.helper.setOnSoundClickListener
import com.hompimpa.comfylearn.ui.HomeActivity
import com.hompimpa.comfylearn.ui.study.number.NumberActivity

class AlphabetActivity : BaseActivity() {

    private lateinit var binding: ActivityAlphabetBinding
    private val viewModel: AlphabetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlphabetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val startLetter = intent.getCharExtra("letter", 'A')
        viewModel.setInitialLetter(startLetter)
        viewModel.markAsVisited()

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.nextButton.setOnSoundClickListener { viewModel.nextLetter() }
        binding.backButton.setOnSoundClickListener { viewModel.previousLetter() }
        binding.homeButton.setOnSoundClickListener { navigateToHome() }
        binding.changeButton.setOnSoundClickListener { navigateToOther() }
    }

    private fun observeViewModel() {
        viewModel.currentLetter.observe(this) { letter ->
            loadAlphabetFragment(letter)
        }
    }

    private fun loadAlphabetFragment(letter: Char) {
        supportFragmentManager.commit {
            replace(R.id.fragment_container, AlphabetFragment.newInstance(letter))
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun navigateToOther() {
        val intent = Intent(this, NumberActivity::class.java).putExtra("number", 1)
        startActivity(intent)
        finish()
    }
}