package com.udacity.project4

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.udacity.project4.authentication.AuthenticationDataSource
import com.udacity.project4.firebase.AuthenticationState

class FakeAuthenticationRepository(private val shouldAuthenticate: Boolean) :
    AuthenticationDataSource {
    override fun getAuthenticationState(): LiveData<AuthenticationState> {
        return if (shouldAuthenticate) {
            MutableLiveData(AuthenticationState.AUTHENTICATED)
        } else {
            MutableLiveData(AuthenticationState.UNAUTHENTICATED)
        }
    }
}