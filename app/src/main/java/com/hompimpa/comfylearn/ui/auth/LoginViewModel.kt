package com.hompimpa.comfylearn.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hompimpa.comfylearn.helper.AuthResult
import com.hompimpa.comfylearn.helper.Event
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {

    private val auth = Firebase.auth

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loginResult = MutableLiveData<Event<AuthResult>>()
    val loginResult: LiveData<Event<AuthResult>> = _loginResult

    private val _googleSignInRequest = MutableLiveData<Event<GetGoogleIdOption>>()
    val googleSignInRequest: LiveData<Event<GetGoogleIdOption>> = _googleSignInRequest

    fun onStart() {
        auth.currentUser?.let {
            _loginResult.value = Event(AuthResult.Success(it))
        }
    }

    fun loginWithEmail(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) {
            _loginResult.value = Event(AuthResult.Error("Email and password cannot be empty"))
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = auth.signInWithEmailAndPassword(email, pass).await()
                _loginResult.value = Event(AuthResult.Success(result.user!!))
            } catch (e: Exception) {
                val message = when (e) {
                    is FirebaseAuthInvalidUserException -> "Email not found"
                    is FirebaseAuthInvalidCredentialsException -> "Incorrect password"
                    else -> "Authentication failed"
                }
                _loginResult.value = Event(AuthResult.Error(message))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onGoogleSignInClicked(serverClientId: String) {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .build()
        _googleSignInRequest.value = Event(googleIdOption)
    }

    fun signInWithGoogleCredential(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                _loginResult.value = Event(AuthResult.Success(result.user!!))
            } catch (_: Exception) {
                _loginResult.value = Event(AuthResult.Error("Google sign-in failed"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}