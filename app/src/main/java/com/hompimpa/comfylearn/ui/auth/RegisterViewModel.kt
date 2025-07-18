package com.hompimpa.comfylearn.ui.auth

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.hompimpa.comfylearn.helper.AuthResult
import com.hompimpa.comfylearn.helper.Event
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

class RegisterViewModel : ViewModel() {

    private val auth = Firebase.auth

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _registrationResult = MutableLiveData<Event<AuthResult>>()
    val registrationResult: LiveData<Event<AuthResult>> = _registrationResult

    fun registerUser(email: String, pass: String, fullName: String) {
        if (fullName.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            _registrationResult.value = Event(AuthResult.Error("All fields must be filled."))
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _registrationResult.value = Event(AuthResult.Error("Invalid email format."))
            return
        }
        if (pass.length < 8) {
            _registrationResult.value =
                Event(AuthResult.Error("Password must be at least 8 characters."))
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                auth.setLanguageCode(Locale.getDefault().language)
                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val user = authResult.user!!

                val profileUpdates = userProfileChangeRequest { displayName = fullName }
                user.updateProfile(profileUpdates).await()

                _registrationResult.value = Event(AuthResult.Success(user))
            } catch (e: Exception) {
                val message = when (e) {
                    is FirebaseAuthUserCollisionException -> "This email is already registered."
                    is FirebaseAuthWeakPasswordException -> "Password is too weak."
                    else -> "Registration failed."
                }
                _registrationResult.value = Event(AuthResult.Error(message))
            } finally {
                _isLoading.value = false
            }
        }
    }
}