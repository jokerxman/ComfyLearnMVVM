package com.hompimpa.comfylearn.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.hompimpa.comfylearn.databinding.ActivityRegisterBinding
import com.hompimpa.comfylearn.helper.AuthResult
import com.hompimpa.comfylearn.helper.setOnSoundClickListener
import com.hompimpa.comfylearn.ui.HomeActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupObservers()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnSoundClickListener {
            val email = binding.edRegisterEmail.text.toString().trim()
            val password = binding.edRegisterPassword.text.toString().trim()
            val fullName = binding.edRegisterName.text.toString().trim()
            viewModel.registerUser(email, password, fullName)
        }
        binding.tvToLogin.setOnSoundClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.registrationResult.observe(this) { event ->
            event.getContentIfNotHandled()?.let { result ->
                when (result) {
                    is AuthResult.Success -> {
                        val intent = Intent(this, HomeActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                    }

                    is AuthResult.Error -> {
                        Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !isLoading
        binding.edRegisterEmail.isEnabled = !isLoading
        binding.edRegisterPassword.isEnabled = !isLoading
        binding.edRegisterName.isEnabled = !isLoading
    }
}