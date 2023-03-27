package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import com.udacity.project4.firebase.AuthenticationState

interface AuthenticationDataSource {

    var authenticationState: LiveData<AuthenticationState>

    fun checkAuthenticationState()

    fun signOut()
}