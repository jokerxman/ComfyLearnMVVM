package com.hompimpa.comfylearn.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivitySettingsBinding
import com.hompimpa.comfylearn.helper.setOnSoundClickListener
import com.hompimpa.comfylearn.ui.HomeViewModel
import com.hompimpa.comfylearn.ui.auth.LoginActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()
    private val authViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.menu_settings)

        setupLanguageSpinner()
        setupObservers()
        setupListeners()
    }

    private fun setupLanguageSpinner() {
        val languages =
            listOf(getString(R.string.language_english), getString(R.string.language_indonesian))
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerLanguage.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.isDarkMode.observe(this) { isDarkMode ->
            binding.switchTheme.isChecked = isDarkMode
        }

        viewModel.languageCode.observe(this) { langCode ->
            val position = if (langCode == "in") 1 else 0
            binding.spinnerLanguage.setSelection(position, false)
        }

        viewModel.finishActivityWithResult.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                Toast.makeText(this, getString(R.string.apply_changes), Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }

        viewModel.navigateToLogin.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            }
        }
    }

    private fun setupListeners() {
        binding.btnApply.setOnSoundClickListener {
            val isDarkMode = binding.switchTheme.isChecked
            val langCode = if (binding.spinnerLanguage.selectedItemPosition == 0) "en" else "id"
            viewModel.applyChanges(isDarkMode, langCode)
        }

        binding.btnSignOut.setOnSoundClickListener {
            authViewModel.onSignOutClicked()
        }
    }
}