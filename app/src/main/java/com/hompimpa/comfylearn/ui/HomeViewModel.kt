package com.hompimpa.comfylearn.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hompimpa.comfylearn.helper.Event
import com.hompimpa.comfylearn.ui.auth.LoginActivity
import com.hompimpa.comfylearn.ui.settings.SettingsActivity

data class NavigationEvent(
    val targetClass: Class<*>,
    val finishActivity: Boolean = false,
    val forResult: Boolean = false
)

class HomeViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    private val _navigationEvent = MutableLiveData<Event<NavigationEvent>>()
    val navigationEvent: LiveData<Event<NavigationEvent>> = _navigationEvent

    init {
        checkAuthenticationStatus()
    }

    private fun checkAuthenticationStatus() {
        val user = auth.currentUser
        _currentUser.value = user
        if (user == null) {
            navigateToLogin()
        }
    }

    fun onSettingsClicked() {
        _navigationEvent.value =
            Event(NavigationEvent(SettingsActivity::class.java, forResult = true))
    }

    fun onSignOutClicked() {
        auth.signOut()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        _navigationEvent.value =
            Event(NavigationEvent(LoginActivity::class.java, finishActivity = true))
    }
}