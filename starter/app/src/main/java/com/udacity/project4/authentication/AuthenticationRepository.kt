package com.udacity.project4.authentication

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.firebase.AuthenticationState
import com.udacity.project4.firebase.FirebaseUserLiveData

class AuthenticationRepository(private val app: Application) :
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

    override fun signOut() {
        AuthUI.getInstance().signOut(app)
    }
}