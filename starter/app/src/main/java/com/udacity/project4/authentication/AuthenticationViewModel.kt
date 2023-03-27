package com.udacity.project4.authentication

import androidx.lifecycle.ViewModel

class AuthenticationViewModel(private val authenticationDataSource: AuthenticationDataSource) :
    ViewModel() {
    val authenticationState = authenticationDataSource.authenticationState

    fun signOutOfAuth() {
        authenticationDataSource.signOut()
    }
}