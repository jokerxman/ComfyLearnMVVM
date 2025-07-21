package com.hompimpa.comfylearn.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityLoginBinding
import com.hompimpa.comfylearn.helper.AuthResult
import com.hompimpa.comfylearn.helper.setOnSoundClickListener
import com.hompimpa.comfylearn.ui.HomeActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupObservers()
    }

    public override fun onStart() {
        super.onStart()
        viewModel.onStart()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnSoundClickListener {
            val email = binding.edLoginEmail.text.toString().trim()
            val password = binding.edLoginPassword.text.toString().trim()
            viewModel.loginWithEmail(email, password)
        }
        binding.tvToRegister.setOnSoundClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
        binding.signInButton.setOnSoundClickListener {
            viewModel.onGoogleSignInClicked(getString(R.string.default_web_client_id))
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.loginResult.observe(this) { event ->
            event.getContentIfNotHandled()?.let { result ->
                when (result) {
                    is AuthResult.Success -> {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finishAffinity()
                    }

                    is AuthResult.Error -> {
                        Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        viewModel.googleSignInRequest.observe(this) { event ->
            event.getContentIfNotHandled()?.let { googleIdOption ->
                launchGoogleSignIn(googleIdOption)
            }
        }
    }

    private fun launchGoogleSignIn(googleIdOption: GetGoogleIdOption) {
        val credentialManager = CredentialManager.create(this)
        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(this@LoginActivity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                    viewModel.signInWithGoogleCredential(googleIdToken.idToken)
                } else {
                    showLoading(false)
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.error_generic_authentication),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (_: GetCredentialException) {
                showLoading(false)
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.error_google_sign_in_canceled),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (_: GoogleIdTokenParsingException) {
                showLoading(false)
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.error_generic_authentication),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.signInButton.isEnabled = !isLoading
        binding.edLoginEmail.isEnabled = !isLoading
        binding.edLoginPassword.isEnabled = !isLoading
    }
}