package com.hompimpa.comfylearn.helper

import com.google.firebase.auth.FirebaseUser

class Event<T>(private val content: T) {
    private var hasBeenHandled = false
    fun getContentIfNotHandled(): T? = if (hasBeenHandled) null else {
        hasBeenHandled = true
        content
    }
}

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}