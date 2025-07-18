package com.hompimpa.comfylearn.ui.study.number

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.commit
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityNumberBinding
import com.hompimpa.comfylearn.helper.BaseActivity
import com.hompimpa.comfylearn.helper.setOnSoundClickListener
import com.hompimpa.comfylearn.ui.HomeActivity
import com.hompimpa.comfylearn.ui.study.alphabet.AlphabetActivity

class NumberActivity : BaseActivity() {

    private lateinit var binding: ActivityNumberBinding
    private val viewModel: NumberViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val startNumber = intent.getIntExtra("number", 0)
        viewModel.setInitialNumber(startNumber)
        viewModel.markAsVisited()

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.nextButton.setOnSoundClickListener { viewModel.nextNumber() }
        binding.backButton.setOnSoundClickListener { viewModel.previousNumber() }
        binding.homeButton.setOnSoundClickListener { navigateToHome() }
        binding.changeButton.setOnSoundClickListener { navigateToOther() }
    }

    private fun observeViewModel() {
        viewModel.currentNumber.observe(this) { number ->
            loadNumberFragment(number)
        }
    }

    private fun loadNumberFragment(number: Int) {
        supportFragmentManager.commit {
            replace(R.id.fragment_container, NumberFragment.newInstance(number))
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun navigateToOther() {
        val intent = Intent(this, AlphabetActivity::class.java).putExtra("letter", 'A')
        startActivity(intent)
        finish()
    }
}