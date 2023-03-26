package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.udacity.project4.firebase.AuthenticationState
import com.udacity.project4.firebase.FirebaseUserLiveData

class AuthenticationRepository : AuthenticationDataSource {
    override fun getAuthenticationState(): LiveData<AuthenticationState> =
        FirebaseUserLiveData().map { user ->
            if (user != null) {
                AuthenticationState.AUTHENTICATED
            } else {
                AuthenticationState.UNAUTHENTICATED
            }
        }
}