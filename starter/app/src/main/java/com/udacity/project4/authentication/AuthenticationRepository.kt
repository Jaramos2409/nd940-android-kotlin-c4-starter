package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.udacity.project4.firebase.AuthenticationState
import com.udacity.project4.firebase.FirebaseUserLiveData

class AuthenticationRepository :
    AuthenticationDataSource {

    override var authenticationState: LiveData<AuthenticationState> = MutableLiveData()

    init {
        checkAuthenticationState()
    }

    override fun checkAuthenticationState() {
        authenticationState =
            FirebaseUserLiveData().map { user ->
                if (user != null) {
                    AuthenticationState.AUTHENTICATED
                } else {
                    AuthenticationState.UNAUTHENTICATED
                }
            }
    }
}